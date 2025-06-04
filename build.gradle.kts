// ✅ Root-level build.gradle.kts
plugins {
    // Use your version catalog if defined
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Manually declare android.library plugin (used in eidsdk)
    id("com.android.library") version "8.10.0" apply false
}
