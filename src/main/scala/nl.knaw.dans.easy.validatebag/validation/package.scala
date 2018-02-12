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

import java.nio.file.{ Files, Path }

import nl.knaw.dans.easy.validatebag.InfoPackageType.{ InfoPackageType, _ }
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

package object validation extends DebugEnhancedLogging {

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

  sealed abstract class ValidationAlgebra
  case class Eval(rule: NumberedRule) extends ValidationAlgebra
  case class OneOf(left: NumberedRule, right: NumberedRule) extends ValidationAlgebra
  case class All(head: NumberedRule, tail: NumberedRule*) extends ValidationAlgebra
  case class Sub(parent: ValidationAlgebra, child: ValidationAlgebra) extends ValidationAlgebra
  case class AllAlgs(head: ValidationAlgebra, tail: ValidationAlgebra*) extends ValidationAlgebra

  // atomic action
  def eval(rule: NumberedRule): ValidationAlgebra = {
    Eval(rule)
  }

  // either the left or right rule should succeed
  def either(left: NumberedRule, right: NumberedRule): ValidationAlgebra = {
    OneOf(left, right)
  }

  // all these rules should succeed
  def all(head: NumberedRule, tail: NumberedRule*): ValidationAlgebra = {
    All(head, tail: _*)
  }

  // all validations should succeed
  def all(head: ValidationAlgebra, tail: ValidationAlgebra*): ValidationAlgebra = {
    AllAlgs(head, tail: _*)
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def sub(parent: NumberedRule, child: NumberedRule): ValidationAlgebra = {
    Sub(eval(parent), eval(child))
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def sub(parent: NumberedRule, child: ValidationAlgebra): ValidationAlgebra = {
    Sub(eval(parent), child)
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def sub(parent: ValidationAlgebra, child: NumberedRule): ValidationAlgebra = {
    Sub(parent, eval(child))
  }

  // if parent succeeds, child should also succeed; if parent fails, child is not evaluated
  def sub(parent: ValidationAlgebra, child: ValidationAlgebra): ValidationAlgebra = {
    Sub(parent, child)
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
  def checkRules(bag: BagDir, rules: Map[ProfileVersion, ValidationAlgebra], asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
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

  private def checkIfValidationCanProceed(bag: BagDir)(implicit isReadable: Path => Boolean): Try[Unit] = Try {
    trace(bag)
    debug(s"Checking existence of $bag")
    require(Files.exists(bag), "Bag does not exist")
    debug(s"Checking readability of $bag")
    require(isReadable(bag), s"Bag is non-readable")
    debug(s"Checking if $bag is directory")
    require(Files.isDirectory(bag), "Bag must be a directory")
    resource.managed(Files.walk(bag)).acquireAndGet {
      _.iterator().asScala.foreach {
        f =>
          debug(s"Checking readability of $f")
          require(isReadable(f), s"Found non-readable file $f")
      }
    }
  }

  private def evaluateRules(bag: BagDir, rules: Map[ProfileVersion, ValidationAlgebra], asInfoPackageType: InfoPackageType = SIP): Try[Unit] = {

    def ruleApplies(ipType: InfoPackageType) = {
      ipType == asInfoPackageType || ipType == BOTH
    }

    def ruleNotApplicableToSuccess: PartialFunction[Throwable, Try[Unit]] = {
      case RuleNotApplicableException() => Success(())
    }

    def evaluate(algebra: ValidationAlgebra): Try[Unit] = {
      algebra match {
        case Eval((nr, rule, ipType)) if ruleApplies(ipType) =>
          rule(bag).recoverWith {
            case RuleViolationDetailsException(details) => Failure(RuleViolationException(nr, details))
          }
        case Eval(_) =>
          Failure(RuleNotApplicableException())
        case OneOf(left, right) =>
          evaluate(eval(left)).recoverWith {
            case _: RuleNotApplicableException => evaluate(eval(right))
            case e => evaluate(eval(right)).recoverWith {
              case _: RuleNotApplicableException => Failure(e)
              case e2 => Failure(new CompositeException(e, e2))
            }
          }
        case All(head, tail @ _*) =>
          (head +: tail).withFilter { case (_, _, ipType) => ruleApplies(ipType) }.map(eval) match {
            case Seq() => Success(())
            case Seq(h, t @ _*) => evaluate(all(h, t: _*))
          }
        case Sub(parent, child) =>
          evaluate(parent).recoverWith(ruleNotApplicableToSuccess).flatMap(_ => evaluate(child))
        case AllAlgs(head, tail @ _*) =>
          (head +: tail)
            .map(alg => evaluate(alg).recoverWith(ruleNotApplicableToSuccess))
            .collectResults
            .map(_ => ())
      }
    }

    evaluate(rules(getProfileVersion(bag)))
      .recoverWith {
        case _: RuleNotApplicableException => Success(())
      }
  }

  private def getProfileVersion(bag: BagDir): ProfileVersion = {
    0 // TODO: retrieve actual version
  }
}
