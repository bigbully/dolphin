def recursiveTest(a:String) {
  val b = try {
    a.toInt + 1
  }catch {
    case e:Exception => {
      return
    }
  }
  println(b)
  recursiveTest(b.toString)
}

recursiveTest("a")