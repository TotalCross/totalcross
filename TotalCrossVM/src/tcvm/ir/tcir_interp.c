// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_interp.h"

#include "tcir_internal.h"

#include <assert.h>
#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

typedef struct TCIRInterpreterState
{
   TCIRRuntimeValue *values;
   unsigned char *defined;
   TCIRRuntimeValue *edge_values;
   size_t value_count;
   size_t edge_value_count;
   size_t steps;
   size_t max_steps;
} TCIRInterpreterState;

static TCIRInterpreterStatus tcirReject(
   const TCIRFunction *function,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic,
   TCIRDiagnosticCode code,
   unsigned int tc_pc,
   const char *message)
{
   if (result != NULL)
   {
      memset(result, 0, sizeof(*result));
      result->status = TCIR_INTERPRETER_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = tc_pc;
   }
   tcirSetDiagnostic(
      diagnostic,
      code,
      function == NULL ? "<function>" : tcirFunctionIdentity(function),
      tc_pc,
      "%s",
      message);
   return TCIR_INTERPRETER_REJECTED;
}

static int32_t tcirI32FromBits(uint32_t bits)
{
   if (bits <= (uint32_t)INT32_MAX)
      return (int32_t)bits;
   return (int32_t)(-1 - (int32_t)(UINT32_MAX - bits));
}

static int32_t tcirAddI32(int32_t left, int32_t right)
{
   return tcirI32FromBits((uint32_t)left + (uint32_t)right);
}

static int32_t tcirSubI32(int32_t left, int32_t right)
{
   return tcirI32FromBits((uint32_t)left - (uint32_t)right);
}

static int32_t tcirMulI32(int32_t left, int32_t right)
{
   return tcirI32FromBits((uint32_t)left * (uint32_t)right);
}

static int32_t tcirShlI32(int32_t value, int32_t distance)
{
   return tcirI32FromBits((uint32_t)value << ((uint32_t)distance & UINT32_C(31)));
}

static int32_t tcirShrI32(int32_t value, int32_t distance)
{
   unsigned int shift = (unsigned int)((uint32_t)distance & UINT32_C(31));
   uint32_t bits = (uint32_t)value;
   if (shift == 0U || value >= 0)
      return tcirI32FromBits(bits >> shift);
   return tcirI32FromBits((bits >> shift) | (UINT32_MAX << (32U - shift)));
}

static int32_t tcirUshrI32(int32_t value, int32_t distance)
{
   return tcirI32FromBits((uint32_t)value >> ((uint32_t)distance & UINT32_C(31)));
}

static int64_t tcirI64FromBits(uint64_t bits)
{
   if (bits <= (uint64_t)INT64_MAX)
      return (int64_t)bits;
   return (int64_t)(-1 - (int64_t)(UINT64_MAX - bits));
}

static int64_t tcirShrI64(int64_t value, int64_t distance)
{
   unsigned int shift = (unsigned int)((uint64_t)distance & UINT64_C(63));
   uint64_t bits = (uint64_t)value;
   if (shift == 0U || value >= 0)
      return tcirI64FromBits(bits >> shift);
   return tcirI64FromBits((bits >> shift) | (UINT64_MAX << (64U - shift)));
}

static double tcirF64FromBits(uint64_t bits)
{
   double value;
   memcpy(&value, &bits, sizeof(value));
   return value;
}

