# OkDown
基于okhttp3的文件下载框架。

**feature:**  
1.调用简单，一键下载  
2.支持自定义header，与其他参数详见以下注释  
3.体积小，仅有一个类  
4.回调自动切换主线程。  
5.重复下载判断。  
6.调用优雅。
  
一切用默认值的话，超简单：
```kotlin
DownLoadHttpUtils.getInstance()
        .setActionCallBack({  //可选，回调函数
        //返回文件一共大小
                Log.d(TAG, "total : $it")  
            }, {  
            //返回当前下载进度，Long
                Log.d(TAG, "progress : $it")  
            }, {  
            //下载成功，返回文件本身。File
                Log.d(TAG, "success : $it")  
            }, {  
            //下载错误，返回错误原因
                Log.d(TAG, "error : $it")  
            })  
            .down() //必选，开始下载
```
    
      预览  下了个应用宝。打算下抖音来着。公司那破网下抖音慢，苦恼。
  
  

<img src="https://github.com/Rocketer2018/OkDown/blob/master/Android%20Emulator%20-%20Pixel_2_API_29_5554%202020-05-13%2023-32-12.gif"  alt="guthub by Microsoft  is sb"/>  
      
还可以自定义一些参数。
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
  
  
请确保调用前，具有文件存储权限，网络请求权限。  
  
  

