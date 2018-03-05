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

import java.nio.file.{ Files, Path, Paths }
import java.util.regex.Pattern

import nl.knaw.dans.easy.validatebag.rules.bagit.bagMustContainBagInfoTxt
import nl.knaw.dans.easy.validatebag.validation.RuleViolationDetailsException
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.io.FileUtils
import org.scalatest._

import scala.util.{ Failure, Success }
import scala.util.matching.Regex

trait TestSupportFixture extends FlatSpec with Matchers with Inside with OneInstancePerTest with DebugEnhancedLogging {
  lazy val testDir: Path = {
    val path = Paths.get(s"target/test/${ getClass.getSimpleName }").toAbsolutePath
    FileUtils.deleteQuietly(path.toFile)
    Files.createDirectories(path)
    path
  }

  protected val bagsDir: Path = Paths.get("src/test/resources/bags")

  implicit val isReadable: Path => Boolean = Files.isReadable

  protected def testRuleViolationRegex(rule: Rule, inputBag: String, includedInErrorMsg: Regex): Unit = {
    inside(rule(bagsDir resolve inputBag)) {
      case Failure(e: RuleViolationDetailsException) =>
        e.getMessage should include regex includedInErrorMsg
    }
  }

  protected def testRuleViolation(rule: Rule, inputBag: String, includedInErrorMsg: String): Unit = {
    inside(rule(bagsDir resolve inputBag)) {
      case Failure(e: RuleViolationDetailsException) =>
        e.getMessage should include(includedInErrorMsg)
    }
  }

  protected def testRuleSuccess(rule: Rule, inputBag: String): Unit = {
    rule(bagsDir resolve inputBag) shouldBe a[Success[_]]
  }
}
