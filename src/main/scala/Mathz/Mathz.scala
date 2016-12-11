package Mathz

/**
  * Created by kevin on 26/11/16.
  */
import java.math.{MathContext, RoundingMode}
import org.scalameter._
import scala.math.BigDecimal

abstract class TaylorSeries

case class Sin(x: Radian) extends TaylorSeries
case class Cos(x: Radian) extends TaylorSeries
case class Euler(x: BigDecimal) extends TaylorSeries

class Radian(val degrees: BigDecimal) {

  val PI = Math.PI

  val value = degrees * PI / 180

  def +(other: Radian) = new Radian(this.value + other.value)

  def -(other: Radian) = new Radian(this.value - other.value)

  def *(by: BigDecimal) = new Radian(this.value * by)

  def /(by: BigDecimal) = new Radian(this.value / by)

  def %(other: Radian) = new Radian(this.value / other.value)

  def toRange(rad: Radian): Radian = new Radian(this.degrees % rad.degrees)

  override def toString: String = "("+degrees + " * PI) / " + 180 + ")"

}


final class Mathz {

  def sin(x: Radian): BigDecimal = recTaylorSeries(Sin(x.toRange(new Radian(90))))(10, 1)

  def sin(x: Radian, nDecPlaces: Int): BigDecimal = recTaylorSeries(Sin(x.toRange(new Radian(90))))(nDecPlaces, 10)

  def cos(x: Radian): BigDecimal = recTaylorSeries(Cos(x.toRange(new Radian(180))))(10, 1)

  def cos(x: Radian, nDecPlaces: Int): BigDecimal = recTaylorSeries(Cos(x.toRange(new Radian(180))))(nDecPlaces, 10)

  def e(x: BigDecimal): BigDecimal = recTaylorSeries(Euler(x))(5, 1)

  def e(x: BigDecimal, mem: Boolean, nDecPlaces: Int): BigDecimal = {
    if (mem) eulerSeries(x, 0, 0, 0, 0, nDecPlaces, 0) else recTaylorSeries(Euler(x))(nDecPlaces, 5)
  }

  def taylorSeries(t: TaylorSeries): Stream[BigDecimal] = t match {
    case Sin(x) => sinSeries(x.value, 0)
    case Cos(x) => cosSeries(x.value.abs, 0)
    case Euler(x) => eulerSeries(x, 0)
  }

  def sumTaylorSeries(ts: Stream[BigDecimal], degree: Int): BigDecimal = ts.take(degree).sum

  def sumTaylorSeries(ts: Stream[BigDecimal]): BigDecimal = ts.sum

  def pow(base: BigDecimal, exponent: Int): BigDecimal = exponent match {
    case 0 => BigDecimal(1)
    case _ => base * pow(base, exponent - 1)
  }

  def factorial(x: Int): Int = x match {
    case 0 => 1
    case _ => x * factorial(x - 1)
  }

  private def recTaylorSeries(t: TaylorSeries)(nDecPlaces: Int, degree: Int): BigDecimal = t match {
    case Sin(x) => {
      val cur = taylorSeries(Sin(x)).take(degree / 2 + 1)
      val curSum = sumTaylorSeries(cur)
      val prevSum = curSum + cur.last
      if (accurateEnough(curSum, prevSum, nDecPlaces)) round(curSum, nDecPlaces) else recTaylorSeries(Sin(x))(nDecPlaces, degree + 1)
    }
    case Cos(x) => {
      val cur = taylorSeries(Cos(x)).take(degree / 2 + 1)
      val curSum = sumTaylorSeries(cur)
      val prevSum = curSum + cur.last
      if (accurateEnough(curSum, prevSum, nDecPlaces)) round(curSum, nDecPlaces) else recTaylorSeries(Cos(x))(nDecPlaces, degree + 1)
    }
    case Euler(x) => {
      val cur = taylorSeries(Euler(x)).take(degree + 1)
      val curSum = sumTaylorSeries(cur)
      val prevSum = curSum + cur.last
      if (accurateEnough(curSum, prevSum, nDecPlaces)) round(curSum, nDecPlaces) else recTaylorSeries(Euler(x))(nDecPlaces, degree + 1)
    }
  }

  private def eulerSeries(x: BigDecimal, n: Int): Stream[BigDecimal] = {
    Stream.cons(pow(x, n) / factorial(n), eulerSeries(x, n + 1))
  }

