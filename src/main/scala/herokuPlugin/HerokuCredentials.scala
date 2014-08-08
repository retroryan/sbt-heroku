package herokuPlugin

import sbt.IO
import java.io.File

case class HerokuCredentials(apiKey: String) {
  override def toString = s"HerokuCredentials($apiKey)"
}

object HerokuCredentials {

  val Keys = Seq("apiKey")

  def templateSrc(apiKey: String) = s"apiKey = $apiKey"

  def read(path: File): Either[String,Option[HerokuCredentials]] =
    path match {
      case creds if creds.exists =>
        import collection.JavaConversions._
        val properties = new java.util.Properties
        IO.load(properties, creds)
        val mapped = properties.map {
          case (k,v) => (k.toString, v.toString.trim)
        }.toMap
        val missing = Keys.filter(!mapped.contains(_))
        if (!missing.isEmpty) Left(
          "missing credential properties %s in %s"
            .format(missing.mkString(", "), creds))
        else Right(Some(HerokuCredentials(
          mapped("apiKey"))))
      case _ => Right(None)
    }

  def write(apiKey: String, path: File) =
    IO.write(path, templateSrc(apiKey))

}
