// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_runtime.h"

#include "tcvm.h"

#if defined(TCIR_RUNTIME_HAS_SLJIT)
#include "tcir_jit.h"
#endif

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#if defined(_WIN32)
#include <windows.h>
typedef CRITICAL_SECTION TCIRRuntimeMutex;
static INIT_ONCE tcir_runtime_once = INIT_ONCE_STATIC_INIT;
#else
#include <pthread.h>
typedef pthread_mutex_t TCIRRuntimeMutex;
static pthread_once_t tcir_runtime_once = PTHREAD_ONCE_INIT;
#endif

typedef enum TCIRRuntimeJitState
{
   TCIR_RUNTIME_JIT_UNTRIED = 0,
   TCIR_RUNTIME_JIT_COMPILING,
   TCIR_RUNTIME_JIT_READY,
   TCIR_RUNTIME_JIT_REJECTED
} TCIRRuntimeJitState;

typedef struct TCIRRuntimeEntry
{
   Method method;
   TCIRModule *module;
   TCIRFunction *function;
   TCCompiledEntry aot_entry;
   char aot_content_hash[17];
   TCIRRuntimeJitState jit_state;
#if defined(TCIR_RUNTIME_HAS_SLJIT)
   TCIRJitArtifact *jit_artifact;
#endif
   struct TCIRRuntimeEntry *next;
} TCIRRuntimeEntry;

typedef struct TCIRRuntimeState
{
   TCIRRuntimeMutex mutex;
   TCIRRuntimeBackend backend;
   Method forced_method;
   TCIRRuntimeEntry *entries;
   size_t active_operations;
   int shutdown;
   TCIRRuntimeStats stats;
} TCIRRuntimeState;

static TCIRRuntimeState tcir_runtime_state;

static void tcirRuntimeRaiseException(
   void *runtime_context,
   TCIRRuntimeExceptionKind kind,
   unsigned int tc_pc)
{
   Context context = (Context)runtime_context;
   (void)tc_pc;
   if (context == null)
      return;
   if (kind == TCIR_RUNTIME_EXCEPTION_ARITHMETIC)
      throwException(context, ArithmeticException, null);
}

#if defined(_WIN32)
static volatile LONG tcir_runtime_dispatch_enabled;

static int tcirRuntimeDispatchEnabled(void)
{
   return InterlockedCompareExchange(&tcir_runtime_dispatch_enabled, 0L, 0L) != 0L;
}

static void tcirRuntimeSetDispatchEnabled(int enabled)
{
   (void)InterlockedExchange(&tcir_runtime_dispatch_enabled, enabled ? 1L : 0L);
}

static BOOL CALLBACK tcirRuntimeInitializeOnce(PINIT_ONCE once, PVOID parameter, PVOID *context)
{
   (void)once;
   (void)parameter;
   (void)context;
   InitializeCriticalSection(&tcir_runtime_state.mutex);
   return TRUE;
}

static int tcirRuntimeInitialize(void)
{
   return InitOnceExecuteOnce(&tcir_runtime_once, tcirRuntimeInitializeOnce, NULL, NULL) != 0;
}

#define TCIR_RUNTIME_LOCK() EnterCriticalSection(&tcir_runtime_state.mutex)
#define TCIR_RUNTIME_UNLOCK() LeaveCriticalSection(&tcir_runtime_state.mutex)
#else
static int tcir_runtime_dispatch_enabled;

static int tcirRuntimeDispatchEnabled(void)
{
   return __atomic_load_n(&tcir_runtime_dispatch_enabled, __ATOMIC_ACQUIRE) != 0;
}

static void tcirRuntimeSetDispatchEnabled(int enabled)
{
   __atomic_store_n(&tcir_runtime_dispatch_enabled, enabled != 0, __ATOMIC_RELEASE);
}

static void tcirRuntimeInitializeOnce(void)
{
   (void)pthread_mutex_init(&tcir_runtime_state.mutex, NULL);
}

static int tcirRuntimeInitialize(void)
{
   return pthread_once(&tcir_runtime_once, tcirRuntimeInitializeOnce) == 0;
}

#define TCIR_RUNTIME_LOCK() ((void)pthread_mutex_lock(&tcir_runtime_state.mutex))
#define TCIR_RUNTIME_UNLOCK() ((void)pthread_mutex_unlock(&tcir_runtime_state.mutex))
#endif

