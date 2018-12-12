package jb.djilink.dji
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import dji.common.error.DJIError
import dji.sdk.camera.DownloadListener
import dji.sdk.camera.MediaFile


class DownloadListener (var download: DownloadProcess) : DownloadListener<String> {     //Downloadfortschritt-Listener
    var main = download.main


    override fun onFailure(p0: DJIError?) {
        Log.e("Download failure", p0!!.description)

        download.isFailure()
    }

    override fun onProgress(total: Long, current: Long) {       //Update GUI
        main.runOnUiThread {
            main.gui.progressBar.max = total.toInt()
            main.gui.progressBar.progress = current.toInt()
        }
    }

    override fun onRateUpdate(total: Long, current: Long, rate: Long) {  //Update GUI
        main.runOnUiThread {
            main.gui.progressBar.max = total.toInt()
            main.gui.progressBar.progress = current.toInt()
            main.gui.rateView.text = ((current / 1000000).toString() + "/" + (total/1000000).toString() + "MB, " + (rate/1000).toString() + "kB/s")
        }


    }

    override fun onStart() {
        main.runOnUiThread {
            main.gui.progressBar.visibility = View.VISIBLE
            main.gui.rateView.visibility = View.VISIBLE
        }
    }

    override fun onSuccess(file: String) {
        Log.e("onSuccess", "Downloaded " + main.getExternalFilesDir(null).toString() + "/" + download.filename)

        download.isSuccess()


    }
}