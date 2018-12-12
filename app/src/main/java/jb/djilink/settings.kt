package jb.djilink

import com.MAVLink.enums.MAV_AUTOPILOT
import com.MAVLink.enums.MAV_COMPONENT
import com.MAVLink.enums.MAV_STATE
import com.MAVLink.enums.MAV_TYPE
import dji.common.mission.waypoint.WaypointMissionFinishedAction

/**
 * Created by jan on 05.10.17.
 */

//Zustandsvariablen, nicht ändern
var DOWNLOAD_IMAGES = false
var TCP_UPLOAD = false
var SHOW_THUMBNAILS = false

var ALTITUDE: Float = 30.toFloat()
var SPEED: Float = 15.toFloat()
var MAXSPEED: Float = 15f
var PITCH_ANGLE: Int = (-90)





//Einstellungen

val SMB_PACKET_SIZE = 262144

val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 1.toFloat() // 1 meters
val MIN_TIME_BW_UPDATES: Long = 1000.toLong() // 1 seconds

val MINIMUM_DISTANCE_BETWEEN_WAYPOINTS = 1
var DISTANCE_BETWEEN_PHOTOS = 0f

val DOWNLOAD_MAX_TIME = 60000L
val MAX_DOWNLOAD_TRIES = 3

var mFinishedAction = WaypointMissionFinishedAction.GO_HOME


//SMB Settings (ftp-Bezeichnung aufgrund früherer Implementierung)
var ftpIp = "0.0.0.0"
var ftpAccount = ""
var ftpPassword = ""
var ftpSubfolder = ""

var SMB_IS_CONNECTED = false

//TCP-Settings

var tcpIp = "0.0.0.0"
var tcpPort = 14551

var TCP_IS_CONNECTED = false
var TCP_BUFFER_SIZE = 1024

//MAVLINK

var udpIp = "0.0.0.0"
var udpPort = 14550
var MAVLINK_IS_CONNECTED = false

val BYTE_ARRAY_LENGTH = 1024

val TIME_BETWEEN_HEARTBEATS = 1000L
val TIME_BETWEEN_RETRIES = 500L

//MSG_IDs

val STATUS_UPDATE = 0
val NEW_MSG = 1
val MESSAGE_SENT = 2


//MAVLink Settings
val MAVLINK_VERSION = 100

val AUTOPILOT = MAV_AUTOPILOT.MAV_AUTOPILOT_PX4
val SYSID = 2
val COMPID = MAV_COMPONENT.MAV_COMP_ID_AUTOPILOT1
val SYSTEMSTATUS = MAV_STATE.MAV_STATE_ACTIVE
val Type = MAV_TYPE.MAV_TYPE_FIXED_WING


val FLAG_CONNECTION_CHANGE = "fpv_tutorial_connection_change"



