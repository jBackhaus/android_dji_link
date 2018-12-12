package jb.djilink.dji
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.util.Log
import dji.common.camera.SettingsDefinitions
import dji.common.flightcontroller.FlightControllerState
import dji.common.gimbal.Rotation
import dji.common.gimbal.RotationMode
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.flightcontroller.FlightController
import dji.sdk.gimbal.Gimbal
import dji.sdk.products.Aircraft
import android.content.Intent
import com.MAVLink.enums.MAV_CMD
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import dji.common.error.DJIError
import dji.common.util.CommonCallbacks
import dji.midware.data.model.P3.DataFlycWayPointMissionSwitch
import dji.sdk.camera.*
import jb.djilink.*
import jb.djilink.mavlink.MavlinkHomePosition
import jb.djilink.mavlink.updateHomePosition
import java.lang.System.currentTimeMillis
import kotlin.math.*



class DJIDrone (var djiController: DJIController, var product: BaseProduct) {       //Drohnenklasse mit eigenen Funktionen und Listenern
    var camera: dji.sdk.camera.Camera? = null
    var gimbal: Gimbal? = null

    var main = djiController.main
    var flightController: FlightController? = null
    var flightControllerState: FlightControllerState? = null

//    var currentImage : MediaFile? = null
    var fetchMediaTaskScheduler: FetchMediaTaskScheduler? = null




    //Callbacks & Listeners:

    val mDJIBaseProductListener = object : BaseProduct.BaseProductListener {            //Meldet Änderungen an der Drohne
        override fun onComponentChange(key: BaseProduct.ComponentKey, oldComponent: BaseComponent?, newComponent: BaseComponent?) {     //An- oder Abmeldung einer Komponente
            var message = "onComponentChange: "
            if (oldComponent != null) {
                message += "Component -> "
            } else {
                message += "null -> "
            }

            if (newComponent != null) {
                message += "Component"
            } else {
                message += "null"
                isArmed(false)
                isManualInput(false)
                isAuto(false)


            }
            Log.e("baseProductListener", message)
            newComponent?.setComponentListener{ onComponentChange ->
                notifyStatusChange()
            }
            main.gui.updateTitleBar()
        }

        override fun onConnectivityChange(isConnected: Boolean) {       //Änderung der Drohnenverbindung
            Log.e("baseProductListener",  "onConnectivityChange, isConnected: "+isConnected.toString())
            initFlightController()      //muss neu initialisert werden. Wenn Verbindung verloren ist, passiert nix
            main.gui.updateTitleBar()
            notifyStatusChange()
        }
    }



    val newImageCallback = object : MediaFile.Callback {     //reagiert auf neue Mediendatei im Speicher der Drohne
        override fun onNewFile(newImage: MediaFile) {
            if (newImage.mediaType == MediaFile.MediaType.JPEG) {

                if (DOWNLOAD_IMAGES) {
                    djiController.downloader.addImageToDownloadList(newImage)
                }

                if (SHOW_THUMBNAILS) {

                    if (flightControllerState!= null){
                        var newMapImage = MapImage(newImage.fileName, flightControllerState!!.aircraftLocation, flightControllerState!!.attitude.yaw.toFloat(), sichtfaktor(product.model), main)
                        main.gui.thumbnailList.add(newMapImage)
                    }

                    if (fetchMediaTaskScheduler != null) {

                        fetchMediaTaskScheduler!!.moveTaskToEnd(FetchMediaTask(newImage, FetchMediaTaskContent.THUMBNAIL) { mediaFile, option, error ->
                            for (image in main.gui.thumbnailList){
                                if (image.name == mediaFile.fileName){
                                    try {
                                        image.saveThumbnail(BitmapDescriptorFactory.fromBitmap(mediaFile.thumbnail))
                                    }catch (error: Exception){
                                        Log.e("FetchThumbnail", error.localizedMessage)
                                    }
                                }
                            }
                        })



                        fetchMediaTaskScheduler!!.resume { error ->
                            if (error != null) {
                                main.debug(error.description)
                            }

                        }
                    }
                }
            }
        }
    }


    init {

        product.setBaseProductListener(mDJIBaseProductListener)
        if (product.camera != null){
            camera = product.camera
            fetchMediaTaskScheduler = camera!!.mediaManager!!.scheduler
            camera!!.setMediaFileCallback(newImageCallback)
            setCameraMode("SHOOT_PHOTO")
        }
        if (product.gimbal != null){
            gimbal = product.gimbal
        }
        initFlightController()


    }