static int tcirInterpreterSupportsOperation(TCIROperation opcode)
{
   switch (opcode)
   {
      case TCIR_OP_CONST_I32:
      case TCIR_OP_COPY:
      case TCIR_OP_ADD_I32:
      case TCIR_OP_SUB_I32:
      case TCIR_OP_MUL_I32:
      case TCIR_OP_DIV_I32:
      case TCIR_OP_MOD_I32:
      case TCIR_OP_SHL_I32:
      case TCIR_OP_SHR_I32:
      case TCIR_OP_USHR_I32:
      case TCIR_OP_AND_I32:
      case TCIR_OP_OR_I32:
      case TCIR_OP_XOR_I32:
      case TCIR_OP_TRUNC_I32_I8:
      case TCIR_OP_TRUNC_I32_I16:
      case TCIR_OP_SEXT_I8_I32:
      case TCIR_OP_SEXT_I16_I32:
      case TCIR_OP_ZEXT_I16_I32:
      case TCIR_OP_CMP_EQ_I32:
      case TCIR_OP_CMP_LT_I32:
      case TCIR_OP_CMP_LE_I32:
      case TCIR_OP_CMP_GT_I32:
      case TCIR_OP_CMP_GE_I32:
      case TCIR_OP_CONST_I64:
      case TCIR_OP_ADD_I64:
      case TCIR_OP_SUB_I64:
      case TCIR_OP_MUL_I64:
      case TCIR_OP_DIV_I64:
      case TCIR_OP_MOD_I64:
      case TCIR_OP_SHL_I64:
      case TCIR_OP_SHR_I64:
      case TCIR_OP_USHR_I64:
      case TCIR_OP_AND_I64:
      case TCIR_OP_OR_I64:
      case TCIR_OP_XOR_I64:
      case TCIR_OP_TRUNC_I64_I32:
      case TCIR_OP_SEXT_I32_I64:
      case TCIR_OP_CMP_EQ_I64:
      case TCIR_OP_CMP_LT_I64:
      case TCIR_OP_CMP_LE_I64:
      case TCIR_OP_CMP_GT_I64:
      case TCIR_OP_CMP_GE_I64:
      case TCIR_OP_CONST_F64:
      case TCIR_OP_ADD_F64:
      case TCIR_OP_SUB_F64:
      case TCIR_OP_MUL_F64:
      case TCIR_OP_DIV_F64:
      case TCIR_OP_CMP_EQ_F64:
      case TCIR_OP_CMP_LT_F64:
      case TCIR_OP_CMP_LE_F64:
      case TCIR_OP_CMP_GT_F64:
      case TCIR_OP_CMP_GE_F64:
      case TCIR_OP_I32_TO_F64:
      case TCIR_OP_I64_TO_F64:
      case TCIR_OP_LOAD_SLOT:
      case TCIR_OP_STORE_SLOT:
         return 1;
      default:
         return 0;
   }
}

static int tcirInterpreterSupportsTerminator(TCIRTerminatorKind kind)
{
   switch (kind)
   {
      case TCIR_TERMINATOR_BRANCH:
      case TCIR_TERMINATOR_BRANCH_IF:
      case TCIR_TERMINATOR_SWITCH:
      case TCIR_TERMINATOR_RETURN:
      case TCIR_TERMINATOR_THROW:
         return 1;
      default:
         return 0;
   }
}

static int tcirUpdateMaximumValue(const TCIRValue *value, size_t *maximum)
{
   size_t candidate;

   if (value == NULL)
      return 1;
   candidate = (size_t)tcirValueId(value) + 1U;
   if (candidate < (size_t)tcirValueId(value))
      return 0;
   if (candidate > *maximum)
      *maximum = candidate;
   return 1;
}

static TCIRInterpreterStatus tcirPreflight(
   const TCIRFunction *function,
   const TCIRInterpreterFrame *frame,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic,
   size_t *value_count,
   size_t *edge_value_count)
{
   size_t block_index;
   size_t parameter_index;

   if (function == NULL || frame == NULL || result == NULL || value_count == NULL || edge_value_count == NULL)
      return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                        "invalid TCIR interpreter arguments");

   if (!tcirVerifyFunction(function, diagnostic))
   {
      memset(result, 0, sizeof(*result));
      result->status = TCIR_INTERPRETER_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = diagnostic == NULL ? TCIR_TCPC_NONE : diagnostic->tc_pc;
      return TCIR_INTERPRETER_REJECTED;
   }

