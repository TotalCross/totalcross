/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



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

#ifdef PALMOS
Err LinkModule(UInt32 type, UInt32 creator, void **dispatchTableP, UInt32 *numEntriesP); // also used by tcSettings
#endif

/// Called by the vm when it exits to destroy all native libs.
void destroyNativeLib();

typedef struct
{
   VoidP handle;
#ifdef PALMOS
   uint32 ref;
#endif
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

#if defined(PALMOS)
 // If we're built for an ARM device, then these macros are used to setup/restore
 // the ELF GOT table (Global Offset Table) register R8 for nested library calls.
 // On ARM, we reserved R8 as the native lib GOT table pointer, thus we never change
 // the VM GOT table pointed by R10.
 // On PalmOS 5, R9 is used by the operating system and cannot be changed neither.
 // So, to compile your ARM native lib never forget these gcc settings:
 // -fPIC -msoft-float -ffixed-r9 -ffixed-r10 -mpic-register=r8 -msingle-pic-base -march=armv4t
 // attention! We cannot use such an expression 'register void *got asm("r8")'
 // because we cannot forbid optimization.
 #define EnterLibrary(newGot) \
     register void *saved_got; \
     asm volatile ("mov %0, r8\n" : "=r" (saved_got) :); \
     asm volatile ("mov r8, %0\n" : : "r" (newGot));
 #define ExitLibrary() \
     asm volatile ("mov r8,%0" : : "r" (saved_got));
 #else
 #define EnterLibrary(newGot)
 #define ExitLibrary()
#endif

#ifdef __cplusplus
 } // __cplusplus
#endif

#endif
