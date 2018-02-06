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
