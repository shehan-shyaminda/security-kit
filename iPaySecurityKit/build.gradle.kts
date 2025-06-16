plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.ipay.securitykit"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        lint.targetSdk = 34
        version = "1.0.1"

        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
    buildFeatures {
        buildConfig = true
        resValues = true
    }
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    ndkVersion = "26.1.10909125"
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.scottyab:rootbeer-lib:0.1.1")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.codelabs"
                artifactId = "security-kit"
                version = "1.0.0"

                pom {
                    name.set("iPay Security Kit")
                    description.set("Security Library for iPay Applicaitons")
                    url.set("https://github.com/shehan-shyaminda/security-kit")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("shehan-shyaminda")
                            name.set("Dinuka Shehan")
                            email.set("shehan.shyaminda@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/shehan-shyaminda/security-kit.git")
                        developerConnection.set("scm:git:ssh://github.com:shehan-shyaminda/security-kit.git")
                        url.set("https://github.com/shehan-shyaminda/security-kit")
                    }
                }
            }
        }
    }
}