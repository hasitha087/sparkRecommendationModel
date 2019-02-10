# sparkRecommendationModel
This is Spark/Scala based Mobile Telecommunication Customer Value Added Services Recommendation Model which used Collaborative Filtering based Alternative Least Square Algorithm. 

* This reads data from impala tables and write into parquet. There are three tables. First one includes customer identity and their current products. Second one includes all the product catelog. Third one includes customer identity and products which they don't have.
* To compile use SBT
  * sbt package
  * sbt compile
* Relavent dependancies included in build.sbt
