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
package nl.knaw.dans.easy.validatebag.rules

import nl.knaw.dans.easy.validatebag.BagDir

import scala.io.Source.fromString
import scala.util.Try
import scala.xml.parsing.ConstructingParser.fromSource
import scala.xml.{ NodeSeq, TopScope, XML }

package object metadata {

  def datasetMustAdhereToVersion2017_09ofDDMxmlschema(b: BagDir) = Try {
     //if (metadataFileMustContainDatasetAndFiles(b).isSuccess) {
       val pathOfMetadata = b.resolve("metadata")
       val pathOfDatasetXml = pathOfMetadata.toRealPath().resolve("dataset.xml")
       val parsedDatasetXml: NodeSeq = parseNoWS(XML.loadFile(pathOfDatasetXml.toString).toString)
     //}
  }


  def parseNoWS(s: String): NodeSeq = fromSource(fromString(s), preserveWS = false).element(TopScope)


  //if (Files.exists(b.resolve("tagmanifest-sha"))) {
   // val readBag = bagReader.read(Paths.get(b.toUri))
   // readBag.









}
