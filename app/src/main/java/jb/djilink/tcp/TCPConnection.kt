package jb.djilink.mavlink

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import jb.djilink.MainActivity
import jb.djilink.TCP_IS_CONNECTED
import jb.djilink.tcp.TCPUploadProcess
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket

/**
 * Created by jan on 01.11.17.
 */
class TCPConnection(val main: MainActivity): HandlerThread("TCPConnection") {
    //Thread zum Versenden von Bilddateien über TCP

    var imageList: MutableList<String> = arrayListOf()
    var uploadProcess = TCPUploadProcess(this)

    var address: InetAddress = InetAddress.getByName("0.0.0.0")
    var port = 14551

    var tcpSocket: Socket? = null
    var tcpOutputStream: OutputStream? = null
    var tcpInputstream: InputStream? = null


    lateinit var handler: Handler



    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
        uploadProcess.handler = handler
    }


    fun connect(ip: String, port: Int){
        handler.post {
            Log.e("TCP", "connect")

            this.port = port
            address = InetAddress.getByName(ip)
            try {
                tcpSocket!!.close()
            } catch (e: Exception) {
            }
            try {
                tcpSocket = Socket(address, port)
                main.debug("socket init")
                main.showToast("socket init")
                tcpOutputStream = tcpSocket!!.getOutputStream()
                tcpInputstream = tcpSocket!!.getInputStream()
                main.debug("OutputStream init")
                main.showToast("OutputStream init")
                TCP_IS_CONNECTED = true

            } catch (e: Exception) {
                main.debug("Could not open TCP-Socket: " + e.localizedMessage)
                Log.e("TCP-Socket", "Could not open TCP-Socket: " + e.localizedMessage)
                e.printStackTrace()
                TCP_IS_CONNECTED = false
                tcpInputstream = null
                tcpOutputStream = null

                main.sendBroadcast(Intent("TCPERROR"))
                main.showToast("TCP konnte nicht verbunden werden!")
            }
        }
    }



    fun disconnect(){

        stopSendingMessages()
        TCP_IS_CONNECTED=false
        tcpInputstream = null
        tcpOutputStream = null

        Log.e("TCP","disconnect" )
        handler.post {
            try {
                tcpSocket!!.close()
            }catch (e:Exception){}

        }



    }

    fun stopSendingMessages() {         //Nachrichtenversand einstellen
        handler.removeCallbacksAndMessages(null)
        if (uploadProcess.inProgress) {
            uploadProcess.abort()
        }
    }


    fun addImageToList(filename: String){
        imageList.add(filename)
        trigger()
    }


    private fun nextImage() {
        if (!imageList.isEmpty()) {
            handler.post {
                val filename = imageList.first()
                imageList.removeAt(0)
                updateTCPPendingGui()

                var imageFile = main.fileAccessor.getFile(filename)
                if (imageFile != null) {
                    uploadProcess.initWithNewFile(imageFile)
                }
            }
        }
    }


    fun trigger(){
        updateTCPPendingGui()
        if (!uploadProcess.inProgress && !imageList.isEmpty()){
            uploadProcess.inProgress = true
            handler.post {
                nextImage()
            }
        }
    }

    fun updateTCPPendingGui(){
        main.runOnUiThread {
            main.gui.smbPendingText.text = imageList.size.toString()
        }
    }

    override fun quitSafely(): Boolean {
        disconnect()
        return super.quitSafely()
    }





}