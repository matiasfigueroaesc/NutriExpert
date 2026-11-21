plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cl.duoc.nutriexpert"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.nutriexpert"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK de entrega. El archivo nutriexpert.jks está en la carpeta app/ del repo
    // (lo pide la pauta). Es como la firma de un documento: Android solo instala un APK firmado,
    // y con la misma llave se pueden firmar las versiones nuevas.
    signingConfigs {
        create("release") {
            storeFile = file("nutriexpert.jks")
            storePassword = "NutriExpert2026"
            keyAlias = "nutriexpert"
            keyPassword = "NutriExpert2026"
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Retrofit: para hablar con los microservicios por HTTP (JSON con Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    // WorkManager: sincroniza en segundo plano cuando vuelve la red
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    // MockK finge el ApiService y el DAO en los tests; coroutines-test corre funciones suspend
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
