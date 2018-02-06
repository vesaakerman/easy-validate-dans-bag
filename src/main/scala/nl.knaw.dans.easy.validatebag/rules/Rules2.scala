package nl.knaw.dans.easy.validatebag.rules

import java.nio.file.Files

import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.lib.InfoPackageType._
import nl.knaw.dans.easy.validatebag.lib.Rules
import nl.knaw.dans.easy.validatebag.lib.ValidatorAPI._

import scala.util.Try

object Rules2 extends Rules {

  val someRule = (b: BagDir) => Try {}
  val someOtherRule = (b: BagDir) => Try {}

  override val rules = Map(
    // TODO: Add the rules to the respective rule bases
    0 -> Seq(
      numberedRule("a.b.c", someRule, SIP),
    ),
    1 -> Seq(
      numberedRule("x.y.z", someOtherRule, AIP),
    ),
  )
}
