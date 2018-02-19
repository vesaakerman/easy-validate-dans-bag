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

import java.nio.file.Files

import gov.loc.repository.bagit.reader.BagReader
import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.{ RuleViolationDetailsException, fail }

import scala.util.Try

package object structural {

  val bagReader: BagReader = new BagReader

  def bagMustContainMetadataFile(b: BagDir) = Try {
    if (!Files.exists(b.resolve("metadata")))
      fail("Mandatory file 'metadata' not found in bag. ")
    if(Files.exists(b.resolve("metadata"))){
      val fileName = b.resolve("metadata").toRealPath().getFileName()
      if(fileName.toString.trim()!= fileName.toString)
        fail("No space is allowed in the file name 'metadata' ")
      if(fileName.toString.toLowerCase != fileName.toString) {
        println(fileName.toString)
        fail("Uppercase chars are not allowed in the file name 'metadata' ")
      }
    }

  }

}
