import java.nio.file.{ Files, Path, Paths }

import org.apache.commons.configuration.PropertiesConfiguration
import resource.managed

import scala.io.Source

//class aFunction(configuration: Configuration) {

  val pwd = System.getProperty("user.dir")

  //val x = Paths.get(pwd).toUri.resolve("bagStore.properties")

  println("pwd: " + pwd)

  //x.getProperty("bag-store")

  // val configur = Paths.get(System.getProperty("app.home"))

  //println(configur)

  //val app = new EasyValidateDansBagApp(configur)

  //val path = Paths.get(configur.properties.getString("bag-store"))

  //println(path)

  // val config = new PropertiesConfiguration(path.toString)

  //println(config.getString("bag-stores"))

Configuration(Paths.get(System.getProperty("user.dir")))



case class Configuration(version: String, properties: PropertiesConfiguration)

object Configuration {

  def apply(home: Path): Configuration = {
    val cfgPath = Seq(
      Paths.get(s"/etc/opt/dans.knaw.nl/easy-validate-dans-bag/"),
      home.resolve("cfg"))
      .find(Files.exists(_))
      .getOrElse { throw new IllegalStateException("No configuration directory found") }

    new Configuration(
      version = managed(Source.fromFile(home.resolve("bin/version").toFile)).acquireAndGet(_.mkString),
      properties = new PropertiesConfiguration() {
        setDelimiterParsingDisabled(true)
        load(cfgPath.resolve("application.properties").toFile)
        load(cfgPath.resolve("bagStore.properties").toFile)
      }
    )
  }

}




//val app = new EasyValidateDansBagApp(configuratio)

 // val bagstorepath = new Configuration(configuratio.properties.getString("bag-store"), new PropertiesConfiguration())


  //bagstorepath


 // println("bagstorepath" + bagstorepath)

//}