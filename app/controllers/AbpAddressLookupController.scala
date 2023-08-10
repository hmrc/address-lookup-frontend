/*
 * Copyright 2023 HM Revenue & Customs
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
import utils.PostcodeHelper
import views.html.abp.{lookup, non_uk_mode_edit, select, uk_mode_edit}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AbpAddressLookupController @Inject()(
                                            journeyRepository: JourneyRepository,
                                            addressService: AddressService,
                                            auditConnector: AuditConnector,
                                            implicit val frontendAppConfig: FrontendAppConfig,
                                            messagesControllerComponents: MessagesControllerComponents,
                                            remoteMessagesApiProvider: RemoteMessagesApiProvider,
                                            countryService: CountryService,
                                            lookup: lookup,
                                            select: select,
                                            uk_mode_edit: uk_mode_edit,
                                            non_uk_mode_edit: non_uk_mode_edit,
                                            confirm: views.html.abp.confirm,
                                            no_results: views.html.abp.no_results,
                                            too_many_results: views.html.abp.too_many_results
                                          )(override implicit val ec: ExecutionContext)
  extends AlfController(journeyRepository, messagesControllerComponents) {

  private def countries(welshFlag: Boolean = false): Seq[Country] =
    countryService.findAll(welshFlag)

  // GET  /:id/lookup
  def lookup(id: String, postcode: Option[String] = None, filter: Option[String] = None): Action[AnyContent] = Action.async {
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

          val isUKMode = journeyData.config.options.isUkMode
          val formPrePopped = lookupForm(isUKMode)(messages).fill(
            Lookup(filter, PostcodeHelper.displayPostcode(postcode))
          )

          requestWithWelshHeader(isWelsh) {
            Ok(lookup(id, journeyData, formPrePopped, isWelsh, isUKMode)
            (req, messages, frontendAppConfig))
          }

        case None => Redirect(routes.AddressLookupController.noJourney())
      }
  }

  // POST  /:id/lookup
  def postLookup(id: String): Action[AnyContent] = Action.async {
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

          val isUKMode = journeyData.config.options.isUkMode

          lookupForm(isUKMode)
            .bindFromRequest()
            .fold(
              errors => requestWithWelshHeader(isWelsh) {
                BadRequest(lookup(id, journeyData, errors, isWelsh, isUKMode))
              },
              lookup => Redirect(routes.AbpAddressLookupController.select(id, lookup.postcode, lookup.filter)))

        case None => Redirect(routes.AddressLookupController.noJourney())
      }
  }

  // GET  /:id/select
  def select(id: String, postcode: String, filter: Option[String] = None): Action[AnyContent] = Action.async { implicit req =>
    withFutureJourneyV2(id) { journeyData =>
      import LanguageLabelsForMessages._

      val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
        journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

      implicit val messages: Messages = remoteMessagesApi.preferred(req)

      val isWelsh = getWelshContent(journeyData)

      val isUKMode = journeyData.config.options.isUkMode

      val formattedPostcode = PostcodeHelper.displayPostcode(postcode)

      handleLookup(id, journeyData, postcode, filter) map {
        case OneResult(address) =>
          val journeyDataWithSelectedAddress = journeyData.copy(
            selectedAddress = Some(address.toConfirmableAddress(id))
          )

          Some(journeyDataWithSelectedAddress) -> requestWithWelshHeader(isWelsh) {
            Redirect(routes.AbpAddressLookupController.confirm(id))
          }
        case ResultsList(addresses, firstLookup) =>
          val journeyDataWithProposals = journeyData.copy(proposals = Some(addresses))

          Some(journeyDataWithProposals) -> requestWithWelshHeader(isWelsh) {
            Ok(select(id, journeyData, selectForm(), Proposals(Some(addresses)), formattedPostcode, filter,
              firstLookup, isWelsh, isUKMode))
          }
        case TooManyResults(_, firstLookup) =>
          None -> requestWithWelshHeader(isWelsh) {
            Ok(too_many_results(id, journeyData, formattedPostcode, filter, firstLookup, isWelsh, isUKMode))
          }
        case NoResults =>
          None -> requestWithWelshHeader(isWelsh) {
            Ok(no_results(id, journeyData, formattedPostcode, isWelsh, isUKMode))
          }
      }
    }
  }

  private[controllers] def handleLookup(
                                         id: String,
                                         journeyData: JourneyDataV2,
                                         postcode: String,
                                         filter: Option[String],
                                         firstLookup: Boolean = true
                                       )(implicit hc: HeaderCarrier): Future[ResultsCount] = {

    val addressLimit = journeyData.config.options.selectPageConfig
      .getOrElse(SelectPageConfig())
      .proposalListLimit

    addressService
      .find(postcode, filter, journeyData.config.options.isUkMode)
      .flatMap {
        case noneFound if noneFound.isEmpty =>
          if (filter.isDefined) {
            handleLookup(
              id: String,
              journeyData,
              postcode,
              None,
              firstLookup = false
            ) //TODO Pass a boolean through to show no results were found and this is a retry?
          } else {
            Future.successful(NoResults)
          }
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
  def handleSelect(id: String,
                   filter: Option[String],
                   postcode: String): Action[AnyContent] = Action.async {
    implicit req =>
      withJourneyV2(id) { journeyData =>
        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val lang: Lang = if (isWelsh) Lang("cy") else Lang("en")

        val isUKMode = journeyData.config.options.isUkMode
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
                  postcode,
                  filter,
                  firstSearch = true,
                  isWelsh = isWelsh,
                  isUKMode = isUKMode
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
                        Redirect(routes.AbpAddressLookupController.confirm(id))
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
                          postcode,
                          filter,
                          firstSearch = true,
                          isWelsh = isWelsh,
                          isUKMode = isUKMode
                        )
                      )
                    })
                }
              }
              case None =>
                (None, Redirect(routes.AbpAddressLookupController.lookup(id)))
            }
          }
        )
      }
  }

  // GET  /:id/edit
  def edit(id: String, lookUpPostCode: Option[String]): Action[AnyContent] =
    Action.async { implicit req =>
      withJourneyV2(id) { journeyData => {

        val allowedSeqCountries = (cs: Seq[Country]) =>
          allowedCountries(cs, journeyData.config.options.allowedCountryCodes)

        import LanguageLabelsForMessages._

        val remoteMessagesApi = remoteMessagesApiProvider.getRemoteMessagesApi(
          journeyData.config.labels.map(ls => Json.toJsObject(ls)).orElse(Some(Json.obj())))

        implicit val messages: Messages = remoteMessagesApi.preferred(req)

        val isWelsh = getWelshContent(journeyData)
        implicit val permittedLangs: Seq[Lang] =
          if (isWelsh) Seq(Lang("cy")) else Seq(Lang("en"))

        val isUKMode = journeyData.config.options.isUkMode

        if (isUKMode) {
          val editAddress = addressOrDefault(journeyData.selectedAddress, lookUpPostCode)

          (None, requestWithWelshHeader(isWelsh) {
            Ok(
              uk_mode_edit(
                id,
                journeyData,
                ukEditForm().fill(editAddress),
                allowedSeqCountries(Seq.empty),
                isWelsh,
                isUKMode
              )
            )
          })
        } else {
          val defaultAddress = addressOrEmpty(journeyData.selectedAddress, lookUpPostCode, journeyData.countryCode)
          (None, requestWithWelshHeader(isWelsh) {
            Ok(
              non_uk_mode_edit(
                id,
                journeyData,
                nonUkEditForm().fill(defaultAddress),
                allowedSeqCountries(countries(isWelsh)),
                isWelsh = isWelsh,
                isUKMode = isUKMode
              )
            )
          })
        }
      }
      }
    }

  private[controllers] def addressOrDefault(oAddr: Option[ConfirmableAddress],
                                            lookUpPostCode: Option[String] =
                                            None): Edit = {
    oAddr
      .map(_.toEdit)
      .getOrElse(
        Edit(
          None,
          None,
          None,
          None,
          None,
          PostcodeHelper.displayPostcode(lookUpPostCode),
          "GB"
        )
      )
  }

  private[controllers] def addressOrEmpty(
                                           oAddr: Option[ConfirmableAddress],
                                           lookUpPostCode: Option[String] = None,
                                           lookupCountryCode: Option[String] = None
                                         ): Edit = {
    oAddr
      .map(_.toEdit)
      .getOrElse(
        Edit(
          None,
          None,
          None,
          None,
          None,
          PostcodeHelper.displayPostcode(lookUpPostCode),
          lookupCountryCode.getOrElse("")
        )
      )
  }

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

        val isUKMode = journeyData.config.options.isUkMode
        if (isUKMode) {
          val validatedForm = isValidPostcode(ukEditForm().bindFromRequest())

          validatedForm.fold(
            errors =>
              (None, requestWithWelshHeader(isWelsh) {
                BadRequest(
                  uk_mode_edit(
                    id,
                    journeyData,
                    errors,
                    allowedCountries(
                      countries(isWelsh),
                      journeyData.config.options.allowedCountryCodes
                    ),
                    isWelsh,
                    isUKMode
                  )
                )
              }),
            edit =>
              (
                Some(
                  journeyData.copy(
                    selectedAddress = Some(edit.toConfirmableAddress(id))
                  )
                ),
                requestWithWelshHeader(isWelsh) {
                  Redirect(routes.AbpAddressLookupController.confirm(id))
                }
              )
          )
        } else {
          val validatedForm =
            isValidPostcode(nonUkEditForm().bindFromRequest())

          validatedForm.fold(
            errors => {

              //val pretendErrors =

              (None, requestWithWelshHeader(isWelsh) {
                BadRequest(
                  non_uk_mode_edit(
                    id,
                    journeyData,
                    errors,
                    allowedCountries(
                      countries(isWelsh),
                      journeyData.config.options.allowedCountryCodes
                    ),
                    isWelsh = isWelsh,
                    isUKMode = isUKMode
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
                  Redirect(routes.AbpAddressLookupController.confirm(id))
                }
              )
          )
        }
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
          Redirect(routes.AbpAddressLookupController.lookup(id))
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
            Redirect(routes.AbpAddressLookupController.confirm(id))
          })
        }
      }
      }
  }
}
