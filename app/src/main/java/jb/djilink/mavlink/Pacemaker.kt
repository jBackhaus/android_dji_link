package jb.djilink.mavlink

import android.os.Handler
import android.os.HandlerThread
import jb.djilink.*

/**
 * Created by jan on 07.11.17.
 */
class Pacemaker(var mavLink: MavLinkConnection): HandlerThread("Pacemaker"){

    var heartbeatRunning = false                   //Variable, die angibt, ob der sekündliche Aufruf gestoppt werden soll. 2. Sicherheitsebene, da asynchrone Zugriffe nicht ausgeschlossen werden können.
    var main = mavLink.main
    lateinit var handler: Handler


    override fun onLooperPrepared() {       //Wenn der Looper initialisiert wurde, wird der Handler zur Aufgabenverwaltung erstellt.
        super.onLooperPrepared()
        handler = Handler(looper)
    }

    private fun pace() {                            //Funktion, die, wenn einmal aufgerufen, sich sekündlich wieder startet

        if (heartbeatRunning) {                  //2. Sicherheitsebene, falls durch Asynchronitäten der Pacer schon nicht mehr senden soll
            sendHeartbeat()
        }
        if (!this.isInterrupted && heartbeatRunning) {       //Abbruchkriterien: Beendeter Thread /
            handler.postDelayed({ pace() }, TIME_BETWEEN_HEARTBEATS)
        }
    }

    fun startPace () {              //startet die sekündliche Ausführung. Die Positions-Übertragung muss manuell gestartet werden (sobald Position verfügbar)
        heartbeatRunning = true

        handler.removeCallbacksAndMessages(null)  //Looper wird geleert, damit der Pacer nicht doppelt läuft.

        if (!this.isInterrupted) {
            main.debug("Heartbeat wird gestartet.")
            handler.post{ pace() }      //Pacer wird zum ersten Mal in den Looper eingestellt. Er ruft sich dann sekündlich neu auf.
        }
    }


    fun stopPace () {               //stoppt den sekündlichen Aufruf
        heartbeatRunning = false           //2. Sicherheitsebene
        handler.removeCallbacksAndMessages(null)        //Looper wird geleert, alle geplanten Ausführungen des Pacers werden entfernt.
    }

    private fun sendHeartbeat() {

        mavLink.mavlinkMessageConstructor.heartbeat(Type.toShort(), AUTOPILOT.toShort(), BASEMODE.toShort(), CUSTOMMODE.toLong(), SYSTEMSTATUS.toShort(), MAVLINK_VERSION.toShort()) //Erzeugen der MavLink-Message

        if (!mavLink.connected){     //Beim ersten Senden des Heartbeats (Neuverbindung) wird die Versionsnummer und die Fähigkeiten des Autopiloten mit übertragen.
            mavLink.connected = true         //MainActivity wird mitgeteilt, dass eine Verbindung zur GCS aufgenommen wurde. //TODO: Erst bei Erhalt einer Antwort ausführen
            mavLink.sendVersion()
        }
    }




}