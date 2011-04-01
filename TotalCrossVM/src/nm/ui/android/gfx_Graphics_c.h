/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "gfx_ex.h"

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnDraw
 * Signature: ([S)V
 */
void Java_totalcross_Launcher4A_nativeOnDraw(JNIEnv *env, jobject this, jobject mBitmap)
{                     
   ScreenSurfaceEx ex;
   if (screen.extension == null)
      screen.extension = newX(ScreenSurfaceEx);
   ex = SCREEN_EX(&screen);
   if (ex->mNativeBitmapID == 0)
      ex->mNativeBitmapID = (*env)->GetFieldID(env, JOBJ_CLASS(mBitmap), "mNativeBitmap", "I");
   if (ex->mBitmap != null)
      (*env)->DeleteGlobalRef(env, (jobject)ex->mBitmap);
   ex->mBitmap = (jobject)(*env)->NewGlobalRef(env, mBitmap);
   graphicsLock(&screen,true);
   graphicsLock(&screen,false);
   //screen.pixels = (uint8*)1; // initialize to something different of zero
}

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnSizeChanged
 * Signature: (II)V
 */

void callExecuteProgram(); // on android/startup_c.h

void privateScreenChange(int32 w, int32 h)
{
   UNUSED(w)
   UNUSED(h)
}

bool graphicsStartup(ScreenSurface screen)
{
   screen->bpp = 16;
   screen->screenX = screen->screenY = 0;
   screen->screenW = lastW;
   screen->screenH = lastH;
   screen->hRes = ascrHRes;
   screen->vRes = ascrVRes;
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   screen->pitch = screen->screenW * screen->bpp / 8;
   return screen->pixels != null;
}

void graphicsUpdateScreen(ScreenSurface screen, int32 transitionEffect)
{
   JNIEnv *env = getJNIEnv();
   if (env)
      (*env)->CallStaticVoidMethod(env, applicationClass, jupdateScreen, screen->dirtyX1,screen->dirtyY1,screen->dirtyX2,screen->dirtyY2,transitionEffect); // will call Java_totalcross_Launcher4A_nativeOnDraw
   else
      debug("thread not attached!");
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   if (!isScreenChange)
      xfree(screen->extension);
}

