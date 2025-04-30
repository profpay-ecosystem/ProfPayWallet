import com.google.protobuf.gradle.id
import io.sentry.android.gradle.instrumentation.logcat.LogcatLevel

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.protobuf") version "0.9.4" apply true
    kotlin("plugin.serialization") version "1.9.21"
    id("io.sentry.android.gradle") version "4.11.0"
}
sentry {
    tracingInstrumentation {
        enabled.set(true)

        logcat {
            enabled.set(true)
            minLevel.set(LogcatLevel.ERROR)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.example.telegramWallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.telegramWallet"
        minSdk = 29
//        minSdk = 25 изменено на 29
        targetSdk = 34
        versionCode = 2

//        MAJOR: Внесение изменений, ломающих обратную совместимость.
//        MINOR: Добавление новых функций без нарушения совместимости.
//        PATCH: Исправление ошибок и незначительные улучшения без изменения функциональности.
        versionName = "22.15.15" // MAJOR.MINOR.PATCH

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName =
                    "profpay-${variant.baseName}-${variant.versionName}-${variant.versionCode}.apk"
                output.outputFileName = outputFileName
            }
    }

    buildTypes {
        release {
//            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "META-INF/**"
        }
    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
            val protoSrcDir = "src/main/proto"
            java.srcDirs(protoSrcDir)
            resources.srcDirs(protoSrcDir)
        }
    }
}

project.configurations.configureEach {
    resolutionStrategy {
        force("androidx.emoji2:emoji2-views-helper:1.3.0")
        force("androidx.emoji2:emoji2:1.3.0")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("java")
            }
        }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
        classpath("com.google.gms:google-services:4.4.2")
        classpath(kotlin("serialization", version = "1.9.21"))
    }
}

dependencies {
    implementation(files("libs/bitcoinj-core-0.17-SNAPSHOT.jar"))
    val navVersion = "2.7.7"
    val roomVersion = "2.6.1"
    val lifecycleVersion = "2.7.0"
    val grpcVersion = "1.63.0"

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    // Алгоритм шифрования
    implementation("org.mindrot:jbcrypt:0.4")

//    biometric
    implementation("androidx.biometric:biometric:1.1.0")

    // gRPC libs
    implementation("com.google.protobuf:protobuf-java:3.25.1")
    implementation("com.google.protobuf:protobuf-java-util:3.25.0")

    implementation("io.grpc:grpc-netty-shaded:1.60.1")
    implementation("io.grpc:grpc-netty:1.45.1")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-okhttp:$grpcVersion")

    implementation("org.bouncycastle:bcprov-jdk15on:1.68")

    implementation(files("libs/utils-0.8.0.jar"))
    implementation(files("libs/abi-0.8.0.jar"))
    implementation(files("libs/core-0.8.0.jar"))

    implementation("com.google.guava:guava:32.0.1-jre")
    implementation(files("libs/bitcoinj-core-0.17-SNAPSHOT.jar"))

    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.49")
    kapt("com.google.dagger:hilt-android-compiler:2.49")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Kotlin
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")

    // Room DataBase
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    // Telegram Bot tgbotapi
    implementation("dev.inmo:tgbotapi:9.4.2")
    implementation("dev.inmo:tgbotapi.core:9.4.2")
    implementation("dev.inmo:tgbotapi.api:9.4.2")
    implementation("dev.inmo:tgbotapi.utils:9.4.2")

    //
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // bip39 mnemonic
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.6")

    // JSON Parse
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Krontab
    implementation("dev.inmo:krontab:2.2.4")

    // OkHttp Client
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // iText7
    implementation("com.itextpdf:itext-core:8.0.2")

    // interactive tips
    implementation("ly.com.tahaben:showcase-layout-compose:1.0.5-beta")

    // shapes(графика)
    implementation("androidx.graphics:graphics-shapes:1.0.0-beta01")

    // Контроллер UI
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    // Pusher Notification
    implementation("com.pusher:pusher-java-client:2.4.2")

    // QR-code builder
    implementation("com.google.zxing:core:3.4.1")

    // google-font
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

    implementation("io.github.rizmaulana:compose-stacked-snackbar:1.0.4")

    implementation("me.pushy:sdk:1.0.113")

    implementation("io.sentry:sentry-android:7.14.0")
    implementation("io.sentry:sentry-compose-android:7.14.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
