package jb.djilink.dji

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import dji.sdk.camera.MediaFile
import jb.djilink.DISTANCE_BETWEEN_PHOTOS
import jb.djilink.calculateDistance

/**
 * Created by jan on 05.10.17.
 */

class DJIDownloadThread (djiController: DJIController) :HandlerThread("DJIDownloadThread") {        //Auf diesem Thread laufen die Download-Prozesse der Bilder von der Drohne zum Tablet
    lateinit var downloadHandler: Handler

    var main = djiController.main
    var downloadList: MutableList<MediaFile> = arrayListOf()
    var downloadProcess= DownloadProcess(this)


    override fun onLooperPrepared() {
        super.onLooperPrepared()
        downloadHandler = Handler(looper)
        downloadProcess.handler = downloadHandler

    }





    fun nextDownload(){                         //Nächsten Download starten
        if (!downloadList.isEmpty()){
            downloadHandler.post{
                downloadProcess.initWithNewFile(downloadList.first())
                try {
                    downloadList.removeAt(0)
                } catch (e: Exception){
                    Log.e("nextDownload, remove", e.message + "\nEmptied whole List")
                    var lostimages = ""
                    for (item in downloadList){
                        lostimages += (item.fileName + ", ")
                    }
                    main.debug("Fehler beim Versuch, einzelnes Bild von der Download-Liste zu löschen! Gesamte Liste wurde zurückgesetzt, " + downloadList.size + " Bilder (" + lostimages + ") wurden übersprungen und nicht heruntergeladen!")
                    downloadList = arrayListOf()
                    main.runOnUiThread {
                        main.gui.pendingText.text = downloadList.size.toString()
                    }
                }
            }
        }
    }

    fun trigger() {                 //Startet einen neuen Download, wenn derzeit keiner läuft und Bilddateien warten
        main.runOnUiThread {
            main.gui.pendingText.text = downloadList.size.toString()
        }
        Log.e("Trigger", "downloadInProgress: " + downloadProcess.inProgress + ", downloadList.size: " + downloadList.size.toString())
        if (!downloadProcess.inProgress && !downloadList.isEmpty()){
            downloadHandler.post {
                nextDownload()
            }
        }
    }

    fun addImageToDownloadList(image: MediaFile){       //Bilddatei auf Warteliste setzen
        downloadList.add(image)
        trigger()
    }

}