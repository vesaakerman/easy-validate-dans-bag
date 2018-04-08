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

import better.files._
import nl.knaw.dans.easy.validatebag.InfoPackageType.InfoPackageType

import scala.util.Try

package object validatebag {
  type ProfileVersion = Int
  type RuleNumber = String
  type ErrorMessage = String
  type BagDir = File
  type Rule = BagDir => Try[Unit]
  type NumberedRule = (RuleNumber, Rule, InfoPackageType)

  val profileVersion0 = 0
  val profileVersion0Uri = "doi:10.17026/dans-z52-ybfe"
  val profileVersion1 = 1
  val profileVersion1Uri = "doi:10.17026/dans-zf3-q54n"

  object InfoPackageType extends Enumeration {
    type InfoPackageType = Value
    val SIP, AIP, BOTH = Value
  }

  import InfoPackageType._

  def validateDansBag(b: BagDir, profileVersion: ProfileVersion, infoPackageType: InfoPackageType = SIP): Try[Unit] = {
    implicit val isReadable: File => Boolean = _.isReadable
    rules.checkBag(b, profileVersion, infoPackageType)
  }
}
