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

import java.nio.file.{ Files, Paths }

import gov.loc.repository.bagit.reader.BagReader
import nl.knaw.dans.easy.validatebag.{ BagDir, TestSupportFixture }
import nl.knaw.dans.easy.validatebag.validation.RuleViolationDetailsException
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.scalatest.exceptions.TestFailedException

import scala.util.{ Failure, Success, Try }

class BagItRulesSpec extends TestSupportFixture {
  private val bagsDir = Paths.get("src/test/resources/bags")

  val testDirOfMissingBagInfo: BagDir = bagsDir.resolve("missingBagInfo")
  val testDirOfMissingBagItProfileVersion: BagDir = Paths.get("src/test/resources/bags/minimal")
  val testDirOfWrongBagItProfileVersion: BagDir = Paths.get("src/test/resources/bags/wrongBagItProfileVersion")
  val testDirOfBagItProfileVersionWithNoValue: BagDir = Paths.get("src/test/resources/bags/BagItProfileVersionWithNoValue")
  val testDirOfSeveralBagItProfileVersionsV1: BagDir = Paths.get("src/test/resources/bags/severalBagItProfileVersionsV1")
  val testDirOfSeveralBagItProfileVersionsV0: BagDir = Paths.get("src/test/resources/bags/severalBagItProfileVersionsV0")
  val testDirOfMissingBagItProfileURI: BagDir = Paths.get("src/test/resources/bags/minimal")
  val testDirOfWrongBagItProfileURI: BagDir = Paths.get("src/test/resources/bags/wrongBagItProfileURI")
  val testDirOfBagItProfileURIwithNoValue: BagDir = Paths.get("src/test/resources/bags/BagItProfileURIwithNoValue")
  val testDirOfSeveralBagItProfileURIs: BagDir = Paths.get("src/test/resources/bags/severalBagItProfileURIs")
  val testDirOfUuidPointingToNonexistingBaseBag: BagDir = Paths.get("src/test/resources/bags/IsVersionOfPointsToNonExistingBaseBag")
  val testDirOfUuidPointingToExistingBaseBag: BagDir = Paths.get("src/test/resources/bags/IsVersionOfPointsToExistingBaseBag")
  val testDirOfMissingCreated: BagDir = Paths.get("src/test/resources/bags/missingCreated")
  val testDirOfMissingTimeAndTimeZoneInCreated: BagDir = Paths.get("src/test/resources/bags/missingTimeAndTimeZoneInCreated")
  val testDirOfViolatedISO806: BagDir = Paths.get("src/test/resources/bags/violatedISO806inCreated")
  val testDirOfLastModTimeNotEqualToCreated: BagDir = Paths.get("src/test/resources/bags/lastModTimeNotEqualToCreated")
  val testDirOfNonexistingManifestSHA1: BagDir = Paths.get("src/test/resources/bags/nonexistingManifestSHA1")
  val testDirOfExistingManifestSHA1: BagDir = Paths.get("src/test/resources/bags/existingManifestSHA1")
  val testDirOfExistingManifestSHA1missingPayloadEntries: BagDir = Paths.get("src/test/resources/bags/existingManifestSHA1missingPayloadEntries")
  val testDirOfExistingManifestAndTagManifestsMD5SHA1: BagDir = Paths.get("src/test/resources/bags/existingManifestsAndTagmanifestsMD5SHA1")
  val testDirOfMissingOptionalManifestAndTagManifests: BagDir = Paths.get("src/test/resources/bags/missingOptionalManifestsAndTagmanifests")
  val testDirOfMissingContentTagmanifestSHA1: BagDir = Paths.get("src/test/resources/bags/missingContentTagmanifestSHA1")
  val testDirOfMissingContentTagmanifestMD5: BagDir = Paths.get("src/test/resources/bags/missingContentTagmanifestMD5")
  val testDirOfMissingContentManifestMD5: BagDir = Paths.get("src/test/resources/bags/missingContentManifestMD5")


