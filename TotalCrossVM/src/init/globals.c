/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "tcvm.h"

// tcclass.c
Hashtable htLoadedClasses;
TCClassArray vLoadedClasses;

// tcexception.c
CharP throwableAsCharP[(int32)ThrowableCount];

// event_c.h
int32 lastPenX, lastPenY, actionStart;
int32 lastW = -2,lastH;
int32 ascrHRes,ascrVRes;
#if defined(WIN32)
uint8 keyIsDown[256];
bool dontPostOnChar;
#elif defined(ANDROID)
jmethodID jeventIsAvailable,jpumpEvents;
bool appPaused;                         
#endif
#if defined(ANDROID) || defined(darwin)
int32 deviceFontHeight,iosScale;
#endif

// GoogleMaps.c
#ifdef ANDROID
jmethodID jshowGoogleMaps, jshowRoute;
#endif

// startup.c
bool traceOn;
char commandLine[256];
int32 exitCode;
bool rebootOnExit;
bool destroyingApplication;
Object mainClass;  // the instance being executed
bool isMainWindow;   // extends MainWindow ?
#if defined(ANDROID)
JavaVM* androidJVM;
jobject applicationObj, applicationContext;
jclass applicationClass,jRadioDevice4A,jBluetooth4A,jConnectionManager4A;
jfieldID jshowingAlert,jhardwareKeyboardIsVisible;
jfieldID jsipVisible,jappTitleH;
jmethodID jgetHeight;
#elif defined WIN32 || defined linux
TCHAR exeName[MAX_PATHNAME];
#endif

// graphicsprimitives.c
uint8 *lookupR, *lookupG, *lookupB, *lookupGray; // on 8 bpp screens
int32* controlEnableUpdateScreenPtr;
TScreenSurface screen;
TCClass uiColorsClass;
int32* shiftScreenColorP;
int32* vistaFadeStepP;

// mem.c
#ifdef INITIAL_MEM
uint32 maxAvail = INITIAL_MEM; // in bytes
#endif
bool warnOnExit;
bool leakCheckingEnabled;
bool showMemoryMessagesAtExit;
VoidPs* createdHeaps;
int32 totalAllocated, maxAllocated, allocCount, freeCount;
DECLARE_MUTEX(createdHeaps);

// PalmFont_c.h
int32 maxFontSize, minFontSize, normalFontSize;
FontFile defaultFont;
int32 *tabSizeField;
Hashtable htUF;
VoidPs* openFonts;
Heap fontsHeap;

// win/gfx_Graphics_c.h
#ifdef WIN32
HWND mainHWnd;
bool bSipUp = false; //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
#endif

// Settings.c
TCClass settingsClass;
TTCSettings tcSettings;
#if defined (WINCE)
TVirtualKeyboardSettings vkSettings;
#endif

// demo.c
#ifdef ANDROID
jmethodID jsetElapsed;
#endif

// objectmemorymanager.c
bool runningGC,runningFinalizer;
ObjectArray freeList; // the array with lists of free objects
ObjectArray usedList; // the array with lists of used objects (allocated after the last GC)
ObjectArray lockList; // locked objects list
uint32 markedAsUsed; // starts as 1
uint32 objCreated,skippedGC,objLocked; // a few counters
int32 lastGC;
Heap ommHeap;
Heap chunksHeap;
Stack objStack;
#if defined(ENABLE_TEST_SUITE)
// The garbage collector tests requires that no objects are created, so we cache the state, then restore it when the test finishes
bool canTraverse=true;
ObjectArray freeList2; // the array with lists of free objects
ObjectArray usedList2; // the array with lists of used objects (allocated after the last GC)
ObjectArray lockList2; // locked objects list
uint32 markedAsUsed2; // starts as 1
uint32 gcCount2,objCreated2,skippedGC2,objLocked2; // the current gc count
Heap ommHeap2,chunksHeap2;
Stack objStack2;
#endif

// context.c
Context mainContext,gcContext,lifeContext;
Context contexts[MAX_CONTEXTS];

// tcvm.c
int32 vmTweaks;
bool showKeyCodes;
int32 profilerMaxMem; // guich@tc111_4 - also on mem.c
TCClass lockClass;

// file.c
#ifdef ANDROID
jmethodID jgetSDCardPath;
#endif

// linux/graphicsprimitives.c, linux/event_c.h, darwin/event.m, tcview.m
#if !defined(WIN32)
void *deviceCtx; // The device context points a structure containing platform specific data that have to handled in platform specific code only, that's why we don't define a structure here insofar some platform specific data can't be defined in plain C (such as SymbianOS C++ classes, iPhone objC data structures, ...) Currently this pointer is mirrored in ScreenSurface in the extension field but this may change sooner or later.
#endif
        
