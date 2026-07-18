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
   double return_f64;
   void *return_ref;
   const char *exception_class;
   char exception_message[1024];
   int frame_restored;
} DifferentialResult;

#include "fixtures/tcir_converter_fixtures.h"

static int isStaticCallFixture(const TCIRConverterFixture *fixture)
{
   return fixture != NULL && fixture->code == tcir_fixture_callStatic_code;
}

static int allocation_token;

static int isObjectAllocationFixture(const TCIRConverterFixture *fixture)
{
   return fixture != NULL && fixture->code == tcir_fixture_newObject_code;
}

static TCIRObjectAllocationStatus allocateFixtureObject(
   void *runtime_context,
   const TCIRSymbol *symbol,
   void **ref_homes,
   size_t ref_home_count,
   unsigned int destination_home,
   TCIRRuntimeValue *result)
{
   const TCIRConverterFixture *fixture = (const TCIRConverterFixture *)runtime_context;
   if (!isObjectAllocationFixture(fixture) || symbol == NULL || ref_homes == NULL ||
       ref_home_count != 1U || destination_home != 0U || result == NULL ||
       tcirSymbolKind(symbol) != TCIR_SYMBOL_CLASS ||
       tcirSymbolConstantPoolIndex(symbol) != 11U)
      return TCIR_OBJECT_ALLOCATION_REJECTED;
   ref_homes[destination_home] = &allocation_token;
   memset(result, 0, sizeof(*result));
   result->ref = &allocation_token;
   return TCIR_OBJECT_ALLOCATION_RETURNED;
}

static TCCompiledStatus allocateCompiledFixtureObject(
   const TCCompiledRuntime *runtime,
   const TCCompiledAllocation *allocation,
   TCCompiledResult *result)
{
   const TCIRConverterFixture *fixture = runtime == NULL
      ? NULL : (const TCIRConverterFixture *)runtime->context;
   if (runtime == NULL || runtime->abi_version != TC_RUNTIME_ABI_VERSION ||
       !isObjectAllocationFixture(fixture) || allocation == NULL || result == NULL ||
       allocation->constant_pool_index != 11U || allocation->ref_homes == NULL ||
       allocation->ref_home_count != 1U || allocation->destination_home != 0U ||
       allocation->tc_pc != 0U)
      return TC_COMPILED_REJECTED;
   allocation->ref_homes[allocation->destination_home] = &allocation_token;
   memset(result, 0, sizeof(*result));
   result->status = TC_COMPILED_RETURNED;
   result->type = TCIR_TYPE_REF;
   result->value.ref = &allocation_token;
   result->tc_pc = allocation->tc_pc;
   return TC_COMPILED_RETURNED;
}

static TCIRMethodCallStatus invokeFixtureCall(
   void *runtime_context,
   const TCIRSymbol *symbol,
   TCIRCallKind kind,
   void *receiver,
   const TCIRRuntimeValue *arguments,
   size_t argument_count,
   TCIRRuntimeValue *result)
{
   const TCIRConverterFixture *fixture = (const TCIRConverterFixture *)runtime_context;
   if (!isStaticCallFixture(fixture) || symbol == NULL || kind != TCIR_CALL_STATIC ||
       receiver != NULL || arguments == NULL || argument_count != 2U || result == NULL ||
       tcirSymbolKind(symbol) != TCIR_SYMBOL_METHOD ||
       tcirSymbolConstantPoolIndex(symbol) != 1U)
      return TCIR_METHOD_CALL_REJECTED;
   memset(result, 0, sizeof(*result));
   result->i32 = arguments[0].i32 + arguments[1].i32;
   return TCIR_METHOD_CALL_RETURNED;
}

static TCCompiledStatus invokeCompiledFixtureCall(
   const TCCompiledRuntime *runtime,
   const TCCompiledCall *call,
   TCCompiledResult *result)
{
   const TCIRConverterFixture *fixture = runtime == NULL
      ? NULL
      : (const TCIRConverterFixture *)runtime->context;
   if (runtime == NULL || runtime->abi_version != TC_RUNTIME_ABI_VERSION ||
       !isStaticCallFixture(fixture) || call == NULL || result == NULL ||
       call->constant_pool_index != 1U || call->kind != TCIR_CALL_STATIC ||
       call->receiver != NULL || call->arguments == NULL || call->argument_count != 2U ||
       call->result_type != TCIR_TYPE_I32)
      return TC_COMPILED_REJECTED;
   memset(result, 0, sizeof(*result));
   result->status = TC_COMPILED_RETURNED;
   result->type = TCIR_TYPE_I32;
   result->value.i32 = call->arguments[0].i32 + call->arguments[1].i32;
   result->tc_pc = TCIR_TCPC_NONE;
   return TC_COMPILED_RETURNED;
}

