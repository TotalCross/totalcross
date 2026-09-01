// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcsdl.h"
#include "../event/sdl/event_sdl.h"
#include "../nm/ui/Window.h"
#if defined(WIN32) && !defined(WINCE)
#include "SDL2/SDL_syswm.h"
#endif

#include <cmath>
#include <cctype>
#include <cstdio>
#include <cstdlib>

enum TCSDLRequestedPixelFormat
{
   TCSDL_PIXEL_FORMAT_AUTO,
   TCSDL_PIXEL_FORMAT_ARGB8888_REQUEST,
   TCSDL_PIXEL_FORMAT_RGB565_REQUEST
};

static SDL_Renderer *renderer;
static SDL_Texture *texture;
static SDL_Window *window;
static bool sdlInitialized;
static TCSDLRequestedPixelFormat requestedPixelFormat = TCSDL_PIXEL_FORMAT_AUTO;
static bool commandLinePixelFormatSet;
static Uint32 selectedPixelFormat = SDL_PIXELFORMAT_UNKNOWN;

static bool equalsIgnoreCase(const char *value, const char *expected)
{
   while (*value != '\0' && *expected != '\0')
   {
      if (std::tolower((unsigned char)*value) != std::tolower((unsigned char)*expected))
         return false;
      value++;
      expected++;
   }
   return *value == '\0' && *expected == '\0';
}

static bool parsePixelFormat(const char *value, TCSDLRequestedPixelFormat *parsed)
{
   if (equalsIgnoreCase(value, "auto"))
      *parsed = TCSDL_PIXEL_FORMAT_AUTO;
   else if (equalsIgnoreCase(value, "argb8888"))
      *parsed = TCSDL_PIXEL_FORMAT_ARGB8888_REQUEST;
   else if (equalsIgnoreCase(value, "rgb565"))
      *parsed = TCSDL_PIXEL_FORMAT_RGB565_REQUEST;
   else
      return false;
   return true;
}

static Uint32 requestedSDLFormat(TCSDLRequestedPixelFormat requested)
{
   return requested == TCSDL_PIXEL_FORMAT_ARGB8888_REQUEST ? SDL_PIXELFORMAT_ARGB8888
      : requested == TCSDL_PIXEL_FORMAT_RGB565_REQUEST ? SDL_PIXELFORMAT_RGB565
      : SDL_PIXELFORMAT_UNKNOWN;
}

static bool isSupportedPixelFormat(Uint32 format)
{
   return format == SDL_PIXELFORMAT_ARGB8888 || format == SDL_PIXELFORMAT_RGB565;
}

static bool rendererSupportsPixelFormat(Uint32 format)
{
   SDL_RendererInfo info;
   if (SDL_GetRendererInfo(renderer, &info) != 0 || info.num_texture_formats <= 0)
      return true;
   for (Uint32 i = 0; i < info.num_texture_formats; ++i)
      if (info.texture_formats[i] == format)
         return true;
   return false;
}

static SDL_Texture *createStreamingTexture(Uint32 format, int width, int height)
{
   if (!rendererSupportsPixelFormat(format))
   {
      SDL_SetError("renderer does not advertise this texture format");
      return NULL;
   }
   return SDL_CreateTexture(renderer, format, SDL_TEXTUREACCESS_STREAMING, width, height);
}

static bool addCandidate(Uint32 *candidates, int *count, Uint32 candidate)
{
   if (!isSupportedPixelFormat(candidate))
      return false;
   for (int i = 0; i < *count; ++i)
      if (candidates[i] == candidate)
         return false;
   candidates[(*count)++] = candidate;
   return true;
}

static bool selectPixelFormat(ScreenSurface screen)
{
   Uint32 candidates[3];
   int candidateCount = 0;
   if (requestedPixelFormat == TCSDL_PIXEL_FORMAT_AUTO)
   {
      addCandidate(candidates, &candidateCount, SDL_GetWindowPixelFormat(window));
      addCandidate(candidates, &candidateCount, SDL_PIXELFORMAT_ARGB8888);
      addCandidate(candidates, &candidateCount, SDL_PIXELFORMAT_RGB565);
   }
   else
      addCandidate(candidates, &candidateCount, requestedSDLFormat(requestedPixelFormat));

   for (int i = 0; i < candidateCount; ++i)
   {
      texture = createStreamingTexture(candidates[i], screen->screenW, screen->screenH);
      if (texture != NULL)
      {
         selectedPixelFormat = candidates[i];
         return true;
      }
      if (requestedPixelFormat != TCSDL_PIXEL_FORMAT_AUTO)
      {
         fprintf(stderr, "SDL_CreateTexture(%s): %s\n",
            SDL_GetPixelFormatName(candidates[i]), SDL_GetError());
         return false;
      }
   }
   fprintf(stderr, "SDL_CreateTexture(): no supported streaming pixel format: %s\n",
      SDL_GetError());
   return false;
}

