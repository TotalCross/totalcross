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



typedef void *HsExtAPIType;
HsExtAPIType gHsExtLink;

#ifndef hsExtCreator
 #define hsExtCreator   'HsEx'
#endif
#ifndef hsExtType
 #define hsExtType      'aexo'
#endif
#ifndef hsFtrCreator
 #define hsFtrCreator   'hsEx'
#endif
#ifndef hsFtrIDVersion
 #define hsFtrIDVersion 0
#endif
#ifndef vfsIncludePrivateVolumes
 #define vfsIncludePrivateVolumes   0x80000000
#endif

Err HsGetPhoneLibrary(UInt16* refNum);
Err HsGetVersionString (UInt16 /*HsVerStrEnum*/selector, Char* outStrP, UInt16* sizeP); //SYS_TRAP(entryNumHsGetVersionString/*hsSelGetVersionString*/);
//Boolean PhnLibCardInfo (UInt16 refNum, CharPtr * manufacturer, CharPtr * model, CharPtr * version, CharPtr * serial);

bool isTreo()
{
   UInt32 romVersion;
   UInt32 handspringExt;

   FtrGet(sysFtrCreator, sysFtrNumROMVersion, &romVersion);
   return romVersion > sysMakeROMVersion(5,0,0,sysROMStageRelease,0) && FtrGet(hsFtrCreator, hsFtrIDVersion, &handspringExt) == errNone;
}

bool loadTreoLib()
{
   if (gHsExtLink == null)
      LinkModule(hsExtType, hsExtCreator, &gHsExtLink, null);
   return gHsExtLink != 0;
}

static bool checkNVFS(int32 *vol)
{
   bool ret = false;
   VolumeInfoType volInfo;
   UInt32 vfsMgrVersion;
   UInt32 volIterator;
   UInt16 volNo;
   Err err;

   if (FtrGet(sysFileCVFSMgr, vfsFtrIDVersion, &vfsMgrVersion) == errNone)
   {
      volIterator = vfsIteratorStart | vfsIncludePrivateVolumes;
      while (volIterator != vfsIteratorStop)
      {
         if ((err = VFSVolumeEnumerate(&volNo, &volIterator)) == errNone)
         {
            if ((err = VFSVolumeInfo(volNo, &volInfo)) != errNone)
               break;
            if (volInfo.attributes & vfsVolumeAttrHidden)
            {
               ret = true;
               break;
            }
         }
         else
            break;
      }
   }

   *vol = (int32) volNo;
   return ret;
}

void updateDaylightSavings(Context currentContext)
{
   *tcSettings.timeZoneMinutesPtr = (int32) PrefGetPreference(prefTimeZone);
   *tcSettings.timeZonePtr = *tcSettings.timeZoneMinutesPtr / 60;
   *tcSettings.daylightSavingsPtr = PrefGetPreference(prefDaylightSavingAdjustment) != 0;
   *tcSettings.daylightSavingsMinutesPtr = PrefGetPreference(prefDaylightSavingAdjustment);
}

