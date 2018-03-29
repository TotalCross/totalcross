# TotalCross Change Log
All notable changes to this project will be documented in this file.

## 4.2.0

### Highlights
- Added support for local notifications on JDK, Android and iOS. Known bugs and limitations:
 - Notification is crashing on iOS 10+
 - Android lacks support for custom images for notifications, only the default TotalCross logo is supported
- Added support for fast IDCT scaling on jpeg images. Combined with the usage of the new utility classes to manage scaling jpeg images, this can greatly reduce the memory footprint of jpeg images and improve graphical performance.

> :information_source: **Using this approach to handle images of approximately 800x800 on medium sized devices can reduce memory consumption by up to 80% while doubling the image loading speed!**


### Added
- Added static method `Image.getScaledJpeg` to load jpeg files using fast IDCT scale, more about this [here](http://jpegclub.org/djpeg/)

> :construction: This is an incubating feature and subject to API changes

- Added utility classes `SimpleImageInfo` and `ImageLoader` to help managing multiple scaled instances of the same image

> :construction: This is an incubating feature and subject to API changes

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