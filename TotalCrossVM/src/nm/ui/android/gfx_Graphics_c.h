// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "gfx_ex.h"


#ifdef ANDROID
#include "../skia/skia.h"
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <android/log.h>
#include <time.h>
#include "rotation_trace.h"
#else
//#include <OpenGLES/gltypes.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#define __gl2_h_
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

typedef struct
{
   int generation;
   bool awaitingFirstSwap;
   int windowChanges;
   int destroyEgl;
   int initGles;
   int initSkia;
   int screenChanged;
   int screenChange;
   int graphicsCreate;
   int repaint;
   int swapCount;
   bool firstSwapSeen;
   bool screenChangeReturned;
   bool summaryEmitted;
} RotationTraceState;

static RotationTraceState rotationTraceStates[32];
static int rotationTraceStateCount;
static int rotationTraceCurrentGeneration;
static int rotationTracePendingCount;
static int rotationTracePendingGeneration[32];
static int rotationTracePendingWidth[32];
static int rotationTracePendingHeight[32];

static long long rotationTraceNowNs()
{
   struct timespec now;
   clock_gettime(CLOCK_MONOTONIC, &now);
   return (long long)now.tv_sec * 1000000000LL + now.tv_nsec;
}

static RotationTraceState *rotationTraceFindState(int generation)
{
   int i;
   for (i = 0; i < rotationTraceStateCount; i++)
      if (rotationTraceStates[i].generation == generation)
         return &rotationTraceStates[i];
   return null;
}

static RotationTraceState *rotationTraceNewState(int generation)
{
   RotationTraceState *state;
   if (rotationTraceStateCount < 32)
      state = &rotationTraceStates[rotationTraceStateCount++];
   else
      state = &rotationTraceStates[generation % 32];
   memset(state, 0, sizeof(*state));
   state->generation = generation;
   state->awaitingFirstSwap = true;
   return state;
}

static void rotationTracePrint(const char *format, ...)
{
   va_list args;
   va_start(args, format);
   __android_log_vprint(ANDROID_LOG_INFO, "TotalCrossRotation", format, args);
   va_end(args);
}

void rotationTraceBeginGeneration(int generation)
{
   if (generation > 0)
   {
      rotationTraceCurrentGeneration = generation;
      rotationTraceNewState(generation);
   }
}

void rotationTraceSelectGeneration(int generation)
{
   if (generation > 0 && rotationTraceFindState(generation) != null)
      rotationTraceCurrentGeneration = generation;
}

void rotationTraceEnqueueScreenChanged(int generation, int width, int height)
{
   if (generation <= 0 || rotationTraceFindState(generation) == null || rotationTracePendingCount == 32)
      return;
   rotationTracePendingGeneration[rotationTracePendingCount] = generation;
   rotationTracePendingWidth[rotationTracePendingCount] = width;
   rotationTracePendingHeight[rotationTracePendingCount] = height;
   rotationTracePendingCount++;
}

int rotationTraceSelectScreenChanged(int width, int height)
{
   int generation;
   int i;
   if (rotationTraceStateCount == 0)
      return 0;
   if (rotationTracePendingCount == 0)
      return 0;
   generation = rotationTracePendingGeneration[0];
   for (i = 0; i + 1 < rotationTracePendingCount; i++)
   {
      rotationTracePendingGeneration[i] = rotationTracePendingGeneration[i + 1];
      rotationTracePendingWidth[i] = rotationTracePendingWidth[i + 1];
      rotationTracePendingHeight[i] = rotationTracePendingHeight[i + 1];
   }
   rotationTracePendingCount--;
   (void)width;
   (void)height;
   return generation;
}

