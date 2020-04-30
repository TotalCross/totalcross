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


static uint32 getSecretKeyCreator(uint32 crtr);
// made these static to prevent them from being changed by a malicious library. the activation depends on these.
static char romSerialNumber[128];
static char imei[64],imei2[64];
static char iccid[30];
static char deviceId[128];

#if defined (WINCE) || defined (WIN32)
 #include "win/settings_c.h"
#else
 #include "posix/settings_c.h"
#endif

TC_API void getRomSerialNumber(CharP outBuf)
{
   xstrcpy(outBuf, romSerialNumber);
}

TC_API void getDeviceId(CharP outBuf)
{
   xstrcpy(outBuf, deviceId);
}

TC_API void getImei(CharP outBuf)
{
   xstrcpy(outBuf, imei);
}


static void createSettingsAliases(Context currentContext, TCZFile loadedTCZ)
{
   tcSettings.dateFormatPtr               = getStaticFieldInt(settingsClass, "dateFormat");
   tcSettings.dateSeparatorPtr            = getStaticFieldInt(settingsClass, "dateSeparator");
   tcSettings.weekStartPtr                = getStaticFieldInt(settingsClass, "weekStart");
   tcSettings.is24HourPtr                 = getStaticFieldInt(settingsClass, "is24Hour");
   tcSettings.timeSeparatorPtr            = getStaticFieldInt(settingsClass, "timeSeparator");
   tcSettings.thousandsSeparatorPtr       = getStaticFieldInt(settingsClass, "thousandsSeparator");
   tcSettings.decimalSeparatorPtr         = getStaticFieldInt(settingsClass, "decimalSeparator");
   tcSettings.screenWidthPtr              = getStaticFieldInt(settingsClass, "screenWidth");
   tcSettings.screenHeightPtr             = getStaticFieldInt(settingsClass, "screenHeight");
   tcSettings.screenWidthInDPIPtr         = getStaticFieldInt(settingsClass, "screenWidthInDPI");
   tcSettings.screenHeightInDPIPtr        = getStaticFieldInt(settingsClass, "screenHeightInDPI");
   tcSettings.screenBPPPtr                = getStaticFieldInt(settingsClass, "screenBPP");
   tcSettings.screenDensityPtr            = getStaticFieldDouble(settingsClass, "screenDensity");
   tcSettings.romVersionPtr               = getStaticFieldInt(settingsClass, "romVersion");
   tcSettings.virtualKeyboardPtr          = getStaticFieldInt(settingsClass, "virtualKeyboard");
   tcSettings.daylightSavingsPtr          = getStaticFieldInt(settingsClass, "daylightSavings");
   tcSettings.daylightSavingsMinutesPtr   = getStaticFieldInt(settingsClass, "daylightSavingsMinutes");
   tcSettings.timeZonePtr                 = getStaticFieldInt(settingsClass, "timeZone");
   tcSettings.timeZoneMinutesPtr          = getStaticFieldInt(settingsClass, "timeZoneMinutes");
   tcSettings.showSecretsPtr              = getStaticFieldInt(settingsClass, "showSecrets");
   tcSettings.keyboardFocusTraversablePtr = getStaticFieldInt(settingsClass, "keyboardFocusTraversable");
   tcSettings.closeButtonTypePtr          = getStaticFieldInt(settingsClass, "closeButtonType");
   tcSettings.isFullScreenPtr             = getStaticFieldInt(settingsClass, "isFullScreen");
   tcSettings.uiStylePtr                  = getStaticFieldInt(settingsClass, "uiStyle");
   tcSettings.dontCloseApplicationPtr     = getStaticFieldInt(settingsClass, "dontCloseApplication");
   tcSettings.multipleInstances           = getStaticFieldInt(settingsClass, "multipleInstances");
   tcSettings.gcCount                     = getStaticFieldInt(settingsClass, "gcCount");
   tcSettings.gcTime                      = getStaticFieldInt(settingsClass, "gcTime");
   tcSettings.chunksCreated               = getStaticFieldInt(settingsClass, "chunksCreated");    *tcSettings.chunksCreated = 3;
   tcSettings.appSettingsPtr              = getStaticFieldObject(currentContext,settingsClass, "appSettings");
   tcSettings.appSecretKeyPtr             = getStaticFieldObject(currentContext,settingsClass, "appSecretKey");
   tcSettings.appSettingsBinPtr           = getStaticFieldObject(currentContext,settingsClass, "appSettingsBin");
   tcSettings.showMemoryMessagesAtExit    = getStaticFieldInt(settingsClass, "showMemoryMessagesAtExit");
   tcSettings.timeZoneStrPtr              = getStaticFieldObject(currentContext,settingsClass, "timeZoneStr");
   tcSettings.fingerTouchPtr              = getStaticFieldInt(settingsClass, "fingerTouch");
   tcSettings.disableDebug                = getStaticFieldInt(loadClass(currentContext, "totalcross.sys.Vm", true), "disableDebug");
   tcSettings.fullScreenPlatformsPtr      = getStaticFieldObject(currentContext,settingsClass, "fullScreenPlatforms");
   tcSettings.disableScreenRotation       = getStaticFieldInt(settingsClass, "disableScreenRotation");
   tcSettings.deviceFontHeightPtr         = getStaticFieldInt(settingsClass, "deviceFontHeight");
   tcSettings.iccidPtr                    = getStaticFieldObject(currentContext,settingsClass, "iccid");
   tcSettings.resizableWindow             = getStaticFieldInt(settingsClass, "resizableWindow");
   tcSettings.windowFont                  = getStaticFieldInt(settingsClass, "windowFont");
   tcSettings.isOpenGL                    = getStaticFieldInt(settingsClass, "isOpenGL");
   tcSettings.lineNumber                  = getStaticFieldObject(currentContext,settingsClass, "lineNumber");
   tcSettings.unmovableSIP                = getStaticFieldInt(settingsClass, "unmovableSIP");
   tcSettings.bugreportEmail              = getStaticFieldObject(currentContext,settingsClass, "bugreportEmail");
   tcSettings.appVersion                  = getStaticFieldObject(currentContext,settingsClass, "appVersion");
   tcSettings.minimalUpdateInterval       = getStaticFieldInt(settingsClass, "minimalUpdateInterval");
   if (loadedTCZ != null)
   {
      *tcSettings.windowFont = (loadedTCZ->header->attr & ATTR_WINDOWFONT_DEFAULT) != 0;
   }
}

