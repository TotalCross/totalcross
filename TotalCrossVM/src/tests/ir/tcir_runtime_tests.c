// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "tcir_runtime.h"

#if defined(TCIR_RUNTIME_TEST_HAS_AOT)
#include "tcir_aot.h"
#include "tcir_aot_generated.h"
#endif

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if defined(_WIN32)
#include <windows.h>
#else
#include <pthread.h>
#endif

#define REQUIRE(condition) \
   do \
   { \
      if (!(condition)) \
      { \
         fprintf(stderr, "requirement failed at %s:%d: %s\n", __FILE__, __LINE__, #condition); \
         return 0; \
      } \
   } while (0)

typedef struct TCIRConverterFixture
{
   const char *identity;
   const unsigned int *code;
   const int *lines;
   size_t code_count;
   unsigned int i32_count;
   unsigned int ref_count;
   unsigned int v64_count;
   unsigned int parameter_count;
   const TCIRMethodParameter *parameters;
   TCIRType return_type;
   const TCIRType *v64_home_types;
} TCIRConverterFixture;

#include "fixtures/tcir_converter_fixtures.h"

typedef struct RuntimeMethod
{
   TCode code[16];
   TMethod method;
   TTCClass class_;
   TConstantPool constant_pool;
   uint8_t parameter_registers[4];
   Method bound_normal[1];
} RuntimeMethod;

typedef struct RuntimeExecution
{
   TValue value;
   int frame_restored;
   int usage_released;
} RuntimeExecution;

static int buildFixtureView(
   const TCIRConverterFixture *fixture,
   TCIRMethodView *view,
   TCIRMethodParameter *parameters)
{
   static const int constants[] = { 0 };
   (void)parameters;
   memset(view, 0, sizeof(*view));
   view->identity = fixture->identity;
   view->code = fixture->code;
   view->code_slot_count = fixture->code_count;
   view->i32_home_count = fixture->i32_count;
   view->ref_home_count = fixture->ref_count;
   view->v64_home_count = fixture->v64_count;
   view->v64_home_types = fixture->v64_home_types;
   view->parameters = fixture->parameters;
   view->parameter_count = fixture->parameter_count;
   view->return_type = fixture->return_type;
   view->i32_constants = constants;
   view->i32_constant_count = sizeof(constants) / sizeof(constants[0]);
   view->source_lines = fixture->lines;
   view->resolve_call_shape = tcirResolveConverterFixtureCall;
   view->resolve_call_shape_user_data = (void *)fixture;
   return 1;
}

static void initializeFixtureMethod(
   RuntimeMethod *runtime_method,
   const TCIRConverterFixture *fixture)
{
   size_t index;

   memset(runtime_method, 0, sizeof(*runtime_method));
   for (index = 0U; index < fixture->code_count; ++index)
      runtime_method->code[index].u32.u32 = (uint32)fixture->code[index];
   runtime_method->class_.cp = &runtime_method->constant_pool;
   runtime_method->class_.name = (CharP)"fixtures.TCIRPoc";
   runtime_method->method.iCount = (uint8)fixture->i32_count;
   runtime_method->method.oCount = (uint8)(fixture->ref_count == 0U ? 1U : fixture->ref_count);
   runtime_method->method.v64Count = (uint8)fixture->v64_count;
   runtime_method->method.paramSkip = (uint8)((3U + fixture->parameter_count) >> 2U);
   runtime_method->method.code = runtime_method->code;
   runtime_method->method.class_ = &runtime_method->class_;
   runtime_method->method.name = (CharP)fixture->identity;
   runtime_method->method.paramCount = (uint16)fixture->parameter_count;
   runtime_method->method.paramRegs = runtime_method->parameter_registers;
   runtime_method->method.cpReturn = 1U;
   runtime_method->method.returnReg = RegI;
   runtime_method->method.flags.isStatic = true;
   for (index = 0U; index < fixture->parameter_count; ++index)
      runtime_method->parameter_registers[index] = (uint8)RegI;
}

static void initializeContext(
   TContext *context,
   int32 *register_i32,
   TCObject *register_ref,
   int64 *register_v64,
   VoidP *call_stack,
   size_t capacity)
{
   memset(context, 0, sizeof(*context));
   memset(register_i32, 0, capacity * sizeof(*register_i32));
   memset(register_ref, 0, capacity * sizeof(*register_ref));
   memset(register_v64, 0, capacity * sizeof(*register_v64));
   memset(call_stack, 0, capacity * sizeof(*call_stack));
   context->regI = context->regIStart = register_i32;
   context->regIEnd = register_i32 + capacity;
   context->regO = context->regOStart = register_ref;
   context->regOEnd = register_ref + capacity;
   context->reg64 = context->reg64Start = register_v64;
   context->reg64End = register_v64 + capacity;
   context->callStack = context->callStackStart = call_stack;
   context->callStackEnd = call_stack + capacity;
   context->nmp.currentContext = context;
   {
      SETUP_MUTEX;
      INIT_MUTEX(context->usageLock);
#if defined(POSIX) || defined(ANDROID)
      pthread_mutexattr_destroy(&mutex_attrs);
#endif
   }
}

static RuntimeExecution executeRuntimeMethod(Method method, int32_t first, int32_t second)
{
   enum { CAPACITY = 64 };
   TContext context;
   int32 register_i32[CAPACITY];
   TCObject register_ref[CAPACITY];
   int64 register_v64[CAPACITY];
   VoidP call_stack[CAPACITY];
   RuntimeExecution execution;

   memset(&execution, 0, sizeof(execution));
   initializeContext(&context, register_i32, register_ref, register_v64, call_stack, CAPACITY);
   if (method->paramCount == 2U)
      execution.value = executeMethod(&context, method, first, second);
   else if (method->paramCount == 1U)
      execution.value = executeMethod(&context, method, first);
   else
      execution.value = executeMethod(&context, method);
   execution.frame_restored = context.regI == context.regIStart
      && context.regO == context.regOStart
      && context.reg64 == context.reg64Start
      && context.callStack == context.callStackStart;
   execution.usage_released = context.usageCount == 0 && context.usageOwner == null;
   DESTROY_MUTEX(context.usageLock);
   return execution;
}

static TCIRRuntimeRegistrationStatus registerFixture(
   RuntimeMethod *runtime_method,
   const TCIRConverterFixture *fixture,
   TCCompiledEntry aot_entry,
   const char *content_hash)
{
   TCIRMethodView view;
   TCIRMethodParameter parameters[2];
   TCIRRuntimeDiagnostic diagnostic;

   if (!buildFixtureView(fixture, &view, parameters))
      return TCIR_RUNTIME_REGISTRATION_ERROR;
   return tcirRuntimeRegisterMethod(
      &runtime_method->method, &view, aot_entry, content_hash, &diagnostic);
}

#if defined(TCIR_RUNTIME_TEST_HAS_AOT)
static const TCIRAotRegistryEntry *findAotEntry(size_t fixture_index)
{
   static const char *const method_names[] = { "add", "abs", "sumTo" };
   static const char *const signatures[] = { "(II)I", "(I)I", "(I)I" };
   static const char *const hashes[] = {
      "2e80511cea626eec", "14d3639c5a105a4a", "5b4140fbcc53b2a4"
   };
   return tcirAotRegistryFind(
      tcir_aot_generated_registry,
      tcir_aot_generated_registry_count,
      "fixtures.TCIRPoc",
      method_names[fixture_index],
      signatures[fixture_index],
      hashes[fixture_index]);
}
#endif

static int testPolicyAndObservability(void)
{
   RuntimeMethod runtime_method;
   RuntimeExecution execution;
   TCIRRuntimeStats stats;
   TCIRRuntimeDiagnostic diagnostic;
   FILE *dump;
   char buffer[256];
   int found_identity = 0;

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&runtime_method, &tcir_converter_fixtures[0]);
   REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0], NULL, NULL)
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeGetBackend() == TCIR_RUNTIME_BACKEND_OFF);
   execution = executeRuntimeMethod(&runtime_method.method, 19, 23);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.dispatch_attempts == 0U);

   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_IR));
   REQUIRE(tcirRuntimeGetBackend() == TCIR_RUNTIME_BACKEND_IR);
   tcirRuntimeSetForcedMethod(&runtime_method.method);
   execution = executeRuntimeMethod(&runtime_method.method, INT32_MAX, 1);
   REQUIRE(execution.value.asInt32 == INT32_MIN && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.ir_invocations == 1U && stats.dispatch_returns == 1U && stats.forced_failures == 0U);
   REQUIRE(tcirRuntimeWriteIr(&runtime_method.method, TCIR_RUNTIME_DUMP_PATH, &diagnostic));
   dump = fopen(TCIR_RUNTIME_DUMP_PATH, "rb");
   REQUIRE(dump != NULL);
   while (fgets(buffer, sizeof(buffer), dump) != NULL)
      if (strstr(buffer, "fixtures.TCIRPoc.add") != NULL)
         found_identity = 1;
   REQUIRE(fclose(dump) == 0);
   REQUIRE(found_identity);
   REQUIRE(remove(TCIR_RUNTIME_DUMP_PATH) == 0);

   tcirRuntimeShutdown();
   execution = executeRuntimeMethod(&runtime_method.method, 20, 22);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(tcirRuntimeGetBackend() == TCIR_RUNTIME_BACKEND_OFF);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_SHUTDOWN] == 0U);
   REQUIRE(tcirRuntimeReset());
   return 1;
}

