package jb.djilink.mavlink

import com.MAVLink.common.*
/**
 * Created by jan on 23.11.17.
 */

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
