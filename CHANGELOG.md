# TotalCross Change Log
All notable changes to this project will be documented in this file.

## Unreleased

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