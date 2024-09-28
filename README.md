# MyAndroidApp

## Introduction
NearbyStore is a mobile application designed to allow users to search for nearby stores based on the store's location allowing also the users to view details about each store, leave reviews, and add stores to their favorites.

## Technologies and Requirements
- **Programming Languages:** Java
- **Development Environment:** Android Studio
- **Android Version:** Pixel 3 with API level 33 and Android 13.0 (“Tiramisu”) | x86_64

## Project Description
MyAndroidApp is built to allow users to search for nearby stores based on the store's location allowing also the users to view details about each store, leave reviews, and add stores to their favorites. It includes features such as:
- Data storage using SQLite
- Integration with Google Maps API

## Installation and Setup
For details and dependecies the `report.pdf` can be checked.

### Clone the Repository
To get started, clone the repository using the following command:
```
git clone git@github.com:ioannismt/android.git
```

Open in Android Studio
Open the project in Android Studio by selecting File -> Open... and navigating to the cloned repository.
Ensure you have the necessary SDKs and build tools installed.
Build Variants
Use the Android Studio Build Variants button to choose between production and staging flavors combined with debug and release build types.
Generate Signed APK
To generate a signed APK:
Go to Build -> Generate Signed APK...
Fill in the keystore information as needed.
Command Line Build
To build the APK using Gradle:
```
./gradlew build
```

To install the debug APK on your device:
```
./gradlew installDebug
```
## Usage Examples

### Running the App
- Connect an Android device to your development machine.
- Select `Run -> Run 'app'` (or `Debug 'app'`) from the menu bar in Android Studio.
- Alternatively, use the command line to install and run the app:
  ```sh
  ./gradlew installDebug
  adb shell am start com.yourpackage.name/com.yourpackage.name.MainActivity