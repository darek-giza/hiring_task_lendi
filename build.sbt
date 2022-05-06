name := """hiring_task_lendi"""
organization := "com.giza.dariusz"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"
val playVersion = "2.9.2"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "com.typesafe.play" %% "play-json" % playVersion,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.11",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
  "org.typelevel" %% "cats-effect-testkit" % "3.3.11" % Test,
  "org.typelevel" %% "cats-effect-laws" % "3.3.11" % Test,
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic-extras" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-shapes" % "0.14.1",
  "org.sorm-framework" % "sorm" % "0.3.21",
  "com.h2database" % "h2" % "2.1.212"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.giza.dariusz.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.giza.dariusz.binders._"
