name := "session_analysis"

version := "0.0.1"

organization := "data.challenge"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.4" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.4" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.4" % "provided"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.8.3" % "provided"

libraryDependencies += "org.rogach" %% "scallop" % "2.0.5"


fork := true

resolvers += "Maven Central Server" at "https://repo1.maven.org/maven2"
