package com.ukonect.www.exception

import android.R.array
import android.app.Activity
import android.content.Context
import android.os.Build
import com.ukonect.www.util.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets


class UkonectUncaughtExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val LINE_SEPARATOR = "\n"
   // private val FILE_NAME = "ukonect_error_log.txt"

    override fun uncaughtException(
        thread: Thread,
        exception: Throwable
    ) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
        errorReport.append("************ CAUSE OF ERROR ************\n\n")
        errorReport.append(stackTrace.toString())
        errorReport.append("\n************ DEVICE INFORMATION ***********\n")
        errorReport.append("Brand: ")
        errorReport.append(Build.BRAND)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Device: ")
        errorReport.append(Build.DEVICE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Model: ")
        errorReport.append(Build.MODEL)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Id: ")
        errorReport.append(Build.ID)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Product: ")
        errorReport.append(Build.PRODUCT)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("\n************ FIRMWARE ************\n")
        errorReport.append("SDK: ")
        errorReport.append(Build.VERSION.SDK)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Release: ")
        errorReport.append(Build.VERSION.RELEASE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Incremental: ")
        errorReport.append(Build.VERSION.INCREMENTAL)
        errorReport.append(LINE_SEPARATOR)

        Utils.logExternal(context, errorReport.toString())

        /*
        val tempFile = context.filesDir.path.toString() + "/" + FILE_NAME

        try {
            val detailedFile = File(tempFile)
            if (!detailedFile.exists()) detailedFile.createNewFile()
            val stream = FileOutputStream(detailedFile, true)
            stream.write(errorReport.toString().toByteArray())
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }

}