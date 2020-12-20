package data.challenge.session

import data.challenge.session.parser.{AccessLogParser, UserAgentParserSetting}
import org.apache.log4j.LogManager
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.rogach.scallop.ScallopConf

object Main {

  val logger = LogManager.getLogger(this.getClass.getSimpleName)

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val inputPath = opt[String](required = true)
    val outputPath = opt[String](required = true)
    val outputLimit = opt[Int](default = Some(100))
    val userAgentParserCacheSize = opt[Int](default = Some(10000))
    val sessionThreshold = opt[Int](default = Some(10 * 60))
    verify()
  }

  val userAgentParserFields = Seq(
    "DeviceName",
    "DeviceClass",
    "OperatingSystemNameVersion",
    "OperatingSystemNameVersionMajor",
    "OperatingSystemClass",
    "AgentNameVersion",
    "AgentNameVersionMajor",
    "AgentClass")

  val filterAgentClasses = Set("Robot", "Hacker")

  val visitorsPartitionFields = Seq(
    "request_ip",
    "user_agent.DeviceName",
    "user_agent.AgentNameVersion",
    "user_agent.OperatingSystemNameVersionMajor",
    "user_agent.AgentClass"
  )

  def main(args: Array[String]) {
    val conf = new Conf(args)

    val spark = SparkSession.builder()
      .appName("Session Analysis")
      .getOrCreate()

    import spark.implicits._

    val ds = spark.read.textFile(conf.inputPath())

    implicit val uapSetting = UserAgentParserSetting(conf.userAgentParserCacheSize(), userAgentParserFields)
    val events = ds
      .map(AccessLogParser.parseAccessLog)
      // filter parsed failed records
      .filter(isnull('_corrupt_record))
      .withColumn("ts", to_timestamp('timestamp).cast("Double"))
      // filter fraud and robot visitors
      .filter(!$"user_agent.AgentClass".isInCollection(filterAgentClasses))
    // visitor partition key
    // TODO: consider using IDFA, IDFV(ios14+), cookies, IMEI, MAC address to help identifying individual users
    val partitionCols = visitorsPartitionFields.map(col)
    // partition events by visitor partition key and sort by ts
    val windowSpec = Window.partitionBy(partitionCols: _*).orderBy('ts)
    val sessionThreshold = conf.sessionThreshold()
    val sessions = events
      // sliding window size of 2 to get previous record ts
      .withColumn("prevTs", lag('ts, 1).over(windowSpec))
      // mark start event of each new session
      .withColumn("isNewSession", when('ts.minus('prevTs) < lit(sessionThreshold), lit(0)).otherwise(lit(1)))
      // generate sessionId for each event
      .withColumn("sessionId", sum('isNewSession).over(windowSpec))
      .groupBy(partitionCols :+ $"sessionId": _*)
      .agg(
        min($"ts").as("startTs"),
        max($"ts").as("endTs"),
        // count size of unique url
        // TODO: consider using HyperLogLog to estimate distinct count (approx_count_distinct)
        size(collect_set($"url")).as("hits"))
      .withColumn("sessionTime", $"endTs".minus($"startTs"))
      .cache
    val avgSessionTime = sessions
      // filter zero-length sessions
      .filter('sessionTime > 0)
      .select(avg('sessionTime))
      .head.getDouble(0)
    val outDF = sessions.withColumn("avgSessionTime", lit(avgSessionTime))
      .drop("startTs", "endTs")
      .sort('sessionTime.desc)
    outDF.show(false)
    outDF
      .limit(conf.outputLimit())
      .write
      .mode(SaveMode.Overwrite)
      .json(conf.outputPath())

    spark.stop()
  }
}
