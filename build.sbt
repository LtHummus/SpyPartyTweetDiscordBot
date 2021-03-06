
name := "SpyPartyBot"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.JCenterRepository

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.0-M2"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.6"
libraryDependencies += "net.jodah" % "expiringmap" % "0.5.8"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "1.1.1"
libraryDependencies += "net.dv8tion" % "JDA" % "3.6.0_359"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"


mainClass in assembly := Some("com.lthummus.spypartybot.Main")
assemblyJarName in assembly := "spypartybot.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}