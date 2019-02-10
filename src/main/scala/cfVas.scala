import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import java.io.IOException

import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.Row
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.sql.SQLContext
//import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
//import sqlContext.implicits._
import sys.process._
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.feature.IndexToString
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.{ParamGridBuilder, CrossValidator}
import org.apache.spark.ml.{PredictionModel, Predictor, PredictorParams}
import org.apache.spark.ml.param._
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vector

import java.io.File
import org.apache.hadoop.conf.Configuration
import com.typesafe.config.ConfigFactory

import java.util.Calendar
import java.text._

object cfVas {
   def main(args: Array[String]): Unit={
     
        val conf = new SparkConf().setAppName("CF VAS Recommendation")
        val sc = new SparkContext(conf)
        val sqlContext = new org.apache.spark.sql.SQLContext(sc)
        val filePath = new File("").getAbsolutePath
        val config = ConfigFactory.parseFile(new File(filePath + "/../conf/cfVas.conf"))
                
        val data1 = sqlContext.read.parquet("/user/hive/warehouse/db1.db/input_table")
        data1.registerTempTable("recommend_cf_vas_input")
        val query = s"select  * from recommend_cf_vas_input"
        val ratings = sqlContext.sql(query)
        ratings.show()
                
        val data2 = sqlContext.read.parquet("/user/hive/warehouse/db1.db/mapping_all_products_table")
        data2.registerTempTable("vas_services")
        val query2 = s"select distinct cast(service_id as int) item, name from vas_services"
        val items = sqlContext.sql(query2)
        items.show()
                
        val data3 = sqlContext.read.parquet("/user/hive/warehouse/db1.db/input_table2_not_rated_data")
        data3.registerTempTable("recommend_cf_vas_notrated")
        val query3 = s"select * from recommend_cf_vas_notrated"
        val notRated = sqlContext.sql(query3)
        notRated.show()
                
        sc.setCheckpointDir("hdfs://nameservice1//user/checkPoint2")
                
        ratings.sample(false, 0.0001, seed=0).show(10)
        val Array(training, test) = ratings.randomSplit(Array(0.7, 0.3), seed=0L)
        training.sample(false, 0.0001, seed=0).show(10)
        test.sample(false, 0.0001, seed=0).show(10)
                
        val als = new ALS().setMaxIter(config.getInt("setMaxIter")).setRegParam(0.01).setUserCol("user").setItemCol("item").setRatingCol("rating")
        val model = als.fit(training)
        
        val predictions = model.transform(test).na.drop()
        predictions.show(10)
                
        //Evalation RMSEA
        val evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("rating").setPredictionCol("prediction")
        val rmse = evaluator.evaluate(predictions)
        println("Root-mean-square error = " + rmse)
                
        //Cross Validation
        val paramGrid = new ParamGridBuilder().addGrid(als.regParam, Array(0.01, 0.1)).build()
        val cv = new CrossValidator().setEstimator(als).setEvaluator(evaluator).setEstimatorParamMaps(paramGrid).setNumFolds(2)
        val cvModel = cv.fit(training)
        println("Best fit root-mean-square error = " + evaluator.evaluate(cvModel.transform(test).na.drop()))
        
        val predictions_user = cvModel.transform(notRated).na.drop()
        //val top10 = predictions_user.select("user", "item", "prediction").sort($"prediction".desc).show(10, false)
        println(predictions_user.count())
                
        predictions_user.registerTempTable("recommend_cf_vas_out")
        val results = sqlContext.sql("SELECT * FROM recommend_cf_vas_out")
        results.write.mode("Overwrite").parquet("/source/rfVasOut")
        
   }
}
