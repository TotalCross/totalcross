// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir.h"
#include "tcir_frontend.h"
#include "tcir_interp.h"
#include "tcir_opcode_map.h"

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifndef TCIR_GOLDEN_DIR
#define TCIR_GOLDEN_DIR "golden"
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

#define BUILD_REQUIRE(condition) \
   do \
   { \
      if (!(condition)) \
         return NULL; \
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

static TCIRSourceLocation source(unsigned int tc_pc, int line)
{
   TCIRSourceLocation location;
   location.tc_pc = tc_pc;
   location.source_line = line;
   return location;
}

static int setAllSourceSlots(TCIRFunction *function, size_t count, TCIRDiagnostic *diagnostic)
{
   unsigned char starts[32];
   if (count > sizeof(starts))
      return 0;
   memset(starts, 1, count);
   return tcirFunctionSetSourceSlots(function, count, starts, diagnostic) == TCIR_STATUS_OK;
}

static TCIRValue *appendOperation(
   TCIRBlock *block,
   TCIROperation opcode,
   TCIRType result_type,
   const TCIRValue *const *operands,
   size_t operand_count,
   int immediate,
   unsigned int tc_pc,
   TCIRDiagnostic *diagnostic)
{
   TCIROperationSpec spec;
   TCIRValue *result = NULL;

   memset(&spec, 0, sizeof(spec));
   spec.opcode = opcode;
   spec.result_type = result_type;
   spec.operands = operands;
   spec.operand_count = operand_count;
   spec.immediate_i32 = immediate;
   spec.source = source(tc_pc, -1);
   if (tcirBlockAppendOperation(block, &spec, &result, diagnostic) != TCIR_STATUS_OK)
      return NULL;
   return result;
}

static TCIRValue *appendConst(
   TCIRBlock *block,
   int value,
   unsigned int tc_pc,
   TCIRDiagnostic *diagnostic)
{
   return appendOperation(block, TCIR_OP_CONST_I32, TCIR_TYPE_I32, NULL, 0, value, tc_pc, diagnostic);
}

static int setTerminator(
   TCIRBlock *block,
   TCIRTerminatorKind kind,
   const TCIRValue *value,
   const TCIREdge *edges,
   size_t edge_count,
   unsigned int tc_pc,
   TCIRDiagnostic *diagnostic)
{
   TCIRTerminatorSpec spec;
   memset(&spec, 0, sizeof(spec));
   spec.kind = kind;
   spec.value = value;
   spec.edges = edges;
   spec.edge_count = edge_count;
   spec.source = source(tc_pc, -1);
   return tcirBlockSetTerminator(block, &spec, diagnostic) == TCIR_STATUS_OK;
}

static TCIRFunction *buildAdd(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32, TCIR_TYPE_I32 };
   const TCIRValue *operands[2];
   TCIRFunction *function = tcirModuleAddFunction(
      module, "Example.add:(II)I", parameters, 2, TCIR_TYPE_I32, diagnostic);
   TCIRBlock *block;
   TCIRValue *sum;

   BUILD_REQUIRE(function != NULL);
   BUILD_REQUIRE(tcirFunctionSetHomes(function, 2, 0, 0, diagnostic) == TCIR_STATUS_OK);
   BUILD_REQUIRE(setAllSourceSlots(function, 2, diagnostic));
   block = tcirFunctionAppendBlock(function, 0, source(0, 10), 0, diagnostic);
   BUILD_REQUIRE(block != NULL);
   operands[0] = tcirFunctionParameter(function, 0);
   operands[1] = tcirFunctionParameter(function, 1);
   sum = appendOperation(block, TCIR_OP_ADD_I32, TCIR_TYPE_I32, operands, 2, 0, 0, diagnostic);
   BUILD_REQUIRE(sum != NULL);
   BUILD_REQUIRE(setTerminator(block, TCIR_TERMINATOR_RETURN, sum, NULL, 0, 1, diagnostic));
   return function;
}

static TCIRFunction *buildAbs(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   const TCIRValue *operands[2];
   const TCIRValue *entry_arguments[1];
   TCIREdge edges[2];
   TCIRFunction *function = tcirModuleAddFunction(
      module, "Example.abs:(I)I", parameters, 1, TCIR_TYPE_I32, diagnostic);
   TCIRBlock *entry;
   TCIRBlock *negative;
   TCIRBlock *positive;
   TCIRValue *negative_argument;
   TCIRValue *positive_argument;
   TCIRValue *zero;
   TCIRValue *condition;
   TCIRValue *negative_zero;
   TCIRValue *result;

   BUILD_REQUIRE(function != NULL);
   BUILD_REQUIRE(tcirFunctionSetHomes(function, 2, 0, 0, diagnostic) == TCIR_STATUS_OK);
   BUILD_REQUIRE(setAllSourceSlots(function, 5, diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, 20), 0, diagnostic);
   negative = tcirFunctionAppendBlock(function, 1, source(2, 21), 0, diagnostic);
   positive = tcirFunctionAppendBlock(function, 2, source(4, 22), 0, diagnostic);
   BUILD_REQUIRE(entry != NULL && negative != NULL && positive != NULL);
   negative_argument = tcirBlockAppendArgument(negative, TCIR_TYPE_I32, diagnostic);
   positive_argument = tcirBlockAppendArgument(positive, TCIR_TYPE_I32, diagnostic);
   BUILD_REQUIRE(negative_argument != NULL && positive_argument != NULL);

   zero = appendConst(entry, 0, 0, diagnostic);
   BUILD_REQUIRE(zero != NULL);
   operands[0] = tcirFunctionParameter(function, 0);
   operands[1] = zero;
   condition = appendOperation(entry, TCIR_OP_CMP_LT_I32, TCIR_TYPE_I1, operands, 2, 0, 1, diagnostic);
   BUILD_REQUIRE(condition != NULL);
   entry_arguments[0] = tcirFunctionParameter(function, 0);
   memset(edges, 0, sizeof(edges));
   edges[0].target = negative;
   edges[0].arguments = entry_arguments;
   edges[0].argument_count = 1;
   edges[1].target = positive;
   edges[1].arguments = entry_arguments;
   edges[1].argument_count = 1;
   BUILD_REQUIRE(setTerminator(entry, TCIR_TERMINATOR_BRANCH_IF, condition, edges, 2, 1, diagnostic));

   negative_zero = appendConst(negative, 0, 2, diagnostic);
   BUILD_REQUIRE(negative_zero != NULL);
   operands[0] = negative_zero;
   operands[1] = negative_argument;
   result = appendOperation(negative, TCIR_OP_SUB_I32, TCIR_TYPE_I32, operands, 2, 0, 3, diagnostic);
   BUILD_REQUIRE(result != NULL);
   BUILD_REQUIRE(setTerminator(negative, TCIR_TERMINATOR_RETURN, result, NULL, 0, 3, diagnostic));
   BUILD_REQUIRE(setTerminator(positive, TCIR_TERMINATOR_RETURN, positive_argument, NULL, 0, 4, diagnostic));
   return function;
}

