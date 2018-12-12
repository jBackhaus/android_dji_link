package jb.djilink.mavlink

import com.MAVLink.common.*
/**
 * Created by jan on 23.11.17.
 */

class MavlinkWaypoint(msg: msg_mission_item){       //Klasse, die zur Umwandlung benötigt wird
    var command: Short = msg.command.toShort()
    var current: Byte = msg.current.toByte()
    var autoContinue: Byte = msg.autocontinue.toByte()
    var param1: Float = msg.param1
    var param2: Float = msg.param2
    var param3: Float = msg.param3
    var param4: Float = msg.param4
    var x: Float = msg.x
    var y: Float = msg.y
    var z: Float = msg.z
    var frame: Short = msg.frame
    var seq: Int = msg.seq



}