// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_frontend.h"
#include "tcir_interp.h"
#include "tcir_jit.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#if defined(_WIN32)
#include <windows.h>
#else
#include <pthread.h>
#if defined(__APPLE__)
#include <mach/mach.h>
#include <mach/mach_vm.h>
#endif
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

typedef struct TCIRJitThreadResult
{
   TCIRJitCache *cache;
   const void *method_key;
   TCIRJitCacheStatus status;
   const TCIRJitArtifact *artifact;
   TCIRJitClaim *claim;
} TCIRJitThreadResult;

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

static int invokeInterpreter(
   const TCIRFunction *function,
   const TCIRConverterFixture *fixture,
   int32_t first,
   int32_t second,
   int32_t *value)
{
   TCIRRuntimeValue arguments[2];
   TCIRInterpreterFrame frame;
   TCIRInterpreterResult result;
   TCIRDiagnostic diagnostic;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];

   memset(arguments, 0, sizeof(arguments));
   arguments[0].i32 = first;
   arguments[1].i32 = second;
   memset(i32_homes, 0, sizeof(i32_homes));
   memset(ref_homes, 0, sizeof(ref_homes));
   memset(v64_homes, 0, sizeof(v64_homes));
   memset(&frame, 0, sizeof(frame));
   frame.i32_homes = i32_homes;
   frame.i32_home_count = fixture->i32_count;
   frame.ref_homes = ref_homes;
   frame.ref_home_count = fixture->ref_count;
   frame.v64_homes = v64_homes;
   frame.v64_home_count = fixture->v64_count;
   frame.arguments = arguments;
   frame.argument_count = fixture->parameter_count;
   frame.tc_pc = TCIR_TCPC_NONE;
   if (tcirInterpretFunction(function, &frame, NULL, &result, &diagnostic) != TCIR_INTERPRETER_RETURNED)
      return 0;
   *value = result.value.i32;
   return 1;
}

static int invokeJit(
   const TCIRJitArtifact *artifact,
   const TCIRConverterFixture *fixture,
   int32_t first,
   int32_t second,
   int32_t *value)
{
   TCIRRuntimeValue arguments[2];
   TCCompiledFrame frame;
   TCCompiledResult result;
   TCIRJitDiagnostic diagnostic;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];

   memset(arguments, 0, sizeof(arguments));
   arguments[0].i32 = first;
   arguments[1].i32 = second;
   memset(i32_homes, 0, sizeof(i32_homes));
   memset(ref_homes, 0, sizeof(ref_homes));
   memset(v64_homes, 0, sizeof(v64_homes));
   memset(&frame, 0, sizeof(frame));
   frame.i32_homes = i32_homes;
   frame.i32_home_count = fixture->i32_count;
   frame.ref_homes = ref_homes;
   frame.ref_home_count = fixture->ref_count;
   frame.v64_homes = v64_homes;
   frame.v64_home_count = fixture->v64_count;
   frame.arguments = arguments;
   frame.argument_count = fixture->parameter_count;
   frame.tc_pc = TCIR_TCPC_NONE;
   if (tcirJitInvoke(artifact, &frame, &result, &diagnostic) != TC_COMPILED_RETURNED
       || result.type != TCIR_TYPE_I32
       || frame.scratch_values != NULL
       || frame.edge_values != NULL)
      return 0;
   *value = result.value.i32;
   return 1;
}

static int executableAddressIsNotWritable(const void *address)
{
#if defined(_WIN32)
   MEMORY_BASIC_INFORMATION information;
   DWORD protection;
   if (VirtualQuery(address, &information, sizeof(information)) == 0U)
      return 0;
   protection = information.Protect & 0xffU;
   return (protection == PAGE_EXECUTE || protection == PAGE_EXECUTE_READ)
      && protection != PAGE_EXECUTE_READWRITE && protection != PAGE_EXECUTE_WRITECOPY;
#elif defined(__APPLE__)
   mach_vm_address_t region_address = (mach_vm_address_t)(uintptr_t)address;
   mach_vm_size_t region_size = 0;
   vm_region_basic_info_data_64_t information;
   mach_msg_type_number_t information_count = VM_REGION_BASIC_INFO_COUNT_64;
   memory_object_name_t object_name = MACH_PORT_NULL;
   kern_return_t status = mach_vm_region(
      mach_task_self(),
      &region_address,
      &region_size,
      VM_REGION_BASIC_INFO_64,
      (vm_region_info_t)&information,
      &information_count,
      &object_name);
   return status == KERN_SUCCESS
      && (information.protection & VM_PROT_EXECUTE) != 0
      && (information.protection & VM_PROT_WRITE) == 0;
#else
   FILE *maps = fopen("/proc/self/maps", "r");
   char line[512];
   uintptr_t target = (uintptr_t)address;
   if (maps == NULL)
      return 0;
   while (fgets(line, sizeof(line), maps) != NULL)
   {
      unsigned long long start;
      unsigned long long end;
      char permissions[5];
      if (sscanf(line, "%llx-%llx %4s", &start, &end, permissions) == 3
          && target >= (uintptr_t)start && target < (uintptr_t)end)
      {
         int accepted = permissions[2] == 'x' && permissions[1] != 'w';
         fclose(maps);
         return accepted;
      }
   }
   fclose(maps);
   return 0;
#endif
}

