// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef WINDOW_STARTUP_H
#define WINDOW_STARTUP_H

#include "../../util/xtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum TCInitialWindowState
{
   TC_INITIAL_WINDOW_NORMAL,
   TC_INITIAL_WINDOW_FULLSCREEN,
   TC_INITIAL_WINDOW_MAXIMIZED
} TCInitialWindowState;

typedef enum
{
   TC_WINDOW_POSITION_DEFAULT,
   TC_WINDOW_POSITION_CENTER,
   TC_WINDOW_POSITION_EXPLICIT
} TCWindowPositionMode;

typedef enum
{
   TC_FULLSCREEN_UNSET,
   TC_FULLSCREEN_FALSE,
   TC_FULLSCREEN_TRUE
} TCFullscreenSetting;

typedef enum
{
   TC_WINDOW_PLATFORM_LINUX_ARM,
   TC_WINDOW_PLATFORM_LINUX_X86,
   TC_WINDOW_PLATFORM_WINDOWS,
   TC_WINDOW_PLATFORM_MACOS
} TCWindowStartupPlatform;

typedef struct
{
   bool screenSpecified;
   int32 x;
   int32 y;
   int32 width;
   int32 height;

   int32 environmentWidth;
   int32 environmentHeight;

   TCInitialWindowState initialState;
   TCFullscreenSetting initialFullscreen;     // Settings.isFullScreen before app startup
   TCFullscreenSetting environmentFullscreen; // TC_FULLSCREEN override
   int16 appTczAttr;
} TCWindowStartupOptions;

typedef struct
{
   int32 width;
   int32 height;
   int32 usableWidth;
   int32 usableHeight;
} TCDisplayMetrics;

typedef struct
{
   int32 width;
   int32 height;

   TCWindowPositionMode xMode;
   TCWindowPositionMode yMode;
   int32 x;
   int32 y;

   bool fullscreen;
   bool maximized;
   bool resizable;
} TCWindowStartupConfiguration;

void windowResetCommandLineOptions(TCWindowStartupOptions *options);
void windowLoadStartupEnvironment(TCWindowStartupOptions *options);
TCFullscreenSetting windowParseFullscreenEnvironment(const char *value);
bool windowResolveFullscreen(
   const TCWindowStartupOptions *options,
   TCFullscreenSetting settingsFullscreen,
   bool *fullscreen);
bool windowResolveStartupConfiguration(
   const TCWindowStartupOptions *options,
   const TCDisplayMetrics *display,
   TCWindowStartupConfiguration *configuration);
bool windowResolveStartupConfigurationForPlatform(
   const TCWindowStartupOptions *options,
   const TCDisplayMetrics *display,
   TCWindowStartupPlatform platform,
   TCWindowStartupConfiguration *configuration);

#ifdef __cplusplus
}
#endif

#endif
