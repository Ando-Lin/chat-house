package com.ando.tastechatgpt.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

object Utils {
    fun copyFile(context: Context, sourceUri: Uri?, destFile: File): Uri?{
        if (!destFile.exists()){
            destFile.createNewFile()
        }
        sourceUri?:return null
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        var newUri: Uri?
        try {
            inputStream = context.contentResolver.openInputStream(sourceUri)
            outputStream = destFile.outputStream()
            inputStream?.copyTo(outputStream)
            newUri = destFile.toUri()
        }catch (e:Exception){
            Log.i(TAG, "copyPictureToPrivateFolder: ", e)
            newUri = null
        }finally {
            outputStream?.close()
            inputStream?.close()
        }
        return newUri
    }

    private const val TAG = "Utils"
}