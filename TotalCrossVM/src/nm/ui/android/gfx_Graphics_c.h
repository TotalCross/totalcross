// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "gfx_ex.h"
#include <stdio.h>


#ifdef ANDROID
#include "skia.h"
#include <android/log.h>
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#else
//#include <OpenGLES/gltypes.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#define __gl2_h_
#endif

#ifdef ANDROID
#define TC_ROTATION_GFX_LOG(...) __android_log_print(ANDROID_LOG_INFO, "TotalCross", __VA_ARGS__)
#else
#define TC_ROTATION_GFX_LOG(...) do { printf(__VA_ARGS__); printf("\n"); } while (0)
#endif

#ifdef darwin
bool isIpad;
#else
bool isIpad = false;
#endif


#ifdef ANDROID
static ANativeWindow *window,*lastWindow;
static EGLDisplay _display;
static EGLSurface _surface;
static EGLContext _context;
static void destroyEGL();
#endif
static bool surfaceWillChange;

int32 realAppH,appW,appH,glShiftY;
extern float f255[256];
int32 flen;

static float lastAlphaMask = -1;
static void resetGlobals()
{
   lastAlphaMask = -1;
}

bool initGLES(ScreenSurface screen);

void setTimerInterval(int32 t);
int32 desiredglShiftY;
int32 setShiftYonNextUpdateScreen;
#ifdef ANDROID
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jobject surface, jint width, jint height) // called only once
{
   TC_ROTATION_GFX_LOG("TC_ROTATION android nativeInitSize enter surface=%p width=%d height=%d screenExt=%p window=%p lastWindow=%p",
      (void*)surface, width, height, screen.extension, (void*)window, (void*)lastWindow);
   if (!screen.extension)
      screen.extension = xmalloc(4);//newX(ScreenSurfaceEx);

   if (surface == null) // passed null when the surface is destroyed
   {
      TC_ROTATION_GFX_LOG("TC_ROTATION android nativeInitSize nullSurface command=%d value=%d needsPaint=%p glShiftY=%d desiredglShiftY=%d",
         width, height, (void*)needsPaint, glShiftY, desiredglShiftY);
      if (width == -999)
      {
         if (needsPaint != null)
         {
            desiredglShiftY = height == 0 ? 0 : height; // change only after the next screen update, since here we are running in a different thread
            setShiftYonNextUpdateScreen = true;
            *needsPaint = true; // schedule a screen paint to update the shiftY values
            setTimerInterval(1);
         }
      }
      else
      if (width == -998)
      {
         if (ENABLE_TEXTURE_TRACE) debug("deleting textures due to screen change");
         if (glShiftY != 0) // fixes green screen that occurs when the keyboard is open and the user turns off the device
         {
            desiredglShiftY = 0; // change only after the next screen update, since here we are running in a different thread
            setShiftYonNextUpdateScreen = true;
            *needsPaint = true; // schedule a screen paint to update the shiftY values
            setTimerInterval(1);
         }
      }
      else
      if (width == -997) // when the screen is turned off and on again, this ensures that the textures will be recreated
      {
         if (lastWindow)
         {
            if (ENABLE_TEXTURE_TRACE) debug("invalidating textures due to screen change 1");
         }
      }
      else
         surfaceWillChange = true; // block all screen updates
      return;
   }
   desiredglShiftY = glShiftY = 0;
   setShiftYonNextUpdateScreen = true;
   appW = width;
   appH = height;
   surfaceWillChange = false;
   if (window) // fixed memory leak
      ANativeWindow_release(window);

   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
   TC_ROTATION_GFX_LOG("TC_ROTATION android nativeInitSize surfaceApplied app=%dx%d realAppH=%d window=%p lastWindow=%p",
      appW, appH, realAppH, (void*)window, (void*)lastWindow);
   if (lastWindow && lastWindow != window)
   {
      if (window == null) {debug("window is null. surface is %p. app will likely crash...", (void*)surface);}
      destroyEGL();
      initGLES(&screen);
      if (ENABLE_TEXTURE_TRACE) debug("invalidating textures due to screen change 2");
   }
   lastWindow = window;
}
#endif






#ifdef ANDROID
#include "skia.h"

