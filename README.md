# OkDown
基于okhttp3的文件下载框架。

**feature:**  
1.调用简单，一键下载  
2.支持自定义header，与其他参数详见以下注释  
3.体积小，仅有一个类  
4.回调自动切换主线程。  
5.重复下载判断。  
6.调用优雅。

    
```kotlin
        val header = HashMap<String, String>()//自定义header  
        header["ua"] = "your dad"
        
        var tvTotal = ""  
        
        //以下方法，标记为必选，在down前必须调用。标记为可选，随意。
        DownLoadHttpUtils.getInstance()
        .addHeader(header)  //可选，自定义header 
        .setReadTImeOut(10L)//可选，自定义超时时间，还有writeTimeout，connectTimeout。默认均为10L
        .setDeleteWhenException(true)//可选,出现异常时，是否删除下载到一半的文件，默认true，删除。
        .initUrl(  
                    "https://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk",  
                    null  
                )//必选，下载链接和参数。参数为map<String,String>类型。  
        .setFilePath(applicationContext.filesDir.absolutePath)//必选，下载路径。  
        .setFileName("app_baby.apk")//可选，存储的下载文件名 
        //kotlin调用方法，优雅
        .setActionCallBack({  //可选，回调函数
                Log.d(TAG, "total : $it")  
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")  
            }, {  
                Log.d(TAG, "progress : $it")  
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")  
            }, {  
                Log.d(TAG, "success : $it")  
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")  
            }, {  
                Log.d(TAG, "error : $it")  
                Log.d(TAG, "Thread : ${Thread.currentThread().name}")  
            })  
            .down() //必选，开始下载
        //非kotlin直接setDownCallBack.
```
一切用默认值的话，超简单：
```kotlin
DownLoadHttpUtils.getInstance()
        .setActionCallBack({  //可选，回调函数
                Log.d(TAG, "total : $it")  
            }, {  
                Log.d(TAG, "progress : $it")  
            }, {  
                Log.d(TAG, "success : $it")  
            }, {  
                Log.d(TAG, "error : $it")  
            })  
            .down() //必选，开始下载
```
