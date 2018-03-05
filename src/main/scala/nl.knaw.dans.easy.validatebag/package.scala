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
package nl.knaw.dans.easy

import java.nio.file.{ Files, Path }

import nl.knaw.dans.easy.validatebag.InfoPackageType.InfoPackageType

import scala.util.Try

package object validatebag {
  type ProfileVersion = Int
  type RuleNumber = String
  type ErrorMessage = String
  type BagDir = Path
  type Rule = BagDir => Try[Unit]
  type NumberedRule = (RuleNumber, Rule, InfoPackageType)

  object InfoPackageType extends Enumeration {
    type InfoPackageType = Value
    val SIP, AIP, BOTH = Value
  }

  import InfoPackageType._

  object ValidationResult extends Enumeration {
    type ValidationResult = Value
    val COMPLIANT, NOT_COMPLIANT = Value
  }

  def validateDansBag(b: BagDir, infoPackageType: InfoPackageType = SIP): Try[Unit] = {
    implicit val isReadable: Path => Boolean = Files.isReadable
    rules.checkBag(b, infoPackageType)
  }
}
