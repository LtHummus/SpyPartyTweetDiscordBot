
name := "SpyPartyBot"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.danielasfregola" %% "twitter4s" % "5.4"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.0-M2"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25"

mainClass in assembly := Some("com.lthummus.spypartybot.TwitterFeed")
assemblyJarName in assembly := "spypartybot.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}