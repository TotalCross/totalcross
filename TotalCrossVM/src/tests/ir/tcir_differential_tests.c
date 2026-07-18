// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_frontend.h"
#include "tcir_interp.h"
#if defined(TCIR_HAS_AOT)
#include "tcir_aot.h"
#include "tcir_aot_generated.h"
#endif
#if defined(TCIR_HAS_SLJIT)
#include "tcir_jit.h"
#endif
#include "tcvm.h"

#include <limits.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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

typedef enum DifferentialOutcome
{
   DIFFERENTIAL_RETURNED = 0,
   DIFFERENTIAL_THROWN
} DifferentialOutcome;

typedef struct DifferentialResult
{
   DifferentialOutcome outcome;
   TCIRType return_type;
   int32_t return_i32;
   int64_t return_i64;
   const char *exception_class;
   char exception_message[1024];
   int frame_restored;
} DifferentialResult;

#include "fixtures/tcir_converter_fixtures.h"

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
   return 1;
}

static void initializeMethod(
   const TCIRConverterFixture *fixture,
   TCode *code,
   TMethod *method,
   TTCClass *class_,
   TConstantPool *constant_pool,
   uint8_t *parameter_registers)
{
   size_t index;

   memset(code, 0, fixture->code_count * sizeof(*code));
   for (index = 0U; index < fixture->code_count; ++index)
      code[index].u32.u32 = (uint32)fixture->code[index];
   memset(constant_pool, 0, sizeof(*constant_pool));
   memset(class_, 0, sizeof(*class_));
   class_->cp = constant_pool;
   class_->name = (CharP)"fixtures.TCIRPoc";
   memset(method, 0, sizeof(*method));
   method->iCount = (uint8)fixture->i32_count;
   method->oCount = 1U;
   method->v64Count = (uint8)fixture->v64_count;
   method->code = code;
   method->class_ = class_;
   method->name = (CharP)fixture->identity;
   method->paramCount = (uint16)fixture->parameter_count;
   method->paramRegs = parameter_registers;
   method->returnReg = fixture->return_type == TCIR_TYPE_I64 ? RegL : RegI;
   method->flags.isStatic = true;
   for (index = 0U; index < fixture->parameter_count; ++index)
      parameter_registers[index] = fixture->parameters[index].type == TCIR_TYPE_I64
         ? (uint8)RegL
         : (uint8)RegI;
}

static void initializeArguments(
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second,
   TCIRRuntimeValue *arguments)
{
   size_t index;
   memset(arguments, 0, fixture->parameter_count * sizeof(*arguments));
   for (index = 0U; index < fixture->parameter_count; ++index)
   {
      if (fixture->parameters[index].type == TCIR_TYPE_I64)
         arguments[index].i64 = first;
      else
         arguments[index].i32 = index == 0U ? (int32_t)first : second;
   }
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

static DifferentialResult executeTCode(
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second)
{
   enum { FRAME_CAPACITY = 16 };
   TCode code[32];
   TMethod method;
   TTCClass class_;
   TConstantPool constant_pool;
   uint8_t parameter_registers[2];
   TContext context;
   int32 register_i32[FRAME_CAPACITY];
   TCObject register_ref[FRAME_CAPACITY];
   int64 register_v64[FRAME_CAPACITY];
   VoidP call_stack[FRAME_CAPACITY];
   TCIRRuntimeValue arguments[2];
   TValue argument_values[2];
   TValue value;
   DifferentialResult result;
   size_t index;

   memset(&result, 0, sizeof(result));
   initializeMethod(fixture, code, &method, &class_, &constant_pool, parameter_registers);
   initializeContext(&context, register_i32, register_ref, register_v64, call_stack, FRAME_CAPACITY);
   initializeArguments(fixture, first, second, arguments);
   memset(argument_values, 0, sizeof(argument_values));
   for (index = 0U; index < fixture->parameter_count; ++index)
   {
      if (fixture->parameters[index].type == TCIR_TYPE_I64)
         argument_values[index].asInt64 = arguments[index].i64;
      else
         argument_values[index].asInt32 = arguments[index].i32;
   }
   context.parametersInArray = fixture->parameter_count != 0U;
   value = fixture->parameter_count == 0U
      ? executeMethod(&context, &method)
      : executeMethod(&context, &method, argument_values);

   result.outcome = context.thrownException == null ? DIFFERENTIAL_RETURNED : DIFFERENTIAL_THROWN;
   result.return_type = fixture->return_type;
   result.return_i32 = (int32_t)value.asInt32;
   result.return_i64 = (int64_t)value.asInt64;
   result.frame_restored = context.regI == context.regIStart
      && context.regO == context.regOStart
      && context.reg64 == context.reg64Start
      && context.callStack == context.callStackStart;
   if (context.thrownException != null)
   {
      result.exception_class = OBJ_CLASS(context.thrownException)->name;
      snprintf(result.exception_message, sizeof(result.exception_message), "%s", context.exmsg);
   }
   DESTROY_MUTEX(context.usageLock);
   return result;
}

static DifferentialResult executeTCIR(
   const TCIRFunction *function,
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second,
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue arguments[2];
   TCIRInterpreterFrame frame;
   TCIRInterpreterResult interpreter_result;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, first, second, arguments);
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
   memset(&result, 0, sizeof(result));
   result.frame_restored = 1;
   if (tcirInterpretFunction(function, &frame, NULL, &interpreter_result, diagnostic)
       == TCIR_INTERPRETER_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = interpreter_result.type;
      result.return_i32 = interpreter_result.value.i32;
      result.return_i64 = interpreter_result.value.i64;
   }
   else
   {
      result.outcome = DIFFERENTIAL_THROWN;
      result.return_type = interpreter_result.type;
      result.exception_class = "<tcir>";
      snprintf(result.exception_message, sizeof(result.exception_message), "%s", diagnostic->message);
   }
   return result;
}

