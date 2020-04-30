// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

// tcclass.c
Hashtable htLoadedClasses = { 0 };
TCClassArray vLoadedClasses = { 0 };

// tcexception.c
CharP throwableAsCharP[(int32)ThrowableCount] = { 0 };

// event_c.h
int32 lastPenX = 0;
int32 lastPenY = 0;
int32 actionStart = 0;
int32 lastW = -2;
int32 lastH = 0;
int32 ascrHRes = 0;
int32 ascrVRes = 0;
#if defined(WIN32)
uint8 keyIsDown[256] = { 0 };
bool dontPostOnChar = false;
HANDLE hModuleTCVM = INVALID_HANDLE_VALUE;
#elif defined(ANDROID)
jmethodID jeventIsAvailable;
jmethodID jpumpEvents;
bool appPaused = false;
#endif
#if defined(ANDROID) || defined(darwin) || defined(WP8)
int32 deviceFontHeight = 0;
int32 iosScale = 0;
#endif

// GoogleMaps.c
#ifdef ANDROID
jmethodID jshowGoogleMaps;
jmethodID jshowRoute;
#endif

// startup.c
bool traceOn = false;
char commandLine[256] = { 0 };
int32 exitCode = 0;
bool rebootOnExit = false;
bool destroyingApplication = false;
TCObject mainClass = { 0 };  // the instance being executed
bool isMainWindow = false;   // extends MainWindow ?
#if defined(ANDROID)
JavaVM* androidJVM;
jobject applicationObj, applicationContext;
jclass applicationClass,jRadioDevice4A,jBluetooth4A,jConnectionManager4A,jSmsManager4A,jNotificationManager4A,jFirebaseInstanceId,jTcFirebaseUtils,jCieloPrinterManager4A;
jfieldID jshowingAlert,jhardwareKeyboardIsVisible;
jfieldID jsipVisible,jappTitleH;
jmethodID jgetHeight;
#elif defined WIN32 || defined linux
TCHAR exeName[MAX_PATHNAME];
#endif

// graphicsprimitives.c
// on 8 bpp screens BEGIN
uint8* lookupR = NULL;
uint8* lookupG = NULL;
uint8* lookupB = NULL;
uint8* lookupGray = NULL;
// on 8 bpp screens END
int32* controlEnableUpdateScreenPtr = NULL;
TScreenSurface screen = { 0 };
TCClass uiColorsClass = { 0 };
int32* shiftScreenColorP = NULL;
int32* vistaFadeStepP = NULL;
TCClass imageClass;
int32 totalTextureLoaded;


// mem.c
#ifdef INITIAL_MEM
uint32 maxAvail = INITIAL_MEM; // in bytes
#endif
bool warnOnExit = false;
bool leakCheckingEnabled = false;
bool showMemoryMessagesAtExit = false;
VoidPs* createdHeaps = NULL;
int32 totalAllocated = 0;
int32 maxAllocated = 0;
int32 allocCount = 0;
int32 freeCount = 0;
DECLARE_MUTEX(createdHeaps);

// PalmFont_c.h
int32 maxFontSize = 0;
int32 minFontSize = 0;
int32 normalFontSize = 0;

FontFile defaultFont = NULL;
int32 *tabSizeField = NULL;
Hashtable htUF = { 0 };
VoidPs* openFonts = NULL;
Heap fontsHeap = NULL;

// win/gfx_Graphics_c.h
#ifdef WIN32
HWND mainHWnd = NULL;
bool bSipUp = false; //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
#endif

// Settings.c
TCClass settingsClass = { 0 };
TTCSettings tcSettings = { 0 };
#if defined (WINCE)
TVirtualKeyboardSettings vkSettings;
#endif

// demo.c
#ifdef ANDROID
jmethodID jsetElapsed;
#endif

