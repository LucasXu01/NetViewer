**一款用于展示应用所有网络请求参数的工具！[NetViewer-让App的网络请求无所遁形](https://blog.csdn.net/LucasXu01/article/details/127205948)**

## 1、netviewer接入

项目级的build.gradle添加：

```gradle
buildscript {
    dependencies {
    ......
        //1、依赖抓包插件库
        classpath 'io.github.lucasxu01:modular-netviewer-plugin:1.0.1'

    }
}
```

app级别build.gradle:

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    //2、添加netviewer的插件，插件内部会hook到okhttp
    id 'netplugin'  
}
```

app级别build.gradle继续添加:

```arduino
//3、添加依赖
debugImplementation 'implementation 'io.github.lucasxu01:modular-netviewer:1.0.0
```

-备注： 使用debugImplementation是为了只在测试环境中引入

## 2、使用

1. 接入netviewer后，所有经过okhttp的请求均会在控制台进行输出；

2. 如果需要在项目中获取到网络请求具体参数信息，可：

   ```kotlin
    // 在主项目中获取NetworkViewer中的每次网络请求数据monitorData
    NetViewerHelper.setMonitorDataInterface(object  : MonitorDataInterface {
           override fun getMonitorData(monitorData: MonitorData) {
                Log.d("tag", "getMonitorData: ." + monitorData.host)
           }
     })
   ```

3. 不展示log日志：

   ```kotlin
   NetViewerHelper.isPrintNetLog = false
   ```

   

## 3、效果

<img src="https://img-blog.csdnimg.cn/96898f8f89054880b2eeda398cba560a.png" style="zoom:50%;" />

## 4、 proguard混淆

默认已经添加混淆，如遇到问题可以添加如下混淆代码：

~~~go
```
   -keep class com.lucas.netviewer.data.** { *; }
```
~~~

## 5、 温馨提示
```
    虽然netviewer只会在debug环境hook代码，
    但是release版编译的时候还是会走一遍Transform操作（空操作），
    为了保险起见建议生产包禁掉此插件。
    在local.properties中添加monitor.enablePlugin=false，全面禁用monitor插件
```

## 6、 todo

- [ ] 添加配置文件能力
- [x] 配置文件添加日志打印与否，插件禁用与否

