// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_JIT_H
#define TCIR_JIT_H

#include "tcir_interp.h"

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define TC_RUNTIME_ABI_VERSION 1U

typedef struct TCIRJitArtifact TCIRJitArtifact;
typedef struct TCIRJitCache TCIRJitCache;
typedef struct TCIRJitClaim TCIRJitClaim;

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

typedef enum TCIRJitCompileStatus
{
   TCIR_JIT_COMPILE_READY = 0,
   TCIR_JIT_COMPILE_VERIFICATION_FAILED,
   TCIR_JIT_COMPILE_INELIGIBLE,
   TCIR_JIT_COMPILE_OUT_OF_MEMORY,
   TCIR_JIT_COMPILE_EMISSION_FAILED
} TCIRJitCompileStatus;

typedef enum TCIRJitDiagnosticCode
{
   TCIR_JIT_DIAGNOSTIC_NONE = 0,
   TCIR_JIT_DIAGNOSTIC_INVALID_ARGUMENT,
   TCIR_JIT_DIAGNOSTIC_VERIFICATION_FAILED,
   TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE,
   TCIR_JIT_DIAGNOSTIC_INELIGIBLE_OPERATION,
   TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TERMINATOR,
   TCIR_JIT_DIAGNOSTIC_OUT_OF_MEMORY,
   TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED,
   TCIR_JIT_DIAGNOSTIC_NOT_READY,
   TCIR_JIT_DIAGNOSTIC_SHUTDOWN
} TCIRJitDiagnosticCode;

typedef struct TCIRJitDiagnostic
{
   TCIRJitDiagnosticCode code;
   unsigned int tc_pc;
   TCIRDiagnostic verifier;
   char message[256];
} TCIRJitDiagnostic;

typedef struct TCIRJitCompileOptions
{
   size_t emission_limit;
} TCIRJitCompileOptions;

typedef enum TCIRJitMemoryPolicy
{
   TCIR_JIT_MEMORY_WX = 0
} TCIRJitMemoryPolicy;

typedef enum TCIRJitCacheStatus
{
   TCIR_JIT_CACHE_READY = 0,
   TCIR_JIT_CACHE_CLAIMED,
   TCIR_JIT_CACHE_COMPILING,
   TCIR_JIT_CACHE_REJECTED,
   TCIR_JIT_CACHE_OUT_OF_MEMORY,
   TCIR_JIT_CACHE_SHUTDOWN,
   TCIR_JIT_CACHE_INVALID_ARGUMENT
} TCIRJitCacheStatus;

void tcirJitDiagnosticClear(TCIRJitDiagnostic *diagnostic);
const char *tcirJitDiagnosticCodeName(TCIRJitDiagnosticCode code);
const char *tcirJitPlatformName(void);

TCIRJitCompileStatus tcirJitCheckEligibility(
   const TCIRFunction *function,
   TCIRJitDiagnostic *diagnostic);
TCIRJitCompileStatus tcirJitCompile(
   const TCIRFunction *function,
   const TCIRJitCompileOptions *options,
   TCIRJitArtifact **artifact,
   TCIRJitDiagnostic *diagnostic);
void tcirJitArtifactDestroy(TCIRJitArtifact *artifact);
size_t tcirJitArtifactCodeSize(const TCIRJitArtifact *artifact);
const void *tcirJitArtifactCodeAddress(const TCIRJitArtifact *artifact);
TCIRJitMemoryPolicy tcirJitArtifactMemoryPolicy(const TCIRJitArtifact *artifact);
TCCompiledStatus tcirJitInvoke(
   const TCIRJitArtifact *artifact,
   TCCompiledFrame *frame,
   TCCompiledResult *result,
   TCIRJitDiagnostic *diagnostic);

TCIRJitCache *tcirJitCacheCreate(void);
void tcirJitCacheDestroy(TCIRJitCache *cache);
TCIRJitCacheStatus tcirJitCacheBegin(
   TCIRJitCache *cache,
   const void *method_key,
   const TCIRJitArtifact **artifact,
   TCIRJitClaim **claim,
   TCIRJitDiagnostic *diagnostic);
int tcirJitCachePublish(TCIRJitClaim *claim, TCIRJitArtifact *artifact);
void tcirJitCacheReject(TCIRJitClaim *claim);

#ifdef __cplusplus
}
#endif

#endif
