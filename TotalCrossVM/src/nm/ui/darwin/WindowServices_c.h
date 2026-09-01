// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifdef __cplusplus
extern "C" {
#endif

void windowSetSIP(Context currentContext, int32 sipOption, TCObject control, bool numeric);
bool windowGetSIP(void);
void windowGetSafeAreaInsets(int32 *top, int32 *left, int32 *bottom, int32 *right);

#ifdef __cplusplus
}
#endif

static bool windowPlatformIsSIPShown(void)
{
   return windowGetSIP();
}

static void windowPlatformSetSIP(
   Context currentContext,
   int32 sipOption,
   TCObject control,
   bool numeric)
{
   windowSetSIP(currentContext, sipOption, control, numeric);
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
   windowGetSafeAreaInsets(top, left, bottom, right);
}
