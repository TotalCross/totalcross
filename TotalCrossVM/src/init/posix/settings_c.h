// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <locale.h>
#include <time.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/param.h>
#include <unistd.h>
#include <errno.h>
#ifdef ANDROID
 #include <fcntl.h>
#else
 #include <sys/fcntl.h>
#endif
#include <sys/utsname.h>

#if HAVE_STDLIB_H
#include <stdlib.h>
#endif

#define HIVE_MAXCHARS      ((4 + sizeof(uint32) * 8) / 5)
#define BLK_SZ             128
#define DEFAULT_DIR_PERMS  0775
#define DEFAULT_FILE_PERMS 0664

#if defined(UNICODE)
 // UNICODE Filesystem API
 #define PATH_SEPARATOR_STR L"/"
 #define lstrlen wcslen
 #define lstrcmp wscmp
 #define lstrcpy wscpy
 #define lstrcat wscat
 #define lstrstr wsstr
#else
 #define PATH_SEPARATOR_STR "/"
 #define lstrlen (int)strlen
 #define lstrcmp strcmp
 #define lstrcpy strcpy
 #define lstrcat strcat
 #define lstrstr strstr
#endif

void getSerialNum(char *id, int maxlen);
#if defined (darwin)
   int getRomVersion();
#endif

#if !defined(darwin)
// todo@ All licensed platforms have to implement this function to identify a device
void getSerialNum(char *id, int maxlen)
{
   strcpy(id, "unknown");
}
#endif

typedef TCHAR AppHive[HIVE_MAXCHARS + 3];  // plus the dot, '\0' and the extra char for binary
static const TCHAR *settingsSubdir = TEXT("appSettings" PATH_SEPARATOR_STR);

static void getAppPreferencesPath(TCHAR *buf, AppHive h)
{
   lstrcpy(buf, appPath);
   int finalpos = lstrlen(buf) - 1;
   if (buf[finalpos] != '/')
   {
      if (buf[finalpos] == '\\')
         buf[finalpos] = '/';
      else lstrcat(buf, PATH_SEPARATOR_STR);
   }
   lstrcat(buf, settingsSubdir);
   lstrcat(buf, h);
}

static int ensureAppPreferencesDirectory(TCHAR * buf)
{
   lstrcpy(buf, appPath);
   if (access(buf, W_OK) != 0)
   {
      if (errno != ENOENT) return 0;
      if (mkdir(buf, DEFAULT_DIR_PERMS) != 0) return 0;
   }
   int finalpos = lstrlen(buf) - 1;
   if (buf[finalpos] != '/')
   {
      if (buf[finalpos] == '\\')
         buf[finalpos] = '/';
      else lstrcat(buf, PATH_SEPARATOR_STR);
   }
   lstrcat(buf, settingsSubdir);
   if (access(buf, W_OK) != 0)
   {
      if (errno != ENOENT) return 0;
      if (mkdir(buf, DEFAULT_DIR_PERMS) != 0) return 0;
   }
   return 1;
}

static TCHAR *getAppHive(AppHive h, uint32 crid, bool bin)
{
   TCHAR *cur = h;
   if (bin) *cur++ = (TCHAR)'B';
   *cur++ = (TCHAR) ((crid >> 24) & 0xFF);
   *cur++ = (TCHAR) ((crid >> 16) & 0xFF);
   *cur++ = (TCHAR) ((crid >> 8 ) & 0xFF);
   *cur++ = (TCHAR) ((crid      ) & 0xFF);
   *cur++ = 0;
   return h;
}

static TCHAR *getAppSecretHive(AppHive h, uint32 crid)
{
   TCHAR *buf = h;
   unsigned i;

   *buf++ = '.';
   // This is not base32!  sure :-D
   for (i = 0; i < HIVE_MAXCHARS; i++)
   {
      char c = crid & 0x1F;
      crid >>= 5;
      c += 'A';
      if (c > 'Z') c += ('2' - 'Z' - 1);
      *buf++ = c;
   }
   *buf = '\0';
   return h;
}

