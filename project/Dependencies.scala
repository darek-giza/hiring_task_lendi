import sbt._

object Dependencies {
  object Version {
    val scalaVersion = "2.12.14"
    val play = "2.8.0"
    val playJson = "2.8.1"
  }

  val playTest = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test)

  val test = Seq("org.scalatest" %% "scalatest" % "3.2.12" % Test, "org.scalamock" %% "scalamock" % "5.2.0" % Test)

  val playJson = Seq("com.typesafe.play" %% "play-json" % Version.play % Test)

  val cats = Seq(
    "org.typelevel" %% "cats-core" % "2.7.0",
    "org.typelevel" %% "cats-effect" % "3.3.11",
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
    "org.typelevel" %% "cats-effect-testkit" % "3.3.11" % Test,
    "org.typelevel" %% "cats-effect-laws" % "3.3.11" % Test
  )

  val swaggerUI = Seq("org.webjars" % "swagger-ui" % "4.10.3", "org.webjars" % "swagger-ui" % "4.10.3")
}
