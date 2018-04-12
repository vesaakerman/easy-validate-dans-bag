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

import gov.loc.repository.bagit.domain.Bag

import scala.xml.Elem

// TODO: optimize validation by making sure expensive actions only need to be done once per bag

/**
 * Interface to the bag under validation.
 *
 * Loads resources from the bag lazily and then hangs on to it, so that subsequent rules do not have
 * to reload this information. The location of some resources depends on the profile version. That is
 * why it is provided as an argument to the constructor.
 *
 * @param profileVersion the profile version used
 */
class TargetBag(profileVersion: ProfileVersion) {

  lazy val bagDir: BagDir = {


    ???
  }

  lazy val bag: Bag = {


    ???
  }

  lazy val filesXml: Elem = {


    ???
  }

}
