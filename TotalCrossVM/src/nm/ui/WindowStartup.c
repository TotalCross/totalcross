// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "WindowStartup.h"
#include "../../tcvm/tcvm.h"

#include <stdlib.h>

static int32 environmentDimension(const char *name)
{
   const char *value = getenv(name);
   char *end = null;
   long parsed;

   if (value == null || *value == '\0')
      return -1;
   parsed = strtol(value, &end, 10);
   return end != value && *end == '\0' && parsed > 0 ? (int32)parsed : -1;
}

void windowResetCommandLineOptions(TCWindowStartupOptions *options)
{
   if (options == null)
      return;
   options->screenSpecified = false;
   options->x = -1;
   options->y = -1;
   options->width = -1;
   options->height = -1;
   options->initialState = TC_INITIAL_WINDOW_NORMAL;
}

void windowLoadStartupEnvironment(TCWindowStartupOptions *options)
{
   if (options == null)
      return;
   options->environmentWidth = -1;
   options->environmentHeight = -1;
#if TC_OS_DESKTOP
   options->environmentWidth = environmentDimension("TC_WIDTH");
   options->environmentHeight = environmentDimension("TC_HEIGHT");
#endif
}

static bool hasTczSize(int16 appTczAttr)
{
   return (appTczAttr & (ATTR_WINDOWSIZE_320X480
      | ATTR_WINDOWSIZE_480X640 | ATTR_WINDOWSIZE_600X800)) != 0;
}

static void resolveTczSize(int16 appTczAttr, const TCDisplayMetrics *display,
   int32 *width, int32 *height)
{
   if (appTczAttr & ATTR_WINDOWSIZE_320X480)
   {
      *width = 320;
      *height = 480;
   }
   else if (appTczAttr & ATTR_WINDOWSIZE_480X640)
   {
      *width = 480;
      *height = 640;
   }
   else if (appTczAttr & ATTR_WINDOWSIZE_600X800)
   {
      *width = 600;
      *height = display->usableHeight > 0 && display->usableHeight < 800
         ? display->usableHeight : 800;
   }
   else
   {
      *width = -1;
      *height = -1;
   }
}

static TCWindowPositionMode resolvePositionMode(int32 position)
{
   return position == -2 ? TC_WINDOW_POSITION_CENTER
      : position >= 0 ? TC_WINDOW_POSITION_EXPLICIT
      : TC_WINDOW_POSITION_DEFAULT;
}

bool windowResolveStartupConfiguration(
   const TCWindowStartupOptions *options,
   const TCDisplayMetrics *display,
   TCWindowStartupConfiguration *configuration)
{
   int32 tczWidth;
   int32 tczHeight;
   int32 defaultWidth;
   int32 defaultHeight;
   bool explicitSizeSource;
   bool tczSizeSource;
   bool tczSizeUsed;

   if (options == null || display == null || configuration == null
      || display->width <= 0 || display->height <= 0)
      return false;

   resolveTczSize(options->appTczAttr, display, &tczWidth, &tczHeight);
   tczSizeSource = hasTczSize(options->appTczAttr);
   explicitSizeSource = options->screenSpecified
      || options->environmentWidth > 0 || options->environmentHeight > 0
      || tczSizeSource;
   defaultWidth = display->width / 2;
   defaultHeight = display->height / 2;
   configuration->fullscreen = options->initialState == TC_INITIAL_WINDOW_FULLSCREEN
      || (options->initialState == TC_INITIAL_WINDOW_NORMAL
         && options->legacyFullscreen);
   configuration->maximized = !configuration->fullscreen
      && options->initialState == TC_INITIAL_WINDOW_MAXIMIZED;
   configuration->resizable = (options->appTczAttr & ATTR_RESIZABLE_WINDOW) != 0
      && !configuration->fullscreen;

   if (configuration->fullscreen && !explicitSizeSource)
   {
      defaultWidth = display->width;
      defaultHeight = display->height;
   }

   if (options->screenSpecified)
   {
      configuration->width = options->width > 0 ? options->width : display->width / 2;
      configuration->height = options->height > 0 ? options->height : display->height / 2;
      configuration->xMode = resolvePositionMode(options->x);
      configuration->yMode = resolvePositionMode(options->y);
      configuration->x = options->x;
      configuration->y = options->y;
   }
   else
   {
      configuration->width = options->environmentWidth > 0 ? options->environmentWidth
         : tczWidth > 0 ? tczWidth : defaultWidth;
      configuration->height = options->environmentHeight > 0 ? options->environmentHeight
         : tczHeight > 0 ? tczHeight : defaultHeight;
      tczSizeUsed = (options->environmentWidth <= 0 && tczWidth > 0)
         || (options->environmentHeight <= 0 && tczHeight > 0);
      configuration->xMode = tczSizeUsed ? TC_WINDOW_POSITION_CENTER
         : TC_WINDOW_POSITION_DEFAULT;
      configuration->yMode = tczSizeUsed ? TC_WINDOW_POSITION_CENTER
         : TC_WINDOW_POSITION_DEFAULT;
      configuration->x = 0;
      configuration->y = 0;
   }

   return configuration->width > 0 && configuration->height > 0;
}
