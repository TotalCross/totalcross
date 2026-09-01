// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifdef __cplusplus
extern "C" {
#endif

static bool windowBackendSetSizeImpl(int32 width, int32 height)
{
   UNUSED(width)
   UNUSED(height)
   return false;
}

static void windowBackendSetDeviceTitle(TCObject titleObj)
{
   UNUSED(titleObj)
}

#ifdef __cplusplus
}
#endif
