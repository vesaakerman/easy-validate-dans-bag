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
package nl.knaw.dans.easy.validatebag.validation

import java.nio.file.{ Files, Path }
import java.util.concurrent.atomic.AtomicBoolean

import nl.knaw.dans.easy.validatebag.InfoPackageType._
import nl.knaw.dans.easy.validatebag.{ Rule, RuleNumber, TestSupportFixture, validation }
import nl.knaw.dans.lib.error.CompositeException

import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }

class ValidationSpec extends TestSupportFixture {

  def successRule: (Rule, AtomicBoolean) = {
    val ran = new AtomicBoolean(false)
    val f: Rule = _ => {
      ran.set(true)
      Success(())
    }
    (f, ran)
  }

  def failedRule(msg: String): (Rule, AtomicBoolean) = {
    val ran = new AtomicBoolean(false)
    val f: Rule = _ => {
      ran.set(true)
      Try { validation.fail(msg) }
    }
    (f, ran)
  }

  def atomicSuccess(infoPackageType: InfoPackageType = BOTH): (RuleExpression, AtomicBoolean) = {
    val (f, ran) = successRule
    (atom(numberedRule("1.1.1", f, infoPackageType)), ran)
  }

  def atomicFailure(ruleNumber: RuleNumber, msg: String, infoPackageType: InfoPackageType = BOTH): (RuleExpression, AtomicBoolean) = {
    val (f, ran) = failedRule(msg)
    (atom(numberedRule(ruleNumber, f, infoPackageType)), ran)
  }

  private implicit def ruleToMap(rule: RuleExpression): Map[Int, RuleExpression] = {
    Map(0 -> rule)
  }

  // success case will be covered by the validation algebra tests
  "checkIfValidationCanProceed" should "fail when the bag is not readable" in {
    val (rule1, ran1) = successRule
    val rule = atom(numberedRule("1.1.1", rule1))

    inside(checkRules(testDir, rule)(isReadable = _ => false)) {
      case Failure(e: IllegalArgumentException) =>
        e.getMessage should include("Bag is non-readable")
    }
    ran1.get shouldBe false
  }

  it should "fail when the bag does not exist" in {
    val bagDir = testDir.resolve("not-exists")
    val (rule1, ran1) = successRule
    val rule = atom(numberedRule("1.1.1", rule1))

    inside(checkRules(bagDir, rule)) {
      case Failure(e: IllegalArgumentException) =>
        e.getMessage should include("Bag does not exist")
    }
    ran1.get shouldBe false
  }

  it should "fail when the bag is not a directory" in {
    val file = testDir.resolve("not-a-directory.txt")
    Files.write(file, "this is not a directory".getBytes)

    val (rule1, ran1) = successRule
    val rule = atom(numberedRule("1.1.1", rule1))

    inside(checkRules(file, rule)) {
      case Failure(e: IllegalArgumentException) =>
        e.getMessage should include("Bag must be a directory")
    }
    ran1.get shouldBe false
  }

  it should "fail when any of the files in the bag is not readable" in {
    val bag = Files.createDirectories(testDir.resolve("bag"))
    val file = bag.resolve("not-a-directory.txt")
    Files.write(file, "this is not a directory".getBytes)

    def fileNotReadable(path: Path): Boolean = path != file

    val (rule1, ran1) = successRule
    val rule = atom(numberedRule("1.1.1", rule1))

    inside(checkRules(bag, rule)(fileNotReadable)) {
      case Failure(e: IllegalArgumentException) =>
        e.getMessage should include(s"Found non-readable file $file")
    }
    ran1.get shouldBe false
  }

  "checkRules with atomic" should "succeed with a successful rule for the same IPT" in {
    val (rule1, ran1) = successRule
    val rule = atom("1.1.1", rule1, SIP)

    checkRules(testDir, rule, SIP) shouldBe a[Success[_]]
    ran1.get shouldBe true
  }

  it should "succeed with a successful rule if the rule has IPT=BOTH" in {
    val (rule1, ran1) = successRule
    val rule = atom("1.1.1", rule1, BOTH)

    checkRules(testDir, rule, SIP) shouldBe a[Success[_]]
    ran1.get shouldBe true
  }

  it should "not run the rule if the IPT is not the same" in {
    val (rule1, ran1) = successRule
    val rule = atom("1.1.1", rule1, AIP)

    checkRules(testDir, rule, SIP) shouldBe a[Success[_]]
    ran1.get shouldBe false
  }

  it should "fail if the rule fails and it has the same IPT" in {
    val (rule1, ran1) = failedRule("err1")
    val rule = atom("1.1.2", rule1, SIP)

    checkRules(testDir, rule, SIP) should matchPattern {
      case Failure(RuleViolationException("1.1.2", "err1")) =>
    }
    ran1.get shouldBe true
  }

  "checkRules with either" should "succeed when neither one of the rules apply" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val rule = or(
      numberedRule("1.1.1", rule1, SIP),
      numberedRule("1.1.2", rule2, SIP)
    )

