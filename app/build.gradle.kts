import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.viettel"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.viettel"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += "armeabi-v7a"
        }
    }


    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }


    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs", "src/main/jniLibs")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java toolchain cho phần Java/AGP
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }


    buildToolsVersion = "36.0.0"
}


kotlin {

    jvmToolchain(21)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // Nếu có flags thêm thì mở comment:
        // freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

configurations.all {
    resolutionStrategy {
        force("org.bouncycastle:bcprov-jdk18on:1.76")
        force("org.bouncycastle:bcutil-jdk18on:1.76")

        eachDependency {
            if (requested.group == "org.bouncycastle" && requested.name.contains("jdk15on")) {
                useTarget("org.bouncycastle:bcprov-jdk18on:1.76")
                because("Avoid duplicate class conflicts with newer BouncyCastle SDK")
            }
        }
    }
}



dependencies {
    implementation(project(":eidsdk"))
    implementation(files("libs/ControlLightLib.jar"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.fragment:fragment-ktx:1.8.9")


    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Vision + MLKit
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:object-detection-common:18.0.0")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation ("com.google.mlkit:face-detection:16.1.7")
    implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")


    // MRZ / Crypto
    implementation("org.jmrtd:jmrtd:0.7.39") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
    }
    implementation("net.sf.scuba:scuba-sc-android:0.0.26")
    implementation(libs.prov)

    implementation("org.ejbca.cvc:cert-cvc:1.4.13") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
    }

    // RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    //PDF reader
    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    //
    implementation ("org.java-websocket:Java-WebSocket:1.6.0")
    implementation ("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    implementation("org.nanohttpd:nanohttpd:2.3.1")



}
