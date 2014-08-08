package herokuPlugin

import sbt._

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global
import com.jamesward.scheroku.HerokuAPI

/**
 * An autoplugin that deploys to heroku.
 */
object SbtHerokuPlugin extends AutoPlugin {

  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
  object autoImport {
    // configuration points, like the built-in `version`, `libraryDependencies`, or `compile`
    val deploy = taskKey[String]("Deploy to heroky.")

    val deployTarget = settingKey[String]("deployTarget setting.")

    val credentialsFile = settingKey[File]("credentialsFile setting")

    // default values for the tasks and settings
    lazy val baseSbtHerokuSettings: Seq[Def.Setting[_]] = Seq(
      credentialsFile in deploy in Global := Path.userHome / ".heroku" / ".credentials",
      deploy := {
        SbtHeroku(deployTarget.value, credentialsFile.value)
      },
      deployTarget in deploy := "file.zip"
    )
  }


  import autoImport._

  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override val projectSettings =
    inConfig(Compile)(baseSbtHerokuSettings) ++
      inConfig(Test)(baseSbtHerokuSettings)
}

object SbtHeroku {

  object HerokuApi extends HerokuAPI {
    override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  }

  def apply(deployTarget: String, credentialsFile: File): String = {
    println(s"deployTarget $deployTarget")

    ensuredCredentials(credentialsFile) map {
      case Some(creds) => println(s"creds = ${creds.apiKey}")
      case None => println("could not read credentials")
    }

    deployTarget
  }


  private def requestCredentials(): (String, String) = {
    val username = Prompt("Enter heroku username").getOrElse {
      sys.error("heroku username required")
    }

    val pass = Prompt.descretely("Enter heroku password").getOrElse {
      sys.error("heroku password key required")
    }
    (username, pass)
  }

  private def fetchApiKey(username: String, pass: String) = {
    HerokuApi.getApiKey(username, pass)
  }

  private def saveHerokuCredentials(to: File)(apiKey: String) = {
    println(s"saving credentials to $to")
    HerokuCredentials.write(apiKey, to)
    println("reload project for sbt setting `publishTo` to take effect")
  }

  private def ensuredCredentials(credsFile: File, prompt: Boolean = true): Future[Option[HerokuCredentials]] =
    HerokuCredentials.read(credsFile).fold(sys.error(_), _ match {
      case None =>
        if (prompt) {
          println("heroku-sbt requires your heroku api key.")
          val (username, password) = requestCredentials()
          fetchApiKey(username, password) map  {
            key => saveHerokuCredentials(credsFile)(key)
             Some(HerokuCredentials(key))
          } 
        } else {
          val errorMsg = s"Missing heroku api key credentials $credsFile."
          println(errorMsg)
          Future.failed(new Throwable(errorMsg))
        }
      case creds => Future.successful(creds)
    })

}
