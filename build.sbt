name := """pass-bakery"""
organization := "com.optum"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.2"
libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "org.postgresql" % "postgresql" % "42.4.0", // postgresql driver dependency (might be redundant w doobie-pgs)
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1", // doobie core dependency
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1", // Postgres driver 42.3.1 + type mappings.
  "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC1" % "test" // Specs2 support for typechecking statements.
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.optum.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.optum.binders._"