static char *readAppPreferences(TCHAR *hive, bool bin, UInt16* outlen)
{
   int f;
   TCHAR path[MAXPATHLEN];
   getAppPreferencesPath(path, hive);
   char *buf = NULL;

   if ((f = open(path, O_RDONLY)) >= 0)
   {
      char temp[BLK_SZ];
      UInt16 total = 0;
      int count;
      while ((count = (int)read(f, temp, sizeof(temp))) > 0)
      {
         uint32 i = total + count;
         char *tmp = (char*)xmalloc(i+(bin?0:1)); // guich@tc124_18
         if (!tmp) break;
         if (buf)
            xmemmove(tmp, buf, total);
         xmemmove(tmp + total, temp, count);
         total += count;
         xfree(buf);
         buf = tmp;
         if (!bin)
            buf[total] = 0;
      }
      if (outlen) *outlen = total;
      close(f);
   }
   return buf;
}

static void writeAppPreferences(AppHive h, uint8 *buf, UInt16 len)
{
   TCHAR path[MAXPATHLEN];
   if (ensureAppPreferencesDirectory(path))
   {
      int f;
      getAppPreferencesPath(path, h);
      if ((f = open(path, O_CREAT|O_WRONLY, 0600)) >= 0)
      {   
         write(f, buf, len);
         close(f);
      }
   }
}

static void deleteAppPreferences(AppHive h)
{
   TCHAR path[MAXPATHLEN];
   getAppPreferencesPath(path, h);
   unlink(path);
}

static void setAppSettings(uint32 crid, TCObject ptr, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
   AppHive hive;
   uint8* data;
   UInt16 len;
   if (!bin)
      ptr = String_chars(ptr);
   len = ARRAYOBJ_LEN(ptr);
   data = (uint8*)ARRAYOBJ_START(ptr);
   TCHAR *k = isHKLM ? getAppSecretHive(hive, applicationId) : getAppHive(hive, applicationId, bin);
   writeAppPreferences(k, data, bin ? len : len*2);
}

static TCObject getAppSettings(Context currentContext, uint32 crid, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
   AppHive hive;
   TCObject target = null;
   TCHAR *k = isHKLM ? getAppSecretHive(hive, applicationId) : getAppHive(hive, applicationId, bin);
   UInt16 len;
   CharP buf = readAppPreferences(k, bin, &len);
   if (buf && len > 0)
   {
      TCObject temp = null;
      target = temp = bin ? createByteArray(currentContext, len) : createCharArray(currentContext, len/2);
      if (temp)
         memcpy(ARRAYOBJ_START(temp), buf, len);

      if (!bin) // if not binary, create a string and set the chars to our created buffer
      {
         target = createObject(currentContext, "java.lang.String");
         if (target)
            String_chars(target) = temp;
      }
   }
   if (buf) xfree(buf);
   return target;
}

static void deleteAppSettings(uint32 crid, bool bin, bool isHKLM) // guich@573_16: added bin option to the three methods below - guich@580_21: use hklm if for secret key
{
   AppHive hive;

   if (bin)
      getAppHive(hive, applicationId, true);
   else
   if (isHKLM)
      getAppSecretHive(hive, applicationId);
   else
      getAppHive(hive, applicationId, false);
   deleteAppPreferences(hive);
}

void saveSoundSettings()
{
}

void restoreSoundSettings()
{
}

void fillIOSSettings(int* daylightSavingsPtr, int* daylightSavingsMinutesPtr, int* timeZonePtr, int* timeZoneMinutesPtr, char* timeZoneStrPtr, int sizeofTimeZoneStr); // darwin/gfx_Graphics_c.h

void updateDaylightSavings(Context currentContext)
{
#ifdef darwin
    char nameBuf[50];
    fillIOSSettings(tcSettings.daylightSavingsPtr, tcSettings.daylightSavingsMinutesPtr, tcSettings.timeZonePtr, tcSettings.timeZoneMinutesPtr, nameBuf,sizeof(nameBuf));
    if (nameBuf[0] != 0)
        setObjectLock(*tcSettings.timeZoneStrPtr = createStringObjectFromCharP(currentContext, nameBuf, -1),UNLOCKED);
#else
   time_t t;
   struct tm tm;
   time(&t);
   *tcSettings.daylightSavingsPtr = localtime_r(&t, &tm)->tm_isdst;

   t = 30326400 + 12*3600;   // 01/01/1970 12:00:00 - UTC
   localtime_r(&t, &tm);     // localtime_r is the reentrant version (multithreading aware)
   *tcSettings.timeZonePtr = tm.tm_hour - 12;
#endif
}

