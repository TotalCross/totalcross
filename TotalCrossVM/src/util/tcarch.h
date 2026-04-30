// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCARCH_H
#define TCARCH_H

#if defined(__x86_64__) || defined(__LP64__) // 1st: GCC, 2nd: XCODE
 #define TBITS 64
 #define TSIZE 8
 #define TSHIFT 3
#else
 #define TBITS 32
 #define TSIZE 4
 #define TSHIFT 2
#endif

#endif
