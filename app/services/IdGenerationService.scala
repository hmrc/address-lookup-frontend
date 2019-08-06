package services

import java.util.UUID

import javax.inject.{Inject, Singleton}

@Singleton
class IdGenerationService @Inject()() {
  //Injectable to allow for mocking in component integration tests
  def uuid: String = UUID.randomUUID().toString
}
