package com.ok.down

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.*
import java.util.concurrent.TimeUnit


/**
 * @ClassName DownLoadHttpUtils
 * @Description 下载工具类
 * @Author 许文奇
 * @Date 2020/5/12 18:15
 * @Version 1.0
 */
class DownLoadHttpUtils private constructor() {
    companion object {
        private const val TAG = "LoggingInterceptor"
        private val downLoadHttpUtils: DownLoadHttpUtils by lazy {
            DownLoadHttpUtils()
        }

        @JvmStatic
        @Synchronized
        fun getInstance(): DownLoadHttpUtils {
            return downLoadHttpUtils
        }
    }

    private var buffSize = 2048//建议设置为2048
    fun setBuffSize(size: Int): DownLoadHttpUtils {
        this.buffSize = size
        return this
    }

    private var interceptor: Interceptor? = null
    fun setInterceptro(interceptor: Interceptor?): DownLoadHttpUtils {
        this.interceptor = interceptor
        return this
    }

    private var readTimeOut = 10L
    fun setReadTImeOut(read: Long): DownLoadHttpUtils {
        this.readTimeOut = read
        return this
    }

    private var writeTimeout = 10L
    fun setWriteTimeOut(write: Long): DownLoadHttpUtils {
        this.writeTimeout = write
        return this
    }

    private var connectTimeout = 10L
    fun setConnectTimeOut(connect: Long): DownLoadHttpUtils {
        this.connectTimeout = connect
        return this
    }

    private var filePath = ""
    fun setFilePath(path: String): DownLoadHttpUtils {
        this.filePath = path
        return this
    }

    private var fileName = ""
    fun setFileName(name: String): DownLoadHttpUtils {
        this.fileName = name
        return this
    }

    private var deleteWhenException = true
    fun setDeleteWhenException(dele: Boolean): DownLoadHttpUtils {
        this.deleteWhenException = dele
        return this
    }

    private val requestBuilder: Request.Builder = Request.Builder()
    private var urlBuilder: HttpUrl.Builder? = null

    private val okHttpClient = lazy {
        OkHttpClient.Builder()
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .addInterceptor(interceptor ?: LoggingInterceptor())
            .build()
    }
    private var actionGetTotal: (total: Long) -> Unit? = { _ -> }
    private var actionProgress: (position: Long) -> Unit? = { _ -> }
    private var actionSuccess: (file: File) -> Unit? = { _ -> }
    private var actionFail: (msg: String) -> Unit? = {}

    fun setActionCallBack(
        actionGetTotal: (total: Long) -> Unit,
        actionProgress: (position: Long) -> Unit,
        actionSuccess: (file: File) -> Unit,
        actionFail: (msg: String) -> Unit
    ): DownLoadHttpUtils {
        this.actionGetTotal = actionGetTotal
        this.actionProgress = actionProgress
        this.actionSuccess = actionSuccess
        this.actionFail = actionFail

        return this
    }

    private var downCallBack: DownCallBack? = null
    fun setDownCallBack(callBack: DownCallBack): DownLoadHttpUtils {
        this.downCallBack = callBack
        return this
    }

    fun initUrl(url: String, params: Map<String, String>?): DownLoadHttpUtils {
        urlBuilder = url.toHttpUrlOrNull()?.newBuilder()
        if (params.isNullOrEmpty()) {
            return this
        }
        for ((k, v) in params) {
            checkName(k)
            urlBuilder?.setQueryParameter(k, v)
        }
        return this
    }

    fun addHeader(map: Map<String, String>): DownLoadHttpUtils {
        requestBuilder.headers(map.toHeaders())
        return this
    }

    private fun checkName(name: String) {
        require(name.isNotEmpty()) { "name is empty" }
    }

    fun down() {
        if (urlBuilder == null) {
            throw IllegalStateException("url not init")
        } else {
            doDown()
        }
    }

