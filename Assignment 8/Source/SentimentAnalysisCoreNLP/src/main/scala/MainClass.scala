import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


object MainClass {

  def main(args: Array[String]) {

    val filters = args

    System.setProperty("twitter4j.oauth.consumerKey", "1oVEx4fjDXAqzOoNLZxOJw91C")
    System.setProperty("twitter4j.oauth.consumerSecret", "gj0731lQSSoZMfWJfeCqW0KUCzrKiqKyEMLcr3C4aTWQEQa2Xm")
    System.setProperty("twitter4j.oauth.accessToken", "702647205807534080-UZjUEaNvQaYhTWI3MdD3cyBHAamf3jd")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "El29gJVngXwIUJmFGb0MoKvUeN5UBGcxdBNCrUrUveSbq")
    val sparkConf = new SparkConf().setAppName("STweetsApp").setMaster("local[*]")
    //Create a Streaming COntext with 2 second window
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    val stream = TwitterUtils.createStream(ssc, None, filters)
    val english_tweets=stream.filter(_.getLang()=="en")
    val tweets = english_tweets.map(status => status.getText())

    tweets.foreachRDD(rdd => rdd.collect().foreach(text =>
    {
      println(text)
      val sentimentAnalyzer: SentimentAnalyzer = new SentimentAnalyzer
      val tweetWithSentiment: TweetWithSentiment = sentimentAnalyzer.findSentiment(text)
      System.out.println(tweetWithSentiment)
    }



    ))
    ssc.start()

    ssc.awaitTermination()


  }
}
