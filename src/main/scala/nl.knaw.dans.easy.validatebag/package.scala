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

import scala.util.{ Failure, Success, Try }
import nl.knaw.dans.lib.error._

package object validatebag {
  object InfoPackageType extends Enumeration {
    type InfoPackageType = Value
    val SIP, AIP, BOTH = Value
  }
  import InfoPackageType._

  type ProfileVersion = Int
  type RuleNumber = String
  type ErrorMessage = String
  type Rule = Path => Try[Unit]
  type RuleBase = Seq[(RuleNumber, Rule, InfoPackageType)]


  case class RuleViolationDetailsException(details: String) extends Exception(details)

  /**
   * Exception specifying the rule violated and what the violation consisted of. The number refers back
   * to the DANS BagIt Profile documents.
   *
   * @param ruleNr rule number violated.
   * @param details details about the violation
   */
  case class RuleViolationException(ruleNr: RuleNumber, details: String) extends Exception(s"[$ruleNr] $details")


  /**
   * The rule functions for all versions of the profile.
   */
  val bagMustBeValid: Rule = (bag: Path) => Try {}
  val bagMustContainMetadataDir: Rule = (bag: Path) => Try {
    if (Files.isDirectory(bag.resolve("metadata"))) ()
    else fail("Mandatory directory 'metadata' not found in bag")
  }
  // TODO: Create lambdas for all the rules


  private def fail(msg: String): Unit = throw RuleViolationDetailsException(msg)

  private def rule(numberedRule: (RuleNumber, Rule), infoPackageType: InfoPackageType = BOTH): (RuleNumber, Rule, InfoPackageType) = {
    val (nr, r) = numberedRule
    (nr, r, infoPackageType)
  }

  /**
   * The rule bases for each version, each of which contains a mapping from rule number to rule function. The rule functions
   * are created above.
   */
  val rules: Map[ProfileVersion, RuleBase] = Map(
    // TODO: Add the rules to the respective rule bases
    0 -> Seq(
      rule("1.1" ->  bagMustBeValid, SIP),
      rule("1.2" -> bagMustContainMetadataDir)
    )
  )

  /**
   * Validates if the bag pointed to compliant with the DANS BagIt Profile version it claims to
   * adhere to. If no claim is made, by default it is assumed that the bag is supposed to comply
   * with v0.
   *
   * @param bag the bag to validate
   * @return Success if compliant, Failure if not compliant or an error occurred. The Failure will contain
   *         [[nl.knaw.dans.lib.error.CompositeException]], which will contain a [[RuleViolationException]]
   *         for every violation of the DANS BagIt Profile rules.
   */
  def validateDansBag(bag: Path): Try[Unit] = {
    // TODO: get profile version
    val result = rules(0).map {
      case (nr, rule, ipType) =>
        rule(bag).recoverWith {
        case RuleViolationDetailsException(details) => Failure(RuleViolationException(nr, details))
      }
    }

    result.collectResults.map(_ => ())
  }
}