  //                      1          1                  1               2       1             5
  private def eulerSeries(x: BigDecimal, numerator: BigDecimal, factorial: BigDecimal,
                          n: Int, term: BigDecimal, nDecPlaces: Int, result: BigDecimal): BigDecimal = {
    val next_factorial = if (n <= 1) BigDecimal(1) else n * factorial
    val next_numer = if (n == 0) BigDecimal(1) else numerator * x
    val next_term = next_numer / next_factorial
    val accumResult = result + next_term
    if (accurateEnough(result, accumResult, nDecPlaces)) round(result, nDecPlaces)
    else eulerSeries(x, next_numer, next_factorial, n + 1, next_term, nDecPlaces, accumResult)

  }

  private def cosSeries(x: BigDecimal, n: Int): Stream[BigDecimal] = {
    val v_n = if (2 * n <= 0) 0 else 2 * n
    Stream.cons(pow(-1, n) * pow(x, v_n) / factorial(v_n), cosSeries(x, n + 1))
  }

  private def sinSeries(x: BigDecimal, n: Int): Stream[BigDecimal] = {
    val v_n = if (2 * (n - 1) - 1 <= 0) 0 else 2 * (n - 1) - 1
    Stream.cons((pow(-1, n) * pow(x, v_n)) / factorial(v_n), sinSeries(x, n + 1))
  }

  private def accurateEnough(curValue: BigDecimal, prevValue: BigDecimal, nDecPts: Int): Boolean = {

    val prev = round(prevValue, nDecPts + 2)
    val cur = round(curValue, nDecPts + 2)
/*    println("p: " + prev)
    println("c: " + cur)
    println()*/
    (cur - prev).abs == 0.0
  }

  def round(n: BigDecimal, p: Int): BigDecimal = {
    n.round(new MathContext(p, RoundingMode.HALF_UP))
  }




}

object Mathz {

  val m = new Mathz()

  def sin(x: Radian): BigDecimal = sin(x)
  def sin(x: Radian, nDecPlaces: Int): BigDecimal = m.sin(x, nDecPlaces)
  def cos(x: Radian): BigDecimal = m.cos(x)
  def cos(x: Radian, nDecPlaces: Int): BigDecimal = m.cos(x, nDecPlaces)
  def e(x: BigDecimal): BigDecimal = m.e(x)
  def e(x: BigDecimal, nDecPlaces: Int, mem: Boolean): BigDecimal = m.e(x, mem, nDecPlaces)

  def pow(base: BigDecimal, exponent: Int): BigDecimal = m.pow(base, exponent)

  def factorial(x: Int): Int = m.factorial(x)

  def taylorSeries(t: TaylorSeries): Stream[BigDecimal] = m.taylorSeries(t)

  def sumTaylorSeries(ts: Stream[BigDecimal], degree: Int): BigDecimal = m.sumTaylorSeries(ts, degree)

  def round(n: BigDecimal, p:Int) = m.round(n, p)

}


object MathzRunner {


  val standardConfig = config(
    Key.exec.minWarmupRuns -> 20,
    Key.exec.maxWarmupRuns -> 40,
    Key.exec.benchRuns -> 25,
    Key.verbose -> false
  ) withWarmer(new Warmer.Default)



  def main(args: Array[String]): Unit = {

    val m = new Mathz()

    val noMemory = standardConfig measure m.e(1, false, 5)

    val memory = standardConfig measure  m.e(1, true, 5)

    val speedUp = noMemory.value / memory.value

    println("========================= e Time ================================== Value")
    println()
    print(s"Memory time:".padTo(25, ' '))
    print(s"$memory".padTo(40, ' '))
    println(m.e(0.3, false, 10).toString.padTo(25, ' '))
    print(s"No Memory time:".padTo(25, ' '))
    print(s"$noMemory".padTo(40, ' '))
    println(m.e(0.3, true,  10).toString.padTo(25, ' '))

    print(s"Speed up:".padTo(25, ' '))
    print(s"$speedUp".padTo(25, ' '))
    println()
    println()
    println("========================================================================")

    val lib = standardConfig measure Math.sin(0.3)

    val mine = standardConfig measure  m.sin(new Radian(0.3))

    val speedUp2 = lib.value / mine.value

    println()
    println("========================= Sin Time ================================== Value")
    println()
    print(s"Mine:".padTo(25, ' '))
    print(s"$mine".padTo(40, ' '))
    println(m.sin(new Radian(0.3), 10).toString.padTo(25, ' '))
    print(s"Library:".padTo(25, ' '))
    print(s"$lib".padTo(40, ' '))
    println(Math.sin(0.3)).toString.padTo(25, ' ')

    print(s"Speed up:".padTo(25, ' '))
    print(s"$speedUp2".padTo(25, ' '))
    println()
    println()
    println("========================================================================")
  }
}




