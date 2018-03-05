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

object Test extends App {

  import java.net.URI

  import nl.knaw.dans.easy.validatebag.InfoPackageType._
  implicit val pretty = true

  println(ResultMessage(bagUri = new URI("file:///test/dit/café"), infoPackageType = SIP, bag = "name", result = ValidationResult.NOT_COMPLIANT,
    ruleViolations = Some(
      Seq(
        "1" -> "oh, no",
        "2" -> "you're not serious"))

  ).toJson)

}