// vm.c
#ifdef ANDROID
jmethodID jvmFuncI,jvmExec;
#endif

// utils.c
int32 firstTS;
#ifdef ANDROID
jmethodID jlistTCZs;
#endif

// debug.c
CharP debugstr;
bool consoleAllocated;
#ifdef ANDROID
jmethodID jalert;
#endif

// nativelib.c
VoidPs* openNativeLibs;

//native proc addresses for iOS
Hashtable htNativeProcAddresses;

// tcz.c
VoidPs* openTCZs;
#ifdef ANDROID
jmethodID jreadTCZ, jfindTCZ;
#endif

// event.c
bool appExitThrown;
bool keepRunning = true; // don't remove from here!
bool eventsInitialized;
int32 nextTimerTick;
bool isDragging;
Method _onTimerTick, _postEvent;
Int32Array interceptedSpecialKeys;

// Vm_c.h
int32 oldAutoOffValue;
#ifdef ANDROID
jmethodID jclipboard;
#endif

// media_Sound.c
TSoundSettings soundSettings;
#ifdef ANDROID
jmethodID jtone,jsoundEnable;
#endif


// ConnectionManager.c
TCClass connMgrClass;

// win/Socket_c.h
#ifdef WIN32
int32 WSACount;
#endif

// xml/xml_Tokenizer.c
bool xmlInitialized;

// ssl_SSL.c
Hashtable htSSLSocket;
Heap heapSSLSocket;
DECLARE_MUTEX(htSSL);

#ifdef ANDROID
jmethodID jshowCamera,jgetNativeResolutions,jzxing;

// android/GPS_c.h
jmethodID jgpsFunc,jcellinfoUpdate;

// android/Dial_c.h
jmethodID jdial;

#endif

// tcthread.c
int32 threadCount;

// class.c
Object *voidTYPE, *booleanTYPE, *byteTYPE, *shortTYPE, *intTYPE, *longTYPE, *floatTYPE, *doubleTYPE, *charTYPE;


// These are set in the application's constructor
uint32 applicationId;
char applicationIdStr[5];

// These are set when the VM is initialized, and their values are copied into totalcross.sys.Settings.
CharP platform; // always a constant
char userName[42];
char appPath[MAX_PATHNAME];
char vmPath[MAX_PATHNAME];
char dataPath[MAX_PATHNAME];
char mainClassName[MAX_PATHNAME];
bool isMotoQ;
bool isWindowsMobile;

#ifdef WINCE
HINSTANCE aygshellDll, coreDll, cellcoreDll;
#endif

DECLARE_MUTEX(omm);
DECLARE_MUTEX(screen);
DECLARE_MUTEX(opengl);

///////////////////////////////////////////////////////////////////////////////////////////////////

#if defined (WIN32)
 bool initWinsock();
 void closeWinsock();
#endif

bool initGlobals()
{
	SETUP_MUTEX;
   INIT_MUTEX(opengl);
   INIT_MUTEX(omm);
   INIT_MUTEX(screen);
   INIT_MUTEX(htSSL); 
   INIT_MUTEX(createdHeaps);
#if defined (WIN32) || defined (WINCE)
   initWinsock();
#endif
#ifdef WINCE
   aygshellDll = LoadLibrary(TEXT("aygshell.dll"));
   coreDll = LoadLibrary(TEXT("coredll.dll"));
   cellcoreDll = LoadLibrary(TEXT("cellcore.dll"));
#endif
   return true;
}

void destroyGlobals()
{
   DESTROY_MUTEX(omm);
   DESTROY_MUTEX(screen);
   DESTROY_MUTEX(htSSL);
   DESTROY_MUTEX(createdHeaps);
#if defined (WIN32) || defined (WINCE)
   closeWinsock();
#endif
#ifdef WINCE
   if (aygshellDll != null) FreeLibrary(aygshellDll);
   if (coreDll != null) FreeLibrary(coreDll);
   if (cellcoreDll != null) FreeLibrary(cellcoreDll);
#endif
}

TC_API UInt32 getApplicationId()     {return applicationId;    }
TC_API CharP  getApplicationIdStr()  {return applicationIdStr; }
TC_API CharP  getVMPath()            {return vmPath;           }
TC_API CharP  getAppPath()           {return appPath;          }
TC_API CharP  getUserName()          {return userName;         }
TC_API Object getMainClass()         {return mainClass;        }

#if defined (WIN32)
TC_API HWND getMainWindowHandle()    {return mainHWnd;         }
#endif
