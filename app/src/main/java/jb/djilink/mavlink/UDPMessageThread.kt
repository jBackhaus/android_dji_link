package jb.djilink.mavlink

import android.os.HandlerThread

/**
 * Created by jan on 30.11.17.
 */
class UDPMessageThread (var mavLink: MavLinkConnection): HandlerThread("UDPMessageThread") {

    lateinit var udpMessageHandler: UDPMessageHandler

    override fun run() {
        super.run()

    }

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        udpMessageHandler = UDPMessageHandler(mavLink, looper)
        mavLink.udpMessageHandler = udpMessageHandler               //Alle Funktionen im Handler definiert!!

        mavLink.initUdpSenderThread()
    }
}