static TCIRFunction *buildSumTo(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   const TCIRValue *operands[2];
   const TCIRValue *entry_arguments[3];
   const TCIRValue *done_arguments[1];
   const TCIRValue *body_arguments[3];
   const TCIRValue *loop_arguments[3];
   TCIREdge edge;
   TCIREdge loop_edges[2];
   TCIRFunction *function = tcirModuleAddFunction(
      module, "Example.sumTo:(I)I", parameters, 1, TCIR_TYPE_I32, diagnostic);
   TCIRBlock *entry;
   TCIRBlock *loop;
   TCIRBlock *body;
   TCIRBlock *done;
   TCIRValue *loop_i;
   TCIRValue *loop_acc;
   TCIRValue *loop_n;
   TCIRValue *body_i;
   TCIRValue *body_acc;
   TCIRValue *body_n;
   TCIRValue *done_result;
   TCIRValue *zero;
   TCIRValue *condition;
   TCIRValue *next_acc;
   TCIRValue *one;
   TCIRValue *next_i;

   BUILD_REQUIRE(function != NULL);
   BUILD_REQUIRE(tcirFunctionSetHomes(function, 4, 0, 0, diagnostic) == TCIR_STATUS_OK);
   BUILD_REQUIRE(setAllSourceSlots(function, 10, diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, 30), 0, diagnostic);
   loop = tcirFunctionAppendBlock(function, 1, source(2, 31), 0, diagnostic);
   body = tcirFunctionAppendBlock(function, 2, source(5, 32), 0, diagnostic);
   done = tcirFunctionAppendBlock(function, 3, source(9, 33), 0, diagnostic);
   BUILD_REQUIRE(entry != NULL && loop != NULL && body != NULL && done != NULL);

   loop_i = tcirBlockAppendArgument(loop, TCIR_TYPE_I32, diagnostic);
   loop_acc = tcirBlockAppendArgument(loop, TCIR_TYPE_I32, diagnostic);
   loop_n = tcirBlockAppendArgument(loop, TCIR_TYPE_I32, diagnostic);
   body_i = tcirBlockAppendArgument(body, TCIR_TYPE_I32, diagnostic);
   body_acc = tcirBlockAppendArgument(body, TCIR_TYPE_I32, diagnostic);
   body_n = tcirBlockAppendArgument(body, TCIR_TYPE_I32, diagnostic);
   done_result = tcirBlockAppendArgument(done, TCIR_TYPE_I32, diagnostic);
   BUILD_REQUIRE(
      loop_i != NULL && loop_acc != NULL && loop_n != NULL && body_i != NULL &&
      body_acc != NULL && body_n != NULL && done_result != NULL);

   zero = appendConst(entry, 0, 0, diagnostic);
   BUILD_REQUIRE(zero != NULL);
   entry_arguments[0] = zero;
   entry_arguments[1] = zero;
   entry_arguments[2] = tcirFunctionParameter(function, 0);
   memset(&edge, 0, sizeof(edge));
   edge.target = loop;
   edge.arguments = entry_arguments;
   edge.argument_count = 3;
   BUILD_REQUIRE(setTerminator(entry, TCIR_TERMINATOR_BRANCH, NULL, &edge, 1, 1, diagnostic));

   operands[0] = loop_i;
   operands[1] = loop_n;
   condition = appendOperation(loop, TCIR_OP_CMP_GE_I32, TCIR_TYPE_I1, operands, 2, 0, 2, diagnostic);
   BUILD_REQUIRE(condition != NULL);
   done_arguments[0] = loop_acc;
   body_arguments[0] = loop_i;
   body_arguments[1] = loop_acc;
   body_arguments[2] = loop_n;
   memset(loop_edges, 0, sizeof(loop_edges));
   loop_edges[0].target = done;
   loop_edges[0].arguments = done_arguments;
   loop_edges[0].argument_count = 1;
   loop_edges[1].target = body;
   loop_edges[1].arguments = body_arguments;
   loop_edges[1].argument_count = 3;
   BUILD_REQUIRE(setTerminator(loop, TCIR_TERMINATOR_BRANCH_IF, condition, loop_edges, 2, 4, diagnostic));

   operands[0] = body_acc;
   operands[1] = body_i;
   next_acc = appendOperation(body, TCIR_OP_ADD_I32, TCIR_TYPE_I32, operands, 2, 0, 5, diagnostic);
   BUILD_REQUIRE(next_acc != NULL);
   one = appendConst(body, 1, 6, diagnostic);
   BUILD_REQUIRE(one != NULL);
   operands[0] = body_i;
   operands[1] = one;
   next_i = appendOperation(body, TCIR_OP_ADD_I32, TCIR_TYPE_I32, operands, 2, 0, 7, diagnostic);
   BUILD_REQUIRE(next_i != NULL);
   loop_arguments[0] = next_i;
   loop_arguments[1] = next_acc;
   loop_arguments[2] = body_n;
   memset(&edge, 0, sizeof(edge));
   edge.target = loop;
   edge.arguments = loop_arguments;
   edge.argument_count = 3;
   BUILD_REQUIRE(setTerminator(body, TCIR_TERMINATOR_BRANCH, NULL, &edge, 1, 8, diagnostic));
   BUILD_REQUIRE(setTerminator(done, TCIR_TERMINATOR_RETURN, done_result, NULL, 0, 9, diagnostic));
   return function;
}

