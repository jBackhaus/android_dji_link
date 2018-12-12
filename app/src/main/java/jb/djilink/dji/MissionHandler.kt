package jb.djilink.dji
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.util.Log
import android.view.View
import com.MAVLink.enums.MAV_CMD
import dji.common.error.DJIError
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.mission.waypoint.*
import dji.sdk.mission.waypoint.WaypointMissionOperator
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener
import jb.djilink.*
import jb.djilink.mavlink.MavlinkWaypoint
import kotlinx.android.synthetic.main.layout.*



class MissionHandler(var djiController: DJIController) {
    var main = djiController.main
    var waypointList: MutableList<Waypoint>? = null
    var waypointMissionBuilder: WaypointMission.Builder? = null
    var waypointMissionOperator: WaypointMissionOperator? = null



    var missionOperatorListener= object : WaypointMissionOperatorListener {         //Listener für alle Drohnen-Aktionen bzgl. Missionen
            override fun onDownloadUpdate(p0: WaypointMissionDownloadEvent) {
            }

            override fun onUploadUpdate(p0: WaypointMissionUploadEvent) {
                Log.e(TAG, "Upload Update")
                    when (p0.currentState) {
                        WaypointMissionState.UPLOADING -> {                         //Wenn Uploadfortschritt der Mission, GUI aktualisieren
                            main.runOnUiThread {
                                main.gui.uploadBar.visibility = View.VISIBLE
                                main.gui.uploadText.visibility = View.VISIBLE
                                main.gui.uploadText.text = "Uploading"
                            }
                            if (p0.progress != null) {
                                if (p0.progress!!.isSummaryUploaded == false) {
                                    main.runOnUiThread {
                                        main.gui.uploadText.text = "no sum"
                                    }
                                } else {
                                    main.runOnUiThread {
                                        main.gui.uploadText.text = p0.progress!!.uploadedWaypointIndex.toString() + " / " + p0.progress!!.totalWaypointCount.toString()
                                    }
                                }
                            }
                        }
                        WaypointMissionState.NOT_SUPPORTED -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Not Supported"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.READY_TO_UPLOAD -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Ready to upload"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.READY_TO_EXECUTE -> {                  //Wenn bereit zur Ausführung, Status ändern und Anzeigen
                            main.runOnUiThread {
                                main.gui.uploadText.visibility = View.INVISIBLE
                                main.gui.uploadBar.visibility = View.INVISIBLE
                                isAuto(true)
                                isManualInput(true)

                                //Todo: State= Ready to Start mission on Command
                                main.debug("Mission bereit zum Start")
                            }
                        }
                        WaypointMissionState.EXECUTING -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Executing"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.EXECUTION_PAUSED -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Execution paused"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.DISCONNECTED -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Disconnected"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.RECOVERING -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Recovering"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }
                        WaypointMissionState.UNKNOWN -> {
                            main.runOnUiThread {
                                main.gui.uploadText.text = "Unknown"
                                main.gui.uploadBar.visibility = View.INVISIBLE
                            }
                        }

                    }

            }

            override fun onExecutionUpdate(event: WaypointMissionExecutionEvent) {      //Wenn Ausfürhungsfortschritt (noch zu integrierendes Update der GCS bzgl. aktueller Wegpunkt)
                if (event.progress!=null) {
                    var aimingWaypoint = event.progress!!.targetWaypointIndex
                    //Todo: neuen aktiven Wegpunkt über MAVLink senden, aber Vorsicht! index ist nicht identisch. Übersetzungstabelle muss erstell und gespeichert werden.
                }
            }

            override fun onExecutionStart() {
                main.runOnUiThread {
                    main.gui.BtnSettings.isActivated = false
                }

            }