#if defined (ANDROID)
#include "sys/system_properties.h"
bool fillSettings(Context currentContext)
{
   JNIEnv* env = getJNIEnv();
   jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestPhoneStatePermission", "()I");
   jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
   if (result <= 0) {
       return false;
   }
   
   jclass jSettingsClass = androidFindClass(env, "totalcross/android/Settings4A");
   jmethodID fillSettingsMethod = (*env)->GetStaticMethodID(env, jSettingsClass, "fillSettings", "()V");
   (*env)->CallStaticVoidMethod(env, jSettingsClass, fillSettingsMethod);

   jfieldID jfID;
   jstring jStringField;
   char strTemp[128];

   // phone number - needed to move to here or jni on android 5 will abort
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "lineNumber", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, strTemp);
      (*env)->DeleteLocalRef(env, jStringField);
      setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "lineNumber") = createStringObjectFromCharP(currentContext, strTemp, -1), UNLOCKED);
   }

   // date format
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "dateFormat", "B");
   *tcSettings.dateFormatPtr = (int32) (*env)->GetStaticByteField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "dateSeparator", "C");
   *tcSettings.dateSeparatorPtr = (char) (*env)->GetStaticCharField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "weekStart", "B");
   *tcSettings.weekStartPtr = (int32) (*env)->GetStaticByteField(env, jSettingsClass, jfID);

   // time format
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "is24Hour", "Z");
   *tcSettings.is24HourPtr = (bool) (*env)->GetStaticBooleanField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "timeSeparator", "C");
   *tcSettings.timeSeparatorPtr = (char) (*env)->GetStaticCharField(env, jSettingsClass, jfID);

   // number format
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "thousandsSeparator", "C");
   *tcSettings.thousandsSeparatorPtr = (char) (*env)->GetStaticCharField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "decimalSeparator", "C");
   *tcSettings.decimalSeparatorPtr = (char) (*env)->GetStaticCharField(env, jSettingsClass, jfID);

   // graphics
   // filled by graphics

   // platform
   platform = "Android";
   *tcSettings.fingerTouchPtr = true;

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "deviceId", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, deviceId);
      (*env)->DeleteLocalRef(env, jStringField);
   }

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "romVersion", "I");
   *tcSettings.romVersionPtr = (int32) (*env)->GetStaticIntField(env, jSettingsClass, jfID);

   // locale
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "daylightSavings", "Z");
   *tcSettings.daylightSavingsPtr = (bool) (*env)->GetStaticBooleanField(env, jSettingsClass, jfID);
   
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "daylightSavingsMinutes", "I");
   *tcSettings.daylightSavingsMinutesPtr = (int32) (*env)->GetStaticIntField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "timeZone", "I");
   *tcSettings.timeZonePtr = (int32) (*env)->GetStaticIntField(env, jSettingsClass, jfID);
   
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "timeZoneMinutes", "I");
   *tcSettings.timeZoneMinutesPtr = (int32) (*env)->GetStaticIntField(env, jSettingsClass, jfID);
   
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "screenDensity", "D");
   *tcSettings.screenDensityPtr = (double) (*env)->GetStaticDoubleField(env, jSettingsClass, jfID);

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "timeZoneStr", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, strTemp);
      (*env)->DeleteLocalRef(env, jStringField);
      setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "timeZoneStr") = createStringObjectFromCharP(currentContext, strTemp, -1), UNLOCKED);
   }

   // identification
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "userName", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, userName);
      (*env)->DeleteLocalRef(env, jStringField);
   }

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "imei", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, imei);
      (*env)->DeleteLocalRef(env, jStringField);
   }

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "imei2", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, imei2);
      (*env)->DeleteLocalRef(env, jStringField);
   }

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "iccid", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, iccid);
      (*env)->DeleteLocalRef(env, jStringField);
   }

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "esn", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, strTemp);
      (*env)->DeleteLocalRef(env, jStringField);
      setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "esn") = createStringObjectFromCharP(currentContext, strTemp, -1), UNLOCKED);
   }
   
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "ANDROID_ID", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, strTemp);
      (*env)->DeleteLocalRef(env, jStringField);
      setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "ANDROID_ID") = createStringObjectFromCharP(currentContext, strTemp, -1), UNLOCKED);
   }

   // device capabilities
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "virtualKeyboard", "Z");
   *tcSettings.virtualKeyboardPtr = (bool) (*env)->GetStaticBooleanField(env, jSettingsClass, jfID);

   // rom serial number
   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "serialNumber", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
      jstring2CharP(jStringField, romSerialNumber);
