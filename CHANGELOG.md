# TotalCross Change Log
All notable changes to this project will be documented in this file.

## Unreleased

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
  - like ``
 - Added `ImageControl.setImage(Image, boolean)`, where one may tells that it is desired (or not) to reset positions
 - `OpticonH16.dll` DLL to run proper scanner on WinCE devices

### Changed
 - Project files/folder structure ressembles a Maven/Gradle project
 - `AccordionContainer.expand()` and `AccordionContainer.collapse()` calls `AccordionContainer.expand/collapse(true)`
 - If the deploy process does not end happily, it will throw an exception and return code will be non-zero
 - `ImageControl.setImage(Image)` calls `ImageControl.setImage(Image, boolean)` requesting to reset positions

### Fixed
 - `ComboBox.clear()` defaults to `clearValueStr`
 - Fixed possible recurrent `Throwable` constructors calls
 - Pressing enter on iOS devices fires a `SpecialEvent` key

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