static void tcirRuntimeSetDiagnostic(
   TCIRRuntimeDiagnostic *diagnostic,
   TCIRRuntimeBackend backend,
   TCIRRuntimeFallbackReason reason,
   const char *method,
   unsigned int tc_pc,
   const char *format,
   ...)
{
   va_list arguments;

   if (diagnostic == NULL)
      return;
   diagnostic->backend = backend;
   diagnostic->fallback_reason = reason;
   diagnostic->tc_pc = tc_pc;
   if (method != NULL)
      (void)snprintf(diagnostic->method, sizeof(diagnostic->method), "%s", method);
   if (format == NULL)
      return;
   va_start(arguments, format);
   (void)vsnprintf(diagnostic->message, sizeof(diagnostic->message), format, arguments);
   va_end(arguments);
}

void tcirRuntimeDiagnosticClear(TCIRRuntimeDiagnostic *diagnostic)
{
   if (diagnostic != NULL)
   {
      memset(diagnostic, 0, sizeof(*diagnostic));
      diagnostic->tc_pc = TCIR_TCPC_NONE;
      diagnostic->ir.tc_pc = TCIR_TCPC_NONE;
   }
}

const char *tcirRuntimeBackendName(TCIRRuntimeBackend backend)
{
   switch (backend)
   {
      case TCIR_RUNTIME_BACKEND_OFF: return "off";
      case TCIR_RUNTIME_BACKEND_IR: return "ir";
      case TCIR_RUNTIME_BACKEND_JIT: return "jit";
      case TCIR_RUNTIME_BACKEND_AOT: return "aot";
      case TCIR_RUNTIME_BACKEND_AUTO: return "auto";
      default: return "invalid";
   }
}

const char *tcirRuntimeFallbackReasonName(TCIRRuntimeFallbackReason reason)
{
   switch (reason)
   {
      case TCIR_RUNTIME_FALLBACK_NONE: return "none";
      case TCIR_RUNTIME_FALLBACK_DISABLED: return "disabled";
      case TCIR_RUNTIME_FALLBACK_UNREGISTERED: return "unregistered";
      case TCIR_RUNTIME_FALLBACK_FORCED_OTHER_METHOD: return "forced_other_method";
      case TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED: return "frontend_rejected";
      case TCIR_RUNTIME_FALLBACK_BACKEND_UNAVAILABLE: return "backend_unavailable";
      case TCIR_RUNTIME_FALLBACK_BACKEND_COMPILING: return "backend_compiling";
      case TCIR_RUNTIME_FALLBACK_BACKEND_REJECTED: return "backend_rejected";
      case TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED: return "invocation_rejected";
      case TCIR_RUNTIME_FALLBACK_SHUTDOWN: return "shutdown";
      default: return "invalid";
   }
}

int tcirRuntimeBackendAvailable(TCIRRuntimeBackend backend)
{
   switch (backend)
   {
      case TCIR_RUNTIME_BACKEND_OFF:
      case TCIR_RUNTIME_BACKEND_IR:
      case TCIR_RUNTIME_BACKEND_AUTO:
         return 1;
      case TCIR_RUNTIME_BACKEND_JIT:
#if defined(TCIR_RUNTIME_HAS_SLJIT)
         return 1;
#else
         return 0;
#endif
      case TCIR_RUNTIME_BACKEND_AOT:
#if defined(TCIR_RUNTIME_HAS_AOT)
         return 1;
#else
         return 0;
#endif
      default:
         return 0;
   }
}

static TCIRRuntimeEntry *tcirRuntimeFindEntry(Method method)
{
   TCIRRuntimeEntry *entry;
   for (entry = tcir_runtime_state.entries; entry != NULL; entry = entry->next)
      if (entry->method == method)
         return entry;
   return NULL;
}

static void tcirRuntimeDisposeEntries(TCIRRuntimeEntry *entry)
{
   while (entry != NULL)
   {
      TCIRRuntimeEntry *next = entry->next;
#if defined(TCIR_RUNTIME_HAS_SLJIT)
      tcirJitArtifactDestroy(entry->jit_artifact);
#endif
      tcirModuleDestroy(entry->module);
      free(entry);
      entry = next;
   }
}

static void tcirRuntimeRecordFallback(
   TCIRRuntimeFallbackReason reason,
   Method method,
   TCIRRuntimeBackend backend,
   TCIRRuntimeDiagnostic *diagnostic,
   const char *message)
{
   if (reason > TCIR_RUNTIME_FALLBACK_NONE && reason < TCIR_RUNTIME_FALLBACK_COUNT)
      ++tcir_runtime_state.stats.fallback_counts[reason];
   if (tcir_runtime_state.forced_method == method
       && reason != TCIR_RUNTIME_FALLBACK_FORCED_OTHER_METHOD)
      ++tcir_runtime_state.stats.forced_failures;
   tcirRuntimeSetDiagnostic(diagnostic, backend, reason,
                            method == NULL ? NULL : method->name,
                            TCIR_TCPC_NONE, "%s", message);
}

