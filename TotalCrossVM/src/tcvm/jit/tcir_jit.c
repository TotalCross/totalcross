// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_jit.h"
#include "tcir_jit_memory.h"

#include <limits.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef sljit_s32 (SLJIT_FUNC *TCIRJitEntry)(TCCompiledFrame *frame);

struct TCIRJitArtifact
{
   void *code;
   TCIRJitEntry entry;
   size_t code_size;
   size_t value_count;
   size_t edge_value_count;
   unsigned int *parameter_value_ids;
   TCIRType *parameter_types;
   size_t parameter_count;
   unsigned int i32_home_count;
   unsigned int ref_home_count;
   unsigned int v64_home_count;
   TCIRType return_type;
   size_t max_call_argument_count;
   int has_method_call;
};

typedef struct TCIRJitPendingJump
{
   struct sljit_jump *jump;
   size_t target_index;
} TCIRJitPendingJump;

typedef struct TCIRJitEmitter
{
   const TCIRFunction *function;
   struct sljit_compiler *compiler;
   struct sljit_label **labels;
   TCIRJitPendingJump *jumps;
   size_t jump_count;
   size_t jump_capacity;
   size_t emitted_count;
   size_t emission_limit;
   unsigned int tc_pc;
   TCIRJitDiagnostic *diagnostic;
} TCIRJitEmitter;

typedef struct TCIRJitEligibility
{
   size_t value_count;
   size_t edge_value_count;
   size_t max_call_argument_count;
   int has_method_call;
} TCIRJitEligibility;

static sljit_s32 SLJIT_FUNC tcirJitInvokeMethodCall(TCCompiledFrame *frame)
{
   TCCompiledStatus status;
   TCCompiledResult *result;

   if (frame == NULL || frame->call_result == NULL)
      return (sljit_s32)TC_COMPILED_REJECTED;
   result = frame->call_result;
   memset(result, 0, sizeof(*result));
   result->status = TC_COMPILED_REJECTED;
   result->type = TCIR_TYPE_VOID;
   result->tc_pc = TCIR_TCPC_NONE;
   if (frame->runtime == NULL || frame->runtime->abi_version != TC_RUNTIME_ABI_VERSION ||
       frame->runtime->invoke == NULL ||
       frame->call.argument_count > frame->call_argument_count ||
       (frame->call.argument_count != 0U && frame->call.arguments == NULL))
      return (sljit_s32)TC_COMPILED_REJECTED;
   status = frame->runtime->invoke(frame->runtime, &frame->call, result);
   if (status != result->status ||
       (status == TC_COMPILED_RETURNED && result->type != frame->call.result_type))
   {
      result->status = TC_COMPILED_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = frame->call.tc_pc;
      return (sljit_s32)TC_COMPILED_REJECTED;
   }
   if (status != TC_COMPILED_RETURNED && result->tc_pc == TCIR_TCPC_NONE)
      result->tc_pc = frame->call.tc_pc;
   return (sljit_s32)status;
}

static void tcirJitSetDiagnostic(
   TCIRJitDiagnostic *diagnostic,
   TCIRJitDiagnosticCode code,
   unsigned int tc_pc,
   const char *format,
   ...)
{
   va_list arguments;

   if (diagnostic == NULL)
      return;
   diagnostic->code = code;
   diagnostic->tc_pc = tc_pc;
   va_start(arguments, format);
   vsnprintf(diagnostic->message, sizeof(diagnostic->message), format, arguments);
   va_end(arguments);
}

void tcirJitDiagnosticClear(TCIRJitDiagnostic *diagnostic)
{
   if (diagnostic != NULL)
   {
      memset(diagnostic, 0, sizeof(*diagnostic));
      diagnostic->tc_pc = TCIR_TCPC_NONE;
      tcirDiagnosticClear(&diagnostic->verifier);
   }
}

const char *tcirJitDiagnosticCodeName(TCIRJitDiagnosticCode code)
{
   switch (code)
   {
      case TCIR_JIT_DIAGNOSTIC_NONE: return "none";
      case TCIR_JIT_DIAGNOSTIC_INVALID_ARGUMENT: return "invalid_argument";
      case TCIR_JIT_DIAGNOSTIC_VERIFICATION_FAILED: return "verification_failed";
      case TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE: return "ineligible_type";
      case TCIR_JIT_DIAGNOSTIC_INELIGIBLE_OPERATION: return "ineligible_operation";
      case TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TERMINATOR: return "ineligible_terminator";
      case TCIR_JIT_DIAGNOSTIC_OUT_OF_MEMORY: return "out_of_memory";
      case TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED: return "emission_failed";
      case TCIR_JIT_DIAGNOSTIC_NOT_READY: return "not_ready";
      case TCIR_JIT_DIAGNOSTIC_SHUTDOWN: return "shutdown";
      default: return "unknown";
   }
}

const char *tcirJitPlatformName(void)
{
   return tcirJitExecutableMemoryPlatformName();
}

static int tcirJitUpdateValueCount(const TCIRValue *value, size_t *value_count)
{
   size_t candidate;

   if (value == NULL)
      return 1;
   candidate = (size_t)tcirValueId(value) + 1U;
   if (candidate == 0U || candidate > (size_t)PTRDIFF_MAX / sizeof(TCIRRuntimeValue))
      return 0;
   if (candidate > *value_count)
      *value_count = candidate;
   return 1;
}

static int tcirJitTypeIsI32Like(TCIRType type)
{
   return type == TCIR_TYPE_I1 || type == TCIR_TYPE_I8 ||
      type == TCIR_TYPE_I16 || type == TCIR_TYPE_I32;
}

static int tcirJitSupportsI64(void)
{
#if defined(SLJIT_64BIT_ARCHITECTURE) && SLJIT_64BIT_ARCHITECTURE
   return 1;
#else
   return 0;
#endif
}

static int tcirJitSupportsF64(void)
{
   return tcirJitSupportsI64();
}

static int tcirJitTypeIsSupported(TCIRType type)
{
   return tcirJitTypeIsI32Like(type) || (type == TCIR_TYPE_I64 && tcirJitSupportsI64()) ||
      (type == TCIR_TYPE_F64 && tcirJitSupportsF64()) || type == TCIR_TYPE_REF;
}

static int tcirJitOperationIsEligible(const TCIROperationView *operation)
{
   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
      case TCIR_OP_ADD_I32:
      case TCIR_OP_SUB_I32:
      case TCIR_OP_MUL_I32:
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
         return operation->result != NULL && tcirJitTypeIsI32Like(operation->result_type)
            && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_COPY:
         return operation->result != NULL && tcirJitTypeIsSupported(operation->result_type) &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CONST_I64:
      case TCIR_OP_ADD_I64:
      case TCIR_OP_SUB_I64:
      case TCIR_OP_MUL_I64:
      case TCIR_OP_SHL_I64:
      case TCIR_OP_SHR_I64:
      case TCIR_OP_USHR_I64:
      case TCIR_OP_AND_I64:
      case TCIR_OP_OR_I64:
      case TCIR_OP_XOR_I64:
         return tcirJitSupportsI64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_I64 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_TRUNC_I64_I32:
         return tcirJitSupportsI64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_I32 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_SEXT_I32_I64:
         return tcirJitSupportsI64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_I64 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CMP_EQ_I64:
      case TCIR_OP_CMP_LT_I64:
      case TCIR_OP_CMP_LE_I64:
      case TCIR_OP_CMP_GT_I64:
      case TCIR_OP_CMP_GE_I64:
         return tcirJitSupportsI64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_I1 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CONST_F64:
      case TCIR_OP_ADD_F64:
      case TCIR_OP_SUB_F64:
      case TCIR_OP_MUL_F64:
         return tcirJitSupportsF64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_F64 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CMP_EQ_F64:
      case TCIR_OP_CMP_LT_F64:
      case TCIR_OP_CMP_LE_F64:
      case TCIR_OP_CMP_GT_F64:
      case TCIR_OP_CMP_GE_F64:
         return tcirJitSupportsF64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_I1 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_I32_TO_F64:
      case TCIR_OP_I64_TO_F64:
         return tcirJitSupportsF64() && operation->result != NULL &&
            operation->result_type == TCIR_TYPE_F64 && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CONST_REF_NULL:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_REF &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CMP_EQ_REF:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I1 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_LOAD_SLOT:
         return operation->result != NULL && operation->effects == TCIR_EFFECT_NONE &&
            ((operation->result_type == TCIR_TYPE_I32 && operation->home_bank == TCIR_HOME_I32) ||
             (operation->result_type == TCIR_TYPE_REF && operation->home_bank == TCIR_HOME_REF) ||
             ((operation->result_type == TCIR_TYPE_I64 || operation->result_type == TCIR_TYPE_F64) &&
              tcirJitTypeIsSupported(operation->result_type) &&
              operation->home_bank == TCIR_HOME_V64));
      case TCIR_OP_STORE_SLOT:
         return operation->result == NULL && operation->operand_count == 1U
            && operation->effects == TCIR_EFFECT_NONE &&
            (operation->home_bank == TCIR_HOME_I32 || operation->home_bank == TCIR_HOME_REF ||
             (tcirJitTypeIsSupported(tcirValueType(operation->operands[0])) &&
              operation->home_bank == TCIR_HOME_V64));
      case TCIR_OP_METHOD_CALL:
         return operation->immediate_i32 == (int)TCIR_CALL_STATIC &&
            operation->symbol != NULL &&
            tcirSymbolKind(operation->symbol) == TCIR_SYMBOL_METHOD &&
            operation->effects == TCIR_METHOD_CALL_EFFECTS &&
            operation->propagates_exception;
      default:
         return 0;
   }
}

