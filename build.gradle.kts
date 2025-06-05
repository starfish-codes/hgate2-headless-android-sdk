plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false

    alias(libs.plugins.apiChecker) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.testLogger) apply false
}

tasks.register<Exec>("addPreCommitGitHook") {
    group = "git"
    description = "Adds a pre-commit git hook to the project."

    doFirst { println("Adding pre-commit git hook...") }
    commandLine = listOf("cp", ".script/pre-commit", ".git/hooks")
    doLast { println("✅ Pre-commit git hook added successfully.") }
}
