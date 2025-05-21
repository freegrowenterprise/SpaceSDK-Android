import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.growspace.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core.uwb:uwb:1.0.0-alpha10")
    implementation("androidx.core.uwb:uwb-rxjava3:1.0.0-alpha10")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            groupId = "com.growspace.sdk"  // ✅ 패키지 그룹 ID
//            artifactId = "growspacesdk"   // ✅ 라이브러리 이름
//            version = "1.0.0"             // ✅ 버전
//
//            afterEvaluate {
//                from(components["release"]) // ✅ 릴리즈 AAR 파일 가져오기
//            }
//        }
//    }
//}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("io.github.freegrowenterprise", "SpaceSDK-Android", "0.0.3")

    pom {
        name = "SpaceSDK-Android"
        description = "SpaceSDK-Android"
        url = "https://github.com/freegrowenterprise/SpaceSDK-Android"
        inceptionYear = "2025"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "MMMIIIN"
                name = "MMMIIIN"
                url = "https://github.com/MMMIIIN"
            }
        }

        scm {
            connection = "scm:git:github.com/freegrowenterprise/SpaceSDK-Android.git"
            developerConnection.set("scm:git:ssh://github.com:freegrowenterprise/SpaceSDK-Android.git")
            url = "https://github.com/freegrowenterprise/SpaceSDK-Android/tree/master"
        }
    }
}
