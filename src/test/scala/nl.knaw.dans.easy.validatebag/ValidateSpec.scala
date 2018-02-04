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

import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{ Files, Path, Paths }

import nl.knaw.dans.lib.error.CompositeException
import org.apache.commons.io.FileUtils

import scala.util.Failure

class ValidateSpec extends TestSupportFixture {
  FileUtils.copyDirectoryToDirectory(Paths.get("src/test/resources/bags/minimal").toFile, testDir.toFile)

  private def expectUnreadable(unreadables: Path*)(path: Path): Boolean = {
    !unreadables.contains(path) && Files.isReadable(path)
  }

  "validateDansBag" should "fail if bag directory is not found" in {
    val result = validateDansBag(testDir.resolve("/non-existent"))
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(e) => e shouldBe a[IllegalArgumentException]
    }
  }

  it should "fail if the bag directory is unreadable" in {
    val minimal = testDir.resolve("minimal")
    val result = validateDansBag(testDir.resolve("minimal"))(expectUnreadable(minimal))
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(iae) =>
        iae shouldBe a[IllegalArgumentException]
        iae.getMessage should include("non-readable")
    }
  }

  it should "fail if there is a non-readable file in the bag directory" in {
    val minimal = testDir.resolve("minimal")
    val leegTxt = minimal.resolve("data/leeg.txt")
    val result = validateDansBag(testDir.resolve("minimal"))(expectUnreadable(leegTxt))
    result shouldBe a[Failure[_]]
    inside(result) {
      case Failure(iae) =>
        iae shouldBe a[IllegalArgumentException]
        iae.getMessage should include("non-readable")
    }
  }


}
