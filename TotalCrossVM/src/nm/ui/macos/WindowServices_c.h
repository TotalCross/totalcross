// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Window.h"

static bool windowPlatformIsSIPShown(void)
{
   return false;
}

static void windowPlatformSetSIP(
   Context currentContext,
   int32 sipOption,
   TCObject control,
   bool numeric)
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
   UNUSED(top)
   UNUSED(left)
   UNUSED(bottom)
   UNUSED(right)
}