void rotationTraceStage(const char *stage, int width, int height)
{
   RotationTraceState *state = rotationTraceFindState(rotationTraceCurrentGeneration);
   long long now;
   if (state == null)
      return;

   if (strEq(stage, "native_window_changed")) state->windowChanges++;
   else if (strEq(stage, "destroy_egl")) state->destroyEgl++;
   else if (strEq(stage, "init_gles")) state->initGles++;
   else if (strEq(stage, "init_skia")) state->initSkia++;
   else if (strEq(stage, "screen_changed_handled")) state->screenChanged++;
   else if (strEq(stage, "screen_change_entered")) state->screenChange++;
   else if (strEq(stage, "screen_change_returned")) state->screenChangeReturned = true;
   else if (strEq(stage, "graphics_create_screen_surface")) state->graphicsCreate++;
   else if (strEq(stage, "repaint_active_windows")) state->repaint++;

   now = rotationTraceNowNs();
   rotationTracePrint("ROTATION_TRACE generation=%d stage=%s ts_ns=%lld width=%d height=%d",
      state->generation, stage, now, width, height);
}

void rotationTraceOnSwap(int width, int height)
{
   RotationTraceState *state = rotationTraceFindState(rotationTraceCurrentGeneration);
   if (state == null)
      return;
   state->swapCount++;
   if (!state->awaitingFirstSwap)
      return;
   state->awaitingFirstSwap = false;
   state->firstSwapSeen = true;
   rotationTraceStage("first_egl_swap_buffers", width, height);
   rotationTraceEmitSummary();
}

void rotationTraceEmitSummary()
{
   RotationTraceState *state = rotationTraceFindState(rotationTraceCurrentGeneration);
   if (state == null || !state->firstSwapSeen || !state->screenChangeReturned || state->summaryEmitted)
      return;
   state->summaryEmitted = true;
   rotationTracePrint("ROTATION_TRACE_SUMMARY generation=%d window_changes=%d destroy_egl=%d init_gles=%d init_skia=%d screen_changed=%d screen_change=%d graphics_create=%d repaint=%d swaps=%d",
      state->generation, state->windowChanges, state->destroyEgl, state->initGles,
      state->initSkia, state->screenChanged, state->screenChange, state->graphicsCreate,
      state->repaint, state->swapCount);
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeRotationTraceGeneration
  (JNIEnv *env, jobject this, jint generation)
{
   (void)env;
   (void)this;
   rotationTraceBeginGeneration(generation);
}

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeRotationTraceScreenChanged
  (JNIEnv *env, jobject this, jint generation, jint width, jint height)
{
   (void)env;
   (void)this;
   rotationTraceEnqueueScreenChanged(generation, width, height);
}
#endif
static bool surfaceWillChange;

#ifdef darwin
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

bool initGLES(ScreenSurface screen);

void setTimerInterval(int32 t);
int32 desiredglShiftY;
int32 setShiftYonNextUpdateScreen;
#ifdef ANDROID
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jobject surface, jint width, jint height) // called only once
{
   if (!screen.extension)
      screen.extension = xmalloc(4);//newX(ScreenSurfaceEx);

   if (surface == null) // passed null when the surface is destroyed
   {
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
   if (lastWindow && lastWindow != window)
   {
      rotationTraceStage("native_window_changed", width, height);
      if (window == null) {debug("window is null. surface is %p. app will likely crash...", (void*)surface);}
      destroyEGL();
      initGLES(&screen);
      if (ENABLE_TEXTURE_TRACE) debug("invalidating textures due to screen change 2");
   }
   lastWindow = window;
}
#endif






#ifdef ANDROID
#include "../skia/skia.h"

bool initGLES(ScreenSurface screen)
{
	   rotationTraceStage("init_gles", screen->screenW, screen->screenH);
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
	   rotationTraceStage("destroy_egl", appW, appH);
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
#ifdef darwin
   surfaceWillChange = false;
#endif
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
   rotationTraceStage("graphics_create_screen_surface", screen->screenW, screen->screenH);
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
   rotationTraceOnSwap(screen->screenW, screen->screenH);
#elif defined (darwin)
   graphicsUpdateScreenIOS();
#endif

   resetGlobals();
}
