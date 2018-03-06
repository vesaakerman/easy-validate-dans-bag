/**
 * Copyright (C) 2018 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.validatebag

import java.net.URI

import nl.knaw.dans.easy.validatebag.InfoPackageType._
import nl.knaw.dans.easy.validatebag.ValidationResult._

class ResultMessageSpec extends TestSupportFixture {

  "toPlainText" should "contain violations if present in message object" in {
    val text = ResultMessage(new URI("file://path/to/file.txt"), "file.txt", SIP, NOT_COMPLIANT, Some(Seq("1" -> "Wrong", "2" -> "Even worse")))
      .toPlainText
    debug(s"Message:\n$text")
    text should include("rule_violations:\n")
    text should include("Wrong")
    text should include("worse")
  }

  it should "not contain violations if no present" in {
    val text = ResultMessage(new URI("file://path/to/file.txt"), "file.txt", SIP, NOT_COMPLIANT)
      .toPlainText
    debug(s"Message:\n$text")
    text shouldNot include("rule_violations:\n")
  }

  "toJson" should "contain violations if present in message object" in {
    val json = ResultMessage(new URI("file://path/to/file.txt"), "file.txt", SIP, NOT_COMPLIANT, Some(Seq("1" -> "Wrong", "2" -> "Even worse")))
      .toJson
    debug(s"Message:\n$json")
    json should include("\"ruleViolations\"")
    json should include("Wrong")
    json should include("worse")
  }

  it should "not contain violations if no present" in {
    val json = ResultMessage(new URI("file://path/to/file.txt"), "file.txt", SIP, NOT_COMPLIANT)
      .toJson
    debug(s"Message:\n$json")
    json shouldNot include("\"ruleViolations\"")
  }


}
