// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_ANDROID_SKIA_EGL_LIFECYCLE_H
#define TC_ANDROID_SKIA_EGL_LIFECYCLE_H

typedef struct TAndroidSkiaEglCleanupOps
{
   void *userData;
   int (*contextIsValid)(void *userData);
   int (*contextIsCurrent)(void *userData);
   int (*makeContextCurrent)(void *userData);
} TAndroidSkiaEglCleanupOps;

/* A non-current context is still usable if it validates and can be rebound. */
static inline int androidSkiaEglContextReadyForCleanup(
      const TAndroidSkiaEglCleanupOps *ops)
{
   if (ops == 0 || ops->contextIsValid == 0
         || ops->contextIsCurrent == 0 || ops->makeContextCurrent == 0
         || !ops->contextIsValid(ops->userData))
      return 0;

   if (ops->contextIsCurrent(ops->userData))
      return 1;

   return ops->makeContextCurrent(ops->userData)
         && ops->contextIsCurrent(ops->userData);
}

#endif
