# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepclassmembers class com.payex.mobilesdk.internal.JSInterface {
    @android.webkit.JavascriptInterface public <methods>;
}

-keepclassmembers,allowobfuscation class com.payex.mobilesdk.internal.remote.json.* {
    <init>();
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers,allowobfuscation class com.payex.mobilesdk.internal.remote.json.Link* {
    <init>(okhttp3.HttpUrl);
}
