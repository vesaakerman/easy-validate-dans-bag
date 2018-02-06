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

import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.JavaConverters._
import scala.util.{ Failure, Try }

package object validatebag extends DebugEnhancedLogging {
  object InfoPackageType extends Enumeration {
    type InfoPackageType = Value
    val SIP, AIP, BOTH = Value
  }

  import InfoPackageType._

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
  private case class RuleViolationDetailsException(details: String) extends Exception(details)

  /**
   * Exception specifying the rule violated and what the violation consisted of. The number refers back
   * to the DANS BagIt Profile documents.
   *
   * @param ruleNr  rule number violated.
   * @param details details about the violation
   */
  case class RuleViolationException(ruleNr: RuleNumber, details: String) extends Exception(s"[$ruleNr] $details")



  /**
   * The rule bases for each version, each of which contains a mapping from rule number to rule function. The rule functions
   * are created above.
   */
  private val rules: Map[ProfileVersion, RuleBase] = {
    def fail(details: String): Unit = throw RuleViolationDetailsException(details)

    def numberedRule(ruleNumber: RuleNumber, rule: Rule, infoPackageType: InfoPackageType = BOTH): (RuleNumber, Rule, InfoPackageType) = {
      (ruleNumber, rule, infoPackageType)
    }

    val bagMustBeValid = (b: BagDir) => Try {
      // TODO: check that the bag is VALID according to BagIt.
    }
    val bagMustBeVirtuallyValid = (b: BagDir) => Try {
      // TODO: same als bagMustBeValid, but when NON-VALID warn that "virtually-only-valid" bags cannot not be recognized by the service yet.
    }
    val bagMustContainBagInfoTxt = (b: BagDir) => Try {}

    def bagInfoTxtMustContainBagItProfileVersion(version: String)(b: BagDir) = Try {

    }

    val bagMustContainMetadataDir = (b: BagDir) => Try {
      if (Files.isDirectory(b.resolve("metadata"))) ()
      else fail("Mandatory directory 'metadata' not found in bag.")
    }


    Map(
      // TODO: Add the rules to the respective rule bases
      0 -> Seq(
        numberedRule("1.1.1", bagMustBeValid, SIP),
        numberedRule("1.1.2", bagMustBeVirtuallyValid, AIP),
        numberedRule("1.2.1", bagMustContainBagInfoTxt),
        numberedRule("1.2.2", bagInfoTxtMustContainBagItProfileVersion("0.0.0"))


      )
    )
  }

  /**
   * Validates if the bag pointed to compliant with the DANS BagIt Profile version it claims to
   * adhere to. If no claim is made, by default it is assumed that the bag is supposed to comply
   * with v0.
   *
   * @param bag               the bag to validate
   * @param asInfoPackageType validate as SIP (default) or AIP
   * @param isReadable        function to check the readability of a file (added for unit testing purposes)
   * @return Success if compliant, Failure if not compliant or an error occurred. The Failure will contain
   *         `nl.knaw.dans.lib.error.CompositeException`, which will contain a [[RuleViolationException]]
   *         for every violation of the DANS BagIt Profile rules.
   */
  def validateDansBag(bag: BagDir, asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
    /**
     * `isReadable` was added because unit testing this by actually setting files on the file system to non-readable and back
     * can get messy. After a failed build one might be left with a target folder that refuses to be cleaned. Unless you are
     * aware of the particular details of the test this will be very confusing.
     */
    trace(bag, asInfoPackageType)
    for {
      _ <- checkIfValidationCanProceed(bag)
      result <- evaluateRules(bag)
    } yield result
  }

  private def evaluateRules(bag: BagDir, asInfoPackageType: InfoPackageType = SIP): Try[Unit] = {
    rules(getProfileVersion(bag))
      .collect {
        case (nr, rule, ipType) if ipType == asInfoPackageType || ipType == BOTH =>
          rule(bag).recoverWith {
            case RuleViolationDetailsException(details) => Failure(RuleViolationException(nr, details))
          }
      }
      .collectResults
      .map(_ => ())
  }

  private def checkIfValidationCanProceed(bag: BagDir)(implicit isReadable: Path => Boolean): Try[Unit] = Try {
    trace(bag)
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

  private def getProfileVersion(bag: BagDir): Int = {
    0 // TODO: retrieve actual version
  }
}
