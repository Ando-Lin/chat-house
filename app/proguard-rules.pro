#壓縮比，預設5不修改
-optimizationpasses 5

#不使用大小寫混合，混淆後類名稱為小寫
-dontusemixedcaseclassnames

#指定不去忽略公開的 publicli classes
-dontskipnonpubliclibraryclasses

#混淆後產生印射文件
-verbose

#註解此行，可以自動上傳 mapping 檔到 Firebase
#-printmapping mapping.txt

#保留泛型
-keepattributes Signature

# 不做預校驗，加速建置速度
-dontpreverify

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 堆栈还原
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# 保留异常
-keep class com.ando.chathouse.exception.**

# 保留 android-support
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
# androidx
-keep class androidx.** {*;}
-keep interface androidx.** {*;}
-keep public class * extends androidx.**
-dontwarn androidx.**
# okhttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
# retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
# moshi
-keepattributes *Annotation*
-keepclassmembers class com.ando.chathouse.domain.pojo.** {
  @com.squareup.moshi.* <methods>;
}
-keep class com.squareup.moshi.** { *; }
# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}