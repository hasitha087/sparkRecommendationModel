name := "CF VAS Recommendation"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "commons-codec" % "commons-codec" % "1.9",
  "org.apache.avro" % "avro" % "1.7.7",
  "org.xerial.snappy" % "snappy-java" % "1.1.2.1",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.apache.spark" % "spark-core_2.10" % "1.6.2",
  "org.apache.hbase" % "hbase" % "1.2.2",
  "org.apache.hbase" % "hbase-server" % "1.2.2",
  "org.apache.hbase" % "hbase-protocol" % "1.2.2",
  "org.apache.hbase" % "hbase-client" % "1.2.2",
  "org.apache.hbase" % "hbase-common" % "1.2.2",
  "org.apache.spark" % "spark-mllib_2.10" % "1.6.2",
  "org.apache.spark" % "spark-core_2.10" % "1.6.2",
  "org.apache.spark" % "spark-sql_2.10" % "1.6.2",
  "com.typesafe" % "config" % "1.2.1"
)

