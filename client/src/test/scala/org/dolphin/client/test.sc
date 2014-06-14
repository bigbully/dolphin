var map = Map.empty[Int, String]
map += (1 -> "abc")

map.get(1).isInstanceOf[String]
map.get(2).isInstanceOf[String]