#ifndef NDEBUG
   assert(tcirVerifyFunction(function, NULL));
#endif

   if (frame->argument_count != tcirFunctionParameterCount(function)
       || (frame->argument_count != 0U && frame->arguments == NULL))
      return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                        "interpreter argument count does not match the function signature");

   if (frame->i32_home_count < tcirFunctionHomeCount(function, TCIR_HOME_I32)
       || (frame->i32_home_count != 0U && frame->i32_homes == NULL)
       || frame->ref_home_count < tcirFunctionHomeCount(function, TCIR_HOME_REF)
       || (frame->ref_home_count != 0U && frame->ref_homes == NULL)
       || frame->v64_home_count < tcirFunctionHomeCount(function, TCIR_HOME_V64)
       || (frame->v64_home_count != 0U && frame->v64_homes == NULL))
      return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                        "interpreter frame does not provide the required typed homes");

   *value_count = 0U;
   *edge_value_count = 0U;
   for (parameter_index = 0U; parameter_index < tcirFunctionParameterCount(function); ++parameter_index)
      if (!tcirUpdateMaximumValue(tcirFunctionParameter(function, parameter_index), value_count))
         return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, TCIR_TCPC_NONE,
                           "TCIR value table is too large");

   for (block_index = 0U; block_index < tcirFunctionBlockCount(function); ++block_index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(function, block_index);
      TCIRTerminatorView terminator;
      size_t argument_index;
      size_t operation_index;

      if (tcirBlockArgumentCount(block) > *edge_value_count)
         *edge_value_count = tcirBlockArgumentCount(block);
      for (argument_index = 0U; argument_index < tcirBlockArgumentCount(block); ++argument_index)
         if (!tcirUpdateMaximumValue(tcirBlockArgumentAt(block, argument_index), value_count))
            return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
                              tcirBlockSource(block).tc_pc, "TCIR value table is too large");

      for (operation_index = 0U; operation_index < tcirBlockOperationCount(block); ++operation_index)
      {
         TCIROperationView operation;
         if (tcirBlockOperationAt(block, operation_index, &operation) != TCIR_STATUS_OK)
            return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
                              tcirBlockSource(block).tc_pc, "unable to inspect a verified TCIR operation");
         if (!tcirInterpreterSupportsOperation(operation.opcode))
            return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
                              operation.source.tc_pc, "function contains an operation unsupported by the reference interpreter");
         if (!tcirUpdateMaximumValue(operation.result, value_count))
            return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
                              operation.source.tc_pc, "TCIR value table is too large");
      }

      if (tcirBlockTerminator(block, &terminator) != TCIR_STATUS_OK)
         return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_TERMINATOR,
                           tcirBlockSource(block).tc_pc, "unable to inspect a verified TCIR terminator");
      if (!tcirInterpreterSupportsTerminator(terminator.kind))
         return tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
                           terminator.source.tc_pc, "function contains a terminator unsupported by the reference interpreter");
   }

   return TCIR_INTERPRETER_RETURNED;
}

static int tcirConsumeStep(
   const TCIRFunction *function,
   TCIRInterpreterState *state,
   TCIRInterpreterFrame *frame,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic,
   unsigned int tc_pc)
{
   frame->tc_pc = tc_pc;
   if (state->steps >= state->max_steps)
   {
      memset(result, 0, sizeof(*result));
      result->status = TCIR_INTERPRETER_STEP_LIMIT;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = tc_pc;
      result->steps = state->steps;
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_EXECUTION_LIMIT, tcirFunctionIdentity(function), tc_pc,
                        "TCIR interpreter exceeded its %lu-step limit", (unsigned long)state->max_steps);
      return 0;
   }
   ++state->steps;
   return 1;
}

static int tcirValueIsDefined(const TCIRInterpreterState *state, const TCIRValue *value)
{
   size_t id = (size_t)tcirValueId(value);
   return id < state->value_count && state->defined[id] != 0U;
}

