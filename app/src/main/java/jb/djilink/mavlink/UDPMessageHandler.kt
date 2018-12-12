package jb.djilink.mavlink

import android.os.Handler
import android.os.Message
import com.MAVLink.MAVLinkPacket
import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.Parser
import com.MAVLink.common.*
import com.MAVLink.enums.MAV_CMD
import android.os.Looper
import android.util.Log
import com.MAVLink.enums.MAV_MISSION_RESULT
import com.MAVLink.enums.MAV_MISSION_TYPE
import dji.common.mission.waypoint.WaypointMissionState

import jb.djilink.*
import kotlinx.android.synthetic.main.layout.*

/**
 * Created by jan on 02.11.17.
 */
class UDPMessageHandler(private val mavLink: MavLinkConnection, looper: Looper) : Handler(looper) {     //enthält alle Funktionen für den UDPMessageThread
    var mavLinkPacket: MAVLinkPacket? = null
    var parser = Parser()
    var main = mavLink.main



    override fun handleMessage(msg: Message) {          //Empfange Intents
        when (msg.what) {
            NEW_MSG -> {                                //MAVLink-Nachricht empfangen
                var packet = msg.obj as ByteArray
                var messageInterator = packet.iterator()
                try {
                    while (mavLinkPacket == null && messageInterator.hasNext()){
                        mavLinkPacket = parser.mavlink_parse_char(getInt(messageInterator.nextByte()))
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                if (mavLinkPacket != null) {
                    reactToMavLinkPacket(mavLinkPacket!!)
                    mavLinkPacket = null
                    parser = Parser()
                } else {
                    main.debug("Message received, but not identified")
                }


            }

            STATUS_UPDATE -> {
                val text = msg.obj.toString()
                main.debug(text)
            }

            MESSAGE_SENT -> {
               //do nothing
            }


            else -> super.handleMessage(msg)
        }

    }

    fun reactToMavLinkPacket(packet: MAVLinkPacket){            //Empfangene MAVLink-Nachricht identifizeren und darauf reagieren
        when (packet.msgid) {

            msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT -> {
               // main.debug("Heartbeat from ID: " + packet.sysid)
            }

            msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT -> {         //Neue Wegpunktübertragung instanzieren und starten
                val msg = packet.unpack() as msg_mission_count
                if (mavLink.waypointTransmission == null) {
                    mavLink.waypointTransmission = WaypointTransmission(msg.target_system, msg.target_component, msg.count, mavLink, true)
                    if (mavLink.waypointTransmission != null) {
                        mavLink.waypointTransmission!!.start()
                        main.debug("Wegpunktliste wird empfangen.")
                    }
                } else {
                    //Todo: Fehlermeldung, Eine Wegpunktübertragung läuft bereits
                }
            }


            msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM -> {       //Neuer Wegpunkt empfangen
                val msg = packet.unpack() as msg_mission_item
                if (mavLink.waypointTransmission != null) {
                    val waypoint = MavlinkWaypoint(msg)
                    mavLink.waypointTransmission!!.addWaypoint(waypoint)
                }
            }

            msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG -> {       //Diverse Kommandos identifizieren und darauf reagieren
                val msg = packet.unpack() as msg_command_long

                main.debug("Mavlink-Command empfangen: " + msg.command + "\nParam1: " + msg.param1)

                when (msg.command){

                    MAV_CMD.MAV_CMD_REQUEST_PROTOCOL_VERSION -> {   //Fordert Übertragung der MAVLink-Version
                        mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_REQUEST_PROTOCOL_VERSION, 0)
                        mavLink.sendVersion()
                    }

                    MAV_CMD.MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES -> {     //Fordert Fähigkeiten des Autopiloten
                        mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES, 0)
                        mavLink.sendVersion()
                    }

                    MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM -> {           //Fordert indirekt Start der Mission, DISARM wird nicht genutzt
                        printModeFlag()
                        try {
                            when(main.djiController.sdkManager.missionControl.waypointMissionOperator.currentState){

                                WaypointMissionState.READY_TO_EXECUTE -> {      //Mission starten
                                    main.debug("Mission wird gestartet")
                                    main.djiController.mission.startMission()
                                    //CommandAck wird von startMission gesendet.
                                }

                                WaypointMissionState.READY_TO_UPLOAD -> {       //Mission muss erst noch hochgeladen werden (sollte nicht so sein...)
                                    main.debug("Mission wird hochgeladen")
                                    main.djiController.mission.loadMission()
                                    main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 1)

                                }

                                else -> {
                                    main.debug("Laufende Mission wird gestoppt")
                                    main.djiController.sdkManager.missionControl.waypointMissionOperator.stopMission { error ->     //Irgendwas ist schiefgelaufen, Mission abbrechen
                                        if (error != null) Log.e("ARM/DISARM stopMission", error.description)
                                    }
                                    main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 1)

                                }
                            }

                        }catch (e: Exception){
                            main.debug("Mission konnte nicht gestartet werden.")
                            Log.e("ARM/DISARM", e.message)
                            main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM, 4)

                        }

                    }
                    MAV_CMD.MAV_CMD_MISSION_START -> {          //Alternatives Kommando zum starten der Mission
                        main.djiController.mission.startMission()
                        //commandAck wird von startMission gesendet
                    }

                    MAV_CMD.MAV_CMD_GET_HOME_POSITION -> {
                        mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_GET_HOME_POSITION, 0)
                        mavLink.mavlinkMessageConstructor.homePosition(main.homePosition)
                    }