    checkRules(testDir, rule, AIP) shouldBe a[Success[_]]
    ran1.get shouldBe false
    ran2.get shouldBe false
  }

  it should "succeed when the first rule succeeds (don't even evaluate the second rule)" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = failedRule("err")
    val rule = or(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2)
    )

    checkRules(testDir, rule) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe false
  }

  it should "succeed when the first rule fails, but the second one succeeds" in {
    val (rule1, ran1) = failedRule("err")
    val (rule2, ran2) = successRule
    val rule = or(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2)
    )

    checkRules(testDir, rule) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe true
  }

  it should "succeed when the first rule doesn't apply, but the second one does" in {
    val (rule1, ran1) = failedRule("err")
    val (rule2, ran2) = successRule
    val rule = or(
      numberedRule("1.1.1", rule1, SIP),
      numberedRule("1.1.2", rule2, AIP)
    )

    checkRules(testDir, rule, AIP) shouldBe a[Success[_]]
    ran1.get shouldBe false
    ran2.get shouldBe true
  }

  it should "fail when only the second rule applies, but fails" in {
    val (rule1, ran1) = failedRule("err1")
    val (rule2, ran2) = failedRule("err2")
    val rule = or(
      numberedRule("1.1.1", rule1, SIP),
      numberedRule("1.1.2", rule2, AIP)
    )

    checkRules(testDir, rule, AIP) should matchPattern {
      case Failure(RuleViolationException("1.1.2", "err2")) =>
    }
    ran1.get shouldBe false
    ran2.get shouldBe true
  }

  it should "fail when both rules apply, but fail" in {
    val (rule1, ran1) = failedRule("err1")
    val (rule2, ran2) = failedRule("err2")
    val rule = or(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2)
    )

    checkRules(testDir, rule) should matchPattern {
      case Failure(CompositeException(Seq(
        RuleViolationException("1.1.1", "err1"),
        RuleViolationException("1.1.2", "err2"),
      ))) =>
    }
    ran1.get shouldBe true
    ran2.get shouldBe true
  }

  "checkRules with all" should "succeed when all rules apply and succeed" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val (rule3, ran3) = successRule
    val rule = validation.all(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2),
      numberedRule("1.1.3", rule3)
    )

    checkRules(testDir, rule) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe true
    ran3.get shouldBe true
  }

  it should "succeed when only some rules apply and all of those succeed (while the others fail)" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = failedRule("err2")
    val (rule3, ran3) = successRule
    val rule = validation.all(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2, SIP),
      numberedRule("1.1.3", rule3)
    )

    checkRules(testDir, rule, AIP) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe false
    ran3.get shouldBe true
  }

  it should "succeed when none of the rules apply" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val (rule3, ran3) = successRule
    val rule = validation.all(
      numberedRule("1.1.1", rule1, AIP),
      numberedRule("1.1.2", rule2, AIP),
      numberedRule("1.1.3", rule3, AIP)
    )

    checkRules(testDir, rule, SIP) shouldBe a[Success[_]]
    ran1.get shouldBe false
    ran2.get shouldBe false
    ran3.get shouldBe false
  }

  it should "succeed with only one rule" in {
    val (rule1, ran1) = successRule
    val rule = validation.all(
      numberedRule("1.1.1", rule1)
    )

    checkRules(testDir, rule) shouldBe a[Success[_]]
    ran1.get shouldBe true
  }

  it should "fail when only one of the rules fails" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = failedRule("err2")
    val (rule3, ran3) = successRule
    val rule = validation.all(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2),
      numberedRule("1.1.3", rule3)
    )

    checkRules(testDir, rule) should matchPattern {
      case Failure(CompositeException(Seq(RuleViolationException("1.1.2", "err2")))) =>
    }
    ran1.get shouldBe true
    ran2.get shouldBe true
    ran3.get shouldBe true
  }

  it should "fail when multiple of the rules fail" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = failedRule("err2")
    val (rule3, ran3) = failedRule("err3")
    val rule = validation.all(
      numberedRule("1.1.1", rule1),
      numberedRule("1.1.2", rule2),
      numberedRule("1.1.3", rule3)
    )

    checkRules(testDir, rule) should matchPattern {
      case Failure(CompositeException(Seq(
        RuleViolationException("1.1.2", "err2"),
        RuleViolationException("1.1.3", "err3"),
      ))) =>
    }
    ran1.get shouldBe true
    ran2.get shouldBe true
    ran3.get shouldBe true
  }

  "checkRules with sub" should "succeed when both parent and child succeed" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val rule = ifThenAlso(
      numberedRule("1.1", rule1),
      numberedRule("1.1.1", rule2)
    )

    checkRules(testDir, rule) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe true
  }

  it should "success when the parent doesn't apply (the child is still evaluated!)" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val rule = ifThenAlso(
      numberedRule("1.1", rule1, SIP),
      numberedRule("1.1.1", rule2)
    )

    checkRules(testDir, rule, AIP) shouldBe a[Success[_]]
    ran1.get shouldBe false
    ran2.get shouldBe true
  }

  it should "succeed when the child doesn't apply" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = successRule
    val rule = ifThenAlso(
      numberedRule("1.1", rule1),
      numberedRule("1.1.1", rule2, SIP)
    )

    checkRules(testDir, rule, AIP) shouldBe a[Success[_]]
    ran1.get shouldBe true
    ran2.get shouldBe false
  }

  it should "fail when the parent fails (the child is not evaluated)" in {
    val (rule1, ran1) = failedRule("err1")
    val (rule2, ran2) = successRule
    val rule = ifThenAlso(
      numberedRule("1.1", rule1),
      numberedRule("1.1.1", rule2)
    )

    checkRules(testDir, rule) should matchPattern {
      case Failure(RuleViolationException("1.1", "err1")) =>
    }
    ran1.get shouldBe true
    ran2.get shouldBe false
  }

  it should "fail when the child fails after the parent succeeded" in {
    val (rule1, ran1) = successRule
    val (rule2, ran2) = failedRule("err2")
    val rule = ifThenAlso(
      numberedRule("1.1", rule1),
      numberedRule("1.1.1", rule2)
    )

    checkRules(testDir, rule) should matchPattern {
      case Failure(RuleViolationException("1.1.1", "err2")) =>
    }
    ran1.get shouldBe true
    ran2.get shouldBe true
  }
}
