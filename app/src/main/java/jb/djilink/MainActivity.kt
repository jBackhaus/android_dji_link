package jb.djilink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.app.FragmentActivity

import android.os.*
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.support.multidex.MultiDex
import android.widget.Toast
import jb.djilink.dji.DJIController
import jb.djilink.mavlink.MavLinkConnection
import kotlinx.android.synthetic.main.layout.*
import java.io.File
import android.text.method.ScrollingMovementMethod
import jb.djilink.mavlink.MavlinkHomePosition
import jb.djilink.mavlink.TCPConnection


class MainActivity : FragmentActivity() {



    lateinit var djiController: DJIController
    var gui = GUI(this)                         //GUI initialisieren
    var gpsTracker = GPSTracker(this)           //GPS starten
    var logger = Logger(this)                   //Logger starten
    var uplogger = UpLogger(this)               //TCP-Logger starten
    var mavLink = MavLinkConnection(this)       //MAVLink-Verbindung initialisieren

    lateinit var handler: Handler
    lateinit var tcpConnection: TCPConnection
    lateinit var fileAccessor: FileAccessorThread


    var PANIC = false
    var homePosition = MavlinkHomePosition()


    lateinit var storageDir: File                       //Speicherort für Bilder und Logs auf dem Tablet (geht erst zur Laufzeit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_PHONE_STATE), 1)
        }
        setContentView(R.layout.layout)
        djiController = DJIController(this)     //DJI-SDK-Manager starten, registrieren

        storageDir = File(this.getExternalFilesDir(null).toURI())  //Speicherort für Bilder und Logs auf dem Tablet


        var filter = IntentFilter()                     //Intents abbonieren
        filter.addAction("SETTINGS_CHANGED")
        filter.addAction("CONNECT")
        filter.addAction("DISCONNECT")
        filter.addAction("TCPCONNECT")
        filter.addAction("TCPDISCONNECT")
        registerReceiver(receiver, filter)

        gui.initUI()                                //GUI zur Laufzeit verändern
        textView2.movementMethod = ScrollingMovementMethod()

        gpsTracker.startUsingGPS()                  //GPS-Positionssuche starten


        try {
            tcpConnection = TCPConnection(this)     //SMB-Thread starten
            tcpConnection.start()
        }catch (e: Exception){
            Log.e("TCPConnection init", e.message)
            e.printStackTrace()
        }

        try {
            fileAccessor = FileAccessorThread(this)     //SMB-Thread starten
            fileAccessor.start()
        }catch (e: Exception){
            Log.e("FileAccessor init", e.message)
            e.printStackTrace()
        }



    }


    override fun onResume() {
        Log.e(TAG, "onResume")
        super.onResume()
        gui.updateTitleBar()
        gui.redrawMap()
    }

    override fun onDestroy() {              //Alle HandlerThreads beenden und Intents abmelden
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        unregisterReceiver(receiver)
        djiController.onDestroy()
        gpsTracker.stopUsingGPS()
        logger.closeFile()
        uplogger.closeFile()
        tcpConnection.quitSafely()
        fileAccessor.quitSafely()
    }


    override fun onPause() {
        Log.e(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.e(TAG, "onStop")
        super.onStop()
    }



    override fun attachBaseContext(base: Context) {         //MultiDex wird vom DJI SDK benötigt
        Log.e(TAG,  "attachBaseContext")
        super.attachBaseContext(base)
        Log.e(TAG, "installing MultiDex")
        MultiDex.install(this)
    }

    companion object {
        private val TAG = MainActivity::class.java.name
    }

    fun showToast(msg: String) {        //Einblendungen per Toast
        this.runOnUiThread{ Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }

    fun debug(text: String){            //Einblendungen im Debug.Fenster
        runOnUiThread {
            textView2.text = text + "\n\n" + textView2.text
        }
    }

    var receiver = object : BroadcastReceiver() {       //Reaktion auf Intents
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {
                when (p1.action){

                    "SETTINGS_CHANGED" ->{
                        updateAfterSettings()
                    }
                    "CONNECT" -> {
                        mavLink.connect()
                        debug("Connect UDPs")

                    }
                    "DISCONNECT" -> {
                        mavLink.disconnect()
                    }
                    "TCPCONNECT" -> {
                        tcpConnection.connect(tcpIp, tcpPort)
                    }
                    "TCPDISCONNECT" -> {
                        tcpConnection.disconnect()
                    }

                }
            }
        }
    }

    fun updateAfterSettings(){      //Kamera der Drohne an Neigungswinkel aus den Settings anpassen

            try {
                if (djiController.drone != null) {
                    djiController.drone!!.setGimbalAngleAndFocus()
                }
            } catch (e: Exception) {
                Log.e("updateAfterSettings", e.message)
                showToast("updateAfterSettingsFailed")
            }
    }


}




