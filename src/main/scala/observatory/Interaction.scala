package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization._

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  val tileSize: Int = 256
  val alphaLevel: Int = 127
  val zoomRange = (0 to 3)

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val numTiles = math.pow(2.0, tile.zoom)
    val lat = math.atan(math.sinh(math.Pi - tile.y / numTiles * 2 * math.Pi)) * 180 / math.Pi
    val lon = (tile.x / numTiles * 360) - 180
    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val innerTileCoordinates =
      for (innerY <- 0 until tileSize; innerX <- 0 until tileSize) yield (innerX + tile.x * tileSize, innerY + tile.y * tileSize)

    val pixels = innerTileCoordinates.par.map(
      (innerTileCoordinate) => {
        val innerX = innerTileCoordinate._1
        val innerY = innerTileCoordinate._2

        val location = tileLocation(Tile(innerX, innerY, tile.zoom + 8))

        val color = Visualization.interpolateColor(colors, Visualization.predictTemperature(temperatures, location))
        Pixel(color.red, color.green, color.blue, alphaLevel)
      }
    ).toArray

    Image(tileSize, tileSize, pixels)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    yearlyData.par.foreach(
      (yearData) => {
        val year = yearData._1
        val data = yearData._2

        zoomRange.par.foreach(
          (zoom) => {
            val numTiles = math.pow(2.0, zoom).toInt
            val coordinates = for (x <- 0 until numTiles; y <- 0 until numTiles) yield (x, y)
            coordinates.par.foreach(
              (coordinate) => {
                val x = coordinate._1
                val y = coordinate._2
                generateImage(year, Tile(x, y, zoom), data)
              }
            )
          }
        )
      }
    )
  }

}