static char *readGoldenPayload(const char *name)
{
   char path[1024];
   FILE *file;
   long length;
   char *data;
   char *payload;

   snprintf(path, sizeof(path), "%s/%s.tcir", TCIR_GOLDEN_DIR, name);
   file = fopen(path, "rb");
   if (file == NULL)
      return NULL;
   if (fseek(file, 0, SEEK_END) != 0)
   {
      fclose(file);
      return NULL;
   }
   length = ftell(file);
   if (length < 0 || fseek(file, 0, SEEK_SET) != 0)
   {
      fclose(file);
      return NULL;
   }
   data = (char *)malloc((size_t)length + 1);
   if (data == NULL)
   {
      fclose(file);
      return NULL;
   }
   if (fread(data, 1, (size_t)length, file) != (size_t)length)
   {
      fclose(file);
      free(data);
      return NULL;
   }
   fclose(file);
   data[length] = '\0';

   payload = data;
   while (*payload == ';' || *payload == '\r' || *payload == '\n')
   {
      char *newline = strchr(payload, '\n');
      if (newline == NULL)
         break;
      payload = newline + 1;
   }
   if (payload != data)
      memmove(data, payload, strlen(payload) + 1);
   return data;
}

static int writeGoldenPayload(const char *name, const char *payload)
{
   char path[1024];
   FILE *file;
   snprintf(path, sizeof(path), "%s/%s.tcir", TCIR_GOLDEN_DIR, name);
   file = fopen(path, "wb");
   if (file == NULL)
      return 0;
   fprintf(
      file,
      "; Copyright (C) 2026 Amalgam Solucoes em TI Ltda\n"
      ";\n"
      "; SPDX-License-Identifier: LGPL-2.1-only\n\n"
      "%s",
      payload);
   return fclose(file) == 0;
}

static int expectInvalid(
   const TCIRFunction *function,
   TCIRDiagnosticCode expected_code,
   unsigned int expected_tc_pc)
{
   TCIRDiagnostic first;
   TCIRDiagnostic second;

   REQUIRE(!tcirVerifyFunction(function, &first));
   REQUIRE(!tcirVerifyFunction(function, &second));
   REQUIRE(first.code == expected_code);
   REQUIRE(first.tc_pc == expected_tc_pc);
   REQUIRE(strcmp(first.function, tcirFunctionIdentity(function)) == 0);
   REQUIRE(first.code == second.code);
   REQUIRE(first.tc_pc == second.tc_pc);
   REQUIRE(strcmp(first.function, second.function) == 0);
   REQUIRE(strcmp(first.message, second.message) == 0);
   return 1;
}

static int checkGolden(
   TCIRModule *module,
   TCIRFunction *function,
   const char *name,
   TCIRDiagnostic *diagnostic)
{
   char *first = NULL;
   char *second = NULL;
   char *expected = NULL;
   int passed = 0;

   if (!tcirVerifyFunction(function, diagnostic))
      goto cleanup;
   first = tcirFunctionDump(function, diagnostic);
   second = tcirFunctionDump(function, diagnostic);
   if (first != NULL && getenv("TCIR_UPDATE_GOLDENS") != NULL && !writeGoldenPayload(name, first))
      goto cleanup;
   expected = readGoldenPayload(name);
   if (first == NULL || second == NULL || expected == NULL)
      goto cleanup;
   passed = strcmp(first, second) == 0 && strcmp(first, expected) == 0;
   if (!passed)
      fprintf(stderr, "golden mismatch for %s\nexpected:\n%s\nactual:\n%s", name, expected, first);

cleanup:
   free(expected);
   tcirFreeText(module, first);
   tcirFreeText(module, second);
   return passed;
}

static int testConstructedFunctions(void)
{
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *add;
   TCIRFunction *absolute;
   TCIRFunction *sum_to;
   TCIROperationView operation;
   TCIRTerminatorView terminator;

   REQUIRE(module != NULL);
   add = buildAdd(module, &diagnostic);
   absolute = buildAbs(module, &diagnostic);
   sum_to = buildSumTo(module, &diagnostic);
   REQUIRE(add != NULL && absolute != NULL && sum_to != NULL);
   REQUIRE(tcirModuleFunctionCount(module) == 3);
   REQUIRE(tcirModuleFunctionAt(module, 0) == add);
   REQUIRE(tcirModuleFunctionAt(module, 3) == NULL);
   REQUIRE(tcirFunctionHomeCount(add, TCIR_HOME_I32) == 2);
   REQUIRE(tcirFunctionHomeCount(add, TCIR_HOME_REF) == 0);
   REQUIRE(tcirFunctionSourceSlotCount(add) == 2);
   REQUIRE(tcirFunctionSourceSlotIsInstructionStart(add, 0));
   REQUIRE(!tcirFunctionSourceSlotIsInstructionStart(add, 2));
   REQUIRE(tcirFunctionBlockCount(add) == 1);
   REQUIRE(tcirBlockOperationCount(tcirFunctionBlockAt(add, 0)) == 1);
   REQUIRE(tcirBlockOperationAt(tcirFunctionBlockAt(add, 0), 0, &operation) == TCIR_STATUS_OK);
   REQUIRE(operation.opcode == TCIR_OP_ADD_I32);
   REQUIRE(operation.operand_count == 2);
   REQUIRE(tcirBlockTerminator(tcirFunctionBlockAt(add, 0), &terminator) == TCIR_STATUS_OK);
   REQUIRE(terminator.kind == TCIR_TERMINATOR_RETURN);

   REQUIRE(tcirVerifyFunction(add, &diagnostic));
   REQUIRE(tcirVerifyFunction(absolute, &diagnostic));
   REQUIRE(tcirVerifyFunction(sum_to, &diagnostic));

   tcirModuleDestroy(module);
   return 1;
}

static TCIRInterpreterStatus interpretI32(
   const TCIRFunction *function,
   const int *arguments,
   size_t argument_count,
   size_t max_steps,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic)
{
   TCIRRuntimeValue runtime_arguments[2];
   TCIRInterpreterOptions options;
   TCIRInterpreterFrame frame;
   int32_t i32_homes[8];
   size_t index;

   memset(runtime_arguments, 0, sizeof(runtime_arguments));
   memset(i32_homes, 0, sizeof(i32_homes));
   for (index = 0U; index < argument_count; ++index)
      runtime_arguments[index].i32 = (int32_t)arguments[index];
   memset(&frame, 0, sizeof(frame));
   frame.i32_homes = i32_homes;
   frame.i32_home_count = sizeof(i32_homes) / sizeof(i32_homes[0]);
   frame.arguments = runtime_arguments;
   frame.argument_count = argument_count;
   frame.tc_pc = TCIR_TCPC_NONE;
   options.max_steps = max_steps;
   return tcirInterpretFunction(function, &frame, &options, result, diagnostic);
}