#if defined(TCIR_HAS_SLJIT)
static DifferentialResult executeSLJIT(
   const TCIRJitArtifact *artifact,
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second)
{
   TCIRRuntimeValue arguments[2];
   TCCompiledFrame frame;
   TCCompiledResult compiled_result;
   TCIRJitDiagnostic diagnostic;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, first, second, arguments);
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
   memset(&result, 0, sizeof(result));
   if (tcirJitInvoke(artifact, &frame, &compiled_result, &diagnostic) == TC_COMPILED_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = compiled_result.type;
      result.return_i32 = compiled_result.value.i32;
      result.return_i64 = compiled_result.value.i64;
   }
   else
   {
      result.outcome = DIFFERENTIAL_THROWN;
      result.return_type = compiled_result.type;
      result.exception_class = "<sljit>";
      snprintf(result.exception_message, sizeof(result.exception_message), "%s", diagnostic.message);
   }
   result.frame_restored = frame.scratch_values == NULL
      && frame.scratch_count == 0U
      && frame.edge_values == NULL
      && frame.edge_count == 0U;
   return result;
}
#endif

#if defined(TCIR_HAS_AOT)
static DifferentialResult executeAOT(
   TCCompiledEntry entry,
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second)
{
   TCIRRuntimeValue arguments[2];
   TCCompiledFrame frame;
   TCCompiledResult compiled_result;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, first, second, arguments);
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
   memset(&result, 0, sizeof(result));
   if (entry(&frame, &compiled_result) == TC_COMPILED_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = compiled_result.type;
      result.return_i32 = compiled_result.value.i32;
      result.return_i64 = compiled_result.value.i64;
   }
   else
   {
      result.outcome = DIFFERENTIAL_THROWN;
      result.return_type = compiled_result.type;
      result.exception_class = "<aot>";
      snprintf(result.exception_message, sizeof(result.exception_message), "portable-C entry rejected the frame");
   }
   result.frame_restored = frame.scratch_values == NULL
      && frame.scratch_count == 0U
      && frame.edge_values == NULL
      && frame.edge_count == 0U;
   return result;
}