// objectmemorymanager.c 
bool callGConMainThread = 0;
bool disableGC = 0;
bool runningGC = 0;
bool runningFinalizer = 0;
TCObjectArray freeList = { 0 }; // the array with lists of free objects
TCObjectArray usedList = { 0 }; // the array with lists of used objects (allocated after the last GC)
TCObjectArray lockList = { 0 }; // locked objects list
uint32 markedAsUsed = 1; // starts as 1
uint32 objCreated = 0;
uint32 skippedGC = 0;
uint32 objLocked = 0; // a few counters
int32 lastGC = 0, markedImages = 0;
Heap ommHeap = NULL;
Heap chunksHeap = NULL;
Stack objStack = NULL;
#if defined(ENABLE_TEST_SUITE)
// The garbage collector tests requires that no objects are created, so we cache the state, then restore it when the test finishes
bool canTraverse=true;
TCObjectArray freeList2 = { 0 }; // the array with lists of free objects
TCObjectArray usedList2 = { 0 }; // the array with lists of used objects (allocated after the last GC)
TCObjectArray lockList2 = { 0 }; // locked objects list
uint32 markedAsUsed2 = 1; // starts as 1
// the current gc count
uint32 gcCount2 = 0;
uint32 objCreated2 = 0;
uint32 skippedGC2 = 0;
uint32 objLocked2 = 0;


Heap ommHeap2 = NULL;
Heap chunksHeap2 = NULL;
Stack objStack2 = NULL;
#endif

// context.c
Context mainContext = NULL;
Context gcContext = NULL;
Context lifeContext = NULL;
Context contexts[MAX_CONTEXTS] = { 0 };

// tcvm.c
int32 vmTweaks = 0;
bool showKeyCodes = false;
int32 profilerMaxMem = 0; // guich@tc111_4 - also on mem.c
TCClass lockClass = { 0 };
Hashtable htMutexes = { 0 };

// file.c
#ifdef ANDROID
jmethodID jgetSDCardPath;
#endif

// linux/graphicsprimitives.c, linux/event_c.h, darwin/event.m, tcview.m
#if !defined(WIN32) || defined WP8
void *deviceCtx = NULL; // The device context points a structure containing platform specific data that have to handled in platform specific code only, that's why we don't define a structure here insofar some platform specific data can't be defined in plain C (such as SymbianOS C++ classes, iPhone objC data structures, ...) Currently this pointer is mirrored in ScreenSurface in the extension field but this may change sooner or later.
#endif
        
// vm.c
#ifdef ANDROID
jmethodID jvmFuncI;
jmethodID jvmExec;
#endif

// utils.c
int32 firstTS = 0;
#ifdef ANDROID
jmethodID jlistTCZs,jgetFreeMemory;
#endif

// debug.c
CharP debugstr = NULL;
bool consoleAllocated = false;
#ifdef ANDROID
jmethodID jalert;
#endif

// nativelib.c
VoidPs* openNativeLibs = NULL;

//native proc addresses for iOS and Android
Hashtable htNativeProcAddresses = { 0 };

// tcz.c
VoidPs* openTCZs = NULL;
#ifdef ANDROID
jmethodID jreadTCZ;
jmethodID jfindTCZ;
#endif

// event.c
bool appExitThrown = false;
bool keepRunning = true; // don't remove from here!
bool eventsInitialized = false;
int32 nextTimerTick = 0;
bool isDragging = false;
Method _onTimerTick = NULL;
Method _postEvent = NULL;
Int32Array interceptedSpecialKeys = NULL;

// Vm_c.h
int32 oldAutoOffValue = 0;
#ifdef ANDROID
jmethodID jclipboard;
#endif

// Convert.c
TCObject *charConverterPtr;
TCClass ISO88591CharacterConverter, UTF8CharacterConverter;

// media_Sound.c
TSoundSettings soundSettings;
#ifdef ANDROID
jmethodID jtone, jsoundToText, jsoundFromText;
jmethodID jsoundEnable,jsoundPlay;
#endif

// money
#ifdef ANDROID
jmethodID jadsFunc;
#endif

// ConnectionManager.c
TCClass connMgrClass = { 0 };

// win/Socket_c.h
#ifdef WIN32
int32 WSACount = 0;
#endif

// xml/xml_Tokenizer.c
bool xmlInitialized = false;

