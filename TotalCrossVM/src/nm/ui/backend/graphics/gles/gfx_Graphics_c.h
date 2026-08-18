// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "gfx_ex.h"

#if defined(ANDROID) || (defined(TC_OS_ANDROID) && TC_OS_ANDROID)
 #define TC_GLES_ANDROID 1
 #include "../skia/skia.h"
 #include <android/native_window.h>
 #include <android/native_window_jni.h>
 #include <GLES2/gl2.h>
 #include <GLES2/gl2ext.h>
 #include <EGL/egl.h>
#else
 #define TC_GLES_ANDROID 0
 #include <OpenGLES/ES2/gl.h>
 #include <OpenGLES/ES2/glext.h>
 #define __gl2_h_
#endif

#if defined(darwin) || (defined(TC_OS_IOS) && TC_OS_IOS)
bool isIpad;
#else
bool isIpad = false;
#endif

static bool surfaceWillChange;

#if defined(darwin) || (defined(TC_OS_IOS) && TC_OS_IOS)
void iphone_privateSetSurfaceWillChange(bool willChange)
{
   surfaceWillChange = willChange;
}
#endif

int32 realAppH,appW,appH,glShiftY;
extern float f255[256];
int32 flen;

static float lastAlphaMask = -1;
static void resetGlobals()
{
   lastAlphaMask = -1;
}

bool initGLES(ScreenSurface screenSurface);

void setTimerInterval(int32 t);
int32 desiredglShiftY;
int32 setShiftYonNextUpdateScreen;

#if TC_GLES_ANDROID
static ScreenSurfaceEx androidGetScreenExtension(ScreenSurface screenSurface)
{
   ScreenSurfaceEx extension = SCREEN_EX(screenSurface);
   if (extension == null)
   {
      extension = (ScreenSurfaceEx)xmalloc(sizeof(TScreenSurfaceEx));
      if (extension == null)
         return null;

      memset(extension, 0, sizeof(TScreenSurfaceEx));
      extension->display = EGL_NO_DISPLAY;
      extension->surface = EGL_NO_SURFACE;
      extension->context = EGL_NO_CONTEXT;
      screenSurface->extension = extension;
   }
   return extension;
}

static void androidDestroyEGL(ScreenSurfaceEx extension)
{
   if (extension == null)
      return;

   if (extension->display != EGL_NO_DISPLAY)
   {
      eglMakeCurrent(extension->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
      if (extension->context != EGL_NO_CONTEXT)
         eglDestroyContext(extension->display, extension->context);
      if (extension->surface != EGL_NO_SURFACE)
         eglDestroySurface(extension->display, extension->surface);
      eglTerminate(extension->display);
   }

   extension->display = EGL_NO_DISPLAY;
   extension->surface = EGL_NO_SURFACE;
   extension->context = EGL_NO_CONTEXT;
   extension->graphicsInitialized = false;
}

static void androidReleaseNativeWindow(ScreenSurfaceEx extension)
{
   if (extension != null && extension->window != null)
   {
      ANativeWindow_release(extension->window);
      extension->window = null;
   }
}

static bool androidApplySurfaceChange(JNIEnv *env, jobject javaSurface,
                                      int32 width, int32 height,
                                      double contentScale, double fontScale,
                                      int32 hRes, int32 vRes,
                                      int32 fontHeight, uint32 generation)
{
   ScreenSurfaceEx extension;
   ANativeWindow *newWindow;
   bool nativeSurfaceChanged;
   bool hadGraphics;
   TScreenConfiguration configuration;

   if (javaSurface == null || width <= 0 || height <= 0)
      return false;
   if (generation < screen.surfaceGeneration)
      return false;

   extension = androidGetScreenExtension(&screen);
   if (extension == null)
      return false;

   newWindow = ANativeWindow_fromSurface(env, javaSurface);
   if (newWindow == null)
      return false;

   nativeSurfaceChanged = extension->window != newWindow;
   hadGraphics = extension->graphicsInitialized;

   if (nativeSurfaceChanged)
   {
      if (hadGraphics)
         androidDestroyEGL(extension);
      androidReleaseNativeWindow(extension);
      extension->window = newWindow;
   }
   else
   {
      // ANativeWindow_fromSurface returns a retained reference.
      ANativeWindow_release(newWindow);
   }

   memset(&configuration, 0, sizeof(configuration));
   configuration.width = width;
   configuration.height = height;
   configuration.hRes = hRes;
   configuration.vRes = vRes;
   configuration.contentScale = contentScale > 0 ? contentScale : 1;
   configuration.fontScale = fontScale > 0 ? fontScale : 1;
   configuration.deviceFontHeight = fontHeight;
   configuration.generation = generation;
   configuration.surfaceReady = true;
   configuration.nativeSurfaceChanged = nativeSurfaceChanged;

   screen.bpp = ANDROID_BPP;
   screenApplyConfiguration(&screen, &configuration);

   desiredglShiftY = glShiftY = 0;
   setShiftYonNextUpdateScreen = true;
   appW = width;
   appH = height;
   surfaceWillChange = false;
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);

   if (nativeSurfaceChanged && hadGraphics && !initGLES(&screen))
   {
      screen.surfaceReady = false;
      surfaceWillChange = true;
      return false;
   }

   return true;
}

