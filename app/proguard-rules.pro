# Mantém classes do CallScreening (críticas para bloqueio)
-keep class org.floatingskies.quiet.service.CallBlockerService { *; }
-keep class org.floatingskies.quiet.receiver.* { *; }
-keep class org.floatingskies.quiet.data.* { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
