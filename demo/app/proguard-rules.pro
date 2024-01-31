# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.esandinfo.esfaceid.jni.FaceDetectResult { *; }
-keep class com.esandinfo.esfaceid.jni.Image { *; }
-keep class com.esandinfo.esfaceid.jni.JniFaceCut { *; }
-keep class com.esandinfo.esfaceid.certificate.EsFaceIDCertificate { *; }
-keep class com.esandinfo.esfaceid.utils.ECC256 { *; }
-keep class com.esandinfo.esfaceid.certificate.EsSign { *; }
-keep class com.esandinfo.esfaceid.certificate.CertEntity { *; }
# 保证uniapp 插件暴露
-keep class com.esandinfo.esfaceid.uniapp.** { *; }

# uniapp 反射需要
-keep class com.esandinfo.esfaceid.db.Feature { *;}
-keep class com.esandinfo.esfaceid.bean.** { *;}
-keep class com.esandinfo.esfaceid.constants.** { *;}
-keep class com.esandinfo.esfaceid.view.EsCameraTextureView { *;}
-keep class com.esandinfo.esfaceid.view.PositionConfig { *;}

# 证书解密需要用到
-keep class com.esandinfo.esfaceid.utils.AppInfoUtil { *;}
-keep class com.esandinfo.esfaceid.utils.EsFaceIDResultFactory { *;}
-keep class com.esandinfo.esfaceid.EsFaceIDManager { public <methods>;}
-keep class com.esandinfo.esfaceid.EsOnVerifyCallback { public <methods>;}

# RS sdk jni类，必须保留，不然程序会崩溃
-keep class readsense.** { *;}

# 杂项
-keep class com.esandinfo.esfaceid.utils.AppExecutors { *;}
-keep class com.esandinfo.esfaceid.utils.ImageUtils { *;}
-keep class com.esandinfo.esfaceid.utils.MyLog { *;}
-keep class com.esandinfo.esfaceid.activity.** { *;}

-keep class android.content.Context { *; }
-keep class com.esandinfo.esfaceid.bean.EsFaceIDResult { *; }
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

