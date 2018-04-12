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

import java.nio.file.Paths

import gov.loc.repository.bagit.domain.Bag
import gov.loc.repository.bagit.reader.BagReader

import scala.util.Try
import scala.xml.{ Elem, XML }

// TODO: optimize validation by making sure expensive actions only need to be done once per bag

/**
 * Interface to the bag under validation.
 *
 * Loads resources from the bag lazily and then hangs on to it, so that subsequent rules do not have
 * to reload this information. The location of some resources depends on the profile version. That is
 * why it is provided as an argument to the constructor.
 *
 * @param profileVersion the profile version used
 */
class TargetBag(val bagDir: BagDir, profileVersion: ProfileVersion = profileVersion0) {
  private val bagReader = new BagReader()
  private val ddmPath = profileVersion match {
    case 0 => Paths.get("metadata/dataset.xml")
    case 1 => Paths.get("metadata/files.xml")
  }
  private val filesXmlPath = profileVersion match {
    case 0 => Paths.get("metadata/files.xml")
    case 1 => Paths.get("data/pdi/files.xml")
  }

  lazy val tryBag: Try[Bag] = Try { bagReader.read(bagDir.path) }

  lazy val tryDdm: Try[Elem] = Try { XML.loadFile((bagDir / ddmPath.toString).toJava) }

  lazy val tryFilesXml: Try[Elem] = Try { XML.loadFile((bagDir / filesXmlPath.toString).toJava) }
}
