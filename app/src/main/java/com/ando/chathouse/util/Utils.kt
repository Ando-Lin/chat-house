package com.ando.chathouse.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.ando.chathouse.ui.component.SnackbarUI
import com.skydoves.cloudy.internals.render.RenderScriptToolkit
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.runBlocking
import okio.FileNotFoundException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object Utils {
    fun copyFile(context: Context, sourceUri: Uri?, destFile: File): Uri? {
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        sourceUri ?: return null
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        var newUri: Uri?
        try {
            inputStream = context.contentResolver.openInputStream(sourceUri)
            outputStream = destFile.outputStream()
            inputStream?.copyTo(outputStream)
            newUri = destFile.toUri()
        } catch (e: Exception) {
            Log.i(TAG, "copyPictureToPrivateFolder: ", e)
            newUri = null
        } finally {
            outputStream?.close()
            inputStream?.close()
        }
        return newUri
    }

    fun blur(context: Context, uri: Uri?): Bitmap? {
        uri ?: return null
        try {
            with(context.contentResolver.openInputStream(uri)) {
                this ?: return@with
                val bitmap = BitmapFactory.decodeStream(this)
                val maxSide = 500
                var width = bitmap.width
                var height = bitmap.height
                if (bitmap.width > 500 || bitmap.height > 500) {
                    width =
                        if (bitmap.width > bitmap.height) maxSide else maxSide * bitmap.width / bitmap.height
                    height = width * bitmap.height / bitmap.width
                }
                val downScale = bitmap.scale(width = width, height = height)
                return RenderScriptToolkit.blur(downScale, 10)
            }
        } catch (e: FileNotFoundException) {
            runBlocking {
                SnackbarUI.showMessage("图片文件未找到")
            }
        } catch (e: Exception) {
            CrashReport.postCatchedException(e)
            e.printStackTrace()
            runBlocking {
                SnackbarUI.showMessage("图片模糊处理失败:${e.localizedMessage}")
            }
        }

        return null
    }

    fun getStringBuilderSaver(): Saver<MutableState<StringBuilder>, Any> =
        mapSaver(
            save = {
                mapOf("string" to it.toString())
            },
            restore = {
                mutableStateOf(StringBuilder(it["string"] as String))
            }
        )


    inline fun <R> computeTime(tag: String, message: String = "", block: () -> R): R {
        val start = System.currentTimeMillis()
        val value = block()
        val end = System.currentTimeMillis()
        Log.i(tag, "$message time: ${end - start}")
        return value
    }

    private const val TAG = "Utils"
}