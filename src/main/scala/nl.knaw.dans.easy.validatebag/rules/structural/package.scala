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

import nl.knaw.dans.easy.validatebag.BagDir
import nl.knaw.dans.easy.validatebag.validation.fail

import scala.util.Try

package object structural {
  def bagMustContainDir(d: Path)(b: BagDir) = Try {
    require(!d.isAbsolute, s"Directory $d must be a relative path")
    if (!(b / d.toString).isDirectory)
      fail(s"Mandatory directory '$d' not found in bag.")
  }

  def bagMustContainFile(f: Path)(b: BagDir) = Try {
    require(!f.isAbsolute, s"File $f must be a relative path")
    if (!(b / f.toString).isDirectory)
      fail(s"Mandatory file '$f' not found in bag.")
  }
}
