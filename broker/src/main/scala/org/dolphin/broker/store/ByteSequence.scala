package org.dolphin.broker.store

/**
 * User: bigbully
 * Date: 14-5-18
 * Time: 下午12:57
 */
class ByteSequence(var data:Array[Byte]) {
  var offset = 0
  var length = data.length

  def this(data: Array[Byte], offset: Int, length: Int) {
    this(data)
    this.offset = offset
    this.length = length
  }

  def getData: Array[Byte] = {
    return data
  }

  def getLength: Int = {
    return length
  }

  def getOffset: Int = {
    return offset
  }

  def setData(data: Array[Byte]) {
    this.data = data
  }

  def setLength(length: Int) {
    this.length = length
  }

  def setOffset(offset: Int) {
    this.offset = offset
  }

  def compact {
    if (length != data.length) {
      val t = new Array[Byte](length)
      System.arraycopy(data, offset, t, 0, length)
      data = t
      offset = 0
    }
  }

  def indexOf(needle:ByteSequence, pos:Int):Int = {
    val max = length - needle.length
    for (i <- pos until max) {
      if (matches(needle, i)) {
        return i
      }
    }
    -1
  }

  private def matches(needle:ByteSequence, pos:Int):Boolean = {
    for(i <- 0 until needle.length){
      if (data(offset + pos + i) != needle.data(needle.offset + i)){
        return false
      }
    }
    return true
  }

}