bool TCSDL_SetPixelFormatRequest(const char *value)
{
   TCSDLRequestedPixelFormat parsed;
   if (!parsePixelFormat(value, &parsed))
      return false;
   requestedPixelFormat = parsed;
   commandLinePixelFormatSet = true;
   return true;
}

bool TCSDL_QueryWindowMetrics(ScreenSurface screen, TScreenConfiguration *configuration)
{
   int logicalWidth, logicalHeight;
   int physicalWidth, physicalHeight;

   if (screen == NULL || configuration == NULL || window == NULL || renderer == NULL)
      return false;
   SDL_GetWindowSize(window, &logicalWidth, &logicalHeight);
   if (logicalWidth <= 0 || logicalHeight <= 0
      || SDL_GetRendererOutputSize(renderer, &physicalWidth, &physicalHeight) != 0
      || physicalWidth <= 0 || physicalHeight <= 0)
      return false;

   double scaleX = (double)physicalWidth / logicalWidth;
   double scaleY = (double)physicalHeight / logicalHeight;
   if (std::fabs(scaleX - scaleY) > 0.01)
      fprintf(stderr, "SDL returned non-uniform window scale: %.4f x %.4f\n", scaleX, scaleY);

   double contentScale = (scaleX + scaleY) / 2.0;
   configuration->width = physicalWidth;
   configuration->height = physicalHeight;
#if defined(WIN32) && !defined(WINCE)
   UINT dpi = mainHWnd != NULL ? GetDpiForWindow(mainHWnd) : 0;
   configuration->hRes = dpi != 0 ? (int32)dpi : (int32)std::lround(contentScale * 96.0);
   configuration->vRes = configuration->hRes;
#else
   configuration->hRes = (int32)std::lround(contentScale * 96.0);
   configuration->vRes = configuration->hRes;
#endif
   configuration->contentScale = contentScale > 0 ? contentScale : 1;
   configuration->fontScale = screen->fontScale > 0 ? screen->fontScale : 1;
   configuration->deviceFontHeight = screen->deviceFontHeight;
   configuration->generation = screen->surfaceGeneration + 1;
   configuration->surfaceReady = true;
   configuration->nativeSurfaceChanged = false;
   return true;
}

static int32 sdlWindowPosition(TCWindowPositionMode mode, int32 position)
{
   return mode == TC_WINDOW_POSITION_CENTER ? SDL_WINDOWPOS_CENTERED
      : mode == TC_WINDOW_POSITION_EXPLICIT ? position : SDL_WINDOWPOS_UNDEFINED;
}

