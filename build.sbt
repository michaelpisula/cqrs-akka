name := "cqrs-es"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions += "-target:jvm-1.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.9"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.9"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
