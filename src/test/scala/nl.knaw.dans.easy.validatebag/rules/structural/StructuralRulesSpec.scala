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
package nl.knaw.dans.easy.validatebag.rules.structural

import java.nio.file.Paths

import gov.loc.repository.bagit.reader.BagReader
import nl.knaw.dans.easy.validatebag.validation.RuleViolationDetailsException
import nl.knaw.dans.easy.validatebag.{ BagDir, TestSupportFixture }

import scala.util.Failure

class StructuralRulesSpec extends TestSupportFixture {
  val bagMissingMetadataDir: BagDir = Paths.get("src/test/resources/bags/missingMetadata")
  val testDirOfExistingMetadataNoSpaceNoCapital: BagDir = Paths.get("src/test/resources/bags/existingMetadata")
  val testDirOfExistingMetadataWithSpaces: BagDir = Paths.get("src/test/resources/bags/existingMetadataWithSpaces")
  val testDirOfExistingMetadataContainingUppercaseLetters: BagDir = Paths.get("src/test/resources/bags/existingMetadataContainingUppercaseLetters")
  val testDirOfExistingMetadataAllFileContentMissing: BagDir = Paths.get("src/test/resources/bags/existingMetadataFileContentMissing")
  val testDirOfExistingMetadataWithExcessiveFileContent: BagDir = Paths.get("src/test/resources/bags/existingMetadataWithExcessiveFileContent")
  val testDirOfExistingMetadataFilesXmlMissing: BagDir = Paths.get("src/test/resources/bags/existingMetadataFilesXmlMissing")
  val testDirOfExistingMetadataDatasetXmlMissing: BagDir = Paths.get("src/test/resources/bags/existingMetadataDatasetXmlMissing")


  "bagMustContainMetadataFileV0" should "fail if the directory 'metadata' is not found" in {
    val result = bagMustContainMetadataDirectory(bagMissingMetadataDir)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if the file name 'metadata' contains spaces" in {
    val result = bagMustContainMetadataDirectory(testDirOfExistingMetadataWithSpaces)
    val b: BagDir = Paths.get(testDirOfExistingMetadataWithSpaces.toUri)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "fail if the file name 'metadata' is found but contains uppercase letters" in {
    val result = bagMustContainMetadataDirectory(testDirOfExistingMetadataContainingUppercaseLetters)
    val b2: BagDir = Paths.get(testDirOfExistingMetadataContainingUppercaseLetters.toUri)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }

  it should "not fail if the file 'metadata' is found and does not contain any spaces or uppercase chars" in {
    val result = bagMustContainMetadataDirectory(testDirOfExistingMetadataNoSpaceNoCapital)
    val b1: BagDir = Paths.get(testDirOfExistingMetadataNoSpaceNoCapital.toUri)
    val readBag = bagReader.read(Paths.get(b1.toUri))
    result should not be a[Failure[_]]
  }

  it should "fail if metadata file does not contain 'datase.xml' " in {
    val result = metadataFileMustContainDatasetAndFiles(testDirOfExistingMetadataDatasetXmlMissing)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }

  }

  it should "fail if metadata file contains extra files in addition to 'files.xml' and 'dataset.xml" in {
    val result = metadataFileMustContainDatasetAndFiles(testDirOfExistingMetadataWithExcessiveFileContent)
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[RuleViolationDetailsException]
    }
  }
}