static TCIRRuntimeValue tcirReadValue(const TCIRInterpreterState *state, const TCIRValue *value)
{
   return state->values[tcirValueId(value)];
}

static void tcirWriteValue(TCIRInterpreterState *state, const TCIRValue *value, TCIRRuntimeValue runtime_value)
{
   unsigned int id = tcirValueId(value);
   state->values[id] = runtime_value;
   state->defined[id] = 1U;
}

static int tcirLoadHome(
   const TCIRInterpreterFrame *frame,
   TCIRHomeBank bank,
   unsigned int index,
   TCIRType type,
   TCIRRuntimeValue *value)
{
   memset(value, 0, sizeof(*value));
   switch (bank)
   {
      case TCIR_HOME_I32:
         if (index >= frame->i32_home_count)
            return 0;
         value->i32 = frame->i32_homes[index];
         return 1;
      case TCIR_HOME_REF:
         if (index >= frame->ref_home_count)
            return 0;
         value->ref = frame->ref_homes[index];
         return 1;
      case TCIR_HOME_V64:
         if (index >= frame->v64_home_count)
            return 0;
         if (type == TCIR_TYPE_F64)
            value->f64 = frame->v64_homes[index].f64;
         else
            value->i64 = frame->v64_homes[index].i64;
         return 1;
      default:
         return 0;
   }
}

static int tcirStoreHome(
   TCIRInterpreterFrame *frame,
   TCIRHomeBank bank,
   unsigned int index,
   TCIRType type,
   TCIRRuntimeValue value)
{
   switch (bank)
   {
      case TCIR_HOME_I32:
         if (index >= frame->i32_home_count)
            return 0;
         frame->i32_homes[index] = value.i32;
         return 1;
      case TCIR_HOME_REF:
         if (index >= frame->ref_home_count)
            return 0;
         frame->ref_homes[index] = value.ref;
         return 1;
      case TCIR_HOME_V64:
         if (index >= frame->v64_home_count)
            return 0;
         if (type == TCIR_TYPE_F64)
            frame->v64_homes[index].f64 = value.f64;
         else
            frame->v64_homes[index].i64 = value.i64;
         return 1;
      default:
         return 0;
   }
}