static uint32 getSecretKeyCreator(uint32 crtr)
{
   uint32 c0 = ((crtr >> 24) & 0xFF)+64;
   uint32 c1 = ((crtr >> 16) & 0xFF)+64;
   uint32 c2 = ((crtr >> 8 ) & 0xFF)+64;
   uint32 c3 = ((crtr      ) & 0xFF)+64;
   return (c0 << 24) | (c1 << 16) | (c2 << 8) | c3;
}

static void getDefaultCrid(CharP name, CharP creat)
{
   CharP dot;
   int32 i, hash=0;
   dot = xstrrchr(name, '.');
   if (dot)
      name = dot+1;
   for (i = 0; name[i]; i++)
      hash += name[i];
   for (i = 0; i < 4; i++)
   {
      creat[i] = (char)((hash % 26) + 'a');
      if ((hash & 64)>0)
         creat[i] += ('A'-'a');
      hash /= 2;
   }
}

bool initSettings(Context currentContext, CharP mainClassNameP, TCZFile loadedTCZ)
{
   xstrcpy(mainClassName, mainClassNameP);
   settingsClass = loadClass(currentContext, "totalcross.sys.Settings", true);
   if (settingsClass == null)
      return false;
   createSettingsAliases(currentContext, loadedTCZ);
#if defined (WINCE)
   isWindowsMobile = checkWindowsMobile();
   *tcSettings.virtualKeyboardPtr = hasVirtualKeyboard();
   saveVKSettings();
#endif
   uiColorsClass = loadClass(currentContext, "totalcross.ui.UIColors", true);
   shiftScreenColorP = getStaticFieldInt(uiColorsClass, "shiftScreenColor");
   vistaFadeStepP = getStaticFieldInt(uiColorsClass, "vistaFadeStep");
   return true;
}

static bool inSerialNumberExclusionList() // empties the serial number in devices that returns it incorrectly
{                     
   int32 i;
   char deviceId[100];
   char* exclusions[] = {"Motorola A3100", null};
   getDeviceId(deviceId);
   for (i =0; exclusions[i] != null; i++)
      if (strEq(deviceId, exclusions[i]))
         return true;
   return false;
}

bool retrieveSettings(Context currentContext, CharP mainClassName)
{      
   dataPath[0] = 0;
   if (!fillSettings(currentContext)) // platform dependent function
      return false;
   if (inSerialNumberExclusionList())
      romSerialNumber[0] = 0;
   saveSoundSettings();

   // sets the default creator id - it may be changed in the application's static initializer
   getDefaultCrid(mainClassName, applicationIdStr);
   applicationId = *((int32*)applicationIdStr);

   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "applicationId")   = createStringObjectFromCharP(currentContext, applicationIdStr, -1), UNLOCKED);
