
#include <core/SkBitmap.h>
#include <core/SkPixelRef.h>
#include <android/bitmap.h>
#include "tcvm.h"

extern "C"
{
   typedef enum {NO_API_DEFINED,API_1_6,API_2_0,NO_API_FOUND} ApiType;
   ApiType apitype = NO_API_DEFINED;
   
   static VoidP skia;
   // 1.6 api
   typedef void (*pixelsFunc)(int);
   static pixelsFunc lockPixels,unlockPixels;
   
   // 2.0 api
   typedef int (*AndroidBitmap_lockPixelsFunc)(JNIEnv* env, jobject jbitmap, void** addrPtr);
   typedef int (*AndroidBitmap_unlockPixelsFunc)(JNIEnv* env, jobject jbitmap);
   typedef int (*AndroidBitmap_getInfoFunc)(JNIEnv* env, jobject jbitmap, AndroidBitmapInfo* info);
   static AndroidBitmap_lockPixelsFunc lockPixels2;
   static AndroidBitmap_unlockPixelsFunc unlockPixels2;
   static AndroidBitmap_getInfoFunc getInfo2;
   
   bool graphicsLock(ScreenSurface screen, bool on)
   {
      char libname[16];
      ScreenSurfaceEx ex = SCREEN_EX(screen);
      JNIEnv *env = getJNIEnv();
      
      if (apitype == NO_API_DEFINED || apitype == API_2_0) // if we're using Android 2.0, use the official way to get access to the pixels
      {
         if (skia == null)
         {
            xstrcpy(libname,"jnigraphics");
            skia = loadLibrary(libname);   
            if (skia != null)
            {                    
               lockPixels2   = (AndroidBitmap_lockPixelsFunc)getProcAddress(skia, "AndroidBitmap_lockPixels");
               unlockPixels2 = (AndroidBitmap_unlockPixelsFunc)getProcAddress(skia, "AndroidBitmap_unlockPixels");
               getInfo2      = (AndroidBitmap_getInfoFunc)getProcAddress(skia, "AndroidBitmap_getInfo");
               if (lockPixels2 == null || unlockPixels2 == null || getInfo2 == null)
               {
                  unloadLibrary(skia);
                  skia = null;
               }
               else
                  apitype = API_2_0;
            }
         }
         if (apitype == API_2_0)
         {
            if (on)
            {
               int ret;
               AndroidBitmapInfo  info;
               ret = getInfo2(env, ex->mBitmap, &info);
               if (ret < 0)
               {
                  screen->pixels = null;
                  debug("@@@@@ ERROR WHEN LOCKING SCREEN 1: %d",ret);
               }
               else
               {                                                            
                  ret = lockPixels2(env, ex->mBitmap, (void**)&screen->pixels);
                  if (ret < 0)
                  {
                     screen->pixels = null;
                     debug("@@@@@ ERROR WHEN LOCKING SCREEN 2: %d - bmp: %d,%d / screen %d,%d",ret,info.width,info.height,screen->screenW,screen->screenH);
                  }
                  else screen->pitch = info.width * 2;
               }
            }
            else
               unlockPixels2(env, ex->mBitmap);                  
         }
      }

      if (apitype == NO_API_DEFINED || apitype == API_1_6) // no else here!
      {
         if (skia == null)
         {
            xstrcpy(libname,"sgl");
            skia = loadLibrary(libname); // android 1.6
            if (skia == null)
            {
               xstrcpy(libname,"skia");
               skia = loadLibrary(libname); // android 2.0
               if (skia == null)
               {
                  apitype = NO_API_FOUND;
                  debug("*** TCVM FATAL ERROR: CANNOT FIND REQUIRED ANDROID GRAPHICS LIBRARY");
                  return false;
               }
            }
            lockPixels   = (pixelsFunc)getProcAddress(skia, "_ZNK8SkBitmap10lockPixelsEv");
            unlockPixels = (pixelsFunc)getProcAddress(skia, "_ZNK8SkBitmap12unlockPixelsEv");
            if (lockPixels == null || unlockPixels == null)
            {
               apitype = NO_API_FOUND;
               unloadLibrary(skia);
               skia = null;
               debug("*** TCVM FATAL ERROR: CANNOT FIND ENTRIES FOR lockPixels (%X) / unlockPixels (%X)",lockPixels,unlockPixels);
               return false;
            }
            apitype = API_1_6;
         }
         if (apitype == API_1_6)
         {
            SkBitmap* bitmap = (SkBitmap *)env->GetIntField(ex->mBitmap, ex->mNativeBitmapID);
   
            if (on)
            {
               lockPixels((int)bitmap); //bitmap->lockPixels();
               screen->pixels = (uint8*)bitmap->getPixels();
               screen->pitch = bitmap->width() * 2;
            }
            else
            {
               unlockPixels((int)bitmap); //bitmap->unlockPixels();
               screen->pixels = (uint8*)1;
            }
         }
      }
      return (apitype == API_1_6 || apitype == API_2_0) && screen->pixels != 0;
   }
}