static int testBackendPolicies(void)
{
   RuntimeMethod runtime_method;
   RuntimeExecution execution;
   TCIRRuntimeStats stats;

   initializeFixtureMethod(&runtime_method, &tcir_converter_fixtures[0]);
   REQUIRE(tcirRuntimeBackendAvailable(TCIR_RUNTIME_BACKEND_IR));
#if defined(TCIR_RUNTIME_TEST_HAS_SLJIT)
   REQUIRE(tcirRuntimeBackendAvailable(TCIR_RUNTIME_BACKEND_JIT));
   REQUIRE(tcirRuntimeReset());
   REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0], NULL, NULL)
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_JIT));
   execution = executeRuntimeMethod(&runtime_method.method, 19, 23);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.jit_compilations == 1U && stats.jit_invocations == 1U && stats.jit_code_bytes > 0U);
#else
   REQUIRE(!tcirRuntimeBackendAvailable(TCIR_RUNTIME_BACKEND_JIT));
   REQUIRE(!tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_JIT));
#endif

#if defined(TCIR_RUNTIME_TEST_HAS_AOT)
   {
      const TCIRAotRegistryEntry *aot_entry = findAotEntry(0U);
      REQUIRE(aot_entry != NULL);
      REQUIRE(tcirRuntimeBackendAvailable(TCIR_RUNTIME_BACKEND_AOT));
      REQUIRE(tcirRuntimeReset());
      REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0],
                              aot_entry->entry, aot_entry->content_hash)
              == TCIR_RUNTIME_REGISTRATION_READY);
      REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
      execution = executeRuntimeMethod(&runtime_method.method, 19, 23);
      REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
      tcirRuntimeGetStats(&stats);
      REQUIRE(stats.aot_invocations == 1U);

      REQUIRE(tcirRuntimeReset());
      REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0],
                              aot_entry->entry, aot_entry->content_hash)
              == TCIR_RUNTIME_REGISTRATION_READY);
      REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AUTO));
      execution = executeRuntimeMethod(&runtime_method.method, 20, 22);
      REQUIRE(execution.value.asInt32 == 42);
      tcirRuntimeGetStats(&stats);
      REQUIRE(stats.aot_invocations == 1U && stats.jit_compilations == 0U);
   }
