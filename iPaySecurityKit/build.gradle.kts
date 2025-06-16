import java.io.FileInputStream
import java.util.Properties

val properties = Properties()
val localPropsFile = FileInputStream("local.properties")
properties.load(localPropsFile)

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

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    ndkVersion = "26.1.10909125"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.rootbeer.lib)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.codelabs"
                artifactId = "security-kit"
                version = "1.0.7"

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
        repositories {
            maven {
                name = "iPay Security Kit"
                url = uri("https://maven.pkg.github.com/shehan-shyaminda/security-kit")
                credentials {
                    username = properties["USERNAME"].toString()
                    password = properties["TOKEN"].toString()
                }
            }
        }
    }
}