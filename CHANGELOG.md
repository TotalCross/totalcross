# TotalCross Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [7.0.0] - 2021-01-18

Welcome to 2021, and our first **stable release**! Let's get straight to the objectives:

- **Huge bug fixes** - Many long-standing bugs have been resolved;
- **arm64 distribution** - Now deploy TotalCross applications for linux arm64 is much easier;
- **Skia update** - A new room for improvements.

### Added
- Linux ARM64 support: packaging with the flag `-linux_arm` will **also** result in arm64 artifacts.
- Improvements for the TotalCross Simulator, including full screen and better resolution support - #185, #198

### Changed
- Skia: updated Skia dependency from branch m71 to m87 - #196

### Fixed
- `VirtualKeyboard` issues: this component widely used for embedded devices has some bugs that hinder the execution of applications - #124, #184, #193
- Android black screen: now everything works correctly when running the .apk for the first time or rotating the screen - #92, #179
- Build issues: we updated the versions of Android API, Android NDK and Gradle. Fixed bugs related to the build for legacy systems (GLIBC issue). Changes were made to implement CICD with Github Actions - #165
- XML encoder not working correctly with special characters - #190
- Misc bugs with graphical controls - #123, #140, #176, #180, #181, #177, #217, #189, #178

## 6.1.1 - October 2020
Hello! This minor release has as main objective:
- **Static SDL2** - Brings it back to work properly on Angstrom for target Linux ARM;
- **Display env-vars** - Configurable displays bounds;
- **`SSLSocketFactory`** - Change `SSLSocketFactory` to always yield a `SSLSocket`;
- **Fixes** - Minor bug fixes :wrench:.

