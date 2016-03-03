import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scala.util.control._
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.rdd;

/**
  * Created by pradyumnad on 07/07/15.
  */
object TSMainApp {

  val sparkConf = new SparkConf().setAppName("TwitterSparkStreaming").setMaster("local[*]");

  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir","F:\\winutils");
    //val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]");

   // val sc= new SparkContext(sparkConf)

    val filters = args
    val loop = new Breaks;

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generate OAuth credentials

    System.setProperty("twitter4j.oauth.consumerKey", "1oVEx4fjDXAqzOoNLZxOJw91C")
    System.setProperty("twitter4j.oauth.consumerSecret", "gj0731lQSSoZMfWJfeCqW0KUCzrKiqKyEMLcr3C4aTWQEQa2Xm")
    System.setProperty("twitter4j.oauth.accessToken", "702647205807534080-UZjUEaNvQaYhTWI3MdD3cyBHAamf3jd")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "El29gJVngXwIUJmFGb0MoKvUeN5UBGcxdBNCrUrUveSbq")



    //Create a spark configuration with a custom name and master
    // For more master configuration see  https://spark.apache.osrg/docs/1.2.0/submitting-applications.html#master-urls
    val sparkConf = new SparkConf().setAppName("STweetsApp").setMaster("local[*]")
    //Create a Streaming COntext with 2 second window
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    //Using the streaming context, open a twitte
    // r stream (By the way you can also use filters)
    //Stream generates a series of random tweets
    val stream = TwitterUtils.createStream(ssc, None, filters)
    stream.print()
    //Map : Retrieving Hash Tags
    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    //Finding the top hash Tags on 30 second window
    val topCounts30 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(30))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))
    //Finding the top hash Tgas on 10 second window


    // Print popular hashtags


    topCounts30.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))

      topList.foreach { case (count, tag) =>
        println("%s (%s tweets)".format(tag, count))
        val result="%s (%s tweets)".format(tag, count);
        rdd.saveAsTextFile("output")
        SocketClient.sendCommandToRobot("TAG:"+tag+" "+"Count:"+count +" tweet\n ")
      }



    })
    ssc.start()

    ssc.awaitTermination()
  }

}