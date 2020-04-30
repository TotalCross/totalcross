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

/** Maximum number of GPS satellites used by the GPS Intermediate Driver. */
#define GPS_MAX_SATELLITES 12
/** Maximum size, in characters, of prefixes used for CreateFile device names. For example, this is the maximum size of the szGPSDriverPrefix and szGPSMultiplexPrefix fields in the GPS_POSITION structure. */
#define GPS_MAX_PREFIX_NAME 16
/** Maximum size, in characters, of data like the friendly name of an input source. For example, this is the maximum size of the szGPSFriendlyName field in the GPS_POSITION structure. */
#define GPS_MAX_FRIENDLY_NAME 64

#define GPS_VERSION_1           1

//
// GPS_VALID_XXX bit flags in GPS_POSITION structure are valid.
//
#define GPS_VALID_UTC_TIME                                 0x00000001
#define GPS_VALID_LATITUDE                                 0x00000002
#define GPS_VALID_LONGITUDE                                0x00000004
#define GPS_VALID_SPEED                                    0x00000008
#define GPS_VALID_HEADING                                  0x00000010
#define GPS_VALID_MAGNETIC_VARIATION                       0x00000020
#define GPS_VALID_ALTITUDE_WRT_SEA_LEVEL                   0x00000040
#define GPS_VALID_ALTITUDE_WRT_ELLIPSOID                   0x00000080
#define GPS_VALID_POSITION_DILUTION_OF_PRECISION           0x00000100
#define GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION         0x00000200
#define GPS_VALID_VERTICAL_DILUTION_OF_PRECISION           0x00000400
#define GPS_VALID_SATELLITE_COUNT                          0x00000800
#define GPS_VALID_SATELLITES_USED_PRNS                     0x00001000
#define GPS_VALID_SATELLITES_IN_VIEW                       0x00002000
#define GPS_VALID_SATELLITES_IN_VIEW_PRNS                  0x00004000
#define GPS_VALID_SATELLITES_IN_VIEW_ELEVATION             0x00008000
#define GPS_VALID_SATELLITES_IN_VIEW_AZIMUTH               0x00010000
#define GPS_VALID_SATELLITES_IN_VIEW_SIGNAL_TO_NOISE_RATIO 0x00020000

typedef enum
{
   GPS_FIX_QUALITY_UNKNOWN = 0,
   GPS_FIX_QUALITY_GPS,
   GPS_FIX_QUALITY_DGPS
} GPS_FIX_QUALITY;

typedef enum
{
   GPS_FIX_UNKNOWN = 0,
   GPS_FIX_2D,
   GPS_FIX_3D
} GPS_FIX_TYPE;

typedef enum
{
   GPS_FIX_SELECTION_UNKNOWN = 0,
   GPS_FIX_SELECTION_AUTO,
   GPS_FIX_SELECTION_MANUAL
} GPS_FIX_SELECTION;

typedef struct _GPS_POSITION
{
   DWORD dwVersion;
   DWORD dwSize;
   DWORD dwValidFields;
   DWORD dwFlags;
   SYSTEMTIME stUTCTime;
   double dblLatitude;
   double dblLongitude;
   float  flSpeed;
   float  flHeading;
   double dblMagneticVariation;
   float  flAltitudeWRTSeaLevel;
   float  flAltitudeWRTEllipsoid;
   GPS_FIX_QUALITY     FixQuality;
   GPS_FIX_TYPE        FixType;
   GPS_FIX_SELECTION   SelectionType;
   float flPositionDilutionOfPrecision;
   float flHorizontalDilutionOfPrecision;
   float flVerticalDilutionOfPrecision;
   DWORD dwSatelliteCount;
   DWORD rgdwSatellitesUsedPRNs[GPS_MAX_SATELLITES];
   DWORD dwSatellitesInView;
   DWORD rgdwSatellitesInViewPRNs[GPS_MAX_SATELLITES];
   DWORD rgdwSatellitesInViewElevation[GPS_MAX_SATELLITES];
   DWORD rgdwSatellitesInViewAzimuth[GPS_MAX_SATELLITES];
   DWORD rgdwSatellitesInViewSignalToNoiseRatio[GPS_MAX_SATELLITES];
} GPS_POSITION, *PGPS_POSITION;

// Actually an enumeration, but its definition is not public and we won't be using it.
typedef DWORD GPS_HW_STATE;

typedef struct _GPS_DEVICE_STATUS
{
   DWORD dwValidFields;
   GPS_HW_STATE ghsHardwareState;
   DWORD dwEphSVMask;
   DWORD dwAlmSVMask;
   DWORD rgdwSatellitesInViewPRNs[GPS_MAX_SATELLITES];
   DWORD rgdwSatellitesInViewCarrierToNoiseRatio[GPS_MAX_SATELLITES];
   DWORD dwDeviceError;
} GPS_DEVICE_STATUS, *PGPS_DEVICE_STATUS; 

typedef struct _GPS_DEVICE
{
   DWORD    dwVersion;
   DWORD    dwSize;
   DWORD    dwServiceState;
   DWORD    dwDeviceState;
   FILETIME ftLastDataReceived;
   WCHAR    szGPSDriverPrefix[GPS_MAX_PREFIX_NAME];
   WCHAR    szGPSMultiplexPrefix[GPS_MAX_PREFIX_NAME];
   WCHAR    szGPSFriendlyName[GPS_MAX_FRIENDLY_NAME];
   GPS_DEVICE_STATUS    gdsDeviceStatus;
} *PGPS_DEVICE, GPS_DEVICE;