bool TCSDL_Init(ScreenSurface screen, const char *title, bool fullScreen, int16 appTczAttr)
{
   SDL_DisplayMode displayMode;
   SDL_Rect displayBounds;
   SDL_Rect usableBounds;
   TCDisplayMetrics display = { 0 };
   TCWindowStartupOptions options = desktopWindowStartupOptions;
   TCWindowStartupConfiguration startupConfiguration;
   Uint32 flags = SDL_WINDOW_SHOWN | SDL_WINDOW_ALLOW_HIGHDPI;

#if defined(WIN32) && !defined(WINCE)
   SDL_SetHint(SDL_HINT_WINDOWS_DPI_SCALING, "1");
#endif
#if __APPLE__
   SDL_SetHint(SDL_HINT_TRACKPAD_IS_TOUCH_ONLY, "1");
#endif

   if (SDL_Init(SDL_INIT_VIDEO) != 0)
   {
      fprintf(stderr, "SDL_Init(): %s\n", SDL_GetError());
      return false;
   }
   sdlInitialized = true;

   if (!commandLinePixelFormatSet)
   {
      const char *value = getenv("TC_SDL_PIXEL_FORMAT");
      TCSDLRequestedPixelFormat parsed;
      if (value != NULL && *value != '\0')
      {
         if (parsePixelFormat(value, &parsed))
            requestedPixelFormat = parsed;
         else
         {
            requestedPixelFormat = TCSDL_PIXEL_FORMAT_AUTO;
            fprintf(stderr, "Ignoring invalid TC_SDL_PIXEL_FORMAT; expected auto, argb8888, or rgb565.\n");
         }
      }
   }

   if (SDL_GetDisplayBounds(0, &displayBounds) == 0)
   {
      display.width = displayBounds.w;
      display.height = displayBounds.h;
   }
   if (SDL_GetDisplayUsableBounds(0, &usableBounds) == 0)
   {
      display.usableWidth = usableBounds.w;
      display.usableHeight = usableBounds.h;
   }
   if ((display.width <= 0 || display.height <= 0)
      && SDL_GetCurrentDisplayMode(0, &displayMode) == 0)
   {
      display.width = displayMode.w;
      display.height = displayMode.h;
   }
   if (display.width <= 0 || display.height <= 0)
   {
      display.width = 800;
      display.height = 600;
   }
   if (display.usableWidth <= 0)
      display.usableWidth = display.width;
   if (display.usableHeight <= 0)
      display.usableHeight = display.height;

   options.appTczAttr = appTczAttr;
   options.legacyFullscreen = fullScreen;
   windowLoadStartupEnvironment(&options);
   if (!windowResolveStartupConfiguration(&options, &display, &startupConfiguration))
   {
      fprintf(stderr, "Could not resolve the initial SDL window size.\n");
      TCSDL_DestroyWindow(screen);
      return false;
   }

   if (startupConfiguration.fullscreen)
      flags |= SDL_WINDOW_FULLSCREEN;
   else if (startupConfiguration.maximized)
      flags |= SDL_WINDOW_MAXIMIZED;
   if (startupConfiguration.resizable)
      flags |= SDL_WINDOW_RESIZABLE;

   window = SDL_CreateWindow(title,
      sdlWindowPosition(startupConfiguration.xMode, startupConfiguration.x),
      sdlWindowPosition(startupConfiguration.yMode, startupConfiguration.y),
      startupConfiguration.width, startupConfiguration.height, flags);
   if (window == NULL)
   {
      fprintf(stderr, "SDL_CreateWindow(): %s\n", SDL_GetError());
      TCSDL_DestroyWindow(screen);
      return false;
   }
   sdlEventWindowCreated();

#if defined(WIN32) && !defined(WINCE)
   SDL_SysWMinfo windowInfo;
   SDL_VERSION(&windowInfo.version);
   if (SDL_GetWindowWMInfo(window, &windowInfo) == SDL_TRUE
      && windowInfo.subsystem == SDL_SYSWM_WINDOWS)
   {
      mainHWnd = windowInfo.info.win.window;
      sdlInstallWindowsMessageHook();
   }
#endif

   renderer = SDL_CreateRenderer(window, -1, 0);
   if (renderer == NULL)
   {
      fprintf(stderr, "SDL_CreateRenderer(): %s\n", SDL_GetError());
      TCSDL_DestroyWindow(screen);
      return false;
   }

   if (screen->extension == NULL)
      screen->extension = (ScreenSurfaceEx)xmalloc(sizeof(TScreenSurfaceEx));
   if (screen->extension == NULL)
   {
      TCSDL_DestroyWindow(screen);
      return false;
   }
   xmemzero(screen->extension, sizeof(TScreenSurfaceEx));
   SCREEN_EX(screen)->window = window;
   SCREEN_EX(screen)->renderer = renderer;

   TScreenConfiguration configuration;
   if (!TCSDL_QueryWindowMetrics(screen, &configuration))
   {
      TCSDL_DestroyWindow(screen);
      return false;
   }
   screenApplyConfiguration(screen, &configuration);
   screenConsumePendingChanges(screen);
   return true;
}

bool TCSDL_SetFullscreen(bool fullscreen)
{
   if (window == NULL)
      return false;
   return SDL_SetWindowFullscreen(window, fullscreen ? SDL_WINDOW_FULLSCREEN : 0) == 0;
}

