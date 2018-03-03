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
import java.nio.file.Paths

import nl.knaw.dans.easy.validatebag.validation.RuleViolationException
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.joda.time.DateTime
import org.scalatra._

import scala.util.{ Failure, Try }

class EasyValidateDansBagServlet(app: EasyValidateDansBagApp) extends ScalatraServlet with DebugEnhancedLogging {

  get("/") {
    contentType = "text/plain"
    Ok("EASY Validate DANS Bag Service running...")
  }

  post("/validate") {
    val result = for {
      infoPackageType <- Try { InfoPackageType.withName(params.get("infoPackageType").getOrElse("SIP")) }
      uri <- params.get("uri").map(getFileUrl).getOrElse(Failure(new IllegalArgumentException("Required query parameter 'uri' not found")))
      violations <- validateDansBag(Paths.get(uri.getPath), infoPackageType)
        .map(_ => List.empty)
        .recoverWith(extractViolations)
      result <- Try {
        if (violations.isEmpty) Ok()
        else BadRequest(violations) // TODO: format as plain text or JSON, depending on Accept header
      }
    } yield result


    result.getOrRecover {
      case t: IllegalArgumentException => BadRequest(s"Input error: ${t.getMessage}")
      case t =>
        logger.error(s"Server error: ${t.getMessage}", t)
        InternalServerError(s"[${ new DateTime() }] The server encountered an error. Consult the logs.")
    }
  }

  private def getFileUrl(uriStr: String): Try[URI] = Try {
    val fileUri = new URI(uriStr)
    if (fileUri.getScheme != "file") throw new IllegalArgumentException("Currently only file:/// URLs are supported")
    fileUri
  }

  val extractViolations: PartialFunction[Throwable, Try[Seq[String]]] = {
    case x @ CompositeException(xs) =>
      if (xs.forall(_.isInstanceOf[RuleViolationException])) Try(xs.map(_.getMessage))
      else Failure(x) // If there are other exceptions, just generate a fatal exception; let the caller sort out the more serious problems first.
  }

}
