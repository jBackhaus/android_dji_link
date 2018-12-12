package jb.djilink.mavlink

/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import com.MAVLink.enums.*


//Derzeit nicht genutzte Umwandlung von MAVLink-Enums in Strings

fun desc_mavType(type: Short): String {
    when (type.toInt()) {
        MAV_TYPE.MAV_TYPE_GENERIC -> return "Generic micro air vehicle"

        MAV_TYPE.MAV_TYPE_FIXED_WING -> return "Fixed wing aircraft"

        MAV_TYPE.MAV_TYPE_QUADROTOR -> return "Quadrotor"

        MAV_TYPE.MAV_TYPE_COAXIAL -> return "Coaxial helicopter"

        MAV_TYPE.MAV_TYPE_HELICOPTER -> return "Normal helicopter with tail rotor"

        MAV_TYPE.MAV_TYPE_ANTENNA_TRACKER -> return "Ground installation"

        MAV_TYPE.MAV_TYPE_GCS -> return "Operator control unit / ground control station"

        MAV_TYPE.MAV_TYPE_AIRSHIP -> return "Airship, controlled"

        MAV_TYPE.MAV_TYPE_FREE_BALLOON -> return "Free balloon, uncontrolled"

        MAV_TYPE.MAV_TYPE_ROCKET -> return "Rocket"

        MAV_TYPE.MAV_TYPE_GROUND_ROVER -> return "Ground rover"

        MAV_TYPE.MAV_TYPE_SURFACE_BOAT -> return "Surface vessel, boat, ship"

        MAV_TYPE.MAV_TYPE_SUBMARINE	 -> return "Submarine"

        MAV_TYPE.MAV_TYPE_HEXAROTOR	 -> return "Hexarotor"

        MAV_TYPE.MAV_TYPE_OCTOROTOR	 -> return "Octorotor"

        MAV_TYPE.MAV_TYPE_TRICOPTER	 -> return "Tricopter"

        MAV_TYPE.MAV_TYPE_FLAPPING_WING	 -> return "Flapping wing"

        MAV_TYPE.MAV_TYPE_KITE	 -> return "Kite"

        MAV_TYPE.MAV_TYPE_ONBOARD_CONTROLLER -> return "Onboard companion controller"

        MAV_TYPE.MAV_TYPE_VTOL_DUOROTOR	 -> return "Two-rotor VTOL using control surfaces in vertical operation in addition. Tailsitter."

        MAV_TYPE.MAV_TYPE_VTOL_QUADROTOR -> return "Quad-rotor VTOL using a V-shaped quad config in vertical operation. Tailsitter."

        MAV_TYPE.MAV_TYPE_VTOL_TILTROTOR -> return "Tiltrotor VTOL"

        MAV_TYPE.MAV_TYPE_VTOL_RESERVED2 -> return "VTOL reserved 2"

        MAV_TYPE.MAV_TYPE_VTOL_RESERVED3 -> return "VTOL reserved 3"

        MAV_TYPE.MAV_TYPE_VTOL_RESERVED4 -> return "VTOL reserved 4"

        MAV_TYPE.MAV_TYPE_VTOL_RESERVED5 -> return "VTOL reserved 5"

        MAV_TYPE.MAV_TYPE_GIMBAL -> return "Onboard gimbal"

        MAV_TYPE.MAV_TYPE_ADSB -> return "Onboard ADSB peripheral"

        else -> return "Not defined"
    }
}


fun desc_mavState(status: Short): String {
    when (status.toInt()) {
        MAV_STATE.MAV_STATE_UNINIT -> return "Uninitialized system, state is unknown."

        MAV_STATE.MAV_STATE_BOOT -> return "System is booting up."

        MAV_STATE.MAV_STATE_CALIBRATING -> return "System is calibrating and not flight-ready."

        MAV_STATE.MAV_STATE_STANDBY -> return "System is grounded and on standby. It can be launched any time."

        MAV_STATE.MAV_STATE_ACTIVE -> return "System is active and might be already airborne. Motors are engaged."

        MAV_STATE.MAV_STATE_CRITICAL -> return "System is in a non-normal flight mode. It can however still navigate."

        MAV_STATE.MAV_STATE_EMERGENCY -> return "System is in a non-normal flight mode. It lost control over parts or over the whole airframe. It is in mayday and going down."

        MAV_STATE.MAV_STATE_POWEROFF -> return "System just initialized its power-down sequence, will shut down now."

        MAV_STATE.MAV_STATE_FLIGHT_TERMINATION -> return "System is terminating itself."

        else -> return "Not defined"
    }
}

