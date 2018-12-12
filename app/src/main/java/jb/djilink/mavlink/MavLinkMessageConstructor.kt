package jb.djilink.mavlink

import android.os.Handler
import android.os.HandlerThread
import com.MAVLink.Messages.MAVLinkMessage
import com.MAVLink.common.*
import java.nio.charset.Charset
import kotlin.math.PI


/**
 * Created by jan on 23.11.17.
 */
class MavLinkMessageConstructor(var mavLink: MavLinkConnection): HandlerThread("MavLinkMessageConstructor") {

        //Thread zum konstruieren von MAVLink-Nachrichten. Erzeugt Nachtichten des entsprechenden Namens, füllt PAYLOAD mit den übergebenen Parameteren und übergibt an UDPSenderThread zum Senden
    lateinit var handler: Handler


    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
    }


    fun commandLong(targetSystem: Short, targetComponent: Short, command: Int, confirmation: Short, p1: Float, p2: Float, p3: Float, p4: Float, p5: Float, p6: Float, p7: Float) {

        handler.post {
            var msg = msg_command_long()
            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.command = command
            msg.confirmation = confirmation
            msg.param1 = p1
            msg.param2 = p2
            msg.param3 = p3
            msg.param4 = p4
            msg.param5 = p5
            msg.param6 = p6
            msg.param6 = p6
            msg.param7 = p7

            sendMessage(msg)
        }

    }


    fun heartbeat(type: Short, autopilot: Short, basemode: Short, custommode: Long, systemStatus: Short, mavlinkVersion: Short) {
        handler.post {

            var msg = msg_heartbeat()
            msg.type = type
            msg.autopilot = autopilot
            msg.base_mode = basemode
            msg.custom_mode = custommode
            msg.system_status = systemStatus
            msg.mavlink_version = mavlinkVersion

            sendMessage(msg)
        }
    }

    fun globalPositionInt(timeBootMS: Long, lat: Double, lon: Double, alt: Int, relativeAlt: Int, vx: Short, vy: Short, vz: Short, hdg: Int) {
        handler.post {
            var msg = msg_global_position_int()
            msg.time_boot_ms = timeBootMS
            msg.lat = (lat * 10000000).toInt()
            msg.lon = (lon * 10000000).toInt()
            msg.alt = alt
            msg.relative_alt = relativeAlt
            msg.vx = vx
            msg.vy = vy
            msg.vz = vz
            msg.hdg = hdg

            sendMessage(msg)
        }

    }

    fun vfrHud(airspeed: Float, groundspeed: Float, heading: Short, throttle: Int, altitude: Float, climb: Float){
        handler.post {
            var msg = msg_vfr_hud()
            msg.airspeed = airspeed
            msg.groundspeed = groundspeed
            msg.heading = heading
            msg.throttle = throttle
            msg.alt = altitude
            msg.climb = climb

            sendMessage(msg)

        }
    }

    fun sysStatus(onboardControlSensorsPresent: Long, onboardControlSensorsEnabled: Long, onboardControlSensorsHealth: Long, load: Int, voltageBattery: Int, currentBattery: Short, batteryRemaining: Byte, dropRateComm: Int, errorsComm: Int, errorCount1: Int, errorCount2: Int, errorCount3: Int, errorCount4: Int) {
        handler.post {
            var msg = msg_sys_status()

            msg.onboard_control_sensors_present = onboardControlSensorsPresent
            msg.onboard_control_sensors_enabled = onboardControlSensorsEnabled
            msg.onboard_control_sensors_health = onboardControlSensorsHealth
            msg.load = load
            msg.voltage_battery = voltageBattery
            msg.current_battery = currentBattery
            msg.battery_remaining = batteryRemaining
            msg.drop_rate_comm = dropRateComm
            msg.errors_comm = errorsComm
            msg.errors_count1 = errorCount1
            msg.errors_count2 = errorCount2
            msg.errors_count3 = errorCount3
            msg.errors_count4 = errorCount4

            sendMessage(msg)
        }
    }



    fun autopilotVersion(capabilities: Long){
        handler.post {
            var msg = msg_autopilot_version()

            msg.capabilities = capabilities
            msg.board_version = 1
            msg.flight_sw_version = 19464447    //Entspricht v1.41.00.255 von PX4 zur Herstellung der Kompatibilität, siehe PX4/Firmware/src/lib/version/version.h "version in the form 0xAABBCCTT (AA: Major, BB: Minor, CC: Patch, TT Type @see FIRMWARE_TYPE)"
            msg.vendor_id = 1
            msg.product_id = 1
            msg.uid = 1

            //TODO: Product-IDs und Firmware nummerieren und global definieren
            mavLink.main.debug("MSG_AUTOPILOT_VERSION gesendet.\nCapabilities: " + msg.capabilities + "\nFlightVersion: "+ msg.flight_sw_version)
            sendMessage(msg)
        }
    }

    fun missionRequest(targetSystem: Short, targetComponent: Short, current: Int) {
        handler.post {

            var msg = msg_mission_request()
            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.seq = current

            sendMessage(msg)
        }
    }

    fun missionAck(targetSystem: Short, targetComponent: Short, type: Short) {
        handler.post {
            var msg = msg_mission_ack()
            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.type = type

            sendMessage(msg)
        }
    }

    fun missionCount(targetSystem: Short, targetComponent: Short, count: Int) {
        handler.post {
            var msg = msg_mission_count()
            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.count = count

            mavLink.main.debug("Mission_Count gesendet. Count: "+ count)

            sendMessage(msg)
        }
    }

    fun missionItem(targetSystem: Short, targetComponent: Short, mavlinkWaypoint: MavlinkWaypoint) {
        handler.post {
            var msg = msg_mission_item()
            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.seq = mavlinkWaypoint.seq
            msg.frame = mavlinkWaypoint.frame
            msg.command = mavlinkWaypoint.command.toInt()
            msg.current = mavlinkWaypoint.current.toShort()
            msg.autocontinue = mavlinkWaypoint.autoContinue.toShort()
            msg.param1 = mavlinkWaypoint.param1
            msg.param2 = mavlinkWaypoint.param2
            msg.param3 = mavlinkWaypoint.param3
            msg.param4 = mavlinkWaypoint.param4
            msg.x = mavlinkWaypoint.x
            msg.y = mavlinkWaypoint.y
            msg.z = mavlinkWaypoint.z

            sendMessage(msg)
        }

    }

    fun paramValue(paramID: String, paramValue: Float, paramType: Short, paramCount: Int, paramIndex: Int) {
        handler.post {
            var msg = msg_param_value()

            msg.param_id = paramID.toByteArray(Charset.forName("UTF-8"))
            msg.param_value = paramValue
            msg.param_type = paramType
            msg.param_count = paramCount
            msg.param_index = paramIndex

            sendMessage(msg)
        }

    }

    fun paramRequestRead(targetSystem: Short, targetComponent: Short, paramID: String, paramIndex: Int) {
        handler.post {
            var msg = msg_param_request_read()

            msg.target_system = targetSystem
            msg.target_component = targetComponent
            msg.param_id = paramID.toByteArray(Charset.forName("UTF-8"))
            msg.param_index = paramIndex.toShort()

            sendMessage(msg)
        }
    }

    fun homePosition(home:MavlinkHomePosition){
        handler.post{
            var msg = msg_home_position()


            msg.latitude = home.lat
            msg.longitude = home.lon
            msg.altitude = home.alt
            msg.x = home.x
            msg.y = home.y
            msg.z = home.z
            msg.q = home.q
            msg.approach_x = home.approachX
            msg.approach_y = home.approachY
            msg.approach_z = home.approachZ

            mavLink.main.debug("Home-Position wird gesendet.")

            sendMessage(msg)
        }
    }


    fun attitude(timeBootMS: Long, roll: Float, pitch: Float, yaw: Float, rollspeed: Float, pitchspeed: Float, yawspeed: Float){
        handler.post{
            var msg = msg_attitude()

            msg.time_boot_ms = timeBootMS
            msg.roll = roll
            msg.pitch = pitch
            msg.yaw = yaw
            msg.rollspeed = rollspeed
            msg.pitchspeed = pitchspeed
            msg.yawspeed = yawspeed


            sendMessage(msg)
        }
    }


    fun sendMessage(msg: MAVLinkMessage){
        mavLink.udpSenderThread.sendMavLinkMessage(msg, mavLink.address, mavLink.port)
    }


    fun commandAck(command: Int, result: Short){
        handler.post{
            var msg = msg_command_ack()

            msg.command = command
            msg.result = result

            sendMessage(msg)
        }
    }
}