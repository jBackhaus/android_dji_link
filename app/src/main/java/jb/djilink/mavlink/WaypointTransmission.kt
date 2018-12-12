package jb.djilink.mavlink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import com.MAVLink.enums.MAV_MISSION_RESULT
import android.os.Handler
import android.os.HandlerThread
import jb.djilink.*
import kotlinx.android.synthetic.main.layout.*


class WaypointTransmission(private val targetSystem: Short, private val targetComponent: Short, private val numberOfWaypoints: Int, private val mavLink: MavLinkConnection, private val receive: Boolean): HandlerThread("WaypointTransmission") {

    //numberOfWaypoints ist nur bei "Receive" nötig, da hier die Anzahl der abzufordernden Wegpunkte von der GCS mitgeteilt wurde.
    //Im Fall "Send" wird mit "count" die Anzahl der Wegpunkte aus der aktuell geladenen Mission extrahiert, sie muss nicht übergeben werden. Dies erleichtert die Einbindung
    //Die lokale Laufvariable des aktuell übertragenen Wegpunkts ist "current"

    private var count = 0

    private var current = 0

    private var mavlinkWaypointList: MutableList<MavlinkWaypoint> = arrayListOf() //TODO: Ersetze MavlinkWaypoint-Class durch DJI-MavlinkWaypoint

    lateinit private var handler: Handler
    private var tries = 0

    private var main = mavLink.main


    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)

        if (receive) {
            startGettingWaypoints()
        } else {
            startSendingWaypoints()
        }
    }


    //Send Waypoints


    fun startSendingWaypoints(){
        mavlinkWaypointList = mavLink.mission
        count = mavlinkWaypointList.size
        current = -1
        mavLink.mavlinkMessageConstructor.missionCount(targetSystem, targetComponent, count)
        handler.postDelayed({
            if (tries < 5) {
                tries++
                startSendingWaypoints()
            } else {
                finishSendTransmission(false)
            }

        }, TIME_BETWEEN_RETRIES)
    }


    fun sendNextWaypoint(seq: Int){
        if (seq == current + 1){                                    //wenn nächster Wegpunkt gefragt wird
            handler.removeCallbacksAndMessages(null)          //alte Sendeversuche einstellen
            tries = 0                                               //Versuchszähler zurücksetzen
            current = seq                                           //Aktueller Wegpunkt erhöhen
            sendWaypoint(seq)                                       //gefragten Wegpunkt senden
        } else {
            //wenn seq == current( wird sowieso gerade 5x gesendet)
            //wenn anders( ist ganz falsch, dann wird der Thread irgendwann automatisch beendet)
        }

    }


    fun sendWaypoint(current: Int) {
        if (current < mavlinkWaypointList.size) {
            mavLink.mavlinkMessageConstructor.missionItem(targetSystem, targetComponent, mavlinkWaypointList[current])
            handler.postDelayed(Runnable {
                if (tries < 5) {
                    tries++
                    sendWaypoint(current)
                } else {
                    finishSendTransmission(false)
                }

            }, TIME_BETWEEN_RETRIES)
        } else {
            //Todo: Fehlermeldung Wegpunkt nicht vorhanden
            finishSendTransmission(false)
        }
    }






    fun finishSendTransmission(result: Boolean) {
        if (result) {
            main.debug("Wegpunktliste wurde erfolgreich gesendet.")
        } else {
            main.debug("Wegpunktliste wurde nicht gesendet.")
        }
        handler.removeCallbacksAndMessages(null)
        mavLink.waypointTransmission = null
        //TODO: Nachricht an Parent, dass Übertragung beendet ist
        this.quit()

    }




//Receive Waypoints


    fun startGettingWaypoints(){
        //TODO: Nachricht an Parent, dass Übertragung begonnen wird
        current = 0
        getWaypoint()
    }


    fun getWaypoint() {
        mavLink.mavlinkMessageConstructor.missionRequest(targetSystem, targetComponent, current)
        handler.postDelayed(Runnable {
            if (tries < 5) {
                tries++
                getWaypoint()
            } else {
                finishReceiveTransmission(false)
            }

        }, TIME_BETWEEN_RETRIES)

    }



    fun addWaypoint(mavlinkWaypoint: MavlinkWaypoint){
        if (mavlinkWaypoint.seq == current) {
            handler.removeCallbacksAndMessages(null)
            tries = 0
            mavlinkWaypointList.add(mavlinkWaypoint)
            current++
            if (current < numberOfWaypoints) {
                getWaypoint()
            } else {
                finishReceiveTransmission(true)
            }
        }
    }


    fun missionAck(result: Int) {
        mavLink.mavlinkMessageConstructor.missionAck(targetSystem, targetComponent, result.toShort())
    }

    fun finishReceiveTransmission(successful: Boolean) {
        if (successful) {
            missionAck(MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED)
            mavLink.didReceiveNewMission(mavlinkWaypointList)
            main.debug("Wegpunktliste wurde erfolgreich empfangen.")

        } else {
            main.debug("Fehler beim Empfangen der Wegpunktliste.")
            missionAck(MAV_MISSION_RESULT.MAV_MISSION_ERROR)
        }
        handler.removeCallbacksAndMessages(null)
        mavLink.waypointTransmission = null
        main.djiController.mission.buildMissionFromMavlinkMission(mavLink.mission)

        this.quit()
        //TODO: Nachricht an Parent, dass Übertragung beendet ist
    }

}