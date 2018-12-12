package jb.djilink

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import dji.sdk.products.Aircraft


import android.graphics.SurfaceTexture
import android.support.percent.PercentRelativeLayout
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dji.common.flightcontroller.FlightControllerState

import kotlinx.android.synthetic.main.layout.*
import com.google.android.gms.maps.model.Marker
import dji.common.mission.waypoint.Waypoint
import dji.sdk.base.BaseProduct


/**
 * Created by jan on 12.09.17.
 */



class GUI(var main: MainActivity) : View.OnClickListener, TextureView.SurfaceTextureListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    //Sammelt alle GUI-Elemente

    lateinit var ConnectStatusTextView: TextView
    lateinit var BtnTakePhoto: Button
    lateinit var BtnResetGUI: Button
    lateinit var progressBar: ProgressBar
    lateinit var videoView: TextureView
    lateinit var rateView: TextView
    lateinit var mapFragment: SupportMapFragment
    lateinit var uploadBar: ProgressBar
    lateinit var uploadText: TextView
    lateinit var BtnSettings: Button
    lateinit var PanicBtn: Button
    lateinit var VideoLayout: PercentRelativeLayout
    lateinit var pendingText: TextView
    lateinit var smbPendingText: TextView
    lateinit var smbProgressBar: ProgressBar
    lateinit var smbRateView: TextView
    lateinit var debugButton: Button




    var isLocated: Boolean = false
    var droneLocated: Boolean = false
    var mapIsReady = false
    var thumbnailList: MutableList<MapImage> =  arrayListOf()
    var missionPolylines : MutableList<Polyline>? = null

    lateinit var map: GoogleMap
    var remoteMarker: Marker? = null
    var droneMarker: Marker? = null


    init {
    }



    fun initUI() {
        Log.e(TAG,  "initUI")
        ConnectStatusTextView = main.ConnectStatusTextView

        BtnTakePhoto = main.btn_take_photo
        BtnResetGUI = main.btn_reset_gps
        progressBar =main.progressBar
        videoView = main.videoViewer
        rateView = main.rateView
        uploadBar = main.uploadProgressBar
        uploadText = main.uploadProgressText
        BtnSettings = main.btn_settings
        PanicBtn = main.btn_panic
        VideoLayout = main.videoLayout
        pendingText = main.pending
        debugButton = main.dbg

        smbPendingText = main.smbpending
        smbProgressBar = main.smbprogressBar
        smbRateView = main.smbrateView


        mapFragment = main.mapFragment as SupportMapFragment

        mapFragment.getMapAsync(this)
        BtnTakePhoto.setOnClickListener(this)
        BtnResetGUI.setOnClickListener(this)
        BtnSettings.setOnClickListener(this)
        PanicBtn.setOnClickListener(this)
        debugButton.setOnClickListener(this)






    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {     //initialisiert Anzeige des FPV-Videos nach Auslösen des Panik-Knopfes
        Log.e(TAG,  "onSurfaceTextureAvailable")
        if (main.djiController.codecManager == null && p0 != null) {
            main.djiController.initCodecManager(main, p0, p1, p2)


        }

    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {      //Beendet Anzeige des FPV-Videos (bremst aber weiterhin die Übertragungsrate aus)
        Log.e(TAG, "onSurfaceTextureDestroyed")
        if (main.djiController.codecManager != null) {
            main.djiController.codecManager!!.cleanSurface()
            main.djiController.codecManager = null
        }
        return false
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
        Log.e(TAG, "onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }






    override fun onClick(v: View) {         //onClickListener aller Schaltflächen
        when (v) {


            BtnTakePhoto -> {       //Einzelnes Foto auslösen (debug)
                if (main.djiController.drone!= null) main.djiController.drone!!.shootPhoto()
                main.debug("Einzelnes Foto befohlen")
            }

            BtnResetGUI -> {        //Karte neu zeichnen (Debug)
                updateTitleBar()
                redrawMap()
            }

            BtnSettings -> {        //Einstellungen öffnen
                openSettings()

            }
            PanicBtn -> {           //FPV-Video einblenden
                VideoLayout.visibility = PercentRelativeLayout.VISIBLE
                main.textView2.visibility = View.GONE
                main.PANIC = true
                main.djiController.initFPV()
            }
            debugButton -> {
                var filename = "a.jpeg"
                main.debug("tcp connected: " + TCP_IS_CONNECTED.toString())
                if (TCP_IS_CONNECTED){
                    main.debug("Send Debug-Image via TCP")
                    main.tcpConnection.addImageToList(filename)
                }
            }
            else -> {
            }
        }
    }





    fun openSettings() {        //Einstellungs-Activity starten
        var settingsPage = Intent(main as Context, SettingsActivity::class.java)
        main.startActivity(settingsPage)
    }


    fun updateDroneLocation(flightControllerState: FlightControllerState) {     //Anzeige der Drohne auf der Karte aktualisieren
        if (mapIsReady) {
                if (droneMarker != null){
                    main.runOnUiThread {
                        droneMarker!!.position = getLatLng(flightControllerState.aircraftLocation)
                        droneMarker!!.rotation = flightControllerState.attitude.yaw.toFloat()
                        Log.i("updateDroneLocation", "update Position: "+flightControllerState.aircraftLocation.latitude.toString() + ", "+flightControllerState.aircraftLocation.longitude.toString())

                    }
                } else {
                    droneLocated = true
                    main.runOnUiThread {
                        droneMarker = map.addMarker(MarkerOptions().position(getLatLng(flightControllerState.aircraftLocation)))
                        droneMarker!!.setAnchor(0.5.toFloat(), 0.5.toFloat())
                        droneMarker!!.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.dronex))
                        //                droneMarker.rotation = main.djiController.drone!!.flightController!!.compass.heading
                        droneMarker!!.rotation = flightControllerState.attitude.yaw.toFloat()
                        Log.i("updateDroneLocation", "new drone Marker at Position: "+flightControllerState.aircraftLocation.latitude.toString() + ", "+flightControllerState.aircraftLocation.longitude.toString())

                    }
                }



        }
    }



    fun redrawMission(waypointList: MutableList<Waypoint>){
        Log.e("redrawMission", "start der #Funktion")



        if (mapIsReady){
            if (missionPolylines != null) {
                Log.e("redrawMission", "remove Last Mission")

                for (currentLine in missionPolylines!!){
                    main.runOnUiThread { currentLine.remove() }
                }
                missionPolylines = null
            }
            main.runOnUiThread {

                var polyline = PolylineOptions().color(Color.CYAN).width(5F)

                Log.e("redrawMission", "start Drawing New Mission")
                //Mission einblenden
                Log.e("redrawMission", waypointList.size.toString() + " Wegpunkte in der waypointList")
                for (cWaypoint in waypointList) {
                    //var newMarker = map.addMarker(MarkerOptions().position(LatLng(cWaypoint.coordinate.latitude, cWaypoint.coordinate.longitude)))
                    //newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))

                    polyline.add(LatLng(cWaypoint.coordinate.latitude, cWaypoint.coordinate.longitude))
                    Log.e("redrawMission", "addPolyline: " + polyline.points.first().toString() + " -> " + polyline.points.last().toString())
                    if (polyline.points.size > 1) {
                        val currentPL = map.addPolyline(polyline)
                        Log.e("redrawMission", "addPolyline:\n\n" + polyline.toString() + "\n\n")
                        if (missionPolylines == null) {
                            missionPolylines = mutableListOf(currentPL)
                            Log.e("redrawMission", "Init missionPolylines")

                        } else {
                            missionPolylines!!.add(currentPL)
                            Log.e("redrawMission", "Added new Polyline to List.")

                        }

                    }


                    if (polyline.points.size == 2) {
                        polyline.points.removeAt(0)     //neuer Startpunkt ist bisheriges Ziel
                    }
                    if (cWaypoint.shootPhotoDistanceInterval != 0F) {
                        polyline.color(Color.MAGENTA)
                    } else {
                        polyline.color(Color.CYAN)
                    }
                }
            }




        }
    }

    fun redrawAllThumbs(thumbnailList: MutableList<MapImage>){
        //Todo
        for (image in thumbnailList){
            image.redrawImage()
        }
    }


    fun updateTabletLocation(latLng: LatLng){
        main.runOnUiThread {
            if (remoteMarker != null) {
                remoteMarker!!.position = latLng
                Log.i("updateTabletLocation", "update Position: "+latLng.latitude.toString() + ", "+latLng.longitude.toString())
            } else {
                remoteMarker = map.addMarker(MarkerOptions().position(main.gpsTracker.latlng))
                Log.i("updateTabletLocation", "new tablet Marker at Position: "+latLng.latitude.toString() + ", "+latLng.longitude.toString())

            }
        }
    }

    fun redrawMap() {       //Karte neu zeichnen (Position von Tablet und Drohne und Einblenden der geladenen Mission)
        if (mapIsReady) {
            main.runOnUiThread {
                map.clear()
            }
            droneMarker=null
            remoteMarker=null
            missionPolylines=null
            if (main.djiController.mission.waypointList != null){
                redrawMission(main.djiController.mission.waypointList!!)
            }
            if (main.djiController.drone != null && main.djiController.drone!!.flightControllerState != null) {
                updateDroneLocation(main.djiController.drone!!.flightControllerState!!)
            }

            updateTabletLocation(main.gpsTracker.getLatLng())


            redrawAllThumbs(thumbnailList)

        }


    }


    fun updateTitleBar() {                  //Verbindungsanzeige aktualisieren
        Log.e(TAG,  "updateTitleBar")
        var somethingIsConnected = false
        var product: BaseProduct? = null

        if (main.djiController.drone != null){
            product = main.djiController.drone!!.product
        }
        if (product != null) {
            if (product.isConnected) {
                //The product is connected
                val productName = product.model.displayName as String
                Log.e(TAG, productName + " connected")
                main.runOnUiThread{
                    ConnectStatusTextView.text = productName + " connected"
                }
                somethingIsConnected = true
            } else {
                if (product is Aircraft) {
                    val aircraft = product
                    if (aircraft.remoteController != null && aircraft.remoteController.isConnected) {
                        // The product is not connected, but the remote controller is connected
                        Log.e(TAG, "Only RC connected")
                        main.runOnUiThread{
                                        ConnectStatusTextView.text =  "Only RC connected"
                        }

                        somethingIsConnected = true
                    }
                }
            }
        }
        if (!somethingIsConnected) {
            // The product or the remote controller are not connected.
            Log.e(TAG, "Disconnected")
            main.runOnUiThread{
                ConnectStatusTextView.text = "Disconnected"
            }
        }


    }





    override fun onMapReady(googleMap: GoogleMap){      //Karte erfolgreich initialisiert (GoogleMaps-Funktion)
        Log.e(TAG,  "onMapReady")
        mapIsReady = true
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.setOnMapClickListener(this)
        redrawMap()

    }

    override fun onMapClick(point: LatLng){

        //do nothing
    }



    companion object {
        private val TAG = GUI::class.java.name
    }
}