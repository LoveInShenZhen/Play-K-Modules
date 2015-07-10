name := """K-Modules"""

lazy val commonSettings = Seq(
  organization := "love.in.shenzhen",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.6")

lazy val k_base = (project in file("kmodules/K-Base"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)

lazy val k_ebean = (project in file("kmodules/K-Ebean"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_jsonapi = (project in file("kmodules/K-JsonApi"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_ebean)

lazy val k_plantask = (project in file("kmodules/K-PlanTask"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_ebean)

lazy val k_eventbus = (project in file("kmodules/K-EventBus"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_ebean, k_plantask, k_jsonapi)

lazy val k_memcached = (project in file("kmodules/K-Memcached"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_freemarker = (project in file("kmodules/K-FreeMarker"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_emailer = (project in file("kmodules/K-Emailer"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_ebean, k_eventbus)

lazy val k_redis = (project in file("kmodules/K-Redis"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_pdftemplate = (project in file("kmodules/K-PdfTemplate"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base)

lazy val k_sms = (project in file("kmodules/K-SMS"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_ebean, k_eventbus)

lazy val k_wechat = (project in file("kmodules/K-WeChat"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_jsonapi, k_ebean, k_eventbus)

lazy val k_chinapnr = (project in file("kmodules/K-ChinaPnr"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava)
  .dependsOn(k_base, k_jsonapi, k_ebean, k_eventbus)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(
    k_base,
    k_ebean,
    k_jsonapi,
    k_plantask,
    k_eventbus,
    k_memcached,
    k_freemarker,
    k_emailer,
    k_redis,
    k_pdftemplate,
    k_sms,
    k_wechat,
    k_chinapnr)

  // lazy val root = (project in file("."))
  // .enablePlugins(PlayJava, PlayEbean)
  // .aggregate(
  //   k_jsonapi)

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