bool fillSettings(Context currentContext)
{
   DateFormatType dateFormat;
   TimeFormatType timeFormat;
   UInt16 romSerialNumberSize;
   UInt32 romVersion;
   CharP romSerialNumberP;
   CharP bufP;
   UInt32 device, company;
   int32 nvfsVolume;
   UNUSED(currentContext)

//   UInt16 pLib = 0;

   /*Platform*/
   platform = "PalmOS";

   /*Rom version*/
   FtrGet(sysFtrCreator, sysFtrNumROMVersion, &romVersion);
   *tcSettings.romVersionPtr = romVersion;

   /*Rom serial number*/
   *romSerialNumber = 0;

   if(isTreo())
   {
      romSerialNumberSize = 128;
      loadTreoLib();
      HsGetVersionString(1/*hsVerStrSerialNo*/, romSerialNumber, &romSerialNumberSize);
   }
   else
   if (SysGetROMToken(sysROMTokenSnum, (void**) &romSerialNumberP, (void*) &romSerialNumberSize) == errNone && romSerialNumberP != null && (char)*romSerialNumberP != 0xFF)
   {
      romSerialNumberP[romSerialNumberSize] = 0;
      xstrcpy(romSerialNumber, romSerialNumberP);
   }

   /*User name*/
   DlkGetSyncInfo(null, null, null, userName, null, null);

   /*Date format and separator*/
   dateFormat = PrefGetPreference(prefDateFormat);
   switch (dateFormat)
   {
      case dfMDYWithSlashes:
      case dfDMYWithSlashes:
      case dfYMDWithSlashes: *tcSettings.dateSeparatorPtr = '/'; break;
      case dfYMDWithDots:
      case dfDMYWithDots: *tcSettings.dateSeparatorPtr = '.'; break;
      case dfDMYWithDashes:
      case dfYMDWithDashes:
      case dfMDYWithDashes: *tcSettings.dateSeparatorPtr = '-'; break;
      default: *tcSettings.dateSeparatorPtr = 'X'; break;
   }
   switch (dateFormat)
   {
      case dfMDYWithSlashes:
      case dfMDYWithDashes: *tcSettings.dateFormatPtr = 1; break;
      case dfDMYWithSlashes:
      case dfDMYWithDots:
      case dfDMYWithDashes: *tcSettings.dateFormatPtr = 2; break;
      case dfYMDWithSlashes:
      case dfYMDWithDots:
      case dfYMDWithDashes: *tcSettings.dateFormatPtr = 3; break;
      default: *tcSettings.dateFormatPtr = 0; break;
   }

   /*Week day start*/
   *tcSettings.weekStartPtr = (int32) PrefGetPreference(prefWeekStartDay);

   /*Time separator and is24Hour*/
   timeFormat = PrefGetPreference(prefTimeFormat);
   switch (timeFormat)
   {
      case tfColon:
      case tfColonAMPM:
      case tfColon24h: *tcSettings.timeSeparatorPtr = ':'; break;
      case tfDot:
      case tfDotAMPM:
      case tfDot24h: *tcSettings.timeSeparatorPtr = '.'; break;
      case tfComma24h: *tcSettings.timeSeparatorPtr = ','; break;
      case tfHoursAMPM:
      case tfHours24h: *tcSettings.timeSeparatorPtr = ' '; break;
      default: *tcSettings.timeSeparatorPtr = 'X'; break;
   }
   switch (timeFormat)
   {
      case tfColon24h:
      case tfDot24h:
      case tfHours24h:
      case tfComma24h: *tcSettings.is24HourPtr = true; break;
      case tfColon:
      case tfDot:
      case tfDotAMPM:
      case tfHoursAMPM:
      case tfColonAMPM:
      default: *tcSettings.is24HourPtr = false; break;
   }

   /*Thousands and decimal separators*/
   LocGetNumberSeparators(PrefGetPreference(prefNumberFormat), (char*) tcSettings.thousandsSeparatorPtr, (char*) tcSettings.decimalSeparatorPtr);

   /*Daylight Savings*/
   updateDaylightSavings(currentContext);

   /*Show secrets*/
   *tcSettings.showSecretsPtr = PrefGetPreference(prefShowPrivateRecords) == showPrivateRecords;

   /*Company ID and Device ID*/
   bufP = deviceId;
   if (FtrGet(sysFtrCreator, sysFtrNumOEMCompanyID, &company) == errNone && company != 0)
      bufP = int2CRID((int32) company, bufP);
   if (FtrGet(sysFtrCreator, sysFtrNumOEMDeviceID, &device) == errNone && device != 0)
      bufP = int2CRID((int32) device, bufP);

   /*NVFS number*/
   if (checkNVFS(&nvfsVolume))
      *tcSettings.nvfsVolumePtr = nvfsVolume;
   else
      *tcSettings.nvfsVolumePtr = -1;

   return true;
}

#define APPSETTINGS 0
#define APPSETTINGS_BIN 1

#define SETTINGS_VERSION 2

// IMPORTANT: appSettings in TotalCross is stored as UNICODE!

static Object getAppSettings(Context currentContext, uint32 crid, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
   UInt16 appSettingsLen=0;
   Object target=null;

   if (bin)
   {
      if (PrefGetAppPreferences(crid, APPSETTINGS_BIN, 0, &appSettingsLen, true) != noPreferenceFound && appSettingsLen > 0 &&
          (target = createByteArray(currentContext, appSettingsLen)) != null)
         PrefGetAppPreferences(crid, APPSETTINGS_BIN, ARRAYOBJ_START(target), &appSettingsLen, true);
   }
   else
   {
      if (isHKLM)
         crid = getSecretKeyCreator(crid);
      if (PrefGetAppPreferences(crid, APPSETTINGS, 0, &appSettingsLen, true) != noPreferenceFound && appSettingsLen > 0)
      {
         target = createStringObjectWithLen(currentContext, appSettingsLen/2);
         if (target != null)
            PrefGetAppPreferences(crid, APPSETTINGS, String_charsStart(target), &appSettingsLen, true);
      }
   }
   return target;
}

static void deleteAppSettings(uint32 crid, bool bin, bool isHKLM)
{
   if (!bin && isHKLM)
      crid = getSecretKeyCreator(crid);
   PrefSetAppPreferences(crid, bin ? APPSETTINGS_BIN : APPSETTINGS, SETTINGS_VERSION, 0, 0, true);
}

static void setAppSettings(uint32 crid, Object ptr, bool bin, bool isHKLM)
{
   int32 ptrLen;

   if (bin)
   {
      ptrLen = ARRAYOBJ_LEN(ptr);
      PrefSetAppPreferences(crid, APPSETTINGS_BIN, SETTINGS_VERSION, ARRAYOBJ_START(ptr), ptrLen, true);
   }
   else
   {
      if (isHKLM)
         crid = getSecretKeyCreator(crid);
      ptrLen = String_charsLen(ptr) * 2;
      PrefSetAppPreferences(crid, APPSETTINGS, SETTINGS_VERSION, String_charsStart(ptr), ptrLen, true);
   }
}

void saveSoundSettings()
{
   soundSettings.volume = (int32) PrefGetPreference(prefSysSoundVolume);
   soundSettings.isSoundEnabled = soundSettings.volumeState = (soundSettings.volume > 0);
   soundSettings.ringer = -1;
   soundSettings.ringerState = 0;
}

void restoreSoundSettings()
{
   int32 volume = (int32) PrefGetPreference(prefSysSoundVolume);
   if (volume != soundSettings.volume)
      PrefSetPreference(prefSysSoundVolume, (UInt16) soundSettings.volume);
}

void restoreVKSettings()
{
}
