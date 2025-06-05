# Setup for SDK Development

## Prerequisites
* Install Android Studio
* Create a Simulator in Android Studio
* Sync the project with gradle
* Run the app project on the simulator


# Usage of the SDK

* Detailed instruction on how to use the SDK will be provided on https://developer.hellgate.dev/resources/sdks/android-sdk

# Deployment to maven central

Currently, the SDK is deployed to maven central using the `maven-publish` plugin. To deploy a new version, follow these steps:
* Update the version number in `gradle.properties` file. Variable `currentVersion` with a v prefix.
* Make sure you have the necessary credentials set up in your global `gradle.properties` file:
```
  signing.keyId=your-signing-key-id
  signing.password=your-signing-password
  signing.secretKeyRingFile=path/to/your/secring.gpg
  maven.central.token=your-maven-central-token
```
* Run the gradle task `publishToMavenCentral` to create the signed zip file and upload it to maven central.
* Log into Sonatype Nexus Repository Manager and verify that the new version is available and validated.
* If everything is correct, release the new version via Maven Central Website.
