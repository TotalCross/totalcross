// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_frontend.h"
#include "tcir_interp.h"
#include "tcir_jit.h"
#include "tcvm.h"

#include <inttypes.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#if defined(_WIN32)
#include <windows.h>
#else
#include <sys/utsname.h>
#include <unistd.h>
#if defined(__APPLE__)
#include <mach/mach_time.h>
#include <sys/sysctl.h>
#else
#include <sys/sysinfo.h>
#endif
#endif

#ifndef TC_BENCHMARK_GIT_REVISION
#define TC_BENCHMARK_GIT_REVISION "unknown"
#endif
#ifndef TC_BENCHMARK_COMPILER_ID
#define TC_BENCHMARK_COMPILER_ID "unknown"
#endif
#ifndef TC_BENCHMARK_COMPILER_VERSION
#define TC_BENCHMARK_COMPILER_VERSION "unknown"
#endif
#ifndef TC_BENCHMARK_BUILD_TYPE
#define TC_BENCHMARK_BUILD_TYPE "unknown"
#endif
#ifndef TC_BENCHMARK_GENERATOR
#define TC_BENCHMARK_GENERATOR "unknown"
#endif
#ifndef TC_BENCHMARK_TARGET_PROCESSOR
#define TC_BENCHMARK_TARGET_PROCESSOR "unknown"
#endif
#ifndef TC_BENCHMARK_C_FLAGS
#define TC_BENCHMARK_C_FLAGS "unknown"
#endif
#ifndef TC_BENCHMARK_CONFIG_WARMUP_COUNT
#define TC_BENCHMARK_CONFIG_WARMUP_COUNT 5
#endif
#ifndef TC_BENCHMARK_CONFIG_SAMPLE_COUNT
#define TC_BENCHMARK_CONFIG_SAMPLE_COUNT 60
#endif

enum
{
   TC_BENCHMARK_BACKEND_COUNT = 3,
   TC_BENCHMARK_FIXTURE_COUNT = 3,
   TC_BENCHMARK_WARMUP_COUNT = TC_BENCHMARK_CONFIG_WARMUP_COUNT,
   TC_BENCHMARK_SAMPLE_COUNT = TC_BENCHMARK_CONFIG_SAMPLE_COUNT,
   TC_BENCHMARK_FRAME_CAPACITY = 16
};

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

typedef enum BenchmarkBackend
{
   BENCHMARK_TCODE = 0,
   BENCHMARK_TCIR,
   BENCHMARK_SLJIT
} BenchmarkBackend;

typedef struct BenchmarkOptions
{
   const char *json_path;
   const char *csv_path;
   const char *power_mode;
   const char *background_load;
   const char *dirty_paths;
} BenchmarkOptions;

typedef struct BenchmarkHost
{
   char os[128];
   char kernel[128];
   char architecture[128];
   char cpu_model[256];
   uint64_t logical_cpu_count;
   uint64_t memory_bytes;
} BenchmarkHost;

typedef struct BenchmarkStats
{
   double mean;
   double median;
   double standard_deviation;
   uint64_t minimum;
   uint64_t maximum;
} BenchmarkStats;

typedef struct BenchmarkFixtureRunner
{
   const TCIRConverterFixture *fixture;
   TCIRFunction *function;
   TCIRJitArtifact *artifact;

   TCode code[16];
   TMethod method;
   TTCClass class_;
   TConstantPool constant_pool;
   uint8_t parameter_registers[2];
   TContext context;
   int32 register_i32[TC_BENCHMARK_FRAME_CAPACITY];
   TCObject register_ref[TC_BENCHMARK_FRAME_CAPACITY];
   int64 register_v64[TC_BENCHMARK_FRAME_CAPACITY];
   VoidP call_stack[TC_BENCHMARK_FRAME_CAPACITY];

   TCIRRuntimeValue arguments[2];
   int32_t tcir_i32_homes[TC_BENCHMARK_FRAME_CAPACITY];
   void *tcir_ref_homes[TC_BENCHMARK_FRAME_CAPACITY];
   TCIRV64Home tcir_v64_homes[TC_BENCHMARK_FRAME_CAPACITY];
   TCIRInterpreterFrame interpreter_frame;

   int32_t jit_i32_homes[TC_BENCHMARK_FRAME_CAPACITY];
   void *jit_ref_homes[TC_BENCHMARK_FRAME_CAPACITY];
   TCIRV64Home jit_v64_homes[TC_BENCHMARK_FRAME_CAPACITY];
   TCCompiledFrame compiled_frame;
   int context_initialized;
} BenchmarkFixtureRunner;

typedef struct BenchmarkWorkload
{
   const char *name;
   const char *input_description;
   size_t invocation_count;
   uint32_t seed;
   int32_t fixed_input;
} BenchmarkWorkload;

typedef struct BenchmarkResult
{
   uint64_t expected_checksum;
   uint64_t compile_nanoseconds[TC_BENCHMARK_SAMPLE_COUNT];
   size_t code_size;
   uint64_t execution_nanoseconds[TC_BENCHMARK_BACKEND_COUNT][TC_BENCHMARK_SAMPLE_COUNT];
   BenchmarkStats compile_stats;
   BenchmarkStats execution_stats[TC_BENCHMARK_BACKEND_COUNT];
} BenchmarkResult;

