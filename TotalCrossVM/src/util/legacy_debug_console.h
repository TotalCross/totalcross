// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef LEGACY_DEBUG_CONSOLE_H
#define LEGACY_DEBUG_CONSOLE_H

#include "xtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

bool legacyDebugConsoleInit();
void legacyDebugConsoleShutdown();
void legacyDebugConsoleClose();
bool legacyDebugConsoleIsOpen();
bool legacyDebugConsoleWrite(const uint8 *bytes, int32 length, bool durable);
bool legacyDebugConsoleFlush(bool durable);
bool legacyDebugConsoleDebugLine(const char *line, const char *lineEnding, bool durable);
bool legacyDebugConsoleErase();
void legacyDebugConsoleDestroy(const char *separator);

#ifdef __cplusplus
}
#endif

#endif
