# Compiling and Testing TotalCross Virtual Machine for iOS

First things first, in order to compile a virtual machine for iOS, one need to install the following requirements on your machine:

- Mac OS X;
- [Xcode](https://apps.apple.com/br/app/xcode/id497799835?mt=12) latest version;
- [Cmake](https://cmake.org/) 3.5.1 or later installed;
- [Cocoapods](https://guides.cocoapods.org/using/getting-started.html#getting-started) 1.10.0.beta.1 or later:
  - Until the moment I was writing this markdown, one could only install the mentioned version by using the following command:
    - `sudo gem install cocoapods --pre`

Once you have all requirements installed will be able to build a TotalCross Virtual Machine, test and debug it on XCode.

## Building the VM

The ios bundle files are located on `TotalCrossVM/xcode` and has the following struct:

```
├── Launch\ Screen.storyboard
├── Podfile
├── README.md
├── TotalCross
├── TotalCross.xcodeproj
├── TotalCross.xcworkspace
├── TCVM.xcodeproj (need to be generated)
└── tcvm
```

In order to be able to start a change on the iOS VM, we need to generate the xcode project that describes our VM (TCVM.xcodeproj). Then use Cocopods to install the VM depedences (Frameworks, dynamic and static libraries). To do so, execute the following commands on you terminal:

- go to the project folder: `cd TotalCrossVM/xcode`;
- generate TCVM project: `cmake ../ -GXcode`;
- install project dependences: `pod install`;
- open TotalCross workspace on xcode: `open TotalCross.xcworkspace`.

Now we are able to develop new features and fix some bugs and Test on your Xcode.

## Testing the VM

The follow picture shows the project structure open on Xcode:

![TotalCross iOS project structure](https://i.imgur.com/E5r4dMA.png)

To test the TotalCross iOS VM, one need to:

- choose a TotalCross project to generate and copy `tcz` files;
- plug an iOS Device (iPhone or iPad) on one of your machine's USB ports.

To do the first step, let's clone our [TCSample repo](https://github.com/TotalCross/tc-sample). Edit it's pom.xml to only generating the TotalCross Zip Files (tcz) by deleting `platforms` tag (lines ~ 59-63).
```xml
- <platforms>
-    <platform>-android</platform>
-    <platform>-linux</platform>
-    <platform>-linux_arm</platform>
- </platforms>
```
Run `mvn package` and the tcz files will take place at `target` folder.

```
target
    ├── Material_Icons.tcz
    ├── TCBase.tcz
    ├── TCFont.tcz
    ├── TCSample.tcz
    └── TCUI.tcz
```
On your Xcode IDE, right click on `TotalCross` and choose `Add Files to "TotalCross"...`. Choose all `.tcz` files at the target folder of your choosen TotalCross project.

Open the TotalCross project at your Xcode Project Nagigator click on the files `TotalCross > AppDelegate.m`. At line ~ 39 replace `#define TCZNAME "TotalCross"` by the name of your TotalCross Application (in this case `TCSample`). So the resulting line will be `#define TCZNAME "TCSample"`.

Plug your device and select it on the TotalCross active scheme (next to the play button). Next, click on the play button and your application will be running on your device.

![Select your device on the active scheme](https://i.imgur.com/P9T6ZCM.png)

> :warning: **if you have not paid for an [Apple Development Certificate](https://developer.apple.com/)**: this [thread](https://stackoverflow.com/questions/4952820/test-ios-app-on-device-without-apple-developer-program-or-jailbreak) on stackoverflow has a little discuss about how to test an application with no cost on XCode. 