static const char *const benchmark_backend_names[TC_BENCHMARK_BACKEND_COUNT] = {
   "executeMethod", "tcir", "sljit"
};

static const unsigned int benchmark_orders[6][TC_BENCHMARK_BACKEND_COUNT] = {
   { BENCHMARK_TCODE, BENCHMARK_TCIR, BENCHMARK_SLJIT },
   { BENCHMARK_TCIR, BENCHMARK_SLJIT, BENCHMARK_TCODE },
   { BENCHMARK_SLJIT, BENCHMARK_TCODE, BENCHMARK_TCIR },
   { BENCHMARK_SLJIT, BENCHMARK_TCIR, BENCHMARK_TCODE },
   { BENCHMARK_TCIR, BENCHMARK_TCODE, BENCHMARK_SLJIT },
   { BENCHMARK_TCODE, BENCHMARK_SLJIT, BENCHMARK_TCIR }
};

static volatile uint64_t benchmark_sink;

static void benchmarkUsage(const char *program)
{
   fprintf(
      stderr,
      "usage: %s --json <path> --csv <path> --power-mode <note> "
      "--background-load <note> --dirty-paths <paths>\n",
      program);
}

static int benchmarkParseOptions(int argc, char **argv, BenchmarkOptions *options)
{
   int index;

   memset(options, 0, sizeof(*options));
   for (index = 1; index < argc; ++index)
   {
      const char **target = NULL;
      if (strcmp(argv[index], "--json") == 0)
         target = &options->json_path;
      else if (strcmp(argv[index], "--csv") == 0)
         target = &options->csv_path;
      else if (strcmp(argv[index], "--power-mode") == 0)
         target = &options->power_mode;
      else if (strcmp(argv[index], "--background-load") == 0)
         target = &options->background_load;
      else if (strcmp(argv[index], "--dirty-paths") == 0)
         target = &options->dirty_paths;
      else
         return 0;
      if (++index >= argc)
         return 0;
      *target = argv[index];
   }
   return options->json_path != NULL
      && options->csv_path != NULL
      && options->power_mode != NULL
      && options->background_load != NULL
      && options->dirty_paths != NULL;
}

static uint64_t benchmarkNowNanoseconds(void)
{
#if defined(_WIN32)
   LARGE_INTEGER counter;
   LARGE_INTEGER frequency;
   QueryPerformanceCounter(&counter);
   QueryPerformanceFrequency(&frequency);
   return (uint64_t)((double)counter.QuadPart * 1000000000.0 / (double)frequency.QuadPart);
#elif defined(__APPLE__)
   static mach_timebase_info_data_t timebase;
   uint64_t ticks;
   if (timebase.denom == 0U)
      (void)mach_timebase_info(&timebase);
   ticks = mach_continuous_time();
   return (uint64_t)((__uint128_t)ticks * timebase.numer / timebase.denom);
#else
   struct timespec timestamp;
   if (clock_gettime(CLOCK_MONOTONIC_RAW, &timestamp) != 0)
      return 0U;
   return (uint64_t)timestamp.tv_sec * UINT64_C(1000000000) + (uint64_t)timestamp.tv_nsec;
#endif
}

static void benchmarkTimestamp(char *buffer, size_t capacity)
{
   time_t now = time(NULL);
   struct tm utc;
#if defined(_WIN32)
   gmtime_s(&utc, &now);
#else
   gmtime_r(&now, &utc);
#endif
   strftime(buffer, capacity, "%Y-%m-%dT%H:%M:%SZ", &utc);
}