static int testForcedJitCorpus(TCIRModule *module, TCIRFunction **functions, TCIRJitArtifact **artifacts)
{
   static const int32_t add_cases[][2] = {
      { 0, 0 }, { 1, -1 }, { 42, 58 }, { -42, -58 }, { INT32_MAX, 1 }, { INT32_MIN, -1 }
   };
   static const int32_t abs_cases[] = { 0, 1, -1, 42, -42, INT32_MAX, INT32_MIN };
   static const int32_t sum_cases[] = { INT32_MIN, -1, 0, 1, 2, 10, 100, 4096, 65537 };
   TCIRDiagnostic frontend_diagnostic;
   TCIRJitDiagnostic jit_diagnostic;
   clock_t compile_ticks = 0;
   size_t fixture_index;
   size_t case_index;

   for (fixture_index = 0U; fixture_index < TCIR_CONVERTER_FIXTURE_COUNT; ++fixture_index)
   {
      TCIRMethodParameter parameters[2];
      TCIRMethodView view;
      clock_t start;
      REQUIRE(buildFixtureView(&tcir_converter_fixtures[fixture_index], &view, parameters));
      REQUIRE(tcirFrontendBuildFunction(module, &view, &functions[fixture_index], &frontend_diagnostic)
              == TCIR_FRONTEND_OK);
      REQUIRE(tcirJitCheckEligibility(functions[fixture_index], &jit_diagnostic) == TCIR_JIT_COMPILE_READY);
      start = clock();
      REQUIRE(tcirJitCompile(functions[fixture_index], NULL, &artifacts[fixture_index], &jit_diagnostic)
              == TCIR_JIT_COMPILE_READY);
      compile_ticks += clock() - start;
      REQUIRE(artifacts[fixture_index] != NULL);
      REQUIRE(tcirJitArtifactCodeSize(artifacts[fixture_index]) > 0U);
      REQUIRE(tcirJitArtifactMemoryPolicy(artifacts[fixture_index]) == TCIR_JIT_MEMORY_WX);
      REQUIRE(executableAddressIsNotWritable(tcirJitArtifactCodeAddress(artifacts[fixture_index])));
   }

   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
   {
      int32_t interpreted;
      int32_t compiled;
      REQUIRE(invokeInterpreter(functions[0], &tcir_converter_fixtures[0],
                                add_cases[case_index][0], add_cases[case_index][1], &interpreted));
      REQUIRE(invokeJit(artifacts[0], &tcir_converter_fixtures[0],
                        add_cases[case_index][0], add_cases[case_index][1], &compiled));
      REQUIRE(interpreted == compiled);
   }
   for (case_index = 0U; case_index < sizeof(abs_cases) / sizeof(abs_cases[0]); ++case_index)
   {
      int32_t interpreted;
      int32_t compiled;
      REQUIRE(invokeInterpreter(functions[1], &tcir_converter_fixtures[1], abs_cases[case_index], 0, &interpreted));
      REQUIRE(invokeJit(artifacts[1], &tcir_converter_fixtures[1], abs_cases[case_index], 0, &compiled));
      REQUIRE(interpreted == compiled);
   }
   for (case_index = 0U; case_index < sizeof(sum_cases) / sizeof(sum_cases[0]); ++case_index)
   {
      int32_t interpreted;
      int32_t compiled;
      REQUIRE(invokeInterpreter(functions[2], &tcir_converter_fixtures[2], sum_cases[case_index], 0, &interpreted));
      REQUIRE(invokeJit(artifacts[2], &tcir_converter_fixtures[2], sum_cases[case_index], 0, &compiled));
      REQUIRE(interpreted == compiled);
   }

   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
   {
      int32_t interpreted;
      int32_t compiled;
      REQUIRE(invokeInterpreter(functions[3], &tcir_converter_fixtures[3],
                                add_cases[case_index][0], add_cases[case_index][1], &interpreted));
      REQUIRE(invokeJit(artifacts[3], &tcir_converter_fixtures[3],
                        add_cases[case_index][0], add_cases[case_index][1], &compiled));
      REQUIRE(interpreted == compiled);
   }

   printf(
      "SLJIT observations: platform=%s, code_bytes={%lu,%lu,%lu,%lu}, "
      "compile_cpu_seconds=%.9f (%lu ticks at %lu ticks/second).\n",
      tcirJitPlatformName(),
      (unsigned long)tcirJitArtifactCodeSize(artifacts[0]),
      (unsigned long)tcirJitArtifactCodeSize(artifacts[1]),
      (unsigned long)tcirJitArtifactCodeSize(artifacts[2]),
      (unsigned long)tcirJitArtifactCodeSize(artifacts[3]),
      (double)compile_ticks / (double)CLOCKS_PER_SEC,
      (unsigned long)compile_ticks,
      (unsigned long)CLOCKS_PER_SEC);
   return 1;
}