// ssl_SSL.c
Hashtable htSSLSocket = { 0 };
Heap heapSSLSocket = NULL;
DECLARE_MUTEX(htSSL);

#ifdef ANDROID
jmethodID jshowCamera;
jmethodID jgetNativeResolutions;
jmethodID jgetDefaultToString;
jmethodID jzxing;
jmethodID jplayYoutube;

// android/GPS_c.h
jmethodID jgpsFunc;
jmethodID jcellinfoUpdate;

// android/Dial_c.h
jmethodID jdial;
#endif

// tcthread.c
int32 threadCount = 0;

// class.c
TCObject *voidTYPE, *booleanTYPE, *byteTYPE, *shortTYPE, *intTYPE, *longTYPE, *floatTYPE, *doubleTYPE, *charTYPE;

TCClass cloneable;

// These are set in the application's constructor
uint32 applicationId = 0;
char applicationIdStr[5] = { 0 };

// These are set when the VM is initialized, and their values are copied into totalcross.sys.Settings.
CharP platform = NULL; // always a constant
char userName[42] = { 0 };
char appPath[MAX_PATHNAME] = { 0 };
char vmPath[MAX_PATHNAME] = { 0 };
char dataPath[MAX_PATHNAME] = { 0 };
char mainClassName[MAX_PATHNAME] = { 0 };
bool isMotoQ = false;
bool isWindowsMobile = false;

#ifdef WINCE
HINSTANCE coreDll;
HINSTANCE cellcoreDll;
#endif

DECLARE_MUTEX(tcz);
DECLARE_MUTEX(metAndCls);
DECLARE_MUTEX(omm);
DECLARE_MUTEX(screen);
DECLARE_MUTEX(opengl);
DECLARE_MUTEX(alloc);
DECLARE_MUTEX(fonts);
DECLARE_MUTEX(mutexes);

///////////////////////////////////////////////////////////////////////////////////////////////////

bool initGlobals()
{
	SETUP_MUTEX;
   INIT_MUTEX(opengl);
   INIT_MUTEX(omm);
   INIT_MUTEX(tcz);
   INIT_MUTEX(metAndCls);
   INIT_MUTEX(alloc);
   INIT_MUTEX(screen);
   INIT_MUTEX(htSSL); 
   INIT_MUTEX(createdHeaps);
   INIT_MUTEX(fonts);
   INIT_MUTEX(mutexes);
#if defined (WIN32) || defined (WINCE)
   initWinsock();
#endif
#ifdef WINCE
	initAygshell();
   coreDll = LoadLibrary(TEXT("coredll.dll"));
   cellcoreDll = LoadLibrary(TEXT("cellcore.dll"));
#elif defined (HEADLESS) && defined (__arm__)
   initGpiod();
#endif
   return true;
}

void destroyGlobals()
{
   DESTROY_MUTEX(omm);   
   DESTROY_MUTEX(tcz);
   DESTROY_MUTEX(metAndCls);
   DESTROY_MUTEX(screen);
   DESTROY_MUTEX(htSSL);
   DESTROY_MUTEX(createdHeaps);
   DESTROY_MUTEX(alloc);
   DESTROY_MUTEX(fonts);
   DESTROY_MUTEX(mutexes);
#if defined (WIN32) || defined (WINCE)
   closeWinsock();
#endif
#ifdef WINCE
  closeAygshell();
   if (coreDll != null) FreeLibrary(coreDll);
   if (cellcoreDll != null) FreeLibrary(cellcoreDll);
#elif defined (HEADLESS) && defined (__arm__)
   closeGpiod();
#endif
}

TC_API UInt32 getApplicationId()     {return applicationId;    }
TC_API CharP  getApplicationIdStr()  {return applicationIdStr; }
TC_API CharP  getVMPath()            {return vmPath;           }
TC_API CharP  getAppPath()           {return appPath;          }
TC_API CharP  getUserName()          {return userName;         }
TC_API TCObject getMainClass()       {return mainClass;        }

#if defined (WIN32)
TC_API HWND getMainWindowHandle()    {return mainHWnd;         }
#endif