bool TCSDL_SetWindowSize(int32 width, int32 height)
{
   if (window == NULL || width <= 0 || height <= 0)
      return false;
   SDL_SetWindowSize(window, width, height);
   return true;
}

bool TCSDL_CreateBackBuffer(ScreenSurface screen)
{
   if (screen == NULL || SCREEN_EX(screen) == NULL || renderer == NULL
      || screen->screenW <= 0 || screen->screenH <= 0)
      return false;

   if (selectedPixelFormat == SDL_PIXELFORMAT_UNKNOWN)
   {
      if (!selectPixelFormat(screen))
         return false;
   }
   else
   {
      texture = createStreamingTexture(selectedPixelFormat, screen->screenW, screen->screenH);
      if (texture == NULL)
      {
         fprintf(stderr, "SDL_CreateTexture(%s): %s\n",
            SDL_GetPixelFormatName(selectedPixelFormat), SDL_GetError());
         return false;
      }
   }

   SDL_PixelFormat *format = SDL_AllocFormat(selectedPixelFormat);
   if (format == NULL)
   {
      fprintf(stderr, "SDL_AllocFormat(%s): %s\n",
         SDL_GetPixelFormatName(selectedPixelFormat), SDL_GetError());
      SDL_DestroyTexture(texture);
      texture = NULL;
      return false;
   }
   screen->bpp = format->BitsPerPixel;
   screen->pitch = format->BytesPerPixel * screen->screenW;
   screen->pixelformat = selectedPixelFormat;
   screen->pixels = (uint8*)xmalloc((size_t)screen->pitch * screen->screenH);
   SDL_FreeFormat(format);
   if (screen->pixels == NULL)
   {
      SDL_DestroyTexture(texture);
      texture = NULL;
      return false;
   }
   SDL_SetTextureBlendMode(texture, SDL_BLENDMODE_NONE);
   SCREEN_EX(screen)->texture = texture;
   return true;
}

void TCSDL_DestroyBackBuffer(ScreenSurface screen)
{
   if (screen == NULL)
      return;
   if (SCREEN_EX(screen) != NULL && SCREEN_EX(screen)->texture != NULL)
   {
      SDL_DestroyTexture(SCREEN_EX(screen)->texture);
      SCREEN_EX(screen)->texture = NULL;
      texture = NULL;
   }
   if (screen->pixels != NULL)
   {
      xfree(screen->pixels);
      screen->pixels = NULL;
   }
   screen->pitch = 0;
}

void TCSDL_DestroyWindow(ScreenSurface screen)
{
   if (screen != NULL)
      TCSDL_DestroyBackBuffer(screen);
   if (renderer != NULL)
   {
      SDL_DestroyRenderer(renderer);
      renderer = NULL;
   }
   if (window != NULL)
   {
#if defined(WIN32) && !defined(WINCE)
      sdlRemoveWindowsMessageHook();
      mainHWnd = NULL;
#endif
      sdlEventWindowDestroying();
      SDL_DestroyWindow(window);
      window = NULL;
   }
   selectedPixelFormat = SDL_PIXELFORMAT_UNKNOWN;
   if (screen != NULL && screen->extension != NULL)
   {
      xfree(screen->extension);
      screen->extension = NULL;
   }
   if (sdlInitialized)
   {
      SDL_Quit();
      sdlInitialized = false;
   }
}

void TCSDL_UpdateTexture(int w, int h, int pitch, void *pixels)
{
   UNUSED(w)
   UNUSED(h)
   if (texture == NULL || pixels == NULL)
      return;
   SDL_UpdateTexture(texture, NULL, pixels, pitch);
   TCSDL_Present();
}

void TCSDL_GetWindowSize(ScreenSurface screen, int32 *width, int32 *height)
{
   if (width != NULL)
      *width = 0;
   if (height != NULL)
      *height = 0;
   if (screen != NULL && SCREEN_EX(screen) != NULL && SCREEN_EX(screen)->window != NULL)
      SDL_GetWindowSize(SCREEN_EX(screen)->window, width, height);
}

void TCSDL_Present()
{
   if (renderer == NULL || texture == NULL)
      return;
   SDL_RenderClear(renderer);
   SDL_RenderCopy(renderer, texture, NULL, NULL);
   SDL_RenderPresent(renderer);
}
