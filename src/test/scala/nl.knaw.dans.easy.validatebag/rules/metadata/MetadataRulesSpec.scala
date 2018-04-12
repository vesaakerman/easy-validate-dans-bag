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

import java.net.URL
import java.nio.file.Paths

import javax.xml.validation.SchemaFactory
import nl.knaw.dans.easy.validatebag.{ TestSupportFixture, XmlValidator }
import nl.knaw.dans.lib.error._

import scala.util.Try

class MetadataRulesSpec extends TestSupportFixture {
  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")

  private val ddmValidator = Try {
    logger.info("Creating ddm.xml validator...")
    val ddmSchema = schemaFactory.newSchema(new URL("https://easy.dans.knaw.nl/schemas/md/ddm/ddm.xsd"))
    val v = new XmlValidator(ddmSchema)
    logger.info("ddm.xml validator created.")
    v
  }.unsafeGetOrThrow

  "xmlFileMustConformToSchema" should "report validation errors if XML not valid" in {
    testRuleViolationRegex(
      rule = xmlFileMustConformToSchema(Paths.get("metadata/dataset.xml"), ddmValidator),
      inputBag = "metadata-unknown-element",
      includedInErrorMsg = "UNKNOWN-ELEMENT".r
    )
  }

  it should "succeed if XML is valid" in {
    testRuleSuccess(
      rule = xmlFileMustConformToSchema(Paths.get("metadata/dataset.xml"), ddmValidator),
      inputBag = "metadata-correct")
  }
}