#if !defined darwin
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "platform")        = createStringObjectFromCharP(currentContext, platform        , -1), UNLOCKED);
#endif
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "userName")        = createStringObjectFromCharP(currentContext, userName        , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "romSerialNumber") = createStringObjectFromCharP(currentContext, romSerialNumber , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "deviceId")        = createStringObjectFromCharP(currentContext, deviceId        , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "imei")            = createStringObjectFromCharP(currentContext, imei            , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "appPath")         = createStringObjectFromCharP(currentContext, appPath         , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "dataPath")        = createStringObjectFromCharP(currentContext, dataPath        , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "vmPath")          = createStringObjectFromCharP(currentContext, vmPath          , -1), UNLOCKED);
   setObjectLock(*getStaticFieldObject(currentContext,settingsClass, "iccid")           = createStringObjectFromCharP(currentContext, iccid           , -1), UNLOCKED);
      
   if (imei[0] || imei2[0])
   {
      TCObject *imeisArrayObj = getStaticFieldObject(currentContext,settingsClass, "imeis");
      *imeisArrayObj = createStringArray(currentContext, imei[0] && imei2[0] ? 2 : 1);
      if (*imeisArrayObj)
      {
         TCObject *imeis = (TCObjectArray) ARRAYOBJ_START(*imeisArrayObj);
         if (imei [0]) setObjectLock(*imeis++ = createStringObjectFromCharP(currentContext, imei, -1), UNLOCKED);
         if (imei2[0]) setObjectLock(*imeis++ = createStringObjectFromCharP(currentContext, imei2,-1), UNLOCKED);
         setObjectLock(*imeisArrayObj, UNLOCKED);
      }
   }   

   return true;
}

void retrieveSettingsChangedAtStaticInitializer(Context currentContext)
{
   TCObject appId = *getStaticFieldObject(currentContext,settingsClass, "applicationId");
   JCharP c = String_charsStart(appId);

   applicationIdStr[0] = (char)c[0];
   applicationIdStr[1] = (char)c[1];
   applicationIdStr[2] = (char)c[2];
   applicationIdStr[3] = (char)c[3];
   applicationId = *((int32*)applicationIdStr);
   applicationId = SWAP32_FORCED(applicationId);
   *tcSettings.appSettingsPtr = getAppSettings(currentContext, applicationId, false,false);
   *tcSettings.appSecretKeyPtr = getAppSettings(currentContext, getSecretKeyCreator(applicationId), false,true);
   *tcSettings.appSettingsBinPtr = getAppSettings(currentContext, applicationId, true,false);
   setObjectLock(*tcSettings.appSettingsBinPtr, UNLOCKED);
   setObjectLock(*tcSettings.appSecretKeyPtr, UNLOCKED);
   setObjectLock(*tcSettings.appSettingsPtr, UNLOCKED);
}

static void updateEntry(char *name, uint32 crtr, bool bin, bool isHKLM)
{
   TCObject obj = *getStaticFieldObject(mainContext,settingsClass, name);
   if (obj == NULL || ARRAYOBJ_LEN(obj) == 0) // if string null, delete it - guich@240_3: first condition added.
      deleteAppSettings(crtr,bin,isHKLM);      
   else                                      
      setAppSettings(crtr, obj, bin, isHKLM);
}

void storeSettings(bool quittingApp) // guich@230_22
{                 
   if (!settingsClass) return;
   updateEntry("appSettings",applicationId,false,false);
   updateEntry("appSecretKey",getSecretKeyCreator(applicationId),false,true); // guich@330_47
   updateEntry("appSettingsBin",applicationId,true,false);
   
   if (quittingApp)
   {
   restoreSoundSettings();
#if defined(WINCE)
   restoreVKSettings();
#endif
   }
}
TC_API void tsS_refresh(NMParams p) // totalcross/sys/Settings native public static void refresh();
{
   UNUSED(p);
   updateDaylightSavings(p->currentContext);
   storeSettings(false); // guich@tc136
}

void updateScreenSettings(int32 width, int32 height, int32 hRes, int32 vRes, int32 bpp) // will be called from initGraphicsAfterSettings
{
   *tcSettings.screenWidthPtr = width;
   *tcSettings.screenHeightPtr = height;
   *tcSettings.screenWidthInDPIPtr = hRes;
   *tcSettings.screenHeightInDPIPtr = vRes;
   *tcSettings.screenBPPPtr = bpp;
#if defined(ANDROID) || defined(darwin) || defined(linux)
   *tcSettings.isOpenGL = true;
#endif
#if defined(ANDROID) || defined(darwin)
   *tcSettings.deviceFontHeightPtr = deviceFontHeight;
#endif
#if defined (darwin)
   *tcSettings.screenDensityPtr = iosScale;
#elif defined (WIN32) || defined (WINCE)
   *tcSettings.screenDensityPtr = min32(width, height) <= 240 ? 0.75 : 1;
#endif
}

TC_API bool getDataPath(CharP storeInto)
{
   TCObject dataPathObj = *getStaticFieldObject(null,settingsClass, "dataPath");
   if (dataPathObj == null)
      return false;
   String2CharPBuf(dataPathObj, storeInto);
   return true;
}

TC_API TCSettings getSettingsPtr()
{
   return &tcSettings;
}