static TCCompiledEntry findAOTEntry(const char *class_name, const char *method_name, const char *signature)
{
   size_t index;
   for (index = 0U; index < tcir_aot_generated_registry_count; ++index)
   {
      const TCIRAotRegistryEntry *candidate = &tcir_aot_generated_registry[index];
      if (strcmp(candidate->class_name, class_name) == 0
          && strcmp(candidate->method_name, method_name) == 0
          && strcmp(candidate->signature, signature) == 0)
      {
         const TCIRAotRegistryEntry *registered = tcirAotRegistryFind(
            tcir_aot_generated_registry,
            tcir_aot_generated_registry_count,
            class_name,
            method_name,
            signature,
            candidate->content_hash);
         return registered == NULL ? NULL : registered->entry;
      }
   }
   return NULL;
}
#endif

static int resultsAgree(
   const TCIRConverterFixture *fixture,
   int64_t first,
   int32_t second,
   const DifferentialResult *tcode,
   const DifferentialResult *candidate,
   const char *candidate_name)
{
   if (tcode->outcome == candidate->outcome
       && tcode->return_type == candidate->return_type
       && tcode->frame_restored
       && candidate->frame_restored
       && ((tcode->outcome == DIFFERENTIAL_RETURNED
            && (tcode->return_type == TCIR_TYPE_I64
                ? tcode->return_i64 == candidate->return_i64
                : tcode->return_i32 == candidate->return_i32))
           || (tcode->outcome == DIFFERENTIAL_THROWN
               && strcmp(tcode->exception_class, candidate->exception_class) == 0
               && strcmp(tcode->exception_message, candidate->exception_message) == 0)))
      return 1;

   fprintf(
      stderr,
      "differential mismatch for %s(%lld, %d): TCode={outcome=%d,type=%d,value=%lld,frame=%d} "
      "%s={outcome=%d,type=%d,value=%lld,frame=%d}\n",
      fixture->identity,
      (long long)first,
      (int)second,
      (int)tcode->outcome,
      (int)tcode->return_type,
      (long long)(tcode->return_type == TCIR_TYPE_I64 ? tcode->return_i64 : tcode->return_i32),
      tcode->frame_restored,
      candidate_name,
      (int)candidate->outcome,
      (int)candidate->return_type,
      (long long)(candidate->return_type == TCIR_TYPE_I64
                     ? candidate->return_i64
                     : candidate->return_i32),
      candidate->frame_restored);
   return 0;
}

static int compareInput(
   const TCIRConverterFixture *fixture,
   const TCIRFunction *function,
   int64_t first,
   int32_t second,
   const void *jit_artifact,
#if defined(TCIR_HAS_AOT)
   TCCompiledEntry aot_entry,
#endif
   TCIRDiagnostic *diagnostic)
{
   DifferentialResult tcode = executeTCode(fixture, first, second);
   DifferentialResult tcir = executeTCIR(function, fixture, first, second, diagnostic);
   if (!resultsAgree(fixture, first, second, &tcode, &tcir, "TCIR"))
      return 0;
#if defined(TCIR_HAS_SLJIT)
   {
      DifferentialResult sljit = executeSLJIT((const TCIRJitArtifact *)jit_artifact, fixture, first, second);
      if (!resultsAgree(fixture, first, second, &tcode, &sljit, "SLJIT"))
         return 0;
   }
#else
   (void)jit_artifact;
#endif
#if defined(TCIR_HAS_AOT)
   {
      DifferentialResult aot = executeAOT(aot_entry, fixture, first, second);
      return resultsAgree(fixture, first, second, &tcode, &aot, "AOT");
   }
#else
   return 1;
#endif
}

static uint32_t nextGeneratedValue(uint32_t *state)
{
   uint32_t value = *state;
   value ^= value << 13;
   value ^= value >> 17;
   value ^= value << 5;
   *state = value;
   return value;
}

static int32_t i32FromBits(uint32_t bits)
{
   if (bits <= (uint32_t)INT32_MAX)
      return (int32_t)bits;
   return (int32_t)(-1 - (int32_t)(UINT32_MAX - bits));
}

static int64_t i64FromBits(uint64_t bits)
{
   if (bits <= (uint64_t)INT64_MAX)
      return (int64_t)bits;
   return (int64_t)(-1 - (int64_t)(UINT64_MAX - bits));
}