static TCIRJitCompileStatus tcirJitInspectEligibility(
   const TCIRFunction *function,
   TCIRJitEligibility *eligibility,
   TCIRJitDiagnostic *diagnostic)
{
   size_t block_index;
   size_t parameter_index;

   if (function == NULL || eligibility == NULL)
   {
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                           "invalid SLJIT eligibility arguments");
      return TCIR_JIT_COMPILE_INELIGIBLE;
   }

   memset(eligibility, 0, sizeof(*eligibility));
   if (!tcirVerifyFunction(function, diagnostic == NULL ? NULL : &diagnostic->verifier))
   {
      unsigned int tc_pc = diagnostic == NULL ? TCIR_TCPC_NONE : diagnostic->verifier.tc_pc;
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_VERIFICATION_FAILED, tc_pc,
                           "TCIR verification failed before SLJIT eligibility");
      return TCIR_JIT_COMPILE_VERIFICATION_FAILED;
   }

   if (tcirFunctionReturnType(function) != TCIR_TYPE_I32 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_I64 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_F64 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_REF &&
       tcirFunctionReturnType(function) != TCIR_TYPE_VOID)
   {
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE, TCIR_TCPC_NONE,
                           "SLJIT baseline supports only i32, i64, f64, ref, and void returns");
      return TCIR_JIT_COMPILE_INELIGIBLE;
   }

   for (parameter_index = 0U; parameter_index < tcirFunctionParameterCount(function); ++parameter_index)
   {
      const TCIRValue *parameter = tcirFunctionParameter(function, parameter_index);
      if (!tcirJitTypeIsSupported(tcirValueType(parameter)) ||
          !tcirJitUpdateValueCount(parameter, &eligibility->value_count))
      {
         tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE, TCIR_TCPC_NONE,
                              "SLJIT baseline supports only bounded scalar parameters");
         return TCIR_JIT_COMPILE_INELIGIBLE;
      }
   }

   for (block_index = 0U; block_index < tcirFunctionBlockCount(function); ++block_index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(function, block_index);
      TCIRTerminatorView terminator;
      size_t argument_index;
      size_t operation_index;
      size_t edge_index;

      if (tcirBlockIsExceptionHandler(block))
      {
         tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TERMINATOR,
                              tcirBlockSource(block).tc_pc,
                              "SLJIT baseline does not compile exception-handler blocks");
         return TCIR_JIT_COMPILE_INELIGIBLE;
      }
      if (tcirBlockArgumentCount(block) > eligibility->edge_value_count)
         eligibility->edge_value_count = tcirBlockArgumentCount(block);
      for (argument_index = 0U; argument_index < tcirBlockArgumentCount(block); ++argument_index)
      {
         const TCIRValue *argument = tcirBlockArgumentAt(block, argument_index);
         if (!tcirJitTypeIsSupported(tcirValueType(argument)) ||
             !tcirJitUpdateValueCount(argument, &eligibility->value_count))
         {
            tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE,
                                 tcirBlockSource(block).tc_pc,
                                 "SLJIT baseline supports only bounded scalar block arguments");
            return TCIR_JIT_COMPILE_INELIGIBLE;
         }
      }

      for (operation_index = 0U; operation_index < tcirBlockOperationCount(block); ++operation_index)
      {
         TCIROperationView operation;
         size_t operand_index;
         if (tcirBlockOperationAt(block, operation_index, &operation) != TCIR_STATUS_OK
             || !tcirJitOperationIsEligible(&operation))
         {
            unsigned int tc_pc = operation_index < tcirBlockOperationCount(block)
               && tcirBlockOperationAt(block, operation_index, &operation) == TCIR_STATUS_OK
               ? operation.source.tc_pc : tcirBlockSource(block).tc_pc;
            tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_OPERATION, tc_pc,
                                 "function contains an operation unsupported by the SLJIT baseline");
            return TCIR_JIT_COMPILE_INELIGIBLE;
         }
         if (!tcirJitUpdateValueCount(operation.result, &eligibility->value_count))
         {
            tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE, operation.source.tc_pc,
                                 "SLJIT value table is too large");
            return TCIR_JIT_COMPILE_INELIGIBLE;
         }
         if (operation.opcode == TCIR_OP_METHOD_CALL)
         {
            eligibility->has_method_call = 1;
            if (operation.operand_count > eligibility->max_call_argument_count)
               eligibility->max_call_argument_count = operation.operand_count;
         }
         for (operand_index = 0U; operand_index < operation.operand_count; ++operand_index)
            if (!tcirJitTypeIsSupported(tcirValueType(operation.operands[operand_index])))
            {
               tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE,
                                    operation.source.tc_pc,
                                    "SLJIT baseline operation has an unsupported operand");
               return TCIR_JIT_COMPILE_INELIGIBLE;
            }
      }

      if (tcirBlockTerminator(block, &terminator) != TCIR_STATUS_OK
          || (terminator.kind != TCIR_TERMINATOR_BRANCH
              && terminator.kind != TCIR_TERMINATOR_BRANCH_IF
              && terminator.kind != TCIR_TERMINATOR_SWITCH
              && terminator.kind != TCIR_TERMINATOR_RETURN))
      {
         tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TERMINATOR,
                              tcirBlockSource(block).tc_pc,
                              "function contains a terminator unsupported by the SLJIT baseline");
         return TCIR_JIT_COMPILE_INELIGIBLE;
      }
      if (terminator.value != NULL && !tcirJitTypeIsSupported(tcirValueType(terminator.value)))
      {
         tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE,
                              terminator.source.tc_pc,
                              "SLJIT baseline terminator has an unsupported value");
         return TCIR_JIT_COMPILE_INELIGIBLE;
      }
      for (edge_index = 0U; edge_index < terminator.edge_count; ++edge_index)
      {
         size_t edge_argument_index;
         for (edge_argument_index = 0U;
              edge_argument_index < terminator.edges[edge_index].argument_count;
              ++edge_argument_index)
            if (!tcirJitTypeIsSupported(
                   tcirValueType(terminator.edges[edge_index].arguments[edge_argument_index])))
            {
               tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INELIGIBLE_TYPE,
                                    terminator.source.tc_pc,
                                    "SLJIT baseline edge has an unsupported argument");
               return TCIR_JIT_COMPILE_INELIGIBLE;
            }
      }
   }

   return TCIR_JIT_COMPILE_READY;
}

TCIRJitCompileStatus tcirJitCheckEligibility(
   const TCIRFunction *function,
   TCIRJitDiagnostic *diagnostic)
{
   TCIRJitEligibility eligibility;
   tcirJitDiagnosticClear(diagnostic);
   return tcirJitInspectEligibility(function, &eligibility, diagnostic);
}

