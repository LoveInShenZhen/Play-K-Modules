name := """ToolBox"""

javacOptions ++= Seq("-parameters", "-Xlint:unchecked", "-Xlint:deprecation", "-encoding", "UTF8")

scalacOptions ++= Seq("-encoding", "UTF8")

kotlinLib("stdlib")
kotlinLib("reflect")
//kotlincOptions += "-verbose"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

kotlincOptions += "-verbose"
kotlinLib("stdlib")
kotlinLib("reflect")


libraryDependencies += filters

libraryDependencies += "org.jodd" % "jodd-core" % "3.7"

libraryDependencies += "org.jodd" % "jodd-bean" % "3.7"

libraryDependencies += "org.jodd" % "jodd-http" % "3.7"

libraryDependencies += "org.jodd" % "jodd-mail" % "3.7"

libraryDependencies += "commons-io" % "commons-io" % "2.5"

libraryDependencies += "org.simpleframework" % "simple-xml" % "2.7.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.39"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-kotlin" % "2.7.7"

libraryDependencies += "org.freemarker" % "freemarker" % "2.3.21"