#else
   REQUIRE(!tcirRuntimeBackendAvailable(TCIR_RUNTIME_BACKEND_AOT));
   REQUIRE(!tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
#endif
   REQUIRE(tcirRuntimeReset());
   return 1;
}

static int testRegistrationAndForcedFallback(void)
{
   static const unsigned int unsupported_code[] = { NEWOBJ };
   static const int unsupported_lines[] = { 1 };
   static const TCIRConverterFixture unsupported_fixture = {
      "fixtures.TCIRPoc.unsupported:()I",
      unsupported_code,
      unsupported_lines,
      1U,
      0U,
      1U,
      0U,
      0U
   };
   RuntimeMethod unsupported_method;
   RuntimeMethod unregistered_method;
   RuntimeExecution execution;
   TCIRRuntimeStats stats;

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&unsupported_method, &unsupported_fixture);
   REQUIRE(registerFixture(&unsupported_method, &unsupported_fixture, NULL, NULL)
           == TCIR_RUNTIME_REGISTRATION_FALLBACK);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.registration_fallbacks == 1U);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_FRONTEND_REJECTED] == 1U);

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&unregistered_method, &tcir_converter_fixtures[0]);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_IR));
   tcirRuntimeSetForcedMethod(&unregistered_method.method);
   execution = executeRuntimeMethod(&unregistered_method.method, 20, 22);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.forced_failures == 1U);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_UNREGISTERED] == 1U);
   REQUIRE(tcirRuntimeReset());
   return 1;
}

