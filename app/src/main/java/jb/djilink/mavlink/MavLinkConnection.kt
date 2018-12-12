package jb.djilink.mavlink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import jb.djilink.MAVLINK_IS_CONNECTED
import jb.djilink.MainActivity
import jb.djilink.udpIp
import jb.djilink.udpPort
import java.net.DatagramSocket
import java.net.InetAddress


class MavLinkConnection (var main: MainActivity){




    var mavlinkMessageConstructor = MavLinkMessageConstructor(this)
    var address: InetAddress = InetAddress.getByName("0.0.0.0")
    var port = 14550

    var udpSocket = DatagramSocket()

    var waypointTransmission : WaypointTransmission? = null


    var connected = false
    var udpRunning = false

    var mission: MutableList<MavlinkWaypoint> = arrayListOf()


    lateinit private var udpReceiverThread: UDPReceiverThread
    lateinit var udpSenderThread: UDPSenderThread

    var pacemaker= Pacemaker(this)

    private var udpMessageThread = UDPMessageThread(this)

    lateinit var udpMessageHandler: UDPMessageHandler


    init {
        udpMessageThread.start()
        pacemaker.start()
        mavlinkMessageConstructor.start()

        //udpSenderThread werden initialisiert durch udpMessageThread, nachdem dessen Handler erzeugt ist.
    }

    fun initUdpSenderThread(){      //wird aufgerufen von udpMessageThread, nachdem dessen Handler erzeugt ist
        udpSenderThread = UDPSenderThread(udpMessageHandler, this)
        udpSenderThread.start()
    }




    fun startUDPThreads(ip: String, port: Int) {        //MAVLink-Verbindung einrichten
        this.port = port
        address = InetAddress.getByName(ip)
        udpSocket.close()
        udpSocket = DatagramSocket(port)
        udpReceiverThread = UDPReceiverThread(udpMessageHandler, this, port)
        udpReceiverThread.start()

        MAVLINK_IS_CONNECTED = true
        udpRunning = true


    }




    fun stopUDPThreads() {                          //MAVLink-Verbindung trennen
        udpReceiverThread.interrupt()
        udpSenderThread.stopSendingMessages()
        udpSocket.close()
        pacemaker.stopPace()
        connected = false
        udpRunning = false

        MAVLINK_IS_CONNECTED = false

    }


    fun didReceiveNewMission(mission: MutableList<MavlinkWaypoint>){ //TODO: Ersetze MavlinkWaypoint-Class durch DJI-MavlinkWaypoint
        this.mission = mission
        //TODO: Veranlasse Übertragung an Drohne
    }

    //UDP Sendungen

    fun sendVersion(){

        if (udpRunning) {
            mavlinkMessageConstructor.autopilotVersion(mavProtocolCapability())
        }
    }

    fun sendStatus() {

        mavlinkMessageConstructor.sysStatus(mavSysStatusSensorPresent(), mavSysStatusSensorEnabled(), mavSysStatusSensorHealth(), mavSysStatusLoad(), mavSysStatusVoltage(), mavSysStatusCurrent(), mavSysStatusRemaining(), mavSysStatusDrops(), mavSysStatusErrors(), mavSysStatusError1(), mavSysStatusError2(), mavSysStatusError3(), mavSysStatusError4())
    }


    fun connect(){          //MAVLink-Verbindung soll starten

        startUDPThreads(udpIp, udpPort)
        pacemaker.startPace()                   //Heartbeat starten
    }

    fun disconnect(){       //MAVLink-Verbindung soll aufgelöst werden
        stopUDPThreads()
        pacemaker.stopPace()
    }





}