static TCIRFunction *buildInvalidFunction(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   static const unsigned char starts[] = { 1U };
   TCIRFunction *function = tcirModuleAddFunction(
      module, "Jit.invalid:()I", NULL, 0U, TCIR_TYPE_I32, diagnostic);
   if (function == NULL
       || tcirFunctionSetSourceSlots(function, 1U, starts, diagnostic) != TCIR_STATUS_OK
       || tcirFunctionAppendBlock(function, 0U, (TCIRSourceLocation){ 0U, 1 }, 0, diagnostic) == NULL)
      return NULL;
   return function;
}

static TCIRFunction *buildNullCheckFunction(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   static const unsigned int code[] = { 0x0000007aU, 0x00000088U };
   TCIRMethodParameter parameter;
   TCIRMethodView view;
   TCIRFunction *function = NULL;
   memset(&parameter, 0, sizeof(parameter));
   parameter.type = TCIR_TYPE_REF;
   parameter.home_bank = TCIR_HOME_REF;
   memset(&view, 0, sizeof(view));
   view.identity = "Jit.checkedRef:(Ljava/lang/Object;)V";
   view.code = code;
   view.code_slot_count = sizeof(code) / sizeof(code[0]);
   view.ref_home_count = 1U;
   view.parameters = &parameter;
   view.parameter_count = 1U;
   view.return_type = TCIR_TYPE_VOID;
   return tcirFrontendBuildFunction(module, &view, &function, diagnostic) == TCIR_FRONTEND_OK
      ? function : NULL;
}

static int testRejectionAndCleanup(TCIRModule *module, const TCIRFunction *valid_function)
{
   TCIRDiagnostic ir_diagnostic;
   TCIRJitDiagnostic jit_diagnostic;
   TCIRJitCompileOptions options;
   TCIRJitArtifact *artifact = NULL;
   TCIRFunction *null_check = buildNullCheckFunction(module, &ir_diagnostic);
   TCIRFunction *invalid = buildInvalidFunction(module, &ir_diagnostic);
   int32_t untouched_homes[] = { 0x12345678, -12345 };
   size_t index;

   REQUIRE(null_check != NULL && tcirVerifyFunction(null_check, &ir_diagnostic));
   REQUIRE(tcirJitCompile(null_check, NULL, &artifact, &jit_diagnostic) == TCIR_JIT_COMPILE_INELIGIBLE);
   REQUIRE(artifact == NULL);
   REQUIRE(jit_diagnostic.code == TCIR_JIT_DIAGNOSTIC_INELIGIBLE_OPERATION);
   REQUIRE(invalid != NULL && !tcirVerifyFunction(invalid, &ir_diagnostic));
   REQUIRE(tcirJitCompile(invalid, NULL, &artifact, &jit_diagnostic)
           == TCIR_JIT_COMPILE_VERIFICATION_FAILED);
   REQUIRE(artifact == NULL);
   REQUIRE(untouched_homes[0] == 0x12345678 && untouched_homes[1] == -12345);

   memset(&options, 0, sizeof(options));
   options.emission_limit = 2U;
   REQUIRE(tcirJitCompile(valid_function, &options, &artifact, &jit_diagnostic)
           == TCIR_JIT_COMPILE_EMISSION_FAILED);
   REQUIRE(artifact == NULL);
   REQUIRE(jit_diagnostic.code == TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED);

   for (index = 0U; index < 64U; ++index)
   {
      REQUIRE(tcirJitCompile(valid_function, NULL, &artifact, &jit_diagnostic) == TCIR_JIT_COMPILE_READY);
      REQUIRE(artifact != NULL);
      tcirJitArtifactDestroy(artifact);
      artifact = NULL;
   }
   return 1;
}

#if defined(_WIN32)
static DWORD WINAPI cacheLookupThread(LPVOID argument)
#else
static void *cacheLookupThread(void *argument)
#endif
{
   TCIRJitThreadResult *thread_result = (TCIRJitThreadResult *)argument;
   TCIRJitDiagnostic diagnostic;
   thread_result->status = tcirJitCacheBegin(
      thread_result->cache,
      thread_result->method_key,
      &thread_result->artifact,
      &thread_result->claim,
      &diagnostic);
#if defined(_WIN32)
   return 0;
#else
   return NULL;
#endif
}

