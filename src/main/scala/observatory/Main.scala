package observatory

import java.io.File

import org.apache.log4j.{Level, Logger}
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool

object Main extends App {
  val colors: List[(Double, Color)] = List(
    (60.0, Color(255, 255, 255)),
    (32.0, Color(255, 0, 0)),
    (12.0, Color(255, 255, 0)),
    (0.0, Color(0, 255, 255)),
    (-15.0, Color(0, 0, 255)),
    (-27.0, Color(255, 0, 255)),
    (-50.0, Color(33, 0, 107)),
    (-60.0, Color(0, 0, 0))
  )

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val normalsRange = 1975 to 1989
  val deviationsRange = 1990 to 2015
  
  val averageTemperaturesByYearTaskSupport = new ForkJoinTaskSupport(new ForkJoinPool(8))
  val yearsPar = (normalsRange.start to deviationsRange.end).par
  yearsPar.tasksupport = averageTemperaturesByYearTaskSupport

  val averageTemperaturesByYear = yearsPar.par.map(
    (year) => {
      val temperatures = Extraction.locateTemperatures(year, "/stations.csv", s"/$year.csv")
      val averagedTemperatures = Extraction.locationYearlyAverageRecords(temperatures)
      (year, averagedTemperatures)
    }
  ).toList

  def generateTemperatureImages(year: Int, tile: Tile, temperatures: Iterable[(Location, Double)]): Unit = {
    val imageFile: File = new File(s"target/temperatures/$year/${tile.zoom}/${tile.x}-${tile.y}.png")
    imageFile.getParentFile.mkdirs()

    Visualization2.visualizeGrid(Manipulation.makeGrid(temperatures), colors, tile).output(imageFile)
  }

  Interaction.generateTiles(averageTemperaturesByYear, generateTemperatureImages)


  val normalsAndDeviations = averageTemperaturesByYear.splitAt(normalsRange.size)
  val normals = Manipulation.average(normalsAndDeviations._1.map(_._2))

  def generateDeviationImages(year: Int, tile: Tile, temperatures: Iterable[(Location, Double)]): Unit = {
    val imageFile: File = new File(s"target/deviations/$year/${tile.zoom}/${tile.x}-${tile.y}.png")
    imageFile.getParentFile.mkdirs()

    Visualization2.visualizeGrid(Manipulation.deviation(temperatures, normals), colors, tile).output(imageFile)
  }

  Interaction.generateTiles(normalsAndDeviations._2, generateDeviationImages)
}
