package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {
  val tileSize: Int = 256
  val alphaLevel: Int = 127

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    (d00 * (1 - point.x) * (1 - point.y)) + 
    (d01 * (1 - point.x) * point.y) + 
    (d10 * point.x * (1 - point.y)) + 
    (d11 * point.x * point.y)
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val innerTileCoordinates =
      for (innerY <- 0 until tileSize; innerX <- 0 until tileSize) yield (innerX + tile.x * tileSize, innerY + tile.y * tileSize)

    val pixels = innerTileCoordinates.par.map(
      (innerTileCoordinate) => {
        val innerX = innerTileCoordinate._1
        val innerY = innerTileCoordinate._2

        val location = Interaction.tileLocation(Tile(innerX, innerY, tile.zoom + 8))

        val lat = location.lat
        val latAsInt = lat.toInt
        val lon = location.lon
        val lonAsInt = lon.toInt

        val temperature = bilinearInterpolation(
          CellPoint((lon - lonAsInt), 1 - (lat - latAsInt)),
          grid(GridLocation(latAsInt + 1, lonAsInt)),
          grid(GridLocation(latAsInt, lonAsInt)),
          grid(GridLocation(latAsInt + 1, lonAsInt + 1)),
          grid(GridLocation(latAsInt, lonAsInt + 1))
        )

        val color = Visualization.interpolateColor(colors, temperature)
        Pixel(color.red, color.green, color.blue, alphaLevel)
      }
    ).toArray

    Image(tileSize, tileSize, pixels)
  }

}
