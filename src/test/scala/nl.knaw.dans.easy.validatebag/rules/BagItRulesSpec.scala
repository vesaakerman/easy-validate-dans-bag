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


import java.nio.file.Paths

import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.RuleViolationDetailsException
import org.apache.commons.configuration.PropertiesConfiguration
//EasyValidateDansBagApp

import nl.knaw.dans.easy.validatebag.TestSupportFixture
import nl.knaw.dans.easy.validatebag.rules.bagit

import scala.util.Failure


class BagItRulesSpec extends TestSupportFixture {

   val testDirOfMissingBagInfo: BagDir = Paths.get("src/test/resources/bags/missingBagInfo")
   val testDirOfMissingBagItProfileVersion: BagDir = Paths.get("src/test/resources/bags/minimal")
   val testDirOfWrongBagItProfileVersion: BagDir = Paths.get("src/test/resources/bags/wrongBagItProfileVersion")
   val testDirOfMissingBagItProfileURI: BagDir = Paths.get("src/test/resources/bags/minimal")
   val testDirOfWrongBagItProfileURI: BagDir = Paths.get("src/test/resources/bags/wrongBagItProfileURI")


  "bagMustContainBagInfoTxt" should "fail if bag-info.txt is not found" in {

    val result = bagMustContainBagInfoTxt(testDirOfMissingBagInfo)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  "bagInfoTxtMustContainBagItProfileVersion" should "fail if 'BagIt-Profile-Version' does not exist in bag-info.txt" in {

    val result = bagInfoTxtMustContainBagItProfileVersion(testDirOfMissingBagItProfileVersion)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if 'BagIt-Profile-Version' in bag-info.txt is neither 1.0.0 nor 0.0.0" in {

    val result = bagInfoTxtMustContainBagItProfileVersion(testDirOfWrongBagItProfileVersion)
    result shouldBe a[Failure[_]]
    inside(result) {
     case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  "bagInfoTxtMustContainBagItProfileURI" should "fail if 'BagIt-Profile-URI' does not exist in bag-info.txt" in {

    val result = bagInfoTxtMustContainBagItProfileURI(testDirOfMissingBagItProfileURI)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if 'BagIt-Profile-URI in bag-info.txt is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' " in {

    val result = bagInfoTxtMustContainBagItProfileVersion(testDirOfWrongBagItProfileURI)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }



   /*
  "print" should "return" in {
    val bagInfoProperties = new PropertiesConfiguration(Paths.get(testDirOfWrongBagItProfileURI.resolve("bag-info.txt").toUri).toFile)

      bagInfoProperties.getString("BagIt-Profile-URI").equals("doi:wrongDoi") shouldBe true
  }
  */





}
