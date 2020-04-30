// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef NATIVELIB_H
#define NATIVELIB_H

#ifdef __cplusplus
 extern "C" {
#endif

/// Load a library with the given name
VoidP loadLibrary(const char *libName);
/// Unload a library
void unloadLibrary(VoidP libPtr);
/// Gets the pointer of a function, given its module and name. If module is null,
/// the function is searched in the current module.
VoidP getProcAddress(const VoidP module, const char *funcName);
typedef VoidP (*getProcAddressFunc)(const VoidP module, const char *funcName);
/// Calls the HandleEvent function for each library. Returns true if the event was handled in one of them and false otherwise.
bool handleEvent(VoidP event);
/// Search in the attached libraries if one of them has the given function name
VoidP findProcAddress(CharP funcName, uint32* ref);
/// Attach the given native library to the list of attached native libraries
bool attachNativeLib(Context currentContext, CharP name);

/// Called by the vm when it exits to destroy all native libs.
void destroyNativeLib();

typedef struct
{
   VoidP handle;
   getProcAddressFunc getProcAddress; // Palm OS dlls must request the address of each API function, and to do this they require getProcAddress
   alertFunc alert;
   Context currentContext;
   CharP commandLine; // the command line passed to the executable that called this dll
#ifdef WIN32
   HWND mainHWnd;
#endif   
} *OpenParams, TOpenParams;

/// Must be implemented by a native library. Returns true if library initialization was successful.
typedef bool (*NativeLibOpenFunc)(OpenParams params);
/// May be implemented by a native library. Called by the vm when it is finishing.
typedef void (*NativeLibCloseFunc)();
/// May be implemented by a native library. Called by the vm to let the library handle an event.
/// Must return true if the event was handled, false otherwise.
typedef bool (*NativeLibHandleEventFunc)(VoidP eventP);

typedef struct
{
   VoidP handle;
   uint32 ref;
   NativeLibOpenFunc     LibOpen;        // required
   NativeLibCloseFunc    LibClose;       // not required
   NativeLibHandleEventFunc HandleEvent; // not required
} TNativeLib, *NativeLib;

#ifdef __cplusplus
 } // __cplusplus
#endif

#endif
