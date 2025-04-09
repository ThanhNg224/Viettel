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

-keep public class vn.leeon.eidsdk.facade.* { *; }
-keep public class vn.leeon.eidsdk.data.* { *; }
-keep public class vn.leeon.eidsdk.utils.StringUtils { *; }
-keep public class vn.leeon.eidsdk.jmrtd.VerificationStatus { *; }
-keep public class vn.leeon.eidsdk.jmrtd.VerificationStatus$* { *; }
-keep public class vn.leeon.eidsdk.jmrtd.FeatureStatus { *; }
-keep public class vn.leeon.eidsdk.jmrtd.FeatureStatus$* { *; }
-keep public class vn.leeon.eidsdk.network.* { *; }
-keep public class vn.leeon.eidsdk.network.models.* { *; }
-keepattributes Exceptions, Signature, InnerClasses


# Open source library
-keep public class net.** { *; }
-keep public class org.** { *; }
-keep public class androidx.** { *; }
-keep public class android.** { *; }
-keep public class com.** { *; }
-keep public class retrofit2.** { *; }
-keep public class io.** { *; }
-keep public class java.** { *; }
-keep public class javax.** { *; }