static void benchmarkHostInfo(BenchmarkHost *host)
{
   memset(host, 0, sizeof(*host));
#if defined(_WIN32)
   SYSTEM_INFO system_info;
   MEMORYSTATUSEX memory_status;
   OSVERSIONINFOA version;
   memset(&version, 0, sizeof(version));
   version.dwOSVersionInfoSize = sizeof(version);
   GetVersionExA(&version);
   snprintf(host->os, sizeof(host->os), "Windows");
   snprintf(host->kernel, sizeof(host->kernel), "%lu.%lu.%lu",
            (unsigned long)version.dwMajorVersion,
            (unsigned long)version.dwMinorVersion,
            (unsigned long)version.dwBuildNumber);
   snprintf(host->architecture, sizeof(host->architecture), "%s", TC_BENCHMARK_TARGET_PROCESSOR);
   snprintf(host->cpu_model, sizeof(host->cpu_model), "not recorded by benchmark");
   GetSystemInfo(&system_info);
   host->logical_cpu_count = (uint64_t)system_info.dwNumberOfProcessors;
   memset(&memory_status, 0, sizeof(memory_status));
   memory_status.dwLength = sizeof(memory_status);
   if (GlobalMemoryStatusEx(&memory_status))
      host->memory_bytes = (uint64_t)memory_status.ullTotalPhys;
#else
   struct utsname system_name;
   if (uname(&system_name) == 0)
   {
      snprintf(host->os, sizeof(host->os), "%s", system_name.sysname);
      snprintf(host->kernel, sizeof(host->kernel), "%s", system_name.release);
      snprintf(host->architecture, sizeof(host->architecture), "%s", system_name.machine);
   }
   else
   {
      snprintf(host->os, sizeof(host->os), "unknown");
      snprintf(host->kernel, sizeof(host->kernel), "unknown");
      snprintf(host->architecture, sizeof(host->architecture), "%s", TC_BENCHMARK_TARGET_PROCESSOR);
   }
   {
      long cpu_count = sysconf(_SC_NPROCESSORS_ONLN);
      if (cpu_count > 0)
         host->logical_cpu_count = (uint64_t)cpu_count;
   }
#if defined(__APPLE__)
   {
      size_t model_size = sizeof(host->cpu_model);
      uint64_t memory_size = 0U;
      size_t memory_size_length = sizeof(memory_size);
      if (sysctlbyname("machdep.cpu.brand_string", host->cpu_model, &model_size, NULL, 0) != 0)
      {
         model_size = sizeof(host->cpu_model);
         if (sysctlbyname("hw.model", host->cpu_model, &model_size, NULL, 0) != 0)
            snprintf(host->cpu_model, sizeof(host->cpu_model), "unknown");
      }
      if (sysctlbyname("hw.memsize", &memory_size, &memory_size_length, NULL, 0) == 0)
         host->memory_bytes = memory_size;
   }
#else
   {
      struct sysinfo information;
      if (sysinfo(&information) == 0)
         host->memory_bytes = (uint64_t)information.totalram * (uint64_t)information.mem_unit;
      snprintf(host->cpu_model, sizeof(host->cpu_model), "not recorded by benchmark");
   }
#endif
#endif
}

static int benchmarkBuildFixtureView(
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
   return 1;
}

static void benchmarkInitializeMethod(BenchmarkFixtureRunner *runner)
{
   size_t index;
   const TCIRConverterFixture *fixture = runner->fixture;

   memset(runner->code, 0, fixture->code_count * sizeof(*runner->code));
   for (index = 0U; index < fixture->code_count; ++index)
      runner->code[index].u32.u32 = (uint32)fixture->code[index];
   memset(&runner->constant_pool, 0, sizeof(runner->constant_pool));
   memset(&runner->class_, 0, sizeof(runner->class_));
   runner->class_.cp = &runner->constant_pool;
   runner->class_.name = (CharP)"fixtures.TCIRPoc";
   memset(&runner->method, 0, sizeof(runner->method));
   runner->method.iCount = (uint8)fixture->i32_count;
   runner->method.oCount = 1U;
   runner->method.v64Count = (uint8)fixture->v64_count;
   runner->method.code = runner->code;
   runner->method.class_ = &runner->class_;
   runner->method.name = (CharP)fixture->identity;
   runner->method.paramCount = (uint16)fixture->parameter_count;
   runner->method.paramRegs = runner->parameter_registers;
   runner->method.returnReg = RegI;
   runner->method.flags.isStatic = true;
   for (index = 0U; index < fixture->parameter_count; ++index)
      runner->parameter_registers[index] = (uint8)RegI;
}

static void benchmarkInitializeContext(BenchmarkFixtureRunner *runner)
{
   TContext *context = &runner->context;

   memset(context, 0, sizeof(*context));
   memset(runner->register_i32, 0, sizeof(runner->register_i32));
   memset(runner->register_ref, 0, sizeof(runner->register_ref));
   memset(runner->register_v64, 0, sizeof(runner->register_v64));
   memset(runner->call_stack, 0, sizeof(runner->call_stack));
   context->regI = context->regIStart = runner->register_i32;
   context->regIEnd = runner->register_i32 + TC_BENCHMARK_FRAME_CAPACITY;
   context->regO = context->regOStart = runner->register_ref;
   context->regOEnd = runner->register_ref + TC_BENCHMARK_FRAME_CAPACITY;
   context->reg64 = context->reg64Start = runner->register_v64;
   context->reg64End = runner->register_v64 + TC_BENCHMARK_FRAME_CAPACITY;
   context->callStack = context->callStackStart = runner->call_stack;
   context->callStackEnd = runner->call_stack + TC_BENCHMARK_FRAME_CAPACITY;
   context->nmp.currentContext = context;
   {
      SETUP_MUTEX;
      INIT_MUTEX(context->usageLock);
#if defined(POSIX) || defined(ANDROID)
      pthread_mutexattr_destroy(&mutex_attrs);
#endif
   }
}