    var file: File? = null
    private fun doDown() {
        val startTime = System.currentTimeMillis()
        Log.i(TAG, "startTime=$startTime")
        val url = urlBuilder?.build()
        if (url == null) {
            doException("url is null")
            return
        }
        if (isDowning(filePath + fileName)) {
            return
        }
        val request = requestBuilder.url(url).tag(filePath + fileName).build()

        okHttpClient.value.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                doException(e.toString())
                Log.i(TAG, "download failed")
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                val buf = ByteArray(buffSize)
                var len: Int
                var fos: FileOutputStream? = null

                try {
                    file = if (fileName.isNullOrEmpty()) {
                        File(filePath)
                    } else {
                        val fileDir = File(filePath)
                        if (!fileDir.exists() || !fileDir.isDirectory) {
                            fileDir.mkdirs()
                        }
                        File(filePath, fileName)
                    }

                    if (file == null) {
                        doException("file create err,not exists")
                        return
                    }

                    `is` = response.body?.byteStream() ?: FileInputStream("")
                    val total = response.body?.contentLength() ?: 0
                    doGetTotal(total)

                    fos = FileOutputStream(file!!)
                    var sum: Long = 0
                    while (`is`.read(buf).also { len = it } != -1) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        Log.e(TAG, "download progress : $sum")
                        doProgress(sum)
                    }
                    fos.flush()
                    Log.e(TAG, "download success")
                    if (file == null || file?.exists() == false) {
                        doException("file create err,not exists")
                        return
                    } else {
                        doSuccess(file)
                    }
                    file = null
                    Log.e(TAG, "totalTime=" + (System.currentTimeMillis() - startTime))
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (deleteWhenException && file?.exists() == true) {
                        file?.delete()
                    }
                    doException(e.message.toString())
                    Log.e(TAG, "download failed : " + e.message)
                } finally {
                    try {
                        `is`?.close()
                    } catch (e: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (e: IOException) {
                    }
                    file = null
                }
            }
        })
    }

    private fun isDowning(tag: String): Boolean {
        for (call in okHttpClient.value.dispatcher.runningCalls()) {
            if (call.request().tag() == tag) {
                return true
            }
        }
        return false
    }

    private fun doException(err: String) {
        runOnUiThread {
            if (downCallBack == null) {
                actionFail.invoke(err)
            } else {
                downCallBack?.fail(err)
            }
            mainThread = null
        }
    }

    private fun doSuccess(file: File?) {
        runOnUiThread {
            if (file == null) {
                doException("file not exit")
            } else {
                if (downCallBack == null) {
                    actionSuccess.invoke(file)
                } else {
                    downCallBack?.success(file)
                }
            }
            mainThread = null
        }
    }

    private fun doGetTotal(total: Long) {
        runOnUiThread {
            if (downCallBack == null) {
                actionGetTotal.invoke(total)
            } else {
                downCallBack?.total(total)
            }
        }
    }

    private fun doProgress(progress: Long) {
        runOnUiThread {
            if (downCallBack == null) {
                actionProgress.invoke(progress)
            } else {
                downCallBack?.progress(progress)
            }
        }
    }

    private var mainThread: Handler? = null
    private fun runOnUiThread(action: () -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) { // If we finish marking off of the main thread, we need to
            // actually do it on the main thread to ensure correct ordering.
            if (mainThread == null) {
                mainThread = Handler(Looper.getMainLooper())
            }
            mainThread?.post {
                action.invoke()
            }
            return
        }
        action.invoke()
    }


    class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val startTime = System.nanoTime()
            Log.d(
                TAG, String.format(
                    "Sending request %s on %s%n%s",
                    request.url, chain.connection(), request.headers
                )
            )
            val response: Response = chain.proceed(request)
            val endTime = System.nanoTime()
            Log.d(
                TAG, String.format(
                    "Received response for %s in %.1fms%n%s",
                    response.request.url, (endTime - startTime) / 1e6, response.headers
                )
            )
            return response
        }

        companion object {
            private const val TAG = "LoggingInterceptor"
        }
    }

    interface DownCallBack {
        fun success(file: File)
        fun fail(str: String)
        fun progress(position: Long)
        fun total(total: Long)
    }
}