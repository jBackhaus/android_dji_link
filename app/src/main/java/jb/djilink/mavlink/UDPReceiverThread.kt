package jb.djilink.mavlink

import android.os.Message
import java.io.IOException
import java.net.*

import jb.djilink.*

/**
 * Created by jan on 01.11.17.
 */
class UDPReceiverThread(var handler: UDPMessageHandler, var parent: MavLinkConnection, var port: Int) : Thread("UDPReceiverThread") {
    //Lesen des Eingangs auf dem UDP-Socket in einen Buffer. Bei Empfang, Übergabe per Intent an UDPMessageThread zur Interpretation
    override fun run() {
        super.run()
        try {
            sendMessage(STATUS_UPDATE,"Start listening on Port " + port.toString())
            while (!this.isInterrupted) {
                var buffer = kotlin.ByteArray(BYTE_ARRAY_LENGTH)
                var packet = DatagramPacket(buffer, buffer.size)
                parent.udpSocket.receive(packet)
                if (!this.isInterrupted){
                    sendMessage(NEW_MSG, packet.data)
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



fun sendMessage (type: Int, string: String){
    handler.sendMessage(Message.obtain(handler, type, string))
}

fun sendMessage(type: Int, content: ByteArray){
    handler.sendMessage(Message.obtain(handler, type, content))
}

}