Join our [community on the telegram](https://t.me/totalcrosscommunity) to give feedback about this release!

### Static SDL2

SDL is now statically linked for Linux arm32v7. Some very simple systems do not have the possibility of installing SDL2 in an easy way. On Angström-type systems this required a certain kludge exporting the path to extern `.so` with `LD_LIBRARY_PATH=<path-to-.so>`. We didn't want it to be hard, that's why the change!

### Display env-vars
We now introduce 3 environment variables for execution. Since we do not have an automatic viewport they need to be set up they are:
|     | TC_WIDTH  | TC_HEIGHT  | TC_FULLSCREEN  |
|---|---|---|---|
| Description  | Size of `width` in pixels  | Size of `height` in pixels  | Only do something when comes to `false` or `0` | 
| Values  |  int | int  | bool | 

You can use them as follows for Linux and Linux ARM platforms:

```bash
$ TC_WIDTH=1280 TC_HEIGHT=720 TC_FULLSCREEN=false ./Launcher MyApp
```
When the fullscreen is deactivated the TC is opened in a window and its logic like:
```cpp
width -= width*0.09;
height -= height*0.09;
```
That should change soon. See more in #99.

>The syntax #\<number\> is related to pull request in GitHub repository.

### `SSLSocketFactory`
Should always yields a `SSLSocket` when in `SSLSocketFactory`. As
`SSLSocketFactory` extends `SocketFactory` and does not override the
`createSocket(String, int)` method, and calls from super classe. As
`SocketFactory` yields plain sockets...

This fix just delegates to `createSocket(String, int, int)` passing the default
`Socket.DEFAULT_OPEN_TIMEOUT` as the open timeout argument. Maybe it should be
better to call `new SSLSocket(String, int)`, but this constructor does not
exists yet. See more in #122.

> Thanks **@jeffque** :smiley:!

### Fixes
Corrections were made:
- Launch args not being parsed, i.e., ex. `/density` #94;
- SDL didn't work properly in systems without libSDL2-dev, `CreateRenderer()` bug #99;
- SDL use `SDL_Surface` instead of `SDL_Renderer` when hardware accelerated graphics is not available;
- iOS: Update `youtube-ios-player-helper` from 0.0.16 to 1.0.2. This is required because UIWebView, a component used by this dependency, is deprecated resulting the error ITMS-90809 when uploading the app for the app store;
- iOS: fix missing icon files when generating;
- Remove some Sun private libraries are not guaranteed to be available to any JDK besides
the Oracle JDK 8 or previous;
- Fix `AnonymousUserData` usage;
- `StringBuffer` adds missing `CharSequence` constructor.


## 6.1.0 - July 2020
Welcome to the July 2020 release (version 6.1.0). We hope you enjoy the updates in this version. The key highlights are:
  - **Maven plugin new version** - your pom.xml file should change;
  - **KnowCode Compatibility** - It is now possible to run Android XMLs Layouts on Linux Arm;
  - **Virtual Keyboard** - New look and animations;
  - **Using external applications** - Through the runtime.exec method;
  - **Anonymous user statistics** - There is now an option to contribute by sending anonymous data;
  - **Changing the SDL implementation** - Removing the SDL code from within the TotalCross SDK and changing the static to dynamic link.

Join our [community on the telegram](https://t.me/totalcrosscommunity) to suggest activities and learn about development for embedded systems.

### Maven plugin new version
After the latest *Language Support for Java* extension updates, some systems had compatibility issues with Java 11. We launched a new version of the TotalCross maven plugin to solve them, the *1.1.6*. You must change the following line:
```xml
...

<plugin>
  <groupId>com.totalcross</groupId>
  <artifactId>totalcross-maven-plugin</artifactId>
  <!-- This line: -->
  <version>1.1.6</version>
  <configuration>
    <name>${project.name}</name>
    <platforms>
    
...
```
### KnowCode compatibility
In this version, a *name tag was added to ContentHandler* (Pull request [#20](https://github.com/TotalCross/totalcross/pull/20)), making it possible for [KnowCode](https://github.com/totalcross/KnowCodeXML), a tool that uses a neural network, to convert [interface images to XML and TotalCross, allowing it to run on Linux Arm](https://opensource.com/article/20/5/linux-arm-ui) with the **same performance** as an application built directly with the TotalCross SDK.

![Imgur](https://i.imgur.com/3NJZSE9.gif)

Find out how to use KnowCode by clicking [here](https://www.youtube.com/watch?v=7o3p14wQPsE).

### Virtual Keyboard
In this milestone some improvements were implemented in the keyboard, which are:
- In Linux Arm the virtual keyboard is enabled by default. To disable it just put `Settings.virtualKeyboard = false` in your `MainWindow` class; 
- In order to improve performance and make the design cleaner, the *ripple effect* animation on the keyboard buttons was removed;
- In order to obtain more effective responsiveness, the calculations of width and height were changed, leaving it less dependent on absolute values;
- To make the interface richer, *sliding animation* was added to the *popup* and *unpopup* of the virtual keyboard. You can see these changes in detail in pull requests [#40](https://github.com/TotalCross/totalcross/pull/40) and [#65](https://github.com/TotalCross/totalcross/pull/65).

In the images below you can see the visual difference:

**Before**:

![Imgur](https://i.imgur.com/PuMckm5.gif)

**After**:

![Imgur](https://i.imgur.com/DMEZysz.gif)

### External applications
To allow the use of external libraries in the simplest way possible is one of TotalCross goals. There is a [branch in our repository](https://github.com/TotalCross/totalcross/tree/feature/tcni-rebased) called `feature/tcni-rebased` which (still a Proof Of Concept) allows the use of these external libraries

> TCNI is a foreign function interface programming framework that enables TotalCross/Java code running in a Java virtual machine to call and be called by native applications and libraries written in other languages ​​such as C, C ++ and assembly.

But while TCNI is not ready to be launched, we have developed a middle ground **that allows the use of external applications** in TotalCross, through the method `runtime.exec()`. This method creates a new process and executes the command you passed in this new process (Pull request [#21](https://github.com/TotalCross/totalcross/pull/21)). 

For example, create a python script to access a temperature sensor and with the `runtime.exec()` get the return of it, as you can see in this [example](https://www.linkedin.com/posts/brunoamuniz_android-linux-linuxarm-activity-6686008251059847168-u-Sr):

![Imgur](https://i.imgur.com/AvVBa0G.gif)

To better understand how it works using a sample application click [here](https://learn.totalcross.com/documentation/guides/running-c++-applications-with-totalcross).

### Anonymous user statistics:

When TotalCross decided to become a free and open source tool, the decision was made to no longer require an activation key from the user, which was previously necessary to differentiate free users from customers. As this removal would imply resolving a series of errors and making many changes, the team decided to generate an eternal and unlimited key and use it as a default, provisionally, until we resolve some pending issues and can go back to this point and withdraw it for good.

So in 6.1.0 we went back to that and removed the activation key, including the class `register.java`, as you can see in detail in pull request [#53](https://github.com/TotalCross/totalcross/pull/53).

And to better track which users preferences when using the TotalCross SDK, we **set up the SDK to ask if the developer wants to contribute anonymous data** and if the answer is positive, we record parameters for Deploy, timezone, launchers, operating system version and version of TotalCross used.

![Imgur](https://i.imgur.com/UYrYUEU.png)

You can see the details of the implementation in Pull Request [#66](https://github.com/TotalCross/totalcross/pull/66).

### Change in SDL implementation

We chose to remove SDL code from the SDK TotalCross in this version **to improve the build organization** and allow people **to customize the build** of their facilities according to their hardware.

In addition, we changed the SDL link from static to dynamic because: 
- Allows the distribution to have corrections and be used without having to compile the TC again but the guy needs to install the SDL before TotalCross
- Avoids License compliance problems as [FOSSA](https://app.fossa.com/account/login?next=%2F) warns about the SDL license (GPLv3).

For details, see pull requests [#23](https://github.com/TotalCross/totalcross/pull/23), [#24](https://github.com/TotalCross/totalcross/pull/24), and [#68](https://github.com/TotalCross/totalcross/pull/68).

### Other changes:
- [Reverted scroll flick animation](https://github.com/TotalCross/totalcross/pull/54) back to a previous version to fix a bug that caused the content to be pulled back after a scroll; 
- Fixed [creation of TotalCross modules (*.tcz* files) using Java headless](https://github.com/TotalCross/totalcross/pull/45);
- Fixed [round border on Container](https://github.com/TotalCross/totalcross/pull/47); 
- Fixed [Switch Bar not being drawn](https://github.com/TotalCross/totalcross/pull/49);
- Fixed [wrong size returned by `FontMetrics.stringWidth(String s)`](https://github.com/TotalCross/totalcross/pull/50) on devices that use skia with .ttf file type fonts. 
- Fixed [Torizon/Wayland SDL](https://github.com/TotalCross/totalcross/pull/35) initialization; 
- Fixed [FadeAnimation](https://github.com/TotalCross/totalcross/pull/33): the animation is now working as intended.

## 6.0.4 - 2020-04-20
  
### Highlights  
- Fixed build for all available platforms on public repository  
- Skia for Android  
  
### Fixed  
- Build: Added some build.xml for Ant build (without ProGuard)  
- Fixed Android buildType  
- Fixed WinCE, Win32, Android and Linux x86-64 VM build 
- Fix Linux x86-64 launcher (*x-sharedlib* to *x-executable*)
- Added iOS files  
  
### Changed  
- Added Skia for Android  
- Added tag name to XML's ContentHandler
- ~~SDL statically linked~~ SDL dynamically linked for Linux x86-64
- Disabled screen shifting when keyboard is over edit (not working on Android)

## 5.1.4 to 6.0.3
The remaining changelogs will be added later

## 5.1.3 - 2019-08-28

### Fixed
- Edit
  - Fixed bug with masks containing two consecutive non-alphanumeric characters -  [TotalCross#581](https://gitlab.com/totalcross/TotalCross/issues/581#note_203298404)
  - When used for dates, the calendar now properly opens at the date informed on the edit - TotalCross#550
  - Fixed getCopy() to properly function when the Edit uses the field decimalPlaces - TotalCross#505
- Spinner: fixed Spinner animation repainting over previous frames - TotalCross#568
- OutlinedEdit: fixed NPE when using the cancel button on CalendarBox - TotalCross#547
- PopupMenu: fixed performance issues with Material style caused by onPaint recreating the ninepatch on every refresh. This change also affects the performance of other components that may use PopupMenu, such as ComboBox.

## 4.4.2 - 2019-11-28

### Highlights
- Deploy: Deprecated argument '/m'

The deployer now automatically uses the dummy certificate and mobile provision located inside the folder {TOTALCROSS3_HOME}/etc/tools/ipa. 

Using the argument is still allowed, but a message is printed informing of this change.

### Fixed
- Android: fixed SQLite performance on 64 bits devices - TotalCross#588

## 4.4.1 - 2019-08-22

### Highlights
- Android: raised target sdk version to API level 28 to comply with Play Store's latest requirements.

### Fixed
- Android: fixed support for 64 bits devices

## 4.4.0 - 2019-08-14

### Fixed
- Android: removed compile flag that caused applications to be unbearably slow in old or low-end devices - TotalCross#561, TotalCross#580
- tccodesign script: fixed script not copying pkg files - TotalCross#572
- Camera: partially reverted changes introduced in version 4.3.3 that caused problems on some devices - TotalCross#574
- Grid: fixed column width sizing - TotalCross#480

## 5.1.2 - 2019-08-05

### Highlights
- Added support for generating QR codes natively on the device - TotalCross#540

> :information_source: **This feature is available on all supported devices, but not on the simulator yet. Refer to the documentation for more information and sample usage**

### Fixed
- Camera: Fixed `CAMERA_NATIVE_NOCOPY` - TotalCross#554
- Fixed support to listening events on Android hardware buttons, broken on version 5.1.0 - TotalCross#557, TotalCross#559, TotalCross#560
- ComboBox: Fixed support for option usePopMenu

## 4.3.9 - 2019-08-05

### Fixed
- tccodesign script: fixed support for push notification

### Added
- Added classes [AsyncTask](https://totalcross.gitbook.io/playbook/apis/asynchronous-task#using-asynctask-class-to-execute-background-tasks) and `ThreadPool` to improve concurrency support, refer to the documentation and the PlayBook for more information and samples

## 5.1.1 - 2019-07-16

### Fixed
- Fixed push and local notifications for Android 8+ - TotalCross#316

## 5.1.0 - 2019-07-04

### Highlights
- YouTube Player: New API for displaying YouTube videos on Android and iOS
- Added support for text prediction when using native keyboard on Android and iOS

### Fixed
- ListContainer: Fixed Check on ListContainer - TotalCross#393
- InputBox: Removed dummy mask that caused trouble when using setMode - TotalCross#506 
- Toast: Fixed using '\n' on the String used on Toast.show - TotalCross#498
- Button: Fixed using an image and no text position - TotalCross#527
- Edit: Fixed base line not showing when running on devices with DP smaller than 1, such as most WinCE devices - TotalCross#389
- PopupMenu: Fixed the Android UI style - TotalCross#369

### Changed
- TabbedContainer: Improved graphical performance and responsiveness

### Added
- MultiEdit: Added fields to set the text gap - TotalCross#489
- Button: Added constant CENTRALIZE to centralize image and text buttons - TotalCross#498
- Settings: Added field `allowBackup`, which may be used to disable Android's automate cloud backup - TotalCross#548

## 4.3.8 - 2019-05-31

### Highlights
- Added support for Android 64 bits - TotalCross#515

### Fixed
- Fixed bug with large CRC or size values overflowing 32 bit integer - TotalCross#529

### Changed
- tccodesign script
  - Added --output parameter
  - Removed lined that deletes UIStoryLaunch
  - Fixed icon problem
  - Changed to use a temporary folder during the process

## 5.0.1 - 2019-05-07

### Added
- Added support for printing on Cielo Lio devices with embedded printer. Refer to the class `totalcross.cielo.sdk.printer.PrinterManager` for usage information.

### Fixed
- Grid: fixed column width sizing - TotalCross#480
- Button: fixed position of contents based on the gap value - TotalCross#487
- CalendarBox: fixed calculation of font size - TotalCross#397
- VirtualKeyboard: fixed Clear button and text input look - TotalCross#397
- Socket: fixed support for open timeout on iOS - TotalCross#128

## 5.0.0 - 2019-04-15

### Highlights
- TotalCross is now aware of the device's screen pixel density and uses this information to scale UI elements uniformly on different platforms. 
 - The font size is now expressed in scaleable pixels (sp) and will now scale uniformly on different screens. The font size should now be expressed as a constant value or based on `Settings.deviceFontHeight`, but **never** based on the screen's resolution or target platform.

> :information_source: **Existing applications may require adjustments to their font sizes!**

- Improved overrall UI responsiveness and reduced application start time on Android
 - Several `Vm.sleep` calls in the vm had their time reduced by 50% or more, some calls were replaced with `Thread.yield()`
 - On Android and Java, reduced overhead in the event thread to improve UI responsiveness

#### Screen density support
All graphical components can now be scaled to have approximately the same physical height on any screen resolution.

##### Density by OS
- Android and iOS: these devices expose a manufacturer-provided screen density value. On TotalCross 5 applications, this value is retrieved by default and used to ensure consistent component sizes across devices
- Java: based on scale, e.g. /scale 0.5 = density 2.0, you can try different densities on the emulator by changing the scale command line argument
- Win32 and WinCE: Most Windows devices do not natively expose screen density information and, thus, are kept at density 1.0.
To improve the look and feel on smaller screens, we use a density of 0.75 on screens with width or height less than 320
- Other platforms: nothing changed (= density 1.0)

##### Font
The font size is now expressed in scalable pixels (sp), this abstracts away the actual physical dimension (screen pixels) of the font. This way, their actual physical size is calculated based on the screen density. e.g:
A font with size of 20sp with different screen resolutions:
- Density 1.0: actual size 20 pixels
- Density 1.5: actual size 30 pixels
- Density 2.0: actual size 40 pixels
- Density 0.75: actual size 15 pixels
This change shall render TotalCross apps ready for different screen sizes with minimal implementation effort. 

>>>
**Updating for 5.0**

Using Font.getDefaultFontSize is no longer necessary, you may now use constant values and the font size will be adjusted accordingly. 
Applications that tried to “guess” the appropriate font size based on screen resolution may need adjustments.
Applications that used Settings.uiAdjustmentsBasedOnFontHeight should have their graphical components resized more uniformly across different platforms and screen densities.
>>>

##### Density Independent Pixels
Controls may now have their dimensions expressed in density independent pixels units (dp), which are scaled for the screen based on its pixel density. e.g:
```java
// adds Button with height of 20dp
add(new Button("Ok"), RIGHT, BOTTOM, PREFERRED, DP + 20);
```
This is now the preferred way of setting the control’s dimensions.

#### Improved support for animations
We now provide a central update event that controls can register for. This event fires periodically and is meant to be used to drive all animations in the framework.
In comparison to using timer events for animations, this approach uses less CPU and memory, and behaves better under high CPU usage scenarios.

>>>
**Updating for 5.0**

Several classes were updated to use the new update events and the effect on them should be noticeable without any changes. In special, we would like to highlight the  ScrollContainer and the ControlAnimation components. 
Users are encouraged to replace similar uses of threads, sleeps and timer events with update events.
>>>

#### Smoother scrolling
The ScrollContainer and the Flick components were updated to provide smoother and more precise scrolling. The changes include the usage of quadratic easing animation and exponential decay.
The Flick also factors in the screen’s pixel density and offers the user the option of tuning the flick acceleration.
Lastly, Flick also supports consecutive drags to increase the scrolling speed.

>>>
**Updating for 5.0**

Most of the changes made are transparent to the user, but there were some breaking changes on Flick. User may have to remove the usage of fields that are no longer available.
>>>

#### Icon control
With the new Icon control it’s easier to add beautiful and meaningful icons to your application.

This is now the preferred way of adding icons to your application and should be favored over image files. One major reason to favor icon font sets over images is that they are highly scalable and will look sharp in any resolution while using only a fraction of the system resources an image would use.
Material Icons are already bundled with the sdk and the IconType interface may be used to implement more font sets.

#### Alpha support for Labels and Icons
It’s now possible to change the opacity of text and icons within Label or Icon controls. This is a key feature to design beautiful applications, as it allows text to remain legible over any background color.
Transparent status bar

Currently only supported on Android, but iOS support will be ready soon.

#### Navigation Drawer
All the previously listed changes were put in use to futher develop the SideMenuContainer, complying with the Navigation Drawer pattern defined by the Material Design guidelines (https://material.io/guidelines/patterns/navigation-drawer.html):
- Action bar height is now 20 dp
- Bar title and menu items using the Roboto Medium font (when available)
- Labels and icons now use the correct font size, weight and opacity
- Keylines and margins are respected both inside and outside the side menu (with a special exception for really small screens)
- Smooth slide-in and out effects, with fade effect during slide removed
- Resting elevation over the content
- Swipe to open support is still in its early stages, but should be functional for most applications
- Adjusts automatically when used with transparent status bar

### Added
- SideMenuContainer.Sub, a collapsible submenu for the navigation drawer
- MaterialWindow, a popup window that slides from the bottom to the top of the screen

### Changed
- Camera: On Android, choosing an image from the Gallery no longer creates a copy of it when not necessary (images from a cloud service still require a local copy) - TotalCross#400
- Java
  - ByteArrayOutputStream: added several missing methods - TotalCross#478
  - System: added method `arraycopy`
  - Charset: method forName now correctly validates the given charset name and may throw `IllegalCharsetNameException`
  - String: added constructor `String(byte[] value, int offset, int count, String encoding)` to allow the creation of strings with a given supported enconding
 
### Deprecated
- Settings: deprecated field `WINDOWSPHONE`, which will be removed in a future release

## 4.3.7 - 2019-05-06

### Added
- Added support for native barcode scanning on iOS, without using any external library - TotalCross#333

## 4.3.6 - 2019-04-13

### Highlights
- Dropped support for Windows Phone 8

### Changed
- HttpStream: Reverts change "Query parameters are now encoded to support the usage of unsafe characters" introduced in 4.3.1 - TotalCross#482, TotalCross#483

## 4.3.5 - 2019-04-01

### Fixed
- Deploy: Fixed bug that would cause a method signature to be included in the constant pool as a class - TotalCross#139, TotalCross#468

## 4.3.4 - 2019-03-20

### Fixed
- Android
  - Reverted back the change reverted on last release and finally fixed the bug that was causing the assets to touble in size - TotalCross#358
- HttpStream: Fixed read operation not retuning EOF when the correct content length is provided on the reply - TotalCross#464

## 4.3.3 - 2019-03-14

- Changed generation of the TotalCross SDK jar:
  - Annotations `@Deprecated` and `@ReplacedByNativeOnDeploy` are no longer stripped by ProGuard - TotalCross#411
  - Argument names are no longer obfuscated
  - The javadoc jar is now being deployed to our Maven repository - TotalCross#391

### Fixed
- Android
  - Fixed deploy not being able to sign the apk when TOTALCROSS3_HOME is set with a relative path - TotalCross#275
  - Reverts change on deploy that seems to be causing the assets to double the size in the apk
  - Fixed Camera NATIVE support
- iOS
  - Fixed screen resolution for newer models such as iPhone XS - TotalCross#308

## 4.3.2 - 2019-01-31

### Fixed
- iOS
  - Usage of "hide keyboard" button on the keyboard - TotalCross#347

## 4.3.1 - 2019-01-14

### Fixed
- URI: Fixed parse of multibyte characters
- Camera: Fixed regression in release 4.2.2 - TotalCross#346
- ArcChart: Fixed coloring of chart when a single series represents 100% of the data - TotalCross#350

### Changed
- HttpStream: Query parameters are now encoded to support the usage of unsafe characters 

### Known issues
- iOS
  - Using the "hide keyboard" button on the keyboard makes it unavailable for the application - TotalCross#347

## 4.3.0 - 2019-01-04

### Highlights
- Added implementation of PBKDF2WithHmacSHA1
- Removed SMS permissions from Android manifest when using the default deploy options, and added the new option /include_sms to deploy including them

### Fixed
- iOS
  - Fixed screen dimensions on IPhone XS
- Resign script tccodesign: 
  - Fixed issue with provided mobileprovision not being added to the Xcode mobileprovisions folder
  - Temp files are now removed before the script execution
  - Fixed issue with certificate name in the following format: iPhone Distribution|Developer: Provided Name (Team ID)
- ImageList: Fixed bug introduced in version 4.2.0 with the correcion of issue TotalCross#192 - TotalCross#291
- DiscoveryAgend: Fixed potential leaks when using service discovery on WinCE
- Grid: Fixed sorting different types of data in Grid - TotalCross#297

### Changed
- Removed SMS permissions from Android manifest when using the default deploy options, and added the new option /include_sms to deploy including them

### Added
- Added implementation of PBKDF2WithHmacSHA1

### Known issues
- Local notifications do not work on Android 8+. This is caused by a change in the security requirements of the Android native notification API that was not listed in the platform's release changelog. This issue will be fixed on the next release of TotalCross 5, but a fix for version 4 won't be issued to maintain our current compatibility with older Android devices.

## 4.2.7 - 2018-12-06

### Fixed
- Camera: Added request for external storage permission - TotalCross#304
- JSONFactory
  - Fixed processing of arrays in complex objects and added support for non-static inner classes - TotalCross#326
  - Fixes bug on json parsing caused by usage of a regex expression

## 4.2.6 - 2018-12-03

### Fixed
- Vm.exec: Added permission REQUEST_INSTALL_PACKAGES to allow the application to install an apk on Android 8+
- TreeMap: Fixed import of java.lang.Comparable

### Changes
- Scanner: Added camera permission request on Android (ZXing/Scandit)

## 4.2.5 - 2018-11-29

### Fixed
- Fixed location permission request

### Changes
- Camera: Removed picture rotation detection on Android - it was source of intermittent bugs on some Samsung and Motorola devices

## 4.2.4 - 2018-11-26

### Fixed
- Fixed permission requests for File constructor, delete and getSize on Android 6+ - TotalCross#317
- Fixed permission request for Camera on Android 6+
- Fixed Vm.exec on Android: intents were not being able to pass external files to other applications - TotalCross#69, TotalCross#320, TotalCross#232
fixes 

### Changes
- GPS permission is no longer requested when the application is launched, instead the permission is automatically requested when required by the application

## 4.2.3 - 2018-11-07

### Fixed
- Fixed regression with the Android deploy that would produce an apk not acceptable by the Play Store
- Radio
  - Fixed autoSplit when the control width is set to PREFERRED

## 4.2.2 - 2018-10-30

### Fixed
- Camera
  - Fixed CAMERA_CUSTOM not taking pictures when the device is held on vertical position, this affected a few models, most notably Samsung tablets - TotalCross#233

### Changes
- Container
  - Method getChildren changed to return an empty array instead of null when the container has no children.

## 4.2.1 - 2018-10-19

### Fixed
- Notification
  - Fixed notification crashing on Android - TotalCross#255
  - Clicking on a notification will no longer start a new instance of the application if there's already one running on background
- PushButtonGroup
  - Fixed regression on PushButtonGroup that made them unclickable on Win32, affecting all dialogs - TotalCross#263

### Changes
- Added a new virtual keyboard that looks more closely to the Android native keyboard and is now the default for Edit and MultiEdit.
- HttpStream: Added support for http methods PUT, PATCH and DELETE in class HttpStream - TotalCross#240

## 4.2.0 - 2018-10-01

### Highlights
- Android
  - Android package is now built with SDK 27 (previous releases were built with SDK 22) and the latest NDK r17b (up from NDK r8b!) - The minimum SDK level required to run TotalCross applications remains unchanged (SDK 10)
  - Applied the Android recommended changes to better handle activities and contexts to prevent possible resource 
  - TotalCross now asks for the user permission to access phone state and location on startup

:construction: **On Android, features that require user permission during runtime may not be working. Please report if you have any trouble with permissions.**

- iOS
  - iOS package is now built with SDK 12
  - Added permission request to use the camera

- General
  - Improved some Color methods to produce better results by properly weighting the RGB components according to our perception of color brightness 
  - Added support for local notifications on JDK, Android and iOS.
  - EscPosPrintStream - New API to handle ESC/POS printer commands
  - Added support for fast IDCT scaling on jpeg images. Combined with the usage of the new utility classes to manage scaling jpeg images, this can greatly reduce the memory footprint of jpeg images and improve graphical performance.

> :information_source: **Using this approach to handle images of approximately 800x800 on medium sized devices can reduce memory consumption by up to 80% while doubling the image loading speed!**

### Fixed
- Fixed bug during activation with some JDK versions - a FileNotFoundException could be thrown when trying to recursively create directories for a new file
- Deploy
  - Will now correctly package tcz dependencies that were split over multiple files - TotalCross#214
  - Added protection for unlikely (but possible) NPE
  - Fixed deploy with Java 10, dependencies are now listed in the jar's Manifest
  - Fixed NPE when deploying for Win32 without initializing Launcher.instance - TotalCross#165
  - Fixed rare (but possible) NPE during deploy
  - Fix path for Win32 dll, case sensitive systems expect binaries on lowercased folders - TotalCross#274
- Scanner
  - Fixed barcode scanning on Dolphin/Honeywell devices - value of barcode's check digit was being carried over to the calculation of the check digit on the next reading - TotalCross#228
- ComboBox
  - Fixed vertical alignment of text - TotalCross#192
- ImageControl
  - Removed duplicated field `effect`
  - Fixed detection of press events
- Toast
  - Fixed Toast appearing relative to the `topMost` window, when it should always be relative to the MainWindow
- MultiButton
  - Fixed graphical bug - a transparent ComboBox arrow was being drawn on the background of the MultiButton (!)
- PushButtonGroup
  - Fixed drag event to allow "giving up" on a press event by dragging outside the button bounds after a press
- MultiEdit
  - Fixed text being hidden with Material style
  - Fixed bug that made every even character to disappear when you typed and reappear when you typed the next one
- AccordionContainer
  - Fixed arrows not changing state when switching focus between multiple collapsible panes
- CalculatorBox
  - Fixed enconding error with plus-minus sign - TotalCross#206
- Grid
  - Fixed column width wrong resize when dragging edge to before column start - TotalCross#186
- JSONFactory
  - Fixed recursive creation of complex objects
  - Improved JSON parser to map methods camel cased to underscored fields in the JSON object

### Changes
- Updated version of the Bouncy Castle dependency
- On Java, Settings.appPath is now initialized even if the Launcher isn't executed - TotalCross#165
- Launcher
  - The Launcher (simulator) can no longer be used without an activation key
- Deploy
  - Check paths from pkg file and throw more meaningful error message when a path is invalid
  - Print Deploy exceptions on System.err instead of System.out and using the default stack trace output
- Whiteboard
  - No longer recreates the content image when repositioned - TotalCross#187, TotalCross#196
- ListBox
  - Deprecated method `add(Object[] moreItems, int startAt, int size)` as it was redundant and more confusing than helpful
- ComboBox
  - Deprecated method `add(Object[] moreItems, int startAt, int size)` as it was redundant and more confusing than helpful
- Radio
  - Added feature autosplit - TotalCross#180

### Added
- EscPosPrintStream - New API which supports several ESC/POS printer commands - refer to the class documentation and samples for more information
- Notification and NotificatonManager - Allows the creation and presentation of local notifications to the user, currently implemented for Android, iOS and Java.
- ImageLoader - New class to help managing image resources, especially jpeg images. Currently no caching is done by the class.
- Image
  - Added static methods `Image.getJpegScaled` and `Image.getJpegBestFit` to load jpeg files using fast IDCT scale, more about this [here](http://jpegclub.org/djpeg/)
- Java
  - Added classes ByteArrayOutputStream and UncheckedIOException 
  - Added classes Charset and UnsupportedCharsetException, added also `String.getBytes(Charset)`
- Added Cp437CharacterConverter, which supports encoding and decoding characters using the CP-437 charset (also known as IBM437, windows-437, among others). Especifically added to be used with EscPosPrintStream to properly support writing text to Leopardo A7.
- Convert
  - Added method `charsetForName(String name)`, which returns one of the registered charsets available
  - Added method `registerCharacterConverter(AbstractCharacterConverter characterConverter)`, which allows users to create and registers their own subclass of AbstractCharacterConverter to support custom encodings
  - Added several aliases to the existing ISO-8859-1 and UTF-8 CharacterConverter classes and changed `setDefaultConverter` to be case insensitive and support any of listed aliases.

> The existing CharacterConverter class and subclasses were changed to extend AbstractCharacterConverter, which extends Charset. The actual support to Java Charset is almost none, the main goal is to allow the usage `String.getBytes(Charset)` with the existing CharacterConverter and let users encode strings with different charsets without changing the charset used by the rest of the application through Convert.setDefaultConverter.

### Deprecated
- ListBox
  - Deprecated method `add(Object[] moreItems, int startAt, int size)` as it was redundant and more confusing than helpful
- ComboBox
  - Deprecated method `add(Object[] moreItems, int startAt, int size)` as it was redundant and more confusing than helpful

### Known issues
- iOS
  - Applications signed for enterprise distribution cannot run on iOS 12 - apparently it fails to validate the certification chain that validates the signed application. It's not clear yet if this was an intended change or a bug, but there's no word from Apple about it yet.
  - Applications signed for distribution through the AppStore cannot be uploaded because of changes on the way image resources are handled starting from iOS 11. We are working on a definite fix for this, in the mean time feel free to contact us to manually package the application for the AppStore. 
- Notification
  - Android lacks support for custom images for notifications, only the default TotalCross logo is supported
  - Notification crashing application on Android - TotalCross#255
- PushButtonGroup
  - The fix for the drag event caused PushButtonGroup to be unclickable on Win32, affecting all dialogs - TotalCross#263

## 4.1.4 - 2018-05-17

### Highlights
- TotalCross is now built with the iOS 11 SDK, as a side effect the minimum iOS version supported by TotalCross is now 8.0 (up from 5.1.1). Applications published on the Apple Store must be updated.

## 4.1.3 - 2018-04-12

### Fixed
- Fixed Edit's material caption animation when navigating using the keyboard
- Fixed `WrapInputStream.read()` - the value returned is now between the range 0-255, as specified by the `InpuStream.read()` documentation. The class `WrapInputStream` is used by `Stream.asInputStream()`
- Fixed `MaterialEffect` to stop discarding `PEN_UP` events sent to the target Control **after** the effect is removed from the target Control
- Implemented `ConnectionManager.getLocalHost()` for iOS
- On iOS, fixed keyboard being closed when navigating to the next text input control using the "Done" button
- Fixed `SideMenuContainer` -  the sidemenu is no longer draggable
- Fixed retrieval of the device's current time on newer Android devices (and possibly other POSIX compliant platforms) - #147
- Fixed `BarButton` only firing a pressed event targeting itself when Material UI style is used - #176
- Fixed redraw after the device is unlocked on Moto G5 Plus - #173
- Fixed javadocs not being included with the SDK

### Changes
- Usage of `Vm.sleep(1)` in the SDK replaced with `Thread.yield()` for clarity sake
- Changes `LineReader` to use `Thread.yield()` between read attempts instead of stoping the Vm for 100 ms
- Spinner's implementation changed to use TimerEvent instead of threads perform the animation
- Edit's material caption animation is faster and will no longer get mixed with the blinking cursor
- `WrapInputStream.read(B[], I, I)` no longer rethrows `totalcross.io.IOException` as `java.io.IOException`
- `WrapOutputStream.write(B[], I, I)` no longer rethrows `totalcross.io.IOException` as `java.io.IOException`
- `WrapInputStream.close()` will now properly close the underlying stream
- `WrapOutputStream.close()` will now properly close the underlying stream
- Changed the way we obtain the current device orientation and screen dimensions on Android, the previous implementations were deprecated

## 4.1.2 - 2018-02-20

### Fixed
- Fixed support for WinCE based scanners that use OpticonH16.dll

### Added
- Added support for native laser scanning for Android based Symbol/Motorola scanners

## 4.1.1 - 2018-02-06

### Highlights
- Launcher default color depth changed from 16 bpp to 24 bpp

### Fixed
- Fixed `Switch` disappearing on Android - calculation of alpha channel applied to the switch was wrong
- Fixed `Socket` and `HttpStream` to properly handle EOF during read operation
- Fixed screen not being shifted when device is in landscape
- Fixed issue where a focused `Edit` would not receive keypress events
- Fixed bug in `Edit` on Android - backspace events would not be issued when the Edit had text but had not received any typing events

### Added
- Added `Edit.canMoveFocus` to disable focus change
- Added `Stream.write(int)`, convenience method to write a single byte to the stream
- Added `ScrollContainer.setScrollBars` to allow subclasses to add or remove scrollbars after its creation
- On Android, Chrome no longer supports using the scheme prefix to display local files. Added workaround to `Vm.exec` to keep backwards compatibility - #148
- Added `Settings.ANDROID_ID`, refer to the Android [documentation](https://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID) for more details
- Added limited support for running Intents through Vm.exec on Android - #155

### Changes
- `Whiteboard` now supports usage of `transparentBackground` to ignore the background color and generate images with transparent background - #153
- Pressing ENTER in a set of `Edit` inside a `ScrollContainer` will now automatically scroll to the next control
- Improved `Control.setRect` error messages, it will now throw distinct messages for invalid width or height

## 4.1.0 - 2018-01-15

### Highlights
- [Firebase for iOS](https://gitlab.com/totalcross/TotalCross/wikis/Features/Post-Notification---Firebase)
- [SMS changes](https://gitlab.com/totalcross/TotalCross/wikis/Features/sms-manager)
 - Implementation for sending and receiving data SMS also disables changing the state of the receiver when the application is paused and resumed
- [Fade transition](https://gitlab.com/totalcross/TotalCross/wikis/Features/fade transition)
- Font support
 - Improved FontGenerator to create better looking fonts. Regenerate your fonts to make them look smoother on device.
 - Spacing between characters was also improved.
 - Fixed support for some unicode characters and handling of the ranges passed to the command line.

### Fixed
- Fixed `ScrollContainer` to properly display the controls if the order is changed
- Fixed MaterialEffect on a `ListBox` that was scrolled up
- Fixed Radio not being correctly painted when checked is set to true within the same event that changed it to false
- Fixed `MaterialEffect` fade out duration - alpha is now computed based on remaining time instead of using a constant decreasing value
- Fixed `ScrollPosition`'s handle not being hidden when released
- Fixed Check and Radio sending PRESSED event when `setChecked` is called, even when `Settings.sendPressEventOnChange` is set to false
- Fixed Launcher to better handle missing or bogus font files when running on desktop
- On Graphics, fixed `NullPointerException` and `ArrayIndexOutOfBoundsException` when repaint is called from a thread
- Fixed `ProgressBar` to retain the z-order when its value is updated #80
- Fixed iOS icons by adding method `colorDist (int rgb1, int rgb2)` and `addFillPoint (int x, int y)`
- Fixed `ImageControl` zooming with poor quality, it was scaling the resized image displayed in the control instead of the original image
- Fixed usage of internal scanners on Android devices
- Fixed bug where screen was not being shifted when changing focus between Edits using `PEN_UP` or ENTER
- Fixed barcode reading with Motorola scanners when the digits of both halves of the barcode were the same (such as 10161016 or 10201020) - #106
- Fixed `NullPointerException` in `Graphics.drawText` that would occur under some situations when the UI is loaded from a thread
- Fixed Window incorrectly calling `onClickedOutside` when a two-finger movement is performed
- Fixed `MaterialEffect` to not apply effects during a flick

### Added
- Added support to Bematech scanner back to the SDK - #100
- Added `Settings.showUIErrors`, which can be set to false to disable UI errors that are shown in desktop only.
- Added `Flick.dontPropagate`, which can be useful if you have two or more intrinsic `ScrollContainers` and dont want to propagate the scroll among them
- Added `ScrollContainer.canShowScrollBar`s, which gives child classes finer control on whether scrollbars should be displayed or not
- Added `ComboBox.getArrowColor` and now you can change the arrow color at runtime
- Added classes `java.awt.Dimension` and its dependencies - `java.awt.geom.Dimension2D`, `java.lang.InternalError`, `java.lang.VirtualMachineError`
- Added method `Long.toString(long i, int radix)` to `java.lang.Long`
- Added method `ConnectionManager.getLocalHostName` to retrieve the host name of the local host - #41

### Changes
- The tcvm.dll no longer requires elevated privileges to be run on Windows desktop
- Changes `Spinner` to have transparent background by default
- Changes `Spinner` to not mess with the colors when created using an `Image`
- On iOS, the application now receives an ENTER key event when the keyboard is closed
- A `RuntimeException` is no longer raised in JavaSE when you add a control to an `AccordionContainer`, and its height reaches zero during animation
- Changed `ImageControl` to paint material effects only if there is an image and `setPressedEventsEnabled` was called
- Now `Edit.autoSelect` puts cursor at end of line instead of begining, matching the behaviour of `MultiEdit`
- Now if you press ENTER in a set of Edits that are inside a `ScrollContainer`, it scrolls automatically to the next control.
- Increased cursor thickness on `Edit` for devices with high resolution
- Changed `Time(char[] sqlTime)` to also parse the milliseconds value (`SQLite.getTime()` now includes milliseconds)

### Deprecated
The following fields and methods were deprecated and should no longer be used
- `File`
 - `readAndClose`
 - `eadAndDelete`
 - `writeAndClose`
 - `read()`
- UIControls
 - `spinnerFore`
 - `spinnerBack`

## 4.0.2 - 2017-09-01

### Fixed
- Methods annotated with `@ReplacedByNativeOnDeploy` and array arguments were not being replaced by their native counterpart on deploy
- Fixed deploy to iOS in Linux without X11 display
- Android binaries are now compiled in release mode, because the Play Store no longer allows debug binaries
- TotalCross should no longer require elevated user access to run on Windows desktop
- Fixed pressed event not being fired when the re-selecting the same item on a `ComboBox`
- Fixed crash on WinCE, invalid function references would make the application crash on startup on some devices

### Changes
- Applied annotation `@ReplacedByNativeOnDeploy` on classes of package `totalcross.crypto`, stack trace line numbers should now be the same either on Java or device

## 4.0.1 - 2017-08-04

### Fixed
- Fixed crash on `File.listFiles` when crawling very big paths
- Fixed support for WinCE devices without the aygshell library, like the Compex PM200
- Fixed `IOException` hierarchy
- Fixed algorithm for `ImageControl.scaleToFit`
- Fixed pressed event not being fired when using a popup with `ComboBox`

### Changes
- Added `CLOSED` state to `File`, which allows it to be properly used in a try-with-resources
- Updated dependency dd-plist.jar to version 1.19

## 4.0.0 - 2017-07-31

### Highlights
- Familiar with the Material design User Experience? Well, you can now give it to your user!
- `@ReplacedByNativeOnDeploy` annotation to denote every method that runs with a native implementation on device
  - The Java implementation is replaced by a native call during the deploy
- `java.util.function.*` functional interfaces of Java 8 to provide a deeper dive into functional programming
  - The project needs to be compiled with Java 8 to works, even if it has been downtargeted by RetroLambda
  - **NOTE**: no default methods nor static methods yet
- Wish to add your dependencies dynamically? Take a look at [`tc-compiler-help`](https://github.com/TotalCross/tc-compiler-help)
  - The file `all.pkg` is dynamically updated with your dependencies and restored to it's original state
  - See build examples:
    1. Build to multiple platforms [here](https://github.com/TotalCross/IFoodUI/blob/c0e96ade3d24539fb2e96cae2637989c6aea5418/src/main/java/tc/samples/like/ifood/IFoodCompile.java#L12)
    1. Must compile with dependenceis `magical-utils`, `tc-utilities` and `tc-components` [here](https://github.com/TotalCross/totalcross-big-file/blob/master/src/main/java/com/totalcross/sample/bigfile/BigFileCompile.java#L11)

### Fixed
- Prevents null pointer when resizing `ScrollContainer`
- Fixed deploy issue with `java.lang.System` while running with Java 8
- Fixed deploy issue with `java.lang.Character` while running in a headless environment

### Changes
- `totalcross.io.Connection` implements `AutoCloseable`, so you may use `try-with-resources` with any `totalcross.io.Stream`, like `HttpStream` or `File`
  - It also warns _Resource leak_ when the compiler detects that you are not releasing a opened resource

## 3.44.3483 - 2017-07-17

### Highlights
- Launcher no longer requires an activation key to run, instead it shows a dismissible popup to input the activation key to be stored on the user's AppData system equivalent directory.
- The stored activation key is used by default for both Launcher and Deploy if the activation key is not provided as a command line argument.

### Fixed
- `AccordionContainer.collapseAll` not using `showAnimation` when set to false.
- AccordionContainer expand/collapse not resizing the parent window.
- Fixed not being able to set focus to a Window under the current top one.
- ImageControl not using the alphaMask that may have been assigned to the Image.
- `Graphics.getAnglePoint` implementation fixed by using the same algorithm of `Graphics.drawPie`.

### Added
- Added missing qsort methods to Vector to allow sorting in ascending or descending order.

## 3.43.3452 - 2017-06-29

### Highlights
- Now you can get Firebase identity token to send a unicast message to a single Android device
  - Check the [wiki page](https://gitlab.com/totalcross/TotalCross/wikis/features/firebase-token) for more details;
  - Also check the [GitHub example](https://github.com/TotalCross/totalcross-firebase-token/);
  - Any news, we will keep updating the post and the source

### Fixed
- Deploy with absolute path in Unix system, it isn't anymore mistakenly recognized as a slash argument
- Fixed `ScrollBar.recomputeParams` to ensure the values calculated are within a valid range.
- Fixed TotalCross.apk deploy, which was not including Litebase libraries.
 - This affected only applications that used Litebase without packaging TotalCross (option `/p`) on Android.

### Added
- Added missing constructor `Throwable(String, Throwable, boolean, boolean)`
- Added missing constructor `Exception(String, Throwable, boolean, boolean)`
- Added missing constructor `RuntimeException(String, Throwable, boolean, boolean)`
- Added method `Image.resizeJpeg(String inputPath, String outputPath, int maxPixelSize)` to resize images on the file system with a smaller memory footprint.
 - On iOS, this method has a native implementation that does not require loading the image on memory, greatly improving performance and memory consumption.
 - On every other platform this method will still load the image on memory to perform the resize, but it should still be preferred over other resize methods as we intend to also add native implementation for other platforms in the future.
- Added `Toast.show(final String message, final int delay, final Window parentWindow)`, where you can pass the window where the tooltip will be shown.
- Added `Font.percentBy` which returns a font resized based on the given percentage.

### Changes
- `SmsManager.sendTextMessage` asks the SMS composer application to automatically exit after the message is sent, switching back to the TotalCross application
- Set the `MessageBox` default colors in the constructor instead of `onPopup`
 - `MessageBox` colors will no longer change if the `UIColors` constants are changed after the object is created
- `ImageControl` resizes and pans the background image with the foreground image.
- `Vm.exec` on Android should now properly execute any video type supported by the viewer, using the MIME type associated with the file extension.
 - Unsupported or wrong file extensions may not work, so make sure the file is ok if this method fails to play it.

### Deprecated
- Those classes are no longer used:
 * `totalcross.phone.PushNotification`
 * `totalcross.ui.event.PushNotificationEvent`

## 3.42.3362 - 2017-05-25

### Highlights
 - New class `SmsManager` to handle sms messages, only on Android for now.
 - New component `SideMenuContainer`, a template to make creating applications using a navbar and a sidemenu. This is an incubating feature, therefore backwards compatibility is not guaranteed for future releases.

### Fixed
 - Fixed saving photo on portrait #47
 - Fixed `AccordionContainer` when `showAnimation` is `false`
 - Fixed support for Samsung's default keyboard on Android which has a bug related to single character event handling and text prediction #35 

### Added
 - Added support for Compex scanners
 - Added `java.sql` exceptions that were being replaced by their equivalents on `totalcross.sql` #40
 - Added `SmsManager` to send and listen to incoming sms messages, only supported on Android now #25
 - Added property `Bar.drawBorders` to remove the component's borders
 - Added constructor for `TopMenu` to allow using other borders type besides the default `ROUND_BORDER`

### Changes 
- Fixed `Edit.clipboardMenu` so that it uses the same font from its parent's Window
- Increased wait time for bluetooth read and write operations on Android #18
- Additional check when drawing `TopMenu` to avoid NPE when there are no items to show
- Deprecated method `Image.setTransparentColor` - use images with alpha channel instead

## 3.41.3331 - 2017-05-10

### Fixed
 - Fixed deploy for Android, which was broken on the last release
    - There were some issues with the CI on the last release that could make the deployed application fail to start on Android, but this was fixed and new tests added to the CI to prevent this problem from happening again 

## 3.41.3327 - 2017-05-09

### Highlights
With this new release, TotalCross now supports [Dagger](https://google.github.io/dagger/). A lightweight DI (dependency injection) framework, maintained by Google and with focus on high performance, low startup and better maintainability, which makes it perfect for mobile applications!


Instructions for usage are available on the [wiki](https://gitlab.com/totalcross/TotalCross/wikis/dagger)


### Added
 - Added `Vector.ensureCapacity(int)` method
 - Added `AccordionContainer.expand(boolean)` and `AccordionContainer.collapse(boolean)`
  - Whether one wish to show an animation, just pass `true` as the argument
 - Added initial support to some classes and interfaces to provides better Java compliance:
  - `java.io.FilterOutputStream`
  - `java.io.PrintStream`
  - `java.lang.Appendable`
  - `java.lang.AssertionError`
  - `java.lang.CharSequence`
  - `java.lang.System`: only the attributes `out` and `err`; both `out` and `err` prints to the `DebugConsole`
  - Deploy time support to referencing `java.lang.ref.Reference` and `java.lang.ref.WeakReference`; no semantics **yet**
  - Deploy time support to referencing `java.util.concurrent.ConcurrentLinkedQueue`; no semantics **yet**
  - `java.inject.Provider`
 - `java.lang.Class.desiredAssertionStatus()` method, returning `false`
 - `java.lang.Error(String, Throwable)` constructor
 - `java.lang.String.contains(CharSequence)` method
 - `java.lang.String.replaceFirst(String, String)` method
 - `java.lang.String.subsequence(int, int)` method
 - `java.lang.StringBuffer.append(CharSequence)` and `java.lang.StringBuffer.append(CharSequence, int, int)` methods
 - `totalcross.ui.font.Font.toString()` method returns the font name and some other properties
  - like `TCFont$N12`
 - Added `ImageControl.setImage(Image, boolean)`, where one may tells that it is desired (or not) to reset positions
 - `OpticonH16.dll` DLL to run proper scanner on WinCE devices

### Changed
 - Project files/folder structure ressembles a Maven/Gradle project
 - `AccordionContainer.expand()` and `AccordionContainer.collapse()` calls `AccordionContainer.expand/collapse(true)`
 - If the deploy process does not end happily, it will throw an exception and return code will be non-zero
 - `ImageControl.setImage(Image)` calls `ImageControl.setImage(Image, boolean)` requesting to reset positions
 - Default background color components changed to white
 - The Win32 deploy now also copies TotalCross files (dll and tcz) to the application folder

### Fixed
 - `ComboBox.clear()` now may default to `clearValueInt` when `clearValueStr` is set but not available on the options
 - Fixed possible stack overflor on `Throwable` constructors calls
 - Fixed `Throwable.toString()` result to match the format used by the JDK
 - Pressing enter on iOS devices fires a `SpecialEvent.ENTER` key

## 3.40.3256 - 2017-04-25

### Added
- Added support to Java 8 Lambda through the usage of [Retrolambda](https://github.com/orfjackal/retrolambda)!
    - [Documentation](https://gitlab.com/totalcross/TotalCross/wikis/retrolambda) on how to use it and [sample](https://github.com/TotalCross/HelloTC) are available.
- Added support to refer to a non-TotalCross-recognized HTTP method and forces
to send the data
- Added class `totalcross.TotalCrossApplication` which can be used as a main
entry point for the application. Check example at [GitHub](https://github.com/TotalCross/HelloTC)
- Added `FONTSIZE` on relative positioning constants
- Added `AccordionContainer.collapseNoAnim`

### Changed
- `totacross.sql.ResultSet` and `totalcross.sql.Statement` interfaces extends
`Autocloseable`
- `LineReader` now uses the current `CharacterConverter`
- Increased height of `ControlBox` to add a few more distance between the
message, the control and the buttons
- `TopMenu.setRect` visibility changed to protected
-  Now if you set `minH` to a negative value, the value will be computed as
`-minH * fmH` (font height); this is useful when the font changes dynamically

### Fixed
- SQLite handling correctly UTF8 (if asked to)
- Added `Sound.fromText` (Android only)
  - Check the snippet $1657156 
- `Socket` open timeout in Java working properly
- Fixed `LineReader` reading of a single non-`\n`-terminanted string
- Fixed `Settings.showMousePosition` not taking into account the scale
- Fixed `TopMenu` when the `widthInPixels` is computed dynamically and in a
rotation the old value is used instead
- Fixed item's height when a single control is used
- Fixed issue when you set
`ScrollContainer.uiAdjustmentsBasedOnFontHeightIsSupported` to false and then
the ScrollContainer reseta it to true
- Fixed some issues with Honeywell and Dolphin scanner

## 3.30.3206 - 2017-03-16

### Added
- Support for IPV6 on iOS

### Changed
- Refactored basic HTTP authentication so that `Proxy Authentication` and `Authentication` uses the same logic int `HttpStream`
- Treating gracefully cancelled photo
- TotalCross VM is now built with Android API level 23

### Fixed
- Fixed basic authentication in `HttpStream`
- Do not deploy placeholder icon, but icon informed icon (Android)
- Workaround to get mac address in Android 6+
- Fixed possible invalid access when freeing a OpenGL texture
- Fixed issue with scanners:
  - Honeywell (Android)
  - Dolphin (WinCE)
- Fixed opening Gallery photo

## 3.30.3098 - 2017-02-14

### Added
- Implemented base 64 decode stream
  - Check example at [GitHub](https://github.com/totalcross/totalcross-big-file)
    - [Use it like a stream](https://github.com/TotalCross/totalcross-big-file/blob/master/src/main/java/com/tc/sample/bigfile/ui/Utils.java#L15)
- Using [Firebase](https://firebase.google.com/) for push messages in Android
  -  Download _google-services.json_ and include within your project and it will automagically work

### Deprecated
- GCM is [deprecated by Google itself](https://developers.google.com/cloud-messaging/)
- There is no longer use for `totalcross.sys.Settings.pushTokenAndroid`

### Known issue
- There is no reliable way _yet_ to treat Firebase notifications in the app

## 3.30.3071 - 2017-01-23

### Added
- Implemented `Class.getCanonicalName()`
- Added `java.lang.Autocloseable` interface
- Added `Window.isSipShown()` to check if the keyboard is currently showing
- Added `java.io.InputStream`
- Added `totalcross.io.Stream.asInputStream()`, which returns a `java.io.InputStream` that wraps the original `totalcross.io.Stream`
- Added `java.io.Reader`
- Added `java.io.InputStreamReader`
- Added `java.io.StringReader`
- Added `java.io.Closeable` interface
- Added `java.io.Flushable` interface
- Added `java.io.Writer`
- Added `java.io.StringWriter`
- Added `StringBuffer.append(String, int, int)`
- [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) statement supported
- Added a SAX-like compiler for JSON, based on [JSON-Simple](https://github.com/fangyidong/json-simple) and its [Cliftonlabs fork](https://github.com/cliftonlabs/json-simple)
- Added instance method `Control.showTip`

### Fixed
- Avoiding overflow operations within `Long.compareTo(Long)`
- End of stream treated properly in zlib, returns `-1` instead of throwing an exception
- Fixed deploy for in-house distribution (iOS)

### Deprecated
- Trying to get mac address on Android 6+ will return a constant `02:00:00:00:00:00` due [to provide users with greater data protection](https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id)

### Known issues
- `Class.getCanonicalName()` doesn't return the canonical name, but defaults to `Class.getName()`

[7.0.0]: https://github.com/totalcross/TotalCross/compare/v6.1.1...v7.0.0
