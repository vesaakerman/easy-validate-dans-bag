package nl.knaw.dans.easy

import java.nio.file.Path

import scala.util.{ Success, Try }
import nl.knaw.dans.lib.error._

package object validatebag {
  object InfoPackageType extends Enumeration {
    type InfoPackageType = Value
    val SIP, AIP, BOTH = Value
  }
  import InfoPackageType._

  type ProfileVersion = Int
  type RuleNumber = String
  type Rule = Path => Try[Unit]
  type RuleBase = Seq[(RuleNumber, Rule, InfoPackageType)]


  /**
   * Exception specifying the rule violated and what the violation consisted of. The number refers back
   * to the DANS BagIt Profile documents.
   *
   * @param profileVersion the version of the Profile used
   * @param ruleNr rule number violated.
   * @param details details about the violation
   */
  case class RuleViolationException(profileVersion: ProfileVersion, ruleNr: RuleNumber, details: String) extends Exception(details)

  /**
   * The rule functions for all versions of the profile.
   */
  val bagMustBeValid: Rule = (bag: Path) => Success(())
  // TODO: Create lambdas for all the rules

  private def rule(numberedRule: (RuleNumber, Rule), infoPackageType: InfoPackageType = BOTH): (RuleNumber, Rule, InfoPackageType) = {
    case (nr, r) => (nr, r, infoPackageType)
  }

  /**
   * The rule bases for each version, each of which contains a mapping from rule number to rule function. The rule functions
   * are created above.
   */
  val rules: Map[ProfileVersion, RuleBase] = Map(
    // TODO: Add the rules to the respective rule bases
    0 -> Seq(
      rule("1.1" ->  bagMustBeValid, SIP),
    )
  )




  /**
   * Validates if the bag pointed to compliant with the DANS BagIt Profile version it claims to
   * adhere to. If no claim is made, by default it is assumed that the bag is supposed to comply
   * with v0.
   *
   * @param bag the bag to validate
   * @return Success if compliant, Failure if not compliant or an error occurred. If
   */
  def validateDansBag(bag: Path): Try[Unit] = {
    // TODO: get profile version

    rules(0).map {
      case (nr, rule, ipType) => rule(bag)
    }.collectResults.map(_ => ())
  }
}
