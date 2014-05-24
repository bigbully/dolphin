import scala.collection.SortedSet
import scala.concurrent.Future

/**
 * User: bigbully
 * Date: 14-5-4
 * Time: 下午11:45
 */

var list = List.empty[Long]
(0 to 10000000).foreach(list ::= _)
val start = System.currentTimeMillis()
list.reduceRight(_ + _)
val end = System.currentTimeMillis()
println(end - start)

var map = Map.empty
map.reduceRight()
var set = Set.empty
set.reduceRight()
