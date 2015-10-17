name := """K-Modules"""

javacOptions ++= Seq("-parameters", "-Xlint:unchecked", "-Xlint:deprecation", "-encoding", "UTF8")

scalacOptions ++= Seq("-encoding", "UTF8")

sources in(Compile, doc) := Seq.empty

fork := true

publishArtifact in(Compile, packageDoc) := false


lazy val commonSettings = Seq(
  organization := "love.in.shenzhen",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.6",
  sources in(Compile, doc) := Seq.empty
)

lazy val k_base = (project in file("kmodules/K-Base"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean, PlayScala)

lazy val k_users = (project in file("kmodules/K-Users"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean, PlayScala)
  .dependsOn(k_base)

lazy val k_sms = (project in file("kmodules/K-SMS"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_wechat = (project in file("kmodules/K-WeChat"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_users)

lazy val k_chinapnr = (project in file("kmodules/K-ChinaPnr"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(k_base, k_sms, k_wechat, k_chinapnr)
  .dependsOn(k_base, k_sms, k_wechat, k_chinapnr)




libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"


