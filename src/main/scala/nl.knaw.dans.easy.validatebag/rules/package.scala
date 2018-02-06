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

import java.nio.file.{ Files, Path }

import nl.knaw.dans.easy.validatebag.InfoPackageType._
import nl.knaw.dans.easy.validatebag.rules.bagit._
import nl.knaw.dans.easy.validatebag.validation._

import scala.util.Try

package object rules {
  def checkBag(b: BagDir, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
    validation.checkRules(b, allRules, asInfoPackageType)
  }

  private val allRules: Map[ProfileVersion, RuleBase] = Map(
    0 -> Seq(
      numberedRule("1.1.1", bagMustBeValid(), SIP),
      numberedRule("1.1.2", bagMustBeVirtuallyValid(), AIP),
      numberedRule("1.2.1", bagMustContainBagInfoTxt()),
      numberedRule("1.2.2", bagInfoTxtMustContainBagItProfileVersion("0.0.0"))
    )
  )
}
