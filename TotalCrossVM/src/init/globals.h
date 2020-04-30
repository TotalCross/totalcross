// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef GLOBALS_H
#define GLOBALS_H

#ifdef __cplusplus
extern "C" {
#endif

// tcclass.c
extern Hashtable htLoadedClasses;
extern TCClassArray vLoadedClasses;

// tcexception.c
extern CharP throwableAsCharP[(int32)ThrowableCount];

// event_c.h
extern int32 lastPenX, lastPenY, actionStart;
extern int32 lastW,lastH;
extern int32 ascrHRes,ascrVRes;
#if defined(WIN32)
extern uint8 keyIsDown[256];
extern bool dontPostOnChar;
extern HANDLE hModuleTCVM;
#elif defined(ANDROID)
extern jmethodID jeventIsAvailable,jpumpEvents;
extern bool appPaused;
#endif
#if defined(ANDROID) || defined(darwin) || defined(WP8)
extern int32 deviceFontHeight,iosScale;
#endif

// GoogleMaps.c
#ifdef ANDROID
extern jmethodID jshowGoogleMaps,jshowRoute;
#endif

// startup.c
extern bool traceOn;
extern char commandLine[256];
extern int32 exitCode;
extern bool rebootOnExit;
extern bool destroyingApplication;
extern TCObject mainClass;  // the instance being executed
extern bool isMainWindow;   // extends MainWindow ?
#if defined(ANDROID)
JavaVM* androidJVM;
extern jobject applicationObj, applicationContext;
extern jclass applicationClass,jRadioDevice4A,jBluetooth4A,jConnectionManager4A,jSmsManager4A,jNotificationManager4A,jFirebaseInstanceId,jTcFirebaseUtils,jCieloPrinterManager4A;
extern jfieldID jshowingAlert,jhardwareKeyboardIsVisible;
extern jfieldID jsipVisible,jappTitleH;
extern jmethodID jgetHeight;
#elif defined WIN32 || defined linux
extern TCHAR exeName[MAX_PATHNAME];
#endif

// graphicsprimitives.c
extern uint8 *lookupR, *lookupG, *lookupB, *lookupGray; // on 8 bpp screens
extern int32* controlEnableUpdateScreenPtr;
extern TScreenSurface screen;
extern TCClass uiColorsClass;
extern int32* shiftScreenColorP;
extern int32* vistaFadeStepP;
extern TCClass imageClass;
extern int32 totalTextureLoaded;

// mem.c
extern uint32 maxAvail; // in bytes
extern bool warnOnExit;
extern bool leakCheckingEnabled;
extern VoidPs* createdHeaps;
extern int32 totalAllocated, maxAllocated, allocCount, freeCount;
extern bool showMemoryMessagesAtExit;

// PalmFont_c.h
extern int32 maxFontSize, minFontSize, normalFontSize;
extern FontFile defaultFont;
extern int32 *tabSizeField;
extern Hashtable htUF;
extern VoidPs* openFonts;
extern Heap fontsHeap;

// win/gfx_Graphics_c.h
#ifdef WIN32
extern HWND mainHWnd;
extern bool bSipUp; //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
#endif

// Settings.c
extern TCClass settingsClass;
extern TTCSettings tcSettings;
#if defined (WINCE)
extern TVirtualKeyboardSettings vkSettings;
#endif

// demo.c
#ifdef ANDROID
extern jmethodID jsetElapsed;
#endif

// objectmemorymanager.c    
extern bool runningGC,runningFinalizer,disableGC,callGConMainThread;
extern TCObjectArray freeList; // the array with lists of free objects
extern TCObjectArray usedList; // the array with lists of used objects (allocated after the last GC)
extern TCObjectArray lockList; // locked objects list
extern uint32 markedAsUsed; // starts as 1
extern uint32 objCreated,skippedGC,objLocked; // a few counters
extern int32 lastGC, markedImages;
extern Heap ommHeap;
extern Heap chunksHeap;
extern Stack objStack;
#if defined(ENABLE_TEST_SUITE)
// The garbage collector tests requires that no objects are created, so we cache the state, then restore it when the test finishes
extern bool canTraverse;
extern TCObjectArray freeList2; // the array with lists of free objects
extern TCObjectArray usedList2; // the array with lists of used objects (allocated after the last GC)
extern TCObjectArray lockList2; // locked objects list
extern uint32 markedAsUsed2; // starts as 1
extern uint32 gcCount2,objCreated2,skippedGC2,objLocked2; // the current gc count
extern Heap ommHeap2,chunksHeap2;
extern Stack objStack2;
#endif

// context.c
extern Context mainContext,gcContext,lifeContext;
#define MAX_CONTEXTS 100
extern Context contexts[MAX_CONTEXTS];


// tcvm.c
extern int32 vmTweaks;
extern bool showKeyCodes;
extern int32 profilerMaxMem;
extern TCClass lockClass;
extern Hashtable htMutexes;

// linux/graphicsprimitives.c, linux/event_c.h, darwin/event.m, tcview.m
#if !defined(WIN32)
extern void *deviceCtx; // The device context points a structure containing platform specific data that have to handled in platform specific code only, that's why we don't define a structure here insofar some platform specific data can't be defined in plain C (such as SymbianOS C++ classes, iPhone objC data structures, ...) Currently this pointer is mirrored in ScreenSurface in the extension field but this may change sooner or later.
#endif

// utils.c
extern int32 firstTS;
#ifdef ANDROID
extern jmethodID jlistTCZs,jgetFreeMemory;
#endif

// file.c
#ifdef ANDROID
extern jmethodID jgetSDCardPath;
#endif

// vm.c
#ifdef ANDROID
extern jmethodID jvmFuncI,jvmExec;
#endif

// debug.c
extern CharP debugstr;
extern bool consoleAllocated;
#ifdef ANDROID
extern jmethodID jalert;
#endif

// nativelib.c
extern VoidPs* openNativeLibs;

// native proc addresses for iOS and Android
extern Hashtable htNativeProcAddresses;

// tcz.c
extern VoidPs* openTCZs;
#ifdef ANDROID
jmethodID jreadTCZ, jfindTCZ;
#endif

// event.c
extern bool appExitThrown;
extern bool keepRunning;
extern bool eventsInitialized;
extern int32 nextTimerTick;
extern bool isDragging;
extern Method _onTimerTick, _postEvent;
extern Int32Array interceptedSpecialKeys;

// Vm_c.h
extern int32 oldAutoOffValue; // if not 0, the device is in NEVER-SLEEP mode, and the old value will be restored when the vm quits
#ifdef ANDROID
extern jmethodID jclipboard;
#endif

// Convert.c
extern TCObject *charConverterPtr;
extern TCClass ISO88591CharacterConverter, UTF8CharacterConverter;

// media_Sound.c
extern TSoundSettings soundSettings;
#ifdef ANDROID
extern jmethodID jtone,jsoundEnable,jsoundPlay, jsoundToText, jsoundFromText;
#endif

// money
#ifdef ANDROID
jmethodID jadsFunc;
#endif

// YoutubePlayer.c
#ifdef ANDROID
extern jmethodID jplayYoutube;
#endif

// ConnectionManager.c
extern TCClass connMgrClass;

// win/Socket_c.h
#ifdef WIN32
extern int32 WSACount;
#endif

// xml/xml_Tokenizer.c
extern bool xmlInitialized;

// ssl_SSL.c
extern Hashtable htSSLSocket;
extern Heap heapSSLSocket;

#ifdef ANDROID
extern jmethodID jshowCamera,jgetNativeResolutions, jgetDefaultToString, jzxing;

// android/GPS_c.h
extern jmethodID jgpsFunc,jcellinfoUpdate;

// android/Dial_c.h
extern jmethodID jdial;
#endif

// tcthread.c
extern int32 threadCount;

// class.c
extern TCObject *voidTYPE,*booleanTYPE, *byteTYPE, *shortTYPE, *intTYPE, *longTYPE, *floatTYPE, *doubleTYPE, *charTYPE;

// object.c
extern TCClass cloneable;

// These are set in the application's constructor
extern uint32 applicationId;
extern char applicationIdStr[5];

// These are set when the VM is initialized, and their values are copied into totalcross.sys.Settings.
extern CharP platform; // always a constant
extern char userName[42];
extern char appPath[MAX_PATHNAME];
extern char vmPath[MAX_PATHNAME];
extern char dataPath[MAX_PATHNAME];
extern char mainClassName[MAX_PATHNAME];
extern bool isMotoQ;
extern bool isWindowsMobile;

/***********************   METHODS THAT CAN BE USED BY LIBRARIES TO ACCESS THE GLOBALS  *************************/

#ifdef WIN32
TC_API HWND getMainWindowHandle();
typedef HWND (*getMainWindowHandleFunc)();
#endif

TC_API UInt32 getApplicationId();
typedef UInt32 (*getApplicationIdFunc)();
TC_API CharP getApplicationIdStr();
typedef CharP (*getApplicationIdStrFunc)();
TC_API TCObject getMainClass();
typedef TCObject (*getMainClassFunc)();
TC_API CharP getVMPath();
typedef CharP (*getVMPathFunc)();
TC_API CharP getAppPath();
typedef CharP (*getAppPathFunc)();
TC_API CharP getUserName();
typedef CharP (*getUserNameFunc)();

#ifdef WINCE
extern HINSTANCE coreDll, cellcoreDll;
#endif

#if defined (WIN32)
 extern bool initWinsock();
 extern void closeWinsock();
#endif

#if defined (WINCE)
 extern bool initAygshell();
 extern void closeAygshell();
#endif

#if defined (HEADLESS)
extern bool initGpiod();
extern void closeGpiod();
#endif

bool initGlobals();
void destroyGlobals();

#ifdef __cplusplus
}
#endif

#endif