typedef HANDLE (__stdcall *GPSOpenDeviceProc)
(
   HANDLE      hNewLocationData,
   HANDLE      hDeviceStateChange,
   const WCHAR *szDeviceName,
   DWORD       dwFlags
);

typedef DWORD (__stdcall *GPSCloseDeviceProc)
(
   HANDLE   hGPSDevice
);

typedef DWORD (__stdcall *GPSGetPositionProc)
(
   HANDLE         hGPSDevice,
   GPS_POSITION   *pGPSPosition,
   DWORD          dwMaximumAge,
   DWORD          dwFlags
);

typedef DWORD (__stdcall *GPSGetDeviceStateProc)
(
   GPS_DEVICE  *pGPSDevice
);

static HANDLE gpsHandle;
static HINSTANCE gpsDll;
static GPSOpenDeviceProc      GPSOpenDevice;
static GPSCloseDeviceProc     GPSCloseDevice;
static GPSGetPositionProc     GPSGetPosition;
static GPSGetDeviceStateProc  GPSGetDeviceState;
static int32 instanceCount = 0;

static Err nativeStartGPS()
{
   Err err = -1; // unable to load the required resources.
   if (++instanceCount > 1)
      return NO_ERROR;
   if ((gpsDll = LoadLibrary(TEXT("gpsapi.dll"))) == null)
      return err;

   GPSOpenDevice     = (GPSOpenDeviceProc)    GetProcAddress(gpsDll, TEXT("GPSOpenDevice"));
   GPSCloseDevice    = (GPSCloseDeviceProc)   GetProcAddress(gpsDll, TEXT("GPSCloseDevice"));
   GPSGetPosition    = (GPSGetPositionProc)   GetProcAddress(gpsDll, TEXT("GPSGetPosition"));
   GPSGetDeviceState = (GPSGetDeviceStateProc)GetProcAddress(gpsDll, TEXT("GPSGetDeviceState"));

   if (!GPSOpenDevice || !GPSCloseDevice || !GPSGetPosition || !GPSGetDeviceState)
      goto onError;
   if ((gpsHandle = GPSOpenDevice(null, null, null, 0)) == null)
   {
      err = GetLastError();
      goto onError;
   }
   return NO_ERROR;

onError:
   if (gpsDll != null)
   {
      GPSOpenDevice = null;
      GPSCloseDevice = null;
      GPSGetPosition = null;
      GPSGetDeviceState = null;
      FreeLibrary(gpsDll);
      gpsDll = null;
   }
   instanceCount = 0;
   return err;
}

static void nativeStopGPS()
{
   if (--instanceCount < 0)
      instanceCount = 0;
   else if (instanceCount == 0)
   {
      GPSCloseDevice(gpsHandle);
      gpsHandle = null;
      GPSOpenDevice = null;
      GPSCloseDevice = null;
      GPSGetPosition = null;
      GPSGetDeviceState = null;
      FreeLibrary(gpsDll);
      gpsDll = null;
   }
}

static Err nativeUpdateLocation(Context currentContext, TCObject gpsObject, int32* flags)
{
   TCObject lastFix = GPS_lastFix(gpsObject);
   GPS_POSITION position;
   Err err;

   if (gpsDll == null)
      return -1;

   xmemzero(&position, sizeof(GPS_POSITION));
   position.dwVersion = GPS_VERSION_1;
   position.dwSize = sizeof(GPS_POSITION);
   if ((err = GPSGetPosition(gpsHandle, &position, 30000, 0)) != NO_ERROR)
      return err;

   if (position.dwValidFields & GPS_VALID_LATITUDE)
   {
      *flags |= 1;
      GPS_latitude(gpsObject) = position.dblLatitude;
   }
   if (position.dwValidFields & GPS_VALID_LONGITUDE)
   {
      *flags |= 2;
      GPS_longitude(gpsObject) = position.dblLongitude;
   }
   if (position.dwValidFields & GPS_VALID_HEADING)
   {
      *flags |= 4;
      GPS_direction(gpsObject) = position.flHeading;
   }
   if (position.dwValidFields & GPS_VALID_SPEED)
   {
      *flags |= 8;
      GPS_velocity(gpsObject) = position.flSpeed;
   }
   if (position.dwValidFields & GPS_VALID_SATELLITE_COUNT)
   {
      *flags |= 16;
      GPS_satellites(gpsObject) = position.dwSatelliteCount;
   }    
   if (position.dwValidFields & GPS_VALID_POSITION_DILUTION_OF_PRECISION)
   {
      *flags |= 32;
      GPS_pdop(gpsObject) = position.flPositionDilutionOfPrecision;
   }    
   
   if (position.dwValidFields & GPS_VALID_UTC_TIME)
   {
      Time_year(lastFix)   = position.stUTCTime.wYear;
      Time_month(lastFix)  = position.stUTCTime.wMonth;
      Time_day(lastFix)    = position.stUTCTime.wDay;
      Time_hour(lastFix)   = position.stUTCTime.wHour;
      Time_minute(lastFix) = position.stUTCTime.wMinute;
      Time_second(lastFix) = position.stUTCTime.wSecond;
      Time_millis(lastFix) = position.stUTCTime.wMilliseconds;
   }

   return NO_ERROR;
}
