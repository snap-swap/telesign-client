name := "telesign-client"

organization  := "com.snapswap"

version       := "1.0.0"

scalaVersion  := "2.11.8"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-encoding",
  "UTF-8"
)

libraryDependencies ++= {
  val akkaHttpV = "10.0.10"
  Seq(
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.google.code.findbugs" % "jsr305" % "3.0.1" % "provided",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
}
