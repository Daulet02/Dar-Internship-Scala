name := "Example1"

version := "0.1"

scalaVersion := "2.13.4"

val AkkaVersion = "2.6.12"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)