JNIEXPORT jboolean JNICALL Java_totalcross_Launcher4A_nativeSurfaceChanged(
      JNIEnv *env, jobject thisObject, jobject javaSurface,
      jint width, jint height, jdouble contentScale, jdouble fontScale,
      jint hRes, jint vRes, jint fontHeight, jint generation)
{
   UNUSED(thisObject);
   return androidApplySurfaceChange(env, javaSurface, width, height,
         contentScale, fontScale, hRes, vRes, fontHeight,
         (uint32)generation) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeSurfaceDestroyed(
      JNIEnv *env, jobject thisObject, jint generation)
{
   UNUSED(env);
   UNUSED(thisObject);

   if ((uint32)generation < screen.surfaceGeneration)
      return;

   screen.surfaceGeneration = (uint32)generation;
   screen.surfaceReady = false;
   screen.pendingChangeFlags = SCREEN_CHANGE_NONE;
   surfaceWillChange = true;
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeSetKeyboardShift(
      JNIEnv *env, jobject thisObject, jint percentage)
{
   UNUSED(env);
   UNUSED(thisObject);

   if (needsPaint != null)
   {
      desiredglShiftY = percentage == 0 ? 0 : percentage;
      setShiftYonNextUpdateScreen = true;
      *needsPaint = true;
      setTimerInterval(1);
   }
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativePrepareForPause(
      JNIEnv *env, jobject thisObject)
{
   UNUSED(env);
   UNUSED(thisObject);

   if (ENABLE_TEXTURE_TRACE)
      debug("preparing graphics for application pause");

   if (glShiftY != 0 && needsPaint != null)
   {
      desiredglShiftY = 0;
      setShiftYonNextUpdateScreen = true;
      *needsPaint = true;
      setTimerInterval(1);
   }
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativePrepareForResume(
      JNIEnv *env, jobject thisObject)
{
   UNUSED(env);
   UNUSED(thisObject);
   surfaceWillChange = true;
}
#endif

#if TC_GLES_ANDROID
bool initGLES(ScreenSurface screenSurface)
{
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
   const EGLint contextAttribs[] = {
      EGL_CONTEXT_CLIENT_VERSION, 2,
      EGL_NONE
   };
   const EGLint surfaceAttribs[] = {
      EGL_RENDER_BUFFER, EGL_BACK_BUFFER,
      EGL_NONE
   };
   ScreenSurfaceEx extension = androidGetScreenExtension(screenSurface);
   EGLDisplay display;
   EGLConfig config;
   EGLint numConfigs;
   EGLint format;
   EGLSurface surface;
   EGLContext context;
   EGLint width;
   EGLint height;

   if (extension == null || extension->window == null)
   {
      debug("window is null");
      return false;
   }
   if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY)
   {
      debug("eglGetDisplay() returned error %d", eglGetError());
      return false;
   }
   if (!eglInitialize(display, 0, 0))
   {
      debug("eglInitialize() returned error %d", eglGetError());
      return false;
   }
   if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs))
   {
      debug("eglChooseConfig() returned error %d", eglGetError());
      eglTerminate(display);
      return false;
   }
   if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format))
   {
      debug("eglGetConfigAttrib() returned error %d", eglGetError());
      eglTerminate(display);
      return false;
   }

   ANativeWindow_setBuffersGeometry(extension->window, 0, 0, format);

   surface = eglCreateWindowSurface(display, config, extension->window, surfaceAttribs);
   if (surface == EGL_NO_SURFACE)
   {
      debug("eglCreateWindowSurface() returned error %d", eglGetError());
      eglTerminate(display);
      return false;
   }

   context = eglCreateContext(display, config, EGL_NO_CONTEXT, contextAttribs);
   if (context == EGL_NO_CONTEXT)
   {
      debug("eglCreateContext() returned error %d", eglGetError());
      eglDestroySurface(display, surface);
      eglTerminate(display);
      return false;
   }

   if (!eglMakeCurrent(display, surface, surface, context))
   {
      debug("eglMakeCurrent() returned error %d", eglGetError());
      eglDestroyContext(display, context);
      eglDestroySurface(display, surface);
      eglTerminate(display);
      return false;
   }

   if (!eglQuerySurface(display, surface, EGL_WIDTH, &width)
       || !eglQuerySurface(display, surface, EGL_HEIGHT, &height))
   {
      debug("eglQuerySurface() returned error %d", eglGetError());
      eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
      eglDestroyContext(display, context);
      eglDestroySurface(display, surface);
      eglTerminate(display);
      return false;
   }

   extension->display = display;
   extension->surface = surface;
   extension->context = context;
   extension->graphicsInitialized = true;

   glViewport(0, 0, (GLsizei)width, (GLsizei)height);
   glClearColor(1, 0, 0, 1);
   glClearStencil(0);
   glStencilMask(0xffffffff);
   glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

   initSkia(width, height, NULL, screenSurface->pitch, screenSurface->pixelformat);
   return true;
}
#endif

