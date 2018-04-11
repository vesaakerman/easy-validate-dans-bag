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

import java.io.ByteArrayInputStream

import better.files.File
import org.xml.sax.InputSource

import scala.xml.EntityResolver

class PreloadedSchemasResolver(schemasDir: File) extends EntityResolver {
  private val dc = loadBytes("dc")
  private val dcmitype = loadBytes("dcmitype")
  private val dcterms = loadBytes("dcterms")
  private val dcx = loadBytes("dcx")
  private val dcxDai = loadBytes("dcx-dai")
  private val dcxGml = loadBytes("dcx-gml")
  private val ddm = loadBytes("ddm")
  private val identifierType = loadBytes("identifier-type")
  private val narcisType = loadBytes("narcis-type")
  private val xml = loadBytes("xml")

  private val publicId2Bytes = Map(
    "http://purl.org/dc/elements/1.1/" -> dc,
    "http://purl.org/dc/dcmitype/" -> dcmitype,
    "http://purl.org/dc/terms/" -> dcterms,
    "http://easy.dans.knaw.nl/schemas/dcx/" -> dcx,
    "http://easy.dans.knaw.nl/schemas/dcx/dai/" -> dcxDai,
    "http://easy.dans.knaw.nl/schemas/dcx/gml/" -> dcxGml,
    "http://easy.dans.knaw.nl/schemas/dcx/dai/" -> ddm,
    "http://easy.dans.knaw.nl/schemas/vocab/identifier-type/" -> identifierType,
    "http://easy.dans.knaw.nl/schemas/vocab/narcis-type/" -> narcisType)

  private val systemId2Bytes = Map(
    "http://dublincore.org/schemas/xmls/qdc/dc.xsd" -> dc,
    "http://dublincore.org/schemas/xmls/qdc/dcmitype.xsd" -> dcmitype,
    "http://dublincore.org/schemas/xmls/qdc/dcterms.xsd" -> dcterms,
    "http://easy.dans.knaw.nl/schemas/dcx/2012/10/dcx.xsd" -> dcx,
    "http://easy.dans.knaw.nl/schemas/dcx/2017/09/dcx-dai.xsd" -> dcxDai,
    "http://easy.dans.knaw.nl/schemas/dcx/2016/dcx-gml.xsd" -> dcxGml,
    "http://easy.dans.knaw.nl/schemas/md/2017/09/ddm.xsd" -> ddm,
    "http://easy.dans.knaw.nl/schemas/vocab/2017/identifier-type.xsd" -> identifierType,
    "http://easy.dans.knaw.nl/schemas/vocab/2015/narcis-type.xsd" -> narcisType)

  private def loadBytes(schema: String): Array[Byte] = {
    (schemasDir / s"$schema.xsd").byteArray
  }

  override def resolveEntity(publicId: String, systemId: String): InputSource = {
    if (publicId2Bytes.contains(publicId)) new InputSource(new ByteArrayInputStream(publicId2Bytes(publicId)))
    else if (systemId2Bytes.contains(systemId)) new InputSource(new ByteArrayInputStream(systemId2Bytes(systemId)))
    else null
  }
}

