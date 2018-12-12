package jb.djilink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.location.Location
import android.util.Log
import dji.common.flightcontroller.LocationCoordinate3D
import kotlin.math.roundToInt

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream




//Berechnungen und Befehl zum Anpassen von (Datums-)Strings auf definierte Länge "1" -> "01"

fun calculateDistance(p1: LocationCoordinate3D, p2: Location): Float {
    val p1x = Location(p2)
    p1x.latitude = p1.latitude
    p1x.longitude = p1.longitude
    p1x.altitude = p1.altitude.toDouble()
    var distance = FloatArray(3)
    Location.distanceBetween(p1x.latitude, p1x.longitude, p2.latitude, p2.longitude, distance)

    return distance[0]
}



fun calculateDistance(p1: LocationCoordinate3D, p2: LocationCoordinate3D): Float {

    var distance = FloatArray(3)

    Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, distance)

    return distance[0]
}


fun calculateHeading(p1: LocationCoordinate3D, p2: LocationCoordinate3D): Int {

    var distance = FloatArray(3)

    Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, distance)

    Log.e("calculateHeading", distance.toString())
    return distance[1].roundToInt()
}


fun calculateHeading(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {

    var distance = FloatArray(3)

    Location.distanceBetween(lat1, lon1, lat2, lon2, distance)

    Log.e("calculateHeading", distance.toString())
    return distance[1].roundToInt()
}

fun minString(string: String, length: Int, symbol: String): String{
    var outString = ""
    val missing = length - string.length
    if (missing > 0) {
        var i = 0
        while (i < missing){
            outString += symbol[0]
            i++
        }
    }
    outString += string
    return outString
}


fun saveImageToFile(main: MainActivity, image: Bitmap, filename: String){
    var directory = main.getExternalFilesDir(null).toString()
    var file: File? = null
    try{

        file = File(directory + "/" + filename)
        file.createNewFile()
        Log.e("saveImageToFile", "New imagefile created")
    } catch (e: Exception) {
        main.debug("Error creating image file:" + e.localizedMessage)
        Log.e("saveImageToFile", e.message)
    }

    if (file != null){


        try {
            val fos = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: Exception) {
            main.debug("Error writing image: " + e.localizedMessage)
        }

    }

}