int tcirRuntimeSetBackend(TCIRRuntimeBackend backend)
{
   if (!tcirRuntimeInitialize() || !tcirRuntimeBackendAvailable(backend))
      return 0;
   TCIR_RUNTIME_LOCK();
   if (tcir_runtime_state.shutdown)
   {
      TCIR_RUNTIME_UNLOCK();
      return 0;
   }
   tcir_runtime_state.backend = backend;
   tcirRuntimeSetDispatchEnabled(backend != TCIR_RUNTIME_BACKEND_OFF);
   TCIR_RUNTIME_UNLOCK();
   return 1;
}

TCIRRuntimeBackend tcirRuntimeGetBackend(void)
{
   TCIRRuntimeBackend backend = TCIR_RUNTIME_BACKEND_OFF;
   if (!tcirRuntimeInitialize())
      return backend;
   TCIR_RUNTIME_LOCK();
   backend = tcir_runtime_state.backend;
   TCIR_RUNTIME_UNLOCK();
   return backend;
}

void tcirRuntimeSetForcedMethod(Method method)
{
   if (!tcirRuntimeInitialize())
      return;
   TCIR_RUNTIME_LOCK();
   tcir_runtime_state.forced_method = method;
   TCIR_RUNTIME_UNLOCK();
}

TCIRRuntimeRegistrationStatus tcirRuntimeRegisterMethod(
   Method method,
   const TCIRMethodView *view,
   TCCompiledEntry aot_entry,
   const char *aot_content_hash,
   TCIRRuntimeDiagnostic *diagnostic)
{
   TCIRRuntimeEntry *entry;
   TCIRModule *module;
   TCIRFunction *function = NULL;
   TCIRDiagnostic ir_diagnostic;
   TCIRFrontendResult frontend_status;

   tcirRuntimeDiagnosticClear(diagnostic);
   tcirDiagnosticClear(&ir_diagnostic);
   if (!tcirRuntimeInitialize() || method == NULL || view == NULL)
   {
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method == NULL ? NULL : method->name,
                               TCIR_TCPC_NONE, "invalid runtime registration arguments");
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   }
   if (method->paramCount != view->parameter_count
       || method->iCount < view->i32_home_count
       || method->oCount < view->ref_home_count
       || method->v64Count < view->v64_home_count)
   {
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method->name, TCIR_TCPC_NONE,
                               "runtime method layout does not match the bounded TCIR view");
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   }

   module = tcirModuleCreate(NULL, &ir_diagnostic);
   if (module == NULL)
   {
      if (diagnostic != NULL)
         diagnostic->ir = ir_diagnostic;
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method->name, ir_diagnostic.tc_pc,
                               "unable to create the runtime TCIR module");
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   }
   frontend_status = tcirFrontendBuildFunction(module, view, &function, &ir_diagnostic);
   if (frontend_status != TCIR_FRONTEND_OK)
   {
      TCIR_RUNTIME_LOCK();
      ++tcir_runtime_state.stats.registration_fallbacks;
      ++tcir_runtime_state.stats.fallback_counts[TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED];
      TCIR_RUNTIME_UNLOCK();
      if (diagnostic != NULL)
         diagnostic->ir = ir_diagnostic;
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method->name, ir_diagnostic.tc_pc,
                               "%s", ir_diagnostic.message);
      tcirModuleDestroy(module);
      return frontend_status == TCIR_FRONTEND_FALLBACK
         ? TCIR_RUNTIME_REGISTRATION_FALLBACK
         : TCIR_RUNTIME_REGISTRATION_ERROR;
   }

   entry = (TCIRRuntimeEntry *)calloc(1U, sizeof(*entry));
   if (entry == NULL)
   {
      tcirModuleDestroy(module);
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method->name, TCIR_TCPC_NONE,
                               "unable to allocate the runtime method entry");
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   }
   entry->method = method;
   entry->module = module;
   entry->function = function;
   entry->aot_entry = aot_entry;
   if (aot_content_hash != NULL)
      (void)snprintf(entry->aot_content_hash, sizeof(entry->aot_content_hash), "%s", aot_content_hash);

   TCIR_RUNTIME_LOCK();
   if (tcir_runtime_state.shutdown || tcirRuntimeFindEntry(method) != NULL)
   {
      int is_shutdown = tcir_runtime_state.shutdown;
      TCIR_RUNTIME_UNLOCK();
      tcirRuntimeDisposeEntries(entry);
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_OFF,
                               is_shutdown
                                  ? TCIR_RUNTIME_FALLBACK_SHUTDOWN
                                  : TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED,
                               method->name, TCIR_TCPC_NONE,
                               is_shutdown
                                  ? "runtime dispatch is shut down"
                                  : "runtime method is already registered");
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   }
   entry->next = tcir_runtime_state.entries;
   tcir_runtime_state.entries = entry;
   ++tcir_runtime_state.stats.methods_registered;
   TCIR_RUNTIME_UNLOCK();
   return TCIR_RUNTIME_REGISTRATION_READY;
}

