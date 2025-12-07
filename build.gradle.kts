plugins {
    // Use your version catalog if defined
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Manually declare android.library plugin (used in eidsdk)
    id("com.android.library") version "8.13.1" apply false

    // Hilt plugin for dependency injection
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
}
