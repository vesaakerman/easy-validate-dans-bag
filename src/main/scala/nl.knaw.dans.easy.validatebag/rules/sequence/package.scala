package nl.knaw.dans.easy.validatebag.rules

import java.net.URI
import java.util.UUID

import nl.knaw.dans.easy.validatebag.{ BagStore, TargetBag }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import nl.knaw.dans.easy.validatebag.validation.fail

import scala.util.Try

package object sequence extends DebugEnhancedLogging {
  private val uuidHexLength = 32
  private val uuidCanonicalNumberOfDashes = 4

  def bagInfoIsVersionOfIfExistsPointsToArchivedBag(bagStore: BagStore)(t: TargetBag): Try[Unit] = {
    trace(())
    val r = for {
      optIsVersionOf <- getBagInfoTxtValue(t, "Is-Version-Of")
      optTryExists = for {
        versionOf <- optIsVersionOf
        tryResult = for {
          uuid <- getUuidFromIsVersionOfValue(versionOf)
          exists <- bagStore.bagExists(uuid)
          _ = if(!exists) fail("Is-Version-Of points to a bag that is not (yet) archived in a bag-store")
        } yield tryResult
      } yield optTryExists
    } yield ()

    ???
  }

  private def getUuidFromIsVersionOfValue(s: String): Try[UUID] = Try {
    val uri = new URI(s)
    require(uri.getScheme == "urn", "Is-Version-Of value must be a URN")
    require(uri.getSchemeSpecificPart.startsWith("uuid:"), "Is-Version-Of URN must be of subtype UUID")
    val Array(_, uuidStr) = uri.getSchemeSpecificPart.split(':')
    require(uuidStr.length == uuidHexLength + uuidCanonicalNumberOfDashes, "UUID must be in canonical textual representation")
    UUID.fromString(uuidStr)
  }
}
