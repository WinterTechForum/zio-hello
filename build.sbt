enablePlugins(JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-zio" % "0.16",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

dockerPermissionStrategy := com.typesafe.sbt.packager.docker.DockerPermissionStrategy.Run

dockerRepository := sys.props.get("docker.repo")

dockerUsername := sys.props.get("docker.username")
