// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef WINDOW_STARTUP_ABI_ASSERTS_H
#define WINDOW_STARTUP_ABI_ASSERTS_H

#include <stddef.h>

#ifdef __cplusplus
#define TC_WINDOW_STARTUP_ABI_ASSERT(condition, message) static_assert(condition, message)
#define TC_WINDOW_STARTUP_ABI_ALIGNOF(type) alignof(type)
#else
#define TC_WINDOW_STARTUP_ABI_ASSERT(condition, message) _Static_assert(condition, message)
#define TC_WINDOW_STARTUP_ABI_ALIGNOF(type) _Alignof(type)
#endif

TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(uint8) == 1, "uint8 must be one byte");

TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(TCWindowStartupOptions) == 44,
   "TCWindowStartupOptions size changed");
TC_WINDOW_STARTUP_ABI_ASSERT(TC_WINDOW_STARTUP_ABI_ALIGNOF(TCWindowStartupOptions) == 4,
   "TCWindowStartupOptions alignment changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, screenSpecified) == 0,
   "TCWindowStartupOptions.screenSpecified offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(((TCWindowStartupOptions *)0)->screenSpecified) == 1,
   "TCWindowStartupOptions.screenSpecified size changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, x) == 4,
   "TCWindowStartupOptions.x offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, y) == 8,
   "TCWindowStartupOptions.y offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, width) == 12,
   "TCWindowStartupOptions.width offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, height) == 16,
   "TCWindowStartupOptions.height offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, environmentWidth) == 20,
   "TCWindowStartupOptions.environmentWidth offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, environmentHeight) == 24,
   "TCWindowStartupOptions.environmentHeight offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, initialState) == 28,
   "TCWindowStartupOptions.initialState offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, initialFullscreen) == 32,
   "TCWindowStartupOptions.initialFullscreen offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, environmentFullscreen) == 36,
   "TCWindowStartupOptions.environmentFullscreen offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupOptions, appTczAttr) == 40,
   "TCWindowStartupOptions.appTczAttr offset changed");

TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(TCWindowStartupConfiguration) == 28,
   "TCWindowStartupConfiguration size changed");
TC_WINDOW_STARTUP_ABI_ASSERT(TC_WINDOW_STARTUP_ABI_ALIGNOF(TCWindowStartupConfiguration) == 4,
   "TCWindowStartupConfiguration alignment changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, width) == 0,
   "TCWindowStartupConfiguration.width offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, height) == 4,
   "TCWindowStartupConfiguration.height offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, xMode) == 8,
   "TCWindowStartupConfiguration.xMode offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, yMode) == 12,
   "TCWindowStartupConfiguration.yMode offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, x) == 16,
   "TCWindowStartupConfiguration.x offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, y) == 20,
   "TCWindowStartupConfiguration.y offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, fullscreen) == 24,
   "TCWindowStartupConfiguration.fullscreen offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, maximized) == 25,
   "TCWindowStartupConfiguration.maximized offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(offsetof(TCWindowStartupConfiguration, resizable) == 26,
   "TCWindowStartupConfiguration.resizable offset changed");
TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(((TCWindowStartupConfiguration *)0)->fullscreen) == 1,
   "TCWindowStartupConfiguration.fullscreen size changed");
TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(((TCWindowStartupConfiguration *)0)->maximized) == 1,
   "TCWindowStartupConfiguration.maximized size changed");
TC_WINDOW_STARTUP_ABI_ASSERT(sizeof(((TCWindowStartupConfiguration *)0)->resizable) == 1,
   "TCWindowStartupConfiguration.resizable size changed");

#undef TC_WINDOW_STARTUP_ABI_ASSERT
#undef TC_WINDOW_STARTUP_ABI_ALIGNOF

#endif
