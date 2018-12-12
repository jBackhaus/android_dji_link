/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE GLOBAL_POSITION_INT_COV PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
* The filtered global position (e.g. fused GPS and accelerometers). The position is in GPS-frame (right-handed, Z-up). It  is designed as scaled integer message since the resolution of float is not sufficient. NOTE: This message is intended for onboard networks / companion computers and higher-bandwidth links and optimized for accuracy and completeness. Please use the GLOBAL_POSITION_INT message for a minimal subset.
*/
public class msg_global_position_int_cov extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV = 63;
    public static final int MAVLINK_MSG_LENGTH = 181;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV;


      
    /**
    * Timestamp (microseconds since system boot or since UNIX epoch)
    */
    public long time_usec;
      
    /**
    * Latitude, expressed as degrees * 1E7
    */
    public int lat;
      
    /**
    * Longitude, expressed as degrees * 1E7
    */
    public int lon;
      
    /**
    * Altitude in meters, expressed as * 1000 (millimeters), above MSL
    */
    public int alt;
      
    /**
    * Altitude above ground in meters, expressed as * 1000 (millimeters)
    */
    public int relative_alt;
      
    /**
    * Ground X Speed (Latitude), expressed as m/s
    */
    public float vx;
      
    /**
    * Ground Y Speed (Longitude), expressed as m/s
    */
    public float vy;
      
    /**
    * Ground Z Speed (Altitude), expressed as m/s
    */
    public float vz;
      
    /**
    * Covariance matrix (first six entries are the first ROW, next six entries are the second row, etc.)
    */
    public float covariance[] = new float[36];
      
    /**
    * Class id of the estimator this estimate originated from.
    */
    public short estimator_type;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV;
              
        packet.payload.putUnsignedLong(time_usec);
              
        packet.payload.putInt(lat);
              
        packet.payload.putInt(lon);
              
        packet.payload.putInt(alt);
              
        packet.payload.putInt(relative_alt);
              
        packet.payload.putFloat(vx);
              
        packet.payload.putFloat(vy);
              
        packet.payload.putFloat(vz);
              
        
        for (int i = 0; i < covariance.length; i++) {
            packet.payload.putFloat(covariance[i]);
        }
                    
              
        packet.payload.putUnsignedByte(estimator_type);
        
        return packet;
    }

    /**
    * Decode a global_position_int_cov message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.time_usec = payload.getUnsignedLong();
              
        this.lat = payload.getInt();
              
        this.lon = payload.getInt();
              
        this.alt = payload.getInt();
              
        this.relative_alt = payload.getInt();
              
        this.vx = payload.getFloat();
              
        this.vy = payload.getFloat();
              
        this.vz = payload.getFloat();
              
         
        for (int i = 0; i < this.covariance.length; i++) {
            this.covariance[i] = payload.getFloat();
        }
                
              
        this.estimator_type = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_global_position_int_cov(){
        msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_global_position_int_cov(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV;
        unpack(mavLinkPacket.payload);        
    }

                        
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GLOBAL_POSITION_INT_COV - sysid:"+sysid+" compid:"+compid+" time_usec:"+time_usec+" lat:"+lat+" lon:"+lon+" alt:"+alt+" relative_alt:"+relative_alt+" vx:"+vx+" vy:"+vy+" vz:"+vz+" covariance:"+covariance+" estimator_type:"+estimator_type+"";
    }
}
        