static void setProjectionMatrix(float w, float h)
{
   skia_shiftScreen(w, h, glShiftY);
}

/////////////////////////////////////////////////////////////////////////

void privateScreenChange(int32 w, int32 h)
{
#if defined(darwin) || (defined(TC_OS_IOS) && TC_OS_IOS)
   surfaceWillChange = false;
#endif

   appW = w;
   appH = h;
   setProjectionMatrix(w,h);
}

int32 graphicsStartup(ScreenSurface screenSurface, int16 appTczAttr)
{
   UNUSED(appTczAttr);

   screenSurface->bpp = 32;
   screenSurface->screenX = screenSurface->screenY = 0;
   if (screenSurface->contentScale <= 0)
      screenSurface->contentScale = 1;
   if (screenSurface->fontScale <= 0)
      screenSurface->fontScale = 1;

   if (!screenSurface->surfaceReady || screenSurface->screenW <= 0 || screenSurface->screenH <= 0)
      return false;

   screenSurface->pitch = screenSurface->screenW * screenSurface->bpp / 8;
   return initGLES(screenSurface);
}

int32 graphicsCreateScreenSurface(ScreenSurface screenSurface)
{
#if !TC_GLES_ANDROID
   screenSurface->extension = deviceCtx;
#endif
   screenSurface->pitch = screenSurface->screenW * screenSurface->bpp / 8;
   screenSurface->pixels = (uint8*)1;

#ifdef SKIA_H
   initSkia(screenSurface->screenW, screenSurface->screenH, NULL,
         screenSurface->pitch, screenSurface->pixelformat);
#endif

   return screenSurface->pixels != null;
}

void graphicsDestroy(ScreenSurface screenSurface, int32 isScreenChange)
{
#if TC_GLES_ANDROID
   if (!isScreenChange)
   {
      ScreenSurfaceEx extension = SCREEN_EX(screenSurface);
      androidDestroyEGL(extension);
      androidReleaseNativeWindow(extension);
      xfree(screenSurface->extension);
      screenSurface->extension = null;
   }
#else
   if (isScreenChange)
      screenSurface->extension = NULL;
   else
   {
      if (screenSurface->extension)
         free(screenSurface->extension);
      deviceCtx = screenSurface->extension = NULL;
   }
#endif
}

void setTimerInterval(int32 t);
void setShiftYgl(int32 shiftY)
{
   if (setShiftYonNextUpdateScreen && needsPaint != null)
   {
      setShiftYonNextUpdateScreen = false;
#if TC_GLES_ANDROID
      if (shiftY == 0)
      {
         if (desiredglShiftY == 0)
            lastShiftY = 0;
         shiftY = lastShiftY;
      }
      glShiftY = desiredglShiftY > 0 ? -shiftY * desiredglShiftY / 100 : 0;
#else
      glShiftY = -shiftY;
#endif
      setProjectionMatrix(appW,appH);
      screen.shiftY = shiftY;
      *needsPaint = true;
      setTimerInterval(1);
   }
}

void graphicsUpdateScreenIOS();
void graphicsUpdateScreen(Context currentContext, ScreenSurface screenSurface)
{
   UNUSED(currentContext);
   if (surfaceWillChange || !screenSurface->surfaceReady)
      return;
#ifdef SKIA_H
   flushSkia();
#endif
#if TC_GLES_ANDROID
   {
      ScreenSurfaceEx extension = SCREEN_EX(screenSurface);
      if (extension != null
          && extension->display != EGL_NO_DISPLAY
          && extension->surface != EGL_NO_SURFACE)
         eglSwapBuffers(extension->display, extension->surface);
   }
#elif defined(darwin) || (defined(TC_OS_IOS) && TC_OS_IOS)
   graphicsUpdateScreenIOS();
#endif

   resetGlobals();
}
