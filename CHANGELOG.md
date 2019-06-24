# TotalCross Change Log
All notable changes to this project will be documented in this file.

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