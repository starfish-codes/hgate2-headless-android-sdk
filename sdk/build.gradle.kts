import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
    id("signing")

    alias(libs.plugins.apiChecker)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kover)
    alias(libs.plugins.testLogger)
}

android {
    group = "io.hellgate"
    namespace = "$group.android.sdk"
    compileSdk = 36

    val currentVersion: String by project
    version = currentVersion.drop(1)

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        aarMetadata {
            minCompileSdk = 28
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/starfish-codes/hgate2-headless-android-sdk")
            credentials {
                username = project.properties["github.token.write.username"].toString()
                password = project.properties["github.token.write.token"].toString()
            }
        }


        maven {
            name = "Staging"
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }

    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "android-sdk"
            version = project.version.toString()
            description = "Hellgate Android SDK"

            afterEvaluate {
                from(components["release"])
            }
        }
        withType<MavenPublication> {
            pom {
                packaging = "aar"
                name.set("hellgate-android-sdk")
                description.set("Hellgate Android SDK")
                url.set("https://hellgate.io/")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Starfish GmbH & Co. KG")
                        email.set("hellgate.service@starfish.team")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:starfish-codes/hgate2-headless-android-sdk.git")
                    url.set("https://github.com/starfish-codes/hgate2-headless-android-sdk")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/starfish-codes/hgate2-headless-android-sdk/issues")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.client.logging)

    implementation(libs.nimbus.jose.jwt)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    // org.hamcrest needs to be excluded to avoid conflicts with JUnit4 and Robolectric
    testImplementation(libs.wiremock) { exclude("org.hamcrest") }
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.ktor.client.logging)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.ui.test.junit4)

    testImplementation(libs.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)

    androidTestImplementation(libs.assertj.core)
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

detekt {
    config.from(files("../config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    baseline = file("../config/detekt/detekt-baseline.xml")
}

testlogger {
    theme = ThemeType.MOCHA
}

tasks {
    task("lintDetekt") {
        group = "verification"
        dependsOn("lintKotlin")
        finalizedBy("detekt", "detektMain", "detektTest")
    }

    task("cleanUpStagingDeploy") {
        group = "publishing"
        dependsOn("publishReleasePublicationToStagingRepository")
        val folder = layout.buildDirectory.dir("staging-deploy/io/hellgate/android-sdk").get().asFile
        doLast { delete(folder.listFiles { _, name -> name.startsWith("maven") }) }
    }

    task("createReleaseZip", type = Zip::class) {
        group = "publishing"
        dependsOn("cleanUpStagingDeploy")
        archiveFileName.set("android-sdk-${project.version}.zip")
        destinationDirectory.set(layout.buildDirectory.dir("mavenCentral").get().asFile)
        from(layout.buildDirectory.dir("staging-deploy").get().asFile)
    }

    task<Exec>("uploadToMavenCentral") {
        group = "publishing"

        val folder = layout.buildDirectory.dir("mavenCentral").get().asFile
        val zipFile = folder.listFiles { _, name -> name.startsWith("android") }
            ?.first()
        val absolutePath = zipFile?.absolutePath.orEmpty()
        val uploadName = zipFile?.nameWithoutExtension.orEmpty()
        val token = project.findProperty("maven.central.token") as? String
            ?: throw GradleException("Property 'maven.central.token' not set")


        val command = listOf(
            "curl",
            "--location", "https://central.sonatype.com/api/v1/publisher/upload?name=$uploadName&publishingType=USER_MANAGED",
            "--header", "Authorization:Bearer $token",
            "--form", "bundle=@$absolutePath",
        )

        commandLine = command

        doLast {
            println("Upload to Maven Central completed successfully.")
        }
        doFirst {
            if (zipFile == null) {
                throw GradleException("No zip file found in staging deploy directory")
            }
            println("Uploading $zipFile to Maven Central")
        }
    }

    task("publishMavenCentral") {
        group = "publishing"
        dependsOn("clean", "createReleaseZip", "uploadToMavenCentral")
        getByName("preBuild").mustRunAfter("clean")
        getByName("createReleaseZip").mustRunAfter("clean")
        getByName("uploadToMavenCentral").mustRunAfter("createReleaseZip")
    }
}
