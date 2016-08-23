name := """K-Base"""

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

libraryDependencies += "com.google.code.maven-play-plugin.org.playframework" % "jj-imaging" % "1.1"

libraryDependencies += "com.google.code.maven-play-plugin.org.playframework" % "jj-simplecaptcha" % "1.1"

libraryDependencies += "commons-io" % "commons-io" % "2.5"

libraryDependencies += "org.jodd" % "jodd-core" % "3.7"

libraryDependencies += "org.jodd" % "jodd-bean" % "3.7"

libraryDependencies += "org.jodd" % "jodd-http" % "3.7"

libraryDependencies += "org.jodd" % "jodd-mail" % "3.7"

libraryDependencies += "org.freemarker" % "freemarker" % "2.3.21"

libraryDependencies += "org.xhtmlrenderer" % "flying-saucer-pdf" % "9.0.8"

libraryDependencies += "net.sourceforge.jexcelapi" % "jxl" % "2.6.12"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.39"

libraryDependencies += "com.googlecode.xmemcached" % "xmemcached" % "2.0.0"

libraryDependencies += "redis.clients" % "jedis" % "2.8.1"

