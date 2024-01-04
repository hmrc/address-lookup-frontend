/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import address.v2.Country
import config.FrontendAppConfig
import controllers.countOfResults._
import forms.ALFForms._
import model._
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.Json
import play.api.mvc._
import services.{AddressService, CountryService, JourneyRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, EventTypes}
import views.html.international.{edit, select}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class InternationalAddressLookupController @Inject()(
                                                      journeyRepository: JourneyRepository,
                                                      addressService: AddressService,
                                                      countryService: CountryService,
                                                      auditConnector: AuditConnector,
                                                      implicit val frontendAppConfig: FrontendAppConfig,
                                                      messagesControllerComponents: MessagesControllerComponents,
                                                      remoteMessagesApiProvider: RemoteMessagesApiProvider,
                                                      select: select,
                                                      edit: edit,
                                                      confirm: views.html.international.confirm,
                                                      no_results: views.html.international.no_results,
                                                      too_many_results: views.html.international.too_many_results,
                                                      lookup: views.html.international.lookup
                                                    )(override implicit val ec: ExecutionContext)
  extends AlfController(journeyRepository, messagesControllerComponents) {

  private def countries(welshFlag: Boolean = false): Seq[Country] =
    countryService.findAll(welshFlag)

  def lookup(id: String, filter: Option[String]): Action[AnyContent] = Action.async {
    implicit req =>
      journeyRepository.getV2(id).map {
        case Some(journeyData) =>
          import LanguageLabelsForMessages._

          val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
            journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

          implicit val messages: Messages = remoteMessagesApi.preferred(req)

          val isWelsh = getWelshContent(journeyData)
          implicit val permittedLangs: Seq[Lang] =
            if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

          val formPrePopped = nonAbpLookupForm()(messages).fill(NonAbpLookup(filter.getOrElse("")))

          requestWithWelshHeader(isWelsh) {
            Ok(lookup(id, journeyData, formPrePopped, isWelsh)
            (req, messages, frontendAppConfig))
          }

        case None => Redirect(routes.AddressLookupController.noJourney())
      }
  }

  def postLookup(id: String) = Action.async {
    implicit req =>
      journeyRepository.getV2(id).map {
        case Some(journeyData) =>

          import LanguageLabelsForMessages._

          val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
            journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

          implicit val messages: Messages = remoteMessagesApi.preferred(req)

          val isWelsh = getWelshContent(journeyData)

          nonAbpLookupForm
            .bindFromRequest()
            .fold(
              errors => requestWithWelshHeader(isWelsh) {
                BadRequest(lookup(id, journeyData, errors, isWelsh))
              },
              lookup => Redirect(routes.InternationalAddressLookupController.select(id, lookup.filter))
            )
      }
  }

  def select(id: String, filter: String): Action[AnyContent] = Action.async { implicit req =>
    withFutureJourneyV2(id) { journeyData =>
      import LanguageLabelsForMessages._

      val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
        journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

      implicit val messages: Messages = remoteMessagesApi.preferred(req)

      val isWelsh = getWelshContent(journeyData)

      handleLookup(id, journeyData, filter) map {
        case OneResult(address) =>
          val journeyDataWithSelectedAddress = journeyData.copy(
            selectedAddress = Some(address.toConfirmableAddress(id))
          )

          Some(journeyDataWithSelectedAddress) -> requestWithWelshHeader(isWelsh) {
            Redirect(routes.InternationalAddressLookupController.confirm(id))
          }
        case ResultsList(addresses, firstLookup) =>
          val journeyDataWithProposals = journeyData.copy(proposals = Some(addresses))

          Some(journeyDataWithProposals) -> requestWithWelshHeader(isWelsh) {
            Ok(select(id, journeyData, selectForm(), Proposals(Some(addresses)), filter, firstLookup, isWelsh))
          }
        case TooManyResults(_, firstLookup) =>
          None -> requestWithWelshHeader(isWelsh) {
            Ok(too_many_results(id, journeyData, filter, firstLookup, isWelsh))
          }
        case NoResults =>
          None -> requestWithWelshHeader(isWelsh) {
            Ok(no_results(id, journeyData, filter, isWelsh))
          }
      }

    }
  }

  def handleLookup(id: String,
                   journeyData: JourneyDataV2,
                   filter: String,
                   firstLookup: Boolean = true
                  )(implicit hc: HeaderCarrier): Future[ResultsCount] = {

    val addressLimit = journeyData.config.options.selectPageConfig
      .getOrElse(SelectPageConfig())
      .proposalListLimit

    addressService
      .findByCountry(journeyData.countryCode.get, filter)
      .flatMap {
        case noneFound if noneFound.isEmpty =>
          Future.successful(NoResults)
        case oneFound if oneFound.size == 1 =>
          Future.successful(OneResult(oneFound.head))
        case tooManyFound
          if tooManyFound.size > addressLimit.getOrElse(tooManyFound.size) =>
          Future.successful(
            TooManyResults(tooManyFound.take(addressLimit.get), firstLookup)
          )
        case displayProposals =>
          Future.successful(ResultsList(displayProposals, firstLookup))
      }
  }

  // TODO enable journey-configurable limit on proposal list size
  // POST /:id/select
  def handleSelect(id: String, filter: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val lang: Lang = if (isWelsh) Lang("cy") else Lang("en")

        val bound = selectForm().bindFromRequest()

        bound.fold(
          errors => {
            (None -> requestWithWelshHeader(isWelsh) {
              BadRequest(
                select(
                  id,
                  journeyData,
                  errors,
                  Proposals(journeyData.proposals),
                  filter,
                  firstSearch = true,
                  isWelsh = isWelsh
                )
              )
            })
          },
          selection => {
            journeyData.proposals match {
              case Some(props) => {
                props.find(_.addressId == selection.addressId) match {
                  case Some(addr) =>
                    val journeyDataWithConfirmableAddress = journeyData.copy(
                      selectedAddress = Some(addr.toConfirmableAddress(id))
                    )
                    (
                      Some(journeyDataWithConfirmableAddress),
                      requestWithWelshHeader(isWelsh) {
                        Redirect(routes.InternationalAddressLookupController.confirm(id))
                      }
                    )
                  case None =>
                    (None, requestWithWelshHeader(isWelsh) {
                      BadRequest(
                        select(
                          id,
                          journeyData,
                          bound,
                          Proposals(Some(props)),
                          filter,
                          firstSearch = true,
                          isWelsh = isWelsh
                        )
                      )
                    })
                }
              }
              case None =>
                (None, Redirect(routes.InternationalAddressLookupController.lookup(id, Some(filter))))
            }
          }
        )
      }
  }

  // GET  /:id/edit
  def edit(id: String): Action[AnyContent] =
    Action.async { implicit req =>
      withJourneyV2(id) { journeyData => {
        import LanguageLabelsForMessages._

        val allowedSeqCountries = (cs: Seq[Country]) =>
          allowedCountries(cs, journeyData.config.options.allowedCountryCodes)

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] = if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        val defaultAddress = addressOrEmpty(journeyData.selectedAddress, journeyData.countryCode.getOrElse(""))
        (None, requestWithWelshHeader(isWelsh) {
          Ok(
            edit(
              id,
              journeyData,
              nonUkEditForm().fill(defaultAddress),
              allowedSeqCountries(countries(isWelsh)),
              isWelsh = isWelsh
            )
          )
        })
      }
      }
    }

  private[controllers] def addressOrEmpty(oAddr: Option[ConfirmableAddress],
                                          lookupCountryCode: String): Edit =
    oAddr
      .map(_.toEdit)
      .getOrElse(
        Edit(None,
          None, None, None, None, ""))
      .copy(countryCode = lookupCountryCode)

  // POST /:id/edit
  def handleEdit(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData => {
        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] =
          if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        val validatedForm =
          isValidPostcode(nonUkEditForm().bindFromRequest())

        validatedForm.fold(
          errors => {
            (None, requestWithWelshHeader(isWelsh) {
              BadRequest(
                edit(
                  id,
                  journeyData,
                  errors,
                  allowedCountries(
                    countries(isWelsh),
                    journeyData.config.options.allowedCountryCodes
                  ),
                  isWelsh = isWelsh
                )
              )
            })
          },
          edit =>
            (
              Some(
                journeyData.copy(
                  selectedAddress = Some(edit.toConfirmableAddress(id))
                )
              ),
              requestWithWelshHeader(isWelsh) {
                Redirect(routes.InternationalAddressLookupController.confirm(id))
              }
            )
        )
      }
      }
  }

  // GET  /:id/confirm
  def confirm(id: String): Action[AnyContent] = Action.async { implicit req =>
    withJourneyV2(id) { journeyData => {
      import LanguageLabelsForMessages._

      val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
        journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

      implicit val messages: Messages = remoteMessagesApi.preferred(req)

      val isWelsh = getWelshContent(journeyData)
      implicit val permittedLangs: Seq[Lang] =
        if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

      val isUKMode = journeyData.config.options.isUkMode

      journeyData.selectedAddress
        .map(
          _ =>
            (None, requestWithWelshHeader(isWelsh) {
              Ok(
                confirm(
                  id,
                  journeyData,
                  journeyData.selectedAddress,
                  isWelsh,
                  isUKMode
                )
              )
            })
        )
        .getOrElse((None, requestWithWelshHeader(isWelsh) {
          Redirect(routes.InternationalAddressLookupController.lookup(id, None))
        }))
    }
    }
  }

  // POST /:id/confirm
  def handleConfirm(id: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData => {
        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] =
          if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        if (journeyData.selectedAddress.isDefined) {
          val jd =
            journeyData.copy(confirmedAddress = journeyData.selectedAddress)

          auditConnector.sendEvent(
            new DataEvent(
              "address-lookup-frontend",
              EventTypes.Succeeded,
              tags = hc.toAuditTags("ConfirmAddress", req.uri),
              detail = Map(
                "auditRef" -> id,
                "confirmedAddress" -> jd.confirmedAddress.get.toDescription,
                "confirmedAddressId" -> jd.confirmedAddress.get.id
                  .getOrElse("-")
              )
            )
          )

          (Some(jd), requestWithWelshHeader(isWelsh) {
            Redirect(urlWithQuery(journeyData.config.options.continueUrl, s"id=$id").toString)
          })
        } else {
          (None, requestWithWelshHeader(isWelsh) {
            Redirect(routes.InternationalAddressLookupController.confirm(id))
          })
        }
      }
      }
  }
}
