# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the
# Android SDK's proguard-android-optimize.txt file.

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# --- Gson: full rules for R8 compatibility ---
# Keep generic type signatures (required for TypeToken reflection)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Gson core
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Keep our data model classes (Gson serialises these by field name)
-keep class com.nfcpoc.data.model.** { *; }
-keepclassmembers class com.nfcpoc.data.model.** {
    <fields>;
    <init>(...);
}

# --- NfcPoc Application classes ---
-keep class com.nfcpoc.hce.** { *; }

# --- Timber ---
-dontwarn org.jetbrains.annotations.**

# --- Kotlin ---
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- RecyclerView: keep LayoutManagers referenced from XML ---
# R8 strips these if they're only referenced via the 'app:layoutManager' XML attribute string.
-keep class androidx.recyclerview.widget.RecyclerView$LayoutManager { *; }
-keep class androidx.recyclerview.widget.LinearLayoutManager { *; }
-keep class androidx.recyclerview.widget.GridLayoutManager { *; }
-keep class androidx.recyclerview.widget.StaggeredGridLayoutManager { *; }

# --- RecyclerView: keep ViewHolder constructors ---
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);
}

# --- Navigation / Fragment: keep Fragment subclasses (instantiated by name) ---
-keep public class * extends androidx.fragment.app.Fragment { *; }

# --- ViewBinding / DataBinding ---
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(...);
    public static * bind(android.view.View);
}