static int tcirRuntimeBuildArguments(
   const TCIRRuntimeEntry *entry,
   Int32Array i32_homes,
   TCObjectArray ref_homes,
   Value64Array v64_homes,
   TCIRRuntimeValue *arguments)
{
   size_t i32_index = 0U;
   size_t ref_index = entry->method->flags.isStatic ? 0U : 1U;
   size_t v64_index = 0U;
   size_t parameter_index;

   if (tcirFunctionParameterCount(entry->function) != entry->method->paramCount)
      return 0;
   for (parameter_index = 0U; parameter_index < entry->method->paramCount; ++parameter_index)
   {
      switch (entry->method->paramRegs[parameter_index])
      {
         case RegI:
            if (i32_homes == NULL || i32_index >= entry->method->iCount)
               return 0;
            arguments[parameter_index].i32 = i32_homes[i32_index++];
            break;
         case RegO:
            if (ref_homes == NULL || ref_index >= entry->method->oCount)
               return 0;
            arguments[parameter_index].ref = ref_homes[ref_index++];
            break;
         case RegD:
            if (v64_homes == NULL || v64_index >= entry->method->v64Count)
               return 0;
            arguments[parameter_index].f64 = ((TCIRV64Home *)v64_homes)[v64_index++].f64;
            break;
         case RegL:
            if (v64_homes == NULL || v64_index >= entry->method->v64Count)
               return 0;
            arguments[parameter_index].i64 = ((TCIRV64Home *)v64_homes)[v64_index++].i64;
            break;
         default:
            return 0;
      }
   }
   return 1;
}

static TCCompiledStatus tcirRuntimeDispatchCall(
   void *runtime_context,
   const void *method_key,
   void *receiver,
   const TCIRRuntimeValue *arguments,
   size_t argument_count,
   TCCompiledResult *result)
{
   Context context = (Context)runtime_context;
   Method method = (Method)method_key;
   TValue *values = NULL;
   TValue returned;
   size_t index;
   bool previous_parameters_in_array;

   if (result != NULL)
   {
      memset(result, 0, sizeof(*result));
      result->status = TC_COMPILED_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = TCIR_TCPC_NONE;
   }
   if (context == NULL || method == NULL || result == NULL
       || argument_count != method->paramCount
       || (argument_count != 0U && arguments == NULL)
       || (!method->flags.isStatic && receiver == NULL))
      return TC_COMPILED_REJECTED;

   if (argument_count != 0U)
   {
      values = (TValue *)calloc(argument_count, sizeof(*values));
      if (values == NULL)
      {
         result->status = TC_COMPILED_OUT_OF_MEMORY;
         return TC_COMPILED_OUT_OF_MEMORY;
      }
      for (index = 0U; index < argument_count; ++index)
      {
         switch (method->paramRegs[index])
         {
            case RegI: values[index].asInt32 = arguments[index].i32; break;
            case RegO: values[index].asObj = (TCObject)arguments[index].ref; break;
            case RegD: values[index].asDouble = arguments[index].f64; break;
            case RegL: values[index].asInt64 = arguments[index].i64; break;
            default:
               free(values);
               return TC_COMPILED_REJECTED;
         }
      }
   }

   TCIR_RUNTIME_LOCK();
   ++tcir_runtime_state.stats.call_thunks;
   TCIR_RUNTIME_UNLOCK();
   previous_parameters_in_array = context->parametersInArray;
   context->parametersInArray = argument_count != 0U;
   if (method->flags.isStatic)
      returned = argument_count == 0U
         ? executeMethod(context, method)
         : executeMethod(context, method, values);
   else
      returned = argument_count == 0U
         ? executeMethod(context, method, (TCObject)receiver)
         : executeMethod(context, method, (TCObject)receiver, values);
   context->parametersInArray = previous_parameters_in_array;
   free(values);

   if (context->thrownException != null)
   {
      result->status = TC_COMPILED_THROWN;
      return TC_COMPILED_THROWN;
   }
   result->status = TC_COMPILED_RETURNED;
   if (method->cpReturn == 0U)
      result->type = TCIR_TYPE_VOID;
   else
      switch (method->returnReg)
      {
         case RegI: result->type = TCIR_TYPE_I32; result->value.i32 = returned.asInt32; break;
         case RegO: result->type = TCIR_TYPE_REF; result->value.ref = returned.asObj; break;
         case RegD: result->type = TCIR_TYPE_F64; result->value.f64 = returned.asDouble; break;
         case RegL: result->type = TCIR_TYPE_I64; result->value.i64 = returned.asInt64; break;
         default: result->status = TC_COMPILED_REJECTED; return TC_COMPILED_REJECTED;
      }
   return TC_COMPILED_RETURNED;
}

