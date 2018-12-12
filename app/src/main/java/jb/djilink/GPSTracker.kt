package jb.djilink

/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import jb.djilink.mavlink.updateHomePosition

class GPSTracker(var main: MainActivity) : LocationListener {           //Bestimmt Position des Tablets
    private val TAG = GPSTracker::class.java.getName()
    internal var isGPSEnabled = false
    internal var isNetworkEnabled = false
    internal var canGetLocation = false
    internal var location: Location? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var altitude: Double = 0.toDouble()
    internal var velocity: Float = 0.toFloat()
    internal var bearing: Float = 0.toFloat()
    internal var latlng = LatLng(0.toDouble(), 0.toDouble())

    internal var running: Boolean = false
    internal var runningLow: Boolean = false

    protected var locationManager: LocationManager? = null



    init {

    }

    internal fun setPartials (newLocation: Location) {
        longitude = newLocation.longitude
        latitude = newLocation.latitude
        altitude = newLocation.altitude
        velocity = newLocation.speed
        bearing = newLocation.bearing
        latlng = LatLng(newLocation.latitude, newLocation.longitude)
    }

    fun startUsingGPS() {       //Startet Positionsbestimmung über GPS oder Telefon-Netz
        Log.e(TAG, "startUsingGPS")

        try {
            locationManager = main.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)

            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.e("GPS", "******isGPSEnabled********" + isGPSEnabled)
            Log.e("Network", "******isNetworkEnabled********" + isNetworkEnabled)

            if (!isGPSEnabled) {
                if (isNetworkEnabled){
                    this.canGetLocation = true
                    if (runningLow == false) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this
                        )
                        runningLow = true
                        Log.e("Network", "Network Localizer started")

                    }
                }
            } else {
                this.canGetLocation = true
                if (running == false) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    )
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    )
                    running = true
                    Log.e("GPS", "GPS Localizer started")

                }
                if (isNetworkEnabled) {
                    if (runningLow == false) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this
                        )
                        runningLow = true
                        Log.e("Network", "Network Localizer started")

                    }
                }


            }

        } catch (e: Exception) {
            e.printStackTrace()
            main.showToast(e.localizedMessage)
        }
    }

    fun stopUsingGPS() {
        Log.e(TAG, "stopUsingGPS")

        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    fun getLatitude(): Double {

        return latitude
    }

    fun getLongitude(): Double {

        return longitude
    }
    fun getAltitude(): Double {

        return altitude
    }

    fun getLatLng(): LatLng {
        return latlng
    }

    fun getVelocity(): Float {

        return velocity
    }

    fun getBearing(): Float {

        return bearing
    }

    fun getLocation(): Location? {
        return this.getLocation()
    }


    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    override fun onLocationChanged(mLocation: Location) {       //Zentriert Karte beim ersten Positions-Wert

        location = mLocation
        setPartials(mLocation)
        main.gui.updateTabletLocation(latlng)
        if (main.gui.isLocated == false) {
            main.gui.isLocated = true
            main.runOnUiThread {
                main.gui.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18.toFloat()))
            }
            updateHomePosition(main)
        }

    }

    override fun onProviderDisabled(arg0: String) {
        Log.e(TAG, "onProviderDisabled:" + arg0)

    }

    override fun onProviderEnabled(arg0: String) {
        Log.e(TAG, "onProviderEnabled:" + arg0)


    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {

    }






}