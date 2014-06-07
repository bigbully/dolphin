package org.dolphin.broker.store

import java.io.{DataInput, InputStream}

/**
 * User: bigbully
 * Date: 14-5-23
 * Time: 下午3:00
 */
class DataByteArrayInputStream(private var buf: Array[Byte]) extends InputStream with DataInput {

  private var pos = 0
  private val offset  = 0
  private var length = buf.length

  private val work = new Array[Byte](8)

  def this(sequence: ByteSequence) {
    this(sequence.getData)
  }

  def this() {
    this(new Array[Byte](0))
  }

  def size = pos - offset

  def getRawData = buf

  def restart(newBuff: Array[Byte]) {
    buf = newBuff
    pos = 0
    length = buf.length
  }

  def restart {
    pos = 0
    length = buf.length
  }

  def restart(sequence: ByteSequence) {
    buf = sequence.getData
    pos = sequence.getOffset
    length = sequence.getLength
  }

  def restart(size: Int) {
    if (buf == null || buf.length < size) buf = new Array[Byte](size)
    restart(buf)
    length = size
  }

  def quickSkip(n: Int) {
    pos += n
  }

  def read = {
    if (pos < length) {
      val result = buf(pos) & 0xff
      pos += 1
      result
    } else {
      -1
    }
  }

  /**
   * Reads up to <code>len</code> bytes of data into an array of bytes from
   * this input stream.
   *
   * @param b the buffer into which the data is read.
   * @param off the start offset of the data.
   * @param len the maximum number of bytes read.
   * @return the total number of bytes read into the buffer, or
   *         <code>-1</code> if there is no more data because the end of the
   *         stream has been reached.
   */
  override def read(b: Array[Byte], off: Int, len: Int):Int = {
    if (b == null) throw new NullPointerException

    if (pos >= length) return -1
    if (len <= 0) return 0
    var newLen: Int = 0
    if (pos + len > length) {
      newLen = length - pos
    } else {
      newLen = len
    }
    System.arraycopy(buf, pos, b, off, newLen)
    pos += newLen
    newLen
  }

  override def available = length - pos

  def readFully(b: Array[Byte]) = read(b, 0, b.length)

  def readFully(b: Array[Byte], off: Int, len: Int) = read(b, off, len)

  def skipBytes(n:Int):Int = {
    if (n < 0) return 0
    var newN = 0
    if (pos + n > length) {
      newN = length - pos
    }else {
      newN = n
    }
    pos += newN
    newN
  }

  def readBoolean = read() != 0

  def readByte = read().asInstanceOf[Byte]

  def readUnsignedByte = read()

  def readShort = {
    read(work, 0, 2)
    (((work(0) & 0xff) << 8) | (work(1) & 0xff)).asInstanceOf[Short]
  }

  def readUnsignedShort = {
    read(work, 0, 2)
    (((work(0) & 0xff) << 8) | (work(1) & 0xff))
  }

  def readChar = {
    read(work, 0, 2)
    (((work(0) & 0xff) << 8) | (work(1) & 0xff)).asInstanceOf[Char]
  }

  def readInt = {
    read(work, 0, 4)
    ((work(0) & 0xff) << 24) | ((work(1) & 0xff) << 16) | ((work(2) & 0xff) << 8) | (work(3) & 0xff)
  }

  def readLong = {
    read(work, 0, 8)
    val i1: Int = ((work(0) & 0xff) << 24) | ((work(1) & 0xff) << 16) | ((work(2) & 0xff) << 8) | (work(3) & 0xff)
    val i2: Int = ((work(4) & 0xff) << 24) | ((work(5) & 0xff) << 16) | ((work(6) & 0xff) << 8) | (work(7) & 0xff)
    ((i1 & 0xffffffffL) << 32) | (i2 & 0xffffffffL)
  }

  def readFloat = java.lang.Float.intBitsToFloat(readInt)


  def readDouble = java.lang.Double.longBitsToDouble(readLong)

  def readLine = {
    val start = pos
    var needBreak = false
    while(pos < length || needBreak){
      var c = read
      if (c == '\n') {
        needBreak = true
      }else if (c == '\r') {
        c = read
        if (c != '\n' && c != -1) pos -= 1
        needBreak = true
      }
    }
    new String(buf, start, pos)
  }

  def readUTF = {
    throw new RuntimeException("还没实现呢!")
  }

  def getPos = pos
  def setPos(pos:Int){this.pos = pos}
  def getLength = length
  def setLength(length:Int) {this.length = length}
}
