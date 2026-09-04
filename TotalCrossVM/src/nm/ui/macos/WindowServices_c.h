// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Window.h"

static int32 windowPlatformIsSIPShown(void)
{
   return 0;
}

static void windowPlatformSetSIP(
   Context currentContext,
   int32 sipOption,
   TCObject control,
   int32 numeric)
{
   UNUSED(currentContext)
   UNUSED(sipOption)
   UNUSED(control)
   UNUSED(numeric)
}

static void windowPlatformSetOrientation(int32 orientation)
{
   UNUSED(orientation)
}

static void windowPlatformGetSafeAreaInsets(
   int32 *top,
   int32 *left,
   int32 *bottom,
   int32 *right)
{
   *top = *left = *bottom = *right = 0;
}