static int tcirJitBeforeEmission(TCIRJitEmitter *emitter)
{
   if (emitter->emission_limit != 0U && emitter->emitted_count >= emitter->emission_limit)
   {
      tcirJitSetDiagnostic(emitter->diagnostic, TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED, emitter->tc_pc,
                           "SLJIT emission limit reached");
      return 0;
   }
   ++emitter->emitted_count;
   return 1;
}

static int tcirJitEmitOp1(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 dst,
   sljit_sw dstw,
   sljit_s32 src,
   sljit_sw srcw)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_op1(emitter->compiler, op, dst, dstw, src, srcw) == SLJIT_SUCCESS;
}

static int tcirJitEmitOp2(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 dst,
   sljit_sw dstw,
   sljit_s32 src1,
   sljit_sw src1w,
   sljit_s32 src2,
   sljit_sw src2w)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_op2(emitter->compiler, op, dst, dstw, src1, src1w, src2, src2w) == SLJIT_SUCCESS;
}

static int tcirJitEmitOp2U(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 src1,
   sljit_sw src1w,
   sljit_s32 src2,
   sljit_sw src2w)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_op2u(emitter->compiler, op, src1, src1w, src2, src2w) == SLJIT_SUCCESS;
}

static int tcirJitEmitOpFlags(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 dst,
   sljit_sw dstw,
   sljit_s32 type)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_op_flags(emitter->compiler, op, dst, dstw, type) == SLJIT_SUCCESS;
}

static int tcirJitEmitFop1(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 dst,
   sljit_sw dstw,
   sljit_s32 src,
   sljit_sw srcw)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_fop1(emitter->compiler, op, dst, dstw, src, srcw) == SLJIT_SUCCESS;
}

static int tcirJitEmitFop2(
   TCIRJitEmitter *emitter,
   sljit_s32 op,
   sljit_s32 dst,
   sljit_sw dstw,
   sljit_s32 src1,
   sljit_sw src1w,
   sljit_s32 src2,
   sljit_sw src2w)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_fop2(emitter->compiler, op, dst, dstw,
                         src1, src1w, src2, src2w) == SLJIT_SUCCESS;
}

static struct sljit_label *tcirJitEmitLabel(TCIRJitEmitter *emitter)
{
   if (!tcirJitBeforeEmission(emitter))
      return NULL;
   return sljit_emit_label(emitter->compiler);
}

static struct sljit_jump *tcirJitEmitJump(TCIRJitEmitter *emitter, sljit_s32 type)
{
   if (!tcirJitBeforeEmission(emitter))
      return NULL;
   return sljit_emit_jump(emitter->compiler, type);
}

static struct sljit_jump *tcirJitEmitCompare(
   TCIRJitEmitter *emitter,
   sljit_s32 type,
   sljit_s32 src1,
   sljit_sw src1w,
   sljit_s32 src2,
   sljit_sw src2w)
{
   if (!tcirJitBeforeEmission(emitter))
      return NULL;
   return sljit_emit_cmp(emitter->compiler, type, src1, src1w, src2, src2w);
}

static int tcirJitEmitReturn(TCIRJitEmitter *emitter, sljit_s32 src, sljit_sw srcw)
{
   return tcirJitBeforeEmission(emitter)
      && sljit_emit_return(emitter->compiler, SLJIT_MOV32, src, srcw) == SLJIT_SUCCESS;
}

static sljit_s32 tcirJitMoveForType(TCIRType type);
static int tcirJitLoadValue(
   TCIRJitEmitter *emitter,
   sljit_s32 register_id,
   const TCIRValue *value);
static int tcirJitStoreValue(
   TCIRJitEmitter *emitter,
   const TCIRValue *value,
   sljit_s32 register_id);
static int tcirJitLoadF64(
   TCIRJitEmitter *emitter,
   sljit_s32 register_id,
   const TCIRValue *value);
static int tcirJitStoreF64(
   TCIRJitEmitter *emitter,
   const TCIRValue *value,
   sljit_s32 register_id);

static int tcirJitEmitMethodCall(
   TCIRJitEmitter *emitter,
   const TCIROperationView *operation)
{
   const sljit_sw call_offset = (sljit_sw)offsetof(TCCompiledFrame, call);
   struct sljit_jump *success_jump;
   struct sljit_label *success_label;
   size_t index;

   for (index = 0U; index < operation->gc_home_count; ++index)
   {
      const TCIRGCHome *home = &operation->gc_homes[index];
      if (!tcirJitEmitOp1(
             emitter,
             SLJIT_MOV_P,
             SLJIT_R0,
             0,
             SLJIT_MEM1(SLJIT_S0),
             (sljit_sw)offsetof(TCCompiledFrame, ref_homes)) ||
          !tcirJitLoadValue(emitter, SLJIT_R1, home->value) ||
          !tcirJitEmitOp1(
             emitter,
             SLJIT_MOV_P,
             SLJIT_MEM1(SLJIT_R0),
             (sljit_sw)((size_t)home->home_index * sizeof(void *)),
             SLJIT_R1,
             0))
         return 0;
   }
   if (!tcirJitEmitOp1(
          emitter,
          SLJIT_MOV_P,
          SLJIT_R0,
          0,
          SLJIT_MEM1(SLJIT_S0),
          (sljit_sw)offsetof(TCCompiledFrame, call_arguments)))
      return 0;
   for (index = 0U; index < operation->operand_count; ++index)
   {
      TCIRType type = tcirValueType(operation->operands[index]);
      sljit_sw offset = (sljit_sw)(index * sizeof(TCIRRuntimeValue));
      if (type == TCIR_TYPE_F64)
      {
         if (!tcirJitLoadF64(emitter, SLJIT_FR0, operation->operands[index]) ||
             !tcirJitEmitFop1(
                emitter, SLJIT_MOV_F64, SLJIT_MEM1(SLJIT_R0), offset, SLJIT_FR0, 0))
            return 0;
      }
      else if (!tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[index]) ||
               !tcirJitEmitOp1(
                  emitter,
                  tcirJitMoveForType(type),
                  SLJIT_MEM1(SLJIT_R0),
                  offset,
                  SLJIT_R1,
                  0))
         return 0;
   }
   if (!tcirJitEmitOp1(
          emitter,
          SLJIT_MOV32,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, constant_pool_index),
          SLJIT_IMM,
          (sljit_sw)tcirSymbolConstantPoolIndex(operation->symbol)) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV32,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, kind),
          SLJIT_IMM,
          (sljit_sw)TCIR_CALL_STATIC) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV_P,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, receiver),
          SLJIT_IMM,
          0) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV_P,
          SLJIT_R0,
          0,
          SLJIT_MEM1(SLJIT_S0),
          (sljit_sw)offsetof(TCCompiledFrame, call_arguments)) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV_P,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, arguments),
          SLJIT_R0,
          0) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, argument_count),
          SLJIT_IMM,
          (sljit_sw)operation->operand_count) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV32,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, result_type),
          SLJIT_IMM,
          (sljit_sw)operation->result_type) ||
       !tcirJitEmitOp1(
          emitter,
          SLJIT_MOV32,
          SLJIT_MEM1(SLJIT_S0),
          call_offset + (sljit_sw)offsetof(TCCompiledCall, tc_pc),
          SLJIT_IMM,
          (sljit_sw)operation->source.tc_pc) ||
       !tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0, SLJIT_S0, 0) ||
       !tcirJitBeforeEmission(emitter) ||
       sljit_emit_icall(
          emitter->compiler,
          SLJIT_CALL,
          SLJIT_ARGS1(32, P),
          SLJIT_IMM,
          (sljit_sw)tcirJitInvokeMethodCall) != SLJIT_SUCCESS)
      return 0;
   success_jump = tcirJitEmitCompare(
      emitter, SLJIT_EQUAL | SLJIT_32, SLJIT_R0, 0, SLJIT_IMM, (sljit_sw)TC_COMPILED_RETURNED);
   if (success_jump == NULL || !tcirJitEmitReturn(emitter, SLJIT_IMM, 0))
      return 0;
   success_label = tcirJitEmitLabel(emitter);
   if (success_label == NULL)
      return 0;
   sljit_set_label(success_jump, success_label);
   if (operation->result != NULL)
   {
      TCIRType type = operation->result_type;
      if (!tcirJitEmitOp1(
             emitter,
             SLJIT_MOV_P,
             SLJIT_R0,
             0,
             SLJIT_MEM1(SLJIT_S0),
             (sljit_sw)offsetof(TCCompiledFrame, call_result)))
         return 0;
      if (type == TCIR_TYPE_F64)
      {
         if (!tcirJitEmitFop1(
                emitter,
                SLJIT_MOV_F64,
                SLJIT_FR0,
                0,
                SLJIT_MEM1(SLJIT_R0),
                (sljit_sw)offsetof(TCCompiledResult, value)) ||
             !tcirJitStoreF64(emitter, operation->result, SLJIT_FR0))
            return 0;
      }
      else if (!tcirJitEmitOp1(
                  emitter,
                  tcirJitMoveForType(type),
                  SLJIT_R1,
                  0,
                  SLJIT_MEM1(SLJIT_R0),
                  (sljit_sw)offsetof(TCCompiledResult, value)) ||
               !tcirJitStoreValue(emitter, operation->result, SLJIT_R1))
         return 0;
   }
   for (index = 0U; index < operation->gc_home_count; ++index)
   {
      const TCIRGCHome *home = &operation->gc_homes[index];
      if (!tcirJitEmitOp1(
             emitter,
             SLJIT_MOV_P,
             SLJIT_R0,
             0,
             SLJIT_MEM1(SLJIT_S0),
             (sljit_sw)offsetof(TCCompiledFrame, ref_homes)) ||
          !tcirJitEmitOp1(
             emitter,
             SLJIT_MOV_P,
             SLJIT_R1,
             0,
             SLJIT_MEM1(SLJIT_R0),
             (sljit_sw)((size_t)home->home_index * sizeof(void *))) ||
          !tcirJitStoreValue(emitter, home->value, SLJIT_R1))
         return 0;
   }
   return 1;
}

