package org.dolphin.broker.store

import java.io.{UTFDataFormatException, DataOutput, OutputStream}

/**
 * User: bigbully
 * Date: 14-5-18
 * Time: 下午12:52
 */
final class DataByteArrayOutputStream(val size:Int) extends OutputStream with DataOutput{
  assert(size >= 0, "Invalid size: " + size)
  private val DEFAULT_SIZE: Int = 2048
  private var buf = new Array[Byte](size)
  private var pos = 0

  def this() {
    this(DEFAULT_SIZE)
  }

  /**
   * start using a fresh byte array
   *
   * @param size
   */
  def restart(size: Int) {
    buf = new Array[Byte](size)
    pos = 0
  }

  /**
   * start using a fresh byte array
   */
  def restart {
    restart(DEFAULT_SIZE)
  }

  /**
   * Get a ByteSequence from the stream
   *
   * @return the byte sequence
   */
  def toByteSequence: ByteSequence = {
    return new ByteSequence(buf, 0, pos)
  }

  /**
   * Writes the specified byte to this byte array output stream.
   *
   * @param b the byte to be written.
   */
  def write(b: Int) {
    val newcount: Int = pos + 1
    ensureEnoughBuffer(newcount)
    buf(pos) = b.asInstanceOf[Byte]
    pos = newcount
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at
   * offset <code>off</code> to this byte array output stream.
   *
   * @param b the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   */
  def write(b: Byte, off: Int, len: Int) {
    if (len == 0) {
      return
    }
    val newcount: Int = pos + len
    ensureEnoughBuffer(newcount)
    System.arraycopy(b, off, buf, pos, len)
    pos = newcount
  }

  /**
   * @return the underlying byte[] buffer
   */
  def getData: Array[Byte] = {
    return buf
  }

  /**
   * reset the output stream
   */
  def reset {
    pos = 0
  }

  /**
   * Set the current position for writing
   *
   * @param offset
   */
  def position(offset: Int) {
    ensureEnoughBuffer(offset)
    pos = offset
  }

  def getSize = pos

  def writeBoolean(v: Boolean) {
    ensureEnoughBuffer(pos + 1)
    buf(pos) = (if (v) 1 else 0).toByte
    pos += 1
  }

  def writeByte(v: Int) {
    ensureEnoughBuffer(pos + 1)
    buf(pos) = (v >>> 0).toByte
    pos += 1
  }

  def writeShort(v: Int) {
    ensureEnoughBuffer(pos + 2)
    buf(pos) = (v >>> 8).toByte;
    pos += 1
    buf(pos) = (v >>> 0).toByte;
    pos += 1
  }

  def writeChar(v: Int) {
    ensureEnoughBuffer(pos + 2)
    buf(pos) = (v >>> 8).toByte;
    pos += 1
    buf(pos) = (v >>> 0).toByte;
    pos += 1
  }

  def writeInt(v: Int) {
    ensureEnoughBuffer(pos + 4)
    buf(pos) = (v >>> 24).toByte;
    pos += 1
    buf(pos) = (v >>> 16).toByte;
    pos += 1
    buf(pos) = (v >>> 8).toByte;
    pos += 1
    buf(pos) = (v >>> 0).toByte;
    pos += 1
  }

  def writeLong(v: Long) {
    ensureEnoughBuffer(pos + 8)
    buf(pos) = (v >>> 56).toByte;
    pos += 1
    buf(pos) = (v >>> 48).toByte;
    pos += 1
    buf(pos) = (v >>> 40).toByte;
    pos += 1
    buf(pos) = (v >>> 32).toByte;
    pos += 1
    buf(pos) = (v >>> 24).toByte;
    pos += 1
    buf(pos) = (v >>> 16).toByte;
    pos += 1
    buf(pos) = (v >>> 8).toByte;
    pos += 1
    buf(pos) = (v >>> 0).toByte;
    pos += 1
  }

  def writeFloat(v: Float) {
    writeInt(java.lang.Float.floatToIntBits(v))
  }

  def writeDouble(v: Double) {
    writeLong(java.lang.Double.doubleToLongBits(v))
  }

  def writeBytes(s: String) {
    val length: Int = s.length
    for(i <- 0 until length){
      write(s.charAt(i).asInstanceOf[Byte])
    }
  }

  def writeChars(s: String) {
    val length: Int = s.length
    for(i <- 0 until length){
      val c: Int = s.charAt(i)
      write((c >>> 8) & 0xFF)
      write((c >>> 0) & 0xFF)
    }
  }

  def writeUTF(str: String) {
    val strlen: Int = str.length
    var encodedsize: Int = 0
    var c: Int = 0
    for (i <- 0 until strlen; c = str.charAt(i)){
      if ((c >= 0x0001) && (c <= 0x007F)) {
        encodedsize += 1
      }else if (c > 0x07FF) {
        encodedsize += 3
      }else {
        encodedsize += 2
      }
    }
    if (encodedsize > 65535) {
      throw new UTFDataFormatException("encoded string too long: " + encodedsize + " bytes")
    }
    ensureEnoughBuffer(pos + encodedsize + 2)
    writeShort(encodedsize)
    var j = 0
    for (i <- 0 until strlen; c = str.charAt(i);if (c >= 0x0001 && c <= 0x007F)){
      buf(pos) = c
      pos += 1
      j += 1
    }
    for(i <- j until strlen; c = str.charAt(i)){
      if ((c >= 0x0001) && (c <= 0x007F)) {
        buf(pos) = c.toByte;
        pos += 1
      } else if (c > 0x07FF) {
        buf(pos)= (0xE0 | ((c >> 12) & 0x0F)).toByte;
        pos += 1
        buf(pos)= (0x80 | ((c >> 6) & 0x3F)).toByte;
        pos += 1
        buf(pos) = (0x80 | ((c >> 0) & 0x3F)).toByte;
        pos += 1
      } else {
        buf(pos) = (0xC0 | ((c >> 6) & 0x1F)).toByte;
        pos += 1
        buf(pos) = (0x80 | ((c >> 0) & 0x3F)).toByte;
        pos += 1
      }
    }
  }

  private def ensureEnoughBuffer(newcount: Int) {
    if (newcount > buf.length) {
      val newbuf = new Array[Byte](Math.max(buf.length << 1, newcount))
      System.arraycopy(buf, 0, newbuf, 0, pos)
      buf = newbuf
    }
  }
  def skip(size:Int) {
    ensureEnoughBuffer(pos + size)
    pos+=size
  }
}
