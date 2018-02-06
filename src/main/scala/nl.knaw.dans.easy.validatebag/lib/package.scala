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
