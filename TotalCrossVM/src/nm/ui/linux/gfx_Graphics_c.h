// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "gfx_ex.h"

void privateScreenChange(int32 w, int32 h)
{
   UNUSED(w)
   UNUSED(h)
}

#include "../../init/tcsdl.h"
#ifdef SKIA_H
#include "../android/skia.h"
#endif


bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
#ifdef __arm__
   *(tcSettings.isFullScreenPtr)=true;
#endif
#ifdef SKIA_H
   TCSDL_Init(screen, exeName, *(tcSettings.isFullScreenPtr));
#elif !defined HEADLESS
   DFBResult err;
   IDirectFB *_dfb;
   IDirectFBSurface *_primary;
   IDirectFBDisplayLayer *_layer;

   screen->extension = ((ScreenSurfaceEx)deviceCtx);

   if (SCREEN_EX(screen)->layer) return true; // already initialized

   _dfb = SCREEN_EX(screen)->dfb;

   err = _dfb->GetDisplayLayer(_dfb, DLID_PRIMARY, &SCREEN_EX(screen)->layer);
   if (err != DFB_OK) return false;

   _layer = SCREEN_EX(screen)->layer;

   err = _layer->SetCooperativeLevel(_layer, DLSCL_ADMINISTRATIVE);
   if (err != DFB_OK) return false;

   err = _layer->GetSurface(_layer, &SCREEN_EX(screen)->primary);
   if (err != DFB_OK) return false;

   _primary = SCREEN_EX(screen)->primary;
   _primary->SetBlittingFlags(_primary, DSBLIT_BLEND_ALPHACHANNEL);
   _primary->SetDrawingFlags(_primary, DSDRAW_BLEND | DSDRAW_SRC_PREMULTIPLY);

   screen->pixels = (uint8*)1; // fake initialize, will be set on each lock

   DFBSurfacePixelFormat format;
   err = _primary->GetPixelFormat (_primary, &format);
   if (err != DFB_OK) return false;

   screen->bpp = DFB_BITS_PER_PIXEL(format);

   int w, h;
   err = _primary->GetSize (_primary, &w, &h);
   if (err != DFB_OK) return false;

   screen->screenW = w;
   screen->screenH = h;
#else
   TCSDL_Init(screen, exeName, *(tcSettings.isFullScreenPtr));
#endif
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
#ifdef SKIA_H
   initSkia(screen->screenW, screen->screenH, screen->pixels, (int)screen->pitch, screen->pixelformat);
#endif
   return true;
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen) // screen's already locked
{            
#ifdef SKIA_H
   flushSkia();
#elif !defined HEADLESS
   DFBRegion bounds;
   bounds.x1 = currentContext->dirtyX1;
   bounds.y1 = currentContext->dirtyY1;
   bounds.x2 = currentContext->dirtyX2;
   bounds.y2 = currentContext->dirtyY2;
   SCREEN_EX(screen)->primary->Flip(SCREEN_EX(screen)->primary, &bounds, DSFLIP_ONSYNC);
#else
   TCSDL_UpdateTexture(screen->screenW, screen->screenH, screen->pitch, screen->pixels);
#endif
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
#ifndef HEADLESS
   if (SCREEN_EX(screen)->layer)
      SCREEN_EX(screen)->layer->Release (SCREEN_EX(screen)->layer);
   if (SCREEN_EX(screen)->primary)
      SCREEN_EX(screen)->primary->Release (SCREEN_EX(screen)->primary);
#else
   TCSDL_Destroy(screen);
#endif
}

bool graphicsLock(ScreenSurface screen, bool on)
{
#ifndef HEADLESS
   IDirectFBSurface *surf = SCREEN_EX(screen)->primary;
   IDirectFBDisplayLayer *_layer = SCREEN_EX(screen)->layer;
   if (on)
   {
      _layer->EnableCursor(_layer, 0);
      return (surf->Lock(surf, DSLF_READ|DSLF_WRITE, (void **)&screen->pixels, &screen->pitch) == DFB_OK);
   }
   else
   {
      bool ok = (surf->Unlock(surf) == DFB_OK);
      _layer->EnableCursor(_layer, 1);
      return ok;
   }
#else
    return true;
#endif
}
