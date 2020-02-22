-keepattributes Signature
-keep class sun.misc.Unsafe { *; }

-keep class com.gradestat.Table** { *; }
-keep class org.threeten.bp.LocalDate** { *; }

-keep class com.gradestat.MainActivity { void onCreate(android.os.Bundle); }
 -keep class androidx.core.app.CoreComponentFactory { *; }

