// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_ANDROID_SKIA_EGL_LIFECYCLE_DIAGNOSTICS_H
#define TC_ANDROID_SKIA_EGL_LIFECYCLE_DIAGNOSTICS_H

#include <stdint.h>

#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
#ifdef __cplusplus
extern "C" {
#endif

void tcAndroidEglLifecycleLogNative(const char *event, uint32_t generation,
      const void *window, const void *candidateWindow,
      const void *display, const void *surface, const void *expectedContext,
      uint32_t flags);
void tcAndroidEglLifecycleLogSkia(const char *event,
      const void *context, const void *surface, const void *canvas,
      uint32_t flags);

#ifdef __cplusplus
}
#endif

#define TC_ANDROID_EGL_DIAG_NATIVE(...) \
   tcAndroidEglLifecycleLogNative(__VA_ARGS__)
#define TC_ANDROID_EGL_DIAG_SKIA(...) \
   tcAndroidEglLifecycleLogSkia(__VA_ARGS__)
#else
#define TC_ANDROID_EGL_DIAG_NATIVE(...) ((void)0)
#define TC_ANDROID_EGL_DIAG_SKIA(...) ((void)0)
#endif

#endif
