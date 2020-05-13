# OkDown
okhttp3的文件下载。  
feature:  
1、调用简单，一键下载  
2、支持自定义header，与其他参数  
3、体积小，仅有一个类  
4、回调自动切换主线程。  
5、重复下载判断。  
6、调用优雅。  
<code>val header = HashMap<String, String>()
        header["ua"] = "your dad"
        var tvTotal = ""
        btn_down.setOnClickListener {
            DownLoadHttpUtils.getInstance().setActionCallBack({
                tvTotal = it.toString()
                Log.d(TAG, "total : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                tv_progress.text = "$it/$tvTotal"
                Log.d(TAG, "progress : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                tv_progress.text = "$it"
                Log.d(TAG, "success : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            }, {
                Log.d(TAG, "error : $it")
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")
            })
                .addHeader(header)
                .initUrl(
                    "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk",
                    null
                )
                .setFilePath(applicationContext.filesDir.absolutePath)
                .setFileName("app_baby.apk")//皮
                .down()
        }
        </code>
