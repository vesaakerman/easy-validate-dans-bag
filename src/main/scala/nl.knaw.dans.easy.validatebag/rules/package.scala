package nl.knaw.dans.easy.validatebag

import nl.knaw.dans.easy.validatebag.rules.bagit.trace

import scala.util.Try

package object rules {
  // Relies on there being only one element with the specified name
  def getBagInfoTxtValue(t: TargetBag, element: String): Try[Option[String]] = {
    trace(t, element)
    t.tryBag.map { bag =>
      Option(bag.getMetadata.get(element))
        .flatMap {
          case list if list.isEmpty => None
          case list => Some(list.get(0))
        }
    }
  }
}
