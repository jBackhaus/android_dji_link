package jb.djilink
/**
 *Created by Jan Backhaus on 12.12.18 as part of the project android_dji_link.
 */
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*


class Logger(var main: MainActivity) {
    var directory: String? = null
    var file: File? = null
    var outStream: FileOutputStream? = null



    init {

    }

    fun newFile () {            //Neue Datei bei Programmstart und neuer Mission
        try {
            directory = main.getExternalFilesDir(null).toString()
            val calendar = Calendar.getInstance()
            val filename = minString(calendar.get(Calendar.YEAR).toString(), 4, "0") + "-" + minString(calendar.get(Calendar.MONTH).toString(), 2, "0") + "-" + minString(calendar.get(Calendar.DAY_OF_MONTH).toString(),2, "0") + "_" + minString(calendar.get(Calendar.HOUR_OF_DAY).toString(),2, "0") + "-" + minString(calendar.get(Calendar.MINUTE).toString(), 2, "0") + "-" + minString(calendar.get(Calendar.SECOND).toString(), 2, "0")

            file = File(directory + "/log_" + filename + ".csv")
            file!!.createNewFile()
            Log.e("newLogFile", "New log created")

        } catch (e: Exception){
            main.showToast("Error creating log file")
            Log.e("newLogFile", e.message)
        }
    }



    fun writeLine(filename: String, size: Long, startTime: Long, endTime: Long, distance: Float, success: Int){
        if (file == null){
            newFile()
        }
        var duration = 0f
        var kbps = 0f

        if (startTime != 0L && endTime != 0L){
            duration = ((endTime.toDouble() - startTime.toDouble()) / 1000).toFloat()
            if (size != 0L){
                kbps = (size.toDouble() / duration / 1000).toFloat()
            }
        }

        if (file != null) {
            outStream = FileOutputStream(file!!.absoluteFile, true)
            var writer = OutputStreamWriter(outStream)
            var line = filename + "," + size.toString() + "," + startTime.toString() + "," + endTime.toString() + "," + duration.toString() + "," + kbps.toString() + "," + distance.toString() + "," + success.toString() + "\n"
            try {
                writer.write(line)
                writer.flush()
                Log.e("writeLogLine", "Line written")

            } catch (e: Exception) {
                main.showToast("Error writing log to file")
                Log.e("writeLogLine", e.message)
            }
        }
    }

    fun closeFile() {
        try {
            if (outStream != null){
                outStream!!.close()
                file = null
                Log.e("closeLogFile", "File closed")

            }
        }catch (e: Exception){
            main.showToast("Error closing log file")
            Log.e("closeLogFile", e.message)
        }
    }

    fun newMission(){
        if (file != null){
            closeFile()
        }
        newFile()
    }
}