static void tcirRuntimeReleaseOperation(void)
{
   TCIRRuntimeEntry *dispose = NULL;
   TCIR_RUNTIME_LOCK();
   if (tcir_runtime_state.active_operations != 0U)
      --tcir_runtime_state.active_operations;
   if (tcir_runtime_state.shutdown && tcir_runtime_state.active_operations == 0U)
   {
      dispose = tcir_runtime_state.entries;
      tcir_runtime_state.entries = NULL;
   }
   TCIR_RUNTIME_UNLOCK();
   tcirRuntimeDisposeEntries(dispose);
}

#if defined(TCIR_RUNTIME_HAS_SLJIT)
static uint64_t tcirRuntimeElapsedNanoseconds(clock_t start, clock_t finish)
{
   if (finish < start)
      return 0U;
   return ((uint64_t)(finish - start) * UINT64_C(1000000000)) / (uint64_t)CLOCKS_PER_SEC;
}
#endif

TCIRRuntimeDispatchStatus tcirRuntimeTryDispatch(
   Context context,
   Method method,
   Int32Array i32_homes,
   TCObjectArray ref_homes,
   Value64Array v64_homes,
   TCCompiledResult *result,
   TCIRRuntimeDiagnostic *diagnostic)
{
   TCIRRuntimeEntry *entry;
   TCIRRuntimeBackend configured_backend;
   TCIRRuntimeBackend selected_backend = TCIR_RUNTIME_BACKEND_OFF;
   TCIRRuntimeFallbackReason fallback_reason = TCIR_RUNTIME_FALLBACK_NONE;
   TCIRRuntimeValue *arguments = NULL;
   TCCompiledFrame frame;
   TCCompiledRuntime runtime;
   TCCompiledStatus compiled_status = TC_COMPILED_REJECTED;
   int compile_jit = 0;
#if defined(TCIR_RUNTIME_HAS_SLJIT)
   const TCIRJitArtifact *jit_artifact = NULL;
#endif

   if (diagnostic == NULL && !tcirRuntimeDispatchEnabled())
      return TCIR_RUNTIME_DISPATCH_FALLBACK;
   tcirRuntimeDiagnosticClear(diagnostic);
   if (result != NULL)
   {
      memset(result, 0, sizeof(*result));
      result->status = TC_COMPILED_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = TCIR_TCPC_NONE;
   }
   if (!tcirRuntimeInitialize() || context == NULL || method == NULL || result == NULL)
      return TCIR_RUNTIME_DISPATCH_FALLBACK;

   TCIR_RUNTIME_LOCK();
   ++tcir_runtime_state.stats.dispatch_attempts;
   configured_backend = tcir_runtime_state.backend;
   if (tcir_runtime_state.shutdown)
      fallback_reason = TCIR_RUNTIME_FALLBACK_SHUTDOWN;
   else if (configured_backend == TCIR_RUNTIME_BACKEND_OFF)
      fallback_reason = TCIR_RUNTIME_FALLBACK_DISABLED;
   else if (tcir_runtime_state.forced_method != NULL && tcir_runtime_state.forced_method != method)
      fallback_reason = TCIR_RUNTIME_FALLBACK_FORCED_OTHER_METHOD;
   entry = fallback_reason == TCIR_RUNTIME_FALLBACK_NONE ? tcirRuntimeFindEntry(method) : NULL;
   if (fallback_reason == TCIR_RUNTIME_FALLBACK_NONE && entry == NULL)
      fallback_reason = TCIR_RUNTIME_FALLBACK_UNREGISTERED;

   if (fallback_reason == TCIR_RUNTIME_FALLBACK_NONE)
   {
      if (configured_backend == TCIR_RUNTIME_BACKEND_AOT)
      {
#if defined(TCIR_RUNTIME_HAS_AOT)
         if (entry->aot_entry != NULL)
            selected_backend = TCIR_RUNTIME_BACKEND_AOT;
         else
#endif
            fallback_reason = TCIR_RUNTIME_FALLBACK_BACKEND_UNAVAILABLE;
      }
      else if (configured_backend == TCIR_RUNTIME_BACKEND_IR)
         selected_backend = TCIR_RUNTIME_BACKEND_IR;
      else if (configured_backend == TCIR_RUNTIME_BACKEND_AUTO)
      {
#if defined(TCIR_RUNTIME_HAS_AOT)
         if (entry->aot_entry != NULL)
            selected_backend = TCIR_RUNTIME_BACKEND_AOT;
         else
#endif
#if defined(TCIR_RUNTIME_HAS_SLJIT)
         if (entry->jit_state == TCIR_RUNTIME_JIT_READY)
         {
            selected_backend = TCIR_RUNTIME_BACKEND_JIT;
            jit_artifact = entry->jit_artifact;
         }
         else if (entry->jit_state == TCIR_RUNTIME_JIT_UNTRIED)
         {
            entry->jit_state = TCIR_RUNTIME_JIT_COMPILING;
            selected_backend = TCIR_RUNTIME_BACKEND_JIT;
            compile_jit = 1;
         }
         else if (entry->jit_state == TCIR_RUNTIME_JIT_COMPILING)
            fallback_reason = TCIR_RUNTIME_FALLBACK_BACKEND_COMPILING;
         else
            selected_backend = TCIR_RUNTIME_BACKEND_IR;
#else
         selected_backend = TCIR_RUNTIME_BACKEND_IR;
#endif
      }
      else if (configured_backend == TCIR_RUNTIME_BACKEND_JIT)
      {
#if defined(TCIR_RUNTIME_HAS_SLJIT)
         if (entry->jit_state == TCIR_RUNTIME_JIT_READY)
         {
            selected_backend = TCIR_RUNTIME_BACKEND_JIT;
            jit_artifact = entry->jit_artifact;
         }
         else if (entry->jit_state == TCIR_RUNTIME_JIT_UNTRIED)
         {
            entry->jit_state = TCIR_RUNTIME_JIT_COMPILING;
            selected_backend = TCIR_RUNTIME_BACKEND_JIT;
            compile_jit = 1;
         }
         else
            fallback_reason = entry->jit_state == TCIR_RUNTIME_JIT_COMPILING
               ? TCIR_RUNTIME_FALLBACK_BACKEND_COMPILING
               : TCIR_RUNTIME_FALLBACK_BACKEND_REJECTED;
#else
         fallback_reason = TCIR_RUNTIME_FALLBACK_BACKEND_UNAVAILABLE;
#endif
      }
      else
         fallback_reason = TCIR_RUNTIME_FALLBACK_BACKEND_UNAVAILABLE;
   }

   if (fallback_reason != TCIR_RUNTIME_FALLBACK_NONE)
   {
      tcirRuntimeRecordFallback(fallback_reason, method, configured_backend, diagnostic,
                                "compiled dispatch selected the legacy interpreter");
      TCIR_RUNTIME_UNLOCK();
      return TCIR_RUNTIME_DISPATCH_FALLBACK;
   }
   ++tcir_runtime_state.active_operations;
   TCIR_RUNTIME_UNLOCK();

#if defined(TCIR_RUNTIME_HAS_SLJIT)
   if (compile_jit)
   {
      TCIRJitArtifact *compiled_artifact = NULL;
      TCIRJitDiagnostic jit_diagnostic;
      TCIRJitCompileStatus compile_status;
      clock_t start = clock();
      clock_t finish;

      compile_status = tcirJitCompile(entry->function, NULL, &compiled_artifact, &jit_diagnostic);
      finish = clock();
      TCIR_RUNTIME_LOCK();
      ++tcir_runtime_state.stats.jit_compilations;
      tcir_runtime_state.stats.jit_compile_nanoseconds += tcirRuntimeElapsedNanoseconds(start, finish);
      if (!tcir_runtime_state.shutdown && compile_status == TCIR_JIT_COMPILE_READY)
      {
         entry->jit_artifact = compiled_artifact;
         entry->jit_state = TCIR_RUNTIME_JIT_READY;
         jit_artifact = compiled_artifact;
         tcir_runtime_state.stats.jit_code_bytes += tcirJitArtifactCodeSize(compiled_artifact);
      }
      else
      {
         entry->jit_state = TCIR_RUNTIME_JIT_REJECTED;
         ++tcir_runtime_state.stats.jit_compile_failures;
         if (tcir_runtime_state.shutdown)
            fallback_reason = TCIR_RUNTIME_FALLBACK_SHUTDOWN;
         else if (configured_backend == TCIR_RUNTIME_BACKEND_AUTO)
            selected_backend = TCIR_RUNTIME_BACKEND_IR;
         else
            fallback_reason = TCIR_RUNTIME_FALLBACK_BACKEND_REJECTED;
      }
      TCIR_RUNTIME_UNLOCK();
      if (jit_artifact != compiled_artifact)
         tcirJitArtifactDestroy(compiled_artifact);
   }
#else
   (void)compile_jit;
#endif

   if (fallback_reason != TCIR_RUNTIME_FALLBACK_NONE)
   {
      TCIR_RUNTIME_LOCK();
      tcirRuntimeRecordFallback(fallback_reason, method, configured_backend, diagnostic,
                                "compiled backend was unavailable after preparation");
      TCIR_RUNTIME_UNLOCK();
      tcirRuntimeReleaseOperation();
      return TCIR_RUNTIME_DISPATCH_FALLBACK;
   }

   arguments = (TCIRRuntimeValue *)calloc(method->paramCount == 0U ? 1U : method->paramCount,
                                           sizeof(*arguments));
   if (arguments == NULL)
   {
      result->status = TC_COMPILED_OUT_OF_MEMORY;
      tcirRuntimeReleaseOperation();
      return TCIR_RUNTIME_DISPATCH_OUT_OF_MEMORY;
   }
   if (!tcirRuntimeBuildArguments(entry, i32_homes, ref_homes, v64_homes, arguments))
   {
      free(arguments);
      TCIR_RUNTIME_LOCK();
      tcirRuntimeRecordFallback(TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED,
                                method, selected_backend, diagnostic,
                                "runtime frame does not match the registered method");
      TCIR_RUNTIME_UNLOCK();
      tcirRuntimeReleaseOperation();
      return TCIR_RUNTIME_DISPATCH_FALLBACK;
   }

   memset(&frame, 0, sizeof(frame));
   frame.i32_homes = i32_homes;
   frame.i32_home_count = method->iCount;
   frame.ref_homes = (void **)ref_homes;
   frame.ref_home_count = method->oCount;
   frame.v64_homes = (TCIRV64Home *)v64_homes;
   frame.v64_home_count = method->v64Count;
   frame.arguments = arguments;
   frame.argument_count = method->paramCount;
   runtime.abi_version = TC_RUNTIME_ABI_VERSION;
   runtime.context = context;
   runtime.dispatch = tcirRuntimeDispatchCall;
   frame.runtime = &runtime;

   if (selected_backend == TCIR_RUNTIME_BACKEND_IR)
   {
      TCIRInterpreterFrame interpreter_frame;
      TCIRInterpreterResult interpreter_result;
      TCIRDiagnostic ir_diagnostic;
      TCIRInterpreterStatus interpreter_status;

      memset(&interpreter_frame, 0, sizeof(interpreter_frame));
      interpreter_frame.i32_homes = frame.i32_homes;
      interpreter_frame.i32_home_count = frame.i32_home_count;
      interpreter_frame.ref_homes = frame.ref_homes;
      interpreter_frame.ref_home_count = frame.ref_home_count;
      interpreter_frame.v64_homes = frame.v64_homes;
      interpreter_frame.v64_home_count = frame.v64_home_count;
      interpreter_frame.arguments = frame.arguments;
      interpreter_frame.argument_count = frame.argument_count;
      interpreter_frame.runtime_context = context;
      interpreter_frame.raise_exception = tcirRuntimeRaiseException;
      interpreter_status = tcirInterpretFunction(entry->function, &interpreter_frame, NULL,
                                                 &interpreter_result, &ir_diagnostic);
      result->status = interpreter_status == TCIR_INTERPRETER_RETURNED
         ? TC_COMPILED_RETURNED
         : interpreter_status == TCIR_INTERPRETER_THROWN
            ? TC_COMPILED_THROWN
            : interpreter_status == TCIR_INTERPRETER_OUT_OF_MEMORY
               ? TC_COMPILED_OUT_OF_MEMORY
               : TC_COMPILED_REJECTED;
      result->type = interpreter_result.type;
      result->value = interpreter_result.value;
      result->tc_pc = interpreter_result.tc_pc;
      compiled_status = result->status;
   }
   else if (selected_backend == TCIR_RUNTIME_BACKEND_AOT)
      compiled_status = entry->aot_entry(&frame, result);
#if defined(TCIR_RUNTIME_HAS_SLJIT)
   else if (selected_backend == TCIR_RUNTIME_BACKEND_JIT)
   {
      TCIRJitDiagnostic jit_diagnostic;
      compiled_status = tcirJitInvoke(jit_artifact, &frame, result, &jit_diagnostic);
   }
#endif

   free(arguments);
   TCIR_RUNTIME_LOCK();
   if (selected_backend == TCIR_RUNTIME_BACKEND_IR)
      ++tcir_runtime_state.stats.ir_invocations;
   else if (selected_backend == TCIR_RUNTIME_BACKEND_JIT)
      ++tcir_runtime_state.stats.jit_invocations;
   else if (selected_backend == TCIR_RUNTIME_BACKEND_AOT)
      ++tcir_runtime_state.stats.aot_invocations;
   if (compiled_status == TC_COMPILED_RETURNED)
      ++tcir_runtime_state.stats.dispatch_returns;
   else if (compiled_status == TC_COMPILED_THROWN && context->thrownException != null)
      ++tcir_runtime_state.stats.dispatch_throws;
   else if (compiled_status == TC_COMPILED_REJECTED
            || compiled_status == TC_COMPILED_THROWN)
      tcirRuntimeRecordFallback(TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED,
                                method, selected_backend, diagnostic,
                                compiled_status == TC_COMPILED_THROWN
                                   ? "compiled invocation reported an exception without an object"
                                   : "compiled invocation rejected the prepared frame");
   TCIR_RUNTIME_UNLOCK();
   tcirRuntimeReleaseOperation();

   if (compiled_status == TC_COMPILED_RETURNED)
      return TCIR_RUNTIME_DISPATCH_RETURNED;
   if (compiled_status == TC_COMPILED_THROWN && context->thrownException != null)
      return TCIR_RUNTIME_DISPATCH_THROWN;
   if (compiled_status == TC_COMPILED_OUT_OF_MEMORY)
      return TCIR_RUNTIME_DISPATCH_OUT_OF_MEMORY;
   return TCIR_RUNTIME_DISPATCH_FALLBACK;
}

