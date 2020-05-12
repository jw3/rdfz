name := "rdfz"
organization := "com.github.jw3"
scalaVersion := "2.12.10"
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

val zioVersion = "1.0.0-RC18"
val rdf4jVersion = "3.1.1"
val scalatest = "3.0.3"
libraryDependencies := Seq(
  "dev.zio" %% "zio" % zioVersion,
  "org.eclipse.rdf4j" % "rdf4j-repository-sail" % rdf4jVersion,
  "org.eclipse.rdf4j" % "rdf4j-sail-memory" % rdf4jVersion,
  // ------------- test
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "org.scalactic" %% "scalactic" % scalatest % Test,
  "org.scalatest" %% "scalatest" % scalatest % Test
)

enablePlugins(GitVersioning, JavaServerAppPackaging)