static sljit_sw tcirJitValueOffset(const TCIRValue *value)
{
   return (sljit_sw)((size_t)tcirValueId(value) * sizeof(TCIRRuntimeValue));
}

static sljit_s32 tcirJitMoveForType(TCIRType type)
{
   if (tcirJitTypeIsI32Like(type))
      return SLJIT_MOV32;
   if (type == TCIR_TYPE_REF)
      return SLJIT_MOV_P;
   return SLJIT_MOV;
}

static int tcirJitLoadValue(TCIRJitEmitter *emitter, sljit_s32 register_id, const TCIRValue *value)
{
   sljit_s32 move = tcirJitMoveForType(tcirValueType(value));
   return tcirJitEmitOp1(emitter, move, register_id, 0,
                         SLJIT_MEM1(SLJIT_S1), tcirJitValueOffset(value));
}

static int tcirJitStoreValue(TCIRJitEmitter *emitter, const TCIRValue *value, sljit_s32 register_id)
{
   sljit_s32 move = tcirJitMoveForType(tcirValueType(value));
   return tcirJitEmitOp1(emitter, move,
                         SLJIT_MEM1(SLJIT_S1), tcirJitValueOffset(value), register_id, 0);
}

static int tcirJitLoadF64(TCIRJitEmitter *emitter, sljit_s32 register_id, const TCIRValue *value)
{
   return tcirJitEmitFop1(emitter, SLJIT_MOV_F64, register_id, 0,
                           SLJIT_MEM1(SLJIT_S1), tcirJitValueOffset(value));
}

static int tcirJitStoreF64(TCIRJitEmitter *emitter, const TCIRValue *value, sljit_s32 register_id)
{
   return tcirJitEmitFop1(emitter, SLJIT_MOV_F64,
                           SLJIT_MEM1(SLJIT_S1), tcirJitValueOffset(value), register_id, 0);
}

static size_t tcirJitBlockIndex(const TCIRFunction *function, const TCIRBlock *target)
{
   size_t index;
   for (index = 0U; index < tcirFunctionBlockCount(function); ++index)
      if (tcirFunctionBlockAt(function, index) == target)
         return index;
   return (size_t)-1;
}

static int tcirJitAppendJump(
   TCIRJitEmitter *emitter,
   struct sljit_jump *jump,
   const TCIRBlock *target)
{
   size_t target_index = tcirJitBlockIndex(emitter->function, target);
   if (jump == NULL || target_index == (size_t)-1)
      return 0;
   if (emitter->jump_count == emitter->jump_capacity)
   {
      size_t next_capacity = emitter->jump_capacity == 0U ? 8U : emitter->jump_capacity * 2U;
      TCIRJitPendingJump *next;
      if (next_capacity < emitter->jump_capacity)
         return 0;
      next = (TCIRJitPendingJump *)realloc(emitter->jumps, next_capacity * sizeof(*next));
      if (next == NULL)
         return 0;
      emitter->jumps = next;
      emitter->jump_capacity = next_capacity;
   }
   emitter->jumps[emitter->jump_count].jump = jump;
   emitter->jumps[emitter->jump_count].target_index = target_index;
   ++emitter->jump_count;
   return 1;
}

static int tcirJitEmitPC(TCIRJitEmitter *emitter, unsigned int tc_pc)
{
   emitter->tc_pc = tc_pc;
   return tcirJitEmitOp1(emitter, SLJIT_MOV32,
                         SLJIT_MEM1(SLJIT_S0), (sljit_sw)offsetof(TCCompiledFrame, tc_pc),
                         SLJIT_IMM, (sljit_sw)tc_pc);
}

