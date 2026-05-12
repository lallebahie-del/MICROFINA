import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

/// Clé injectée dans AndroidManifest → com.google.android.geo.API_KEY
/// Ordre : propriété Gradle -P / gradle.properties → android/local.properties → variable d'environnement.
val googleMapsApiKey: String = run {
    val fromGradle = (project.findProperty("GOOGLE_MAPS_API_KEY") as String?)?.trim().orEmpty()
    if (fromGradle.isNotEmpty()) return@run fromGradle

    val local = Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { local.load(it) }
    }
    val fromLocal = local.getProperty("GOOGLE_MAPS_API_KEY")?.trim().orEmpty()
    if (fromLocal.isNotEmpty()) return@run fromLocal

    System.getenv("GOOGLE_MAPS_API_KEY")?.trim().orEmpty()
}

if (googleMapsApiKey.isEmpty()) {
    logger.warn(
        "GOOGLE_MAPS_API_KEY manquante : tuiles Google Maps grises (carte vide). " +
            "Ajoutez GOOGLE_MAPS_API_KEY dans android/local.properties " +
            "(voir android/local.properties.example) et activez « Maps SDK for Android » sur la clé.",
    )
}

android {
    namespace = "com.microcredit.client.mobile"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.microcredit.client.mobile"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}