            override fun onExecutionFinish(p0: DJIError?) {     //Wenn Ausführung beendet, Status und Anzeige aktualisieren
                Log.e(TAG, "onExecutionFinish")
                if (p0 != null) {
                    Log.e(TAG, p0.description)
                }
                isOnCustomWaypointmissionPX4(false)
                isArmed(false)

                main.runOnUiThread {
                    main.gui.BtnSettings.isActivated = true
                }

            }
    }




    init{

    }


    fun initMissionOperator(){
        Log.e(TAG, "initMissionOperator")

        waypointMissionOperator = djiController.sdkManager.missionControl.waypointMissionOperator
        waypointMissionOperator!!.addListener(missionOperatorListener)      //initialisiert obigen Listener
    }

    fun addWaypointToList(waypoint: Waypoint){          //DJI-Wegpunkt zu DJI-Mission hinzufügen (aufgerufen durch Umwandlungs-Funktion)
        if (waypointList == null){
            waypointList = mutableListOf(waypoint)
        } else {
            waypointList!!.add(waypoint)
        }
    }

    fun clearWaypointList() {                           //Wegpunktliste löschen
        if (waypointList != null) {
            waypointList!!.clear()
        }
    }




    fun buildMavlinkMissionFromDJIMission(djiMission: WaypointMission){
        //Todo
    }


    fun buildMissionFromMavlinkMission(mavlinkMission: MutableList<MavlinkWaypoint>){       //Umwandlungsfunktion von MAVLink-Mission zu DJI-Mission

        waypointMissionBuilder = WaypointMission.Builder()
                .autoFlightSpeed(SPEED)
                .maxFlightSpeed(MAXSPEED)
                .headingMode(WaypointMissionHeadingMode.USING_WAYPOINT_HEADING)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .finishedAction(mFinishedAction)
        clearWaypointList()

        var currentSpeed = SPEED
        var currentPhotoDistance = 0f
        var currentwaypoint: Waypoint? = null
        var nextwaypoint: Waypoint?
        var heading: Int? = null
        var headingSetsOnNextWaypoint = false
        var gimbalPitch = 0F
        for (mavlinkWaypoint in mavlinkMission.iterator()) {
            when (mavlinkWaypoint.command.toInt()){
                178 -> {    //Do_Change_Speed
                    currentSpeed = mavlinkWaypoint.param2
                    if (currentwaypoint != null) currentwaypoint.speed = currentSpeed
                }

                530 -> {    //Set_Camera_Mode
                    // CameraMode ist immer auf "TakePhoto" gesetzt. Andere Optionen sind nicht implementiert.
                }

                16 -> {     //Nav_Waypoint

                    nextwaypoint = Waypoint(mavlinkWaypoint.x.toDouble(), mavlinkWaypoint.y.toDouble(), mavlinkWaypoint.z)
                    if (currentwaypoint != null){
                        var distance = calculateDistance(
                                LocationCoordinate3D(currentwaypoint.coordinate.latitude, currentwaypoint.coordinate.longitude, currentwaypoint.altitude)
                                , LocationCoordinate3D(nextwaypoint.coordinate.latitude, nextwaypoint.coordinate.longitude, nextwaypoint.altitude))
                        if (distance > MINIMUM_DISTANCE_BETWEEN_WAYPOINTS) {     //doppelte oder überflüssige (weil zu nahe) Wegpunkte werden entfernt
                            if (headingSetsOnNextWaypoint) {    //Survey-Heading soll bestimmt werden, da es sich im die erste Bahn handelt
                                heading = calculateHeading(currentwaypoint.coordinate.latitude, currentwaypoint.coordinate.longitude,
                                        nextwaypoint.coordinate.latitude, nextwaypoint.coordinate.longitude)
                                headingSetsOnNextWaypoint = false
                                currentwaypoint.heading = heading      //Heading wird dem letzten Wegpunkt (an dem die Fotoaufzeichnung gestertet wird) zugewiesen.
                                Log.e("BuildMissionFromMavlink", "Survey-Heading = "+heading.toString())
                            }
                            waypointMissionBuilder!!.addWaypoint(currentwaypoint)
                            addWaypointToList(currentwaypoint)
                            currentwaypoint = nextwaypoint
                            currentwaypoint.speed = currentSpeed
                            currentwaypoint.gimbalPitch = gimbalPitch
                            if (heading != null){                       //Wenn Survey-Heading bestimmt wurde
                                currentwaypoint.heading = heading       //alle neuen Wegpunkte behalten dieses Heading bei
                            }
                            if (currentPhotoDistance > 0){
                                currentwaypoint.shootPhotoDistanceInterval = currentPhotoDistance
                            } else {
                                currentwaypoint.shootPhotoDistanceInterval = 0f
                            }
                        }
                    } else {
                        currentwaypoint = nextwaypoint
                        currentwaypoint.speed = currentSpeed
                        if (currentPhotoDistance > 0){
                            currentwaypoint.shootPhotoDistanceInterval = currentPhotoDistance
                        } else {
                            currentwaypoint.shootPhotoDistanceInterval = 0f
                        }
                    }
                }

                206 -> {    //Set Camera Trigger Distance
                    currentPhotoDistance = mavlinkWaypoint.param1
                    if (currentwaypoint != null){
                        currentwaypoint.shootPhotoDistanceInterval = currentPhotoDistance
                        DISTANCE_BETWEEN_PHOTOS = currentPhotoDistance
                    }
                    if (heading == null){   //Survey-Heading ist noch nicht bestimmt
                        headingSetsOnNextWaypoint = true   //Survey-Heading kann jetzt bestimmt werden
                        Log.e("buildMissionFromMavlink", "Heading muss bestimmt werden")
                    }
                }

                205 -> {    //DO_MOUNT_CONTROL (param1: GimbalPitch)
                    if (mavlinkWaypoint.param1 >= -90F && mavlinkWaypoint.param1 <=0F) {
                        gimbalPitch = mavlinkWaypoint.param1
                        if (currentwaypoint != null) currentwaypoint.gimbalPitch = gimbalPitch
                    }
                }

                else -> {
                    //ignore
                }
            }
        }
        if (currentwaypoint != null) {
            waypointMissionBuilder!!.addWaypoint(currentwaypoint)
            addWaypointToList(currentwaypoint)
        }
        if (waypointList!= null) main.gui.redrawMission(waypointList!!)
        loadMission()

    }

    fun loadMission(){                                  //Upload der Mission zur Drohne vorbereiten und veranlassen
        Log.e(TAG, "loadMission")

        if (waypointMissionOperator == null){
            initMissionOperator()
        }
        val missionBuild = waypointMissionBuilder!!.build()
        main.runOnUiThread{
            main.debug("isGimbalPitchRotationEnabled: "+missionBuild.isGimbalPitchRotationEnabled)
        }
        val error = waypointMissionOperator!!.loadMission(missionBuild)
        if (error == null){
            var stop_exec = false
            while (stop_exec == false) {
                displayCurrentState()
                when (waypointMissionOperator!!.currentState) {
                    WaypointMissionState.READY_TO_UPLOAD -> {           //Upload starten
                        stop_exec = true
                        uploadMission()
                    }

                    WaypointMissionState.DISCONNECTED -> {
                        stop_exec = true
                        main.showToast("Drohne nicht verbunden")
                    }

                    WaypointMissionState.NOT_SUPPORTED -> {
                        stop_exec = true
                        main.showToast("Mission-Upload wird nicht unterstützt")
                    }

                    WaypointMissionState.EXECUTING, WaypointMissionState.EXECUTION_PAUSED -> waypointMissionOperator!!.stopMission { djiError ->        //Wenn Mission läuft, alte abbrechen, dann neue hochladen
                        if (djiError != null) {
                            Log.e("Waypointmission stop", djiError.description)
                        } else {
                            stop_exec = true
                            uploadMission()
                        }
                    }

                    else -> {
                        main.showToast("Drone is busy, try again")
                    }
                }
            }

        } else {
            Log.e(TAG, "Error building Mission: "+ error.description)
        }
    }

    fun uploadMission() {               //Tatsächlicher Upload der Mission zur Drohne
        Log.e(TAG, "uploadMission")


            waypointMissionOperator!!.uploadMission { error ->
                if (error == null){
                    main.showToast("Missions-Upload gestarted")
                } else {
                    main.showToast("Mission-Upload konnte nicht gestartet werden: "+error.description)
                    Log.e(TAG, "Error uploading mission: "+ error.description)
                }
            }
    }

   fun startMission() {                             //Mission starten, auf MAVLink-Kommando ausgeführt
       Log.e(TAG, "startMission")

       displayCurrentState()
       when (waypointMissionOperator!!.currentState) {
           WaypointMissionState.READY_TO_EXECUTE -> {
               main.logger.newMission()
               main.uplogger.newMission()
               waypointMissionOperator!!.startMission { error ->
                   if (error == null) {
                       main.runOnUiThread {
                           main.gui.BtnSettings.isActivated = false
                       }
                       isArmed(true)
                       main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 0)

                       main.showToast("starting Mission")
                   } else {
                       main.showToast("Error starting Mission: " + error.description)
                       Log.e(TAG, "Error starting mission: " + error.description)
                       main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)

                   }
               }
           }
           WaypointMissionState.DISCONNECTED -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.NOT_SUPPORTED -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.READY_TO_UPLOAD -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.UPLOADING -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.EXECUTING -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.EXECUTION_PAUSED -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.RECOVERING -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
           WaypointMissionState.UNKNOWN -> {
               main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)
           }
       }

    }

    fun displayCurrentState() {                             //Debug
        when (waypointMissionOperator!!.currentState) {
            WaypointMissionState.EXECUTING -> main.showToast("EXECUTING")
            WaypointMissionState.EXECUTION_PAUSED -> main.showToast("EXECUTION_PAUSED")
            WaypointMissionState.NOT_SUPPORTED -> main.showToast("NOT_SUPPORTED")
            WaypointMissionState.DISCONNECTED -> main.showToast("DISCONNECTED")
            WaypointMissionState.READY_TO_UPLOAD -> main.showToast("READY_TO_UPLOAD")
            WaypointMissionState.READY_TO_EXECUTE -> main.showToast("READY_TO_EXECUTE")
            WaypointMissionState.RECOVERING -> main.showToast("RECOVERING")
            WaypointMissionState.UNKNOWN -> main.showToast("UNKNOWN")
            WaypointMissionState.UPLOADING -> main.showToast("UPLOADING")
            else -> main.showToast("Unidentified WaypointMissionState")
        }
    }

    companion object {
        private val TAG = MissionHandler::class.java.name
    }
}