package org.dolphin.broker.mail

import org.dolphin.broker.store.DataFile

/**
 * User: bigbully
 * Date: 14-6-9
 * Time: 下午11:23
 */
case class ReturnDataFile(dataFile:DataFile, readOffset:Long) {

}
