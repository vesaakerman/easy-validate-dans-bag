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

import java.nio.file.Path

import nl.knaw.dans.easy.validatebag.InfoPackageType._
import nl.knaw.dans.easy.validatebag.rules.bagit._
import nl.knaw.dans.easy.validatebag.rules.structural._
//import nl.knaw.dans.easy.validatebag.rules.bagit.baseBag
import nl.knaw.dans.easy.validatebag.validation.numberedRule

import scala.util.Try

package object rules {

  /**
   * Checks the bags using the rules specified in the sub-packages of this package.
   *
   * @param b the bag directory to check
   * @param asInfoPackageType check as AIP or SIP
   * @param isReadable the function that checks if a file is readable
   * @return Success or Failure
   */
  def checkBag(b: BagDir, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
    require(asInfoPackageType != BOTH, "asInfoPackageType must be either SIP (default) or AIP")
    validation.checkRules(b, allRules, asInfoPackageType)
  }

  private val allRules: Map[ProfileVersion, RuleBase] = Map(
    0 -> Seq(
      numberedRule("1.1.1", bagMustBeValid, SIP),
      numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP),
      numberedRule("1.2.1", bagMustContainBagInfoTxt),
      numberedRule("1.2.2", bagInfoTxtMayContainBagItProfileVersionV0),
      numberedRule("1.2.3", bagInfoTxtMayContainBagItProfileURIV0),
      numberedRule("1.2.4", bagInfoTxtMustContainCreated),
      numberedRule("1.2.5", bagInfoTxtMayContainIsVersionOf),
      numberedRule("2.0.1", bagMustContainMetadataFile),
      numberedRule("2.0.2", metadataFileMustContainDatasetAndFiles)
    ),
    1 -> Seq(
      numberedRule("1.1.1", bagMustBeValid, SIP),
      numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP),
      numberedRule("1.2.1", bagMustContainBagInfoTxt),
      numberedRule("1.2.2", bagInfoTxtMustContainBagItProfileVersionV1),
      numberedRule("1.2.3", bagInfoTxtMustContainBagItProfileURIV1),
      numberedRule("1.2.4", bagInfoTxtMustContainCreated),
      numberedRule("1.2.5", bagInfoTxtMayContainIsVersionOf)
    )
  )
}
