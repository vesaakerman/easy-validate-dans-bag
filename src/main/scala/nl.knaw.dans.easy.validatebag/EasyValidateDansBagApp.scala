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

import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }

import javax.xml.validation.SchemaFactory
import nl.knaw.dans.easy.validatebag.InfoPackageType.InfoPackageType
import nl.knaw.dans.easy.validatebag.rules.{ ProfileVersion0, ProfileVersion1 }
import nl.knaw.dans.easy.validatebag.validation.RuleViolationException
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.{ Failure, Try }

class EasyValidateDansBagApp(configuration: Configuration) extends DebugEnhancedLogging {
  logger.info("Creating XML Schema factory...")
  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
  logger.info("XML Schema factory created.")

  private val ddmValidator = Try {
    logger.info("Creating ddm.xml validator...")
    val ddmSchema = schemaFactory.newSchema(new URL("https://easy.dans.knaw.nl/schemas/md/ddm/ddm.xsd"))
    val v = new XmlValidator(ddmSchema)
    logger.info("ddm.xml validator created.")
    v
  }.unsafeGetOrThrow

  private val filesValidator = Try {
    logger.info("Creating files.xml validator...")
    val filesSchema = schemaFactory.newSchema(new URL("https://easy.dans.knaw.nl/schemas/bag/metadata/files/files.xsd"))
    val v = new XmlValidator(filesSchema)
    logger.info("files.xml validator created.")
    v
  }.unsafeGetOrThrow

  private val xmlValidators: Map[String, XmlValidator] = Map(
    "dataset.xml" -> ddmValidator,
    "files.xml" -> filesValidator
  )

  private val allRules: Map[ProfileVersion, RuleBase] = {
    Map(
      profileVersion0 -> ProfileVersion0(xmlValidators, configuration.allowedLicenses),
      profileVersion1 -> ProfileVersion1(xmlValidators))
  }

  def validate(uri: URI, infoPackageType: InfoPackageType): Try[ResultMessage] = {
    for {
      bag <- getBagPath(uri)
      version <- getProfileVersion(bag)
      violations <- validation.checkRules(new TargetBag(bag, version), allRules(version), infoPackageType)(isReadable = _.isReadable)
        .map(_ => Seq.empty)
        .recoverWith(extractViolations)
    } yield ResultMessage(uri, bag.getFileName.toString, version, infoPackageType, violations)
  }

  private def getProfileVersion(path: BagDir): Try[Int] = {
    validation.getProfileVersion(path)
  }

  private def getBagPath(uri: URI): Try[Path] = Try {
    Paths.get(uri.getPath)
  }

  val extractViolations: PartialFunction[Throwable, Try[Seq[(RuleNumber, String)]]] = {
    case x @ CompositeException(xs) =>
      if (xs.forall(_.isInstanceOf[RuleViolationException])) Try(xs.map { case RuleViolationException(nr, details) => (nr, details) })
      else Failure(x) // If there are other exceptions, just generate a fatal exception; let the caller sort out the more serious problems first.
  }
}
