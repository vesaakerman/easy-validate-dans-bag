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

import java.nio.file.{ Files, NoSuchFileException, Paths }

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper
import gov.loc.repository.bagit.domain.Bag
import gov.loc.repository.bagit.exceptions._
import gov.loc.repository.bagit.reader.BagReader
import gov.loc.repository.bagit.verify.BagVerifier
import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.{ RuleViolationDetailsException, _ }
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.language.postfixOps
import scala.util.{ Failure, Try }

/**
 * Rules that refer back to the BagIt specifications.
 */
package object bagit {

  private val bagReader = new BagReader()
  private val bagVerifier = new BagVerifier()

  def closeVerifier(): Unit = {
    bagVerifier.close()
  }

  def bagMustBeValid(b: BagDir): Try[Unit] = {
    def failBecauseInvalid(t: Throwable): Try[Unit] = {
      val details = s"Bag is not valid: Exception = ${ t.getClass.getSimpleName }, cause = ${ t.getCause }, message = ${ t.getMessage }"
      debug(details)
      Try(fail(details))
    }

    Try { bagReader.read(b) }
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

  def bagMustBeVirtuallyValid(b: BagDir): Try[Unit] = {
    bagMustBeValid(b)
      .recover {
        case cause: RuleViolationDetailsException =>
          Try(fail(s"${ cause.details } (WARNING: bag may still be virtually-valid, but this version of the service cannot check that."))
      }
    // TODO: implement proper virtual-validity check.
  }

  def bagMustContainBagInfoTxt(b: BagDir) = Try {
    if (!Files.exists(b.resolve("bag-info.txt")))
      fail("Mandatory file 'bag-info.txt' not found in bag. ")
  }

  def bagInfoTxtMayContainOne(element: String)(b: BagDir): Try[Unit] = Try {
    val bag = bagReader.read(Paths.get(b.toUri))
    val values = bag.getMetadata.get(element)
    if (values != null && values.size > 1) fail(s"bag-info.txt may contain at most one element: $element")
  }

  def bagInfoTxtOptionalElementMustHaveValue(element: String, value: String)(b: BagDir): Try[Unit] = {
    getBagInfoTxtValue(b, element).map(_.map(s => if (s != value) Try(fail(s"$element must be $value"))))
  }

  // Relies on there being only one element with the specified name
  private def getBagInfoTxtValue(b: BagDir, element: String): Try[Option[String]] = Try {
    trace(b, element)
    val bag = bagReader.read(Paths.get(b.toUri))
    Option(bag.getMetadata.get(element)).map(_.get(0))
  }

  def bagInfoTxtMustContainCreated(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (!readBag.getMetadata.contains("Created"))
      fail("The bag-info.txt file MUST contain an element called Created")
  }

  def bagInfoTxtCreatedMustBeIsoDate(b: BagDir): Try[Unit] = {
    val readBag = bagReader.read(Paths.get(b.toUri))
    val valueOfCreated = readBag.getMetadata.get("Created").get(0)
    Try { DateTime.parse(valueOfCreated, ISODateTimeFormat.dateTime) }
      .map(_ => ())
      .recoverWith {
        case _: Throwable =>
          Failure(RuleViolationDetailsException("Created-entry in bag-info.txt not in correct ISO 8601 format"))
      }
  }

  def bagMustContainSHA1(b: BagDir) = Try {
    if (!Files.exists(b.resolve("manifest-sha1.txt")))
      fail("Mandatory file 'manifest-sha1.txt' not found in bag.")
  }

  def bagMustContainMetadataDir(b: BagDir) = Try {
    if (Files.isDirectory(b.resolve("metadata"))) ()
    else fail("Mandatory directory 'metadata' not found in bag.")
  }
}
