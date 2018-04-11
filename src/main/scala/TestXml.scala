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
import java.io.ByteArrayInputStream

import better.files.File
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import nl.knaw.dans.easy.validatebag.{ PreloadedSchemasResolver, XmlValidator }
import org.w3c.dom.ls.{ LSInput, LSResourceResolver }
import org.xml.sax.InputSource

object TestXml extends App {
  val sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
  val resolver = new PreloadedSchemasResolver(File("/Users/janm/git/service/easy/easy-validate-dans-bag/data"))
  val dcxDai = new StreamSource(File("target/easy-schema/dcx/2017/09/dcx-dai.xsd").toJava)
  dcxDai.setPublicId("http://easy.dans.knaw.nl/schemas/dcx/dai/")
  dcxDai.setSystemId("http://easy.dans.knaw.nl/schemas/dcx/2017/09/dcx-dai.xsd")
  val sources = List(new StreamSource(File("target/easy-schema/md/ddm/ddm.xsd").toJava), dcxDai).toArray[Source]
  val schema = sf.newSchema(sources)
  val v = new XmlValidator(schema, resolver)
  File("data/example2.xml").inputStream.map {
    is =>
      println(v.validate(is))
  }

}
