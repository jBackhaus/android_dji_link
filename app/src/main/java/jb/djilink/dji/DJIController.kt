package jb.djilink.dji

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Handler
import android.util.Log
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.common.product.Model
import dji.sdk.base.BaseProduct
import dji.sdk.camera.VideoFeeder
import dji.sdk.codec.DJICodecManager
import dji.sdk.sdkmanager.DJISDKManager
import jb.djilink.FLAG_CONNECTION_CHANGE
import jb.djilink.MainActivity

/**
 * Created by jan on 07.12.17.
 */
class DJIController(var main: MainActivity) {       //Stellt die Verbindung zur Drohne her, registiert App


    var drone: DJIDrone? = null
    var codecManager: DJICodecManager? = null
    var sdkManager = DJISDKManager.getInstance()

    var mission = MissionHandler(this)
    lateinit var handler: Handler

    var downloader= DJIDownloadThread(this)




    //Callbacks:



    var djiSDKManagerCallback = object : DJISDKManager.SDKManagerCallback {
        override fun onRegister(error: DJIError?) {                             //Bei Registrierung, GUI-Output
            Log.e("DJISDKManagerCallback",  "onRegister")
            Log.d("DJISDKManagerCallback", if (error == null) {
                "success"
            } else {
                error.description
            })
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct()
                main.showToast("SDK bei DJI registriert.")
                main.debug("SDK bei DJI registriert.")
            } else {
                main.showToast("SDK nicht registriert! Internetverbindung erfolrderlich.")
                main.debug("SDK nicht registriert! Internetverbindung erfolrderlich.")
            }
        }

        override fun onProductChange(oldProduct: BaseProduct?, newProduct: BaseProduct?) { //Ber Verbinden oder Trennen einer Drohne
            var message = "Neues Product: "
            if (oldProduct != null) {
                message += oldProduct.model
            } else {
                message += "null"
            }
            message += " -> "
            if (newProduct != null) {
                message += newProduct.model
            } else {
                message += "null"
            }
            Log.e("DJISDKManagerCallback", message)  //Debug
            main.debug(message)

            if (newProduct == null){
                drone = null
            } else {
                drone = DJIDrone(this@DJIController, newProduct)      //Verbundene Drohne als DJIDrone-Objekt

            }
            notifyStatusChange()
            main.gui.updateTitleBar()
        }
    }




    //init


    init {
        downloader.start()
        sdkManager.registerApp(main, djiSDKManagerCallback)

    }




    fun onDestroy(){
        downloader.quit()
    }

    fun initCodecManager (context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) {      //Livefeed-Initialisierung
        Log.e("DJIDrone",  "initCodecManager")
        codecManager = DJICodecManager(context, surfaceTexture, width, height)
    }




    fun initFPV() {                                 //Lisvebild soll angerufen und angezeigt werden
        Log.e("DJIDrone",  "initFPV")
        if (drone != null && drone!!.product.isConnected && drone!!.product.model != Model.UNKNOWN_AIRCRAFT) {

            main.gui.videoView.surfaceTextureListener = main.gui
            VideoFeeder.getInstance().primaryVideoFeed.callback = VideoFeeder.VideoDataCallback { videoBuffer, size ->
                if (codecManager != null) {
                    //       Log.e(TAG, "sendDataToDecoder")
                    codecManager!!.sendDataToDecoder(videoBuffer, size)
                }
            }
            Log.e("DJIDrone", "VideoCallback installed")


        }
    }



    fun notifyStatusChange() {                                      //Intent, dass sich Drohne verbunden oder getrennt hat
        main.handler.removeCallbacks(updateRunnable)
        main.handler.postDelayed(updateRunnable, 500)
    }

    private val updateRunnable = Runnable {
        val intent = Intent(FLAG_CONNECTION_CHANGE)
        main.sendBroadcast(intent)
    }

}