//    (*env)->DeleteLocalRef(env, jSettingsClass); - this is NOT a local ref. breaks on android 4.2.2

   jfID = (*env)->GetStaticFieldID(env, jSettingsClass, "macAddress", "Ljava/lang/String;");
   jStringField = (jstring) (*env)->GetStaticObjectField(env, jSettingsClass, jfID);
   if (jStringField != null)
   {
      jstring2CharP(jStringField, strTemp);
      (*env)->DeleteLocalRef(env, jStringField);
      if (strTemp[0])
         setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "macAddress") = createStringObjectFromCharP(currentContext, strTemp, -1), UNLOCKED);
   }

   return true;
}

#else

bool fillSettings(Context currentContext)
{
   char ts;                 // decimal separator
   char ds;                 // thousands separator
   int ws = 0;              // week start
   char dateSep = '/';      // datefmt separator
   char timeSep = ':';      // time separator
   int time24h;             // time 0-24h
   int datefmt;             // date format
#ifndef darwin
   int gmtBias;             // gmt+0
   int daylightSavings;     // 1 when DST is on
#endif

#if defined (darwin)
   *tcSettings.romVersionPtr = getRomVersion(); //flsobral@tc126_3: implemented Settings.romVersion for iPhone/iPad.
#else
   *tcSettings.romVersionPtr = 0;
#endif

   getSerialNum(romSerialNumber, sizeof(romSerialNumber));

   struct utsname u;
   uname(&u);

// deviceid
#if defined darwin //fdie@tc115_72: fixed userName & deviceId on iPhone & Linux
   xstrncpy(deviceId, u.machine, sizeof(deviceId));
#else
   snprintf(deviceId, sizeof(deviceId), "%s/%s", u.sysname, u.machine);
#endif

// username
#if !defined(darwin) && defined(HAVE_GETENV)
   char *user = getenv("USER");
   xstrcpy(userName, user ? user : "noname");
#else
   xstrncpy(userName, u.nodename, sizeof(userName));
#endif

   *tcSettings.keyboardFocusTraversablePtr = 0;

// platform, touch screen and virtual keyboard settings
#if defined darwin
   platform = strCaseEqn(deviceId, "ipad", 4) ? "IPAD" : "IPHONE";
   *getStaticFieldObject(currentContext, settingsClass, "platform") = *getStaticFieldObject(currentContext, settingsClass, platform); //flsobral@tc126_38: fixed implementation of Settings.platform for iPhone and iPad.
   *tcSettings.virtualKeyboardPtr = *tcSettings.fingerTouchPtr = 1;
#elif defined linux
   platform = "Linux";
   *tcSettings.virtualKeyboardPtr = 0;
#else
#error "not supported platform"
#endif

   time_t t;
   struct tm tm;
   struct lconv *locale;

   // obtain number formats
   locale = localeconv();
   ts = locale->thousands_sep[0];
   ds = locale->decimal_point[0];
   if (ds == ts) // guich@421_12: make sure they differ
      ds = (ts == '.') ? ',' : '.';

   // obtain the locale time
   updateDaylightSavings(currentContext);

   char tmpbuf[10];
   t = 30326400 + 12*3600;   // 01/01/1970 12:00:00 - UTC
   localtime_r(&t, &tm);
   strftime(tmpbuf, sizeof tmpbuf, "%x", &tm);
   if (strlen(tmpbuf) == 8)
   {
      dateSep = tmpbuf[2];
      if (tmpbuf[1] == '0')
         datefmt = 3;                // 70/12/17
      else
      if (tmpbuf[1] == '7')
         datefmt = 2;                // 17/12/70
      else
         datefmt = 1;                // 12/17/70
   }
   else
      datefmt = 0; // ?

   tm.tm_hour = 22;
   strftime(tmpbuf, sizeof tmpbuf, "%X", &tm);
   if (strlen(tmpbuf) == 8)
      timeSep = tmpbuf[2];
   
   time24h = (tmpbuf[0] > '1');
   
   *tcSettings.decimalSeparatorPtr      = ds;
   *tcSettings.thousandsSeparatorPtr    = ts;
   *tcSettings.dateSeparatorPtr         = dateSep;
   *tcSettings.dateFormatPtr            = datefmt;
   *tcSettings.is24HourPtr              = time24h;
   *tcSettings.timeSeparatorPtr         = timeSep;
   *tcSettings.weekStartPtr             = ws;

   return true;
}
#endif
