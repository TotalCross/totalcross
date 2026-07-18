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

#define TC_RUNTIME_ABI_VERSION 3U

typedef struct TCCompiledResult TCCompiledResult;

typedef enum TCCompiledStatus
{
   TC_COMPILED_RETURNED = 0,
   TC_COMPILED_THROWN,
   TC_COMPILED_REJECTED,
   TC_COMPILED_OUT_OF_MEMORY
} TCCompiledStatus;

typedef TCCompiledStatus (*TCCompiledDispatchThunk)(
   void *runtime_context,
   const void *method_key,
   void *receiver,
   const TCIRRuntimeValue *arguments,
   size_t argument_count,
   TCCompiledResult *result);

typedef struct TCCompiledRuntime
{
   unsigned int abi_version;
   void *context;
   TCCompiledDispatchThunk dispatch;
} TCCompiledRuntime;

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
   TCIRRuntimeValue *scratch_values;
   size_t scratch_count;
   TCIRRuntimeValue *edge_values;
   size_t edge_count;
   TCIRRuntimeValue jit_return_value;
   const TCCompiledRuntime *runtime;
} TCCompiledFrame;

struct TCCompiledResult
{
   TCCompiledStatus status;
   TCIRType type;
   TCIRRuntimeValue value;
   unsigned int tc_pc;
};

typedef TCCompiledStatus (*TCCompiledEntry)(TCCompiledFrame *frame, TCCompiledResult *result);

#ifdef __cplusplus
}
#endif

#endif
