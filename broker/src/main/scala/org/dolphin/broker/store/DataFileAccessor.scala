package org.dolphin.broker.store

/**
 * User: bigbully
 * Date: 14-5-16
 * Time: 下午11:11
 */
class DataFileAccessor(val dataFile:DataFile) {

  private val file = dataFile.openRandomAccessFile

  def dispose {

  }

  def readFully(offset:Long, data:Array[Byte]) {
    file.seek(offset)
    file.readFully(data)
  }

  def read(offset:Long, data:Array[Byte]) = {
    file.seek(offset)
    file.read(data)
  }

}
