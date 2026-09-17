# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Dhizuku API classes
-keep class com.rosan.dhizuku.** { *; }

# Keep our app entry points
-keep class com.dnsguard.locker.** { *; }
