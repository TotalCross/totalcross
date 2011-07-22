
#include <core/SkBitmap.h>
#include <core/SkPixelRef.h>
#include "tcvm.h"

extern "C"
{
   static VoidP skia;
   typedef void (*pixelsFunc)(int);
   pixelsFunc lockPixels,unlockPixels;
   
   bool graphicsLock(ScreenSurface screen, bool on)
   {         
      ScreenSurfaceEx ex = SCREEN_EX(screen);
      JNIEnv *env = getJNIEnv();
      SkBitmap* bitmap;
      if (skia == null)
      {
         char libname[10];
         xstrcpy(libname,"sgl");
         skia = loadLibrary(libname); // android 1.6
         if (skia == null)
         {
            xstrcpy(libname,"skia");
            skia = loadLibrary(libname); // android 2.0
            if (skia == null)                     
            {
               debug("*** TCVM FATAL ERROR: CANNOT FIND REQUIRED ANDROID GRAPHICS LIBRARY");
               return false;
            }
         }
         lockPixels   = (pixelsFunc)getProcAddress(skia, "_ZNK8SkBitmap10lockPixelsEv");
         unlockPixels = (pixelsFunc)getProcAddress(skia, "_ZNK8SkBitmap12unlockPixelsEv");
         if (lockPixels == null || unlockPixels == null)
         {
            debug("*** TCVM FATAL ERROR: CANNOT FIND ENTRIES FOR lockPixels (%X) / unlockPixels (%X)",lockPixels,unlockPixels);
            return false;
         }
      }
      bitmap = (SkBitmap *)env->GetIntField(ex->mBitmap, ex->mNativeBitmapID);
      debug("bitmap: %X",bitmap);
      
      if (on)
      {
         lockPixels((int)bitmap); //bitmap->lockPixels();
         screen->pixels = (uint8*)bitmap->getPixels();
      }
      else
      {
         unlockPixels((int)bitmap); //bitmap->unlockPixels();
         screen->pixels = (uint8*)1;
      }
      return screen->pixels != 0;
   }
}
