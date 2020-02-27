package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val distanceThreshold: Double = 1000.0

  val powerParameter: Double = 5.0
  val earthRadius: Double = 6371000.0

  val imageWidth: Int = 360
  val imageHeight: Int = 180

  val alphaLevel: Int = 255

  def distance(location1: Location, location2: Location): Double = {
    val phi1 = math.toRadians(location1.lat)
    val phi2 = math.toRadians(location2.lat)

    val deltaPhi = math.toRadians(location2.lat - location1.lat)
    val deltaLambda = math.toRadians(location2.lon - location1.lon)

    val a =
      math.sin(deltaPhi / 2.0) * math.sin(deltaPhi / 2.0) +
        math.cos(phi1) * math.cos(phi2) *
          math.sin(deltaLambda / 2.0) * math.sin(deltaLambda / 2.0)

    val c = 2 * math.atan2(math.sqrt(a), math.sqrt(math.max(0.0, 1 - a))) // the max operation protects against rounding errors (1 - 1.00000000002)
    val distance = earthRadius * c
    distance
  }

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val distances: Iterable[((Location, Double), Double)] = temperatures.map(
      (pair) => {
        val otherLocation = pair._1
        (pair, distance(otherLocation, location))
      }
    ).toList.sortBy(_._2)

    if (distances.head._2 < distanceThreshold) {
      distances.head._1._2
    } else {
      val result = distances.map(
        (distancePair) => {
          val distance = distancePair._2
          val temperature = distancePair._1._2

          assert(distance >= 0.0, "distancePair: " + distancePair + ", location: " + location)
          val weight: Double = 1.0 / math.pow(distance, powerParameter)
          (weight * temperature, weight)
        }
      ).reduce(
        (left: (Double, Double), right: (Double, Double)) => {
          (left._1 + right._1, left._2 + right._2)
        }
      )

      val finalResult = result._1 / result._2
      finalResult
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val sortedPoints = points.toList.sortBy(_._1)
    val pointRanges = sortedPoints.zip(sortedPoints.tail)
    val pointRangeOption = pointRanges.find((pointRange) => value >= pointRange._1._1 && value < pointRange._2._1)

    if (pointRangeOption.isDefined) {
      val pointRange = pointRangeOption.get

      val value1 = pointRange._1._1
      val value2 = pointRange._2._1
      val color1 = pointRange._1._2
      val color2 = pointRange._2._2

      val shiftedValue = (value.toDouble - value1)
      val linearScalar = shiftedValue / (value2 - value1)
      val interpolatedColor = new Color(
        math.round(color1.red + (linearScalar) * (color2.red - color1.red)).toInt,
        math.round(color1.green + (linearScalar) * (color2.green - color1.green)).toInt,
        math.round(color1.blue + (linearScalar) * (color2.blue - color1.blue)).toInt
      )
      interpolatedColor
    } else {
      if (value < pointRanges.head._1._1) {
        pointRanges.head._1._2
      } else {
        assert(value >= pointRanges.last._2._1, s"value: $value")
        pointRanges.last._2._2
      }
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val locations =
      for (y <- 0 until imageHeight; x <- 0 until imageWidth) yield new Location(imageHeight / 2 - y, x - imageWidth / 2)

    val pixels = locations.par.map(
      (location) => {
        val color = interpolateColor(colors, predictTemperature(temperatures, location))
        Pixel(color.red, color.green, color.blue, alphaLevel)
      }
    ).toArray

    Image(imageWidth, imageHeight, pixels)
  }

}

