// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_RUNTIME_H
#define TCIR_RUNTIME_H

#include "tcir_compiled.h"
#include "tcir_frontend.h"
#include "tcapi.h"
#include "tcclass.h"

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef enum TCIRRuntimeBackend
{
   TCIR_RUNTIME_BACKEND_OFF = 0,
   TCIR_RUNTIME_BACKEND_IR,
   TCIR_RUNTIME_BACKEND_JIT,
   TCIR_RUNTIME_BACKEND_AOT,
   TCIR_RUNTIME_BACKEND_AUTO
} TCIRRuntimeBackend;

typedef enum TCIRRuntimeFallbackReason
{
   TCIR_RUNTIME_FALLBACK_NONE = 0,
   TCIR_RUNTIME_FALLBACK_DISABLED,
   TCIR_RUNTIME_FALLBACK_UNREGISTERED,
   TCIR_RUNTIME_FALLBACK_FORCED_OTHER_METHOD,
   TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
   TCIR_RUNTIME_FALLBACK_BACKEND_UNAVAILABLE,
   TCIR_RUNTIME_FALLBACK_BACKEND_COMPILING,
   TCIR_RUNTIME_FALLBACK_BACKEND_REJECTED,
   TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED,
   TCIR_RUNTIME_FALLBACK_SHUTDOWN,
   TCIR_RUNTIME_FALLBACK_COUNT
} TCIRRuntimeFallbackReason;

typedef enum TCIRRuntimeRegistrationStatus
{
   TCIR_RUNTIME_REGISTRATION_READY = 0,
   TCIR_RUNTIME_REGISTRATION_FALLBACK,
   TCIR_RUNTIME_REGISTRATION_ERROR
} TCIRRuntimeRegistrationStatus;

typedef enum TCIRRuntimeDispatchStatus
{
   TCIR_RUNTIME_DISPATCH_FALLBACK = 0,
   TCIR_RUNTIME_DISPATCH_RETURNED,
   TCIR_RUNTIME_DISPATCH_THROWN,
   TCIR_RUNTIME_DISPATCH_OUT_OF_MEMORY
} TCIRRuntimeDispatchStatus;

typedef struct TCIRRuntimeDiagnostic
{
   TCIRRuntimeBackend backend;
   TCIRRuntimeFallbackReason fallback_reason;
   unsigned int tc_pc;
   TCIRDiagnostic ir;
   char method[128];
   char message[256];
} TCIRRuntimeDiagnostic;

typedef struct TCIRRuntimeStats
{
   uint64_t methods_registered;
   uint64_t registration_fallbacks;
   uint64_t dispatch_attempts;
   uint64_t dispatch_returns;
   uint64_t dispatch_throws;
   uint64_t ir_invocations;
   uint64_t jit_invocations;
   uint64_t aot_invocations;
   uint64_t jit_compilations;
   uint64_t jit_compile_failures;
   uint64_t jit_compile_nanoseconds;
   uint64_t jit_code_bytes;
   uint64_t call_thunks;
   uint64_t allocation_thunks;
   uint64_t forced_failures;
   uint64_t fallback_counts[TCIR_RUNTIME_FALLBACK_COUNT];
} TCIRRuntimeStats;

TC_API void tcirRuntimeDiagnosticClear(TCIRRuntimeDiagnostic *diagnostic);
TC_API const char *tcirRuntimeBackendName(TCIRRuntimeBackend backend);
TC_API const char *tcirRuntimeFallbackReasonName(TCIRRuntimeFallbackReason reason);

TC_API int tcirRuntimeBackendAvailable(TCIRRuntimeBackend backend);
TC_API int tcirRuntimeSetBackend(TCIRRuntimeBackend backend);
TC_API TCIRRuntimeBackend tcirRuntimeGetBackend(void);
TC_API void tcirRuntimeSetForcedMethod(Method method);

TC_API TCIRRuntimeRegistrationStatus tcirRuntimeRegisterMethod(
   Method method,
   const TCIRMethodView *view,
   TCCompiledEntry aot_entry,
   const char *aot_content_hash,
   TCIRRuntimeDiagnostic *diagnostic);

/* A NULL diagnostic enables the lock-free backend-off fast path used by executeMethod. */
TC_API TCIRRuntimeDispatchStatus tcirRuntimeTryDispatch(
   Context context,
   Method method,
   Int32Array i32_homes,
   TCObjectArray ref_homes,
   Value64Array v64_homes,
   TCCompiledResult *result,
   TCIRRuntimeDiagnostic *diagnostic);

TC_API int tcirRuntimeWriteIr(Method method, const char *path, TCIRRuntimeDiagnostic *diagnostic);
TC_API void tcirRuntimeGetStats(TCIRRuntimeStats *stats);
TC_API void tcirRuntimeShutdown(void);
TC_API int tcirRuntimeReset(void);

#ifdef __cplusplus
}
#endif

#endif
