# Room entities — prevent R8 from stripping field names used by SQLite cursor mapping
-keep class com.lmfd.warboss.data.db.** { *; }

# OkHttp — BSData zip download
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# XmlPullParser — BSData .gst/.cat XML parse
-keep class org.xmlpull.** { *; }
-dontwarn org.xmlpull.**
