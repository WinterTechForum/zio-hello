enablePlugins(JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-zio" % "0.16",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
