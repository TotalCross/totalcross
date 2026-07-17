// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir.h"
#include "tcir_opcode_map.h"

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

static int testGoldenFunctions(void)
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

   REQUIRE(checkGolden(module, add, "add", &diagnostic));
   REQUIRE(checkGolden(module, absolute, "abs", &diagnostic));
   REQUIRE(checkGolden(module, sum_to, "sumTo", &diagnostic));

   tcirModuleDestroy(module);
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

int main(void)
{
   int passed = 1;
   passed = testGoldenFunctions() && passed;
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
   passed = testOpcodeRegistry() && passed;
   if (!passed)
      return 1;
   printf("TCIR tests passed: 3 golden fixtures, 10 stable verifier diagnostics, 160 opcode dispositions.\n");
   return 0;
}
