name := "Week3"

version := "0.1"

scalaVersion := "2.13.4"

lazy val akkaHttpVersion = "10.2.3"
lazy val akkaVersion    = "2.6.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
  //"ch.qos.logback"    % "logback-classic"           % "1.2.3",
)