static TCIRFunction *buildHomeRoundTrip(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   const TCIRValue *operands[1];
   TCIROperationSpec spec;
   TCIRFunction *function = tcirModuleAddFunction(
      module, "Example.homeRoundTrip:(I)I", parameters, 1, TCIR_TYPE_I32, diagnostic);
   TCIRBlock *entry;
   TCIRValue *loaded;

   BUILD_REQUIRE(function != NULL);
   BUILD_REQUIRE(tcirFunctionSetHomes(function, 1, 0, 0, diagnostic) == TCIR_STATUS_OK);
   BUILD_REQUIRE(setAllSourceSlots(function, 3, diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, 40), 0, diagnostic);
   BUILD_REQUIRE(entry != NULL);

   operands[0] = tcirFunctionParameter(function, 0);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_STORE_SLOT;
   spec.result_type = TCIR_TYPE_VOID;
   spec.operands = operands;
   spec.operand_count = 1;
   spec.home_bank = TCIR_HOME_I32;
   spec.home_index = 0;
   spec.source = source(0, 40);
   BUILD_REQUIRE(tcirBlockAppendOperation(entry, &spec, NULL, diagnostic) == TCIR_STATUS_OK);

   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_LOAD_SLOT;
   spec.result_type = TCIR_TYPE_I32;
   spec.home_bank = TCIR_HOME_I32;
   spec.home_index = 0;
   spec.source = source(1, 40);
   BUILD_REQUIRE(tcirBlockAppendOperation(entry, &spec, &loaded, diagnostic) == TCIR_STATUS_OK);
   BUILD_REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, loaded, NULL, 0, 2, diagnostic));
   return function;
}

static int testReferenceInterpreter(void)
{
   const int add_inputs[][2] = {
      { 0, 0 },
      { 19, 23 },
      { -19, 23 },
      { INT_MAX, 1 },
      { INT_MIN, -1 }
   };
   const int add_outputs[] = { 0, 42, 4, INT_MIN, INT_MAX };
   const int abs_inputs[] = { 0, 42, -42, INT_MAX, INT_MIN };
   const int abs_outputs[] = { 0, 42, 42, INT_MAX, INT_MIN };
   const int sum_inputs[] = { -7, 0, 1, 10, 100 };
   const int sum_outputs[] = { 0, 0, 0, 45, 4950 };
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *add;
   TCIRFunction *absolute;
   TCIRFunction *sum_to;
   TCIRFunction *home_round_trip;
   TCIRInterpreterResult result;
   size_t index;

   REQUIRE(module != NULL);
   add = buildAdd(module, &diagnostic);
   absolute = buildAbs(module, &diagnostic);
   sum_to = buildSumTo(module, &diagnostic);
   home_round_trip = buildHomeRoundTrip(module, &diagnostic);
   REQUIRE(add != NULL && absolute != NULL && sum_to != NULL && home_round_trip != NULL);

   for (index = 0U; index < sizeof(add_inputs) / sizeof(add_inputs[0]); ++index)
   {
      REQUIRE(interpretI32(add, add_inputs[index], 2, 0, &result, &diagnostic) == TCIR_INTERPRETER_RETURNED);
      REQUIRE(result.type == TCIR_TYPE_I32);
      REQUIRE(result.value.i32 == add_outputs[index]);
      REQUIRE(result.tc_pc == 1U);
   }
   for (index = 0U; index < sizeof(abs_inputs) / sizeof(abs_inputs[0]); ++index)
   {
      REQUIRE(interpretI32(absolute, &abs_inputs[index], 1, 0, &result, &diagnostic)
              == TCIR_INTERPRETER_RETURNED);
      REQUIRE(result.value.i32 == abs_outputs[index]);
   }
   for (index = 0U; index < sizeof(sum_inputs) / sizeof(sum_inputs[0]); ++index)
   {
      REQUIRE(interpretI32(sum_to, &sum_inputs[index], 1, 0, &result, &diagnostic)
              == TCIR_INTERPRETER_RETURNED);
      REQUIRE(result.value.i32 == sum_outputs[index]);
   }
   REQUIRE(interpretI32(home_round_trip, &abs_inputs[1], 1, 0, &result, &diagnostic)
           == TCIR_INTERPRETER_RETURNED);
   REQUIRE(result.value.i32 == abs_inputs[1]);

   REQUIRE(interpretI32(sum_to, &sum_inputs[3], 1, 3, &result, &diagnostic)
           == TCIR_INTERPRETER_STEP_LIMIT);
   REQUIRE(result.status == TCIR_INTERPRETER_STEP_LIMIT);
   REQUIRE(result.steps == 3U);
   REQUIRE(diagnostic.code == TCIR_DIAGNOSTIC_EXECUTION_LIMIT);

   tcirModuleDestroy(module);
   return 1;
}

static int testInterpreterRejectsBeforeExecution(void)
{
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRSymbol *helper;
   TCIRFunction *unsupported;
   TCIRFunction *invalid;
   TCIRBlock *entry;
   TCIRInterpreterFrame frame;
   TCIRInterpreterResult result;
   int32_t home = 123456;

   REQUIRE(module != NULL);
   helper = tcirModuleAddSymbol(
      module, TCIR_SYMBOL_HELPER, "runtime", "noop", "()V", 0, TCIR_EFFECT_NONE, &diagnostic);
   unsupported = tcirModuleAddFunction(module, "Fallback.noop:()V", NULL, 0, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(helper != NULL && unsupported != NULL);
   REQUIRE(tcirFunctionSetHomes(unsupported, 1, 0, 0, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setAllSourceSlots(unsupported, 2, &diagnostic));
   entry = tcirFunctionAppendBlock(unsupported, 0, source(0, 50), 0, &diagnostic);
   REQUIRE(entry != NULL);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_RUNTIME_CALL;
   spec.result_type = TCIR_TYPE_VOID;
   spec.symbol = helper;
   spec.source = source(0, 50);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, NULL, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 1, &diagnostic));
   REQUIRE(tcirVerifyFunction(unsupported, &diagnostic));

   memset(&frame, 0, sizeof(frame));
   frame.i32_homes = &home;
   frame.i32_home_count = 1;
   frame.tc_pc = 99U;
   REQUIRE(tcirInterpretFunction(unsupported, &frame, NULL, &result, &diagnostic)
           == TCIR_INTERPRETER_REJECTED);
   REQUIRE(diagnostic.code == TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE);
   REQUIRE(home == 123456);
   REQUIRE(frame.tc_pc == 99U);

   invalid = tcirModuleAddFunction(module, "Invalid.execute:()I", NULL, 0, TCIR_TYPE_I32, &diagnostic);
   REQUIRE(invalid != NULL && setAllSourceSlots(invalid, 1, &diagnostic));
   entry = tcirFunctionAppendBlock(invalid, 0, source(0, 51), 0, &diagnostic);
   REQUIRE(entry != NULL);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 0, &diagnostic));
   REQUIRE(tcirInterpretFunction(invalid, &frame, NULL, &result, &diagnostic)
           == TCIR_INTERPRETER_REJECTED);
   REQUIRE(diagnostic.code == TCIR_DIAGNOSTIC_RETURN_TYPE);
   REQUIRE(home == 123456);
   REQUIRE(frame.tc_pc == 99U);

   tcirModuleDestroy(module);
   return 1;
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
   return 1;
}