static int tcirExecuteOperation(
   TCIRInterpreterState *state,
   TCIRInterpreterFrame *frame,
   const TCIROperationView *operation,
   int *thrown)
{
   TCIRRuntimeValue left;
   TCIRRuntimeValue right;
   TCIRRuntimeValue value;

   *thrown = 0;
   memset(&value, 0, sizeof(value));
   if (operation->operand_count > 0U && !tcirValueIsDefined(state, operation->operands[0]))
      return 0;
   if (operation->operand_count > 1U && !tcirValueIsDefined(state, operation->operands[1]))
      return 0;
   if (operation->operand_count > 0U)
      left = tcirReadValue(state, operation->operands[0]);
   else
      memset(&left, 0, sizeof(left));
   if (operation->operand_count > 1U)
      right = tcirReadValue(state, operation->operands[1]);
   else
      memset(&right, 0, sizeof(right));

   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
         value.i32 = (int32_t)operation->immediate_i32;
         break;
      case TCIR_OP_COPY:
         value = left;
         break;
      case TCIR_OP_ADD_I32:
         value.i32 = tcirAddI32(left.i32, right.i32);
         break;
      case TCIR_OP_SUB_I32:
         value.i32 = tcirSubI32(left.i32, right.i32);
         break;
      case TCIR_OP_MUL_I32:
         value.i32 = tcirMulI32(left.i32, right.i32);
         break;
      case TCIR_OP_DIV_I32:
         if (right.i32 == 0)
         {
            if (frame->raise_exception != NULL)
               frame->raise_exception(
                  frame->runtime_context, TCIR_RUNTIME_EXCEPTION_ARITHMETIC, operation->source.tc_pc);
            *thrown = 1;
            return 1;
         }
         value.i32 = left.i32 == INT32_MIN && right.i32 == -1
            ? INT32_MIN : left.i32 / right.i32;
         break;
      case TCIR_OP_MOD_I32:
         if (right.i32 == 0)
         {
            if (frame->raise_exception != NULL)
               frame->raise_exception(
                  frame->runtime_context, TCIR_RUNTIME_EXCEPTION_ARITHMETIC, operation->source.tc_pc);
            *thrown = 1;
            return 1;
         }
         value.i32 = left.i32 == INT32_MIN && right.i32 == -1
            ? 0 : left.i32 % right.i32;
         break;
      case TCIR_OP_SHL_I32:
         value.i32 = tcirShlI32(left.i32, right.i32);
         break;
      case TCIR_OP_SHR_I32:
         value.i32 = tcirShrI32(left.i32, right.i32);
         break;
      case TCIR_OP_USHR_I32:
         value.i32 = tcirUshrI32(left.i32, right.i32);
         break;
      case TCIR_OP_AND_I32:
         value.i32 = tcirI32FromBits((uint32_t)left.i32 & (uint32_t)right.i32);
         break;
      case TCIR_OP_OR_I32:
         value.i32 = tcirI32FromBits((uint32_t)left.i32 | (uint32_t)right.i32);
         break;
      case TCIR_OP_XOR_I32:
         value.i32 = tcirI32FromBits((uint32_t)left.i32 ^ (uint32_t)right.i32);
         break;
      case TCIR_OP_TRUNC_I32_I8:
         value.i32 = (int32_t)((uint32_t)left.i32 & UINT32_C(0xff));
         break;
      case TCIR_OP_TRUNC_I32_I16:
         value.i32 = (int32_t)((uint32_t)left.i32 & UINT32_C(0xffff));
         break;
      case TCIR_OP_SEXT_I8_I32:
      {
         uint32_t bits = (uint32_t)left.i32 & UINT32_C(0xff);
         value.i32 = tcirI32FromBits((bits & UINT32_C(0x80)) != 0U
            ? bits | UINT32_C(0xffffff00) : bits);
         break;
      }
      case TCIR_OP_SEXT_I16_I32:
      {
         uint32_t bits = (uint32_t)left.i32 & UINT32_C(0xffff);
         value.i32 = tcirI32FromBits((bits & UINT32_C(0x8000)) != 0U
            ? bits | UINT32_C(0xffff0000) : bits);
         break;
      }
      case TCIR_OP_ZEXT_I16_I32:
         value.i32 = (int32_t)((uint32_t)left.i32 & UINT32_C(0xffff));
         break;
      case TCIR_OP_CMP_EQ_I32:
         value.i1 = left.i32 == right.i32;
         break;
      case TCIR_OP_CMP_LT_I32:
         value.i1 = left.i32 < right.i32;
         break;
      case TCIR_OP_CMP_LE_I32:
         value.i1 = left.i32 <= right.i32;
         break;
      case TCIR_OP_CMP_GT_I32:
         value.i1 = left.i32 > right.i32;
         break;
      case TCIR_OP_CMP_GE_I32:
         value.i1 = left.i32 >= right.i32;
         break;
      case TCIR_OP_CONST_I64:
         value.i64 = operation->immediate_i64;
         break;
      case TCIR_OP_ADD_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 + (uint64_t)right.i64);
         break;
      case TCIR_OP_SUB_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 - (uint64_t)right.i64);
         break;
      case TCIR_OP_MUL_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 * (uint64_t)right.i64);
         break;
      case TCIR_OP_DIV_I64:
         if (right.i64 == 0)
         {
            if (frame->raise_exception != NULL)
               frame->raise_exception(
                  frame->runtime_context, TCIR_RUNTIME_EXCEPTION_ARITHMETIC, operation->source.tc_pc);
            *thrown = 1;
            return 1;
         }
         value.i64 = left.i64 == INT64_MIN && right.i64 == -1
            ? INT64_MIN : left.i64 / right.i64;
         break;
      case TCIR_OP_MOD_I64:
         if (right.i64 == 0)
         {
            if (frame->raise_exception != NULL)
               frame->raise_exception(
                  frame->runtime_context, TCIR_RUNTIME_EXCEPTION_ARITHMETIC, operation->source.tc_pc);
            *thrown = 1;
            return 1;
         }
         value.i64 = left.i64 == INT64_MIN && right.i64 == -1
            ? 0 : left.i64 % right.i64;
         break;
      case TCIR_OP_SHL_I64:
         value.i64 = tcirI64FromBits(
            (uint64_t)left.i64 << ((uint64_t)right.i64 & UINT64_C(63)));
         break;
      case TCIR_OP_SHR_I64:
         value.i64 = tcirShrI64(left.i64, right.i64);
         break;
      case TCIR_OP_USHR_I64:
         value.i64 = tcirI64FromBits(
            (uint64_t)left.i64 >> ((uint64_t)right.i64 & UINT64_C(63)));
         break;
      case TCIR_OP_AND_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 & (uint64_t)right.i64);
         break;
      case TCIR_OP_OR_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 | (uint64_t)right.i64);
         break;
      case TCIR_OP_XOR_I64:
         value.i64 = tcirI64FromBits((uint64_t)left.i64 ^ (uint64_t)right.i64);
         break;
      case TCIR_OP_TRUNC_I64_I32:
         value.i32 = tcirI32FromBits((uint32_t)(uint64_t)left.i64);
         break;
      case TCIR_OP_SEXT_I32_I64:
         value.i64 = (int64_t)left.i32;
         break;
      case TCIR_OP_CMP_EQ_I64:
         value.i1 = left.i64 == right.i64;
         break;
      case TCIR_OP_CMP_LT_I64:
         value.i1 = left.i64 < right.i64;
         break;
      case TCIR_OP_CMP_LE_I64:
         value.i1 = left.i64 <= right.i64;
         break;
      case TCIR_OP_CMP_GT_I64:
         value.i1 = left.i64 > right.i64;
         break;
      case TCIR_OP_CMP_GE_I64:
         value.i1 = left.i64 >= right.i64;
         break;
      case TCIR_OP_CONST_F64:
         value.f64 = tcirF64FromBits(operation->immediate_f64_bits);
         break;
      case TCIR_OP_ADD_F64:
         value.f64 = left.f64 + right.f64;
         break;
      case TCIR_OP_SUB_F64:
         value.f64 = left.f64 - right.f64;
         break;
      case TCIR_OP_MUL_F64:
         value.f64 = left.f64 * right.f64;
         break;
      case TCIR_OP_DIV_F64:
         if (right.f64 == 0.0)
         {
            if (frame->raise_exception != NULL)
               frame->raise_exception(
                  frame->runtime_context, TCIR_RUNTIME_EXCEPTION_ARITHMETIC, operation->source.tc_pc);
            *thrown = 1;
            return 1;
         }
         value.f64 = left.f64 / right.f64;
         break;
      case TCIR_OP_CMP_EQ_F64:
         value.i1 = left.f64 == right.f64;
         break;
      case TCIR_OP_CMP_LT_F64:
         value.i1 = left.f64 < right.f64;
         break;
      case TCIR_OP_CMP_LE_F64:
         value.i1 = left.f64 <= right.f64;
         break;
      case TCIR_OP_CMP_GT_F64:
         value.i1 = left.f64 > right.f64;
         break;
      case TCIR_OP_CMP_GE_F64:
         value.i1 = left.f64 >= right.f64;
         break;
      case TCIR_OP_I32_TO_F64:
         value.f64 = (double)left.i32;
         break;
      case TCIR_OP_I64_TO_F64:
         value.f64 = (double)left.i64;
         break;
      case TCIR_OP_LOAD_SLOT:
         if (!tcirLoadHome(frame, operation->home_bank, operation->home_index, operation->result_type, &value))
            return 0;
         break;
      case TCIR_OP_STORE_SLOT:
         return tcirStoreHome(frame, operation->home_bank, operation->home_index,
                              tcirValueType(operation->operands[0]), left);
      default:
         return 0;
   }

   if (operation->result != NULL)
      tcirWriteValue(state, operation->result, value);
   return 1;
}

