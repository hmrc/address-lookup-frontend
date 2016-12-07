/*
 *
 *  * Copyright 2016 HM Revenue & Customs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

import it.helper.{AppServerUnderTest, Context, PSuites}
import it.suites._
import org.scalatest.{Args, SequentialNestedSuiteExecution, Status}
import org.scalatestplus.play.PlaySpec

class UnigrationTest extends PlaySpec with AppServerUnderTest with SequentialNestedSuiteExecution {

  def context: Context = this

  override def runNestedSuites(args: Args): Status = {
    val s = new PSuites(
      new KeystoreSuite(this)(app),
//      new UkSuite(this)(app),
      new IntSuite(this)(app),
      new PingSuite(this)(app)
    )
    s.runNestedSuites(args)
  }

}