static int tcirJitEmitOperation(TCIRJitEmitter *emitter, const TCIROperationView *operation)
{
   sljit_s32 arithmetic_op;
   sljit_s32 comparison_flag;
   sljit_s32 comparison_type;

   if (!tcirJitEmitPC(emitter, operation->source.tc_pc))
      return 0;
   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
         return tcirJitEmitOp1(emitter, SLJIT_MOV32, SLJIT_R0, 0,
                               SLJIT_IMM, (sljit_sw)operation->immediate_i32)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_CONST_I64:
         return tcirJitEmitOp1(emitter, SLJIT_MOV, SLJIT_R0, 0,
                               SLJIT_IMM, (sljit_sw)operation->immediate_i64)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_CONST_F64:
         return tcirJitEmitOp1(emitter, SLJIT_MOV, SLJIT_R0, 0,
                               SLJIT_IMM, (sljit_sw)operation->immediate_f64_bits)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_CONST_REF_NULL:
         return tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0, SLJIT_IMM, 0)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_COPY:
         if (operation->result_type == TCIR_TYPE_F64)
            return tcirJitLoadF64(emitter, SLJIT_FR0, operation->operands[0])
               && tcirJitStoreF64(emitter, operation->result, SLJIT_FR0);
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_ADD_I32:
         arithmetic_op = SLJIT_ADD32;
         break;
      case TCIR_OP_SUB_I32:
         arithmetic_op = SLJIT_SUB32;
         break;
      case TCIR_OP_MUL_I32:
         arithmetic_op = SLJIT_MUL32;
         break;
      case TCIR_OP_SHL_I32:
         arithmetic_op = SLJIT_MSHL32;
         break;
      case TCIR_OP_SHR_I32:
         arithmetic_op = SLJIT_MASHR32;
         break;
      case TCIR_OP_USHR_I32:
         arithmetic_op = SLJIT_MLSHR32;
         break;
      case TCIR_OP_AND_I32:
         arithmetic_op = SLJIT_AND32;
         break;
      case TCIR_OP_OR_I32:
         arithmetic_op = SLJIT_OR32;
         break;
      case TCIR_OP_XOR_I32:
         arithmetic_op = SLJIT_XOR32;
         break;
      case TCIR_OP_TRUNC_I32_I8:
      case TCIR_OP_TRUNC_I32_I16:
      case TCIR_OP_ZEXT_I16_I32:
      {
         sljit_sw mask = operation->opcode == TCIR_OP_TRUNC_I32_I8 ? 0xff : 0xffff;
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitOp2(emitter, SLJIT_AND32, SLJIT_R0, 0,
                              SLJIT_R0, 0, SLJIT_IMM, mask)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      }
      case TCIR_OP_SEXT_I8_I32:
      case TCIR_OP_SEXT_I16_I32:
      {
         sljit_sw shift = operation->opcode == TCIR_OP_SEXT_I8_I32 ? 24 : 16;
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitOp2(emitter, SLJIT_SHL32, SLJIT_R0, 0,
                              SLJIT_R0, 0, SLJIT_IMM, shift)
            && tcirJitEmitOp2(emitter, SLJIT_ASHR32, SLJIT_R0, 0,
                              SLJIT_R0, 0, SLJIT_IMM, shift)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      }
      case TCIR_OP_CMP_EQ_I32:
         comparison_flag = SLJIT_SET_Z;
         comparison_type = SLJIT_EQUAL;
         goto comparison;
      case TCIR_OP_CMP_LT_I32:
         comparison_flag = SLJIT_SET_SIG_LESS;
         comparison_type = SLJIT_SIG_LESS;
         goto comparison;
      case TCIR_OP_CMP_LE_I32:
         comparison_flag = SLJIT_SET_SIG_LESS_EQUAL;
         comparison_type = SLJIT_SIG_LESS_EQUAL;
         goto comparison;
      case TCIR_OP_CMP_GT_I32:
         comparison_flag = SLJIT_SET_SIG_GREATER;
         comparison_type = SLJIT_SIG_GREATER;
         goto comparison;
      case TCIR_OP_CMP_GE_I32:
         comparison_flag = SLJIT_SET_SIG_GREATER_EQUAL;
         comparison_type = SLJIT_SIG_GREATER_EQUAL;
         goto comparison;
      case TCIR_OP_ADD_I64:
         arithmetic_op = SLJIT_ADD;
         goto arithmetic_i64;
      case TCIR_OP_SUB_I64:
         arithmetic_op = SLJIT_SUB;
         goto arithmetic_i64;
      case TCIR_OP_MUL_I64:
         arithmetic_op = SLJIT_MUL;
         goto arithmetic_i64;
      case TCIR_OP_SHL_I64:
         arithmetic_op = SLJIT_MSHL;
         goto arithmetic_i64;
      case TCIR_OP_SHR_I64:
         arithmetic_op = SLJIT_MASHR;
         goto arithmetic_i64;
      case TCIR_OP_USHR_I64:
         arithmetic_op = SLJIT_MLSHR;
         goto arithmetic_i64;
      case TCIR_OP_AND_I64:
         arithmetic_op = SLJIT_AND;
         goto arithmetic_i64;
      case TCIR_OP_OR_I64:
         arithmetic_op = SLJIT_OR;
         goto arithmetic_i64;
      case TCIR_OP_XOR_I64:
         arithmetic_op = SLJIT_XOR;
         goto arithmetic_i64;
      case TCIR_OP_TRUNC_I64_I32:
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitOp1(emitter, SLJIT_MOV32, SLJIT_R0, 0, SLJIT_R0, 0)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_SEXT_I32_I64:
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitOp1(emitter, SLJIT_MOV_S32, SLJIT_R0, 0, SLJIT_R0, 0)
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
      case TCIR_OP_CMP_EQ_I64:
         comparison_flag = SLJIT_SET_Z;
         comparison_type = SLJIT_EQUAL;
         goto comparison_i64;
      case TCIR_OP_CMP_EQ_REF:
         comparison_flag = SLJIT_SET_Z;
         comparison_type = SLJIT_EQUAL;
         goto comparison_word;
      case TCIR_OP_CMP_LT_I64:
         comparison_flag = SLJIT_SET_SIG_LESS;
         comparison_type = SLJIT_SIG_LESS;
         goto comparison_i64;
      case TCIR_OP_CMP_LE_I64:
         comparison_flag = SLJIT_SET_SIG_LESS_EQUAL;
         comparison_type = SLJIT_SIG_LESS_EQUAL;
         goto comparison_i64;
      case TCIR_OP_CMP_GT_I64:
         comparison_flag = SLJIT_SET_SIG_GREATER;
         comparison_type = SLJIT_SIG_GREATER;
         goto comparison_i64;
      case TCIR_OP_CMP_GE_I64:
         comparison_flag = SLJIT_SET_SIG_GREATER_EQUAL;
         comparison_type = SLJIT_SIG_GREATER_EQUAL;
         goto comparison_i64;
      case TCIR_OP_ADD_F64:
         arithmetic_op = SLJIT_ADD_F64;
         goto arithmetic_f64;
      case TCIR_OP_SUB_F64:
         arithmetic_op = SLJIT_SUB_F64;
         goto arithmetic_f64;
      case TCIR_OP_MUL_F64:
         arithmetic_op = SLJIT_MUL_F64;
         goto arithmetic_f64;
      case TCIR_OP_CMP_EQ_F64:
         comparison_flag = SLJIT_SET_ORDERED_EQUAL;
         comparison_type = SLJIT_ORDERED_EQUAL;
         goto comparison_f64;
      case TCIR_OP_CMP_LT_F64:
         comparison_flag = SLJIT_SET_ORDERED_LESS;
         comparison_type = SLJIT_ORDERED_LESS;
         goto comparison_f64;
      case TCIR_OP_CMP_LE_F64:
         comparison_flag = SLJIT_SET_ORDERED_LESS_EQUAL;
         comparison_type = SLJIT_ORDERED_LESS_EQUAL;
         goto comparison_f64;
      case TCIR_OP_CMP_GT_F64:
         comparison_flag = SLJIT_SET_ORDERED_GREATER;
         comparison_type = SLJIT_ORDERED_GREATER;
         goto comparison_f64;
      case TCIR_OP_CMP_GE_F64:
         comparison_flag = SLJIT_SET_ORDERED_GREATER_EQUAL;
         comparison_type = SLJIT_ORDERED_GREATER_EQUAL;
         goto comparison_f64;
      case TCIR_OP_I32_TO_F64:
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitFop1(emitter, SLJIT_CONV_F64_FROM_S32,
                                SLJIT_FR0, 0, SLJIT_R0, 0)
            && tcirJitStoreF64(emitter, operation->result, SLJIT_FR0);
      case TCIR_OP_I64_TO_F64:
         return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
            && tcirJitEmitFop1(emitter, SLJIT_CONV_F64_FROM_SW,
                                SLJIT_FR0, 0, SLJIT_R0, 0)
            && tcirJitStoreF64(emitter, operation->result, SLJIT_FR0);
      case TCIR_OP_LOAD_SLOT:
      {
         size_t pointer_offset = operation->home_bank == TCIR_HOME_I32
            ? offsetof(TCCompiledFrame, i32_homes)
            : (operation->home_bank == TCIR_HOME_REF
               ? offsetof(TCCompiledFrame, ref_homes) : offsetof(TCCompiledFrame, v64_homes));
         size_t item_size = operation->home_bank == TCIR_HOME_I32
            ? sizeof(int32_t)
            : (operation->home_bank == TCIR_HOME_REF ? sizeof(void *) : sizeof(TCIRV64Home));
         sljit_s32 move = operation->home_bank == TCIR_HOME_I32
            ? SLJIT_MOV32 : (operation->home_bank == TCIR_HOME_REF ? SLJIT_MOV_P : SLJIT_MOV);
         if (operation->result_type == TCIR_TYPE_F64)
            return tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0,
                                  SLJIT_MEM1(SLJIT_S0), (sljit_sw)pointer_offset)
               && tcirJitEmitFop1(emitter, SLJIT_MOV_F64, SLJIT_FR0, 0,
                                   SLJIT_MEM1(SLJIT_R0),
                                   (sljit_sw)((size_t)operation->home_index * item_size))
               && tcirJitStoreF64(emitter, operation->result, SLJIT_FR0);
         return tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0,
                               SLJIT_MEM1(SLJIT_S0), (sljit_sw)pointer_offset)
            && tcirJitEmitOp1(emitter, move, SLJIT_R1, 0, SLJIT_MEM1(SLJIT_R0),
                              (sljit_sw)((size_t)operation->home_index * item_size))
            && tcirJitStoreValue(emitter, operation->result, SLJIT_R1);
      }
      case TCIR_OP_STORE_SLOT:
      {
         size_t pointer_offset = operation->home_bank == TCIR_HOME_I32
            ? offsetof(TCCompiledFrame, i32_homes)
            : (operation->home_bank == TCIR_HOME_REF
               ? offsetof(TCCompiledFrame, ref_homes) : offsetof(TCCompiledFrame, v64_homes));
         size_t item_size = operation->home_bank == TCIR_HOME_I32
            ? sizeof(int32_t)
            : (operation->home_bank == TCIR_HOME_REF ? sizeof(void *) : sizeof(TCIRV64Home));
         sljit_s32 move = operation->home_bank == TCIR_HOME_I32
            ? SLJIT_MOV32 : (operation->home_bank == TCIR_HOME_REF ? SLJIT_MOV_P : SLJIT_MOV);
         if (tcirValueType(operation->operands[0]) == TCIR_TYPE_F64)
            return tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0,
                                  SLJIT_MEM1(SLJIT_S0), (sljit_sw)pointer_offset)
               && tcirJitLoadF64(emitter, SLJIT_FR0, operation->operands[0])
               && tcirJitEmitFop1(emitter, SLJIT_MOV_F64,
                                   SLJIT_MEM1(SLJIT_R0),
                                   (sljit_sw)((size_t)operation->home_index * item_size),
                                   SLJIT_FR0, 0);
         return tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_R0, 0,
                               SLJIT_MEM1(SLJIT_S0), (sljit_sw)pointer_offset)
            && tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[0])
            && tcirJitEmitOp1(emitter, move, SLJIT_MEM1(SLJIT_R0),
                              (sljit_sw)((size_t)operation->home_index * item_size), SLJIT_R1, 0);
      }
      case TCIR_OP_METHOD_CALL:
         return tcirJitEmitMethodCall(emitter, operation);
      default:
         return 0;
   }

   return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
      && tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[1])
      && tcirJitEmitOp2(emitter, arithmetic_op, SLJIT_R0, 0, SLJIT_R0, 0, SLJIT_R1, 0)
      && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);

