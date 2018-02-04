package nl.knaw.dans.easy.validatebag

import java.nio.file.{ Files, Path, Paths }

import org.apache.commons.io.FileUtils
import org.scalatest._

trait TestSupportFixture extends FlatSpec with Matchers with Inside with OneInstancePerTest {
  lazy val testDir: Path = {
    val path = Paths.get(s"target/test/${ getClass.getSimpleName }").toAbsolutePath
    FileUtils.deleteQuietly(path.toFile)
    Files.createDirectories(path)
    path
  }

  implicit val isReable: Path => Boolean = Files.isReadable
}