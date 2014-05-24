package org.dolphin.broker.store

import java.io.IOException
import scala.Option

/**
 * User: bigbully
 * Date: 14-5-12
 * Time: 下午10:46
 */
class DataFileAccessorPool {

  private var pools = Map.empty[Int, Pool]
  private var closed:Boolean = _
  val maxOpenReadersPerFile = 5

  class Pool(private val file:DataFile) {
    private var pool = List.empty[DataFileAccessor]
    private var used:Boolean = _
    private var openCounter:Int = _
    private var disposed:Boolean = _

    def openDataFileReader = {
      val accessor = pool match {
        case Nil => new DataFileAccessor(file)
        case _ => {
          pool = pool.tail
          pool.head
        }
      }
      used = true
      openCounter += 1
      accessor
    }

    def closeDataFileReader(reader:DataFileAccessor){
      this.synchronized({
        openCounter -= 1
        if (pool.size >= maxOpenReadersPerFile || disposed){
          reader.dispose
        } else {
          pool ::= reader
        }
      })
    }

  }

  def openDataFileAccessor(file:DataFile) = {
    if (closed) throw new IOException("Closed.")
    val pool = pools.getOrElse(file.id, {
      val pool = new Pool(file)
      pools += (file.id -> pool)
      pool
    })
    pool.openDataFileReader
  }

  def closeDataFileAccessor(reader:DataFileAccessor) {
    val pool = pools.get(reader.dataFile.id)
    pool match {
      case Nil => reader.dispose
      case Some(_) if (closed) => reader.dispose
      case Some(p) => p.closeDataFileReader(reader)
    }
  }

}