static int testConcurrentPublication(
   const TCIRFunction *function,
   const TCIRConverterFixture *fixture)
{
   static const int method_key = 42;
   TCIRJitCache *cache = tcirJitCacheCreate();
   TCIRJitClaim *claim = NULL;
   const TCIRJitArtifact *cached_artifact = NULL;
   TCIRJitArtifact *artifact = NULL;
   TCIRJitDiagnostic diagnostic;
   TCIRJitThreadResult thread_result;
   int32_t interpreted_value;
   int32_t compiled_value;
#if defined(_WIN32)
   HANDLE thread;
#else
   pthread_t thread;
#endif

   REQUIRE(cache != NULL);
   REQUIRE(tcirJitCacheBegin(cache, &method_key, &cached_artifact, &claim, &diagnostic)
           == TCIR_JIT_CACHE_CLAIMED);
   REQUIRE(claim != NULL && cached_artifact == NULL);
   memset(&thread_result, 0, sizeof(thread_result));
   thread_result.cache = cache;
   thread_result.method_key = &method_key;
#if defined(_WIN32)
   thread = CreateThread(NULL, 0U, cacheLookupThread, &thread_result, 0U, NULL);
   REQUIRE(thread != NULL);
   REQUIRE(WaitForSingleObject(thread, INFINITE) == WAIT_OBJECT_0);
   CloseHandle(thread);
#else
   REQUIRE(pthread_create(&thread, NULL, cacheLookupThread, &thread_result) == 0);
   REQUIRE(pthread_join(thread, NULL) == 0);
#endif
   REQUIRE(thread_result.status == TCIR_JIT_CACHE_COMPILING);
   REQUIRE(thread_result.claim == NULL && thread_result.artifact == NULL);
   REQUIRE(invokeInterpreter(function, fixture, 19, 23, &interpreted_value));
   REQUIRE(interpreted_value == 42);
   REQUIRE(tcirJitCompile(function, NULL, &artifact, &diagnostic) == TCIR_JIT_COMPILE_READY);
   REQUIRE(tcirJitCachePublish(claim, artifact));
   artifact = NULL;
   claim = NULL;
   REQUIRE(tcirJitCacheBegin(cache, &method_key, &cached_artifact, &claim, &diagnostic)
           == TCIR_JIT_CACHE_READY);
   REQUIRE(cached_artifact != NULL && claim == NULL);
   REQUIRE(invokeJit(cached_artifact, fixture, 19, 23, &compiled_value));
   REQUIRE(compiled_value == 42);
   tcirJitCacheDestroy(cache);
   return 1;
}

static int testShutdownWithCompilationClaim(const TCIRFunction *function)
{
   static const int method_key = 84;
   TCIRJitCache *cache = tcirJitCacheCreate();
   TCIRJitClaim *claim = NULL;
   const TCIRJitArtifact *cached_artifact = NULL;
   TCIRJitArtifact *artifact = NULL;
   TCIRJitDiagnostic diagnostic;

   REQUIRE(cache != NULL);
   REQUIRE(tcirJitCacheBegin(cache, &method_key, &cached_artifact, &claim, &diagnostic)
           == TCIR_JIT_CACHE_CLAIMED);
   REQUIRE(claim != NULL && cached_artifact == NULL);
   tcirJitCacheDestroy(cache);
   REQUIRE(tcirJitCompile(function, NULL, &artifact, &diagnostic) == TCIR_JIT_COMPILE_READY);
   REQUIRE(!tcirJitCachePublish(claim, artifact));
   tcirJitArtifactDestroy(artifact);
   return 1;
}

int main(void)
{
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT] = { NULL };
   TCIRJitArtifact *artifacts[TCIR_CONVERTER_FIXTURE_COUNT] = { NULL };
   size_t index;
   int accepted;

   if (module == NULL)
      return 1;
   accepted = testForcedJitCorpus(module, functions, artifacts)
      && testRejectionAndCleanup(module, functions[0])
      && testConcurrentPublication(functions[0], &tcir_converter_fixtures[0])
      && testShutdownWithCompilationClaim(functions[0]);
   for (index = 0U; index < TCIR_CONVERTER_FIXTURE_COUNT; ++index)
      tcirJitArtifactDestroy(artifacts[index]);
   tcirModuleDestroy(module);
   if (!accepted)
      return 1;
   printf("SLJIT backend tests passed: forced execution, W^X, rejection, cleanup, concurrency, shutdown.\n");
   return 0;
}
