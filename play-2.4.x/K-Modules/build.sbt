name := """K-Modules"""

lazy val commonSettings = Seq(
  organization := "love.in.shenzhen",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.6")

lazy val k_base = (project in file("kmodules/K-Base"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava, PlayEbean)

lazy val k_sms = (project in file("kmodules/K-SMS"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_wechat = (project in file("kmodules/K-WeChat"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_chinapnr = (project in file("kmodules/K-ChinaPnr"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(
    k_base,
    k_sms,
    k_wechat,
    k_chinapnr)


libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs)

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