static int tcirEnterEdge(TCIRInterpreterState *state, const TCIREdge *edge, const TCIRBlock **next_block)
{
   size_t index;

   if (edge == NULL || edge->target == NULL || edge->argument_count > state->edge_value_count)
      return 0;
   for (index = 0U; index < edge->argument_count; ++index)
   {
      if (!tcirValueIsDefined(state, edge->arguments[index]))
         return 0;
      state->edge_values[index] = tcirReadValue(state, edge->arguments[index]);
   }
   if (tcirBlockArgumentCount(edge->target) != edge->argument_count)
      return 0;
   for (index = 0U; index < edge->argument_count; ++index)
      tcirWriteValue(state, tcirBlockArgumentAt(edge->target, index), state->edge_values[index]);
   *next_block = edge->target;
   return 1;
}

static const TCIREdge *tcirSelectSwitchEdge(const TCIRTerminatorView *terminator, int32_t selector)
{
   const TCIREdge *default_edge = NULL;
   size_t index;

   for (index = 0U; index < terminator->edge_count; ++index)
   {
      if (!terminator->edges[index].has_case_value)
         default_edge = &terminator->edges[index];
      else if ((int32_t)terminator->edges[index].case_value == selector)
         return &terminator->edges[index];
   }
   return default_edge;
}

