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

import java.nio.file.Path

import nl.knaw.dans.easy.validatebag.TargetBag
import nl.knaw.dans.easy.validatebag.validation.fail
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.Try

package object structural extends DebugEnhancedLogging {
  def bagMustContainDir(d: Path)(t: TargetBag) = Try {
    trace(d)
    require(!d.isAbsolute, s"Directory $d must be a relative path")
    if (!(t.bagDir / d.toString).isDirectory)
      fail(s"Mandatory directory '$d' not found in bag.")
  }

  def bagMustContainFile(f: Path)(t: TargetBag) = Try {
    trace(f)
    require(!f.isAbsolute, s"File $f must be a relative path")
    val fileToCheck = t.bagDir / f.toString
    if (!fileToCheck.exists)
      fail(s"Mandatory file '$f' not found in bag.")
    val relativeRealPath = t.bagDir.path.relativize(fileToCheck.path.toRealPath())
    val relativeRequiredPath = t.bagDir.path.relativize(fileToCheck.path)
    if (relativeRealPath != relativeRequiredPath)
      fail(s"Path name differs in case; found: $relativeRealPath, required: $relativeRequiredPath")
  }

  def bagMustNotContainFile(f: Path)(t: TargetBag) = Try {
    trace(f)
    require(!f.isAbsolute, s"File $f must be a relative path")
    if ((t.bagDir / f.toString).exists)
      fail(s"File '$f' MUST NOT exist in bag (of this information package type).")
  }

  def bagDirectoryMustNotContainAnythingElseThan(d: Path, ps: Seq[String])(t: TargetBag) = Try {
    trace(d, ps)
    require(!d.isAbsolute, s"Directory $d must be a relative path")
    val extraFiles = (t.bagDir / d.toString).list.filterNot(ps contains _.name).map(t.bagDir relativize _.path)
    if (extraFiles.nonEmpty) fail(s"Directory $d contains files that are not allowed: ${ extraFiles.mkString(", ") }")
  }
}
