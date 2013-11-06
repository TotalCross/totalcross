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



#ifndef __SETTINGS_H
#define __SETTINGS_H

/// All settings stored in the totalcross.sys.Settings class

/// Enable a sound when the garbage collector runs
#define VMTWEAK_AUDIBLE_GC        1
/// Collects memory usage statistics during all program run
#define VMTWEAK_DUMP_MEMORY_STATS 2
/// Collects memory usage statistics during a piece of code
#define VMTWEAK_MEM_PROFILER      3
/// Disables gc
#define VMTWEAK_DISABLE_GC        4

#define IS_VMTWEAK_ON(x) (vmTweaks & (1 << (x-1))) // guich@tc114_19: better use this macro

typedef enum
{
   NO_BUTTON,
   CLOSE_BUTTON,
   MINIMIZE_BUTTON
} CloseButtonType; // guich@tc111_3

typedef struct
{
   // These values are pointers to the fields in totalcross.sys.Settings
   int32* dateFormatPtr;                 // byte      -    Java type
   int32* dateSeparatorPtr;              // char
   int32* weekStartPtr;                  // int
   int32* is24HourPtr;                   // boolean
   int32* timeSeparatorPtr;              // char
   int32* thousandsSeparatorPtr;         // char
   int32* decimalSeparatorPtr;           // char
   int32* screenWidthPtr;                // int
   int32* screenHeightPtr;               // int
   int32* screenWidthInDPIPtr;           // int
   int32* screenHeightInDPIPtr;          // int
   int32* screenBPPPtr;                  // int
   int32* romVersionPtr;                 // int
   int32* virtualKeyboardPtr;            // boolean
   int32* daylightSavingsPtr;            // boolean
   int32* timeZonePtr;                   // int
   int32* timeZoneMinutesPtr;            // int
   int32* daylightSavingsMinutesPtr;     // int
   int32* showSecretsPtr;                // boolean
   int32* nvfsVolumePtr;                 // int
   int32* keypadOnlyPtr;                 // boolean
   int32* keyboardFocusTraversablePtr;   // boolean
   int32* closeButtonTypePtr;            // int
   int32* isFullScreenPtr;               // boolean
   int32* uiStylePtr;                    // byte
   int32* dontCloseApplicationPtr;       // boolean
   Object* appSettingsPtr;               // java.lang.String
   int32* multipleInstances;             // boolean
   int32* gcCount;                       // int
   int32* gcTime;                        // int
   int32* chunksCreated;                 // int
   Object* appSecretKeyPtr;              // java.lang.String
   Object* appSettingsBinPtr;            // byte[]
   int32* showMemoryMessagesAtExit;      // boolean
   Object* timeZoneStrPtr;               // java.lang.String
   int32* fingerTouchPtr;                // boolean
   int32* disableDebug;                  // boolean (totalcross.sys.Vm)
   Object* fullScreenPlatformsPtr;       // java.lang.String
   int32* disableScreenRotation;         // boolean
   int32* deviceFontHeightPtr;           // int
   Object* iccidPtr;                     // java.lang.String
   int32* useNewFont;                    // boolean
   int32* resizableWindow;               // boolean
   int32* windowFont;                    // int
   Object* lineNumber;                  // java.lang.String
} TTCSettings, *TCSettings;

typedef struct
{
   int32 volume;
   int32 ringer; // < 0 - not available
   int32 volumeState; // 0 - disabled, < 0 - vibrate, > 0 - enabled
   int32 ringerState; // 0 - disabled, < 0 - vibrate, > 0 - enabled
   bool isSoundEnabled;
} TSoundSettings, *SoundSettings;

typedef struct
{
   bool changed;
   int32 left;
   int32 top;
   int32 right;
   int32 bottom;
   int32 topGap;	//flsobral@tc123: gap between the top of the screen and the sip.
} TVirtualKeyboardSettings, *VirtualKeyboardSettings;

bool hasVirtualKeyboard();
bool initSettings(Context currentContext, CharP mainClassName, TCZFile loadedTCZ);
bool retrieveSettings(Context currentContext, CharP mainClassName);
void retrieveSettingsChangedAtStaticInitializer(Context currentContext);
void storeSettings(bool quittingApp);
void restoreVKSettings();

TC_API TCSettings getSettingsPtr(); // to be called by another dll to get access to the tcSettings structure
typedef TCSettings (*getSettingsPtrFunc)(); // to be called by another dll to get access to the tcSettings structure

TC_API bool getDataPath(CharP storeInto); // stores the current Settings.dataPath value into the given buffer, or returns false if it is null
typedef bool (*getDataPathFunc)(CharP storeInto); // stores the current Settings.dataPath value into the given buffer, or returns false if it is null
void updateScreenSettings(int32 width, int32 height, int32 hRes, int32 vRes, int32 bpp); // updates Settings.screenWidth/screenHeight

TC_API void getRomSerialNumber(CharP outBuf); // stores the rom serial number in the given buffer of size 128.
typedef void (*getRomSerialNumberFunc)(CharP outBuf);
TC_API void getImei(CharP outBuf);            // stores the imei in the given buffer of size 128.
typedef void (*getImeiFunc)(CharP outBuf);
TC_API void getDeviceId(CharP outBuf);        // stores the device id in the given buffer of size 128.
typedef void (*getDeviceIdFunc)(CharP outBuf);

#endif
