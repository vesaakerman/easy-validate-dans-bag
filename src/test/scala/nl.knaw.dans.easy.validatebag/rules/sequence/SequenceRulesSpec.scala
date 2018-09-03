package nl.knaw.dans.easy.validatebag.rules.sequence

import java.util.UUID

import nl.knaw.dans.easy.validatebag.{ BagStore, TestSupportFixture }
import org.scalamock.scalatest.MockFactory

import scala.util.Success

class SequenceRulesSpec extends TestSupportFixture with MockFactory  {
  private val bagStoreMock = mock[BagStore]

  private def expectUuidDoesNotExist(): Unit = {
    (bagStoreMock.bagExists(_: UUID)) expects *  anyNumberOfTimes() returning Success(false)
  }

  "bagInfoIsVersionOfIfExistsPointsToArchivedBag" should "fail if UUID not found in bag-store" in {
    expectUuidDoesNotExist()
    testRuleViolation(rule = bagInfoIsVersionOfIfExistsPointsToArchivedBag(bagStoreMock), inputBag = "baginfo-with-is-version-of", includedInErrorMsg = "not (yet) archived in a bag-store", doubleCheckBagItValidity = false)
  }


}
