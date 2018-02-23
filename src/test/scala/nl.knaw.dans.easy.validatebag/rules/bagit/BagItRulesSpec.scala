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

class BagItRulesSpec extends TestSupportFixture {
  "bagMustContainBagInfoTxt" should "fail if bag-info.txt is not found" in {
    testRule(bagMustContainBagInfoTxt, "missingBagInfo", "bag-info.txt")
  }

  "bagInfoTxtMustContainCreated" should "fail if 'Created' is missing in bag-info.txt" in {
    testRule(bagInfoTxtMustContainCreated, "missingCreated", "Created")
  }

  "bagInfoTxtCreatedMustBeIsoDate" should "fail if 'Created' is lacking time and time zone" in {
    testRule(bagInfoTxtCreatedMustBeIsoDate, "missingTimeAndTimeZoneInCreated", "not in correct ISO 8601 format")
  }

  it should "fail if no millisecond precision provided" in {
    testRule(bagInfoTxtCreatedMustBeIsoDate, "nonISO8106inCreated", "not in correct ISO 8601 format")
  }

  it should "fail if incorrect date format" in {
    testRule(bagInfoTxtCreatedMustBeIsoDate, "nonMillisecondPrecisionInCreated", "not in correct ISO 8601 format")
  }





  //  "bagInfoTxtMustContainBagItProfileVersionV1" should "fail if 'BagIt-Profile-Version' does not exist in bag-info.txt" in {
  //    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfMissingBagItProfileVersion)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if 'BagIt-Profile-Version' in bag-info.txt is not 1.0.0 " in {
  //    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfWrongBagItProfileVersion)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if nbr of the keys named 'BagIt-Profile-Version' in bag-info.txt is not exactly 1" in {
  //    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfSeveralBagItProfileVersionsV1)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if no value is provided for 'BagIt-Profile-Version' " in {
  //    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfBagItProfileVersionWithNoValue)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  "bagInfoTxtMayContainBagItProfileVersionV0" should "fail if 'BagIt-Profile-Version' in bag-info.txt exists but not equal to 0.0.0 " in {
  //    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfWrongBagItProfileVersion)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if 'BagIt-Profile-Version' exists but no value is provided " in {
  //    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfBagItProfileVersionWithNoValue)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if nbr of the keys named 'BagIt-Profile-Version' in bag-info.txt is greater than 1" in {
  //    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfSeveralBagItProfileVersionsV0)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  "bagInfoTxtMustContainBagItProfileURIV1" should "fail if 'BagIt-Profile-URI' does not exist in bag-info.txt" in {
  //    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfMissingBagItProfileURI)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if 'BagIt-Profile-URI in bag-info.txt is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' " in {
  //    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfWrongBagItProfileURI)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if nbr of the keys named 'BagIt-Profile-URI' in bag-info.txt is not exactly 1" in {
  //    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfSeveralBagItProfileURIs)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if no value is provided for 'BagIt-Profile-URI' " in {
  //    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfBagItProfileURIwithNoValue)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  "bagInfoTxtMayContainBagItProfileURIV0" should "fail if 'BagIt-Profile-URI' in bag-info.txt exists but not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' " in {
  //    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfWrongBagItProfileURI)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if 'BagIt-Profile-URI' exists but no value is provided " in {
  //    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfBagItProfileURIwithNoValue)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if nbr of the keys named 'BagIt-Profile-URI' in bag-info.txt is greater than 1" in {
  //    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfSeveralBagItProfileURIs)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  "bagInfoTxtMustContainCreated" should "fail if 'Created' is missing in bag-info.txt" in {
  //    val result = bagInfoTxtMustContainCreated(testDirOfMissingCreated)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if the value of 'Created' does not violate ISO806 format but is not in combined date and time format in bag-info.txt. Note that in this case the default time 00:00:00 or default timezone EUROPE/Amsterdam can still be retrieved by DateTime function! " in {
  //    val result = bagInfoTxtMustContainCreated(testDirOfMissingTimeAndTimeZoneInCreated)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if the value of 'Created' is not in ISO806 format in bag-info.txt." in {
  //    val result = bagInfoTxtMustContainCreated(testDirOfViolatedISO806)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "fail if the value of 'Created' is not equal to last modified time of the bag" in {
  //    val result = bagInfoTxtMustContainCreated(testDirOfLastModTimeNotEqualToCreated)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  "bagMustContainSHA1" should "fail if 'manifest-sha1.txt' is not found in the bag" in {
  //    val result = bagMustContainSHA1(testDirOfNonexistingManifestSHA1)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
  //
  //  it should "succeed if 'manifest-sha1.txt' is correct" in {
  //    bagMustContainSHA1(testDirOfExistingManifestSHA1) shouldBe a[Success[_]]
  //  }
  //
  //  it should " fail if one of the payload manifests is missing in 'manifest-sha1.txt' " in {
  //    val result = bagMustContainSHA1(testDirOfExistingManifestSHA1missingPayloadEntries)
  //    result shouldBe a[Failure[_]]
  //    inside(result) {
  //      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
  //    }
  //  }
}