                    MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL -> {
                        PITCH_ANGLE = msg.param1.toInt()
                        if (main.djiController.drone != null) {
                            main.djiController.drone!!.setGimbalAngleAndFocus()
                            //CommandAck wird von setGimbalAngleAndFocus gesendet
                        } else {
                            mavLink.mavlinkMessageConstructor.commandAck(msg.command, 4)
                        }
                    }

                    else -> {
                        main.debug("Message received: \n" + msg.toString())
                        mavLink.mavlinkMessageConstructor.commandAck(msg.command, 3) //cmd unknown
                    }

                }
            }


            msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST -> {   //Fordert Senden der aktuell geladenen Mission
                val msg = packet.unpack() as msg_mission_request_list
                if (mavLink.waypointTransmission == null) {
                    mavLink.waypointTransmission = WaypointTransmission(msg.target_system, msg.target_component, 0, mavLink, false)
                    if (mavLink.waypointTransmission != null) {
                        mavLink.waypointTransmission!!.start()
                        main.debug("Wegpunktliste wird gesendet.")
                    }
                } else {
                    main.debug("Wegpunktübertragung kann nicht erneut gestartet werden. Eine Übertragung läuft bereits.")
                }
            }

            msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST -> {     //Fordert Senden eines bestimmten Wegpunkt
                val msg = packet.unpack() as msg_mission_request
                if (mavLink.waypointTransmission != null) {
                    mavLink.waypointTransmission!!.sendNextWaypoint(msg.seq)
                }
            }

            msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK -> {             //Missionsübertragung erfolgreich
                if (mavLink.waypointTransmission != null){
                    mavLink.waypointTransmission!!.finishSendTransmission(true)
                }
            }

            msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST -> {       //Fordert Parameterliste, derzeit nicht implementiert
                var msgrec = packet.unpack() as msg_param_request_list
                main.debug("Message received: \n" + msgrec.toString())
                //TODO: geeignete Reaktion auf Param_Request_List
            }


            msg_set_mode.MAVLINK_MSG_ID_SET_MODE -> {           //Fordert setzen eines bestimmten Modes
                var msg = packet.unpack() as msg_set_mode
                setModes(msg.base_mode, msg.custom_mode)
                //printModeFlag()
            }

            msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL -> {     //Fordert löschen der aktuellen Mission
                var msg = packet.unpack() as msg_mission_clear_all
                mavLink.mavlinkMessageConstructor.missionAck(msg.target_system, msg.target_component, MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED.toShort())
                main.djiController.mission.buildMissionFromMavlinkMission(arrayListOf())
            }



            else -> {
                val msg = packet.unpack() as MAVLinkMessage
                main.debug("Message received: \n" + msg.toString())
            }

        }
    }


    fun getInt(byte: Byte): Int{            //Byte to Int, Kotlin-Umrechnungsfehler umgehen
        if (byte.toInt() < 0) {
            return 255 + byte.toInt() + 1
        }else {
            return byte.toInt()
        }

    }



    fun printModeFlag(){            //Debug

        main.debug("Mode_Flag: " +
                isArmed.toString() + ", "+
                isManualInput.toString() + ", "+
                isHIL.toString() + ", "+
                isStabilized.toString() + ", "+
                isGuided.toString() + ", "+
                isAuto.toString() + ", "+
                isTest.toString() + ", "+
                CUSTOMMODE.toString())
    }


}