static void initializeCaller(RuntimeMethod *caller, Method callee)
{
   memset(caller, 0, sizeof(*caller));
   caller->class_.cp = &caller->constant_pool;
   caller->class_.name = (CharP)"fixtures.TCIRCaller";
   caller->constant_pool.boundNormal = caller->bound_normal;
   caller->bound_normal[0] = callee;
   caller->code[0].mtd.op = CALL_normal;
   caller->code[0].mtd.sym = 0U;
   caller->code[0].mtd.retOr1stParam = 2U;
   caller->code[1].params.param1 = 0U;
   caller->code[1].params.param2 = 1U;
   caller->code[2].reg.op = RETURN_regI;
   caller->code[2].reg.reg = 2U;
   caller->method.iCount = 3U;
   caller->method.oCount = 1U;
   caller->method.code = caller->code;
   caller->method.class_ = &caller->class_;
   caller->method.name = (CharP)"fixtures.TCIRCaller.callAdd:(II)I";
   caller->method.paramCount = 2U;
   caller->method.paramRegs = caller->parameter_registers;
   caller->parameter_registers[0] = RegI;
   caller->parameter_registers[1] = RegI;
   caller->method.cpReturn = 1U;
   caller->method.returnReg = RegI;
   caller->method.flags.isStatic = true;
}

static int testInterpreterToCompiledCall(void)
{
   RuntimeMethod callee;
   RuntimeMethod caller;
   RuntimeExecution execution;
   TCIRRuntimeStats stats;

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&callee, &tcir_converter_fixtures[0]);
   initializeCaller(&caller, &callee.method);
   REQUIRE(registerFixture(&callee, &tcir_converter_fixtures[0], NULL, NULL)
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_IR));
   execution = executeRuntimeMethod(&caller.method, 19, 23);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.ir_invocations == 1U);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_UNREGISTERED] == 1U);
   REQUIRE(tcirRuntimeReset());
   return 1;
}

#if defined(TCIR_RUNTIME_TEST_HAS_AOT)
static Method forwarding_target;

static TCCompiledStatus forwardingEntry(TCCompiledFrame *frame, TCCompiledResult *result)
{
   if (frame == NULL || frame->runtime == NULL
       || frame->runtime->abi_version != TC_RUNTIME_ABI_VERSION
       || frame->runtime->dispatch == NULL || forwarding_target == NULL)
      return TC_COMPILED_REJECTED;
   return frame->runtime->dispatch(
      frame->runtime->context,
      forwarding_target,
      NULL,
      frame->arguments,
      frame->argument_count,
      result);
}

static TCCompiledStatus throwingEntry(TCCompiledFrame *frame, TCCompiledResult *result)
{
   Context context;

   if (frame == NULL || frame->runtime == NULL || result == NULL)
      return TC_COMPILED_REJECTED;
   context = (Context)frame->runtime->context;
   if (context == NULL)
      return TC_COMPILED_REJECTED;
   context->thrownException = (TCObject)(uintptr_t)1U;
   result->status = TC_COMPILED_THROWN;
   result->type = TCIR_TYPE_VOID;
   result->tc_pc = 1U;
   return TC_COMPILED_THROWN;
}

#if defined(_WIN32)
static HANDLE blocking_started;
static HANDLE blocking_release;
#else
static pthread_mutex_t blocking_mutex;
static pthread_cond_t blocking_condition;
static int blocking_started;
static int blocking_release;
#endif