static int testConverterFrontendFixtures(void)
{
   static const char *const golden_names[] = {
      "frontend-add", "frontend-abs", "frontend-sumTo", "frontend-pureI32", "frontend-pureI64"
   };
   static const size_t expected_block_counts[] = { 1, 4, 4, 1, 4 };
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   size_t fixture_index;

   REQUIRE(module != NULL);
   REQUIRE(TCIR_CONVERTER_FIXTURE_COUNT == 5U);
   for (fixture_index = 0; fixture_index < TCIR_CONVERTER_FIXTURE_COUNT; fixture_index++)
   {
      const TCIRConverterFixture *fixture = &tcir_converter_fixtures[fixture_index];
      TCIRMethodParameter parameters[2];
      TCIRMethodView view;
      TCIRFunction *first = NULL;
      TCIRFunction *second = NULL;
      char *first_dump;
      char *second_dump;
      size_t pc;

      REQUIRE(buildFixtureView(fixture, &view, parameters));
      REQUIRE(tcirFrontendBuildFunction(module, &view, &first, &diagnostic) == TCIR_FRONTEND_OK);
      REQUIRE(first != NULL);
      REQUIRE(tcirVerifyFunction(first, &diagnostic));
      REQUIRE(tcirFunctionBlockCount(first) == expected_block_counts[fixture_index]);
      REQUIRE(tcirFunctionSourceSlotCount(first) == fixture->code_count);
      for (pc = 0; pc < fixture->code_count; pc++)
         REQUIRE(tcirFunctionSourceSlotIsInstructionStart(first, pc));
      REQUIRE(checkGolden(module, first, golden_names[fixture_index], &diagnostic));

      REQUIRE(tcirFrontendBuildFunction(module, &view, &second, &diagnostic) == TCIR_FRONTEND_OK);
      REQUIRE(second != NULL && tcirVerifyFunction(second, &diagnostic));
      first_dump = tcirFunctionDump(first, &diagnostic);
      second_dump = tcirFunctionDump(second, &diagnostic);
      REQUIRE(first_dump != NULL && second_dump != NULL);
      REQUIRE(strcmp(first_dump, second_dump) == 0);
      tcirFreeText(module, first_dump);
      tcirFreeText(module, second_dump);
   }
   tcirModuleDestroy(module);
   return 1;
}

static int expectFrontendDiagnostic(
   const TCIRMethodView *view,
   TCIRFrontendResult expected_result,
   TCIRDiagnosticCode expected_code,
   unsigned int expected_pc)
{
   TCIRDiagnostic first;
   TCIRDiagnostic second;
   TCIRModule *first_module = tcirModuleCreate(NULL, &first);
   TCIRModule *second_module = tcirModuleCreate(NULL, &second);
   TCIRFunction *function = NULL;

   REQUIRE(first_module != NULL && second_module != NULL);
   REQUIRE(tcirFrontendBuildFunction(first_module, view, &function, &first) == expected_result);
   REQUIRE(function == NULL);
   REQUIRE(tcirFrontendBuildFunction(second_module, view, &function, &second) == expected_result);
   REQUIRE(function == NULL);
   REQUIRE(first.code == expected_code && first.tc_pc == expected_pc);
   REQUIRE(first.code == second.code && first.tc_pc == second.tc_pc);
   REQUIRE(strcmp(first.function, second.function) == 0);
   REQUIRE(strcmp(first.message, second.message) == 0);
   REQUIRE(tcirModuleFunctionCount(first_module) == 0);
   REQUIRE(tcirModuleFunctionCount(second_module) == 0);
   tcirModuleDestroy(first_module);
   tcirModuleDestroy(second_module);
   return 1;
}

static int resolveLargeCall(void *user_data, unsigned int symbol, TCIRCallShape *shape)
{
   (void)user_data;
   (void)symbol;
   shape->parameter_count = 6;
   shape->returns_value = 1;
   return 1;
}

static TCIRMethodView diagnosticView(
   const char *identity,
   const unsigned int *code,
   size_t code_count,
   TCIRType return_type)
{
   static const int constants[] = { 0 };
   TCIRMethodView view;
   memset(&view, 0, sizeof(view));
   view.identity = identity;
   view.code = code;
   view.code_slot_count = code_count;
   view.i32_home_count = 1;
   view.return_type = return_type;
   view.i32_constants = constants;
   view.i32_constant_count = sizeof(constants) / sizeof(constants[0]);
   return view;
}

