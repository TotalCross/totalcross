// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_COMPILED_H
#define TCIR_COMPILED_H

#include "tcir_interp.h"

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define TC_RUNTIME_ABI_VERSION 1U

typedef struct TCCompiledFrame
{
   int32_t *i32_homes;
   size_t i32_home_count;
   void **ref_homes;
   size_t ref_home_count;
   TCIRV64Home *v64_homes;
   size_t v64_home_count;
   const TCIRRuntimeValue *arguments;
   size_t argument_count;
   unsigned int tc_pc;
   int32_t *scratch_i32_values;
   size_t scratch_i32_count;
   int32_t *edge_i32_values;
   size_t edge_i32_count;
} TCCompiledFrame;

typedef enum TCCompiledStatus
{
   TC_COMPILED_RETURNED = 0,
   TC_COMPILED_THROWN,
   TC_COMPILED_REJECTED,
   TC_COMPILED_OUT_OF_MEMORY
} TCCompiledStatus;

typedef struct TCCompiledResult
{
   TCCompiledStatus status;
   TCIRType type;
   TCIRRuntimeValue value;
   unsigned int tc_pc;
} TCCompiledResult;

typedef TCCompiledStatus (*TCCompiledEntry)(TCCompiledFrame *frame, TCCompiledResult *result);

#ifdef __cplusplus
}
#endif

#endif
