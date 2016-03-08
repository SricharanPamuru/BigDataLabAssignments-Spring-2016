package edu.umkc.fv

import edu.umkc.fv.NLPUtils._
import edu.umkc.fv.Utils._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by Sricharan on 02-Mar-16.
  */
object FeatureVector1 {

  def main(args: Array[String]) {
    val filters = args

    System.setProperty("hadoop.home.dir", "F:\\winutils")
    System.setProperty("twitter4j.oauth.consumerKey", "9twtGVvM5vld4fbpUXxNoQcMC")
    System.setProperty("twitter4j.oauth.consumerSecret", "RYJf5QD3wNtrSSM17LmTgWFsVCHhvSlItHMUq4kXvbl98izsMK")
    System.setProperty("twitter4j.oauth.accessToken", "4875580214-Xo4Wv2cz2D8dnNM1KTmPM5APNuOtB1gIOcOaXuM")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "suI93glRxpZtrEJpWRQxvgfZrzlCrTmxtSKvxhbUKAEQt")


    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-Machine_Learning-Text-1").set("spark.driver.memory", "3g").set("spark.executor.memory", "3g")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None, filters)
    // stream.saveAsTextFiles("data/testing/output.txt")
    /*val test = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Gas")))
    test.saveAsTextFiles("data/testing/gas")
    val health = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Gas")))
    health.saveAsTextFiles("data/training/gas/gas.txt")
     val android1 = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Restaurants")))
     android1.saveAsTextFiles("data/training/restaurants/restaurants.txt")
     val jobs1 = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Bars")))
     jobs1.saveAsTextFiles("data/training/bars/bars.txt")
     ssc.start()
     ssc.awaitTermination(10000)*/
    val sc = ssc.sparkContext
    val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value
    val labelToNumeric = createLabelMap("data/training/")
    var model: NaiveBayesModel = null
    // Training the data
    val training = sc.wholeTextFiles("data/training/*")
      .map(rawText => createLabeledDocument(rawText, labelToNumeric, stopWords))
    val X_train = tfidfTransformer(training)
    X_train.foreach(vv => println(vv))

    model = NaiveBayes.train(X_train, lambda = 1.0)

    val lines=sc.wholeTextFiles("data/testing/*")
    val data = lines.map(line => {

      val test = createLabeledDocumentTest(line._2, labelToNumeric, stopWords)
      println(test.body)
      test


    })

    val X_test = tfidfTransformerTest(sc, data)

    val predictionAndLabel = model.predict(X_test)
    println("PREDICTION")
    predictionAndLabel.foreach(x => {
      labelToNumeric.foreach { y => if (y._2 == x) {
        println(y._1)
      }
      }
    })





  }


}