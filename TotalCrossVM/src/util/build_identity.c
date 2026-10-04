// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_BUILD_ID
#define TC_BUILD_ID "unknown"
#endif

#ifndef TC_SOURCE_TREE
#define TC_SOURCE_TREE "unknown"
#endif

#ifndef TC_RUNTIME_ABI
#define TC_RUNTIME_ABI "unknown"
#endif

#if defined(_MSC_VER)
#define TC_BUILD_ID_USED __declspec(selectany)
#elif defined(__GNUC__) || defined(__clang__)
#define TC_BUILD_ID_USED __attribute__((used))
#else
#define TC_BUILD_ID_USED
#endif

TC_BUILD_ID_USED const char totalcrossBuildIdentityMarker[] =
    "TOTALCROSS_BUILD_IDENTITY_V1:" TC_BUILD_ID;

TC_BUILD_ID_USED const char totalcrossSourceTreeMarker[] =
    "TOTALCROSS_SOURCE_TREE:" TC_SOURCE_TREE;

TC_BUILD_ID_USED const char totalcrossRuntimeAbiMarker[] =
    "TOTALCROSS_RUNTIME_ABI:" TC_RUNTIME_ABI;
