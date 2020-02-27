package observatory

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 1st milestone: data extraction
  */
object Extraction {

  @transient lazy val conf: SparkConf =  new SparkConf()
    .setMaster("local[*]")
    .setAppName("Extraction")
    .set("spark.network.timeout", "10000001")
    .set("spark.executor.heartbeatInterval", "10000000")
  @transient lazy val sc: SparkContext = new SparkContext(conf)

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stationLines: RDD[String] = sc.textFile(new File(Extraction.getClass.getResource(stationsFile).toURI).getAbsolutePath)
    val stationStns: RDD[((String, String), Location)] = stationLines.map(
        (line) => {
          val columns = line.split(",", 4)
          assert(columns.size == 4)
          val stn = columns(0)
          val wban = columns(1)
          val lat = columns(2)
          val lon = columns(3)

          ((stn, wban), if (lat.nonEmpty && lon.nonEmpty) Location(lat.toDouble, lon.toDouble) else Location.invalid)
        }
      ).filter(_._2 != Location.invalid)

    val temperatureLines: RDD[String] = sc.textFile(new File(Extraction.getClass.getResource(temperaturesFile).toURI).getAbsolutePath)
    val temperatures: RDD[((String, String), (LocalDate, Double))] = temperatureLines.map(
      (line) => {
        val columns = line.split(",", 5)
        assert(columns.size == 5)
        val stn = columns(0)
        val wban = columns(1)
        val month = columns(2).toInt
        val day = columns(3).toInt
        val temperature = (columns(4).toDouble - 32.0) * 5.0 / 9.0

        ((stn, wban), (LocalDate.of(year, month, day), temperature))
      }
    )

    stationStns.join(temperatures).mapValues(value => (value._2._1, value._1, value._2._2)).groupByKey().flatMap(_._2).collect()
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records.par.map(
      (record) => (record._2, record._3)
    ).groupBy(
      _._1
    ).mapValues(
      (vals) => {
        val result = vals.map((temp) => (1, temp._2)).reduce((temp1, temp2) => (temp1._1 + temp2._1, temp1._2 + temp2._2))
        result._2 / result._1
      }
    ).toList
  }

}
