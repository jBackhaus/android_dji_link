package jb.djilink

import android.util.Log
import com.google.android.gms.maps.model.*
import dji.common.flightcontroller.LocationCoordinate3D

class MapImage(var name: String, var location: LocationCoordinate3D, var heading: Float, var sichtfaktor: Float, var main:MainActivity){
    var image: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.azure)
    var width:Float = sichtfaktor * location.altitude
    var thumbnail: BitmapDescriptor? = null
    var overlay : GroundOverlay? = null


    init {
        Log.e("MapImage", "Blaues Thumbnail initialisiert für Bild "+name)

        redrawImage()
    }


    fun saveThumbnail(newImage: BitmapDescriptor){
        thumbnail = newImage
        Log.e("MapImage", "Thumbnail geladen für Bild "+name)


    }

    fun downloadSuccessful(newLocation: LocationCoordinate3D, newHeading: Float) {
        location = newLocation
        heading = newHeading
        width = sichtfaktor * newLocation.altitude

        if (thumbnail != null) {
            image = thumbnail!!
            Log.e("MapImage", "downloadSuccessful mit Thumbnail für Bild "+name)
        } else {
            thumbnail = BitmapDescriptorFactory.fromResource(R.mipmap.green)
            Log.e("MapImage", "downloadSuccessful mit Grünem Feld für Bild "+name)
            image = thumbnail!!
        }

        redrawImage()
    }

    fun redrawImage(){
        main.runOnUiThread {
            if (overlay != null){
                overlay!!.remove()
            }
            try{
                overlay = main.gui.map.addGroundOverlay(GroundOverlayOptions().image(image).position(LatLng(location.latitude, location.longitude), width).bearing(heading).transparency(0.5F))
                Log.e("MapImage", "Thumbnail gezeichnet für Bild "+name)

            }catch (e:Exception){
                image = BitmapDescriptorFactory.fromResource(R.mipmap.green)
                overlay = main.gui.map.addGroundOverlay(GroundOverlayOptions().image(image).position(LatLng(location.latitude, location.longitude), width).bearing(heading).transparency(0.5F))
                Log.e("MapImage", "Bitmap fehlerhaft, daher grünes Feld für Bild "+name)

            }

        }
    }



}