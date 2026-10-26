# Proguard rules for CiudadActiva
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class com.ciudadactiva.app.data.model.** { *; }