int tcirRuntimeWriteIr(Method method, const char *path, TCIRRuntimeDiagnostic *diagnostic)
{
   TCIRRuntimeEntry *entry;
   TCIRDiagnostic ir_diagnostic;
   char *text;
   FILE *output;
   int written = 0;

   tcirRuntimeDiagnosticClear(diagnostic);
   if (!tcirRuntimeInitialize() || method == NULL || path == NULL)
      return 0;
   TCIR_RUNTIME_LOCK();
   entry = tcir_runtime_state.shutdown ? NULL : tcirRuntimeFindEntry(method);
   if (entry != NULL)
      ++tcir_runtime_state.active_operations;
   TCIR_RUNTIME_UNLOCK();
   if (entry == NULL)
   {
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_IR,
                               TCIR_RUNTIME_FALLBACK_UNREGISTERED,
                               method->name, TCIR_TCPC_NONE,
                               "method is not registered for an IR dump");
      return 0;
   }
   text = tcirFunctionDump(entry->function, &ir_diagnostic);
   output = text == NULL ? NULL : fopen(path, "wb");
   if (output != NULL)
   {
      size_t length = strlen(text);
      size_t bytes_written = fwrite(text, 1U, length, output);
      int close_status = fclose(output);
      written = bytes_written == length && close_status == 0;
   }
   if (text != NULL)
      tcirFreeText(entry->module, text);
   if (!written)
      tcirRuntimeSetDiagnostic(diagnostic, TCIR_RUNTIME_BACKEND_IR,
                               TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED,
                               method->name, TCIR_TCPC_NONE,
                               "unable to write the registered IR dump");
   tcirRuntimeReleaseOperation();
   return written;
}