TCIRInterpreterStatus tcirInterpretFunction(
   const TCIRFunction *function,
   TCIRInterpreterFrame *frame,
   const TCIRInterpreterOptions *options,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic)
{
   TCIRInterpreterState state;
   TCIRInterpreterStatus preflight_status;
   const TCIRBlock *block;
   size_t parameter_index;

   tcirDiagnosticClear(diagnostic);
   memset(&state, 0, sizeof(state));
   preflight_status = tcirPreflight(function, frame, result, diagnostic, &state.value_count, &state.edge_value_count);
   if (preflight_status != TCIR_INTERPRETER_RETURNED)
      return preflight_status;

   state.max_steps = options == NULL || options->max_steps == 0U
      ? (size_t)TCIR_INTERPRETER_DEFAULT_STEP_LIMIT
      : options->max_steps;
   state.values = (TCIRRuntimeValue *)calloc(state.value_count == 0U ? 1U : state.value_count,
                                             sizeof(*state.values));
   state.defined = (unsigned char *)calloc(state.value_count == 0U ? 1U : state.value_count,
                                           sizeof(*state.defined));
   state.edge_values = (TCIRRuntimeValue *)calloc(state.edge_value_count == 0U ? 1U : state.edge_value_count,
                                                  sizeof(*state.edge_values));
   if (state.values == NULL || state.defined == NULL || state.edge_values == NULL)
   {
      free(state.edge_values);
      free(state.defined);
      free(state.values);
      memset(result, 0, sizeof(*result));
      result->status = TCIR_INTERPRETER_OUT_OF_MEMORY;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = TCIR_TCPC_NONE;
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, tcirFunctionIdentity(function),
                        TCIR_TCPC_NONE, "unable to allocate TCIR interpreter state");
      return TCIR_INTERPRETER_OUT_OF_MEMORY;
   }

   for (parameter_index = 0U; parameter_index < frame->argument_count; ++parameter_index)
      tcirWriteValue(&state, tcirFunctionParameter(function, parameter_index), frame->arguments[parameter_index]);

   block = tcirFunctionBlockAt(function, 0U);
   for (;;)
   {
      TCIRTerminatorView terminator;
      const TCIREdge *edge = NULL;
      size_t operation_index;

      for (operation_index = 0U; operation_index < tcirBlockOperationCount(block); ++operation_index)
      {
         TCIROperationView operation;
         int operation_threw;
         (void)tcirBlockOperationAt(block, operation_index, &operation);
         if (!tcirConsumeStep(function, &state, frame, result, diagnostic, operation.source.tc_pc))
            goto done;
         if (!tcirExecuteOperation(&state, frame, &operation, &operation_threw))
         {
            tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNDEFINED_VALUE, operation.source.tc_pc,
                       "verified TCIR reached an invalid operation state");
            goto done;
         }
         if (operation_threw)
         {
            memset(result, 0, sizeof(*result));
            result->status = TCIR_INTERPRETER_THROWN;
            result->type = TCIR_TYPE_VOID;
            result->tc_pc = operation.source.tc_pc;
            result->steps = state.steps;
            goto done;
         }
      }

      (void)tcirBlockTerminator(block, &terminator);
      if (!tcirConsumeStep(function, &state, frame, result, diagnostic, terminator.source.tc_pc))
         goto done;
      switch (terminator.kind)
      {
         case TCIR_TERMINATOR_BRANCH:
            edge = &terminator.edges[0];
            break;
         case TCIR_TERMINATOR_BRANCH_IF:
            if (!tcirValueIsDefined(&state, terminator.value))
            {
               tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
                          terminator.source.tc_pc, "verified TCIR reached an undefined branch condition");
               goto done;
            }
            edge = &terminator.edges[tcirReadValue(&state, terminator.value).i1 ? 0U : 1U];
            break;
         case TCIR_TERMINATOR_SWITCH:
            if (!tcirValueIsDefined(&state, terminator.value))
            {
               tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
                          terminator.source.tc_pc, "verified TCIR reached an undefined switch selector");
               goto done;
            }
            edge = tcirSelectSwitchEdge(&terminator, tcirReadValue(&state, terminator.value).i32);
            break;
         case TCIR_TERMINATOR_RETURN:
            memset(result, 0, sizeof(*result));
            result->status = TCIR_INTERPRETER_RETURNED;
            result->type = tcirFunctionReturnType(function);
            result->tc_pc = terminator.source.tc_pc;
            result->steps = state.steps;
            if (terminator.value != NULL)
            {
               if (!tcirValueIsDefined(&state, terminator.value))
               {
                  tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
                             terminator.source.tc_pc, "verified TCIR reached an undefined return value");
                  goto done;
               }
               result->value = tcirReadValue(&state, terminator.value);
            }
            goto done;
         case TCIR_TERMINATOR_THROW:
            if (!tcirValueIsDefined(&state, terminator.value))
            {
               tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
                          terminator.source.tc_pc, "verified TCIR reached an undefined thrown value");
               goto done;
            }
            memset(result, 0, sizeof(*result));
            result->status = TCIR_INTERPRETER_THROWN;
            result->type = tcirValueType(terminator.value);
            result->value = tcirReadValue(&state, terminator.value);
            result->tc_pc = terminator.source.tc_pc;
            result->steps = state.steps;
            goto done;
         default:
            tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
                       terminator.source.tc_pc, "verified TCIR reached an unsupported terminator");
            goto done;
      }

      if (edge == NULL || !tcirEnterEdge(&state, edge, &block))
      {
         tcirReject(function, result, diagnostic, TCIR_DIAGNOSTIC_INVALID_TARGET,
                    terminator.source.tc_pc, "verified TCIR reached an invalid control-flow edge");
         goto done;
      }
   }

done:
   free(state.edge_values);
   free(state.defined);
   free(state.values);
   return result->status;
}
