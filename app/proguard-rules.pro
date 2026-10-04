# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers and source file names for readable crash stack traces in Google Play Console
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve runtime annotations and generic signatures
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep ViewModel constructors for reflection/instantiation
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep domain and state data classes
-keepclassmembers class com.example.model.** { *; }

# Keep BuildConfig fields
-keep class com.example.BuildConfig { *; }

