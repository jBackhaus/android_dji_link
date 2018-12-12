package jb.djilink.mavlink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import com.MAVLink.common.*


class MavlinkHomePosition{       //Klasse, die zur Umwandlung benötigt wird
    var lat: Int = 0
    var lon: Int = 0
    var alt: Int = 0
    var x: Float = 0F
    var y: Float = 0F
    var z: Float = 0F
    var q: FloatArray = FloatArray(4)
    var approachX: Float = 0F
    var approachY: Float = 0F
    var approachZ: Float = 0F

    init{
        q[0]=1F
    }


}
