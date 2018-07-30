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
package nl.knaw.dans.easy.validatebag.rules.profile

import java.net.URI
import java.nio.charset.StandardCharsets

import better.files.File
import nl.knaw.dans.easy.validatebag._
import nl.knaw.dans.easy.validatebag.rules.metadata.normalizeLicenseUri
import nl.knaw.dans.lib.error._
import org.apache.commons.configuration.PropertiesConfiguration

class NumberedRulesSpec extends TestSupportFixture with CanConnectFixture {

  private val resourceDir = File(getClass.getResource("/"))
  private val configuration = new Configuration("version x.y.z",
    new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
      load(resourceDir / "debug-config" / "application.properties" toJava)
    },
    (resourceDir / "debug-config" / "licenses.txt")
      .lines(StandardCharsets.UTF_8)
      .filterNot(_.isEmpty)
      .map(s => normalizeLicenseUri(new URI(s))).toSeq.collectResults.unsafeGetOrThrow
  )

  "rulesCheck" should "succeed if all rules, that other rules depend on, exist" in {
    List(0, 1).map(version => {
      val rulesBase = new EasyValidateDansBagApp(configuration).allRules(version)
      val ruleNumbers = rulesBase.map(_.nr)
      val result = rulesBase.flatMap(_.dependsOn).forall(ruleNumber => ruleNumbers.contains(ruleNumber))
      result shouldBe true
    })
  }

  it should "succeed if there are no duplicate rule numbers" in {
    List(0, 1).map(version => {
      val ruleNumbers = new EasyValidateDansBagApp(configuration).allRules(version).map(_.nr)
      ruleNumbers shouldEqual ruleNumbers.distinct
    })
  }
}
