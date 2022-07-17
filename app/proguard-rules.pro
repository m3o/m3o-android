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

-keepattributes Annotation, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class com.m3o.mobile.**$$serializer { *; }
-keepclassmembers class com.m3o.mobile.** {
    *** Companion;
}
-keepclasseswithmembers class com.m3o.mobile.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.cyb3rko.m3okotlin.**$$serializer { *; }
-keepclassmembers class com.cyb3rko.m3okotlin.** {
    *** Companion;
}
-keepclasseswithmembers class com.cyb3rko.m3okotlin.** {
    kotlinx.serialization.KSerializer serializer(...);
}