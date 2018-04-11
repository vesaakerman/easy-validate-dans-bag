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

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import nl.knaw.dans.lib.error._
import org.xml.sax.{ EntityResolver, ErrorHandler, InputSource, SAXParseException }

import scala.collection.mutable.ListBuffer
import scala.tools.nsc.interpreter.InputStream
import scala.util.Try

class XmlValidator(schema: Schema, entityResolver: EntityResolver) {

  def validate(is: InputStream): Try[Unit] = {
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
        val validator = schema.newValidator()
        validator.setErrorHandler(errorHandler)
        xr.setEntityResolver(entityResolver)
        validator.validate(new StreamSource(is))
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