static int testFixtureCorpus(void)
{
   static const int32_t add_cases[][2] = {
      { 0, 0 },
      { 1, -1 },
      { 42, 58 },
      { -42, -58 },
      { INT_MAX, 0 },
      { INT_MIN, 0 },
      { INT_MAX, 1 },
      { INT_MIN, -1 },
      { INT_MAX, INT_MAX },
      { INT_MIN, INT_MIN }
   };
   static const int32_t abs_cases[] = { 0, 1, -1, 42, -42, INT_MAX, INT_MIN };
   static const int32_t sum_cases[] = { INT_MIN, -100, -1, 0, 1, 2, 10, 100, 4096, 65537 };
   static const int64_t pure_i64_cases[][2] = {
      { INT64_C(0), INT64_C(0) },
      { INT64_C(1), -INT64_C(1) },
      { -INT64_C(1), INT64_C(1) },
      { INT64_MAX, INT64_C(63) },
      { INT64_MIN, INT64_C(64) },
      { INT64_MAX, INT64_C(65) },
      { INT64_MIN, -INT64_C(65) },
      { INT64_C(0x0123456789abcdef), INT64_C(31) },
      { -INT64_C(0x0123456789abcdef), INT64_C(130) },
      { INT64_C(42), -INT64_C(130) }
   };
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT];
   void *jit_artifacts[TCIR_CONVERTER_FIXTURE_COUNT] = { NULL };
#if defined(TCIR_HAS_AOT)
   static const char *const method_names[] = { "add", "abs", "sumTo", "pureI32", "pureI64" };
   static const char *const signatures[] = { "(II)I", "(I)I", "(I)I", "(II)I", "(JI)J" };
   TCCompiledEntry aot_entries[TCIR_CONVERTER_FIXTURE_COUNT];
#endif
   uint32_t generated_state = UINT32_C(0x4d595df4);
   uint32_t generated_i64_state = UINT32_C(0x8a5cd789);
   size_t case_index;
   size_t fixture_index;

   REQUIRE(module != NULL);
   REQUIRE(sizeof(TCode) == sizeof(unsigned int));
   for (fixture_index = 0U; fixture_index < TCIR_CONVERTER_FIXTURE_COUNT; ++fixture_index)
   {
      TCIRMethodParameter parameters[2];
      TCIRMethodView view;
      REQUIRE(buildFixtureView(&tcir_converter_fixtures[fixture_index], &view, parameters));
      REQUIRE(tcirFrontendBuildFunction(module, &view, &functions[fixture_index], &diagnostic) == TCIR_FRONTEND_OK);
      REQUIRE(functions[fixture_index] != NULL);
#if defined(TCIR_HAS_SLJIT)
      {
         TCIRJitDiagnostic jit_diagnostic;
         TCIRJitArtifact *artifact = NULL;
         REQUIRE(tcirJitCompile(functions[fixture_index], NULL, &artifact, &jit_diagnostic)
                 == TCIR_JIT_COMPILE_READY);
         REQUIRE(artifact != NULL);
         jit_artifacts[fixture_index] = artifact;
      }
#endif
#if defined(TCIR_HAS_AOT)
      aot_entries[fixture_index] = findAOTEntry(
         "fixtures.TCIRPoc", method_names[fixture_index], signatures[fixture_index]);
      REQUIRE(aot_entries[fixture_index] != NULL);
#endif
   }

   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[0], functions[0], add_cases[case_index][0],
                           add_cases[case_index][1], jit_artifacts[0],
