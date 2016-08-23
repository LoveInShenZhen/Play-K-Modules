name := """K-WeChat"""

kotlinLib("stdlib")

kotlincOptions += "-verbose"

// libraryDependencies ++= Seq(
//   javaJdbc,
//   cache,
//   javaWs
// )

libraryDependencies += "org.simpleframework" % "simple-xml" % "2.7.1"