static int benchmarkPrepareRunner(
   TCIRModule *module,
   const TCIRConverterFixture *fixture,
   BenchmarkFixtureRunner *runner)
{
   TCIRMethodParameter parameters[2];
   TCIRMethodView view;
   TCIRDiagnostic ir_diagnostic;
   TCIRJitDiagnostic jit_diagnostic;

   memset(runner, 0, sizeof(*runner));
   runner->fixture = fixture;
   if (!benchmarkBuildFixtureView(fixture, &view, parameters)
       || tcirFrontendBuildFunction(module, &view, &runner->function, &ir_diagnostic) != TCIR_FRONTEND_OK
       || tcirJitCompile(runner->function, NULL, &runner->artifact, &jit_diagnostic)
          != TCIR_JIT_COMPILE_READY)
      return 0;

   benchmarkInitializeMethod(runner);
   benchmarkInitializeContext(runner);
   runner->context_initialized = 1;

   memset(&runner->interpreter_frame, 0, sizeof(runner->interpreter_frame));
   runner->interpreter_frame.i32_homes = runner->tcir_i32_homes;
   runner->interpreter_frame.i32_home_count = fixture->i32_count;
   runner->interpreter_frame.ref_homes = runner->tcir_ref_homes;
   runner->interpreter_frame.ref_home_count = fixture->ref_count;
   runner->interpreter_frame.v64_homes = runner->tcir_v64_homes;
   runner->interpreter_frame.v64_home_count = fixture->v64_count;
   runner->interpreter_frame.arguments = runner->arguments;
   runner->interpreter_frame.argument_count = fixture->parameter_count;

   memset(&runner->compiled_frame, 0, sizeof(runner->compiled_frame));
   runner->compiled_frame.i32_homes = runner->jit_i32_homes;
   runner->compiled_frame.i32_home_count = fixture->i32_count;
   runner->compiled_frame.ref_homes = runner->jit_ref_homes;
   runner->compiled_frame.ref_home_count = fixture->ref_count;
   runner->compiled_frame.v64_homes = runner->jit_v64_homes;
   runner->compiled_frame.v64_home_count = fixture->v64_count;
   runner->compiled_frame.arguments = runner->arguments;
   runner->compiled_frame.argument_count = fixture->parameter_count;
   return 1;
}

static void benchmarkDestroyRunner(BenchmarkFixtureRunner *runner)
{
   if (runner->context_initialized)
      DESTROY_MUTEX(runner->context.usageLock);
   tcirJitArtifactDestroy(runner->artifact);
}

static int benchmarkInvokeTCode(
   BenchmarkFixtureRunner *runner,
   int32_t first,
   int32_t second,
   int32_t *value)
{
   TValue result;
   TContext *context = &runner->context;

   context->thrownException = null;
   if (runner->fixture->parameter_count == 2U)
      result = executeMethod(context, &runner->method, (int32)first, (int32)second);
   else
      result = executeMethod(context, &runner->method, (int32)first);
   if (context->thrownException != null
       || context->regI != context->regIStart
       || context->regO != context->regOStart
       || context->reg64 != context->reg64Start
       || context->callStack != context->callStackStart)
      return 0;
   *value = (int32_t)result.asInt32;
   return 1;
}

static int benchmarkInvokeTCIR(
   BenchmarkFixtureRunner *runner,
   int32_t first,
   int32_t second,
   int32_t *value)
{
   TCIRInterpreterResult result;
   TCIRDiagnostic diagnostic;

   runner->arguments[0].i32 = first;
   runner->arguments[1].i32 = second;
   runner->interpreter_frame.tc_pc = TCIR_TCPC_NONE;
   if (tcirInterpretFunction(runner->function, &runner->interpreter_frame, NULL, &result, &diagnostic)
       != TCIR_INTERPRETER_RETURNED
       || result.type != TCIR_TYPE_I32)
      return 0;
   *value = result.value.i32;
   return 1;
}

static int benchmarkInvokeSLJIT(
   BenchmarkFixtureRunner *runner,
   int32_t first,
   int32_t second,
   int32_t *value)
{
   TCCompiledResult result;
   TCIRJitDiagnostic diagnostic;

   runner->arguments[0].i32 = first;
   runner->arguments[1].i32 = second;
   runner->compiled_frame.tc_pc = TCIR_TCPC_NONE;
   if (tcirJitInvoke(runner->artifact, &runner->compiled_frame, &result, &diagnostic)
       != TC_COMPILED_RETURNED
       || result.type != TCIR_TYPE_I32
       || runner->compiled_frame.scratch_values != NULL
       || runner->compiled_frame.edge_values != NULL)
      return 0;
   *value = result.value.i32;
   return 1;
}

static uint32_t benchmarkNextValue(uint32_t value)
{
   value ^= value << 13;
   value ^= value >> 17;
   value ^= value << 5;
   return value;
}

static int32_t benchmarkI32FromBits(uint32_t bits)
{
   if (bits <= (uint32_t)INT32_MAX)
      return (int32_t)bits;
   return (int32_t)(-1 - (int32_t)(UINT32_MAX - bits));
}

static void benchmarkInputAt(
   const BenchmarkWorkload *workload,
   size_t index,
   int32_t *first,
   int32_t *second)
{
   uint32_t state = workload->seed ^ (uint32_t)index * UINT32_C(0x9e3779b9);
   if (workload->fixed_input != 0)
   {
      *first = workload->fixed_input;
      *second = 0;
      return;
   }
   state = benchmarkNextValue(state);
   *first = benchmarkI32FromBits(state);
   state = benchmarkNextValue(state);
   *second = benchmarkI32FromBits(state);
}

