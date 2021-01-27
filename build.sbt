name := "wikidata-subgraph-builder"

version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += Classpaths.typesafeReleases

parallelExecution in Test := false
fork := true
outputStrategy := Some(StdoutOutput)
envVars := Map("SCALA_ENV" -> "main")
envVars in Test := Map("SCALA_ENV" -> "test")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1",
  "commons-logging" % "commons-logging" % "1.2",
  "com.github.jsonld-java" % "jsonld-java" % "0.13.2",
  "commons-io" % "commons-io" % "2.4"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports/html")