typedef struct BlockingExecution
{
   Method method;
   RuntimeExecution result;
} BlockingExecution;

static TCCompiledStatus blockingEntry(TCCompiledFrame *frame, TCCompiledResult *result)
{
   if (frame == NULL || result == NULL || frame->argument_count != 2U)
      return TC_COMPILED_REJECTED;
#if defined(_WIN32)
   SetEvent(blocking_started);
   if (WaitForSingleObject(blocking_release, INFINITE) != WAIT_OBJECT_0)
      return TC_COMPILED_REJECTED;
#else
   pthread_mutex_lock(&blocking_mutex);
   blocking_started = 1;
   pthread_cond_broadcast(&blocking_condition);
   while (!blocking_release)
      pthread_cond_wait(&blocking_condition, &blocking_mutex);
   pthread_mutex_unlock(&blocking_mutex);
#endif
   result->status = TC_COMPILED_RETURNED;
   result->type = TCIR_TYPE_I32;
   result->value.i32 = frame->arguments[0].i32 + frame->arguments[1].i32;
   result->tc_pc = TCIR_TCPC_NONE;
   return TC_COMPILED_RETURNED;
}

#if defined(_WIN32)
static DWORD WINAPI blockingExecute(LPVOID argument)
#else
static void *blockingExecute(void *argument)
#endif
{
   BlockingExecution *execution = (BlockingExecution *)argument;
   execution->result = executeRuntimeMethod(execution->method, 19, 23);
#if defined(_WIN32)
   return 0;
#else
   return NULL;
#endif
}

static void nativeAddAsDouble(NMParams parameters)
{
   parameters->retD = (double)parameters->i32[0] + (double)parameters->i32[1];
}

static void initializeNativeTarget(RuntimeMethod *target)
{
   memset(target, 0, sizeof(*target));
   target->class_.cp = &target->constant_pool;
   target->class_.name = (CharP)"fixtures.TCIRNative";
   target->code[0].op.op = BREAK;
   target->method.iCount = 2U;
   target->method.oCount = 1U;
   target->method.code = target->code;
   target->method.class_ = &target->class_;
   target->method.name = (CharP)"fixtures.TCIRNative.add:(II)D";
   target->method.paramCount = 2U;
   target->method.paramRegs = target->parameter_registers;
   target->parameter_registers[0] = RegI;
   target->parameter_registers[1] = RegI;
   target->method.cpReturn = 1U;
   target->method.returnReg = RegD;
   target->method.flags.isStatic = true;
   target->method.flags.isNative = true;
   target->method.boundNM = nativeAddAsDouble;
}

