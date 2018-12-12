package jb.djilink.mavlink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.os.HandlerThread


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