static int testFrontendDiagnostics(void)
{
   const unsigned int malformed_switch[] = { 145U | (1U << 16) };
   const unsigned int malformed_call[] = { 153U };
   const unsigned int continuation_target[] = {
      123U | (2U << 8),
      145U | (1U << 16),
      4U,
      7U,
      4U,
      136U
   };
   const unsigned int invalid_register[] = { 7U | (3U << 8), 133U };
   const unsigned int invalid_symbol[] = { 6U | (1U << 16), 133U };
   const unsigned int returns_void[] = { 136U };
   const unsigned int unsupported[] = { 149U, 136U };
   const unsigned int unsupported_long_division[] = { 65U, 136U };
   const unsigned int supported_call_shape[] = { 153U, 0U, 0U, 136U };
   TCIRMethodView view;
   TCIRMethodParameter parameter;
   TCIRMethodHandler handler;

   view = diagnosticView("Invalid.switch:()V", malformed_switch, 1, TCIR_TYPE_VOID);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_MALFORMED_CONTINUATION, 0));

   view = diagnosticView("Invalid.call:()V", malformed_call, 1, TCIR_TYPE_VOID);
   view.resolve_call_shape = resolveLargeCall;
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_MALFORMED_CONTINUATION, 0));

   view = diagnosticView("Invalid.target:()V", continuation_target, 6, TCIR_TYPE_VOID);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_INVALID_TARGET, 0));

   view = diagnosticView("Invalid.register:()I", invalid_register, 2, TCIR_TYPE_I32);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_INVALID_REGISTER, 0));

   view = diagnosticView("Invalid.symbol:()I", invalid_symbol, 2, TCIR_TYPE_I32);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_INVALID_SYMBOL, 0));

   view = diagnosticView("Invalid.handler:()V", returns_void, 1, TCIR_TYPE_VOID);
   handler.start_pc = 0;
   handler.end_pc = 0;
   handler.handler_pc = 0;
   handler.exception_home = 0;
   view.handlers = &handler;
   view.handler_count = 1;
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_INVALID_HANDLER, 0));

   view = diagnosticView("Invalid.merge:(O)V", returns_void, 1, TCIR_TYPE_VOID);
   parameter.type = TCIR_TYPE_REF;
   parameter.home_bank = TCIR_HOME_I32;
   parameter.home_index = 0;
   view.parameters = &parameter;
   view.parameter_count = 1;
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_ERROR, TCIR_DIAGNOSTIC_TYPE_MERGE, 0));

   view = diagnosticView("Fallback.object:()V", unsupported, 2, TCIR_TYPE_VOID);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_FALLBACK, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE, 0));

   view = diagnosticView("Fallback.longDivision:()V", unsupported_long_division, 2, TCIR_TYPE_VOID);
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_FALLBACK, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE, 0));

   view = diagnosticView("Fallback.call:()V", supported_call_shape, 4, TCIR_TYPE_VOID);
   view.resolve_call_shape = resolveLargeCall;
   REQUIRE(expectFrontendDiagnostic(
      &view, TCIR_FRONTEND_FALLBACK, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE, 0));
   return 1;
}

