# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 保留行号信息，有助于调试堆栈跟踪
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit混淆规则
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp混淆规则
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson混淆规则
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes Signature
-keepattributes *Annotation*

# 保留ApiResponse类不被混淆
-keep class com.paijiantuan.UNID2693B0.model.ApiResponse { 
    <fields>;
    <methods>;
}
-keep class com.paijiantuan.UNID2693B0.model.ApiResponse$* { 
    <fields>;
    <methods>;
}

# 保留ApiResponse类的getData和setData方法
-keepclassmembers class com.paijiantuan.UNID2693B0.model.ApiResponse { 
    java.lang.Object getData();
    void setData(java.lang.Object);
}

# 保留应用中的模型类不被混淆
-keep class com.paijiantuan.UNID2693B0.model.** { *; }
-keep class com.paijiantuan.UNID2693B0.entity.** { *; }
-keep class com.paijiantuan.UNID2693B0.bean.** { *; }

# 保留SharedPreference相关代码不被混淆
-keep class com.paijiantuan.UNID2693B0.utils.SharedPreferenceUtil { *; }
-keep class com.paijiantuan.UNID2693B0.utils.StorageUtil { *; }

# 保留ApiManager中的AnswerStats内部类不被混淆
-keep class com.paijiantuan.UNID2693B0.api.ApiManager$AnswerStats { 
    <fields>;
    <methods>;
}

# 保留ApiManager中的ApiCallback接口不被混淆
-keep interface com.paijiantuan.UNID2693B0.api.ApiManager$ApiCallback { 
    <methods>;
}

# 保留QuizActivity中的关键方法不被混淆
-keepclassmembers class com.paijiantuan.UNID2693B0.QuizActivity { 
    void updateAnswerStats(int, int);
    void loadUserAnswerStats();
    void submitAnswerToServer(int, int, int, com.paijiantuan.UNID2693B0.api.ApiManager$ApiCallback);
}

# 保留SettingActivity中的关键方法不被混淆
-keepclassmembers class com.paijiantuan.UNID2693B0.SettingActivity { 
    void updateAnswerStats(int, int);
    void loadUserAnswerStats();
}

# 保留ApiManager中的关键方法不被混淆
-keepclassmembers class com.paijiantuan.UNID2693B0.api.ApiManager { 
    void getUserAnswerStats(com.paijiantuan.UNID2693B0.api.ApiManager$ApiCallback);
    void saveUserInfo(com.paijiantuan.UNID2693B0.model.UserInfo);
}

# Taku SDK混淆配置
-keep public class com.anythink.**
-keepclassmembers class com.anythink.** {
   *;
}

-keep public class com.anythink.network.**
-keepclassmembers class com.anythink.network.** {
   public *;
}

-dontwarn com.anythink.hb.**
-keep class com.anythink.hb.**{ *;}

-dontwarn com.anythink.china.api.**
-keep class com.anythink.china.api.**{ *;}

-keep class com.anythink.myoffer.ui.**{ *;}
-keepclassmembers public class com.anythink.myoffer.ui.** {
   public *;
}

# 完善腾讯广告SDK(GDT)混淆规则 - 解决工厂类实例化失败问题
-keep class com.qq.e.** {*;}
-keep interface com.qq.e.** {*;}
-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-dontwarn com.qq.e.**

# 特别保留工厂类和接口，防止实例化失败
-keep class com.qq.e.comm.pi.POFactory {*;}
-keep class com.qq.e.comm.pi.** {*;}
-keep class com.qq.e.comm.plugin.POFactoryImpl {*;}

# 保留内部类和匿名类
-keepclassmembers class com.qq.e.** {
    *;
}
-keepclassmembers class com.tencent.** {
    *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.widget.** {*;}
-dontwarn com.vivo.secboxsdk.**
-keep class com.vivo.secboxsdk.SecBoxCipherException { *; }
-keep class com.vivo.secboxsdk.jni.SecBoxNative { *; }
-keep class com.vivo.secboxsdk.BuildConfig { *; }
-keep class com.vivo.advv.**{*;}
-keep class com.kwad.sdk.** { *;}
-keep class com.ksad.download.** { *;}
-keep class com.kwai.filedownloader.** { *;}
# sdk
-keep class com.bun.miitmdid.** { *; }
-keep interface com.bun.supplier.** { *; }
# asus
-keep class com.asus.msa.SupplementaryDID.** { *; }
-keep class com.asus.msa.sdid.** { *; }
# freeme
-keep class com.android.creator.** { *; }
-keep class com.android.msasdk.** { *; }
# huawei
-keep class com.huawei.hms.ads.** { *; }
-keep interface com.huawei.hms.ads.** {*; }
# lenovo
-keep class com.zui.deviceidservice.** { *; }
-keep class com.zui.opendeviceidlibrary.** { *; }
# meizu
-keep class com.meizu.flyme.openidsdk.** { *; }
# nubia
-keep class com.bun.miitmdid.provider.nubia.NubiaIdentityImpl { *; }
# oppo
-keep class com.heytap.openid.** { *; }
# samsung
-keep class com.samsung.android.deviceidservice.** { *; }
# vivo
-keep class com.vivo.identifier.** { *; }
# xiaomi
-keep class com.bun.miitmdid.provider.xiaomi.IdentifierManager { *; }
# zte
-keep class com.bun.lib.** { *; }
# coolpad
-keep class com.coolpad.deviceidsupport.** { *; }
# ---------掌酷 SDK--------------
-keep class com.wrapper.ZkViewSDK {
 public <fields>;
 public <methods>;
}
-keep class com.wrapper.ZkViewSDK$ActionCallBack {
 public <fields>;
 public <methods>;
}
# 保留枚举类不被混淆
-keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
}
-keep class com.wrapper.ZkViewSDK$KEY {
 public <fields>;
 public <methods>;
}
-keep class com.wrapper.ZkViewSDK$Event {
 public <fields>;
 public <methods>;
}
-keeppackagenames com.zk.**
# ---------掌酷 SDK--------------
#huawei
-keep class com.huawei.openalliance.ad.** { *; }
-keep class com.huawei.hms.ads.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
    public *;
}
-keep class MTT.ThirdAppInfoNew {
    *;
}
-keep class com.tencent.** {
    *;
}
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-dontwarn com.tapadn.**
-keep class com.tapadn.** { *;}
-dontshrink