comparison:
   return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
      && tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[1])
      && tcirJitEmitOp2U(emitter, SLJIT_SUB32 | comparison_flag, SLJIT_R0, 0, SLJIT_R1, 0)
      && tcirJitEmitOpFlags(emitter, SLJIT_MOV32, SLJIT_R0, 0, comparison_type)
      && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);

arithmetic_i64:
   return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
      && tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[1])
      && tcirJitEmitOp2(emitter, arithmetic_op, SLJIT_R0, 0, SLJIT_R0, 0, SLJIT_R1, 0)
      && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);

comparison_i64:
comparison_word:
   return tcirJitLoadValue(emitter, SLJIT_R0, operation->operands[0])
      && tcirJitLoadValue(emitter, SLJIT_R1, operation->operands[1])
      && tcirJitEmitOp2U(emitter, SLJIT_SUB | comparison_flag, SLJIT_R0, 0, SLJIT_R1, 0)
      && tcirJitEmitOpFlags(emitter, SLJIT_MOV32, SLJIT_R0, 0, comparison_type)
      && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);

arithmetic_f64:
   return tcirJitLoadF64(emitter, SLJIT_FR0, operation->operands[0])
      && tcirJitLoadF64(emitter, SLJIT_FR1, operation->operands[1])
      && tcirJitEmitFop2(emitter, arithmetic_op, SLJIT_FR0, 0,
                          SLJIT_FR0, 0, SLJIT_FR1, 0)
      && tcirJitStoreF64(emitter, operation->result, SLJIT_FR0);

comparison_f64:
   return tcirJitLoadF64(emitter, SLJIT_FR0, operation->operands[0])
      && tcirJitLoadF64(emitter, SLJIT_FR1, operation->operands[1])
      && tcirJitEmitFop1(emitter, SLJIT_CMP_F64 | comparison_flag,
                          SLJIT_FR0, 0, SLJIT_FR1, 0)
      && tcirJitEmitOpFlags(emitter, SLJIT_MOV32, SLJIT_R0, 0, comparison_type)
      && tcirJitStoreValue(emitter, operation->result, SLJIT_R0);
}

static int tcirJitEmitEdge(TCIRJitEmitter *emitter, const TCIREdge *edge)
{
   size_t index;
   struct sljit_jump *jump;

   for (index = 0U; index < edge->argument_count; ++index)
   {
      if (tcirValueType(edge->arguments[index]) == TCIR_TYPE_F64)
      {
         if (!tcirJitLoadF64(emitter, SLJIT_FR0, edge->arguments[index]) ||
             !tcirJitEmitFop1(emitter, SLJIT_MOV_F64, SLJIT_MEM1(SLJIT_S2),
                               (sljit_sw)(index * sizeof(TCIRRuntimeValue)), SLJIT_FR0, 0))
            return 0;
         continue;
      }
      sljit_s32 move = tcirJitMoveForType(tcirValueType(edge->arguments[index]));
      if (!tcirJitLoadValue(emitter, SLJIT_R0, edge->arguments[index]) ||
          !tcirJitEmitOp1(emitter, move, SLJIT_MEM1(SLJIT_S2),
                          (sljit_sw)(index * sizeof(TCIRRuntimeValue)), SLJIT_R0, 0))
         return 0;
   }
   for (index = 0U; index < edge->argument_count; ++index)
   {
      const TCIRValue *target = tcirBlockArgumentAt(edge->target, index);
      if (tcirValueType(target) == TCIR_TYPE_F64)
      {
         if (!tcirJitEmitFop1(emitter, SLJIT_MOV_F64, SLJIT_FR0, 0, SLJIT_MEM1(SLJIT_S2),
                               (sljit_sw)(index * sizeof(TCIRRuntimeValue))) ||
             !tcirJitStoreF64(emitter, target, SLJIT_FR0))
            return 0;
         continue;
      }
      sljit_s32 move = tcirJitMoveForType(tcirValueType(target));
      if (!tcirJitEmitOp1(emitter, move, SLJIT_R0, 0, SLJIT_MEM1(SLJIT_S2),
                          (sljit_sw)(index * sizeof(TCIRRuntimeValue))) ||
          !tcirJitStoreValue(emitter, target, SLJIT_R0))
         return 0;
   }

   jump = tcirJitEmitJump(emitter, SLJIT_JUMP);
   return tcirJitAppendJump(emitter, jump, edge->target);
}

static int tcirJitEmitSwitch(TCIRJitEmitter *emitter, const TCIRTerminatorView *terminator)
{
   struct sljit_jump **case_jumps;
   const TCIREdge *default_edge = NULL;
   size_t edge_index;

   case_jumps = (struct sljit_jump **)calloc(
      terminator->edge_count == 0U ? 1U : terminator->edge_count, sizeof(*case_jumps));
   if (case_jumps == NULL || !tcirJitLoadValue(emitter, SLJIT_R0, terminator->value))
      goto failed;
   for (edge_index = 0U; edge_index < terminator->edge_count; ++edge_index)
   {
      const TCIREdge *edge = &terminator->edges[edge_index];
      if (!edge->has_case_value)
      {
         default_edge = edge;
         continue;
      }
      case_jumps[edge_index] = tcirJitEmitCompare(
         emitter,
         SLJIT_EQUAL | SLJIT_32,
         SLJIT_R0,
         0,
         SLJIT_IMM,
         (sljit_sw)edge->case_value);
      if (case_jumps[edge_index] == NULL)
         goto failed;
   }
   if (default_edge == NULL || !tcirJitEmitEdge(emitter, default_edge))
      goto failed;
   for (edge_index = 0U; edge_index < terminator->edge_count; ++edge_index)
   {
      struct sljit_label *label;
      if (case_jumps[edge_index] == NULL)
         continue;
      label = tcirJitEmitLabel(emitter);
      if (label == NULL)
         goto failed;
      sljit_set_label(case_jumps[edge_index], label);
      if (!tcirJitEmitEdge(emitter, &terminator->edges[edge_index]))
         goto failed;
   }
   free(case_jumps);
   return 1;

failed:
   free(case_jumps);
   return 0;
}