    fun executePhoto() {                //Fotoaufzeichnung manuell beginnen (wird aufgerufen, nachdem der Modus auf Einzelbild gesetzt wurde)
        if (camera != null) {
            camera!!.startShootPhoto{ error ->
                if (error != null) {
                    main.showToast("Photo Error: " + error.description)
                }
            }
        }
    }


    fun setCameraMode(mode: String) {       //Kamera in Fotomodus versetzen (Videomodus nicht implementiert)
        if (mode == "SHOOT_PHOTO"){
            if (camera != null) {
                camera!!.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO) {error ->
                    if (error == null){
                        main.showToast("CameraMode: SHOOT_PHOTO")
                    } else {
                        main.showToast("Error setting CameraMode: " + error.description)
                    }
                }
            }
        }
    }


    fun setGimbalAngleAndFocus(){           //Kamera ausrichten und Fokus auf unendlich



        var builder = Rotation.Builder()
        builder.mode(RotationMode.ABSOLUTE_ANGLE)
        builder.pitch(PITCH_ANGLE.toFloat())
        if (gimbal!= null){
            gimbal!!.rotate(builder.build()) {djiError ->
                if (djiError != null){
                    Log.e("setGimbal", djiError.description)
                    main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL, 4)
                } else {
                    main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL, 0)
                }

            }

        } else {
            main.mavLink.mavlinkMessageConstructor.commandAck(MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL, 4)
        }

        if(camera != null && camera!!.isAdjustableFocalPointSupported){
            camera!!.setFocusRingValue(0) {error ->
                if (error != null) Log.e("setFocus", error.description)
            }
        }
    }






    fun shootPhoto() {              //einzelnes Foto veranlassen, nachdem Einzelbildmodus gesetzt wurde
        if (camera != null) {
            camera!!.setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.SINGLE) {error ->
                if (error == null) {
                    executePhoto()
                } else {
                    main.showToast("setShootPhotoMode " + error.description)
                    Log.e("setShootPhotoMode", error.description)
                }

            }
        }
    }



    fun initFlightController() {                                //Flightcontroller identifizieren und Listener registrieren
        Log.e("DJIDrone",  "initFlightController")
        if (product.isConnected && product is Aircraft) {
            flightController = (product as Aircraft).flightController

        } else {
            flightController = null
            main.debug("Keine Verbindung zum Flightcontroller!")
        }
        if (flightController != null) {
            flightController!!.setStateCallback{ newFlightControllerState ->        //Updates mit 10 Hz, weiterleiten per MAVLink und GUI updaten
                flightControllerState = newFlightControllerState
                updateHomePosition(main)
                main.gui.updateDroneLocation(newFlightControllerState)

                val attitude = newFlightControllerState.attitude
                var roll = ((attitude.roll / 180)* PI)

                var pitch = (attitude.pitch / 180)* PI

                var yaw = (attitude.yaw / 180)* PI

                val vx = newFlightControllerState.velocityX
                val vy = newFlightControllerState.velocityY
                val vz = newFlightControllerState.velocityZ

                var heading = (((atan2(vx, vy) + PI)*180)/(PI))

                main.mavLink.mavlinkMessageConstructor.attitude(newFlightControllerState.flightTimeInSeconds.toLong(), roll.toFloat(), pitch.toFloat(), yaw.toFloat(), 0f, 0f, 0f)


                main.mavLink.mavlinkMessageConstructor.globalPositionInt(newFlightControllerState.flightTimeInSeconds.toLong(), newFlightControllerState.aircraftLocation.latitude, newFlightControllerState.aircraftLocation.longitude, (newFlightControllerState.aircraftLocation.altitude*1000).toInt(), (newFlightControllerState.aircraftLocation.altitude*1000).toInt(), (newFlightControllerState.velocityX*100).toShort(), (newFlightControllerState.velocityY*100).toShort(), (newFlightControllerState.velocityZ*100).toShort(), (heading*100).roundToInt())
                var speed = hypot(vx, vy)
                main.mavLink.mavlinkMessageConstructor.vfrHud(speed, speed, heading.toShort(), 0, newFlightControllerState.aircraftLocation.altitude, vz)

            }
        }

    }





    fun notifyStatusChange() {
        main.handler.removeCallbacks(updateRunnable)
        main.handler.postDelayed(updateRunnable, 500)
    }

    private val updateRunnable = Runnable {
        val intent = Intent(FLAG_CONNECTION_CHANGE)
        main.sendBroadcast(intent)
    }

}