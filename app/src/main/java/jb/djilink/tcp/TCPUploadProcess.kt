package jb.djilink.tcp

import android.os.Handler
import android.util.Log
import android.view.View
import jb.djilink.TCP_BUFFER_SIZE
import jb.djilink.TCP_IS_CONNECTED
import jb.djilink.mavlink.TCPConnection
import jb.djilink.saveImageToFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.System.currentTimeMillis
import java.nio.Buffer
import java.nio.ByteBuffer

class TCPUploadProcess(var tcpConnection: TCPConnection) {

    var main = tcpConnection.main

    lateinit var handler: Handler //Handler des TCPThreads, wird hier verknüpft, sobald er initialisiert ist.

    var file: File? = null
    var filestream: FileInputStream? = null
    var max = 0
    var filesize = 0
    var filename = ""
    var startTime = 0L
    var endTime = 0L
    var success = 1
    var currentBytes = 0




    var inProgress = false
    var current = 0

    fun initWithNewFile(newFile: File){
        current = 0
        file = newFile
        filestream= FileInputStream(file!!)
        currentBytes = 0
        startTime = currentTimeMillis()
        inProgress = true
        max = newFile.length().div(TCP_BUFFER_SIZE).toInt()
        filename = newFile.name
        filesize = newFile.length().toInt()



        enableTCPgui()
        sendFilename()
    }

    fun sendFilename(){
        if (filestream != null && TCP_IS_CONNECTED && tcpConnection.tcpOutputStream != null && tcpConnection.tcpInputstream != null){
            tcpConnection.tcpOutputStream!!.write(intToBytes(filename.length))
            tcpConnection.tcpOutputStream!!.flush()

            tcpConnection.tcpOutputStream!!.write(filename.toByteArray(Charsets.UTF_8))
            tcpConnection.tcpOutputStream!!.flush()
            var buffer = ByteArray(filename.length)
            tcpConnection.tcpInputstream!!.read(buffer)
            if (buffer.toString(Charsets.UTF_8) == filename){
                sendFilesize()
            }else{
                //Returned Name is wrong
                main.debug("Image may be sent with wrong filename!")
                sendFilesize()
            }
        }

    }

    fun readFinish(){

        if (TCP_IS_CONNECTED && tcpConnection.tcpOutputStream != null) {

            var buffer = ByteArray(4)
            tcpConnection.tcpInputstream!!.read(buffer)
            var retsize = bytesToInt(buffer)

            if (retsize == filesize) {
                main.debug("Sending Image was successful.")
                isFinished()
            } else {
                //Returned Free-Size is wrong
                main.debug("Sending Image threw an error.")
                isFinished()
            }
        } else{
            abort()
        }
    }

    fun sendFilesize(){
        if (filestream != null && TCP_IS_CONNECTED && tcpConnection.tcpOutputStream != null && tcpConnection.tcpInputstream != null){
            tcpConnection.tcpOutputStream!!.write(intToBytes(filesize))
            tcpConnection.tcpOutputStream!!.flush()
            var buffer = ByteArray(4)
            tcpConnection.tcpInputstream!!.read(buffer)
            var retsize = bytesToInt(buffer)

            if (retsize == filesize){
                sendNextStep()
            }else{
                //Returned Name is wrong
                main.debug("Wrong filesize returned. Try sending file nevertheless...")
                sendNextStep()
            }
        }
    }

    fun sendNextStep(){
        if (filestream != null) {
            var bufSize = TCP_BUFFER_SIZE
            if ((filesize - currentBytes)< TCP_BUFFER_SIZE) {
                bufSize = (filesize - currentBytes)
            }
            var buffer = ByteArray(bufSize)
            currentBytes += bufSize
            var read = filestream!!.read(buffer, 0, bufSize)
            current += 1
            if (read != -1 && bufSize > 0 ) {
                handler.post {
                    if (TCP_IS_CONNECTED && tcpConnection.tcpOutputStream != null) {
                        tcpConnection.tcpOutputStream!!.write(buffer)
                        tcpConnection.tcpOutputStream!!.flush()


                        updateTCPgui(current,max)
                        handler.post { sendNextStep() }
                    }else{
                        abort()
                    }
                }
            } else {
                handler.post {
                    if (TCP_IS_CONNECTED && tcpConnection.tcpOutputStream != null) {
                        tcpConnection.tcpOutputStream!!.flush()
                    }else {
                        abort()
                    }
                    readFinish()
                }
            }
        }

    }



    fun isFinished(){
        disableTCPgui()
        endTime = currentTimeMillis()
        main.uplogger.writeLine(filename,filesize.toLong(),startTime ,endTime,success)
        filename = ""
        filesize = 0
        startTime = 0L
        endTime = 0L
        success = 1

        file = null
        filestream = null
        max = 0
        current = 0
        inProgress = false
        tcpConnection.trigger()


    }

    fun abort(){
        Log.e("TCPUploadProcess", "Aborted: TCP-Connection lost")
        main.showToast("Aborted: TCP-Connection lost")
        success = 0
        isFinished()
    }

    fun updateTCPgui(current: Int, max: Int){
        main.runOnUiThread {
            main.gui.smbProgressBar.max = max
            main.gui.smbProgressBar.progress = current
            main.gui.smbRateView.text = ((current * TCP_BUFFER_SIZE / 1000000).toString() + " / " + (max * TCP_BUFFER_SIZE / 1000000).toString() + " MB")

        }
    }

    fun enableTCPgui(){
        main.runOnUiThread {
            main.gui.smbProgressBar.visibility = View.VISIBLE
            main.gui.smbRateView.visibility = View.VISIBLE
        }
    }

    fun disableTCPgui(){
        main.runOnUiThread {
            main.gui.smbProgressBar.visibility = View.INVISIBLE
            main.gui.smbRateView.visibility = View.INVISIBLE
        }
    }

    fun intToBytes(x: Int): ByteArray{
        val buffer = ByteBuffer.allocate(4)
        buffer.putInt(x)
        main.debug(x.toString()+" -> "+buffer.array())
        return buffer.array()
    }

    fun longToBytes(x: Long): ByteArray {
        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(x)
        return buffer.array()
    }

    fun bytesToLong(bytes: ByteArray): Long {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.put(bytes)
        buffer.flip()//need flip
        return buffer.long
    }


    fun bytesToInt(bytes: ByteArray): Int {
        val buffer = ByteBuffer.allocate(4)
        buffer.put(bytes)
        buffer.flip()//need flip
        return buffer.int
    }
}