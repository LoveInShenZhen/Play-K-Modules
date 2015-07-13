name := """K-Base"""

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies += "com.google.code.maven-play-plugin.org.playframework" % "jj-imaging" % "1.1"

libraryDependencies += "com.google.code.maven-play-plugin.org.playframework" % "jj-simplecaptcha" % "1.1"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.jodd" % "jodd-core" % "3.6.4"

libraryDependencies += "org.jodd" % "jodd-bean" % "3.6.4"

libraryDependencies += "org.jodd" % "jodd-http" % "3.6.4"

libraryDependencies += "org.jodd" % "jodd-mail" % "3.6.4"

libraryDependencies += "org.freemarker" % "freemarker" % "2.3.21"
