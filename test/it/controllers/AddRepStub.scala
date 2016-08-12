/*
 * Copyright 2016 HM Revenue & Customs
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

import address.uk.AddressRecord
import com.pyruby.stubserver._
import config.JacksonMapper

class AddRepStub {

  private val stub = new StubServer()

  def clearExpectations() {
    stub.clearExpectations()
  }

  def start() {
    stub.start()
  }

  def stop() {
    stub.stop()
  }

  def endpoint = s"http://localhost:${stub.getLocalPort}"

  def givenAddressResponse(url: String, ra: List[AddressRecord]) {
    val json = JacksonMapper.writeValueAsString(ra)
    stub.expect(StubMethod.get(url)).thenReturn(200, "application/json", json)
  }

  def givenAddressError(url: String, status: Int, message: String) {
    stub.expect(StubMethod.get(url)).thenReturn(status, "text/plain", message)
  }

}