#if defined(TCIR_HAS_AOT)
                           aot_entries[0],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(abs_cases) / sizeof(abs_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[1], functions[1], abs_cases[case_index], 0,
                           jit_artifacts[1],
#if defined(TCIR_HAS_AOT)
                           aot_entries[1],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(sum_cases) / sizeof(sum_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[2], functions[2], sum_cases[case_index], 0,
                           jit_artifacts[2],
#if defined(TCIR_HAS_AOT)
                           aot_entries[2],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[3], functions[3], add_cases[case_index][0],
                           add_cases[case_index][1], jit_artifacts[3],
#if defined(TCIR_HAS_AOT)
                           aot_entries[3],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(pure_i64_cases) / sizeof(pure_i64_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[4], functions[4], pure_i64_cases[case_index][0],
                           (int32_t)pure_i64_cases[case_index][1], jit_artifacts[4],
#if defined(TCIR_HAS_AOT)
                           aot_entries[4],
#endif
                           &diagnostic));

   for (case_index = 0U; case_index < 512U; ++case_index)
   {
      int32_t first = i32FromBits(nextGeneratedValue(&generated_state));
      int32_t second = i32FromBits(nextGeneratedValue(&generated_state));
      REQUIRE(compareInput(&tcir_converter_fixtures[0], functions[0], first, second,
                           jit_artifacts[0],
#if defined(TCIR_HAS_AOT)
                           aot_entries[0],
#endif
                           &diagnostic));
      REQUIRE(compareInput(&tcir_converter_fixtures[1], functions[1], first, 0,
                           jit_artifacts[1],
#if defined(TCIR_HAS_AOT)
                           aot_entries[1],
#endif
                           &diagnostic));
      REQUIRE(compareInput(&tcir_converter_fixtures[3], functions[3], first, second,
                           jit_artifacts[3],
#if defined(TCIR_HAS_AOT)
                           aot_entries[3],
#endif
                           &diagnostic));
      {
         uint64_t bits = ((uint64_t)nextGeneratedValue(&generated_i64_state) << 32)
            | (uint64_t)nextGeneratedValue(&generated_i64_state);
         int64_t long_value = i64FromBits(bits);
         int32_t distance = i32FromBits(nextGeneratedValue(&generated_i64_state));
         REQUIRE(compareInput(&tcir_converter_fixtures[4], functions[4], long_value, distance,
                              jit_artifacts[4],
#if defined(TCIR_HAS_AOT)
                              aot_entries[4],
#endif
                              &diagnostic));
      }
   }
   for (case_index = 0U; case_index < 128U; ++case_index)
   {
      int32_t value = (int32_t)(nextGeneratedValue(&generated_state) % UINT32_C(4129)) - 32;
      REQUIRE(compareInput(&tcir_converter_fixtures[2], functions[2], value, 0,
                           jit_artifacts[2],
#if defined(TCIR_HAS_AOT)
                           aot_entries[2],
#endif
                           &diagnostic));
   }

#if defined(TCIR_HAS_SLJIT)
   for (fixture_index = 0U; fixture_index < TCIR_CONVERTER_FIXTURE_COUNT; ++fixture_index)
      tcirJitArtifactDestroy((TCIRJitArtifact *)jit_artifacts[fixture_index]);
#endif
   tcirModuleDestroy(module);
   return 1;
}

static int testUnsupportedFrontendFallback(void)
{
   const unsigned int unsupported_code[] = { 149U, 136U };
   TCIRDiagnostic diagnostic;
   TCIRMethodView view;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function = NULL;

   REQUIRE(module != NULL);
   memset(&view, 0, sizeof(view));
   view.identity = "Fallback.object:()V";
   view.code = unsupported_code;
   view.code_slot_count = sizeof(unsupported_code) / sizeof(unsupported_code[0]);
   view.ref_home_count = 1U;
   view.return_type = TCIR_TYPE_VOID;
   REQUIRE(tcirFrontendBuildFunction(module, &view, &function, &diagnostic) == TCIR_FRONTEND_FALLBACK);
   REQUIRE(function == NULL);
   REQUIRE(tcirModuleFunctionCount(module) == 0U);
   REQUIRE(diagnostic.code == TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE);
   tcirModuleDestroy(module);
   return 1;
}

int main(void)
{
   if (!testFixtureCorpus() || !testUnsupportedFrontendFallback())
      return 1;
#if defined(TCIR_HAS_SLJIT)
#if defined(TCIR_HAS_AOT)
   printf("TCIR differential tests passed: 5 fixtures, 2,223 executeMethod/TCIR/SLJIT/AOT comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789.\n");
#else
   printf("TCIR differential tests passed: 5 fixtures, 2,223 executeMethod/TCIR/SLJIT comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789.\n");
#endif
#elif defined(TCIR_HAS_AOT)
   printf("TCIR differential tests passed: 5 fixtures, 2,223 executeMethod/TCIR/AOT comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789.\n");
#else
   printf("TCIR differential tests passed: 5 fixtures, 2,223 executeMethod comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789.\n");
#endif
   return 0;
}
