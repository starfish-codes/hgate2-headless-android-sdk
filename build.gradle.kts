// Top-level build file where you can add configuration options common to all sub-projects/modules.
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

tasks.register("addPreCommitGitHook") {
    doLast {
        println("Running Add Pre Commit Git Hook Script on Build")
        exec {
            commandLine("cp", ".script/pre-commit", ".git/hooks")
        }
        println("✅ Added Pre Commit Git Hook Script.")
    }
}
