import scala.concurrent.Future

/**
 * User: bigbully
 * Date: 14-5-4
 * Time: 下午11:45
 */


case class Fraction(val num: Int, val den: Int) {

  var name:String = _
  def this(num:Int, den:Int, name:String) = {
    this(num, den)
    this.name = name
  }

}

object Fraction {
  def apply(num:Int, den:Int, name:String) = {
    new Fraction(num, den, name)
  }

  def unapply(fraction:Fraction) = {
    Some((1,2, 3))
  }
}

val f = Fraction(1, 2)

f match {
  case Fraction(a, b) => {
    println("fd")
  }
  case Fraction(a, b, c) => {

  }
}