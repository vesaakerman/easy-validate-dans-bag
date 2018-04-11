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

import java.net.{ URI, URL }
import java.nio.file.Path

import nl.knaw.dans.easy.validatebag.{ BagDir, XmlValidator }
import nl.knaw.dans.easy.validatebag.validation._

import scala.util.{ Success, Try }
import scala.xml._

package object metadata {
  val namespaceSchemaInstance = new URI("http://www.w3.org/2001/XMLSchema-instance")
  val namespaceDcterms = new URI("http://purl.org/dc/terms/")

  def xmlFileMustConformToSchema(xmlFile: Path, validator: XmlValidator)(b: BagDir): Try[Unit] = {
    trace(xmlFile)
    require(!xmlFile.isAbsolute, "Path to xmlFile must be relative.")
    (b / xmlFile.toString).inputStream.map(validator.validate).map {
      _.recoverWith { case t: Throwable => Try(fail(t.getMessage)) }
    }
  }.get()

  def ddmMayContainDctermsLicenseFromList(ddmPath: Path, allowedLicenses: Seq[URL])(b: BagDir): Try[Unit] = Try {
    trace(())
    val metadata = XML.loadFile((b / ddmPath.toString).toJava)  \ "dcmiMetadata"
    val licenses = (metadata \ "license").toList
    debug(s"Found licences: ${licenses.mkString(", ")}")
    lazy val rightsHolders = (metadata \ "rightsHolder").toList

    licenses match {
      case license :: Nil if hasXsiType(namespaceDcterms, "URI")(license) =>
        val licenseURL = new URL(license.text)
        if (allowedLicenses.contains(licenseURL)) {
          if (rightsHolders.isEmpty) fail("Valid license found, but no rightsHolder specified")
          else Success(())
        }
        else fail(s"Found unknown or unsupported license: $licenseURL")
      case Nil | _ :: Nil =>
        debug("No licences with xsi:type=\"dcterms:URI\"")
        Success(())
      case _ => fail(s"Found ${ licenses.size } dcterms:license elements. Only one license is allowed.")
    }
  }

  private def hasXsiType(attrNamespace: URI, attrValue: String)(e: Node): Boolean = {
    e.attribute(namespaceSchemaInstance.toString, "type")
      .exists {
        case Seq(n) =>
          n.text.split(":") match {
            case Array(pref, label) => e.getNamespace(pref) == attrNamespace.toString && label == attrValue
            case _ => false
          }
        case _ => false
      }
  }
}
