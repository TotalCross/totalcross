# Compiling TotalCross Virtual Machine for Android

First things first, in order to compile a virtual machine for Android, one need to install the following requirements on your machine:

- [Android SDK 28](https://www.androidauthority.com/how-to-install-android-sdk-software-development-kit-21137/) ;
- [Android NDK 17](https://developer.android.com/ndk/downloads/older_releases) ;
- [CMake](https://cmake.org/) 3.5.1 or later installed (if you are building from the target platform);

Once you have all requirements installed will be able to build a TotalCross Virtual Machine and test.

## Building step

Basically, to start building TotalCross VM and SDK for Android for testing, one just need two key elements: i) the `dist/` folder which contains our _.apk_; and ii) the `etc/` folder which contains our certificates.

### ANDROID_HOME

You must have the environment variable `ANDROID_HOME` pointed to the path that has the Android SDK folder with the following structure.

```
.
├── build-tools  
├── cmake  
├── emulator  
├── licenses  
├── ndk  
├── ndk-bundle  
├── patcher  
├── platforms  
├── platform-tools  
└── sources
````
> It's very important that you have the `ndk-bundle/` folder. See more about [NDK](https://developer.android.com/studio/projects/install-ndk).

## Command-line
You just need to run the following command line inside `TotalCrossVM/android/` folder:
```bash
./gradlew assembleDebug copyApk -x test
```
The result should be something like:
```bash
.
├── LitebaseSDK/
│   └── output/
│       └── debug/
│           └── TotalCrossSDK/
├── TotalCrossSDK/
├── TotalCrossVM/ 
├── LICENSE
└── README.md
```
Copy the content of this folder must be pasted to the root of a valid SDK.