static int testCompiledCallThunks(void)
{
   const TCIRAotRegistryEntry *aot_entry = findAotEntry(0U);
   RuntimeMethod wrapper;
   RuntimeMethod target;
   RuntimeExecution execution;
   TCIRRuntimeStats stats;

   REQUIRE(aot_entry != NULL);

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&wrapper, &tcir_converter_fixtures[0]);
   initializeFixtureMethod(&target, &tcir_converter_fixtures[0]);
   forwarding_target = &target.method;
   REQUIRE(registerFixture(&wrapper, &tcir_converter_fixtures[0], forwardingEntry, "forward-interpreter")
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
   execution = executeRuntimeMethod(&wrapper.method, 19, 23);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.aot_invocations == 1U && stats.call_thunks == 1U);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_UNREGISTERED] == 1U);

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&wrapper, &tcir_converter_fixtures[0]);
   initializeFixtureMethod(&target, &tcir_converter_fixtures[0]);
   forwarding_target = &target.method;
   REQUIRE(registerFixture(&wrapper, &tcir_converter_fixtures[0], forwardingEntry, "forward-compiled")
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(registerFixture(&target, &tcir_converter_fixtures[0],
                           aot_entry->entry, aot_entry->content_hash)
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
   execution = executeRuntimeMethod(&wrapper.method, 20, 22);
   REQUIRE(execution.value.asInt32 == 42 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.aot_invocations == 2U && stats.call_thunks == 1U);

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&wrapper, &tcir_converter_fixtures[0]);
   initializeNativeTarget(&target);
   wrapper.method.returnReg = RegD;
   forwarding_target = &target.method;
   REQUIRE(registerFixture(&wrapper, &tcir_converter_fixtures[0], forwardingEntry, "forward-native")
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
   execution = executeRuntimeMethod(&wrapper.method, 19, 23);
   REQUIRE(execution.value.asDouble == 42.0 && execution.frame_restored && execution.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.aot_invocations == 1U && stats.call_thunks == 1U);
   forwarding_target = NULL;
   REQUIRE(tcirRuntimeReset());
   return 1;
}

static int testThrownStatusHandoff(void)
{
   enum { CAPACITY = 64 };
   RuntimeMethod runtime_method;
   TContext context;
   int32 register_i32[CAPACITY];
   TCObject register_ref[CAPACITY];
   int64 register_v64[CAPACITY];
   VoidP call_stack[CAPACITY];
   TCCompiledResult result;
   TCIRRuntimeDiagnostic diagnostic;
   TCIRRuntimeStats stats;

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&runtime_method, &tcir_converter_fixtures[0]);
   REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0], throwingEntry, "throwing-entry")
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
   initializeContext(&context, register_i32, register_ref, register_v64, call_stack, CAPACITY);
   register_i32[0] = 19;
   register_i32[1] = 23;
   REQUIRE(tcirRuntimeTryDispatch(&context, &runtime_method.method,
                                  NULL, register_ref, register_v64,
                                  &result, &diagnostic)
           == TCIR_RUNTIME_DISPATCH_FALLBACK);
   REQUIRE(diagnostic.fallback_reason == TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED);
   REQUIRE(tcirRuntimeTryDispatch(&context, &runtime_method.method,
                                  register_i32, register_ref, register_v64,
                                  &result, &diagnostic)
           == TCIR_RUNTIME_DISPATCH_THROWN);
   REQUIRE(result.status == TC_COMPILED_THROWN && result.tc_pc == 1U);
   REQUIRE(context.thrownException != null);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.dispatch_throws == 1U && stats.aot_invocations == 1U);
   REQUIRE(stats.fallback_counts[TCIR_RUNTIME_FALLBACK_INVOCATION_REJECTED] == 1U);
   context.thrownException = null;
   DESTROY_MUTEX(context.usageLock);
   REQUIRE(tcirRuntimeReset());
   return 1;
}

static int testShutdownDuringActiveDispatch(void)
{
   RuntimeMethod runtime_method;
   BlockingExecution execution;
   TCIRRuntimeStats stats;
#if defined(_WIN32)
   HANDLE thread;
#else
   pthread_t thread;
#endif

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&runtime_method, &tcir_converter_fixtures[0]);
   REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0], blockingEntry, "blocking-entry")
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_AOT));
   memset(&execution, 0, sizeof(execution));
   execution.method = &runtime_method.method;
#if defined(_WIN32)
   blocking_started = CreateEvent(NULL, TRUE, FALSE, NULL);
   blocking_release = CreateEvent(NULL, TRUE, FALSE, NULL);
   REQUIRE(blocking_started != NULL && blocking_release != NULL);
   thread = CreateThread(NULL, 0U, blockingExecute, &execution, 0U, NULL);
   REQUIRE(thread != NULL);
   REQUIRE(WaitForSingleObject(blocking_started, INFINITE) == WAIT_OBJECT_0);
#else
   REQUIRE(pthread_mutex_init(&blocking_mutex, NULL) == 0);
   REQUIRE(pthread_cond_init(&blocking_condition, NULL) == 0);
   blocking_started = 0;
   blocking_release = 0;
   REQUIRE(pthread_create(&thread, NULL, blockingExecute, &execution) == 0);
   REQUIRE(pthread_mutex_lock(&blocking_mutex) == 0);
   while (!blocking_started)
      REQUIRE(pthread_cond_wait(&blocking_condition, &blocking_mutex) == 0);
   REQUIRE(pthread_mutex_unlock(&blocking_mutex) == 0);
#endif

   tcirRuntimeShutdown();
#if defined(_WIN32)
   SetEvent(blocking_release);
   REQUIRE(WaitForSingleObject(thread, INFINITE) == WAIT_OBJECT_0);
   CloseHandle(thread);
   CloseHandle(blocking_started);
   CloseHandle(blocking_release);
