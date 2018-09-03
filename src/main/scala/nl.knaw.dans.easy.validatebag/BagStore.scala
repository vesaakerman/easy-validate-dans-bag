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

import java.net.URI
import java.util.UUID

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import scalaj.http.Http

import scala.util.Try

/**
 * Simple, incomplete interface to the bag store service that provides only the methods necessary to perform validations.
 */
trait BagStore extends DebugEnhancedLogging {
  val bagStoreBaseUrl: URI
  val connectionTimeoutMs: Int
  val readTimeoutMs: Int

  /**
   * Determines if a bag with the given UUID exists in one of the bag stores managed by the service.
   *
   * @param uuid the bag-id to check
   * @return `true` if the UUID was found, `false` otherwise
   */
  def bagExists(uuid: UUID): Try[Boolean] = Try {
    trace(uuid)
    val bagUrl = bagStoreBaseUrl.resolve(s"bags/$uuid").toASCIIString
    debug(s"Requesting: $bagUrl")
    Http(bagUrl)
      .header("Accept", "text/plain")
      .timeout(connTimeoutMs = connectionTimeoutMs, readTimeoutMs = readTimeoutMs)
      .method("HEAD")
      .asBytes.code == 200
  }
}

object BagStore {
  def apply(baseUrl: URI, cto: Int, rto: Int): BagStore = new BagStore() {
    override val bagStoreBaseUrl: URI = baseUrl
    override val connectionTimeoutMs: Int = cto
    override val readTimeoutMs: Int = rto
  }
}