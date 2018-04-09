package nl.knaw.dans.easy.validatebag

import javax.xml.parsers.SAXParserFactory
import javax.xml.validation.Schema
import nl.knaw.dans.lib.error._
import org.xml.sax.{ EntityResolver, ErrorHandler, InputSource, SAXParseException }

import scala.collection.mutable.ListBuffer
import scala.util.Try

class XmlValidator(schema: Schema) {

  def validate(is: InputSource): Try[Unit] = {
    val errorHandler = new AccumulatingErrorHandler
    Try {
      val f = SAXParserFactory.newInstance()
      f.setNamespaceAware(true)
      f.setValidating(true)
      f.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
      f.newSAXParser()
    }.map {
      parser =>
        val xr = parser.getXMLReader
        parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema")
        val vh = schema.newValidatorHandler()
        vh.setErrorHandler(errorHandler)
        xr.setContentHandler(vh)
        xr.setEntityResolver(PreloadedSchemasResolver)
        xr.setErrorHandler(new SilentErrorHandler()) // If we do not provide one here, we get a warning about no error handler being set, even though we set one on vh
        xr.parse(is)
    }

    errorHandler.errors.toList.map(x => Try[Unit] { throw x }).collectResults.map(_ => ())
  }
}

class SilentErrorHandler extends ErrorHandler {
  override def warning(exception: SAXParseException): Unit = ()

  override def error(exception: SAXParseException): Unit = ()

  override def fatalError(exception: SAXParseException): Unit = ()
}

class AccumulatingErrorHandler extends ErrorHandler {
  val warnings = new ListBuffer[Exception]
  val errors = new ListBuffer[Exception]
  val fatals = new ListBuffer[Exception]


  override def warning(exception: SAXParseException): Unit = {
    warnings.append(exception)
  }

  override def error(exception: SAXParseException): Unit = {
    errors.append(exception)
  }

  override def fatalError(exception: SAXParseException): Unit = {
    fatals.append(exception)
  }
}

object PreloadedSchemasResolver extends EntityResolver {
  override def resolveEntity(publicId: String, systemId: String): InputSource = {

  }
}

