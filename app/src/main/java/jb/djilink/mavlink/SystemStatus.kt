package jb.djilink.mavlink

import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR.*
import com.MAVLink.enums.MAV_PROTOCOL_CAPABILITY.*
import dji.common.model.LocationCoordinate2D
import jb.djilink.MainActivity
import jb.djilink.dji.DJIDrone
import kotlin.math.roundToInt

/**
 * Created by jan on 21.11.17.
 */

//Derzeit globale Funktionen zur Bestimmung der Bitmaps von Status und Fähigkeiten

fun mavProtocolCapability(): Long{
    var value = 0L

    value += MAV_PROTOCOL_CAPABILITY_MISSION_INT
    value += MAV_PROTOCOL_CAPABILITY_COMMAND_INT
    value += MAV_PROTOCOL_CAPABILITY_MISSION_FLOAT
    value += MAV_PROTOCOL_CAPABILITY_FLIGHT_INFORMATION
    value += MAV_PROTOCOL_CAPABILITY_FLIGHT_TERMINATION
    value += MAV_PROTOCOL_CAPABILITY_MISSION_RALLY
    value += MAV_PROTOCOL_CAPABILITY_SET_ATTITUDE_TARGET
    value += MAV_PROTOCOL_CAPABILITY_SET_ACTUATOR_TARGET
    value += MAV_PROTOCOL_CAPABILITY_SET_POSITION_TARGET_GLOBAL_INT
    value += MAV_PROTOCOL_CAPABILITY_SET_POSITION_TARGET_LOCAL_NED
    value += MAV_PROTOCOL_CAPABILITY_COMPASS_CALIBRATION
    value += MAV_PROTOCOL_CAPABILITY_PARAM_FLOAT
    value += MAV_PROTOCOL_CAPABILITY_PARAM_UNION
    value += MAV_PROTOCOL_CAPABILITY_TERRAIN

    return value
}

fun mavSysStatusSensorPresent(): Long{
    var value = 0.toLong()

    value += MAV_SYS_STATUS_SENSOR_3D_GYRO
    value += MAV_SYS_STATUS_SENSOR_3D_ACCEL
    value += MAV_SYS_STATUS_SENSOR_3D_MAG
    value += MAV_SYS_STATUS_SENSOR_GPS
    value += MAV_SYS_STATUS_SENSOR_ANGULAR_RATE_CONTROL
    value += MAV_SYS_STATUS_SENSOR_ATTITUDE_STABILIZATION
    value += MAV_SYS_STATUS_SENSOR_YAW_POSITION
    value += MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS
    value += MAV_SYS_STATUS_GEOFENCE
    value += MAV_SYS_STATUS_SENSOR_BATTERY


    return value
}

fun mavSysStatusSensorEnabled(): Long{
    var value = 0.toLong()

    value += MAV_SYS_STATUS_SENSOR_3D_GYRO
    value += MAV_SYS_STATUS_SENSOR_3D_ACCEL
    value += MAV_SYS_STATUS_SENSOR_3D_MAG
    value += MAV_SYS_STATUS_SENSOR_GPS
    value += MAV_SYS_STATUS_SENSOR_ANGULAR_RATE_CONTROL
    value += MAV_SYS_STATUS_SENSOR_ATTITUDE_STABILIZATION
    value += MAV_SYS_STATUS_SENSOR_YAW_POSITION
    value += MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS
    value += MAV_SYS_STATUS_GEOFENCE
    value += MAV_SYS_STATUS_SENSOR_BATTERY


    return value
}

fun mavSysStatusSensorHealth(): Long{
    var value = 0.toLong()

    value += MAV_SYS_STATUS_SENSOR_3D_GYRO
    value += MAV_SYS_STATUS_SENSOR_3D_ACCEL
    value += MAV_SYS_STATUS_SENSOR_3D_MAG
    value += MAV_SYS_STATUS_SENSOR_GPS
    value += MAV_SYS_STATUS_SENSOR_ANGULAR_RATE_CONTROL
    value += MAV_SYS_STATUS_SENSOR_ATTITUDE_STABILIZATION
    value += MAV_SYS_STATUS_SENSOR_YAW_POSITION
    value += MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS
    value += MAV_SYS_STATUS_GEOFENCE
    value += MAV_SYS_STATUS_SENSOR_BATTERY


    return value
}


fun mavSysStatusLoad(): Int{
    var value = 100


    return value
}

fun mavSysStatusVoltage(): Int{
    var value = 3700        //Todo: Voltage von der Drohne


    return value
}


fun mavSysStatusCurrent(): Short{
    var value = (-1).toShort()


    return value
}


fun mavSysStatusRemaining(): Byte{
    var value = (-1).toByte()


    return value
}


fun mavSysStatusDrops(): Int{
    var value = 0


    return value
}



fun mavSysStatusErrors(): Int{
    var value = 0


    return value
}

fun mavSysStatusError1():Int{
    var value = 0


    return value
}

fun mavSysStatusError2():Int{
    var value = 0


    return value
}

fun mavSysStatusError3():Int{
    var value = 0


    return value
}

fun mavSysStatusError4():Int{
    var value = 0


    return value
}


fun updateHomePosition(main: MainActivity){
    var home = main.homePosition
    var home2D = LocationCoordinate2D(0.toDouble(),0.toDouble())
    var homeAlt = 0F
    var homeApproachZ = 30F

    if (main.djiController.drone != null && main.djiController.drone!!.flightControllerState != null && main.djiController.drone!!.flightControllerState!!.isHomeLocationSet){
        home2D = main.djiController.drone!!.flightControllerState!!.homeLocation
        homeAlt = main.djiController.drone!!.flightControllerState!!.homePointAltitude
        homeApproachZ = main.djiController.drone!!.flightControllerState!!.goHomeHeight.toFloat()

    } else {
        if(main.gpsTracker.running){
            home2D = LocationCoordinate2D(main.gpsTracker.latitude, main.gpsTracker.longitude)
            homeAlt = main.gpsTracker.altitude.toFloat()
        }
    }

    home.lat = (home2D.latitude * 1E7).roundToInt()
    home.lon = (home2D.longitude * 1E7).roundToInt()
    home.alt = (homeAlt * 1000).roundToInt()

    home.x = 0F
    home.y = 0F
    home.z = 0F

    //home.q unchanged at (0F,0F,0F,0F)
    //Todo home.q=

    home.approachX = 0F
    home.approachY = 0F
    home.approachZ = homeApproachZ


    main.homePosition = home
}

