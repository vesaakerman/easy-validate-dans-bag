package nl.knaw.dans.easy.validatebag.lib

import java.nio.file.{ Files, Path }

import nl.knaw.dans.easy.validatebag.lib.InfoPackageType.{ InfoPackageType, _ }
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.collection.JavaConverters._
import scala.util.{ Failure, Try }

object ValidatorAPI extends DebugEnhancedLogging {

  def fail(details: String): Unit = throw RuleViolationDetailsException(details)

  def numberedRule(ruleNumber: RuleNumber, rule: Rule, infoPackageType: InfoPackageType = BOTH): NumberedRule = {
    (ruleNumber, rule, infoPackageType)
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
  private def validateDansBag(bag: BagDir, rules: Map[ProfileVersion, RuleBase], asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
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

  def validateDansBag(bag: BagDir, rules: Seq[Rules], asInfoPackageType: InfoPackageType = SIP)(implicit isReadable: Path => Boolean): Try[Unit] = {
    def merge[K, V](map1: Map[K, Seq[V]], map2: Map[K, Seq[V]]): Map[K, Seq[V]] = {
      map2.foldLeft(map1) {
        case (map, entry @ (key, values)) =>
          map.get(key)
            .map(seq1 => map.updated(key, seq1 ++ values))
            .getOrElse(map + entry)
      }
    }

    val rulesMap: Map[ProfileVersion, RuleBase] = rules.foldLeft(Map.empty[ProfileVersion, RuleBase])((map, rules) => merge(map, rules.rules))

    validateDansBag(bag, rulesMap, asInfoPackageType)
  }

  private def evaluateRules(bag: BagDir, rules: Map[ProfileVersion, RuleBase], asInfoPackageType: InfoPackageType = SIP): Try[Unit] = {
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