static uint64_t benchmarkChecksumUpdate(uint64_t checksum, int32_t value)
{
   checksum ^= (uint64_t)(uint32_t)value;
   checksum *= UINT64_C(1099511628211);
   return checksum;
}

static int benchmarkRunBatch(
   BenchmarkFixtureRunner *runner,
   const BenchmarkWorkload *workload,
   BenchmarkBackend backend,
   uint64_t *checksum)
{
   uint64_t value_checksum = UINT64_C(1469598103934665603);
   size_t index;

   for (index = 0U; index < workload->invocation_count; ++index)
   {
      int32_t first;
      int32_t second;
      int32_t value;
      int accepted;
      benchmarkInputAt(workload, index, &first, &second);
      if (backend == BENCHMARK_TCODE)
         accepted = benchmarkInvokeTCode(runner, first, second, &value);
      else if (backend == BENCHMARK_TCIR)
         accepted = benchmarkInvokeTCIR(runner, first, second, &value);
      else
         accepted = benchmarkInvokeSLJIT(runner, first, second, &value);
      if (!accepted)
         return 0;
      value_checksum = benchmarkChecksumUpdate(value_checksum, value);
   }
   benchmark_sink ^= value_checksum;
   *checksum = value_checksum;
   return 1;
}

static int benchmarkCompareU64(const void *left, const void *right)
{
   uint64_t left_value = *(const uint64_t *)left;
   uint64_t right_value = *(const uint64_t *)right;
   return left_value < right_value ? -1 : left_value > right_value;
}

static BenchmarkStats benchmarkCalculateStats(const uint64_t *values)
{
   uint64_t sorted[TC_BENCHMARK_SAMPLE_COUNT];
   long double sum = 0.0L;
   long double squared_difference_sum = 0.0L;
   BenchmarkStats stats;
   size_t index;

   memcpy(sorted, values, sizeof(sorted));
   qsort(sorted, TC_BENCHMARK_SAMPLE_COUNT, sizeof(sorted[0]), benchmarkCompareU64);
   for (index = 0U; index < TC_BENCHMARK_SAMPLE_COUNT; ++index)
      sum += (long double)values[index];
   memset(&stats, 0, sizeof(stats));
   stats.mean = (double)(sum / (long double)TC_BENCHMARK_SAMPLE_COUNT);
   stats.median = ((double)sorted[TC_BENCHMARK_SAMPLE_COUNT / 2U - 1U]
                   + (double)sorted[TC_BENCHMARK_SAMPLE_COUNT / 2U]) / 2.0;
   stats.minimum = sorted[0];
   stats.maximum = sorted[TC_BENCHMARK_SAMPLE_COUNT - 1U];
   for (index = 0U; index < TC_BENCHMARK_SAMPLE_COUNT; ++index)
   {
      long double difference = (long double)values[index] - (long double)stats.mean;
      squared_difference_sum += difference * difference;
   }
   stats.standard_deviation = sqrt((double)(squared_difference_sum
      / (long double)(TC_BENCHMARK_SAMPLE_COUNT - 1U)));
   return stats;
}

static int benchmarkCompileFixture(BenchmarkFixtureRunner *runner, BenchmarkResult *result)
{
   TCIRJitDiagnostic diagnostic;
   size_t index;

   for (index = 0U; index < TC_BENCHMARK_WARMUP_COUNT + TC_BENCHMARK_SAMPLE_COUNT; ++index)
   {
      TCIRJitArtifact *artifact = NULL;
      uint64_t start = benchmarkNowNanoseconds();
      TCIRJitCompileStatus status = tcirJitCompile(runner->function, NULL, &artifact, &diagnostic);
      uint64_t elapsed = benchmarkNowNanoseconds() - start;
      if (status != TCIR_JIT_COMPILE_READY || artifact == NULL)
         return 0;
      if (index >= TC_BENCHMARK_WARMUP_COUNT)
      {
         size_t sample = index - TC_BENCHMARK_WARMUP_COUNT;
         result->compile_nanoseconds[sample] = elapsed;
         if (result->code_size == 0U)
            result->code_size = tcirJitArtifactCodeSize(artifact);
         else if (result->code_size != tcirJitArtifactCodeSize(artifact))
         {
            tcirJitArtifactDestroy(artifact);
            return 0;
         }
      }
      tcirJitArtifactDestroy(artifact);
   }
   result->compile_stats = benchmarkCalculateStats(result->compile_nanoseconds);
   return 1;
}

