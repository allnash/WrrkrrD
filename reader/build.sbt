import NativePackagerHelper._

name := """wrrkrr-reader"""

version := "0.1"

maintainer := "Nash Gadre <gadre@omegatrace.com>"

packageSummary := "OmegaTrace WrrKrr Engage Reader "

packageDescription := """Owned by OmegaTrace Inc. Copyright 2018."""

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

lazy val reader = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += guice

libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  guice,
  filters,
  evolutions,
  "com.h2database" % "h2" % "1.4.197",
  "mysql" % "mysql-connector-java" % "5.1.46",
  "com.mashape.unirest" % "unirest-java" % "1.4.9",
  "com.mashape.unirest" % "unirest-java" % "1.4.9",
  "io.ebean" % "ebean" % "11.15.3",
  "com.github.javafaker" % "javafaker" % "0.15",
  "org.zeroturnaround" % "zt-exec" % "1.10",
  "org.zeroturnaround" % "zt-process-killer" % "1.8",
  "com.neovisionaries" % "nv-oui" % "1.1",
)
libraryDependencies += "com.github.0xbaadf00d" % "ebean-encryption" % "release~17.01"
// Discovery Service
libraryDependencies += "org.jmdns" % "jmdns" % "3.5.4"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

// Add scaner directory to the Node scanner package.
mappings in Universal ++= directory("scanner")
mappings in Universal ++= directory("lookaroundyou")
mappings in Universal ++= directory("data")

publishArtifact in (Compile, packageDoc) := false

enablePlugins(JavaServerAppPackaging)
