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
package nl.knaw.dans.easy.validatebag.rules.metadata

import java.nio.file.Paths

import nl.knaw.dans.easy.validatebag.{ BagDir, TestSupportFixture }

import scala.xml.{ NodeSeq, XML }


class MetadataRulesSpec extends TestSupportFixture {

  " datasetMustAdhereToVersion2017_09ofDDMxmlschema" should "parse dataset.xml" in {
    val testDirOfMissingOptionalManifestAndTagManifests: BagDir = Paths.get("src/test/resources/bags/missingOptionalManifestsAndTagmanifests")
    val b: BagDir = Paths.get(testDirOfMissingOptionalManifestAndTagManifests.toUri)
    val pathOfMetadata = b.resolve("metadata")
    val pathOfDatasetXml = pathOfMetadata.toRealPath().resolve("dataset.xml")
    val parsedDatasetXml: NodeSeq = parseNoWS(XML.loadFile(pathOfDatasetXml.toString).toString)

    //parsedDatasetXml.seq shouldBe "sth"


  }


}
