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
import nl.knaw.dans.easy.validatebag.validation._

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

  private val allRules: Map[ProfileVersion, ValidationAlgebra] = {
    Map(
      0 -> all(
        sub(
          either(
            numberedRule("1.1.1", bagMustBeValid, SIP),
            numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP)
          ),
          all(
            sub(
              numberedRule("1.2.1", bagMustContainBagInfoTxt),
              all(
                numberedRule("1.2.2", bagInfoTxtMustContainBagItProfileVersion("0.0.0"))
                // TODO add other rules regarding bagInfo here
              )
            )
            // TODO add sha1 and payload rules here
          )
        )
        // TODO add the others
      )
    )
  }

  /*
   * - (1.1.1, The bag MUST be VALID according to ...,          SIP) \/ (1.1.2, The bag MUST be virtually-valid in the ...,    AIP)
   *     - (1.2.1, The bag MUST contain a bag-info.txt file..., BOTH)
   *         - (1.2.2, The bag-info.txt file MAY contain  ...,  BOTH)
   *         - (1.2.3, The bag-info.txt file MAY contain  ...,  BOTH)
   *         - (1.2.4, The bag-info.txt file MUST contain ...,  BOTH)
   *         - (1.2.5, The bag-info.txt file MAY contain  ...,  BOTH)
   *     - (1.3.1, The bag MUST have a SHA-1 payload ...,       BOTH)
   *     - (1.3.2, The bag MAY have other payload manifests..., BOTH)
   * - (2.1.1, The bag MUST have a tag-directory ...,           BOTH)
   *     - (2.1.2, The metadata directory MUST contain ...,     BOTH)
   *         - (3.1.1, The file metadata/dataset.xml MUST ...,  BOTH)
   *         - (3.1.2, The file metadata/dataset.xml MAY ...,   BOTH)
   *         - (3.1.3, The file metadata/dataset.xml MUST ...,  AIP)
   *         - (3.2.1, ...
   *         - (3.2.2, ...
   *         - (3.2.3, ...
   *         - (3.2.4, ...
   *         - (3.2.5, ...
   *         - (3.2.6, ...
   *         - (3.2.7, ...
   */
}
