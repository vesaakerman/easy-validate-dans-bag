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
//import gov.loc.repository.bagit.domain.Bag
//import gov.loc.repository.bagit.exceptions._

import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.fail
import org.joda.time.{ DateTime, Duration, Interval }

import scala.util.Try

/**
 * Rules that refer back to the BagIt specifications.
 */
package object bagit {
  def bagMustBeValid(b: BagDir) = Try {
    // TODO: check that the bag is VALID according to BagIt.
  }

  def bagMustBeVirtuallyValid(b: BagDir) = Try {
    // TODO: same als bagMustBeValid, but when NON-VALID warn that "virtually-only-valid" bags cannot not be recognized by the service yet.
  }

  private val bagReader: BagReader = new BagReader


  def bagMustContainBagInfoTxt(b: BagDir)= Try {
    if(Files.exists(b.resolve("bag-info.txt"))) ()
    else fail("Mandatory file 'bag-info.txt' not found in bag. ")
  }

  def bagInfoTxtMustContainBagItProfileVersion(b: BagDir ) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (readBag.getMetadata.contains("BagIt-Profile-Version")) {
      if (readBag.getMetadata.get("BagIt-Profile-Version").get(0).equals("1.0.0") || readBag.getMetadata.get("BagIt-Profile-Version").get(0).equals("0.0.0")) ()
      else fail("version is not '1.0.0' or '0.0.0' ")
    }
    else fail(" 'BagIt-Profile-Version' not found in 'bag-info.txt'. ")
  }

  /*
  def bagInfoTxtMustContainBagItProfileVersion(b: BagDir ) = Try {
    val bagInfoProperties = new PropertiesConfiguration(Paths.get(b.resolve("bag-info.txt").toUri).toFile)
    if (bagInfoProperties.containsKey("BagIt-Profile-Version")) {
         if (bagInfoProperties.getString("BagIt-Profile-Version").equals("1.0.0")|| bagInfoProperties.getString("BagIt-Profile-Version").equals("0.0.0")) ()
         else fail("version is not '1.0.0' or '0.0.0' ")
    }
    else fail(" 'BagIt-Profile-Version' not found in 'bag-info.txt'. ")
  }
  */

  def bagInfoTxtMustContainBagItProfileURI(b: BagDir ) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (readBag.getMetadata.contains("BagIt-Profile-URI")) {
      if (readBag.getMetadata.get("BagIt-Profile-URI").get(0).equals("doi:<TODO MINT DOI FOR THIS SPEC>")) ()
      else fail("BagIt-Profile-URI is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' ")
    }
    else fail(" 'BagIt-Profile-URI' not found in 'bag-info.txt'. ")
  }

  /*
  def bagInfoTxtMustContainBagItProfileURI(b: BagDir ) = Try {
    val bagInfoProperties = new PropertiesConfiguration(Paths.get(b.resolve("bag-info.txt").toUri).toFile)
    if (bagInfoProperties.containsKey("BagIt-Profile-URI")) {
      if (bagInfoProperties.getString("BagIt-Profile-URI").equals("doi:<TODO MINT DOI FOR THIS SPEC>")) ()
      else fail("BagIt-Profile-URI is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>' ")
    }
    else fail(" 'BagIt-Profile-URI' not found in 'bag-info.txt'. ")
  }
  */

  /*
  def bagInfoTxtMustContainCreatedISO8601(b: BagDir ) = Try {
    val bagInfoProperties = new PropertiesConfiguration(Paths.get(b.resolve("bag-info.txt").toUri).toFile)
    if (bagInfoProperties.containsKey("Created")) {
      val createdValue = new DateTime(bagInfoProperties.getString("Created"))
      if (createdValue.toDateTimeISO.

      else fail("  ")
  }
  */

  def bagMustContainMetadataDir(b: BagDir) = Try {
    if (Files.isDirectory(b.resolve("metadata"))) ()
    else fail("Mandatory directory 'metadata' not found in bag.")
  }



}
