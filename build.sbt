name := """hiring_task_lendi"""
organization := "com.giza.dariusz"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

swaggerDomainNameSpaces := Seq("models")

libraryDependencies += guice
libraryDependencies ++= Dependencies.playTest ++
  Dependencies.test ++
  Dependencies.playJson ++
  Dependencies.cats ++
  Dependencies.swaggerUI
