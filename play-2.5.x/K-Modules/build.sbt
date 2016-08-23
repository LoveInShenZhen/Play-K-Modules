name := """K-Modules"""

import sbt.Keys._

name := """PlayStub"""

publishArtifact in(Compile, packageDoc) := false

kotlinLib("stdlib")

kotlincOptions += "-verbose"

version := "1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  javacOptions ++= Seq("-parameters", "-Xlint:unchecked", "-Xlint:deprecation", "-encoding", "UTF8"),
  scalacOptions ++= Seq("-encoding", "UTF8"),
  publishMavenStyle := true,
  organization := "love.in.shenzhen",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.7",
  sources in(Compile, doc) := Seq.empty
)

lazy val k_base = (project in file("kmodules/K-Base"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean, PlayScala)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(k_base)
  .dependsOn(k_base)

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)