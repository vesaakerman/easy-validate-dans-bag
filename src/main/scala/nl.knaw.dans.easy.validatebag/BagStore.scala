package nl.knaw.dans.easy.validatebag

import java.net.URI
import java.util.UUID

import scala.util.Try

/**
 * Simple, incomplete interface to the bag store service that provides only the methods necessary to perform validations.
 */
trait BagStore {
  val bagStoreBaseUrl: URI

  /**
   * Determines if a bag with the given UUID exists in one of the bag stores managed by the service.
   *
   * @param UUID the bag-id to check
   * @return `true` if the UUID was found, `false` otherwise
   */
  def bagExists(UUID: UUID): Try[Boolean] = {

    ???
  }
}

object BagStore {
  def apply(baseUrl: URI): BagStore = new BagStore() {
    override val bagStoreBaseUrl: URI = baseUrl
  }
}