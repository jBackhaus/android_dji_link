package jb.djilink.mavlink

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.MAVLink.MAVLinkPacket
import com.MAVLink.Messages.MAVLinkMessage
import jb.djilink.*

import java.net.*

/**
 * Created by jan on 01.11.17.
 */
class UDPSenderThread(private var udpMessageHandler: UDPMessageHandler, val parent: MavLinkConnection): HandlerThread("UDPSenderThread") {
        //Thread zum Versenden von MAVLink-Nachrichten

    lateinit private var handler: Handler



    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
    }


    fun stopSendingMessages() {         //Nachrichtenversand einstellen
        handler.removeCallbacksAndMessages(null)
    }


    private fun sendMessage (type: Int){        //Intent, das Nachricht gesendet wurde
        udpMessageHandler.sendMessage(Message.obtain(udpMessageHandler, type))
    }




    private fun composePacket (mavLinkMessage: MAVLinkMessage): MAVLinkPacket {     //MAVLink-Packet aus Nachricht erzeugen, Versand veranlassen
        val mavLinkPacket = mavLinkMessage.pack()
        mavLinkPacket.sysid = SYSID
        mavLinkPacket.compid = COMPID
        return mavLinkPacket
    }

    fun sendMavLinkMessage(msg: MAVLinkMessage, address: InetAddress, port: Int){       //MAVLink-Nachricht packen lassen und versenden
        handler.post {

            val mavLinkPacket = composePacket(msg)
            val buffer = mavLinkPacket.encodePacket()
            if (parent.udpRunning) {
                parent.udpSocket.send(DatagramPacket(buffer, buffer.size, address, port))
                sendMessage(MESSAGE_SENT)
            } else {
                //TODO: showToast("Senden unmöglich, Sender-Thread läuft nicht")
            }
        }

    }



}