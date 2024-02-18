@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    id("kotlin-parcelize")
    alias(libs.plugins.materialthemebuilder)
}

android {
    namespace = "dev.rohitverma882.miunlock"
    compileSdk = project.properties["compileSdk"].toString().toInt()
    ndkVersion = project.properties["ndkVersion"].toString()

    defaultConfig {
        applicationId = "dev.rohitverma882.miunlock"
        minSdk = project.properties["minSdk"].toString().toInt()
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations += "en"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("")
                arguments("-DANDROID_STL=c++_static")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        prefab = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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

    lint {
        checkReleaseBuilds = false
        abortOnError = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = project.properties["cmakeVersion"].toString()
        }
    }

    materialThemeBuilder {
        themes {
            create("MiUnlock") {
                primaryColor = "#3F51B5"

                lightThemeFormat = "Theme.Material3.Light.%s"
                lightThemeParent = "Theme.Material3.Light"
                darkThemeFormat = "Theme.Material3.Dark.%s"
                darkThemeParent = "Theme.Material3.Dark"
            }
        }
        generatePaletteAttributes = true
        generateTextColors = true
    }
}

dependencies {
    implementation(project(":terminal"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.webkit)

    compileOnly(libs.boringssl)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}