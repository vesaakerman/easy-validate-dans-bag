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

import java.nio.file.NoSuchFileException

import gov.loc.repository.bagit.domain.Bag
import gov.loc.repository.bagit.exceptions._
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms._
import gov.loc.repository.bagit.verify.BagVerifier
import nl.knaw.dans.easy.validatebag.TargetBag
import nl.knaw.dans.easy.validatebag.validation._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.collection.JavaConverters._
import scala.language.postfixOps
import scala.util.{ Failure, Try }

/**
 * Rules that refer back to the BagIt specifications.
 */
package object bagit extends DebugEnhancedLogging {
  private val bagVerifier = new BagVerifier()

  def closeVerifier(): Unit = {
    bagVerifier.close()
  }

  def bagMustBeValid(t: TargetBag): Try[Unit] = {
    trace(())

    def failBecauseInvalid(t: Throwable): Try[Unit] = {
      val details = s"Bag is not valid: Exception = ${ t.getClass.getSimpleName }, cause = ${ t.getCause }, message = ${ t.getMessage }"
      debug(details)
      Try(fail(details))
    }

    t.tryBag
      .recoverWith {
        case cause: NoSuchFileException if cause.getMessage.endsWith("bagit.txt") =>
          /*
           * This seems to be the only reason when failing to read the bag should be construed as its being non-valid.
           */
          Try(fail("Mandatory file 'bagit.txt' is missing.")).asInstanceOf[Try[Bag]]
      }
      .map(bagVerifier.isValid(_, false))
      .recoverWith {
        /*
         * Any of these (unfortunately unrelated) exception types mean that the bag is non-valid. The reason is captured in the
         * exception. Any other (non-fatal) exception type means the verification process itself failed;
         * this should lead to a Failure. (Btw fatal errors will NOT be wrapped in a Failure by above Try block!)
         *
         * Note that VerificationException is not included below, as it indicates a error during validation rather
         * than that the bag is non-valid.
         */
        case cause: MissingPayloadManifestException => failBecauseInvalid(cause)
        case cause: MissingBagitFileException => failBecauseInvalid(cause)
        case cause: MissingPayloadDirectoryException => failBecauseInvalid(cause)
        case cause: FileNotInPayloadDirectoryException => failBecauseInvalid(cause)
        case cause: FileNotInManifestException => failBecauseInvalid(cause)
        case cause: MaliciousPathException => failBecauseInvalid(cause)
        case cause: CorruptChecksumException => failBecauseInvalid(cause)
        case cause: UnsupportedAlgorithmException => failBecauseInvalid(cause)
        case cause: InvalidBagitFileFormatException => failBecauseInvalid(cause)
      }
  }

  def bagMustBeVirtuallyValid(t: TargetBag): Try[Unit] = {
    trace(())
    bagMustBeValid(t)
      .recoverWith {
        case cause: RuleViolationDetailsException =>
          Try(fail(s"${ cause.details } (WARNING: bag may still be virtually-valid, but this version of the service cannot check that."))
      }
    // TODO: implement proper virtual-validity check.
  }

  def bagMustContainBagInfoTxt(t: TargetBag) = Try {
    trace(())
    if (!(t.bagDir / "bag-info.txt").exists)
      fail("Mandatory file 'bag-info.txt' not found in bag. ")
  }

  def bagInfoTxtMayContainOne(element: String)(t: TargetBag): Try[Unit] = Try {
    val bag = t.tryBag.get // TODO: put inside Try
    val values = bag.getMetadata.get(element)
    if (values != null && values.size > 1) fail(s"bag-info.txt may contain at most one element: $element")
  }

  def bagInfoTxtMustContainExactlyOne(element: String)(t: TargetBag): Try[Unit] = Try {
    val bag = t.tryBag.get // TODO: put inside Try
    val values = bag.getMetadata.get(element)
    val numberFound = Option(values).getOrElse(List.empty[String].asJava).size
    if (numberFound != 1) fail(s"bag-info.txt must contain exactly one '$element' element; number found: $numberFound")
  }

  def bagInfoTxtMustNotContain(element: String)(t: TargetBag): Try[Unit] = Try {
    trace(element)
    val bag = t.tryBag.get // TODO: put inside Try
    if (bag.getMetadata.contains(element)) fail(s"bag-info.txt must not contain element: $element")
  }

  def bagInfoTxtElementMustHaveValue(element: String, value: String)(t: TargetBag): Try[Unit] = {
    trace(element, value)
    getBagInfoTxtValue(t, element).map {
      _.foreach {
        s =>
          if (s != value) fail(s"$element must be $value; found: $s")
      }
    }
  }

  // Relies on there being only one element with the specified name
  private def getBagInfoTxtValue(t: TargetBag, element: String): Try[Option[String]] = Try {
    trace(t, element)
    val bag = t.tryBag.get // TODO: put inside Try
    Option(bag.getMetadata.get(element)).map(_.get(0))
  }

  def bagInfoTxtCreatedMustBeIsoDate(t: TargetBag): Try[Unit] = {
    trace(())
    val readBag = t.tryBag.get // TODO: put inside Try
    val valueOfCreated = readBag.getMetadata.get("Created").get(0)
    Try { DateTime.parse(valueOfCreated, ISODateTimeFormat.dateTime) }
      .map(_ => ())
      .recoverWith {
        case _: Throwable =>
          Failure(RuleViolationDetailsException("Created-entry in bag-info.txt not in correct ISO 8601 format"))
      }
  }

  def bagMustContainSha1PayloadManifest(t: TargetBag) = Try {
    trace(())
    if (!(t.bagDir / "manifest-sha1.txt").exists)
      fail("Mandatory file 'manifest-sha1.txt' not found in bag.")
  }

  def bagSha1PayloadManifestMustContainAllPayloadFiles(t: TargetBag) = Try {
    trace(())
    val bag = t.tryBag.get // TODO: put inside Try
    bag.getPayLoadManifests.asScala.find(_.getAlgorithm == SHA1)
      .foreach {
        manifest =>
          val filesInManifest = manifest.getFileToChecksumMap.keySet().asScala.map(p => t.bagDir.relativize(p)).toSet
          debug(s"Manifest files: ${ filesInManifest.mkString(", ") }")
          val filesInPayload = (t.bagDir / "data").walk().filter(_.isRegularFile).map(f => t.bagDir.path.relativize(f.path)).toSet
          debug(s"Payload files: ${ filesInManifest.mkString(", ") }")
          if (filesInManifest != filesInPayload) {
            val filesOnlyInPayload = filesInPayload -- filesInManifest // The other way around should have been caught by the validity check
            fail(s"All payload files must have an SHA-1 checksum. Files missing from SHA-1 manifest: ${ filesOnlyInPayload.mkString(", ") }")
          }
      }
  }
}
