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
//import gov.loc.repository.bagit.reader.ManifestReader
import nl.knaw.dans.easy.validatebag.validation
import org.apache.commons.io.filefilter.{ DirectoryFileFilter, NotFileFilter, TrueFileFilter, WildcardFileFilter }
import org.apache.commons.io.{ FileUtils, IOCase }

import scala.language.postfixOps
import scala.util.{ Failure, Success }
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

  val bagReader: BagReader = new BagReader

  //val x: validation.RuleViolationDetailsException.type = RuleViolationDetailsException

  def bagMustContainBagInfoTxt(b: BagDir) = Try {
    if (!Files.exists(b.resolve("bag-info.txt")))
      fail("Mandatory file 'bag-info.txt' not found in bag. ")
  }

  def counterOfTheSameKeys(b: BagDir, nameOfKey: String): Int = {
    val readBag = bagReader.read(Paths.get(b.toUri))

    var counter = 0
    readBag.getMetadata.getAll.forEach { k =>
      if (k.getKey.equals(nameOfKey))
        counter = counter + 1
    }
    counter
  }

  def bagInfoTxtMustContainBagItProfileVersionV1(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (readBag.getMetadata.contains("BagIt-Profile-Version")) {
      if (counterOfTheSameKeys(b, "BagIt-Profile-Version") != 1)
        fail("Violation of v1: Several keys with the name 'BagIt-Profile-Version' exists. 'bag-info.txt' must contain exactly one 'BagIt-Profile-Version' ")
      else if (readBag.getMetadata.get("BagIt-Profile-Version").get(0) != "1.0.0")
             fail("Violation of v1: BagIt-Profile-Version is not '1.0.0'.")
    }
    else fail(" Violation of v1: 'BagIt-Profile-Version' not found in 'bag-info.txt'. 'bag-info.txt' must contain exactly one 'BagIt-Profile-Version' ")
  }

  def bagInfoTxtMayContainBagItProfileVersionV0(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (counterOfTheSameKeys(b, "BagIt-Profile-Version") <= 1) {
      if (readBag.getMetadata.contains("BagIt-Profile-Version")) {
        if (readBag.getMetadata.get("BagIt-Profile-Version").get(0) != "0.0.0")
          fail("Violation of v0: Version is not '0.0.0' ")
      }
    }
    else fail("Violation of v0: Nbr of keys with the name 'BagIt-Profile-Version' is not smaller than or equal to 1. 'bag-info.txt' may contain at most one 'BagIt-Profile-Version'. ")
  }


  def bagInfoTxtMustContainBagItProfileURIV1(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (readBag.getMetadata.contains("BagIt-Profile-URI")) {
      if (counterOfTheSameKeys(b, "BagIt-Profile-URI") != 1)
        fail("Violation of v1: Several keys with the name 'BagIt-Profile-URI' exists. 'bag-info.txt' must contain exactly one 'BagIt-Profile-URI' ")
      else if (readBag.getMetadata.get("BagIt-Profile-URI").get(0) != "doi:<TODO MINT DOI FOR THIS SPEC>")
             fail("Violation of v1: BagIt-Profile-URI is not equal to 'doi:<TODO MINT DOI FOR THIS SPEC>'. ")
    }
    else fail(" Violation of v1: 'BagIt-Profile-URI' not found in 'bag-info.txt'. 'bag-info.txt' must contain exactly one 'BagIt-Profile-URI' ")
  }

  def bagInfoTxtMayContainBagItProfileURIV0(b: BagDir) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    if (counterOfTheSameKeys(b, "BagIt-Profile-URI") <= 1) {
      if (readBag.getMetadata.contains("BagIt-Profile-URI")) {
        if (readBag.getMetadata.get("BagIt-Profile-URI").get(0) != "doi:<TODO MINT DOI FOR THIS SPEC>")
          fail("Violation of v0: BagIt-Profile-URI is not 'doi:<TODO MINT DOI FOR THIS SPEC>' ")
      }
    }
    else fail("Violation of v0: Nbr of keys with the name 'BagIt-Profile-URI' is not smaller than or equal to 1. 'bag-info.txt' may contain at most one 'BagIt-Profile-URI'. ")
  }


  //TODO These are the uri s I saw in the documentation of easy-bag-store for bag-store directory:
  /* The path of directory where archived base bag is stored on the VM */
  //val bagStoreDirectoryDefault : BagDir = Paths.get("/srv/dans.knaw.nl/bag-store")
  /* Local uri of bag-store */
  //val bagStoreDirectoryLocal : BagDir = Paths.get("http://localhost")

  //TODO I used the following path for bag-store for now.
  //TODO I also tried to write some configuration functions for bag-store but it didn't work.
  //TODO I appreciate if you have any suggestions for this case.
  val baseDir=  Paths.get("srv/dans.knaw.nl/bag-store")

  //TODO In unit tests I redefined this function as bagInfoTxtMayContainIsVersionOf(b: BagDir, bb: BagDir)
  //TODO where bb corresponds to "baseDir" defined above
  def bagInfoTxtMayContainIsVersionOf(b: BagDir) = Try {
      val readBag = bagReader.read(Paths.get(b.toUri))
      val dirOfBagStore = Paths.get(baseDir.toUri)
      //config.getString("bag-stores")
      if (counterOfTheSameKeys(b, "Is-Version-Of") <= 1) {
        if (readBag.getMetadata.contains("Is-Version-Of")) {
          if (readBag.getMetadata.get("Is-Version-Of").get(0).contains("urn:uuid:")) {
            val uuid = readBag.getMetadata.get("Is-Version-Of").get(0).stripPrefix("urn:uuid:").replaceAll("-", "").trim()
            val dirOfBaseBag: BagDir = Paths.get(dirOfBagStore + "/" + uuid.splitAt(2)._1 + "/" + uuid.splitAt(2)._2)
            if (Files.notExists(dirOfBaseBag))
              fail(" Violation of rule 1.2.5 in v1 and v0: uuid points to a nonexisting base-id ")
              //val bagId = readBag.getRootDir.getParent.getParent.getFileName.toString + readBag.getRootDir.getParent.getFileName.toString
          }
          else fail("Violation of rule 1.2.5 in v1 and v0: 'urn:uuid:' title is missing in the value of the key'Is-Version-Of' ")
        }
      }
      else fail("Violation of rule 1.2.5 in v1 and v0: nbr of provided uuids are greater than 1 in bag-info.txt")
  }


  def bagInfoTxtMustContainCreated(b: BagDir ) = Try {
    val readBag = bagReader.read(Paths.get(b.toUri))
    val lastModTimeOfBag: DateTime = new DateTime (Paths.get(b.toUri).toFile.lastModified())
    //println("of File: " + Paths.get(b.toUri))
    //println("last ModTime: " + lastModTimeOfBag)
    if (!readBag.getMetadata.contains("Created")) {
       fail("Violation of rule 1.2.4 in v1 and v0: Created is missing")
    }
    else {
      val valueOfCreated = readBag.getMetadata.get("Created").get(0)
      Try(new DateTime(valueOfCreated)) match {
        //TODO This is not a TODO list but an explanation
        //TODO DateTime function creates a failure when the argument (valueOfCreated in this case) is not in ISO806 format
        //TODO Therefore this is a quick check of whether the time is in ISO806 format or not.
        //TODO This function does not give a failure when the time is only in date format and we can still get the default timezone etc.
        //TODO For this case I put sth in the success part
        case Failure(_) => fail("Violation of rule 1.2.4 in v1 and v0: The value of 'Created' is not an instance of ISO806")
        case Success(_) => {
          val valueOfCreatedInDateTimeFormat: DateTime = new DateTime(valueOfCreated)
          //TODO Is this true : The value of 'Created' should be equal to the last modified time of the bag ???????????????????????????????
          if(valueOfCreatedInDateTimeFormat!= lastModTimeOfBag)
            fail("Violation of rule 1.2.4 in v1 and v0: The value of 'Created' should be equal to the last modified time of the bag" )
          if (!valueOfCreated.contains("T"))
            fail("Violation of rule 1.2.4 in v1 and v0: 'T' is missing in the value of Created. ISO806 format is not violated but it must be in combined date and time format.")
          if (valueOfCreatedInDateTimeFormat.toString.contains("T00:00:00") && !valueOfCreated.toString.contains("T00:00:00"))
            fail("Violation of rule 1.2.4 in v1 and v0: The value of 'Created' is in date format and the default time value 'T00:00:00' is missing. ISO806 format is not violated but it must be in combined date and time format ")
        }
      }
    }
  }

  def bagMustContainSHA1(b: BagDir) = Try {
    if (!Files.exists(b.resolve("manifest-sha1.txt")))
      fail("Mandatory file 'manifest-sha1.txt' not found in bag. ")
    else {
      val readBag = bagReader.read(Paths.get(b.toUri))
      FileUtils.listFilesAndDirs(b.resolve("data").toRealPath().toFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE).forEach(i =>
        if(i.isFile){
          if(!readBag.getPayLoadManifests.toString.contains(i.toString))
            fail("Mandatory payload manifest is missing for " + i + " manifest-sha1.txt ")
        }
      )

    }
  }

  def bagMayContainOtherManifestsAndTagManifests(b: BagDir) = Try {

    if (Files.exists(b.resolve("tagmanifest-sha1.txt"))) {
      val readBag = bagReader.read(Paths.get(b.toUri))
      FileUtils.listFilesAndDirs(b.resolve("metadata").toRealPath().toFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE).forEach(i =>
        if (i.isFile) {
          readBag.getTagManifests.toArray().foreach(j =>
            if(j.toString.contains("algorithm=SHA1") && (!j.toString.contains(i.toString)))
               fail("Mandatory tag manifest is missing for " + i + " in tagmanifest-sha1.txt ")
            )
        }
      )
      FileUtils.listFiles(b.toRealPath().toFile,
        new WildcardFileFilter("*.txt", IOCase.SENSITIVE),
        new NotFileFilter(DirectoryFileFilter.DIRECTORY)).forEach(i =>
        if(i != b.resolve("tagmanifest-sha1.txt").toRealPath().toFile){
          readBag.getTagManifests.toArray().foreach(j =>
            if(j.toString.contains("algorithm=SHA1") && (!j.toString.contains(i.toString)))
              fail("Mandatory tag manifest is missing for " + i + " in tagmanifest-sha1.txt ")
          )
        }
      )
    }

    if (Files.exists(b.resolve("tagmanifest-md5.txt"))) {
      val readBag = bagReader.read(Paths.get(b.toUri))
      FileUtils.listFilesAndDirs(b.resolve("metadata").toRealPath().toFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE).forEach(i =>
        if (i.isFile) {
          readBag.getTagManifests.toArray().foreach(j =>
            if(j.toString.contains("algorithm=MD5") && (!j.toString.contains(i.toString)))
            fail("Mandatory tag manifest is missing for " + i + " in tagmanifest-md5.txt ")
          )
        }
      )
      FileUtils.listFiles(b.toRealPath().toFile,
        new WildcardFileFilter("*.txt", IOCase.SENSITIVE),
        new NotFileFilter(DirectoryFileFilter.DIRECTORY)).forEach(i =>
        if (i != b.resolve("tagmanifest-md5.txt").toRealPath().toFile) {
          readBag.getTagManifests.toArray().foreach(j =>
            if(j.toString.contains("algorithm=MD5") && (!j.toString.contains(i.toString)))
            fail("Mandatory tag manifest is missing for " + i + " in tagmanifest-md5.txt ")
          )
        }
      )
    }

    if (Files.exists(b.resolve("manifest-md5.txt"))) {
        val readBag = bagReader.read(Paths.get(b.toUri))
        FileUtils.listFilesAndDirs(b.resolve("data").toRealPath().toFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE).forEach(i =>
          if(i.isFile){
            readBag.getPayLoadManifests.toArray().foreach(j =>
              if(j.toString.contains("algorithm=MD5") && (!j.toString.contains(i.toString)))
              fail("Mandatory payload manifest is missing for " + i + " in manifest-md5.txt ")
            )
          }
        )
      }
  }



  def bagMustContainMetadataDir(b: BagDir) = Try {
    if (Files.isDirectory(b.resolve("metadata"))) ()
    else fail("Mandatory directory 'metadata' not found in bag.")
  }



}
