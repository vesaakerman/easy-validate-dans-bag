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

import java.nio.file.{ Files, Paths }

import gov.loc.repository.bagit.reader.BagReader
import gov.loc.repository.bagit.verify.BagVerifier
import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.{ RuleViolationDetailsException, fail }
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


  def bagMustBeValid(b: BagDir) = Try {
//    val bag = bagReader.read(b)
//    Try { bagVerifier.isValid(bag) } {
//
//
//
//    }
  }

  def bagMustBeVirtuallyValid(b: BagDir) = Try {
    // TODO: same als bagMustBeValid, but when NON-VALID warn that "virtually-only-valid" bags cannot not be recognized by the service yet.
  }

  def bagMustContainBagInfoTxt(b: BagDir) = Try {
    if (!Files.exists(b.resolve("bag-info.txt")))
      fail("Mandatory file 'bag-info.txt' not found in bag. ")
  }

  def bagInfoTxtMustContainCreated(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (!readBag.getMetadata.contains("Created")) {
      fail("The bag-info.txt file MUST contain an element called Created")
    }
  }

  def bagInfoTxtCreatedMustBeIsoDate(b: BagDir):  Try[Unit] =  {
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
    if (!Files.exists(b.resolve("manifest-sha1.txt"))) fail("Mandatory file 'manifest-sha1.txt' not found in bag.")
  }

  def bagMustContainMetadataDir(b: BagDir) = Try {
    if (Files.isDirectory(b.resolve("metadata"))) ()
    else fail("Mandatory directory 'metadata' not found in bag.")
  }
}
