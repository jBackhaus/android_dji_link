package jb.djilink.dji
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.os.Handler
import android.util.Log
import android.view.View
import dji.common.error.DJIError
import dji.sdk.camera.MediaFile
import jb.djilink.*
import java.lang.System.currentTimeMillis


class DownloadProcess(var downloadThread: DJIDownloadThread) {      //Verwaltet Doenload und Logging-Daten

    var main = downloadThread.main
    lateinit var handler: Handler       //Handler des DownloadThreads, wird verknüpft, sobald er existiert


    var logger = downloadThread.main.logger
    var file: MediaFile? = null
    var filename = ""
    var size = 0L
    var startTime = 0L
    var endTime = 0L
    var distance = 0f
    var success = 0

    var tries = 0
    var inProgress = false


    fun initWithNewFile(newFile: MediaFile){        //neuen Download und Logging initialisieren
        file = newFile
        size = newFile.fileSize
        filename = newFile.fileName
        startTime = 0
        endTime = 0
        distance = 0f
        success = 0
        tries = 0

        start()
    }




    fun start(){                //Download starten (auf DownloadThread)
        if (file!= null){
            inProgress = true
            handler.post {
                if (file != null) {
                    startTime = currentTimeMillis()
                    if (main.djiController.drone!!.flightControllerState != null && main.gpsTracker.location != null) {
                        distance = calculateDistance(main.djiController.drone!!.flightControllerState!!.aircraftLocation, main.gpsTracker.location!!)
                    } else {
                        distance = 0.toFloat()
                    }
                    file!!.fetchFileData(main.storageDir, null, DownloadListener(this))
                }
            }
            handler.postDelayed({           //Timer für Abbruch des Downloads nach bestimmter Zeit (Timeout)
                isFailure()
            }, DOWNLOAD_MAX_TIME)
        }else{
            Log.e("DownloadProcess, start", "No file is set to Download")
        }

    }

    fun retry(){            //Download mit selbem Bild neustarten
        startTime = 0
        endTime = 0
        distance = 0f

        if (file != null) {
            file!!.stopFetchingFileData { djiError: DJIError? ->
                if (djiError != null){
                    Log.e("stopFetchingFileData", djiError.description)
                }
            }

            handler.postDelayed({start()}, 2000)
        }
    }

    fun quitDownload(){             //Download dieses Bildes beenden
        file = null
        inProgress = false
        downloadThread.trigger()

    }

    fun isFailure(){                    //Fehlschlag. Loggen und ggf. neustarten, wenn unterhalb Anzahl der max. Versuche
        handler.removeCallbacksAndMessages(null)
        endTime = currentTimeMillis()
        success = 0
        logger.writeLine(filename, size, startTime, endTime, distance, success)
        tries += 1
        main.runOnUiThread {
            main.gui.progressBar.visibility = View.INVISIBLE
            main.gui.rateView.visibility = View.INVISIBLE
        }
        if (tries < MAX_DOWNLOAD_TRIES){
            retry()
        } else {
            main.debug(filename + " konnte nicht heruntergeladen werden. Wird übersprungen.\n\nManuell laden!")
            main.showToast(filename + " konnte nicht heruntergeladen werden. Wird übersprungen.\n\nManuell laden!")
            quitDownload()
        }
    }

    fun isSuccess(){        //Erfolgreich heruntergeladen. Loggen, ggf. Upload per SMB oder Dropbox einleiten, nächstes Bild herunterladen
        handler.removeCallbacksAndMessages(null)
        endTime = currentTimeMillis()
        success = 1
        logger.writeLine(filename, size, startTime, endTime, distance, success)

        main.runOnUiThread {
            main.gui.progressBar.visibility = View.INVISIBLE
            main.gui.rateView.visibility = View.INVISIBLE
        }


        if (TCP_UPLOAD){
            main.tcpConnection.addImageToList(filename)
        }

        if (SHOW_THUMBNAILS){
            main.fileAccessor.getMetas(filename)
        }
        quitDownload()
    }


}






