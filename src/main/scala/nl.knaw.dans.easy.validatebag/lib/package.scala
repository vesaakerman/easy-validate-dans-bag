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

import nl.knaw.dans.easy.validatebag.lib.InfoPackageType.InfoPackageType

import scala.util.Try

package object lib {

  type ProfileVersion = Int
  type RuleNumber = String
  type ErrorMessage = String
  type BagDir = Path
  type BagStoreBaseDir = Path
  type Rule = BagDir => Try[Unit]
  type NumberedRule = (RuleNumber, Rule, InfoPackageType)
  type RuleBase = Seq[NumberedRule]

  /**
   * Internal exception, only specifying the details of a rule violation, but not the rule number, as this number
   * may vary across versions of the Profile.
   *
   * @param details details about the violation
   */
  private[lib] case class RuleViolationDetailsException(details: String) extends Exception(details)

  /**
   * Exception specifying the rule violated and what the violation consisted of. The number refers back
   * to the DANS BagIt Profile documents.
   *
   * @param ruleNr  rule number violated.
   * @param details details about the violation
   */
  case class RuleViolationException(ruleNr: RuleNumber, details: String) extends Exception(s"[$ruleNr] $details")
}