  "bagMustContainBagInfoTxt" should "fail if bag-info.txt is not found" in {

    val result = bagMustContainBagInfoTxt(testDirOfMissingBagInfo)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*---------------------------*/

  "bagInfoTxtMustContainBagItProfileVersionV1" should "fail if 'BagIt-Profile-Version' does not exist in bag-info.txt" in {

    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfMissingBagItProfileVersion)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if 'BagIt-Profile-Version' in bag-info.txt is not 1.0.0 " in {

    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfWrongBagItProfileVersion)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if nbr of the keys named 'BagIt-Profile-Version' in bag-info.txt is not exactly 1" in {

    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfSeveralBagItProfileVersionsV1)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if no value is provided for 'BagIt-Profile-Version' " in {

    val result = bagInfoTxtMustContainBagItProfileVersionV1(testDirOfBagItProfileVersionWithNoValue)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*---------------------------*/

  "bagInfoTxtMayContainBagItProfileVersionV0" should "fail if 'BagIt-Profile-Version' in bag-info.txt exists but not equal to 0.0.0 " in {

    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfWrongBagItProfileVersion)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if 'BagIt-Profile-Version' exists but no value is provided " in {

    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfBagItProfileVersionWithNoValue)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if nbr of the keys named 'BagIt-Profile-Version' in bag-info.txt is greater than 1" in {

    val result = bagInfoTxtMayContainBagItProfileVersionV0(testDirOfSeveralBagItProfileVersionsV0)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*----------------------------------------------------*/


  "bagInfoTxtMustContainBagItProfileURIV1" should "fail if 'BagIt-Profile-URI' does not exist in bag-info.txt" in {

    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfMissingBagItProfileURI)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }


  it should "fail if 'BagIt-Profile-URI in bag-info.txt is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' " in {

    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfWrongBagItProfileURI)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if nbr of the keys named 'BagIt-Profile-URI' in bag-info.txt is not exactly 1" in {

    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfSeveralBagItProfileURIs)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if no value is provided for 'BagIt-Profile-URI' " in {

    val result = bagInfoTxtMustContainBagItProfileURIV1(testDirOfBagItProfileURIwithNoValue)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*---------------------------------------------------*/


  "bagInfoTxtMayContainBagItProfileURIV0" should "fail if 'BagIt-Profile-URI' in bag-info.txt exists but not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' " in {

    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfWrongBagItProfileURI)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if 'BagIt-Profile-URI' exists but no value is provided " in {

    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfBagItProfileURIwithNoValue)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if nbr of the keys named 'BagIt-Profile-URI' in bag-info.txt is greater than 1" in {

    val result = bagInfoTxtMayContainBagItProfileURIV0(testDirOfSeveralBagItProfileURIs)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*---------------------------------------------------------------*/

  "bagInfoTxtMustContainCreated" should "fail if 'Created' is missing in bag-info.txt" in {

    val result = bagInfoTxtMustContainCreated(testDirOfMissingCreated)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if the value of 'Created' does not violate ISO806 format but is not in combined date and time format in bag-info.txt. Note that in this case the default time 00:00:00 or default timezone EUROPE/Amsterdam can still be retrieved by DateTime function! " in {

    val result = bagInfoTxtMustContainCreated(testDirOfMissingTimeAndTimeZoneInCreated)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if the value of 'Created' is not in ISO806 format in bag-info.txt." in {

    val result = bagInfoTxtMustContainCreated(testDirOfViolatedISO806)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if the value of 'Created' is not equal to last modified time of the bag" in {

    val result = bagInfoTxtMustContainCreated(testDirOfLastModTimeNotEqualToCreated)
    //println(result)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*----------------------------------------------------------------*/

  "bagInfoTxtMayContainIsVersionOf" should "fail if the urn in 'Is-Version-Of' points to a nonexisting base bag in bag-store" in {
    val baseDir: BagDir = Paths.get("src/test/resources/bags/bag-store")
    val result = bagInfoTxtMayContainIsVersionOf(testDirOfUuidPointingToNonexistingBaseBag, baseDir)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[TestFailedException] //TODO not a RuleViolationDetailsException. Because of different definition of function??
    }
  }

  it should " not fail if the urn in 'Is-Version-Of' points to an existing base bag in bag-store" in {
    val baseDir: BagDir = Paths.get("src/test/resources/bags/bag-store")
    val result = bagInfoTxtMayContainIsVersionOf(testDirOfUuidPointingToExistingBaseBag, baseDir)
    result should not be a[Failure[_]]
  }

  /*
  "printFunction" should "return" in {
    class aFunction(configuration: Configuration) {
      val pwd = System.getProperty("user.dir")
      //val x = Paths.get(pwd).toUri.resolve("bagStore.properties")
      println("pwd: " + pwd)
      //x.getProperty("bag-store")
      // val configur = Paths.get(System.getProperty("app.home"))
      //val app = new EasyValidateDansBagApp(configur)
      //val path = Paths.get(configur.properties.getString("bag-store"))
      //println(path)
      // val config = new PropertiesConfiguration(path.toString)
      //println(config.getString("bag-stores"))
      val configuratio = Configuration(Paths.get(System.getProperty("app.home")))
      val app = new EasyValidateDansBagApp(configuratio)
      val bagstorepath = new Configuration(configuratio.properties.getString("bag-store"), new PropertiesConfiguration())
      //bagstorepath
      println("bagstorepath" + bagstorepath)
      bagstorepath shouldBe "x"
      }
    }
    */

  //TODO Here I redefined the function bagInfoTxtMayContainIsVersionOf(b: BagDir) as bagInfoTxtMayContainIsVersionOf(b: BagDir, bb : BagDir)
  //TODO where b is used as the path of the bag for which validation rule is being checked
  //TODO and bb is used as the path of the base bag pointed by the uuid in the bag to be validated.
  //TODO I was not able to make this change in /src/main/scala/nl.knaw.dans.../rules/bagit/bagit
  //TODO because the definition of the function (bagInfoTxtMayContainIsVersionOf) does not allow more than one input argument
  //TODO In unit tests I define the path of directory of bag-store as "src/test/resources/bags/bag-store"
  //TODO and in /src/main/scala/nl.knaw.dans.../rules/bagit/bagit, I define the path of directory of bag-store as
  //TODO "srv/dans.knaw.nl/bag-store"
  //TODO I tried to get these different paths by writing a new configuration function (see bagStore.properties files in cfg s)
  //TODO with the aim of finding a way of reading a different path without changing the definition of bagInfoTxtMayContainIsVersionOf(b: BagDir)
  //TODO but I was not able to integrate that function into this project. Do I really need that?
  //TODO Any suggestions?

  def bagInfoTxtMayContainIsVersionOf(b: BagDir, bb: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    val dirOfBagStore = Paths.get(bb.toUri)
    println("dirOfBagStore: " + dirOfBagStore)
    if (counterOfTheSameKeys(b, "Is-Version-Of") <= 1) {
      if (readBag.getMetadata.contains("Is-Version-Of")) {
        if (readBag.getMetadata.get("Is-Version-Of").get(0).contains("urn:uuid:")) {
          val uuid = readBag.getMetadata.get("Is-Version-Of").get(0).stripPrefix("urn:uuid:").replaceAll("-", "").trim()
          println(uuid)
          val dirOfBaseBag: BagDir = Paths.get(dirOfBagStore + "/" + uuid.splitAt(2)._1 + "/" + uuid.splitAt(2)._2)
          println("dirOfBaseBag: " + dirOfBaseBag)
          if (Files.notExists(dirOfBaseBag))
            fail(" Violation of rule 1.2.5 in v1 and v0: uuid points to a nonexisting base-id ")
          println(Files.notExists(dirOfBaseBag))
        }
        else fail("Violation of rule 1.2.5 in v1 and v0: 'urn:uuid:' title is missing in the value of the key'Is-Version-Of' ")
      }
    }
    else fail("Violation of rule 1.2.5 in v1 and v0: nbr of provided uuids are greater than 1 in bag-info.txt")
  }

  /*----------------------------------------------------------------*/

  "bagMustContainSHA1" should "fail if 'manifest-sha1.txt' is not found in the bag" in {
    val result = bagMustContainSHA1(testDirOfNonexistingManifestSHA1)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "succeed if 'manifest-sha1.txt' is correct" in {
    bagMustContainSHA1(testDirOfExistingManifestSHA1) shouldBe a[Success[_]]
  }

  it should " fail if one of the payload manifests is missing in 'manifest-sha1.txt' " in {
    val result = bagMustContainSHA1(testDirOfExistingManifestSHA1missingPayloadEntries)
    val b: BagDir = Paths.get(testDirOfExistingManifestSHA1missingPayloadEntries.toUri)
    val readBag = bagReader.read(Paths.get(b.toUri))
    println(readBag.getPayLoadManifests)
    FileUtils.listFilesAndDirs(b.resolve("data").toRealPath().toFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE).forEach(i =>
      if (i.isFile) {
        if (readBag.getPayLoadManifests.toString.contains(i.toString))
          println(i)
      }
    )
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*----------------------------------------------------------------*/

  "bagMayContainOtherManifestsAndTagManifests" should "not fail if 'tagmanifest-sha1.txt' is found and contains all tag manifests" in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfExistingManifestAndTagManifestsMD5SHA1)
    val b: BagDir = Paths.get(testDirOfExistingManifestAndTagManifestsMD5SHA1.toUri)
    val readBag = bagReader.read(Paths.get(b.toUri))
    //println(readBag.getTagManifests)
    readBag.getTagManifests.toArray().foreach(i => println(i))
    //println(readBag.getTagManifests.toArray().splitAt(1)._2.toString())
    result should not be a[Failure[_]]
  }

  it should "not fail if 'tagmanifest-md5.txt' is found and contains all tag manifests" in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfExistingManifestAndTagManifestsMD5SHA1)
    val b: BagDir = Paths.get(testDirOfExistingManifestAndTagManifestsMD5SHA1.toUri)
    val readBag = bagReader.read(Paths.get(b.toUri))
    //println(readBag.getTagManifests)
    result should not be a[Failure[_]]
  }

  it should "not fail if 'manifest-md5.txt' is found and contains all payload manifests" in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfExistingManifestAndTagManifestsMD5SHA1)
    val b: BagDir = Paths.get(testDirOfExistingManifestAndTagManifestsMD5SHA1.toUri)
    val readBag = bagReader.read(Paths.get(b.toUri))
    //println(readBag.getPayLoadManifests)
    result should not be a[Failure[_]]
  }

  it should " not fail if 'tagmanifest-sha1.txt' is missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingOptionalManifestAndTagManifests)
    result should not be a[Failure[_]]
  }

  it should " not fail if 'tagmanifest-md5.txt' is missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingOptionalManifestAndTagManifests)
    result should not be a[Failure[_]]
  }

  it should " not fail if 'manifest-md5.txt' is missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingOptionalManifestAndTagManifests)
    result should not be a[Failure[_]]
  }

  it should " fail if 'tagmanifest-sha1.txt' exists but a part of the content missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingContentTagmanifestSHA1)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should " fail if 'tagmanifest-md5.txt' exists but a part of the content missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingContentTagmanifestMD5)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should " fail if 'manifest-md5.txt' exists but a part of the content missing " in {
    val result = bagMayContainOtherManifestsAndTagManifests(testDirOfMissingContentManifestMD5)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  /*----------------------------------------------------------------*/


  val bagReader: BagReader = new BagReader

  val b1: BagDir = Paths.get(testDirOfMissingBagItProfileVersion.toUri)
  val b2: BagDir = Paths.get(testDirOfWrongBagItProfileVersion.toUri)
  val b3: BagDir = Paths.get(testDirOfBagItProfileVersionWithNoValue.toUri)

  val b4: BagDir = Paths.get(testDirOfMissingBagItProfileURI.toUri)
  val b5: BagDir = Paths.get(testDirOfWrongBagItProfileURI.toUri)
  val b6: BagDir = Paths.get(testDirOfBagItProfileURIwithNoValue.toUri)

  val b7: BagDir = Paths.get(testDirOfMissingBagInfo.toUri)

  val b8: BagDir = Paths.get(testDirOfSeveralBagItProfileVersionsV1.toUri)
  val b9: BagDir = Paths.get(testDirOfSeveralBagItProfileVersionsV0.toUri)
  val b10: BagDir = Paths.get(testDirOfSeveralBagItProfileURIs.toUri)

  val b11: BagDir = Paths.get(testDirOfUuidPointingToNonexistingBaseBag.toUri)


  /* the following tests are just for double-check on test inputs and getMetadata function */

  "a NullPointerException" should "be thrown for BagIt-Profile-Version in testDirOfMissingBagItProfileVersion" in {
    a[NullPointerException] should be thrownBy { bagReader.read(b1).getMetadata.get("BagIt-Profile-Version").get(0) }
  }

  "BagIt-Profile-Version" should "be '9.9.9' in testDirOfWrongBagItProfileVersion" in {
    bagReader.read(b2).getMetadata.get("BagIt-Profile-Version").get(0) shouldBe "9.9.9"
  }

  it should "have no value in testDirOfBagItProfileVersionWithNoValue" in {
    bagReader.read(b3).getMetadata.get("BagIt-Profile-Version").get(0) shouldBe ""
  }

  "a NullPointerException" should "be thrown for BagIt-Profile-URI in testDirOfMissingBagItProfileURI" in {
    a[NullPointerException] should be thrownBy { bagReader.read(b4).getMetadata.get("BagIt-Profile-URI").get(0) }
  }

  "BagIt-Profile-URI" should "be 'doi:wrongDoi' in testDirOfWrongBagItProfileURI" in {
    bagReader.read(b5).getMetadata.get("BagIt-Profile-URI").get(0) shouldBe "doi:wrongDoi"
  }

  it should "have no value in testDirOfBagItProfileURIwithNoValue" in {
    bagReader.read(b6).getMetadata.get("BagIt-Profile-URI").get(0) shouldBe ""
  }

  "bag-info.txt" should "not exist in testDirOfMissingBagInfo" in {
    Files.exists(b7.resolve("bag-info.txt")) shouldBe false
  }

  "nbr of the key BagIt-Profile-Version" should "be greater than 1 in testDirOfSeveralBagItProfileVersionsV1" in {
    counterOfTheSameKeys(b8, "BagIt-Profile-Version") should be > 1
  }


  "print" should "return" in {
    // val bagInfoProperties = new PropertiesConfiguration(Paths.get(testDirOfWrongBagItProfileURI.resolve("bag-info.txt").toUri).toFile)
    // bagInfoProperties.getString("BagIt-Profile-URI").equals("doi:wrongDoi") shouldBe true
    //val b = Paths.get(testDirOfWrongBagItProfileURI.resolve("bag-info.txt").toUri)
    //val b2 = Paths.get(testDirOfWrongBagItProfileURI.toUri)
    val b1 = Paths.get(testDirOfWrongBagItProfileVersion.toUri)
    //val b2 = Paths.get(testDirOfBagItProfileVersionWithNoValue.toUri)

    val b2 = Paths.get(testDirOfMissingBagItProfileVersion.toUri)

    //resolve("bag-info.txt")
    //bagReader.read(b2).getMetadata.get("BagIt-Profile-Version").get(0) shouldBe ""


    //bagReader.read(b2).getMetadata.get("BagIt-Profile-URI") shouldBe null
    //(bagReader.read(b11).getRootDir.getParent.getParent.getFileName.toString + bagReader.read(b11).getRootDir.getParent.getFileName.toString)  shouldBe "x"
    //val uuid = bagReader.read(b11).getMetadata.get("Is-Version-Of").get(0).stripPrefix("urn:uuid:").replaceAll("-", "").trim()
    //Files.notExists(Paths.get("./srv/dans.knaw.nl/bag-store/" + uuid.splitAt(2)._1 + "/" + uuid.splitAt(2)._2)) shouldBe false
    val baseDir: BagDir = Paths.get("src/test/resources/bags/bag-store")
    //Paths.get(baseDir.toUri) shouldBe "x"

    val bb = Paths.get(testDirOfUuidPointingToExistingBaseBag.toUri)
    val uuid = bagReader.read(bb).getMetadata.get("Is-Version-Of").get(0).stripPrefix("urn:uuid:").replaceAll("-", "").trim()
    //uuid shouldBe "x"

    //val result = bagInfoTxtMayContainIsVersionOf(testDirOfUuidPointingToExistingBaseBag)

    val dirOfBagStore = Paths.get(baseDir.toUri)

    val dirOfBaseBag = Paths.get(dirOfBagStore + "/" + uuid.splitAt(2)._1 + "/" + uuid.splitAt(2)._2)

    //Files.notExists(dirOfBaseBag) shouldBe true

    //dirOfBaseBag shouldBe "x"

    //Files.exists(dirOfBaseBag) shouldBe true


    // result shouldBe "x"

    // val result = bagInfoTxtMayContainIsVersionOf(testDirOfUuidPointingToExistingBaseBag)
    // result shouldBe a[Success[_]]

    //result shouldBe "X"

    //bagReader.read(b11).getPayLoadManifests shouldBe "x"

    //def bagInfoTxtMustContainCreated(b: BagDir ) = Try {
    val b6: BagDir = Paths.get(testDirOfMissingTimeAndTimeZoneInCreated.toUri)
    val b7: BagDir = Paths.get(testDirOfViolatedISO806.toUri)
    val b8: BagDir = Paths.get(testDirOfBagItProfileURIwithNoValue.toUri)


    val readBag = bagReader.read(Paths.get(b8.toUri))

    println(readBag.getMetadata.get("Created").get(0))



    //val valueOfCreatedNonIso = new DateTime("2018/01/29")
    //println(valueOfCreatedNonIso.toDateTimeISO


    /*
    Try (value.getMillisOfSecond) match {
      case Success(_) => println(value)
      case Failure(_) => fail("Violation of rule 1.2.4 in v1 and v0: millisecond time part is missing")
    }
    Try (value.getZone) match {
      case Success(_) => println(value.getZone)
      case Failure(_) => fail("Violation of rule 1.2.4 in v1 and v0: timezone is missing")
    }
    Try (value.getMinuteOfHour) match {
      case Success(_) => (value.getMinuteOfHour)
      case Failure(_) => fail("Violation of rule 1.2.4 in v1 and v0: minute time part is missing")
    }
    Try (value.getHourOfDay) match {
      case Success(_) => (value.getHourOfDay)
      case Failure(_) => fail("Violation of rule 1.2.4 in v1 and v0: hour time part is missing")
    }
    */

    //println(formatcheckISO806)

    //if (a[IllegalArgumentException] should be thrownBy(new DateTime("2018/01/29")) ){
    //  fail("not ISO")
    //}


    //  val valueOfCreated = new DateTime(readBag.getMetadata.get("Created").get(0))
    //   println(valueOfCreated)
    //  println(valueOfCreated.getMillisOfSecond)
    //  println(valueOfCreated.getZone)
    //   println(valueOfCreated.toDateTimeISO)
    //if (valueOfCreated.getMillisOfSecond) {
    //  fail("Violation of rule 1.2.4 in v1 and v0: No milliseconds info in 'Created' ")
    //}


    Files.exists(dirOfBaseBag) should not be a[Failure[_]]


  }

}
