package jb.djilink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.settings.*


class SettingsActivity : View.OnClickListener, TextWatcher, Activity() {

    //zeigt Einstellungsseite

    val TAG = SettingsActivity::class.java.name

    fun initUI() {

        txt_height.setText(ALTITUDE.toString())
        txt_speed.setText(SPEED.toString())
        txt_pitch.setText(PITCH_ANGLE.toString())
        editMLPort.setText(udpPort.toString())

        cbx_full.isChecked = DOWNLOAD_IMAGES

        cbx_tcp.isChecked = TCP_UPLOAD
        cbx_thumbnails.isChecked = SHOW_THUMBNAILS

        editMLPort.text.clear()
        editMLPort.text.append(udpPort.toString())

        editTCPPort.text.clear()
        editTCPPort.text.append(tcpPort.toString())




        if (MAVLINK_IS_CONNECTED){
            txt_MLconnected.text = "MAVLink verbunden mit " + udpIp + ":" + udpPort
            editMLAddress.isEnabled = false
            editMLPort.isEnabled = false
            btn_connect.isEnabled = false
            btn_disconnect.isEnabled = true
        }else {
            txt_MLconnected.text = "MAVLink nicht verbunden"
            editMLAddress.isEnabled = true
            editMLPort.isEnabled = true
            btn_connect.isEnabled = true
            btn_disconnect.isEnabled = false        }

        if (TCP_IS_CONNECTED){
            txt_TCPconnected.text = "TCP verbunden mit " + tcpIp + ":" + tcpPort
            editTCPAddress.isEnabled = false
            editTCPPort.isEnabled = false
            btn_TCPconnect.isEnabled = false
            btn_TCPdisconnect.isEnabled = true
        }else {
            txt_TCPconnected.text = "TCP nicht verbunden"
            editTCPAddress.isEnabled = true
            editTCPPort.isEnabled = true
            btn_TCPconnect.isEnabled = true
            btn_TCPdisconnect.isEnabled = false
        }




        txt_height.addTextChangedListener(this)
        txt_speed.addTextChangedListener(this)
        txt_pitch.addTextChangedListener(this)

        btn_back.setOnClickListener(this)
        btn_connect.setOnClickListener(this)
        btn_disconnect.setOnClickListener(this)
        btn_TCPconnect.setOnClickListener(this)
        btn_TCPdisconnect.setOnClickListener(this)

        updateCheckboxes()

        var filter = IntentFilter()                     //Intents abbonieren
        filter.addAction("TCPERROR")
        registerReceiver(receiver, filter)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        initUI()
    }

    override fun onResume() {
        Log.e(TAG, "onResume")
        super.onResume()
        initUI()
    }

    override fun onPause() {
        Log.e(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.e(TAG, "onStop")
        unregisterReceiver(receiver)
        super.onStop()
    }


    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(txt: Editable?) {
    }



    override fun onClick(v: View) {
        Log.e("onClick", v.id.toString())
        when (v) {
            cbx_full -> {
                DOWNLOAD_IMAGES = cbx_full.isChecked
                if (!DOWNLOAD_IMAGES){
                    TCP_UPLOAD = false
                }
                updateCheckboxes()
            }

            cbx_tcp -> {
                TCP_UPLOAD = cbx_tcp.isChecked
                updateCheckboxes()
            }
            cbx_thumbnails -> {
                SHOW_THUMBNAILS = cbx_thumbnails.isChecked
                updateCheckboxes()
            }
            btn_back -> {

                try {
                    ALTITUDE = txt_height.text.toString().toFloat()
                } catch (e:Exception){}

                try {
                    SPEED = txt_speed.text.toString().toFloat()
                } catch (e:Exception){}

                try {
                    val pitch = txt_pitch.text.toString().toInt()
                    if (pitch >= -90 && pitch <= 30) {
                        PITCH_ANGLE = pitch
                    }
                } catch (e:Exception){}

                sendBroadcast(Intent("SETTINGS_CHANGED"))
                finish()
            }

            btn_connect -> {

                udpIp = editMLAddress.text.toString()
                udpPort = editMLPort.text.toString().toInt()
                sendBroadcast(Intent("CONNECT"))

                txt_MLconnected.text = "MAVLink verbunden mit " + udpIp + ":" + udpPort
                editMLAddress.isEnabled = false
                editMLPort.isEnabled = false
                btn_connect.isEnabled = false
                btn_disconnect.isEnabled = true

            }
            btn_disconnect -> {


                txt_MLconnected.text = "MAVLink nicht verbunden"
                editMLAddress.isEnabled = true
                editMLPort.isEnabled = true
                btn_connect.isEnabled = true
                btn_disconnect.isEnabled = false

                sendBroadcast(Intent("DISCONNECT"))


            }
            btn_TCPconnect -> {

                tcpIp = editTCPAddress.text.toString()
                tcpPort = editTCPPort.text.toString().toInt()




                sendBroadcast(Intent("TCPCONNECT"))

                txt_TCPconnected.text = "TCP verbunden mit " + tcpIp + ":" + tcpPort
                editTCPAddress.isEnabled = false
                editTCPPort.isEnabled = false
                btn_TCPconnect.isEnabled = false
                btn_TCPdisconnect.isEnabled = true
                cbx_tcp.isEnabled = true

            }
            btn_TCPdisconnect -> {


                txt_TCPconnected.text = "TCP nicht verbunden"
                editTCPAddress.isEnabled = true
                editTCPPort.isEnabled = true
                btn_TCPconnect.isEnabled = true
                btn_TCPdisconnect.isEnabled = false
                cbx_tcp.isEnabled = false


                sendBroadcast(Intent("TCPDISCONNECT"))


            }
            else -> {
            }
        }
    }

    fun updateCheckboxes(){
        cbx_full.isChecked = DOWNLOAD_IMAGES
        cbx_tcp.isChecked = TCP_UPLOAD
        cbx_thumbnails.isChecked = SHOW_THUMBNAILS

        cbx_tcp.isEnabled = (DOWNLOAD_IMAGES && TCP_IS_CONNECTED)

    }

    var receiver = object : BroadcastReceiver() {       //Reaktion auf Intents
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {
                when (p1.action){

                    "TCPERROR" -> {
                        txt_TCPconnected.text = "TCP nicht verbunden"
                        editTCPAddress.isEnabled = true
                        editTCPPort.isEnabled = true
                        btn_TCPconnect.isEnabled = true
                        btn_TCPdisconnect.isEnabled = false
                        TCP_UPLOAD = false
                        updateCheckboxes()
                    }


                }
            }
        }
    }

}