void tcirRuntimeGetStats(TCIRRuntimeStats *stats)
{
   if (stats == NULL)
      return;
   memset(stats, 0, sizeof(*stats));
   if (!tcirRuntimeInitialize())
      return;
   TCIR_RUNTIME_LOCK();
   *stats = tcir_runtime_state.stats;
   TCIR_RUNTIME_UNLOCK();
}

void tcirRuntimeShutdown(void)
{
   TCIRRuntimeEntry *dispose = NULL;
   if (!tcirRuntimeInitialize())
      return;
   TCIR_RUNTIME_LOCK();
   tcir_runtime_state.shutdown = 1;
   tcir_runtime_state.backend = TCIR_RUNTIME_BACKEND_OFF;
   tcirRuntimeSetDispatchEnabled(0);
   tcir_runtime_state.forced_method = NULL;
   if (tcir_runtime_state.active_operations == 0U)
   {
      dispose = tcir_runtime_state.entries;
      tcir_runtime_state.entries = NULL;
   }
   TCIR_RUNTIME_UNLOCK();
   tcirRuntimeDisposeEntries(dispose);
}

int tcirRuntimeReset(void)
{
   TCIRRuntimeEntry *dispose;
   if (!tcirRuntimeInitialize())
      return 0;
   TCIR_RUNTIME_LOCK();
   if (tcir_runtime_state.active_operations != 0U)
   {
      TCIR_RUNTIME_UNLOCK();
      return 0;
   }
   dispose = tcir_runtime_state.entries;
   tcir_runtime_state.entries = NULL;
   tcir_runtime_state.backend = TCIR_RUNTIME_BACKEND_OFF;
   tcirRuntimeSetDispatchEnabled(0);
   tcir_runtime_state.forced_method = NULL;
   tcir_runtime_state.shutdown = 0;
   memset(&tcir_runtime_state.stats, 0, sizeof(tcir_runtime_state.stats));
   TCIR_RUNTIME_UNLOCK();
   tcirRuntimeDisposeEntries(dispose);
   return 1;
}
