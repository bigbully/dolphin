package org.dolphin.broker.store

import java.io.{RandomAccessFile, File}

/**
 * User: bigbully
 * Date: 14-5-12
 * Time: 下午11:09
 */
class DataFile(val id:Int, private[this] val file:File) extends Ordered[DataFile]{

  private var length = if (file.exists()) file.length() else 0
  var corruptedBlocks = List.empty[(Int, Int)]

  //倒叙排列（..,3,2,1）取head更方便
  override def compare(that: DataFile): Int = that.id - this.id

  def openRandomAccessFile = {
    this.synchronized(new RandomAccessFile(file, "rw"))
  }

  def closeRandomAccessFile(file:RandomAccessFile) {
    this.synchronized(file.close())
  }

  def addCorruptedBlocks(sequence:(Int, Int)) {
    corruptedBlocks ::= sequence
  }

  def isCorrupted = !corruptedBlocks.isEmpty

  def setLength(length:Int) {this.length = length}

  def getLength = this.synchronized(length.asInstanceOf[Int])

  def incrementLength(size:Int) {
    this.synchronized(length += size)
  }

}
