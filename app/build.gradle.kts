import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use(::load)
}

val instagramClientId: String = localProperties.getProperty("instagram.clientId", "")
val instagramClientSecret: String = localProperties.getProperty("instagram.clientSecret", "")
val instagramRedirectScheme: String = localProperties.getProperty("instagram.redirect.scheme", "itemshop")
val instagramRedirectHost: String = localProperties.getProperty("instagram.redirect.host", "oauth")
val instagramRedirectPath: String = localProperties.getProperty("instagram.redirect.path", "/callback")

android {
    namespace = "com.svyd.itemshop"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.svyd.itemshop"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "INSTAGRAM_CLIENT_ID", "\"$instagramClientId\"")
        buildConfigField("String", "INSTAGRAM_CLIENT_SECRET", "\"$instagramClientSecret\"")
        buildConfigField("String", "INSTAGRAM_REDIRECT_SCHEME", "\"$instagramRedirectScheme\"")
        buildConfigField("String", "INSTAGRAM_REDIRECT_HOST", "\"$instagramRedirectHost\"")
        buildConfigField("String", "INSTAGRAM_REDIRECT_PATH", "\"$instagramRedirectPath\"")

        manifestPlaceholders["oauthRedirectScheme"] = instagramRedirectScheme
        manifestPlaceholders["oauthRedirectHost"] = instagramRedirectHost
        manifestPlaceholders["oauthRedirectPath"] = instagramRedirectPath
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose.viewmodel)

    // Custom Tabs (for OAuth)
    implementation(libs.androidx.browser)

    // Coil 3
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
