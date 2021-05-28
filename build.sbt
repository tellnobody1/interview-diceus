scalaVersion := "3.0.0"

run / fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor-typed_2.13" % "2.6.14"
, "com.typesafe.akka" % "akka-stream_2.13" % "2.6.14"
, "com.typesafe.akka" % "akka-http-core_2.13" % "10.2.4"
)

scalacOptions := Seq(
  "-encoding", "UTF-8"
, "-language:strictEquality", "-language:postfixOps"
, "-source", "future", "-deprecation"
, "-Yexplicit-nulls"
)

ThisBuild / turbo := true
ThisBuild / useCoursier := true
Global / onChangedBuildSource := ReloadOnSourceChanges