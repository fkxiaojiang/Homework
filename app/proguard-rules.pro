# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep data classes used by Room
-keep class edu.guigu.accountbook.data.model.** { *; }
-keep class edu.guigu.accountbook.data.dao.** { *; }
