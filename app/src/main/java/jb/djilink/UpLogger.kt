package jb.djilink

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * Created by jan on 14.11.17.
 */
class UpLogger(var main: MainActivity) {        //Logger für den SMB-Upload
    var directory: String? = null
    var file: File? = null
    var outStream: FileOutputStream? = null



    init {

    }

    fun newFile () {        //Neuer Log bei Programmstart oder neuer Mission
        try {
            directory = main.getExternalFilesDir(null).toString()
            val calendar = Calendar.getInstance()
            val filename = minString(calendar.get(Calendar.YEAR).toString(), 4, "0") + "-" + minString(calendar.get(Calendar.MONTH).toString(), 2, "0") + "-" + minString(calendar.get(Calendar.DAY_OF_MONTH).toString(),2, "0") + "_" + minString(calendar.get(Calendar.HOUR_OF_DAY).toString(),2, "0") + "-" + minString(calendar.get(Calendar.MINUTE).toString(), 2, "0") + "-" + minString(calendar.get(Calendar.SECOND).toString(), 2, "0")

            file = File(directory + "/uplog_" + filename + ".csv")
            file!!.createNewFile()
            Log.e("newUpLogFile", "New Uplog created")

        } catch (e: Exception){
            main.showToast("Error creating Uplog file")
            Log.e("newUpLogFile", e.message)
        }
    }



    fun writeLine(filename: String, size: Long, startTime: Long, endTime: Long, success: Int){      //Log-Zeile schreiben
        if (file == null){
            newFile()
        }




        if (file != null) {
            outStream = FileOutputStream(file!!.absoluteFile, true)
            var writer = OutputStreamWriter(outStream)
            var line = filename + "," + size.toString() + "," + startTime.toString() + "," + endTime.toString() + "," + success.toString() + "\n"
            try {
                writer.write(line)
                writer.flush()
                Log.e("writeUpLogLine", "UpLine written")

            } catch (e: Exception) {
                main.showToast("Error writing uplog to file")
                Log.e("writeUpLogLine", e.message)
            }
        }
    }

    fun closeFile() {       //Log-Datei schließen
        try {
            if (outStream != null){
                outStream!!.close()
                file = null
                Log.e("closeUpLogFile", "File closed")

            }
        }catch (e: Exception){
            main.showToast("Error closing Uplog file")
            Log.e("closeUpLogFile", e.message)
        }
    }

    fun newMission(){
        if (file != null){
            closeFile()
        }
        newFile()
    }
}