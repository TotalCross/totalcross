// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_PLATFORM_H
#define TC_PLATFORM_H

/*
 * TotalCross normalized target detection.
 *
 * All public macros in this header are always defined as either 0 or 1.
 * Therefore, use:
 *
 *     #if TC_OS_ANDROID
 *
 * and not:
 *
 *     #ifdef TC_OS_ANDROID
 */

#if defined(TC_PLATFORM_CONFIGURED)

#include "tc_platform_config.h"

#define TC_OS_ANDROID TC_CONFIG_OS_ANDROID
#define TC_OS_IOS TC_CONFIG_OS_IOS
#define TC_OS_MACOS TC_CONFIG_OS_MACOS
#define TC_OS_WINDOWS TC_CONFIG_OS_WINDOWS
#define TC_OS_WINCE TC_CONFIG_OS_WINCE
#define TC_OS_LINUX TC_CONFIG_OS_LINUX

#define TC_OS_APPLE TC_CONFIG_OS_APPLE
#define TC_OS_WINDOWS_FAMILY TC_CONFIG_OS_WINDOWS_FAMILY
#define TC_OS_MOBILE TC_CONFIG_OS_MOBILE
#define TC_OS_DESKTOP TC_CONFIG_OS_DESKTOP

#define TC_OS_NAME TC_CONFIG_OS_NAME

#else

/*
 * Fallback for non-CMake builds.
 *
 * Detection order matters:
 *
 * - Android toolchains also identify themselves as Linux.
 * - Windows CE may also define _WIN32.
 * - Apple targets share __APPLE__ and must be separated with
 *   TargetConditionals.h.
 */

#if defined(__APPLE__)
#include <TargetConditionals.h>
#endif

/* ------------------------------------------------------------------------- */
/* Operating system                                                          */
/* ------------------------------------------------------------------------- */

#define TC_OS_ANDROID       0
#define TC_OS_IOS           0
#define TC_OS_MACOS         0
#define TC_OS_MACCATALYST   0
#define TC_OS_WINDOWS       0
#define TC_OS_WINCE         0
#define TC_OS_LINUX         0
#define TC_OS_UNKNOWN       0


#if defined(__ANDROID__) || defined(ANDROID)

#undef TC_OS_ANDROID
#define TC_OS_ANDROID 1
#define TC_OS_NAME "android"

#elif defined(_WIN32_WCE) || defined(WINCE)

#undef TC_OS_WINCE
#define TC_OS_WINCE 1
#define TC_OS_NAME "wince"

#elif defined(__APPLE__) \
   && defined(TARGET_OS_MACCATALYST) \
   && TARGET_OS_MACCATALYST

#undef TC_OS_MACCATALYST
#define TC_OS_MACCATALYST 1
#define TC_OS_NAME "maccatalyst"

#elif defined(__APPLE__) \
   && defined(TARGET_OS_IOS) \
   && TARGET_OS_IOS

#undef TC_OS_IOS
#define TC_OS_IOS 1
#define TC_OS_NAME "ios"

#elif defined(__APPLE__) \
   && defined(TARGET_OS_OSX) \
   && TARGET_OS_OSX

#undef TC_OS_MACOS
#define TC_OS_MACOS 1
#define TC_OS_NAME "macos"

#elif defined(_WIN32)

#undef TC_OS_WINDOWS
#define TC_OS_WINDOWS 1
#define TC_OS_NAME "windows"

#elif defined(__linux__) || \
      (defined(linux) && !defined(__APPLE__))

#undef TC_OS_LINUX
#define TC_OS_LINUX 1
#define TC_OS_NAME "linux"

#else

#undef TC_OS_UNKNOWN
#define TC_OS_UNKNOWN 1
#define TC_OS_NAME "unknown"

#endif

/* ------------------------------------------------------------------------- */
/* Apple target environment                                                  */
/* ------------------------------------------------------------------------- */

#define TC_ENV_SIMULATOR    0
#define TC_ENV_DEVICE       0
#define TC_ENV_MACCATALYST  0

#if defined(__APPLE__) \
    && defined(TARGET_OS_SIMULATOR) \
    && TARGET_OS_SIMULATOR