static int testUndefinedValue(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   const TCIRValue *operands[2];
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRFunction *foreign;
   TCIRBlock *block;
   TCIRValue *constant;
   TCIRValue *result;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.undefined:()I", NULL, 0, TCIR_TYPE_I32, &diagnostic);
   foreign = tcirModuleAddFunction(module, "Foreign.value:(I)I", parameters, 1, TCIR_TYPE_I32, &diagnostic);
   REQUIRE(function != NULL && foreign != NULL);
   REQUIRE(setAllSourceSlots(function, 2, &diagnostic));
   block = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(block != NULL);
   constant = appendConst(block, 1, 0, &diagnostic);
   REQUIRE(constant != NULL);
   operands[0] = constant;
   operands[1] = tcirFunctionParameter(foreign, 0);
   result = appendOperation(block, TCIR_OP_ADD_I32, TCIR_TYPE_I32, operands, 2, 0, 0, &diagnostic);
   REQUIRE(result != NULL);
   REQUIRE(setTerminator(block, TCIR_TERMINATOR_RETURN, result, NULL, 0, 1, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_UNDEFINED_VALUE, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testBlockArgumentType(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   const TCIRValue *arguments[1];
   TCIREdge edge;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRBlock *entry;
   TCIRBlock *target;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.blockArgs:(I)V", parameters, 1, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(function != NULL && setAllSourceSlots(function, 2, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   target = tcirFunctionAppendBlock(function, 1, source(1, -1), 0, &diagnostic);
   REQUIRE(entry != NULL && target != NULL);
   REQUIRE(tcirBlockAppendArgument(target, TCIR_TYPE_I64, &diagnostic) != NULL);
   arguments[0] = tcirFunctionParameter(function, 0);
   memset(&edge, 0, sizeof(edge));
   edge.target = target;
   edge.arguments = arguments;
   edge.argument_count = 1;
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_BRANCH, NULL, &edge, 1, 0, &diagnostic));
   REQUIRE(setTerminator(target, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 1, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_BLOCK_ARGUMENT_TYPE, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testInvalidTerminator(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_I32 };
   TCIREdge edges[2];
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRBlock *entry;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.terminator:(I)V", parameters, 1, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(function != NULL && setAllSourceSlots(function, 1, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   memset(edges, 0, sizeof(edges));
   edges[0].target = entry;
   edges[1].target = entry;
   REQUIRE(setTerminator(
      entry,
      TCIR_TERMINATOR_BRANCH_IF,
      tcirFunctionParameter(function, 0),
      edges,
      2,
      0,
      &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_INVALID_TERMINATOR, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testWrongReturn(void)
{
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRBlock *entry;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.return:()I", NULL, 0, TCIR_TYPE_I32, &diagnostic);
   REQUIRE(function != NULL && setAllSourceSlots(function, 1, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 0, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_RETURN_TYPE, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testMissingGCHome(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_REF };
   const TCIRValue *operands[1];
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRSymbol *helper;
   TCIRFunction *function;
   TCIRBlock *entry;

   REQUIRE(module != NULL);
   helper = tcirModuleAddSymbol(
      module, TCIR_SYMBOL_HELPER, "runtime", "collect", "(O)V", 0, TCIR_EFFECT_MAY_GC, &diagnostic);
   function = tcirModuleAddFunction(module, "Invalid.gc:(O)O", parameters, 1, TCIR_TYPE_REF, &diagnostic);
   REQUIRE(helper != NULL && function != NULL);
   REQUIRE(tcirFunctionSetHomes(function, 0, 1, 0, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setAllSourceSlots(function, 2, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   operands[0] = tcirFunctionParameter(function, 0);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_RUNTIME_CALL;
   spec.result_type = TCIR_TYPE_VOID;
   spec.operands = operands;
   spec.operand_count = 1;
   spec.symbol = helper;
   spec.effects = TCIR_EFFECT_MAY_GC;
   spec.source = source(0, -1);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, NULL, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setTerminator(
      entry,
      TCIR_TERMINATOR_RETURN,
      tcirFunctionParameter(function, 0),
      NULL,
      0,
      1,
      &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_GC_HOME, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testSourceContinuation(void)
{
   unsigned char starts[] = { 1, 0 };
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRBlock *entry;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.source:()V", NULL, 0, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(function != NULL);
   REQUIRE(tcirFunctionSetSourceSlots(function, 2, starts, &diagnostic) == TCIR_STATUS_OK);
   entry = tcirFunctionAppendBlock(function, 0, source(1, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 1, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_SOURCE_TARGET, 1));
   tcirModuleDestroy(module);
   return 1;
}

static int testHelperEffects(void)
{
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRSymbol *helper;
   TCIRFunction *function;
   TCIRBlock *entry;

   REQUIRE(module != NULL);
   helper = tcirModuleAddSymbol(
      module, TCIR_SYMBOL_HELPER, "runtime", "read", "()V", 0, TCIR_EFFECT_READS_HEAP, &diagnostic);
   function = tcirModuleAddFunction(module, "Invalid.effects:()V", NULL, 0, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(helper != NULL && function != NULL && setAllSourceSlots(function, 1, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_RUNTIME_CALL;
   spec.result_type = TCIR_TYPE_VOID;
   spec.symbol = helper;
   spec.effects = TCIR_EFFECT_WRITES_HEAP;
   spec.source = source(0, -1);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, NULL, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 0, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_HELPER_EFFECTS, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testUncheckedArrayProof(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_REF, TCIR_TYPE_I32, TCIR_TYPE_I32 };
   const TCIRValue *check_operands[2];
   const TCIRValue *load_operands[3];
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function;
   TCIRBlock *entry;
   TCIRValue *proof;
   TCIRValue *loaded;

   REQUIRE(module != NULL);
   function = tcirModuleAddFunction(module, "Invalid.array:(OII)I", parameters, 3, TCIR_TYPE_I32, &diagnostic);
   REQUIRE(function != NULL && setAllSourceSlots(function, 2, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   check_operands[0] = tcirFunctionParameter(function, 0);
   check_operands[1] = tcirFunctionParameter(function, 2);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_BOUNDS_CHECK;
   spec.result_type = TCIR_TYPE_TOKEN;
   spec.operands = check_operands;
   spec.operand_count = 2;
   spec.effects = TCIR_EFFECT_MAY_THROW;
   spec.propagates_exception = 1;
   spec.source = source(0, -1);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, &proof, &diagnostic) == TCIR_STATUS_OK);
   load_operands[0] = tcirFunctionParameter(function, 0);
   load_operands[1] = tcirFunctionParameter(function, 1);
   load_operands[2] = proof;
   loaded = appendOperation(
      entry,
      TCIR_OP_ARRAY_LOAD_UNCHECKED,
      TCIR_TYPE_I32,
      load_operands,
      3,
      0,
      1,
      &diagnostic);
   REQUIRE(loaded != NULL);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, loaded, NULL, 0, 1, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_UNCHECKED_ARRAY_PROOF, 1));
   tcirModuleDestroy(module);
   return 1;
}

static int testInternalAddressLifetime(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_REF };
   const TCIRValue *address_operands[1];
   const TCIRValue *copy_operands[1];
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRSymbol *helper;
   TCIRFunction *function;
   TCIRBlock *entry;
   TCIRValue *address;
   TCIRValue *copy;

   REQUIRE(module != NULL);
   helper = tcirModuleAddSymbol(
      module, TCIR_SYMBOL_HELPER, "runtime", "collect", "()V", 0, TCIR_EFFECT_MAY_GC, &diagnostic);
   function = tcirModuleAddFunction(module, "Invalid.address:(O)V", parameters, 1, TCIR_TYPE_VOID, &diagnostic);
   REQUIRE(helper != NULL && function != NULL && setAllSourceSlots(function, 3, &diagnostic));
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   address_operands[0] = tcirFunctionParameter(function, 0);
   address = appendOperation(
      entry,
      TCIR_OP_INTERNAL_ADDRESS,
      TCIR_TYPE_INTERNAL_ADDRESS,
      address_operands,
      1,
      0,
      0,
      &diagnostic);
   REQUIRE(address != NULL);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_RUNTIME_CALL;
   spec.result_type = TCIR_TYPE_VOID;
   spec.symbol = helper;
   spec.effects = TCIR_EFFECT_MAY_GC;
   spec.source = source(1, -1);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, NULL, &diagnostic) == TCIR_STATUS_OK);
   copy_operands[0] = address;
   copy = appendOperation(
      entry, TCIR_OP_COPY, TCIR_TYPE_INTERNAL_ADDRESS, copy_operands, 1, 0, 2, &diagnostic);
   REQUIRE(copy != NULL);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, NULL, NULL, 0, 2, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_INTERNAL_ADDRESS_LIFETIME, 1));
   tcirModuleDestroy(module);
   return 1;
}

static int testNonNullProof(void)
{
   const TCIRType parameters[] = { TCIR_TYPE_REF };
   const TCIRValue *operands[1];
   TCIROperationSpec spec;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRSymbol *field;
   TCIRFunction *function;
   TCIRBlock *entry;
   TCIRValue *loaded;

   REQUIRE(module != NULL);
   field = tcirModuleAddSymbol(
      module, TCIR_SYMBOL_FIELD, "Example", "value", "LObject;", 1, TCIR_EFFECT_NONE, &diagnostic);
   function = tcirModuleAddFunction(
      module, "Invalid.nonNull:(O)O", parameters, 1, TCIR_TYPE_NON_NULL_REF, &diagnostic);
   REQUIRE(field != NULL && function != NULL && setAllSourceSlots(function, 2, &diagnostic));
   REQUIRE(tcirModuleSymbolCount(module) == 1);
   REQUIRE(tcirModuleSymbolAt(module, 0) == field);
   REQUIRE(tcirModuleSymbolAt(module, 1) == NULL);
   entry = tcirFunctionAppendBlock(function, 0, source(0, -1), 0, &diagnostic);
   REQUIRE(entry != NULL);
   operands[0] = tcirFunctionParameter(function, 0);
   memset(&spec, 0, sizeof(spec));
   spec.opcode = TCIR_OP_FIELD_LOAD;
   spec.result_type = TCIR_TYPE_NON_NULL_REF;
   spec.operands = operands;
   spec.operand_count = 1;
   spec.symbol = field;
   spec.source = source(0, -1);
   REQUIRE(tcirBlockAppendOperation(entry, &spec, &loaded, &diagnostic) == TCIR_STATUS_OK);
   REQUIRE(setTerminator(entry, TCIR_TERMINATOR_RETURN, loaded, NULL, 0, 1, &diagnostic));
   REQUIRE(expectInvalid(function, TCIR_DIAGNOSTIC_RESULT_TYPE, 0));
   tcirModuleDestroy(module);
   return 1;
}

static int testOpcodeRegistry(void)
{
   TCIRDiagnostic diagnostic;
   size_t index;
   REQUIRE(tcirOpcodeRegistryValidate(&diagnostic));
   REQUIRE(tcirOpcodeCount() == 160);
   for (index = 0; index < tcirOpcodeCount(); index++)
   {
      const TCIROpcodeInfo *info = tcirOpcodeAt(index);
      REQUIRE(info != NULL);
      REQUIRE(info == tcirOpcodeLookup(info->value));
      REQUIRE(strcmp(tcirDecoderClassName(info->decoder_class), "invalid") != 0);
      REQUIRE(strcmp(tcirLoweringClassName(info->lowering_class), "invalid") != 0);
      REQUIRE(strcmp(tcirPOCStatusName(info->poc_status), "invalid") != 0);
   }
   return 1;
}

typedef struct TCIRExceptionCapture
{
   unsigned int count;
   TCIRRuntimeExceptionKind kind;
   unsigned int tc_pc;
} TCIRExceptionCapture;

static void captureRuntimeException(
   void *runtime_context,
   TCIRRuntimeExceptionKind kind,
   unsigned int tc_pc)
{
   TCIRExceptionCapture *capture = (TCIRExceptionCapture *)runtime_context;
   capture->count++;
   capture->kind = kind;
   capture->tc_pc = tc_pc;
}

static int testCheckedI32Arithmetic(void)
{
   static const unsigned int code[] = {
      0x0100023fU, 0x01000343U, 0x0302022eU, 0x00000285U
   };
   static const unsigned int immediate_code[] = {
      0x0030023eU, 0x00300342U, 0x0302022eU, 0x00000285U
   };
   TCIRMethodParameter parameters[2];
   TCIRMethodView view;
   TCIRDiagnostic diagnostic;
   TCIRModule *module = tcirModuleCreate(NULL, &diagnostic);
   TCIRFunction *function = NULL;
   TCIROperationView operation;
   TCIRRuntimeValue arguments[2];
   TCIRInterpreterFrame frame;
   TCIRInterpreterResult result;
   TCIRExceptionCapture capture;
   int32_t homes[4];

   REQUIRE(module != NULL);
   memset(parameters, 0, sizeof(parameters));
   parameters[0].type = TCIR_TYPE_I32;
   parameters[0].home_bank = TCIR_HOME_I32;
   parameters[1].type = TCIR_TYPE_I32;
   parameters[1].home_bank = TCIR_HOME_I32;
   parameters[1].home_index = 1U;
   memset(&view, 0, sizeof(view));
   view.identity = "Example.checkedArithmetic:(II)I";
   view.code = code;
   view.code_slot_count = sizeof(code) / sizeof(code[0]);
   view.i32_home_count = 4U;
   view.parameters = parameters;
   view.parameter_count = 2U;
   view.return_type = TCIR_TYPE_I32;
   REQUIRE(tcirFrontendBuildFunction(module, &view, &function, &diagnostic) == TCIR_FRONTEND_OK);
   REQUIRE(function != NULL && tcirVerifyFunction(function, &diagnostic));
   REQUIRE(tcirBlockOperationAt(tcirFunctionBlockAt(function, 0U), 0U, &operation) == TCIR_STATUS_OK);
   REQUIRE(operation.opcode == TCIR_OP_DIV_I32);
   REQUIRE(operation.effects == (TCIR_EFFECT_MAY_THROW | TCIR_EFFECT_MAY_GC));
   REQUIRE(operation.propagates_exception);

   memset(&frame, 0, sizeof(frame));
   memset(arguments, 0, sizeof(arguments));
   memset(homes, 0, sizeof(homes));
   memset(&capture, 0, sizeof(capture));
   frame.i32_homes = homes;
   frame.i32_home_count = sizeof(homes) / sizeof(homes[0]);
   frame.arguments = arguments;
   frame.argument_count = 2U;
   frame.runtime_context = &capture;
   frame.raise_exception = captureRuntimeException;

   arguments[0].i32 = 7;
   arguments[1].i32 = 3;
   REQUIRE(tcirInterpretFunction(function, &frame, NULL, &result, &diagnostic) == TCIR_INTERPRETER_RETURNED);
   REQUIRE(result.value.i32 == 3 && capture.count == 0U);

   arguments[0].i32 = INT_MIN;
   arguments[1].i32 = -1;
   REQUIRE(tcirInterpretFunction(function, &frame, NULL, &result, &diagnostic) == TCIR_INTERPRETER_RETURNED);
   REQUIRE(result.value.i32 == INT_MIN && capture.count == 0U);

   arguments[0].i32 = 7;
   arguments[1].i32 = 0;
   REQUIRE(tcirInterpretFunction(function, &frame, NULL, &result, &diagnostic) == TCIR_INTERPRETER_THROWN);
   REQUIRE(result.tc_pc == 0U && capture.count == 1U);
   REQUIRE(capture.kind == TCIR_RUNTIME_EXCEPTION_ARITHMETIC && capture.tc_pc == 0U);

   view.identity = "Example.checkedImmediate:(I)I";
   view.code = immediate_code;
   view.parameter_count = 1U;
   function = NULL;
   REQUIRE(tcirFrontendBuildFunction(module, &view, &function, &diagnostic) == TCIR_FRONTEND_OK);
   REQUIRE(function != NULL && tcirVerifyFunction(function, &diagnostic));
   frame.argument_count = 1U;
   arguments[0].i32 = 7;
   capture.count = 0U;
   REQUIRE(tcirInterpretFunction(function, &frame, NULL, &result, &diagnostic) == TCIR_INTERPRETER_RETURNED);
   REQUIRE(result.value.i32 == 3 && capture.count == 0U);
   tcirModuleDestroy(module);
   return 1;
}

int main(void)
{
   int passed = 1;
   passed = testConstructedFunctions() && passed;
   passed = testReferenceInterpreter() && passed;
   passed = testInterpreterRejectsBeforeExecution() && passed;
   passed = testConverterFrontendFixtures() && passed;
   passed = testFrontendDiagnostics() && passed;
   passed = testUndefinedValue() && passed;
   passed = testBlockArgumentType() && passed;
   passed = testInvalidTerminator() && passed;
   passed = testWrongReturn() && passed;
   passed = testMissingGCHome() && passed;
   passed = testSourceContinuation() && passed;
   passed = testHelperEffects() && passed;
   passed = testUncheckedArrayProof() && passed;
   passed = testInternalAddressLifetime() && passed;
   passed = testNonNullProof() && passed;
   passed = testCheckedI32Arithmetic() && passed;
   passed = testOpcodeRegistry() && passed;
   if (!passed)
      return 1;
   printf("TCIR tests passed: reference execution, 5 converter fixtures, 20 stable diagnostics, 160 opcode dispositions.\n");
   return 0;
}
