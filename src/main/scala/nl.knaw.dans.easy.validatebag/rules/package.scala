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

import java.nio.file.{ Path, Paths }

import nl.knaw.dans.easy.validatebag.InfoPackageType._
import nl.knaw.dans.easy.validatebag.rules.bagit._
import nl.knaw.dans.easy.validatebag.rules.structural._
import nl.knaw.dans.easy.validatebag.validation.{ RuleExpression, _ }
import better.files._
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
  def checkBag(b: BagDir, profileVersion: ProfileVersion, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: File => Boolean): Try[Unit] = {
    require(asInfoPackageType != BOTH, "asInfoPackageType must be either SIP (default) or AIP")
    validation.checkRules(b, allRules(profileVersion), asInfoPackageType)
  }

  private val allRules: Map[ProfileVersion, RuleExpression] = {
    Map(
      profileVersion0 -> all(
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
                  numberedRule("1.2.2", bagInfoTxtMayContainOne("BagIt-Profile-Version")),
                  numberedRule("1.2.2", bagInfoTxtOptionalElementMustHaveValue("BagIt-Profile-Version", profileVersion0.toString))),
                ifThenAlso(
                  numberedRule("1.2.3", bagInfoTxtMayContainOne("BagIt-Profile-URI")),
                  numberedRule("1.2.3", bagInfoTxtOptionalElementMustHaveValue("BagIt-Profile-URI", profileVersion0Uri))),
                ifThenAlso(
                  numberedRule("1.2.4", bagInfoTxtMustContainCreated),
                  numberedRule("1.2.4", bagInfoTxtCreatedMustBeIsoDate)),
                all(
                  numberedRule("1.2.5", bagInfoTxtMayContainOne("Is-Version-Of"))
                ),
                or(
                  numberedRule("1.2.6", bagInfoTxtMustContainExactlyOne("EASY-User-Account"), AIP),
                  numberedRule("1.2.6", bagInfoTxtMustNotContain("EASY-User-Account"), SIP)
                )
              )
            ),
            all(
              numberedRule("1.3.1", bagMustContainSha1PayloadManifest)
              // 1.3.2 does not evaluation, as it states no restrictions
            )
          )
        ),
        IfThenAlso(
          numberedRule("2.1", bagMustContainDir(Paths.get("metadata"))),
          all(
            numberedRule("2.2", bagMustContainFile(Paths.get("metadata/dataset.xml"))),
            numberedRule("2.2", bagMustContainFile(Paths.get("metadata/files.xml")))
          )
        )
      ),

      profileVersion1 -> all(
        or(
          numberedRule("1.1.1", bagMustBeValid, SIP),
          numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP)
        ), all(
          ifThenAlso(
            numberedRule("1.2.1", bagMustContainBagInfoTxt),
            all(
              numberedRule("1.3.1", bagMustContainSha1PayloadManifest)
            )
          )
        )
      )
    )
  }
}
