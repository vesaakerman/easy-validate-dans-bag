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
import nl.knaw.dans.easy.validatebag.validation.{ RuleExpression, _ }
//import nl.knaw.dans.easy.validatebag.rules.bagit.baseBag
import nl.knaw.dans.easy.validatebag.validation.numberedRule

import scala.util.Try

package object rules {

  /**
   * Checks the bags using the rules specified in the sub-packages of this package.
   *
   * @param b                 the bag directory to check
   * @param asInfoPackageType check as AIP or SIP
   * @param isReadable        the function that checks if a file is readable
   * @return Success or Failure
   */
  def checkBag(b: BagDir, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
    require(asInfoPackageType != BOTH, "asInfoPackageType must be either SIP (default) or AIP")
    validation.checkRules(b, allRules, asInfoPackageType)
  }

  private val allRules: Map[ProfileVersion, RuleExpression] = {
    Map(
      0 -> all(
        ifThenAlso(
          or(
            numberedRule("1.1.1", bagMustBeValid, SIP),
            numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP)
          ),
          all(
            ifThenAlso(
              numberedRule("1.2.1", bagMustContainBagInfoTxt),
              all(
                ifThenAlso(
                  numberedRule("1.2.1", bagInfoTxtMayContainOne("BagIt-Profile-Version")),
                  numberedRule("1.2.1", bagInfoTxtOptionalElementMustHaveValue("BagIt-Profile-Version", "0.0.0"))),
                all(
                  numberedRule("1.2.4", bagInfoTxtCreatedMustBeIsoDate),
                  numberedRule("1.3.1", bagMustContainSHA1)
                ))
            )
            // TODO add sha1 and payload rules here
          )
        )
        // TODO add the others
      ),
      1 -> all(
        or(
          numberedRule("1.1.1", bagMustBeValid, SIP),
          numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP)
        ), all(
          ifThenAlso(
            numberedRule("1.2.1", bagMustContainBagInfoTxt),
            all(
              numberedRule("1.3.1", bagMustContainSHA1)
            )
          )
        )
      )
    )
  }
}