#undef TC_ENV_SIMULATOR
#define TC_ENV_SIMULATOR 1

#elif TC_OS_IOS

#undef TC_ENV_DEVICE
#define TC_ENV_DEVICE 1

#endif

#if TC_OS_MACCATALYST

#undef TC_ENV_MACCATALYST
#define TC_ENV_MACCATALYST 1

#endif

/* ------------------------------------------------------------------------- */
/* CPU architecture                                                          */
/* ------------------------------------------------------------------------- */

#define TC_ARCH_ARM32       0
#define TC_ARCH_ARM64       0
#define TC_ARCH_X86         0
#define TC_ARCH_X86_64      0
#define TC_ARCH_UNKNOWN     0

/*
 * ABI refinements. These do not represent separate CPU architectures.
 */
#define TC_ABI_ARM64EC      0

/*
 * ARM64EC must be checked before x86_64 because MSVC may define x64-related
 * macros for ARM64EC builds.
 */
#if defined(_M_ARM64EC) || defined(__arm64ec__)

#undef TC_ARCH_ARM64
#define TC_ARCH_ARM64 1

#undef TC_ABI_ARM64EC
#define TC_ABI_ARM64EC 1

#elif defined(__aarch64__) \
   || defined(__arm64__) \
   || defined(_M_ARM64) \
   || (defined(TARGET_CPU_ARM64) && TARGET_CPU_ARM64)

#undef TC_ARCH_ARM64
#define TC_ARCH_ARM64 1

#elif defined(__arm__) \
   || defined(_M_ARM) \
   || (defined(TARGET_CPU_ARM) && TARGET_CPU_ARM)

#undef TC_ARCH_ARM32
#define TC_ARCH_ARM32 1

#elif defined(__x86_64__) \
   || defined(__amd64__) \
   || defined(_M_X64) \
   || defined(_M_AMD64) \
   || (defined(TARGET_CPU_X86_64) && TARGET_CPU_X86_64)

#undef TC_ARCH_X86_64
#define TC_ARCH_X86_64 1

#elif defined(__i386__) \
   || defined(_M_IX86) \
   || (defined(TARGET_CPU_X86) && TARGET_CPU_X86)

#undef TC_ARCH_X86
#define TC_ARCH_X86 1

#else

#undef TC_ARCH_UNKNOWN
#define TC_ARCH_UNKNOWN 1

#endif

/* ------------------------------------------------------------------------- */
/* ARM architecture refinements                                              */
/* ------------------------------------------------------------------------- */

#define TC_ARCH_ARMV7 0

#if TC_ARCH_ARM32 \
    && (defined(__ARM_ARCH_7A__) \
        || defined(__ARM_ARCH_7R__) \
        || defined(__ARM_ARCH_7S__) \
        || (defined(__ARM_ARCH) && __ARM_ARCH == 7) \
        || (defined(_M_ARM) && _M_ARM == 7))

#undef TC_ARCH_ARMV7
#define TC_ARCH_ARMV7 1

#endif

/* ------------------------------------------------------------------------- */
/* Derived families                                                          */
/* ------------------------------------------------------------------------- */

#define TC_OS_APPLE \
   (TC_OS_IOS || TC_OS_MACOS || TC_OS_MACCATALYST)

#define TC_OS_WINDOWS_FAMILY \
   (TC_OS_WINDOWS || TC_OS_WINCE)

#define TC_OS_MOBILE \
   (TC_OS_ANDROID || TC_OS_IOS || TC_OS_WINCE)

#define TC_OS_DESKTOP \
   (TC_OS_WINDOWS || TC_OS_LINUX || TC_OS_MACOS)

#define TC_ARCH_ARM_FAMILY \
   (TC_ARCH_ARM32 || TC_ARCH_ARM64)

#define TC_ARCH_X86_FAMILY \
   (TC_ARCH_X86 || TC_ARCH_X86_64)

#define TC_ARCH_32BIT \
   (TC_ARCH_ARM32 || TC_ARCH_X86)

#define TC_ARCH_64BIT \
   (TC_ARCH_ARM64 || TC_ARCH_X86_64)

#endif /* TC_PLATFORM_CONFIGURED */
#endif /* TC_PLATFORM_H */
