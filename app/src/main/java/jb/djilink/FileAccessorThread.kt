package jb.djilink

import android.os.HandlerThread
import android.util.Log
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.xmp.XmpDirectory
import dji.common.flightcontroller.LocationCoordinate3D
import java.io.File

class FileAccessorThread (var main: MainActivity):HandlerThread("FileAccessorThread"){

    lateinit private var handler: android.os.Handler



    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = android.os.Handler(looper)

    }


    fun getFile(filename: String): File? {
        try {
            val imageFile = File(main.storageDir.absolutePath + "/" + filename)
            return imageFile
        }catch (e:Exception){
            main.debug("Cannot access file: "+filename)
            return null
        }
    }

    fun getMetas(filename: String){
        val file = File(main.storageDir.absolutePath+"/"+filename)
        var metadata: com.drew.metadata.Metadata?
        try {
            metadata = ImageMetadataReader.readMetadata(file)

        }catch (e:Exception){
            Log.e("getMetas", "Could not fetch metadata")
            metadata = null
        }

        var altitude = 0F
        var lon = 0.toDouble()
        var lat = 0.toDouble()
        var heading = 0F


        if (metadata!= null) {
            try {


                var gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                lat = gpsDirectory.geoLocation.latitude
                Log.i("getMetas", "lat= " + lat.toString())

                lon = gpsDirectory.geoLocation.longitude
                Log.i("getMetas", "lon= " + lon.toString())
            } catch (e: Exception) {
                Log.e("accessGPSTag", "Error: " + e.message)
            }


            val xmpDirectory = metadata.getFirstDirectoryOfType(XmpDirectory::class.java)
            if (xmpDirectory.xmpProperties["drone-dji:RelativeAltitude"] != null) altitude = xmpDirectory.xmpProperties["drone-dji:RelativeAltitude"]!!.toFloat()
            Log.i("getMetas", "alt= " + altitude.toString())

            if (xmpDirectory.xmpProperties["drone-dji:GimbalYawDegree"] != null) heading = xmpDirectory.xmpProperties["drone-dji:GimbalYawDegree"]!!.toFloat()
            Log.i("getMetas", "heading= " + heading.toString())


            for (image in main.gui.thumbnailList) {
                if (image.name == filename) {
                    if (lat == 0.toDouble()) {
                        lat = image.location.latitude
                        lon = image.location.longitude
                        Log.e("getMetas", "Bild " + filename + "enthält keine GPS-Informationen!")
                    }
                    image.downloadSuccessful(LocationCoordinate3D(lat, lon, altitude), heading)
                    break
                }
            }
        }

    }


    override fun quitSafely(): Boolean {
        handler.removeCallbacksAndMessages(null)
        return super.quitSafely()
    }

}