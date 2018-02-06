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

import java.nio.file.Files

import nl.knaw.dans.easy.validatebag.lib.ValidatorAPI
import nl.knaw.dans.easy.validatebag.rules.{ Rules1, Rules2 }

import scala.util.Try

// example of how to validate a bag
// the various rules Maps are agregated and used inside the API. You only register the specific classes in here
// typically this function is written in the EasyValidateDansBagApp class; this is just an example
object RuleRunner {

  def validateBag(bag: BagDir): Try[Unit] = {
    val r1 = Rules1
    val r2 = Rules2

    ValidatorAPI.validate(bag, Seq(r1, r2))(Files.isReadable)
  }
}