static int benchmarkExecuteFixture(
   BenchmarkFixtureRunner *runner,
   const BenchmarkWorkload *workload,
   BenchmarkResult *result)
{
   uint64_t checksum;
   size_t warmup;
   size_t sample;

   if (!benchmarkRunBatch(runner, workload, BENCHMARK_TCODE, &result->expected_checksum))
      return 0;
   for (warmup = 0U; warmup < TC_BENCHMARK_WARMUP_COUNT; ++warmup)
   {
      size_t position;
      const unsigned int *order = benchmark_orders[warmup % 6U];
      for (position = 0U; position < TC_BENCHMARK_BACKEND_COUNT; ++position)
         if (!benchmarkRunBatch(runner, workload, (BenchmarkBackend)order[position], &checksum)
             || checksum != result->expected_checksum)
            return 0;
   }

   for (sample = 0U; sample < TC_BENCHMARK_SAMPLE_COUNT; ++sample)
   {
      size_t position;
      const unsigned int *order = benchmark_orders[sample % 6U];
      for (position = 0U; position < TC_BENCHMARK_BACKEND_COUNT; ++position)
      {
         BenchmarkBackend backend = (BenchmarkBackend)order[position];
         uint64_t start = benchmarkNowNanoseconds();
         int accepted = benchmarkRunBatch(runner, workload, backend, &checksum);
         uint64_t elapsed = benchmarkNowNanoseconds() - start;
         if (!accepted || checksum != result->expected_checksum)
            return 0;
         result->execution_nanoseconds[backend][sample] = elapsed;
      }
   }
   for (sample = 0U; sample < TC_BENCHMARK_BACKEND_COUNT; ++sample)
      result->execution_stats[sample] = benchmarkCalculateStats(result->execution_nanoseconds[sample]);
   return 1;
}

static void benchmarkWriteJsonString(FILE *output, const char *value)
{
   const unsigned char *cursor = (const unsigned char *)value;
   fputc('"', output);
   while (*cursor != 0U)
   {
      switch (*cursor)
      {
         case '"': fputs("\\\"", output); break;
         case '\\': fputs("\\\\", output); break;
         case '\b': fputs("\\b", output); break;
         case '\f': fputs("\\f", output); break;
         case '\n': fputs("\\n", output); break;
         case '\r': fputs("\\r", output); break;
         case '\t': fputs("\\t", output); break;
         default:
            if (*cursor < 0x20U)
               fprintf(output, "\\u%04x", (unsigned int)*cursor);
            else
               fputc((int)*cursor, output);
            break;
      }
      ++cursor;
   }
   fputc('"', output);
}

static void benchmarkWriteJsonStats(
   FILE *output,
   const BenchmarkStats *stats,
   size_t invocation_count)
{
   fprintf(
      output,
      "{\"unit\":\"nanoseconds per batch\",\"lower_is_better\":true,"
      "\"sample_count\":%u,\"mean\":%.3f,\"median\":%.3f,"
      "\"standard_deviation\":%.3f,\"minimum\":%" PRIu64 ",\"maximum\":%" PRIu64
      ",\"mean_nanoseconds_per_invocation\":%.6f}",
      TC_BENCHMARK_SAMPLE_COUNT,
      stats->mean,
      stats->median,
      stats->standard_deviation,
      stats->minimum,
      stats->maximum,
      stats->mean / (double)invocation_count);
}

static void benchmarkWriteJsonSamples(FILE *output, const uint64_t *samples)
{
   size_t index;
   fputc('[', output);
   for (index = 0U; index < TC_BENCHMARK_SAMPLE_COUNT; ++index)
   {
      if (index != 0U)
         fputc(',', output);
      fprintf(output, "%" PRIu64, samples[index]);
   }
   fputc(']', output);
}

