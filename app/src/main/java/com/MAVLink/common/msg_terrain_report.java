/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE TERRAIN_REPORT PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
* Response from a TERRAIN_CHECK request
*/
public class msg_terrain_report extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_TERRAIN_REPORT = 136;
    public static final int MAVLINK_MSG_LENGTH = 22;
    private static final long serialVersionUID = MAVLINK_MSG_ID_TERRAIN_REPORT;


      
    /**
    * Latitude (degrees *10^7)
    */
    public int lat;
      
    /**
    * Longitude (degrees *10^7)
    */
    public int lon;
      
    /**
    * Terrain height in meters AMSL
    */
    public float terrain_height;
      
    /**
    * Current vehicle height above lat/lon terrain height (meters)
    */
    public float current_height;
      
    /**
    * grid spacing (zero if terrain at this location unavailable)
    */
    public int spacing;
      
    /**
    * Number of 4x4 terrain blocks waiting to be received or read from disk
    */
    public int pending;
      
    /**
    * Number of 4x4 terrain blocks in memory
    */
    public int loaded;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_TERRAIN_REPORT;
              
        packet.payload.putInt(lat);
              
        packet.payload.putInt(lon);
              
        packet.payload.putFloat(terrain_height);
              
        packet.payload.putFloat(current_height);
              
        packet.payload.putUnsignedShort(spacing);
              
        packet.payload.putUnsignedShort(pending);
              
        packet.payload.putUnsignedShort(loaded);
        
        return packet;
    }

    /**
    * Decode a terrain_report message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.lat = payload.getInt();
              
        this.lon = payload.getInt();
              
        this.terrain_height = payload.getFloat();
              
        this.current_height = payload.getFloat();
              
        this.spacing = payload.getUnsignedShort();
              
        this.pending = payload.getUnsignedShort();
              
        this.loaded = payload.getUnsignedShort();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_terrain_report(){
        msgid = MAVLINK_MSG_ID_TERRAIN_REPORT;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_terrain_report(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_TERRAIN_REPORT;
        unpack(mavLinkPacket.payload);        
    }

                  
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_TERRAIN_REPORT - sysid:"+sysid+" compid:"+compid+" lat:"+lat+" lon:"+lon+" terrain_height:"+terrain_height+" current_height:"+current_height+" spacing:"+spacing+" pending:"+pending+" loaded:"+loaded+"";
    }
}
        