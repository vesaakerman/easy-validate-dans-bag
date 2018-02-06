package nl.knaw.dans.easy.validatebag

import nl.knaw.dans.easy.validatebag.lib.ValidatorAPI
import nl.knaw.dans.easy.validatebag.rules.{ Rules1, Rules2 }

import scala.util.Try

// example of how to validate a bag
// the various rules Maps are agregated and used inside the API. You only register the specific classes in here
// typically this function is written in the EasyValidateDansBagApp class; this is just an example
object RuleRunner {

  def validateBag(bag: BagDir): Try[Unit] = {
    val r1 = Rules1
    val r2 = Rules2

    ValidatorAPI.validateDansBag(bag, Seq(r1, r2))
  }
}
