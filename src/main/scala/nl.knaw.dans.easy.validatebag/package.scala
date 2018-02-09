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

  def validateDansBag(b: BagDir, infoPackageType: InfoPackageType = SIP): Try[Unit] = {
    implicit val isReadable: Path => Boolean = Files.isReadable
    rules.checkBag(b, infoPackageType)
  }

  /*
   * eval(X): Y
   * either(X, X): Y
   * all(X*): Y == all(map eval X*)
   * all(Y*): Y
   * sub(X, X): Y = sub(eval X, eval X)
   * sub(X, Y): Y = sub(eval X, Y)
   * sub(Y, X): Y = sub(Y, eval X)
   * sub(Y, Y): Y
   */

  /*
   * val bagValid: Result = either("1.1.1", "1.1.2")
   * val bagInfoExists: Result = eval("1.2.1")
   * val bagInfoTests: Result = all("1.2.2", "1.2.3", "1.2.4", "1.2.5")
   * val sha1: Result = eval("1.3.1")
   * val payload: Result = eval("1.3.2")
   *
   * val bagInfo: Result = sub(bagInfoExists, bagInfoTests)
   * val ifValid: Result = all(bagInfo, sha1, payload)
   * val valid: Result = sub(bagValid, ifValid)
   *
   * val total: Result = all(valid, ...)
   */

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
