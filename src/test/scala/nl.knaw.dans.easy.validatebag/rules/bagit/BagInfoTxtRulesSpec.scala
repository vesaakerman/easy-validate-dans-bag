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
package nl.knaw.dans.easy.validatebag.rules.bagit

import nl.knaw.dans.easy.validatebag.TestSupportFixture

class BagInfoTxtRulesSpec extends TestSupportFixture {
  "bagMustContainBagInfoTxt" should "fail if bag-info.txt is not found" in {
    testRuleViolation(bagMustContainBagInfoTxt, "missing-bag-info.txt", "bag-info.txt")
  }

  "bagInfoTxtMayContainOne(\"ELEMENT\")" should "fail if bag-info.txt contains two ELEMENT elements" in {
    testRuleViolation(bagInfoTxtMayContainOne("ELEMENT"), "two-many-ELEMENT-in-bag-info.txt", "may contain at most one")
  }

  "bagInfoTxtOptionalElementMustHaveValue(\"ELEMENT\", \"VALUE\")" should "succeed if ELEMENT exists has value VALUE" in {
    testRuleSuccess(bagInfoTxtOptionalElementMustHaveValue("ELEMENT", "VALUE"), "one-ELEMENT-VALUE-in-bag-info")
  }

  "bagInfoTxtMustContainCreated" should "fail if 'Created' is missing in bag-info.txt" in {
    testRuleViolation(bagInfoTxtMustContainCreated, "missing-Created", "Created")
  }

  "bagInfoTxtCreatedMustBeIsoDate" should "fail if 'Created' is lacking time and time zone" in {
    testRuleViolation(bagInfoTxtCreatedMustBeIsoDate, "missing-time-and-timezone-in-Created", "not in correct ISO 8601 format")
  }

  it should "fail if incorrect date format" in {
    testRuleViolation(bagInfoTxtCreatedMustBeIsoDate, "non-ISO8601-in-Created", "not in correct ISO 8601 format")
  }

  it should "fail if no millisecond precision provided" in {
    testRuleViolation(bagInfoTxtCreatedMustBeIsoDate, "no-millisecond-precision-in-Created", "not in correct ISO 8601 format")
  }
}
