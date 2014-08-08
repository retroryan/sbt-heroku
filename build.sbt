// this bit is important
sbtPlugin := true

organization := "com.typesafe"

name := "sbt-heroku"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += bintray.Opts.resolver.mavenRepo("jamesward")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.jamesward" %% "scheroku" % "1.0-e40f9e277be4e31d18dcf8033a5985b89096f8d7"
)

publishMavenStyle := false

/** Console */
initialCommands in console := "import com.typesafe.sbt.rss._"

bintraySettings

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

