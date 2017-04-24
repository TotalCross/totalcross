# TotalCross Change Log
All notable changes to this project will be documented in this file.

## Unreleased

### Added
- Added support to refer to a non-TotalCross-recognized HTTP method and forces
to send the data
- Adds new class `totalcross.TotalCrossApplication` which can be used as a main
entry point for the application
- Added `FONTSIZE` on relative positioning constants
- Added `AccordionContainer.collapseNoAnim`

### Changed
- `totacross.sql.ResultSet` and `totalcross.sql.Statement` interfaces extends
`Autocloseable`
- Handling cancelled photo gracefully
- `LineReader` now uses the current `CharacterConverter`
- Increased height of `ControlBox` to add a few more distance between the
message, the control and the buttons
- `TopMenu.setRect` is protected now
-  Now if you set `minH` to a negative value, the value will be computed as
`-minH * fmH` (font height); this is useful when the font changes dynamically

### Fixed
- SQLite handling correctly UTF8 (if asked to)
- Added `Sound.fromText` (Andoird only)
  - Check the snippet $1657156 
- `Socket` open timeout in Java working properly
- Fixed `LineReader` reading of a single non-`\n`-terminanted string
- Fixed `Settings.showMousePosition` not taking into account the scale
- Fixed `TopMenu` when the `widthInPixels` is computed dynamically and in a
rotation the old value is used instead
- Fixed item's height when a single control is used
- Fixed issue when you set
`ScrollContainer.uiAdjustmentsBasedOnFontHeightIsSupported` to false and then
the ScrollContainer reset it to true
- Fixed some issues with Honeywell and Dolphin scanner

## 3.30.3206 - 2017-03-16

### Added
- Support to IPV6 in iOS

### Changed
- Refactoring basic HTTP authentication so that `Proxy Authentication` and `Authentication` uses the same logic int `HttpStream`
- Treating gracefully cancelled photo
- Compiling TotalCross VM in Andorid to API level 23

### Fixed
- Fixed basic authentication in `HttpStream`
- Do not deploy placeholder icon, but icon informed icon (Android)
- Workaround to get mac address in Android 6+
- Fixed possible invalid access to free an OpenGL texture
- Fixed issue with sacenners:
  - Honeywell (Android)
  - Dolphin (WinCE)
- Fixed open Gallery photo

## 3.30.3098 - 2017-02-14

### Added
- Implementing base 64 decode stream
  - Check example at [GitHub](https://github.com/totalcross/totalcross-big-file)
    - [Use it like a stream](https://github.com/TotalCross/totalcross-big-file/blob/master/src/main/java/com/tc/sample/bigfile/ui/Utils.java#L15)
- Using [Firebase](https://firebase.google.com/) for push messages in Android
  -  Download _google-services.json_, put in within the project and it will automogically works

### Deprecated
- GCM is [deprecated by Google itself](https://developers.google.com/cloud-messaging/)
- There is no longer use for `totalcross.sys.Settings.pushTokenAndroid`

### Known issue
- There is no reliable way _yet_ to treat Firebase notifications in the app

## 3.30.3071 - 2017-01-23

### Added
- Implementing `Class.getCanonicalName()`
- Added `Autocloseable` interface
- `Window.isSipShown()` tells wheter or not the keyboard is currently showing
- Added `InputStream`
- `totalcross.io.Stream` can generate a `InputStream`
- Added `Reader`
- Added `InputStreamReader`
- Added `StringReader`
- Added `Closeable` interface<
- Added `Flushable` interface
- Added `Writer`
- Added `StringWriter`
- Added `StringBuffer.append(String, int, int)`
- [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) statement supported
- Adding a SAX-like compiler for JSON, based on [JSON-Simple](https://github.com/fangyidong/json-simple) and its [Cliftonlabs fork](https://github.com/cliftonlabs/json-simple)
- Adding an instance method to show a tool tip to a `Control`

### Fixed
- Avoiding overflow operations within `Long.compareTo(Long)`
- End of stream treated properly in zlib, returns `-1` instead of throwing an exception<
- Fixes deploy to in-house distribution (iOS)

### Deprecated
- Trying to get mac address in Android 6+ will return a constant `02:00:00:00:00:00` due [to provide users with greater data protection](https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id)

### Known issues
- `Class.getCanonicalName()` doesn't return the canonical name, but defaults to `Class.getName()`