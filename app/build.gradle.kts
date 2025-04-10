plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.viettel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.viettel"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += "armeabi-v7a"
        }
    }

    // 👇 This MUST be OUTSIDE of defaultConfig!
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    buildToolsVersion = "36.0.0"
}


configurations.all {
    resolutionStrategy {
        force("org.bouncycastle:bcprov-jdk18on:1.76")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Vision + MLKit
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:object-detection:17.0.0")
    implementation("com.google.mlkit:object-detection-common:17.0.0")
    implementation("com.google.mlkit:vision-common:17.3.0")
    // MRZ / Crypto
    implementation("org.jmrtd:jmrtd:0.7.39") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
    }
    implementation("net.sf.scuba:scuba-sc-android:0.0.20")
    implementation("com.madgag.spongycastle:prov:1.58.0.0")

    implementation("org.ejbca.cvc:cert-cvc:1.4.13") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
    }

    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
}
