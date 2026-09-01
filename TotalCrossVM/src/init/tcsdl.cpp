// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcsdl.h"
#include "../event/sdl/event_sdl.h"
#if defined(WIN32) && !defined(WINCE)
#include "SDL2/SDL_syswm.h"
#endif

#include <cmath>
#include <cstdio>
#include <cstdlib>

static const Uint32 TCSDL_PIXEL_FORMAT = SDL_PIXELFORMAT_ARGB8888;
static SDL_Renderer *renderer;
static SDL_Texture *texture;
static SDL_Window *window;
static bool sdlInitialized;

static int32 environmentDimension(const char *name, int32 fallback)
{
   const char *value = getenv(name);
   if (value == NULL || *value == '\0')
      return fallback;
   char *end = NULL;
   long parsed = strtol(value, &end, 10);
   return end != value && *end == '\0' && parsed > 0 ? (int32)parsed : fallback;
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

bool TCSDL_Init(ScreenSurface screen, const char *title, bool fullScreen)
{
   SDL_DisplayMode displayMode;
   int32 width = 800;
   int32 height = 600;
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

   if (SDL_GetCurrentDisplayMode(0, &displayMode) == 0)
   {
      width = displayMode.w;
      height = displayMode.h;
   }
   width = environmentDimension("TC_WIDTH", width);
   height = environmentDimension("TC_HEIGHT", height);

   if (fullScreen)
      flags |= SDL_WINDOW_FULLSCREEN;
   if (tcSettings.resizableWindow != NULL && *tcSettings.resizableWindow)
      flags |= SDL_WINDOW_RESIZABLE;

   window = SDL_CreateWindow(title, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED,
      width, height, flags);
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

bool TCSDL_CreateBackBuffer(ScreenSurface screen)
{
   if (screen == NULL || SCREEN_EX(screen) == NULL || renderer == NULL
      || screen->screenW <= 0 || screen->screenH <= 0)
      return false;

   screen->bpp = 32;
   screen->pitch = screen->screenW * sizeof(Pixel32);
   screen->pixelformat = TCSDL_PIXEL_FORMAT;
   screen->pixels = (uint8*)xmalloc((size_t)screen->pitch * screen->screenH);
   if (screen->pixels == NULL)
      return false;

   texture = SDL_CreateTexture(renderer, TCSDL_PIXEL_FORMAT, SDL_TEXTUREACCESS_STREAMING,
      screen->screenW, screen->screenH);
   if (texture == NULL)
   {
      xfree(screen->pixels);
      screen->pixels = NULL;
      fprintf(stderr, "SDL_CreateTexture(): %s\n", SDL_GetError());
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
