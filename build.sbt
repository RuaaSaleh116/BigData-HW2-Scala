name := "BigData-HW2-Scala"

version := "0.1"

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "4.2.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.6.0",
  "io.github.cdimascio" % "dotenv-java" % "3.0.0"
)

// Run Spark in a separate JVM
fork := true

// Java 21 compatibility
javaOptions ++= Seq(
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
)
