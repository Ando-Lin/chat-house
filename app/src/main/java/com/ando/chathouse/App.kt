package com.ando.chathouse

import android.app.Application
import android.content.Context
import android.text.TextUtils
import androidx.datastore.preferences.preferencesDataStore
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import dagger.hilt.android.HiltAndroidApp
import okio.IOException
import java.io.BufferedReader
import java.io.FileReader


@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //设置策略
        val processName = getProcessName(android.os.Process.myPid())
        val strategy = UserStrategy(applicationContext)
        strategy.isUploadProcess = processName==null|| processName == packageName
        //初始化崩溃报告
        CrashReport.initCrashReport(this.applicationContext, strategy)
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName: String = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                if (reader != null) {
                    reader.close()
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }
}

val Context.profile by preferencesDataStore(name = "profile")

//val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//
//}