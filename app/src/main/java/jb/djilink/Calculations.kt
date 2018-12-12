package jb.djilink

/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.product.Model
import dji.keysdk.ProductKey
import dji.sdk.base.BaseProduct
import jb.djilink.dji.DJIDrone

//Derzeit nicht mehr benötigte Umrechnung zwischen unterschiedlichen Koordinatentypen (MAVLink, Java und DJI)

fun getAndroidLocation(djiLocation: LocationCoordinate3D): Location {
    var retLocation = Location("dji")
    retLocation.latitude = djiLocation.latitude
    retLocation.longitude = djiLocation.longitude
    retLocation.altitude = djiLocation.altitude.toDouble()
    retLocation.time = System.currentTimeMillis()

    return retLocation
}

fun getDJILocation(androidLocation: Location): LocationCoordinate3D {
    return LocationCoordinate3D(androidLocation.latitude, androidLocation.longitude, androidLocation.altitude.toFloat())
}

fun getLatLng(locationCoordinate3D: LocationCoordinate3D): LatLng{
    return LatLng(locationCoordinate3D.latitude, locationCoordinate3D.longitude)
}

fun getLatLng(location: Location): LatLng{
    return LatLng(location.latitude, location.longitude)
}

fun sichtfaktor(model: Model): Float{
/*
    Zur Berechnung der Breite des projizierten Thumbnails in Abhängigkeit von der Höhe über Grund

    "Sichtfaktor": k [-]
    Öffnungswinkel des Kameraobjektivs: alpha [°]
    Flughöhe: h [m]
    Breite des projizierten Bilds (4:3): a [m]

    k=(8/5)*tan(alpha/2)

    a=k*h
*/

    when (model) {
        //MODEL_NAME nach https://developer.dji.com/api-reference/android-api/BaseClasses/DJIBaseProduct.html#djibaseproduct_model_inline
        //berechnet nach oben stehender Gleichung aus alpha nach DJI-Website

        Model.PHANTOM_4_ADVANCED, Model.PHANTOM_4, Model.PHANTOM_4_ADV -> return 1.7158F       //alpha = 94°

        Model.PHANTOM_4_PRO -> return 1.4406F   //alpha = 84°

        //Model.MAVIC_AIR -> return 1.4661F       //alpha = 85°
        Model.MAVIC_PRO -> return 1.3143F       //alpha = 78,8°

        Model.Spark -> return 1.3884F           //alpha = 81,9°

        else -> {
            //do nothing
        }

    }
    return 1.6F //Faktor für generische Kameramodelle mit alpha = 90°
}