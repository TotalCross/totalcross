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

#if _WIN32_WCE >= 300
 #include "win/media_Camera_c.h"
#elif defined(PALMOS)
 #include "palm/media_Camera_c.h"
#elif defined(ANDROID)
 #include "android/media_Camera_c.h"
#elif defined(darwin)
 #include "darwin/media_Camera_c.h"
#endif

//#define TOGGLE_CLICK_BENCH

#ifdef TOGGLE_CLICK_BENCH
#define CLICK_BENCH(x) x
#else
#define CLICK_BENCH(x)
#endif

//////////////////////////////////////////////////////////////////////////
void createTempFileName(char* dest, char* ext)
{
   IntBuf intBuf;
   xstrcpy(dest, getAppPath());
   xstrcat(dest, "/");
   xstrcat(dest, getApplicationIdStr());
   xstrcat(dest, int2str(getTimeStamp(), intBuf));
   xstrcat(dest, ext);
}
TC_API void tumC_nativeClick(NMParams p) // totalcross/ui/media/Camera native private String nativeClick();
{
#if defined(PALMOS)
   Object cameraObj = p->obj[0];
   Object defaultFileName = Camera_defaultFileName(cameraObj);
   char tempPictureName[MAX_PATHNAME];
   char fileName[MAX_PATHNAME];
   IntBuf intBuf;
   Err err;

   //flsobral@tc115_12: fixed creation of images without setting defaultFileName.
   xstrcpy(tempPictureName, getAppPath());
   xstrcat(tempPictureName, getApplicationIdStr());
   xstrcat(tempPictureName, int2str(getTimeStamp(), intBuf));
   if (defaultFileName == null)
   {
      xstrcpy(fileName, tempPictureName);
      xstrcat(fileName, ".jpg");
   }
   else
      String2CharPBuf(defaultFileName, fileName);
   xstrcat(tempPictureName, ".tmp");
   
   if ((err = cameraClick(p->currentContext, tempPictureName, fileName)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   else
   {
      p->retO = createStringObjectFromCharP(p->currentContext, fileName, -1);
      setObjectLock(p->retO, UNLOCKED);
   }
#elif defined(ANDROID) || (defined(WINCE) && _WIN32_WCE >= 300) || defined(darwin) //flsobral@tc115_56: fixed Camera API support for WinCE. (nothing was changed, for some reason the compiler was ignoring the WinCE code)
   cameraClick(p);
#else
   UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumC_initCamera(NMParams p) // totalcross/ui/media/Camera native private void initCamera();
{
#if defined(PALMOS)
   Camera_initCamera();
#else
   UNUSED(p)
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumC_nativeFinalize(NMParams p) // totalcross/ui/media/Camera native private void nativeFinalize();
{
#if defined(PALMOS)
   Camera_finalize(); //flsobral@tc115_8: finalizing the object ensure that we are properly handling the camera library, avoiding random resets.
#else
   UNUSED(p)
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tumC_getNativeResolutions(NMParams p) // totalcross/ui/media/Camera static native private String getNativeResolutions();
{
#if defined(ANDROID)
   setObjectLock(p->retO = Camera_getNativeResolutions(p->currentContext), UNLOCKED);
#else
   p->retO = null;
#endif
}