#else
   REQUIRE(pthread_mutex_lock(&blocking_mutex) == 0);
   blocking_release = 1;
   REQUIRE(pthread_cond_broadcast(&blocking_condition) == 0);
   REQUIRE(pthread_mutex_unlock(&blocking_mutex) == 0);
   REQUIRE(pthread_join(thread, NULL) == 0);
   REQUIRE(pthread_cond_destroy(&blocking_condition) == 0);
   REQUIRE(pthread_mutex_destroy(&blocking_mutex) == 0);
#endif
   REQUIRE(execution.result.value.asInt32 == 42
           && execution.result.frame_restored && execution.result.usage_released);
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.aot_invocations == 1U && stats.dispatch_returns == 1U);
   REQUIRE(tcirRuntimeReset());
   return 1;
}
#endif

#if defined(TCIR_RUNTIME_TEST_HAS_SLJIT)
typedef struct ConcurrentExecution
{
   Method method;
   int accepted;
} ConcurrentExecution;

#if defined(_WIN32)
static DWORD WINAPI concurrentExecute(LPVOID argument)
#else
static void *concurrentExecute(void *argument)
#endif
{
   ConcurrentExecution *execution = (ConcurrentExecution *)argument;
   RuntimeExecution result = executeRuntimeMethod(execution->method, 19, 23);
   execution->accepted = result.value.asInt32 == 42 && result.frame_restored && result.usage_released;
#if defined(_WIN32)
   return 0;
#else
   return NULL;
#endif
}

static int testConcurrentDispatch(void)
{
   enum { THREAD_COUNT = 8 };
   RuntimeMethod runtime_method;
   ConcurrentExecution executions[THREAD_COUNT];
   TCIRRuntimeStats stats;
   size_t index;
#if defined(_WIN32)
   HANDLE threads[THREAD_COUNT];
#else
   pthread_t threads[THREAD_COUNT];
#endif

   REQUIRE(tcirRuntimeReset());
   initializeFixtureMethod(&runtime_method, &tcir_converter_fixtures[0]);
   REQUIRE(registerFixture(&runtime_method, &tcir_converter_fixtures[0], NULL, NULL)
           == TCIR_RUNTIME_REGISTRATION_READY);
   REQUIRE(tcirRuntimeSetBackend(TCIR_RUNTIME_BACKEND_JIT));
   memset(executions, 0, sizeof(executions));
   for (index = 0U; index < THREAD_COUNT; ++index)
   {
      executions[index].method = &runtime_method.method;
#if defined(_WIN32)
      threads[index] = CreateThread(NULL, 0U, concurrentExecute, &executions[index], 0U, NULL);
      REQUIRE(threads[index] != NULL);
#else
      REQUIRE(pthread_create(&threads[index], NULL, concurrentExecute, &executions[index]) == 0);
#endif
   }
   for (index = 0U; index < THREAD_COUNT; ++index)
   {
#if defined(_WIN32)
      REQUIRE(WaitForSingleObject(threads[index], INFINITE) == WAIT_OBJECT_0);
      CloseHandle(threads[index]);
#else
      REQUIRE(pthread_join(threads[index], NULL) == 0);
#endif
      REQUIRE(executions[index].accepted);
   }
   tcirRuntimeGetStats(&stats);
   REQUIRE(stats.jit_compilations == 1U);
   REQUIRE(stats.jit_invocations >= 1U);
   REQUIRE(stats.dispatch_returns == stats.jit_invocations);
   REQUIRE(tcirRuntimeReset());
   return 1;
}
#else
static int testConcurrentDispatch(void)
{
   return 1;
}
#endif

int main(void)
{
   int accepted = testPolicyAndObservability()
      && testBackendPolicies()
      && testRegistrationAndForcedFallback()
      && testInterpreterToCompiledCall()
#if defined(TCIR_RUNTIME_TEST_HAS_AOT)
      && testCompiledCallThunks()
      && testThrownStatusHandoff()
      && testShutdownDuringActiveDispatch()
#endif
      && testConcurrentDispatch();

   tcirRuntimeShutdown();
   if (!accepted)
      return 1;
   printf("TCIR runtime dispatch tests passed with conditional integration and mixed-call thunks.\n");
   return 0;
}
