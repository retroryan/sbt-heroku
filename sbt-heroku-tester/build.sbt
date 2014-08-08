name := """sbt-heroku-tester"""

version := "0.1-SNAPSHOT"

lazy val root = project.in(file("."))

deployTarget := "the.zip"
