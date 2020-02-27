import observatory.Visualization.earthRadius
import observatory.{Location, Visualization}

val inputs = for (i <- 1 to 20 by 5) yield i
val outputs = for (i <- 10 to 200 by 50) yield i.toDouble
val pairs = inputs.zip(outputs)
val ranges = pairs.zip(pairs.tail)

def interp(input: Int): Double = {
  val rangeOption = ranges.find((range) => input >= range._1._1 && input < range._2._1)

  if (rangeOption.isDefined) {
    val range = rangeOption.get

    val input1 = range._1._1
    val input2 = range._2._1
    val output1 = range._1._2
    val output2 = range._2._2

    val adjustedInput = (input.toDouble - input1)
    val linearScalar = adjustedInput / (input2 - input1)
    val interpolatedOutput = output1 + (linearScalar) * (output2 - output1)
    interpolatedOutput
  } else {
    if (input < ranges.head._1._1) {
      ranges.head._1._2
    } else {
      assert(input >= ranges.last._2._1)
      ranges.last._2._2
    }
  }
}

val results = for (i <- -5 to 25) yield interp(i)


for (x <- 0 until 5; y <- 10 until 15) yield {
  (x, y)
}

val location1 = Location(50.0,-145.0)
val location2 = Location(130.0,35.0)
//val location1 = Location(12.0,-95.0)
//val location2 = Location(-12.0,85.0)
val phi1 = math.toRadians(location1.lat)
val phi2 = math.toRadians(location2.lat)

val deltaPhi = math.toRadians(location2.lat - location1.lat)
val deltaLambda = math.toRadians(location2.lon - location1.lon)

math.sin(deltaPhi / 2.0)
math.cos(phi1)
math.cos(phi2)
math.sin(deltaLambda / 2.0)

val a =
  math.sin(deltaPhi / 2.0) * math.sin(deltaPhi / 2.0) +
    math.cos(phi1) * math.cos(phi2) *
      math.sin(deltaLambda / 2.0) * math.sin(deltaLambda / 2.0)

val c = 2 * math.atan2(math.sqrt(a), math.sqrt(math.max(0.0, 1 - a)))
val distance = earthRadius * c
distance