static int tcirJitEmitTerminator(TCIRJitEmitter *emitter, const TCIRTerminatorView *terminator)
{
   if (!tcirJitEmitPC(emitter, terminator->source.tc_pc))
      return 0;
   switch (terminator->kind)
   {
      case TCIR_TERMINATOR_BRANCH:
         return tcirJitEmitEdge(emitter, &terminator->edges[0]);
      case TCIR_TERMINATOR_BRANCH_IF:
      {
         struct sljit_jump *true_jump;
         struct sljit_label *true_label;
         if (!tcirJitLoadValue(emitter, SLJIT_R0, terminator->value))
            return 0;
         true_jump = tcirJitEmitCompare(emitter, SLJIT_NOT_EQUAL | SLJIT_32,
                                        SLJIT_R0, 0, SLJIT_IMM, 0);
         if (true_jump == NULL || !tcirJitEmitEdge(emitter, &terminator->edges[1]))
            return 0;
         true_label = tcirJitEmitLabel(emitter);
         if (true_label == NULL)
            return 0;
         sljit_set_label(true_jump, true_label);
         return tcirJitEmitEdge(emitter, &terminator->edges[0]);
      }
      case TCIR_TERMINATOR_SWITCH:
         return tcirJitEmitSwitch(emitter, terminator);
      case TCIR_TERMINATOR_RETURN:
         if (terminator->value == NULL)
            return tcirJitEmitReturn(emitter, SLJIT_IMM, 0);
         if (tcirValueType(terminator->value) == TCIR_TYPE_I64)
            return tcirJitLoadValue(emitter, SLJIT_R0, terminator->value)
               && tcirJitEmitOp1(
                  emitter,
                  SLJIT_MOV,
                  SLJIT_MEM1(SLJIT_S0),
                  (sljit_sw)offsetof(TCCompiledFrame, jit_return_value),
                  SLJIT_R0,
                  0)
               && tcirJitEmitReturn(emitter, SLJIT_IMM, 0);
         if (tcirValueType(terminator->value) == TCIR_TYPE_F64)
            return tcirJitLoadF64(emitter, SLJIT_FR0, terminator->value)
               && tcirJitEmitFop1(
                  emitter,
                  SLJIT_MOV_F64,
                  SLJIT_MEM1(SLJIT_S0),
                  (sljit_sw)offsetof(TCCompiledFrame, jit_return_value),
                  SLJIT_FR0,
                  0)
               && tcirJitEmitReturn(emitter, SLJIT_IMM, 0);
         if (tcirValueType(terminator->value) == TCIR_TYPE_REF)
            return tcirJitLoadValue(emitter, SLJIT_R0, terminator->value)
               && tcirJitEmitOp1(
                  emitter,
                  SLJIT_MOV_P,
                  SLJIT_MEM1(SLJIT_S0),
                  (sljit_sw)offsetof(TCCompiledFrame, jit_return_value),
                  SLJIT_R0,
                  0)
               && tcirJitEmitReturn(emitter, SLJIT_IMM, 0);
         return tcirJitLoadValue(emitter, SLJIT_R0, terminator->value)
            && tcirJitEmitReturn(emitter, SLJIT_R0, 0);
      default:
         return 0;
   }
}

static int tcirJitEmitFunction(TCIRJitEmitter *emitter)
{
   size_t block_index;

   emitter->tc_pc = TCIR_TCPC_NONE;
   if (!tcirJitBeforeEmission(emitter)
       || sljit_emit_enter(emitter->compiler, 0, SLJIT_ARGS1(32, P),
                           2 | SLJIT_ENTER_FLOAT(2), 3, 0) != SLJIT_SUCCESS
       || !tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_S1, 0,
                          SLJIT_MEM1(SLJIT_S0), (sljit_sw)offsetof(TCCompiledFrame, scratch_values))
       || !tcirJitEmitOp1(emitter, SLJIT_MOV_P, SLJIT_S2, 0,
                          SLJIT_MEM1(SLJIT_S0), (sljit_sw)offsetof(TCCompiledFrame, edge_values)))
      return 0;

   for (block_index = 0U; block_index < tcirFunctionBlockCount(emitter->function); ++block_index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(emitter->function, block_index);
      TCIRTerminatorView terminator;
      size_t operation_index;

      emitter->labels[block_index] = tcirJitEmitLabel(emitter);
      if (emitter->labels[block_index] == NULL)
         return 0;
      for (operation_index = 0U; operation_index < tcirBlockOperationCount(block); ++operation_index)
      {
         TCIROperationView operation;
         if (tcirBlockOperationAt(block, operation_index, &operation) != TCIR_STATUS_OK
             || !tcirJitEmitOperation(emitter, &operation))
            return 0;
      }
      if (tcirBlockTerminator(block, &terminator) != TCIR_STATUS_OK
          || !tcirJitEmitTerminator(emitter, &terminator))
         return 0;
   }

   for (block_index = 0U; block_index < emitter->jump_count; ++block_index)
   {
      size_t target_index = emitter->jumps[block_index].target_index;
      if (target_index >= tcirFunctionBlockCount(emitter->function)
          || emitter->labels[target_index] == NULL)
         return 0;
      sljit_set_label(emitter->jumps[block_index].jump, emitter->labels[target_index]);
   }
   return sljit_get_compiler_error(emitter->compiler) == SLJIT_SUCCESS;
}

TCIRJitCompileStatus tcirJitCompile(
   const TCIRFunction *function,
   const TCIRJitCompileOptions *options,
   TCIRJitArtifact **artifact,
   TCIRJitDiagnostic *diagnostic)
{
   TCIRJitEligibility eligibility;
   TCIRJitCompileStatus eligibility_status;
   TCIRJitEmitter emitter;
   TCIRJitArtifact *created = NULL;
   size_t parameter_index;

   tcirJitDiagnosticClear(diagnostic);
   if (artifact == NULL)
   {
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                           "SLJIT compile requires an artifact output");
      return TCIR_JIT_COMPILE_INELIGIBLE;
   }
   *artifact = NULL;
   eligibility_status = tcirJitInspectEligibility(function, &eligibility, diagnostic);
   if (eligibility_status != TCIR_JIT_COMPILE_READY)
      return eligibility_status;

   created = (TCIRJitArtifact *)calloc(1U, sizeof(*created));
   if (created == NULL)
      goto out_of_memory;
   created->parameter_count = tcirFunctionParameterCount(function);
   created->parameter_value_ids = (unsigned int *)calloc(
      created->parameter_count == 0U ? 1U : created->parameter_count,
      sizeof(*created->parameter_value_ids));
   created->parameter_types = (TCIRType *)calloc(
      created->parameter_count == 0U ? 1U : created->parameter_count,
      sizeof(*created->parameter_types));
   if (created->parameter_value_ids == NULL || created->parameter_types == NULL)
      goto out_of_memory;
   for (parameter_index = 0U; parameter_index < created->parameter_count; ++parameter_index)
   {
      created->parameter_value_ids[parameter_index] = tcirValueId(tcirFunctionParameter(function, parameter_index));
      created->parameter_types[parameter_index] = tcirValueType(tcirFunctionParameter(function, parameter_index));
   }
   created->value_count = eligibility.value_count;
   created->edge_value_count = eligibility.edge_value_count;
   created->i32_home_count = tcirFunctionHomeCount(function, TCIR_HOME_I32);
   created->ref_home_count = tcirFunctionHomeCount(function, TCIR_HOME_REF);
   created->v64_home_count = tcirFunctionHomeCount(function, TCIR_HOME_V64);
   created->return_type = tcirFunctionReturnType(function);
   created->max_call_argument_count = eligibility.max_call_argument_count;
   created->has_method_call = eligibility.has_method_call;

   memset(&emitter, 0, sizeof(emitter));
   emitter.function = function;
   emitter.diagnostic = diagnostic;
   emitter.emission_limit = options == NULL ? 0U : options->emission_limit;
   emitter.compiler = sljit_create_compiler(NULL);
   emitter.labels = (struct sljit_label **)calloc(
      tcirFunctionBlockCount(function) == 0U ? 1U : tcirFunctionBlockCount(function),
      sizeof(*emitter.labels));
   if (emitter.compiler == NULL || emitter.labels == NULL)
   {
      sljit_free_compiler(emitter.compiler);
      free(emitter.labels);
      goto out_of_memory;
   }
   if (!tcirJitEmitFunction(&emitter))
   {
      if (diagnostic == NULL || diagnostic->code == TCIR_JIT_DIAGNOSTIC_NONE)
         tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED, emitter.tc_pc,
                              "SLJIT failed while emitting verified eligible TCIR");
      sljit_free_compiler(emitter.compiler);
      free(emitter.jumps);
      free(emitter.labels);
      tcirJitArtifactDestroy(created);
      return TCIR_JIT_COMPILE_EMISSION_FAILED;
   }

   created->code = tcirJitExecutableMemoryFinalize(emitter.compiler, &created->code_size);
   if (created->code != NULL)
      created->entry = (TCIRJitEntry)created->code;
   sljit_free_compiler(emitter.compiler);
   free(emitter.jumps);
   free(emitter.labels);
   if (created->entry == NULL)
   {
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_EMISSION_FAILED, emitter.tc_pc,
                           "SLJIT could not finalize executable code");
      tcirJitArtifactDestroy(created);
      return TCIR_JIT_COMPILE_EMISSION_FAILED;
   }

   *artifact = created;
   return TCIR_JIT_COMPILE_READY;