static int benchmarkWriteJson(
   const BenchmarkOptions *options,
   const BenchmarkHost *host,
   const BenchmarkWorkload *workloads,
   const BenchmarkResult *results)
{
   FILE *output = fopen(options->json_path, "w");
   char timestamp[64];
   size_t fixture_index;

   if (output == NULL)
      return 0;
   benchmarkTimestamp(timestamp, sizeof(timestamp));
   fputs("{\n  \"schema_version\":1,\n  \"artifact_kind\":\"tcir_jit_benchmark\",\n", output);
   fputs("  \"benchmark_source\":\"TotalCrossVM/src/tests/ir/tcir_jit_benchmark.c\",\n", output);
   fputs("  \"fixture_source\":\"TotalCrossVM/src/tests/ir/fixtures/tcir_converter_fixtures.h\",\n", output);
   fputs("  \"recorded_at_utc\":", output); benchmarkWriteJsonString(output, timestamp);
   fputs(",\n  \"repository_revision\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_GIT_REVISION);
   fputs(",\n  \"dirty_paths\":", output); benchmarkWriteJsonString(output, options->dirty_paths);
   fputs(",\n  \"host\":{\"os\":", output); benchmarkWriteJsonString(output, host->os);
   fputs(",\"kernel\":", output); benchmarkWriteJsonString(output, host->kernel);
   fputs(",\"architecture\":", output); benchmarkWriteJsonString(output, host->architecture);
   fputs(",\"cpu_model\":", output); benchmarkWriteJsonString(output, host->cpu_model);
   fprintf(output, ",\"logical_cpu_count\":%" PRIu64 ",\"memory_bytes\":%" PRIu64,
           host->logical_cpu_count, host->memory_bytes);
   fputs(",\"power_mode\":", output); benchmarkWriteJsonString(output, options->power_mode);
   fputs(",\"affinity_policy\":\"not set; platform scheduler\",\"background_load\":", output);
   benchmarkWriteJsonString(output, options->background_load);
   fputs("},\n  \"build\":{\"type\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_BUILD_TYPE);
   fputs(",\"compiler_id\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_COMPILER_ID);
   fputs(",\"compiler_version\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_COMPILER_VERSION);
   fputs(",\"c_flags\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_C_FLAGS);
   fputs(",\"generator\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_GENERATOR);
   fputs(",\"target_processor\":", output); benchmarkWriteJsonString(output, TC_BENCHMARK_TARGET_PROCESSOR);
   fputs(",\"sljit_platform\":", output); benchmarkWriteJsonString(output, tcirJitPlatformName());
   fputs(",\"options\":[\"TC_ENABLE_SLJIT_JIT=ON\",\"TC_BUILD_IR_BENCHMARKS=ON\"]},\n", output);
   fprintf(
      output,
      "  \"protocol\":{\"timer\":\"monotonic wall clock\",\"warmup_count\":%u,"
      "\"sample_count\":%u,\"order_policy\":\"six backend permutations in round-robin order; counts differ by at most one\","
      "\"validation\":\"every warmup and measured batch checksum must match executeMethod\","
      "\"outlier_policy\":\"no samples excluded or filtered\","
      "\"scope\":\"hot standalone API invocation with contexts and artifacts reused; current per-invocation verifier and scratch allocation costs included\"},\n",
      TC_BENCHMARK_WARMUP_COUNT,
      TC_BENCHMARK_SAMPLE_COUNT);
   fputs("  \"workloads\":[\n", output);
   for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
   {
      const BenchmarkWorkload *workload = &workloads[fixture_index];
      const BenchmarkResult *result = &results[fixture_index];
      size_t backend;
      if (fixture_index != 0U)
         fputs(",\n", output);
      fputs("    {\"name\":", output); benchmarkWriteJsonString(output, workload->name);
      fputs(",\"method\":", output); benchmarkWriteJsonString(output, tcir_converter_fixtures[fixture_index].identity);
      fputs(",\"input\":", output); benchmarkWriteJsonString(output, workload->input_description);
      fprintf(output, ",\"invocations_per_batch\":%lu,\"expected_checksum\":\"0x%016" PRIx64 "\",",
              (unsigned long)workload->invocation_count, result->expected_checksum);
      fputs("\"correctness\":{\"validated\":true,\"oracle\":\"executeMethod\"},", output);
      fputs("\"jit_compile\":{\"baseline\":\"not_applicable\",\"code_bytes\":", output);
      fprintf(output, "%lu,\"stats\":", (unsigned long)result->code_size);
      benchmarkWriteJsonStats(output, &result->compile_stats, 1U);
      fputs(",\"raw_nanoseconds\":", output);
      benchmarkWriteJsonSamples(output, result->compile_nanoseconds);
      fputs("},\"execution\":{", output);
      for (backend = 0U; backend < TC_BENCHMARK_BACKEND_COUNT; ++backend)
      {
         if (backend != 0U)
            fputc(',', output);
         benchmarkWriteJsonString(output, benchmark_backend_names[backend]);
         fputs(":{\"stats\":", output);
         benchmarkWriteJsonStats(output, &result->execution_stats[backend], workload->invocation_count);
         fputs(",\"raw_nanoseconds\":", output);
         benchmarkWriteJsonSamples(output, result->execution_nanoseconds[backend]);
         fputc('}', output);
      }
      fputs("},\"comparisons_vs_executeMethod\":{", output);
      for (backend = BENCHMARK_TCIR; backend <= BENCHMARK_SLJIT; ++backend)
      {
         double baseline = result->execution_stats[BENCHMARK_TCODE].mean;
         double candidate = result->execution_stats[backend].mean;
         if (backend != BENCHMARK_TCIR)
            fputc(',', output);
         benchmarkWriteJsonString(output, benchmark_backend_names[backend]);
         fprintf(
            output,
            ":{\"mean_difference_nanoseconds\":%.3f,\"percent_change\":%.6f,"
            "\"speedup_ratio\":%.6f}",
            candidate - baseline,
            (candidate - baseline) * 100.0 / baseline,
            baseline / candidate);
      }
      fputs("}}", output);
   }
   fputs("\n  ]\n}\n", output);
   if (fclose(output) != 0)
      return 0;
   return 1;
}

static int benchmarkWriteCsv(
   const BenchmarkOptions *options,
   const BenchmarkWorkload *workloads,
   const BenchmarkResult *results)
{
   FILE *output = fopen(options->csv_path, "w");
   size_t fixture_index;

   if (output == NULL)
      return 0;
   fputs("schema_version,revision,workload,metric,backend,sample,order_position,"
         "invocations,duration_nanoseconds,checksum,validated\n", output);
   for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
   {
      const BenchmarkWorkload *workload = &workloads[fixture_index];
      const BenchmarkResult *result = &results[fixture_index];
      size_t sample;
      for (sample = 0U; sample < TC_BENCHMARK_SAMPLE_COUNT; ++sample)
      {
         size_t backend;
         fprintf(
            output,
            "1,%s,%s,jit_compile,sljit,%lu,-1,1,%" PRIu64 ",0x%016" PRIx64 ",true\n",
            TC_BENCHMARK_GIT_REVISION,
            workload->name,
            (unsigned long)(sample + 1U),
            result->compile_nanoseconds[sample],
            result->expected_checksum);
         for (backend = 0U; backend < TC_BENCHMARK_BACKEND_COUNT; ++backend)
         {
            size_t position;
            const unsigned int *order = benchmark_orders[sample % 6U];
            for (position = 0U; position < TC_BENCHMARK_BACKEND_COUNT; ++position)
               if (order[position] == backend)
                  break;
            fprintf(
               output,
               "1,%s,%s,execution,%s,%lu,%lu,%lu,%" PRIu64 ",0x%016" PRIx64 ",true\n",
               TC_BENCHMARK_GIT_REVISION,
               workload->name,
               benchmark_backend_names[backend],
               (unsigned long)(sample + 1U),
               (unsigned long)position,
               (unsigned long)workload->invocation_count,
               result->execution_nanoseconds[backend][sample],
               result->expected_checksum);
         }
      }
   }
   if (fclose(output) != 0)
      return 0;
   return 1;
}

static void benchmarkPrintSummary(
   const BenchmarkWorkload *workloads,
   const BenchmarkResult *results,
   const BenchmarkOptions *options)
{
   size_t fixture_index;
   printf("TCIR JIT benchmark: %u warmups, %u measured samples, six-order rotation.\n",
          TC_BENCHMARK_WARMUP_COUNT, TC_BENCHMARK_SAMPLE_COUNT);
   for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
   {
      const BenchmarkResult *result = &results[fixture_index];
      double baseline = result->execution_stats[BENCHMARK_TCODE].mean;
      printf(
         "%s (%lu invocations/batch): executeMethod=%.3f ns/invocation, "
         "TCIR=%.3f (%.3fx), SLJIT=%.3f (%.3fx), compile=%.3f us, code=%lu bytes.\n",
         workloads[fixture_index].name,
         (unsigned long)workloads[fixture_index].invocation_count,
         baseline / (double)workloads[fixture_index].invocation_count,
         result->execution_stats[BENCHMARK_TCIR].mean / (double)workloads[fixture_index].invocation_count,
         baseline / result->execution_stats[BENCHMARK_TCIR].mean,
         result->execution_stats[BENCHMARK_SLJIT].mean / (double)workloads[fixture_index].invocation_count,
         baseline / result->execution_stats[BENCHMARK_SLJIT].mean,
         result->compile_stats.mean / 1000.0,
         (unsigned long)result->code_size);
   }
   printf("Raw artifacts: %s and %s.\n", options->json_path, options->csv_path);
}

int main(int argc, char **argv)
{
   static const BenchmarkWorkload workloads[TC_BENCHMARK_FIXTURE_COUNT] = {
      { "add", "50,000 deterministic fixed-seed i32 pairs", 50000U, UINT32_C(0x4d595df4), 0 },
      { "abs", "50,000 deterministic fixed-seed i32 values", 50000U, UINT32_C(0x9e3779b9), 0 },
      { "sumTo", "32 invocations with n=65,537", 32U, 0U, 65537 }
   };
   BenchmarkOptions options;
   BenchmarkHost host;
   TCIRDiagnostic diagnostic;
   TCIRModule *module;
   BenchmarkFixtureRunner runners[TC_BENCHMARK_FIXTURE_COUNT];
   BenchmarkResult results[TC_BENCHMARK_FIXTURE_COUNT];
   size_t fixture_index;
   int accepted = 1;

   if (!benchmarkParseOptions(argc, argv, &options))
   {
      benchmarkUsage(argv[0]);
      return 2;
   }
   module = tcirModuleCreate(NULL, &diagnostic);
   if (module == NULL)
      return 1;
   memset(runners, 0, sizeof(runners));
   memset(results, 0, sizeof(results));
   for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
      if (!benchmarkPrepareRunner(module, &tcir_converter_fixtures[fixture_index], &runners[fixture_index]))
      {
         fprintf(stderr, "failed to prepare benchmark fixture %s\n",
                 tcir_converter_fixtures[fixture_index].identity);
         accepted = 0;
         break;
      }
   if (accepted)
      for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
         if (!benchmarkCompileFixture(&runners[fixture_index], &results[fixture_index])
             || !benchmarkExecuteFixture(&runners[fixture_index], &workloads[fixture_index],
                                         &results[fixture_index]))
         {
            fprintf(stderr, "benchmark validation failed for %s\n", workloads[fixture_index].name);
            accepted = 0;
            break;
         }
   if (accepted)
   {
      benchmarkHostInfo(&host);
      accepted = benchmarkWriteJson(&options, &host, workloads, results)
         && benchmarkWriteCsv(&options, workloads, results);
      if (!accepted)
         fprintf(stderr, "failed to write benchmark artifacts\n");
   }
   if (accepted)
      benchmarkPrintSummary(workloads, results, &options);
   for (fixture_index = 0U; fixture_index < TC_BENCHMARK_FIXTURE_COUNT; ++fixture_index)
      if (runners[fixture_index].fixture != NULL)
         benchmarkDestroyRunner(&runners[fixture_index]);
   tcirModuleDestroy(module);
   return accepted ? 0 : 1;
}
