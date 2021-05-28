scalaVersion := "3.0.0"

run / fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor-typed_2.13" % "2.6.14"
, "com.typesafe.akka" % "akka-stream_2.13" % "2.6.14"
, "com.typesafe.akka" % "akka-http-core_2.13" % "10.2.4"
)

dependsOn(proto)

lazy val proto = project.in(file("deps/proto/proto")).settings(
  scalaVersion := "3.0.0"
, libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.17.0"
).dependsOn(protoops)

lazy val protoops = project.in(file("deps/proto/ops")).settings(
  scalaVersion := "3.0.0"
).dependsOn(protosyntax)

lazy val protosyntax = project.in(file("deps/proto/syntax")).settings(
  scalaVersion := "3.0.0"
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