out_of_memory:
   tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_OUT_OF_MEMORY, TCIR_TCPC_NONE,
                        "unable to allocate the SLJIT artifact");
   tcirJitArtifactDestroy(created);
   return TCIR_JIT_COMPILE_OUT_OF_MEMORY;
}

void tcirJitArtifactDestroy(TCIRJitArtifact *artifact)
{
   if (artifact != NULL)
   {
      if (artifact->code != NULL)
         tcirJitExecutableMemoryDispose(artifact->code);
      free(artifact->parameter_value_ids);
      free(artifact->parameter_types);
      free(artifact);
   }
}

size_t tcirJitArtifactCodeSize(const TCIRJitArtifact *artifact)
{
   return artifact == NULL ? 0U : artifact->code_size;
}

const void *tcirJitArtifactCodeAddress(const TCIRJitArtifact *artifact)
{
   return artifact == NULL ? NULL : artifact->code;
}

TCIRJitMemoryPolicy tcirJitArtifactMemoryPolicy(const TCIRJitArtifact *artifact)
{
   (void)artifact;
   return TCIR_JIT_MEMORY_WX;
}

TCCompiledStatus tcirJitInvoke(
   const TCIRJitArtifact *artifact,
   TCCompiledFrame *frame,
   TCCompiledResult *result,
   TCIRJitDiagnostic *diagnostic)
{
   TCIRRuntimeValue *scratch_values;
   TCIRRuntimeValue *edge_values;
   TCIRRuntimeValue *call_arguments;
   TCIRRuntimeValue *previous_call_arguments;
   TCCompiledResult call_result;
   TCCompiledResult *previous_call_result;
   TCCompiledCall previous_call;
   TCIRRuntimeValue *previous_scratch;
   TCIRRuntimeValue *previous_edge;
   size_t previous_call_argument_count;
   size_t previous_scratch_count;
   size_t previous_edge_count;
   sljit_s32 return_value;
   size_t parameter_index;

   tcirJitDiagnosticClear(diagnostic);
   if (result != NULL)
   {
      memset(result, 0, sizeof(*result));
      result->status = TC_COMPILED_REJECTED;
      result->type = TCIR_TYPE_VOID;
      result->tc_pc = TCIR_TCPC_NONE;
   }
   if (artifact == NULL || artifact->entry == NULL || frame == NULL || result == NULL
       || frame->argument_count != artifact->parameter_count
       || (frame->argument_count != 0U && frame->arguments == NULL)
       || frame->i32_home_count < artifact->i32_home_count
       || (frame->i32_home_count != 0U && frame->i32_homes == NULL)
       || frame->ref_home_count < artifact->ref_home_count
       || (frame->ref_home_count != 0U && frame->ref_homes == NULL)
       || frame->v64_home_count < artifact->v64_home_count
       || (frame->v64_home_count != 0U && frame->v64_homes == NULL)
       || (artifact->has_method_call &&
           (frame->runtime == NULL || frame->runtime->abi_version != TC_RUNTIME_ABI_VERSION ||
            frame->runtime->invoke == NULL)))
   {
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_INVALID_ARGUMENT, TCIR_TCPC_NONE,
                           "compiled frame does not match the SLJIT artifact ABI");
      return TC_COMPILED_REJECTED;
   }

   scratch_values = (TCIRRuntimeValue *)calloc(
      artifact->value_count == 0U ? 1U : artifact->value_count, sizeof(*scratch_values));
   edge_values = (TCIRRuntimeValue *)calloc(
      artifact->edge_value_count == 0U ? 1U : artifact->edge_value_count, sizeof(*edge_values));
   call_arguments = artifact->has_method_call
                       ? (TCIRRuntimeValue *)calloc(
                            artifact->max_call_argument_count == 0U
                               ? 1U
                               : artifact->max_call_argument_count,
                            sizeof(*call_arguments))
                       : NULL;
   if (scratch_values == NULL || edge_values == NULL ||
       (artifact->has_method_call && call_arguments == NULL))
   {
      free(call_arguments);
      free(edge_values);
      free(scratch_values);
      result->status = TC_COMPILED_OUT_OF_MEMORY;
      tcirJitSetDiagnostic(diagnostic, TCIR_JIT_DIAGNOSTIC_OUT_OF_MEMORY, TCIR_TCPC_NONE,
                           "unable to allocate the SLJIT invocation scratch frame");
      return TC_COMPILED_OUT_OF_MEMORY;
   }
   for (parameter_index = 0U; parameter_index < artifact->parameter_count; ++parameter_index)
   {
      unsigned int value_id = artifact->parameter_value_ids[parameter_index];
      if (artifact->parameter_types[parameter_index] == TCIR_TYPE_I64)
         scratch_values[value_id].i64 = frame->arguments[parameter_index].i64;
      else if (artifact->parameter_types[parameter_index] == TCIR_TYPE_F64)
         scratch_values[value_id].f64 = frame->arguments[parameter_index].f64;
      else if (artifact->parameter_types[parameter_index] == TCIR_TYPE_REF)
         scratch_values[value_id].ref = frame->arguments[parameter_index].ref;
      else
         scratch_values[value_id].i32 = frame->arguments[parameter_index].i32;
   }

   previous_scratch = frame->scratch_values;
   previous_scratch_count = frame->scratch_count;
   previous_edge = frame->edge_values;
   previous_edge_count = frame->edge_count;
   previous_call_arguments = frame->call_arguments;
   previous_call_argument_count = frame->call_argument_count;
   previous_call = frame->call;
   previous_call_result = frame->call_result;
   frame->scratch_values = scratch_values;
   frame->scratch_count = artifact->value_count;
   frame->edge_values = edge_values;
   frame->edge_count = artifact->edge_value_count;
   frame->call_arguments = call_arguments;
   frame->call_argument_count = artifact->max_call_argument_count;
   memset(&frame->call, 0, sizeof(frame->call));
   memset(&call_result, 0, sizeof(call_result));
   call_result.status = TC_COMPILED_RETURNED;
   call_result.type = TCIR_TYPE_VOID;
   call_result.tc_pc = TCIR_TCPC_NONE;
   frame->call_result = &call_result;
   memset(&frame->jit_return_value, 0, sizeof(frame->jit_return_value));
   frame->tc_pc = TCIR_TCPC_NONE;
   return_value = artifact->entry(frame);
   frame->scratch_values = previous_scratch;
   frame->scratch_count = previous_scratch_count;
   frame->edge_values = previous_edge;
   frame->edge_count = previous_edge_count;
   frame->call_arguments = previous_call_arguments;
   frame->call_argument_count = previous_call_argument_count;
   frame->call = previous_call;
   frame->call_result = previous_call_result;
   free(call_arguments);
   free(edge_values);
   free(scratch_values);

   if (call_result.status != TC_COMPILED_RETURNED)
   {
      *result = call_result;
      return call_result.status;
   }

   result->status = TC_COMPILED_RETURNED;
   result->type = artifact->return_type;
   result->tc_pc = frame->tc_pc;
   if (artifact->return_type == TCIR_TYPE_I32)
      result->value.i32 = (int32_t)return_value;
   else if (artifact->return_type == TCIR_TYPE_I64)
      result->value.i64 = frame->jit_return_value.i64;
   else if (artifact->return_type == TCIR_TYPE_F64)
      result->value.f64 = frame->jit_return_value.f64;
   else if (artifact->return_type == TCIR_TYPE_REF)
      result->value.ref = frame->jit_return_value.ref;
   return TC_COMPILED_RETURNED;
}