fun desc_mavModeFlag(flag: Short): String{
    when (flag.toInt()) {
        MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED -> return "MAV safety set to armed. Motors are enabled / running / can start. Ready to fly. Additional note: this flag is to be ignore when sent in the command MAV_CMD_DO_SET_MODE and MAV_CMD_COMPONENT_ARM_DISARM shall be used instead. The flag can still be used to report the armed state."

        MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED -> return " remote control input is enabled."

        MAV_MODE_FLAG.MAV_MODE_FLAG_HIL_ENABLED	 -> return " hardware in the loop simulation. All motors / actuators are blocked, but internal software is full operational."

        MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED -> return " system stabilizes electronically its attitude (and optionally position). It needs however further control inputs to move around."

        MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED	 -> return " guided mode enabled, system flies waypoints / mission items."

        MAV_MODE_FLAG.MAV_MODE_FLAG_AUTO_ENABLED	 -> return " autonomous mode enabled, system finds its own goal positions. Guided flag can be set or not, depends on the actual implementation."

        MAV_MODE_FLAG.MAV_MODE_FLAG_TEST_ENABLED	 -> return " system has a test mode enabled. This flag is intended for temporary system tests and should not be used for stable implementations."

        MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED	 -> return " Reserved for future use."

        else -> return "Not defined"
    }
}

fun desc_mavAutopilot(autopilot: Short):String {
    when (autopilot.toInt()) {
        MAV_AUTOPILOT. MAV_AUTOPILOT_GENERIC -> return "Generic autopilot, full support for everything"

        MAV_AUTOPILOT. MAV_AUTOPILOT_RESERVED -> return "Reserved for future use."

        MAV_AUTOPILOT. MAV_AUTOPILOT_SLUGS  -> return "SLUGS autopilot, http://slugsuav.soe.ucsc.edu"

        MAV_AUTOPILOT. MAV_AUTOPILOT_ARDUPILOTMEGA  -> return "ArduPilotMega / ArduCopter, http://diydrones.com"

        MAV_AUTOPILOT. MAV_AUTOPILOT_OPENPILOT  -> return "OpenPilot, http://openpilot.org"

        MAV_AUTOPILOT. MAV_AUTOPILOT_GENERIC_WAYPOINTS_ONLY  -> return "Generic autopilot only supporting simple waypoints"

        MAV_AUTOPILOT. MAV_AUTOPILOT_GENERIC_WAYPOINTS_AND_SIMPLE_NAVIGATION_ONLY  -> return "Generic autopilot supporting waypoints and other simple navigation commands"

        MAV_AUTOPILOT. MAV_AUTOPILOT_GENERIC_MISSION_FULL  -> return "Generic autopilot supporting the full mission command set"

        MAV_AUTOPILOT. MAV_AUTOPILOT_INVALID  -> return "No valid autopilot, e.g.a GCS or other MAVLink component"

        MAV_AUTOPILOT. MAV_AUTOPILOT_PPZ  -> return "PPZ UAV -http://nongnu.org/paparazzi"

        MAV_AUTOPILOT. MAV_AUTOPILOT_UDB  -> return "UAV Dev Board"

        MAV_AUTOPILOT. MAV_AUTOPILOT_FP  -> return "FlexiPilot"

        MAV_AUTOPILOT. MAV_AUTOPILOT_PX4  -> return "PX4 Autopilot - http://pixhawk.ethz.ch/px4/"

        MAV_AUTOPILOT. MAV_AUTOPILOT_SMACCMPILOT  -> return "SMACCMPilot - http://smaccmpilot.org"

        MAV_AUTOPILOT. MAV_AUTOPILOT_AUTOQUAD  -> return "AutoQuad-- http ://autoquad.org"

        MAV_AUTOPILOT. MAV_AUTOPILOT_ARMAZILA  -> return "Armazila-- http ://armazila.com"

        MAV_AUTOPILOT. MAV_AUTOPILOT_AEROB  -> return "Aerob --http://aerob.ru"

        MAV_AUTOPILOT. MAV_AUTOPILOT_ASLUAV  -> return "ASLUAV autopilot --http://www.asl.ethz.ch"

        MAV_AUTOPILOT. MAV_AUTOPILOT_SMARTAP  -> return "SmartAP Autopilot -http://sky-drones.com"

        else -> return "Not defined"
    }
}

fun desc_mavMissionType(missionType: Short):String {
    when (missionType.toInt()) {
        MAV_MISSION_TYPE.MAV_MISSION_TYPE_MISSION -> return "Items are mission commands for main mission."
        MAV_MISSION_TYPE.MAV_MISSION_TYPE_FENCE -> return "Specifies GeoFence area(s). Items are MAV_CMD_FENCE_ GeoFence items."
        MAV_MISSION_TYPE.MAV_MISSION_TYPE_RALLY -> return "Specifies the rally points for the vehicle. Rally points are alternative RTL points. Items are MAV_CMD_RALLY_POINT rally point items."
        MAV_MISSION_TYPE.MAV_MISSION_TYPE_ALL -> return "Only used in MISSION_CLEAR_ALL to clear all mission types."
        else -> return  "Not defined"
    }
}