bool initGLES(ScreenSurface screen)
{
	   int32 i;
	   const EGLint attribs[] = {
	      EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
	      EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
	      EGL_BLUE_SIZE, 8,
	      EGL_GREEN_SIZE, 8,
	      EGL_RED_SIZE, 8,
	      EGL_ALPHA_SIZE, 8,
	      EGL_STENCIL_SIZE, 8,
	      EGL_NONE
	   };
	   EGLint context_attribs[] = { 
	      EGL_CONTEXT_CLIENT_VERSION, 2, 
	      EGL_NONE 
	   };
	   const EGLint surfaceAttribs[] = {
	      EGL_RENDER_BUFFER, EGL_BACK_BUFFER,
	      EGL_NONE
	   };
	   
	   EGLDisplay display;
	   EGLConfig config;
	   EGLint numConfigs;
	   EGLint format;
	   EGLSurface surface;
	   EGLContext context;
	   EGLint width;
	   EGLint height;

	   if (!window)                                                             {debug("window is null"); return false;}
	   if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY)    {debug("eglGetDisplay() returned error %d", eglGetError()); return false;}
	   if (!eglInitialize(display, 0, 0))                                       {debug("eglInitialize() returned error %d", eglGetError()); return false;}
	   if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs))         {debug("eglChooseConfig() returned error %d", eglGetError()); destroyEGL(); return false;}
	   if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {debug("eglGetConfigAttrib() returned error %d", eglGetError()); destroyEGL(); return false;}

	   ANativeWindow_setBuffersGeometry(window, 0, 0, format);

	   if (!(surface = eglCreateWindowSurface(display, config, window, surfaceAttribs)))     {debug("eglCreateWindowSurface() returned error %d", eglGetError()); destroyEGL(); return false;}
	   if (!(context = eglCreateContext(display, config, EGL_NO_CONTEXT, context_attribs))) {debug("eglCreateContext() returned error %d", eglGetError()); destroyEGL(); return false;}
	   if (!eglMakeCurrent(display, surface, surface, context))                 {debug("eglMakeCurrent() returned error %d", eglGetError()); destroyEGL(); return false;}
	   if (!eglQuerySurface(display, surface, EGL_WIDTH, &width) || !eglQuerySurface(display, surface, EGL_HEIGHT, &height)) {debug("eglQuerySurface() returned error %d", eglGetError()); destroyEGL(); return false;}
      TC_ROTATION_GFX_LOG("TC_ROTATION android initGLES eglSurface=%dx%d app=%dx%d screen=%dx%d pitch=%d window=%p",
         width, height, appW, appH, screen->screenW, screen->screenH, screen->pitch, (void*)window);

	   _display = display;
	   _surface = surface;
	   _context = context;
	   
	    glViewport(0, 0, (GLsizei) width, (GLsizei) height);
	    glClearColor(1, 0, 0, 1);
	    glClearStencil(0);
	    glStencilMask(0xffffffff);
	    glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	
   initSkia(width, height, NULL, screen->pitch, screen->pixelformat);

   return true;
}

static void destroyEGL()
{
   eglMakeCurrent(_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
   eglDestroyContext(_display, _context);
   eglDestroySurface(_display, _surface);
   eglTerminate(_display);

   _display = EGL_NO_DISPLAY;
   _surface = EGL_NO_SURFACE;
   _context = EGL_NO_CONTEXT;
}
#endif

static void setProjectionMatrix(float w, float h)
{
    skia_shiftScreen(w, h, glShiftY);
}

/////////////////////////////////////////////////////////////////////////

void privateScreenChange(int32 w, int32 h)
{
   TC_ROTATION_GFX_LOG("TC_ROTATION gfx privateScreenChange oldApp=%dx%d new=%dx%d glShiftY=%d",
      appW, appH, w, h, glShiftY);
   appW = w;
   appH = h;
   setProjectionMatrix(w,h);
}

int32 graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   screen->bpp = 32;
   screen->screenX = screen->screenY = 0;
   screen->screenW = lastW;
   screen->screenH = lastH;
   screen->hRes = ascrHRes;
   screen->vRes = ascrVRes;

   return initGLES(screen);
}

int32 graphicsCreateScreenSurface(ScreenSurface screen)
{
#ifndef ANDROID
   screen->extension = deviceCtx;
#endif
   screen->pitch = screen->screenW * screen->bpp / 8;
   screen->pixels = (uint8*)1;
 
#ifdef SKIA_H
   initSkia(screen->screenW, screen->screenH, NULL, screen->pitch, screen->pixelformat);
#endif
   
   return screen->pixels != null;
}

void graphicsDestroy(ScreenSurface screen, int32 isScreenChange)
{
#ifdef ANDROID
   if (!isScreenChange)
   {
      xfree(screen->extension);
   }
#else
   if (isScreenChange)
       screen->extension = NULL;
   else
   {
      if (screen->extension)
         free(screen->extension);
      deviceCtx = screen->extension = NULL;
   }
#endif
}

void setTimerInterval(int32 t);
void setShiftYgl(int32 shiftY)
{
   if (setShiftYonNextUpdateScreen && needsPaint != null)
   {
      setShiftYonNextUpdateScreen = false;
#ifdef ANDROID
       if (shiftY == 0) { // keyboard is closing
           if (desiredglShiftY == 0) { // keyboard animation has finished
               lastShiftY = 0; // reset lastShiftY when the animation has finished
           }
           shiftY = lastShiftY; // keep using lastShiftY for smooth slide down animation
       }
       glShiftY = desiredglShiftY > 0 ? -shiftY * desiredglShiftY / 100 : 0;
#else
      glShiftY = -shiftY;
#endif
      setProjectionMatrix(appW,appH);
      screen.shiftY = shiftY;
      *needsPaint = true; // now that the shifts has been set, schedule another window update to paint at the given location
      setTimerInterval(1); // needed, dont remove!
   }
}
void graphicsUpdateScreenIOS();
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{
   if (surfaceWillChange) return;
#ifdef SKIA_H
   flushSkia();
#endif
#if defined (ANDROID)
   eglSwapBuffers(_display, _surface);
#elif defined (darwin)
   graphicsUpdateScreenIOS();
   // erase buffer with keyboard's background color
   PixelConv gray;
   gray.pixel = unsafeAreaColorP ? makePixelARGB(*unsafeAreaColorP) : 0xFFFFFFFF;
   glClearColor(f255[gray.r],f255[gray.g],f255[gray.b],1);
   glClear(GL_COLOR_BUFFER_BIT);
   glClear(GL_DEPTH_BUFFER_BIT);
#endif

   resetGlobals();
}
