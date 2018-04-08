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

import better.files._
import gov.loc.repository.bagit.reader.BagReader
import nl.knaw.dans.easy.validatebag.InfoPackageType.{ InfoPackageType, _ }
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

package object validation extends DebugEnhancedLogging {

  private val bagReader = new BagReader()

  /**
   * Exception specifying the rule violated and what the violation consisted of. The number refers back
   * to the DANS BagIt Profile documents.
   *
   * @param ruleNr  rule number violated.
   * @param details details about the violation
   */
  case class RuleViolationException(ruleNr: RuleNumber, details: String) extends Exception(s"[$ruleNr] $details")

  /**
   * Internal exception, only specifying the details of a rule violation, but not the rule number, as this number
   * may vary across versions of the Profile.
   *
   * @param details details about the violation
   */
  case class RuleViolationDetailsException(details: String) extends Exception(details)

  private case class RuleNotApplicableException() extends Exception("rule not applicable, testing for something else")

  /**
   * Signals a rule violation. This function should be called from the rule functions to indicate that
   * the rule was not met.
   *
   * @param details the details about the rule violation
   */
  def fail(details: String): Unit = throw RuleViolationDetailsException(details)

  /**
   * Creates a NumberedRule.
   *
   * @param ruleNumber      the number of the rule in the profile version
   * @param rule            the rule function
   * @param infoPackageType the Information Package type(s) that this rule applies to
   * @return
   */

  def numberedRule(ruleNumber: RuleNumber, rule: Rule, infoPackageType: InfoPackageType = BOTH): NumberedRule = {
    (ruleNumber, rule, infoPackageType)
  }

  /**
   * A rule expression is one rule or a composite rule. Rules can be combined in several ways:
   *
   * - Or: one of the rules must succeed
   * - All: all rules must succeed
   * - IfThenAlso: If the first rule succeeds, the rest must also succeed
   *
   */
  sealed abstract class RuleExpression
  case class Atom(rule: NumberedRule) extends RuleExpression
  case class Or(left: NumberedRule, right: NumberedRule) extends RuleExpression
  case class AllRules(head: NumberedRule, tail: NumberedRule*) extends RuleExpression
  case class IfThenAlso(parent: RuleExpression, child: RuleExpression) extends RuleExpression
  case class AllExpr(head: RuleExpression, tail: RuleExpression*) extends RuleExpression

  // atomic action
  def atom(rule: NumberedRule): RuleExpression = {
    Atom(rule)
  }

  // either the left or right rule should succeed
  def or(left: NumberedRule, right: NumberedRule): RuleExpression = {
    Or(left, right)
  }

  // all these rules should succeed
  def all(head: NumberedRule, tail: NumberedRule*): RuleExpression = {
    AllRules(head, tail: _*)
  }

  // all validations should succeed
  def all(head: RuleExpression, tail: RuleExpression*): RuleExpression = {
    AllExpr(head, tail: _*)
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def ifThenAlso(parent: NumberedRule, child: NumberedRule): RuleExpression = {
    IfThenAlso(atom(parent), atom(child))
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def ifThenAlso(parent: NumberedRule, child: RuleExpression): RuleExpression = {
    IfThenAlso(atom(parent), child)
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def ifThenAlso(parent: RuleExpression, child: NumberedRule): RuleExpression = {
    IfThenAlso(parent, atom(child))
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def ifThenAlso(parent: RuleExpression, child: RuleExpression): RuleExpression = {
    IfThenAlso(parent, child)
  }

  /**
   * Validates if the bag pointed to compliant with the DANS BagIt Profile version it claims to
   * adhere to. If no claim is made, by default it is assumed that the bag is supposed to comply
   * with v0.
   *
   * @param bag               the bag to validate
   * @param rules             a map containing a RuleBase for each profile version
   * @param asInfoPackageType validate as SIP (default) or AIP
   * @param isReadable        function to check the readability of a file (added for unit testing purposes)
   * @return Success if compliant, Failure if not compliant or an error occurred. The Failure will contain
   *         `nl.knaw.dans.lib.error.CompositeException`, which will contain a [[RuleViolationException]]
   *         for every violation of the DANS BagIt Profile rules.
   */
  def checkRules(bag: BagDir, rules: RuleExpression, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: File => Boolean): Try[Unit] = {
    /**
     * `isReadable` was added because unit testing this by actually setting files on the file system to non-readable and back
     * can get messy. After a failed build one might be left with a target folder that refuses to be cleaned. Unless you are
     * aware of the particular details of the test this will be very confusing.
     */
    trace(bag, asInfoPackageType)
    for {
      _ <- checkIfValidationCanProceed(bag)
      result <- evaluateRules(bag, rules, asInfoPackageType)
    } yield result
  }


  private def checkIfValidationCanProceed(bag: BagDir)(implicit isReadable: File => Boolean): Try[Unit] = Try {
    trace(bag)
    debug(s"Checking existence of $bag")
    require(bag.exists, "Bag does not exist")
    debug(s"Checking readability of $bag")
    require(isReadable(bag), s"Bag is non-readable") // Using the passed-in isReadable function here!!
    debug(s"Checking if $bag is directory")
    require(bag.isDirectory, "Bag must be a directory")
    bag.walk().foreach {
      f =>
        debug(s"Checking readability of $f")
        require(isReadable(f), s"Found non-readable file $f") // Using the passed-in isReadable function here!!
    }
  }

  private def evaluateRules(bag: BagDir, rules: RuleExpression, asInfoPackageType: InfoPackageType = SIP): Try[Unit] = {

    def ruleApplies(ipType: InfoPackageType) = {
      ipType == asInfoPackageType || ipType == BOTH
    }

    def ruleNotApplicableToSuccess: PartialFunction[Throwable, Try[Unit]] = {
      case RuleNotApplicableException() => Success(())
    }

    def evaluate(expression: RuleExpression): Try[Unit] = {
      expression match {
        case Atom((nr, rule, ipType)) if ruleApplies(ipType) =>
          rule(bag).recoverWith {
            case RuleViolationDetailsException(details) => Failure(RuleViolationException(nr, details))
          }
        case Atom(_) =>
          Failure(RuleNotApplicableException())
        case Or(left, right) =>
          evaluate(atom(left)).recoverWith {
            case _: RuleNotApplicableException => evaluate(atom(right))
            case e => evaluate(atom(right)).recoverWith {
              case _: RuleNotApplicableException => Failure(e)
              case e2 => Failure(new CompositeException(e, e2))
            }
          }
        case AllRules(head, tail @ _*) =>
          (head +: tail).withFilter { case (_, _, ipType) => ruleApplies(ipType) }.map(atom) match {
            case Seq() => Success(())
            case Seq(h, t @ _*) => evaluate(all(h, t: _*))
          }
        case IfThenAlso(parent, child) =>
          evaluate(parent).recoverWith(ruleNotApplicableToSuccess).flatMap(_ => evaluate(child))
        case AllExpr(head, tail @ _*) =>
          (head +: tail)
            .map(alg => evaluate(alg).recoverWith(ruleNotApplicableToSuccess))
            .collectResults
            .map(_ => ())
      }
    }

    evaluate(rules).recoverWith(ruleNotApplicableToSuccess)
  }

  def getProfileVersion(bag: BagDir): Try[ProfileVersion] = Try {
    if ((bag / "bag-info.txt").exists) {
      val b = bagReader.read(bag.path)
      Option(b.getMetadata.get("BagIt-Profile-Version")).map(_.asScala.toList match {
        case (v :: _) => Try { v.split('.').head.toInt }.recover { // There will always be a 'head', even if there are no dots in the version value
          case _: NumberFormatException => 0
        }.getOrElse(0)
        case _ => 0
      }).getOrElse(0)
    }
    /*
     * This will fail later, as bag-info.txt is mandatory in all versions, but we don't report that here,
     * to keep this function simple.
     */
    else 0
  }
}
