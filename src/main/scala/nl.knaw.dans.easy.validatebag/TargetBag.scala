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