static void nativeAddI32(NMParams parameters)
{
   parameters->retI = parameters->i32[0] + parameters->i32[1];
}

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
   view->resolve_class_name = tcirResolveConverterFixtureClass;
   view->resolve_class_name_user_data = (void *)fixture;
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
   method->oCount = (uint8)fixture->ref_count;
   method->v64Count = (uint8)fixture->v64_count;
   method->code = code;
   method->class_ = class_;
   method->name = (CharP)fixture->identity;
   method->paramCount = (uint16)fixture->parameter_count;
   method->paramRegs = parameter_registers;
   method->returnReg = fixture->return_type == TCIR_TYPE_I64 ? RegL :
      (fixture->return_type == TCIR_TYPE_F64 ? RegD :
       (fixture->return_type == TCIR_TYPE_REF ? RegO : RegI));
   method->flags.isStatic = true;
   for (index = 0U; index < fixture->parameter_count; ++index)
      parameter_registers[index] = fixture->parameters[index].type == TCIR_TYPE_I64
         ? (uint8)RegL
         : (fixture->parameters[index].type == TCIR_TYPE_F64 ? (uint8)RegD :
            (fixture->parameters[index].type == TCIR_TYPE_REF ? (uint8)RegO : (uint8)RegI));
}

static void initializeArguments(
   const TCIRConverterFixture *fixture,
   const TCIRRuntimeValue *inputs,
   TCIRRuntimeValue *arguments)
{
   size_t index;
   memset(arguments, 0, fixture->parameter_count * sizeof(*arguments));
   for (index = 0U; index < fixture->parameter_count; ++index)
      arguments[index] = inputs[index];
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
   const TCIRRuntimeValue *inputs)
{
   enum { FRAME_CAPACITY = 16 };
   TCode code[64];
   TMethod method;
   TTCClass class_;
   TConstantPool constant_pool;
   TCode target_code[1];
   TMethod target_method;
   TTCClass target_class;
   Method bound_normal[2];
   uint8_t target_parameter_registers[2];
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
   if (isStaticCallFixture(fixture))
   {
      memset(target_code, 0, sizeof(target_code));
      memset(&target_method, 0, sizeof(target_method));
      memset(&target_class, 0, sizeof(target_class));
      memset(bound_normal, 0, sizeof(bound_normal));
      target_class.cp = &constant_pool;
      target_class.name = (CharP)"fixtures.TCIRPoc";
      target_code[0].op.op = BREAK;
      target_method.iCount = 2U;
      target_method.oCount = 1U;
      target_method.paramSkip = 1U;
      target_method.code = target_code;
      target_method.class_ = &target_class;
      target_method.name = (CharP)"callTarget";
      target_method.paramCount = 2U;
      target_method.paramRegs = target_parameter_registers;
      target_parameter_registers[0] = RegI;
      target_parameter_registers[1] = RegI;
      target_method.cpReturn = 1U;
      target_method.returnReg = RegI;
      target_method.flags.isStatic = true;
      target_method.flags.isNative = true;
      target_method.boundNM = nativeAddI32;
      bound_normal[1] = &target_method;
      constant_pool.boundNormal = bound_normal;
      constant_pool.mtdCount = 2U;
   }
   initializeContext(&context, register_i32, register_ref, register_v64, call_stack, FRAME_CAPACITY);
   initializeArguments(fixture, inputs, arguments);
   memset(argument_values, 0, sizeof(argument_values));
   for (index = 0U; index < fixture->parameter_count; ++index)
   {
      if (fixture->parameters[index].type == TCIR_TYPE_I64)
         argument_values[index].asInt64 = arguments[index].i64;
      else if (fixture->parameters[index].type == TCIR_TYPE_F64)
         argument_values[index].asDouble = arguments[index].f64;
      else if (fixture->parameters[index].type == TCIR_TYPE_REF)
         argument_values[index].asObj = (TCObject)arguments[index].ref;
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
   result.return_f64 = value.asDouble;
   result.return_ref = value.asObj;
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
   const TCIRRuntimeValue *inputs,
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue arguments[2];
   TCIRInterpreterFrame frame;
   TCIRInterpreterResult interpreter_result;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, inputs, arguments);
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
   frame.runtime_context = (void *)fixture;
   frame.call_method = invokeFixtureCall;
   frame.allocate_object = allocateFixtureObject;
   memset(&result, 0, sizeof(result));
   result.frame_restored = 1;
   if (tcirInterpretFunction(function, &frame, NULL, &interpreter_result, diagnostic)
       == TCIR_INTERPRETER_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = interpreter_result.type;
      result.return_i32 = interpreter_result.value.i32;
      result.return_i64 = interpreter_result.value.i64;
      result.return_f64 = interpreter_result.value.f64;
      result.return_ref = interpreter_result.value.ref;
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
   const TCIRRuntimeValue *inputs)
{
   TCIRRuntimeValue arguments[2];
   TCCompiledFrame frame;
   TCCompiledRuntime runtime;
   TCCompiledResult compiled_result;
   TCIRJitDiagnostic diagnostic;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, inputs, arguments);
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
   memset(&runtime, 0, sizeof(runtime));
   runtime.abi_version = TC_RUNTIME_ABI_VERSION;
   runtime.context = (void *)fixture;
   runtime.invoke = invokeCompiledFixtureCall;
   runtime.allocate = allocateCompiledFixtureObject;
   frame.runtime = &runtime;
   memset(&result, 0, sizeof(result));
   if (tcirJitInvoke(artifact, &frame, &compiled_result, &diagnostic) == TC_COMPILED_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = compiled_result.type;
      result.return_i32 = compiled_result.value.i32;
      result.return_i64 = compiled_result.value.i64;
      result.return_f64 = compiled_result.value.f64;
      result.return_ref = compiled_result.value.ref;
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
   const TCIRRuntimeValue *inputs)
{
   TCIRRuntimeValue arguments[2];
   TCCompiledFrame frame;
   TCCompiledRuntime runtime;
   TCCompiledResult compiled_result;
   int32_t i32_homes[16];
   void *ref_homes[16];
   TCIRV64Home v64_homes[16];
   DifferentialResult result;

   initializeArguments(fixture, inputs, arguments);
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
   memset(&runtime, 0, sizeof(runtime));
   runtime.abi_version = TC_RUNTIME_ABI_VERSION;
   runtime.context = (void *)fixture;
   runtime.invoke = invokeCompiledFixtureCall;
   runtime.allocate = allocateCompiledFixtureObject;
   frame.runtime = &runtime;
   memset(&result, 0, sizeof(result));
   if (entry(&frame, &compiled_result) == TC_COMPILED_RETURNED)
   {
      result.outcome = DIFFERENTIAL_RETURNED;
      result.return_type = compiled_result.type;
      result.return_i32 = compiled_result.value.i32;
      result.return_i64 = compiled_result.value.i64;
      result.return_f64 = compiled_result.value.f64;
      result.return_ref = compiled_result.value.ref;
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

static uint64_t f64Bits(double value)
{
   uint64_t bits;
   memcpy(&bits, &value, sizeof(bits));
   return bits;
}

static uint64_t runtimeValueBits(const TCIRRuntimeValue *value)
{
   uint64_t bits = 0U;
   memcpy(&bits, value, sizeof(bits));
   return bits;
}

static uint64_t resultBits(const DifferentialResult *result)
{
   if (result->return_type == TCIR_TYPE_I64)
      return (uint64_t)result->return_i64;
   if (result->return_type == TCIR_TYPE_F64)
      return f64Bits(result->return_f64);
   if (result->return_type == TCIR_TYPE_REF)
      return (uint64_t)(uintptr_t)result->return_ref;
   return (uint64_t)(uint32_t)result->return_i32;
}

static int resultsAgree(
   const TCIRConverterFixture *fixture,
   const TCIRRuntimeValue *inputs,
   const DifferentialResult *tcode,
   const DifferentialResult *candidate,
   const char *candidate_name)
{
   if (tcode->outcome == candidate->outcome
       && tcode->return_type == candidate->return_type
       && tcode->frame_restored
       && candidate->frame_restored
       && ((tcode->outcome == DIFFERENTIAL_RETURNED
            && resultBits(tcode) == resultBits(candidate))
           || (tcode->outcome == DIFFERENTIAL_THROWN
               && strcmp(tcode->exception_class, candidate->exception_class) == 0
               && strcmp(tcode->exception_message, candidate->exception_message) == 0)))
      return 1;

   fprintf(
      stderr,
      "differential mismatch for %s(input0=0x%016llx,input1=0x%016llx): "
      "TCode={outcome=%d,type=%d,bits=0x%016llx,frame=%d} "
      "%s={outcome=%d,type=%d,bits=0x%016llx,frame=%d}\n",
      fixture->identity,
      (unsigned long long)runtimeValueBits(&inputs[0]),
      (unsigned long long)runtimeValueBits(&inputs[1]),
      (int)tcode->outcome,
      (int)tcode->return_type,
      (unsigned long long)resultBits(tcode),
      tcode->frame_restored,
      candidate_name,
      (int)candidate->outcome,
      (int)candidate->return_type,
      (unsigned long long)resultBits(candidate),
      candidate->frame_restored);
   return 0;
}

static int compareValues(
   const TCIRConverterFixture *fixture,
   const TCIRFunction *function,
   const TCIRRuntimeValue *inputs,
   const void *jit_artifact,
#if defined(TCIR_HAS_AOT)
   TCCompiledEntry aot_entry,
#endif
   TCIRDiagnostic *diagnostic)
{
   DifferentialResult tcode = executeTCode(fixture, inputs);
   DifferentialResult tcir = executeTCIR(function, fixture, inputs, diagnostic);
   if (!resultsAgree(fixture, inputs, &tcode, &tcir, "TCIR"))
      return 0;
#if defined(TCIR_HAS_SLJIT)
   {
      DifferentialResult sljit = executeSLJIT((const TCIRJitArtifact *)jit_artifact, fixture, inputs);
      if (!resultsAgree(fixture, inputs, &tcode, &sljit, "SLJIT"))
         return 0;
   }
#else
   (void)jit_artifact;
#endif
#if defined(TCIR_HAS_AOT)
   {
      DifferentialResult aot = executeAOT(aot_entry, fixture, inputs);
      return resultsAgree(fixture, inputs, &tcode, &aot, "AOT");
   }
#else
   return 1;
#endif
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
   TCIRRuntimeValue inputs[2];
   memset(inputs, 0, sizeof(inputs));
   if (fixture->parameters[0].type == TCIR_TYPE_I64)
      inputs[0].i64 = first;
   else
      inputs[0].i32 = (int32_t)first;
   if (fixture->parameter_count > 1U)
      inputs[1].i32 = second;
   return compareValues(fixture, function, inputs, jit_artifact,
#if defined(TCIR_HAS_AOT)
                        aot_entry,
#endif
                        diagnostic);
}

static int compareF64Input(
   const TCIRConverterFixture *fixture,
   const TCIRFunction *function,
   double first,
   double second,
   const void *jit_artifact,
#if defined(TCIR_HAS_AOT)
   TCCompiledEntry aot_entry,
#endif
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue inputs[2];
   memset(inputs, 0, sizeof(inputs));
   inputs[0].f64 = first;
   inputs[1].f64 = second;
   return compareValues(fixture, function, inputs, jit_artifact,
#if defined(TCIR_HAS_AOT)
                        aot_entry,
#endif
                        diagnostic);
}

static int compareRefInput(
   const TCIRConverterFixture *fixture,
   const TCIRFunction *function,
   void *first,
   void *second,
   const void *jit_artifact,
#if defined(TCIR_HAS_AOT)
   TCCompiledEntry aot_entry,
#endif
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue inputs[2];
   memset(inputs, 0, sizeof(inputs));
   inputs[0].ref = first;
   inputs[1].ref = second;
   return compareValues(fixture, function, inputs, jit_artifact,
#if defined(TCIR_HAS_AOT)
                        aot_entry,
#endif
                        diagnostic);
}

static int compareObjectAllocation(
   const TCIRConverterFixture *fixture,
   const TCIRFunction *function,
   const void *jit_artifact,
#if defined(TCIR_HAS_AOT)
   TCCompiledEntry aot_entry,
#endif
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue inputs[2];
   DifferentialResult reference;
   memset(inputs, 0, sizeof(inputs));
   reference = executeTCIR(function, fixture, inputs, diagnostic);
   if (reference.outcome != DIFFERENTIAL_RETURNED ||
       reference.return_type != TCIR_TYPE_REF ||
       reference.return_ref != &allocation_token)
      return 0;
#if defined(TCIR_HAS_SLJIT)
   {
      DifferentialResult sljit = executeSLJIT(
         (const TCIRJitArtifact *)jit_artifact, fixture, inputs);
      if (!resultsAgree(fixture, inputs, &reference, &sljit, "SLJIT allocation contract"))
         return 0;
   }
#else
   (void)jit_artifact;
#endif
#if defined(TCIR_HAS_AOT)
   {
      DifferentialResult aot = executeAOT(aot_entry, fixture, inputs);
      return resultsAgree(fixture, inputs, &reference, &aot, "AOT allocation contract");
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

static double f64FromBits(uint64_t bits)
{
   double value;
   memcpy(&value, &bits, sizeof(value));
   return value;
}

static double promotedF32FromBits(uint32_t bits)
{
   float value;
   memcpy(&value, &bits, sizeof(value));
   return (double)value;
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
   static const uint64_t pure_f64_cases[][2] = {
      { UINT64_C(0x0000000000000000), UINT64_C(0x0000000000000000) },
      { UINT64_C(0x8000000000000000), UINT64_C(0x0000000000000000) },
      { UINT64_C(0x3ff0000000000000), UINT64_C(0xbff0000000000000) },
      { UINT64_C(0xbff0000000000000), UINT64_C(0x4000000000000000) },
      { UINT64_C(0x7fefffffffffffff), UINT64_C(0x0000000000000001) },
      { UINT64_C(0x0010000000000000), UINT64_C(0x8000000000000001) },
      { UINT64_C(0x7ff0000000000000), UINT64_C(0x3ff0000000000000) },
      { UINT64_C(0xfff0000000000000), UINT64_C(0x3ff0000000000000) },
      { UINT64_C(0x7ff8000000000001), UINT64_C(0x3ff0000000000000) },
      { UINT64_C(0xfff8000000001234), UINT64_C(0x7ff0000000000000) }
   };
   static const uint32_t normalized_f32_cases[] = {
      UINT32_C(0x00000000), UINT32_C(0x80000000),
      UINT32_C(0x3f800000), UINT32_C(0xbf800000),
      UINT32_C(0x00000001), UINT32_C(0x80000001),
      UINT32_C(0x7f800000), UINT32_C(0xff800000),
      UINT32_C(0x7fc00001), UINT32_C(0xffc01234)
   };
   static int reference_token_a;
   static int reference_token_b;
   static void *const reference_values[] = { NULL, &reference_token_a, &reference_token_b };
   static const unsigned int reference_cases[][2] = {
      { 0U, 0U }, { 1U, 0U }, { 0U, 2U }, { 1U, 1U }, { 1U, 2U }, { 2U, 1U }
   };
   static const int32_t switch_cases[] = {
      INT32_MIN, -8, -7, -6, -1, 0, 1, 4, 5, 6, 1023, 1024, 1025, INT32_MAX
   };
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT];
   void *jit_artifacts[TCIR_CONVERTER_FIXTURE_COUNT] = { NULL };
#if defined(TCIR_HAS_AOT)
   static const char *const method_names[] = {
      "add", "abs", "sumTo", "pureI32", "pureI64", "pureF64", "normalizedF32",
      "i32ToF64", "i64ToF64", "selectRef", "referenceScore", "nullRef", "switchScore",
      "callStatic", "newObject"
   };
   static const char *const signatures[] = {
      "(II)I", "(I)I", "(I)I", "(II)I", "(JI)J", "(DD)D", "(F)F",
      "(I)D", "(J)D", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
      "(Ljava/lang/Object;Ljava/lang/Object;)I", "(Ljava/lang/Object;)Ljava/lang/Object;", "(I)I",
      "(II)I", "()Ljava/lang/Object;"
   };
   TCCompiledEntry aot_entries[TCIR_CONVERTER_FIXTURE_COUNT];
#endif
   uint32_t generated_state = UINT32_C(0x4d595df4);
   uint32_t generated_i64_state = UINT32_C(0x8a5cd789);
   uint32_t generated_f64_state = UINT32_C(0x31f2a8c7);
   uint32_t generated_f32_state = UINT32_C(0xc42b91e5);
   uint32_t generated_ref_state = UINT32_C(0x7f4a7c15);
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
         TCIRJitCompileStatus status = tcirJitCompile(
            functions[fixture_index], NULL, &artifact, &jit_diagnostic);
         if (status != TCIR_JIT_COMPILE_READY)
            fprintf(stderr, "SLJIT rejected %s at tc_pc %u: %s\n",
                    tcir_converter_fixtures[fixture_index].identity,
                    jit_diagnostic.tc_pc, jit_diagnostic.message);
         REQUIRE(status == TCIR_JIT_COMPILE_READY);
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
   for (case_index = 0U; case_index < sizeof(pure_f64_cases) / sizeof(pure_f64_cases[0]); ++case_index)
      REQUIRE(compareF64Input(
         &tcir_converter_fixtures[5], functions[5],
         f64FromBits(pure_f64_cases[case_index][0]), f64FromBits(pure_f64_cases[case_index][1]),
         jit_artifacts[5],
#if defined(TCIR_HAS_AOT)
         aot_entries[5],
#endif
         &diagnostic));
   for (case_index = 0U;
        case_index < sizeof(normalized_f32_cases) / sizeof(normalized_f32_cases[0]);
        ++case_index)
      REQUIRE(compareF64Input(
         &tcir_converter_fixtures[6], functions[6],
         promotedF32FromBits(normalized_f32_cases[case_index]), 0.0,
         jit_artifacts[6],
#if defined(TCIR_HAS_AOT)
         aot_entries[6],
#endif
         &diagnostic));
   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[7], functions[7], add_cases[case_index][0], 0,
                           jit_artifacts[7],
#if defined(TCIR_HAS_AOT)
                           aot_entries[7],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(pure_i64_cases) / sizeof(pure_i64_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[8], functions[8], pure_i64_cases[case_index][0], 0,
                           jit_artifacts[8],
#if defined(TCIR_HAS_AOT)
                           aot_entries[8],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(reference_cases) / sizeof(reference_cases[0]); ++case_index)
   {
      void *first = reference_values[reference_cases[case_index][0]];
      void *second = reference_values[reference_cases[case_index][1]];
      REQUIRE(compareRefInput(&tcir_converter_fixtures[9], functions[9], first, second,
                              jit_artifacts[9],
#if defined(TCIR_HAS_AOT)
                              aot_entries[9],
#endif
                              &diagnostic));
      REQUIRE(compareRefInput(&tcir_converter_fixtures[10], functions[10], first, second,
                              jit_artifacts[10],
#if defined(TCIR_HAS_AOT)
                              aot_entries[10],
#endif
                              &diagnostic));
   }
   for (case_index = 0U; case_index < sizeof(reference_values) / sizeof(reference_values[0]); ++case_index)
      REQUIRE(compareRefInput(&tcir_converter_fixtures[11], functions[11], reference_values[case_index], NULL,
                              jit_artifacts[11],
#if defined(TCIR_HAS_AOT)
                              aot_entries[11],
#endif
                              &diagnostic));
   for (case_index = 0U; case_index < sizeof(switch_cases) / sizeof(switch_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[12], functions[12], switch_cases[case_index], 0,
                           jit_artifacts[12],
#if defined(TCIR_HAS_AOT)
                           aot_entries[12],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < sizeof(add_cases) / sizeof(add_cases[0]); ++case_index)
      REQUIRE(compareInput(&tcir_converter_fixtures[13], functions[13], add_cases[case_index][0],
                           add_cases[case_index][1], jit_artifacts[13],
#if defined(TCIR_HAS_AOT)
                           aot_entries[13],
#endif
                           &diagnostic));
   for (case_index = 0U; case_index < 16U; ++case_index)
      REQUIRE(compareObjectAllocation(
         &tcir_converter_fixtures[14], functions[14], jit_artifacts[14],
#if defined(TCIR_HAS_AOT)
         aot_entries[14],
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
      REQUIRE(compareInput(&tcir_converter_fixtures[7], functions[7], first, 0,
                           jit_artifacts[7],
#if defined(TCIR_HAS_AOT)
                           aot_entries[7],
#endif
                           &diagnostic));
      REQUIRE(compareInput(&tcir_converter_fixtures[12], functions[12], first, 0,
                           jit_artifacts[12],
#if defined(TCIR_HAS_AOT)
                           aot_entries[12],
#endif
                           &diagnostic));
      REQUIRE(compareInput(&tcir_converter_fixtures[13], functions[13], first, second,
                           jit_artifacts[13],
#if defined(TCIR_HAS_AOT)
                           aot_entries[13],
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
         REQUIRE(compareInput(&tcir_converter_fixtures[8], functions[8], long_value, 0,
                              jit_artifacts[8],
#if defined(TCIR_HAS_AOT)
                              aot_entries[8],
#endif
                              &diagnostic));
      }
      {
         uint64_t first_bits = ((uint64_t)nextGeneratedValue(&generated_f64_state) << 32)
            | (uint64_t)nextGeneratedValue(&generated_f64_state);
         uint64_t second_bits = ((uint64_t)nextGeneratedValue(&generated_f64_state) << 32)
            | (uint64_t)nextGeneratedValue(&generated_f64_state);
         REQUIRE(compareF64Input(&tcir_converter_fixtures[5], functions[5],
                                 f64FromBits(first_bits), f64FromBits(second_bits),
                                 jit_artifacts[5],
#if defined(TCIR_HAS_AOT)
                                 aot_entries[5],
#endif
                                 &diagnostic));
      }
      REQUIRE(compareF64Input(
         &tcir_converter_fixtures[6], functions[6],
         promotedF32FromBits(nextGeneratedValue(&generated_f32_state)), 0.0,
         jit_artifacts[6],
#if defined(TCIR_HAS_AOT)
         aot_entries[6],
#endif
         &diagnostic));
      {
         void *first_ref = reference_values[nextGeneratedValue(&generated_ref_state) % 3U];
         void *second_ref = reference_values[nextGeneratedValue(&generated_ref_state) % 3U];
         REQUIRE(compareRefInput(&tcir_converter_fixtures[9], functions[9], first_ref, second_ref,
                                 jit_artifacts[9],
#if defined(TCIR_HAS_AOT)
                                 aot_entries[9],
#endif
                                 &diagnostic));
         REQUIRE(compareRefInput(&tcir_converter_fixtures[10], functions[10], first_ref, second_ref,
                                 jit_artifacts[10],
#if defined(TCIR_HAS_AOT)
                                 aot_entries[10],
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
   const unsigned int unsupported_code[] = { RETURN_symO };
   TCIRDiagnostic diagnostic;
   TCIRMethodView view;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function = NULL;

   REQUIRE(module != NULL);
   memset(&view, 0, sizeof(view));
   view.identity = "Fallback.constantRef:()V";
   view.code = unsupported_code;
   view.code_slot_count = sizeof(unsupported_code) / sizeof(unsupported_code[0]);
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
   printf("TCIR differential tests passed: 15 fixtures, 6,398 executeMethod/TCIR/SLJIT/AOT comparisons, "
          "16 TCIR/SLJIT/AOT allocation-contract comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789/0x31f2a8c7/0xc42b91e5/0x7f4a7c15.\n");
#else
   printf("TCIR differential tests passed: 15 fixtures, 6,398 executeMethod/TCIR/SLJIT comparisons, "
          "16 TCIR/SLJIT allocation-contract comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789/0x31f2a8c7/0xc42b91e5/0x7f4a7c15.\n");
#endif
#elif defined(TCIR_HAS_AOT)
   printf("TCIR differential tests passed: 15 fixtures, 6,398 executeMethod/TCIR/AOT comparisons, "
          "16 TCIR/AOT allocation-contract comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789/0x31f2a8c7/0xc42b91e5/0x7f4a7c15.\n");
#else
   printf("TCIR differential tests passed: 15 fixtures, 6,398 executeMethod comparisons, "
          "16 TCIR allocation-contract comparisons, "
          "fixed seeds 0x4d595df4/0x8a5cd789/0x31f2a8c7/0xc42b91e5/0x7f4a7c15.\n");
#endif
   return 0;
}
