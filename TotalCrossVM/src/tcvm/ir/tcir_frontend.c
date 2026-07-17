// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_frontend.h"

#include "../opcodes.h"
#include "tcir_decode.h"
#include "tcir_internal.h"

#include <stdlib.h>
#include <string.h>

typedef struct TCIRFrontendBlocks
{
   unsigned char *leaders;
   size_t *block_indexes;
   TCIRBlock **blocks;
   size_t block_count;
} TCIRFrontendBlocks;

static TCIRSourceLocation tcirFrontendSource(const TCIRMethodView *method, unsigned int pc)
{
   TCIRSourceLocation source;
   source.tc_pc = pc;
   source.source_line = method->source_lines == NULL ? -1 : method->source_lines[pc];
   return source;
}

static int tcirFrontendIsConditional(unsigned int opcode)
{
   return opcode == JEQ_regI_regI || opcode == JEQ_regI_s6 || opcode == JEQ_regI_sym ||
          opcode == JNE_regI_regI || opcode == JNE_regI_s6 || opcode == JNE_regI_sym ||
          opcode == JLT_regI_regI || opcode == JLT_regI_s6 ||
          opcode == JLE_regI_regI || opcode == JLE_regI_s6 ||
          opcode == JGT_regI_regI || opcode == JGT_regI_s6 ||
          opcode == JGE_regI_regI || opcode == JGE_regI_s6 ||
          opcode == DECJGTZ_regI || opcode == DECJGEZ_regI;
}

static int tcirFrontendIsReturn(unsigned int opcode)
{
   return opcode == RETURN_regI || opcode == RETURN_void ||
          opcode == RETURN_s24I || opcode == RETURN_symI;
}

static void tcirFrontendBlocksDestroy(TCIRFrontendBlocks *blocks)
{
   if (blocks == NULL)
      return;
   free(blocks->leaders);
   free(blocks->block_indexes);
   free(blocks->blocks);
   memset(blocks, 0, sizeof(*blocks));
}

static int tcirFrontendDiscoverBlocks(
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   TCIRFrontendBlocks *blocks,
   TCIRDiagnostic *diagnostic)
{
   size_t index;
   size_t block_index;

   memset(blocks, 0, sizeof(*blocks));
   blocks->leaders = (unsigned char *)calloc(method->code_slot_count, 1);
   blocks->block_indexes = (size_t *)malloc(method->code_slot_count * sizeof(size_t));
   if (blocks->leaders == NULL || blocks->block_indexes == NULL)
      goto out_of_memory;
   for (index = 0; index < method->code_slot_count; index++)
      blocks->block_indexes[index] = (size_t)-1;
   blocks->leaders[0] = 1;

   for (index = 0; index < decoded->instruction_count; index++)
   {
      const TCIRDecodedInstruction *instruction = &decoded->instructions[index];
      unsigned int opcode = instruction->info->value;
      if (opcode == JUMP_s24 || tcirFrontendIsConditional(opcode))
         blocks->leaders[(size_t)instruction->target] = 1;
      if (tcirFrontendIsConditional(opcode))
         blocks->leaders[instruction->pc + instruction->width] = 1;
   }

   for (index = 0; index < method->code_slot_count; index++)
      if (blocks->leaders[index])
         blocks->block_count++;
   blocks->blocks = (TCIRBlock **)calloc(blocks->block_count, sizeof(TCIRBlock *));
   if (blocks->blocks == NULL)
      goto out_of_memory;

   block_index = 0;
   for (index = 0; index < method->code_slot_count; index++)
      if (blocks->leaders[index])
         blocks->block_indexes[index] = block_index++;
   return 1;

out_of_memory:
   tcirSetDiagnostic(
      diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, method->identity, 0, "cannot allocate frontend CFG maps");
   tcirFrontendBlocksDestroy(blocks);
   return 0;
}

static TCIRValue *tcirFrontendAppendOperation(
   TCIRBlock *block,
   TCIROperation opcode,
   TCIRType result_type,
   const TCIRValue *const *operands,
   size_t operand_count,
   int immediate,
   TCIRHomeBank home_bank,
   unsigned int home_index,
   TCIRSourceLocation source,
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
   spec.home_bank = home_bank;
   spec.home_index = home_index;
   spec.source = source;
   if (tcirBlockAppendOperation(block, &spec, &result, diagnostic) != TCIR_STATUS_OK)
      return NULL;
   return result;
}

static TCIRValue *tcirFrontendAppendConst(
   TCIRBlock *block,
   int value,
   TCIRSourceLocation source,
   TCIRDiagnostic *diagnostic)
{
   return tcirFrontendAppendOperation(
      block, TCIR_OP_CONST_I32, TCIR_TYPE_I32, NULL, 0, value, TCIR_HOME_I32, 0, source, diagnostic);
}

static int tcirFrontendSetTerminator(
   TCIRBlock *block,
   TCIRTerminatorKind kind,
   const TCIRValue *value,
   const TCIREdge *edges,
   size_t edge_count,
   TCIRSourceLocation source,
   TCIRDiagnostic *diagnostic)
{
   TCIRTerminatorSpec spec;
   memset(&spec, 0, sizeof(spec));
   spec.kind = kind;
   spec.value = value;
   spec.edges = edges;
   spec.edge_count = edge_count;
   spec.source = source;
   return tcirBlockSetTerminator(block, &spec, diagnostic) == TCIR_STATUS_OK;
}

static int tcirFrontendSetBranch(
   TCIRBlock *block,
   TCIRBlock *target,
   const TCIRValue *const *state,
   size_t state_count,
   TCIRSourceLocation source,
   TCIRDiagnostic *diagnostic)
{
   TCIREdge edge;
   memset(&edge, 0, sizeof(edge));
   edge.target = target;
   edge.arguments = state;
   edge.argument_count = state_count;
   return tcirFrontendSetTerminator(
      block, TCIR_TERMINATOR_BRANCH, NULL, &edge, 1, source, diagnostic);
}

static int tcirFrontendSetConditionalBranch(
   TCIRBlock *block,
   const TCIRValue *condition,
   TCIRBlock *true_target,
   TCIRBlock *false_target,
   const TCIRValue *const *state,
   size_t state_count,
   TCIRSourceLocation source,
   TCIRDiagnostic *diagnostic)
{
   TCIREdge edges[2];
   memset(edges, 0, sizeof(edges));
   edges[0].target = true_target;
   edges[0].arguments = state;
   edges[0].argument_count = state_count;
   edges[1].target = false_target;
   edges[1].arguments = state;
   edges[1].argument_count = state_count;
   return tcirFrontendSetTerminator(
      block, TCIR_TERMINATOR_BRANCH_IF, condition, edges, 2, source, diagnostic);
}

static TCIRBlock *tcirFrontendBlockAtPC(
   const TCIRMethodView *method,
   const TCIRFrontendBlocks *blocks,
   unsigned int pc)
{
   size_t index;
   if ((size_t)pc >= method->code_slot_count)
      return NULL;
   index = blocks->block_indexes[pc];
   return index == (size_t)-1 ? NULL : blocks->blocks[index];
}

static int tcirFrontendCreateBlocks(
   TCIRFunction *function,
   const TCIRMethodView *method,
   TCIRFrontendBlocks *blocks,
   TCIRDiagnostic *diagnostic)
{
   size_t pc;
   size_t block_index = 0;
   size_t home;
   for (pc = 0; pc < method->code_slot_count; pc++)
   {
      TCIRBlock *block;
      if (!blocks->leaders[pc])
         continue;
      block = tcirFunctionAppendBlock(
         function,
         (unsigned int)block_index,
         tcirFrontendSource(method, (unsigned int)pc),
         0,
         diagnostic);
      if (block == NULL)
         return 0;
      blocks->blocks[block_index] = block;
      if (block_index != 0)
      {
         for (home = 0; home < method->i32_home_count; home++)
            if (tcirBlockAppendArgument(block, TCIR_TYPE_I32, diagnostic) == NULL)
               return 0;
      }
      block_index++;
   }
   return 1;
}

static int tcirFrontendSeedEntryState(
   TCIRFunction *function,
   const TCIRMethodView *method,
   const TCIRValue **state)
{
   size_t parameter;
   for (parameter = 0; parameter < method->parameter_count; parameter++)
      state[method->parameters[parameter].home_index] = tcirFunctionParameter(function, parameter);
   return 1;
}

static int tcirFrontendSeedBlockState(
   TCIRBlock *block,
   size_t home_count,
   const TCIRValue **state)
{
   size_t home;
   for (home = 0; home < home_count; home++)
      state[home] = tcirBlockArgumentAt(block, home);
   return 1;
}

static TCIRValue *tcirFrontendAppendBinary(
   TCIRBlock *block,
   TCIROperation operation,
   const TCIRValue *left,
   const TCIRValue *right,
   TCIRType result_type,
   TCIRSourceLocation source,
   TCIRDiagnostic *diagnostic)
{
   const TCIRValue *operands[2];
   operands[0] = left;
   operands[1] = right;
   return tcirFrontendAppendOperation(
      block, operation, result_type, operands, 2, 0, TCIR_HOME_I32, 0, source, diagnostic);
}

static int tcirFrontendTranslateMove(
   TCIRBlock *block,
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   const TCIRValue **state,
   TCIRDiagnostic *diagnostic)
{
   const TCIRValue *operand[1];
   TCIRSourceLocation source = tcirFrontendSource(method, instruction->pc);
   switch (instruction->info->value)
   {
      case MOV_regI_regI:
         operand[0] = state[instruction->reg1];
         state[instruction->reg0] = tcirFrontendAppendOperation(
            block, TCIR_OP_COPY, TCIR_TYPE_I32, operand, 1, 0, TCIR_HOME_I32, 0, source, diagnostic);
         break;
      case MOV_regI_sym:
         state[instruction->reg0] = tcirFrontendAppendConst(
            block, method->i32_constants[instruction->symbol], source, diagnostic);
         break;
      case MOV_regI_s18:
         state[instruction->reg0] = tcirFrontendAppendConst(
            block, instruction->immediate, source, diagnostic);
         break;
      default:
         return 0;
   }
   return state[instruction->reg0] != NULL;
}

static int tcirFrontendTranslateArithmetic(
   TCIRBlock *block,
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   const TCIRValue **state,
   TCIRDiagnostic *diagnostic)
{
   unsigned int opcode = instruction->info->value;
   TCIROperation operation;
   const TCIRValue *left;
   const TCIRValue *right;
   TCIRValue *constant = NULL;
   TCIRSourceLocation source = tcirFrontendSource(method, instruction->pc);

   if (opcode == INC_regI)
   {
      constant = tcirFrontendAppendConst(block, instruction->immediate, source, diagnostic);
      if (constant == NULL)
         return 0;
      state[instruction->reg0] = tcirFrontendAppendBinary(
         block, TCIR_OP_ADD_I32, state[instruction->reg0], constant, TCIR_TYPE_I32, source, diagnostic);
      return state[instruction->reg0] != NULL;
   }

   operation = (opcode == SUB_regI_s12_regI || opcode == SUB_regI_regI_regI) ?
      TCIR_OP_SUB_I32 :
      ((opcode == MUL_regI_regI_s12 || opcode == MUL_regI_regI_regI) ? TCIR_OP_MUL_I32 : TCIR_OP_ADD_I32);
   if (opcode == ADD_regI_regI_regI || opcode == SUB_regI_regI_regI || opcode == MUL_regI_regI_regI)
   {
      left = state[instruction->reg1];
      right = state[instruction->reg2];
   }
   else
   {
      int value = opcode == ADD_regI_regI_sym ?
         method->i32_constants[instruction->symbol] : instruction->immediate;
      constant = tcirFrontendAppendConst(block, value, source, diagnostic);
      if (constant == NULL)
         return 0;
      if (opcode == SUB_regI_s12_regI)
      {
         left = constant;
         right = state[instruction->reg1];
      }
      else
      {
         left = state[instruction->reg1];
         right = constant;
      }
   }
   state[instruction->reg0] = tcirFrontendAppendBinary(
      block, operation, left, right, TCIR_TYPE_I32, source, diagnostic);
   return state[instruction->reg0] != NULL;
}

static TCIRValue *tcirFrontendComparisonRight(
   TCIRBlock *block,
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   const TCIRValue *const *state,
   TCIRDiagnostic *diagnostic)
{
   unsigned int opcode = instruction->info->value;
   if (opcode == JEQ_regI_regI || opcode == JNE_regI_regI || opcode == JLT_regI_regI ||
       opcode == JLE_regI_regI || opcode == JGT_regI_regI || opcode == JGE_regI_regI)
      return (TCIRValue *)state[instruction->reg1];
   if (opcode == JEQ_regI_sym || opcode == JNE_regI_sym)
      return tcirFrontendAppendConst(
         block,
         method->i32_constants[instruction->symbol],
         tcirFrontendSource(method, instruction->pc),
         diagnostic);
   return tcirFrontendAppendConst(
      block, instruction->immediate, tcirFrontendSource(method, instruction->pc), diagnostic);
}

static int tcirFrontendTranslateConditional(
   TCIRBlock *block,
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   const TCIRFrontendBlocks *blocks,
   const TCIRValue **state,
   TCIRDiagnostic *diagnostic)
{
   unsigned int opcode = instruction->info->value;
   TCIROperation comparison = TCIR_OP_CMP_EQ_I32;
   const TCIRValue *left;
   TCIRValue *right;
   TCIRValue *condition;
   TCIRBlock *true_target;
   TCIRBlock *false_target;
   TCIRSourceLocation source = tcirFrontendSource(method, instruction->pc);

   if (opcode == DECJGTZ_regI || opcode == DECJGEZ_regI)
   {
      TCIRValue *one = tcirFrontendAppendConst(block, 1, source, diagnostic);
      if (one == NULL)
         return 0;
      state[instruction->reg0] = tcirFrontendAppendBinary(
         block, TCIR_OP_SUB_I32, state[instruction->reg0], one, TCIR_TYPE_I32, source, diagnostic);
      if (state[instruction->reg0] == NULL)
         return 0;
      left = state[instruction->reg0];
      right = tcirFrontendAppendConst(block, 0, source, diagnostic);
      comparison = opcode == DECJGTZ_regI ? TCIR_OP_CMP_GT_I32 : TCIR_OP_CMP_GE_I32;
   }
   else
   {
      left = state[instruction->reg0];
      right = tcirFrontendComparisonRight(block, method, instruction, state, diagnostic);
      if (opcode == JLT_regI_regI || opcode == JLT_regI_s6)
         comparison = TCIR_OP_CMP_LT_I32;
      else if (opcode == JLE_regI_regI || opcode == JLE_regI_s6)
         comparison = TCIR_OP_CMP_LE_I32;
      else if (opcode == JGT_regI_regI || opcode == JGT_regI_s6)
         comparison = TCIR_OP_CMP_GT_I32;
      else if (opcode == JGE_regI_regI || opcode == JGE_regI_s6)
         comparison = TCIR_OP_CMP_GE_I32;
   }
   if (right == NULL)
      return 0;
   condition = tcirFrontendAppendBinary(
      block, comparison, left, right, TCIR_TYPE_I1, source, diagnostic);
   if (condition == NULL)
      return 0;
   true_target = tcirFrontendBlockAtPC(method, blocks, (unsigned int)instruction->target);
   false_target = tcirFrontendBlockAtPC(method, blocks, instruction->pc + instruction->width);
   if (opcode == JNE_regI_regI || opcode == JNE_regI_s6 || opcode == JNE_regI_sym)
   {
      TCIRBlock *temporary = true_target;
      true_target = false_target;
      false_target = temporary;
   }
   return tcirFrontendSetConditionalBranch(
      block,
      condition,
      true_target,
      false_target,
      state,
      method->i32_home_count,
      source,
      diagnostic);
}

static int tcirFrontendTranslateBlock(
   TCIRFunction *function,
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   const TCIRFrontendBlocks *blocks,
   size_t block_index,
   unsigned int start_pc,
   unsigned int end_pc,
   TCIRDiagnostic *diagnostic)
{
   TCIRBlock *block = blocks->blocks[block_index];
   const TCIRValue **state;
   size_t instruction_index = decoded->instruction_indexes[start_pc];
   int terminated = 0;

   state = (const TCIRValue **)calloc(method->i32_home_count == 0 ? 1U : method->i32_home_count, sizeof(TCIRValue *));
   if (state == NULL)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         method->identity,
         start_pc,
         "cannot allocate block register state");
      return 0;
   }
   if (block_index == 0)
   {
      if (!tcirFrontendSeedEntryState(function, method, state))
         goto failed;
   }
   else
      tcirFrontendSeedBlockState(block, method->i32_home_count, state);

   while (instruction_index < decoded->instruction_count)
   {
      const TCIRDecodedInstruction *instruction = &decoded->instructions[instruction_index];
      unsigned int opcode = instruction->info->value;
      TCIRSourceLocation source;
      if (instruction->pc >= end_pc)
         break;
      source = tcirFrontendSource(method, instruction->pc);

      if (opcode == BREAK)
      {
         instruction_index++;
         continue;
      }
      if (opcode == MOV_regI_regI || opcode == MOV_regI_sym || opcode == MOV_regI_s18)
      {
         if (!tcirFrontendTranslateMove(block, method, instruction, state, diagnostic))
            goto failed;
      }
      else if (opcode == INC_regI || (opcode >= ADD_regI_regI_regI && opcode <= ADD_regI_regI_sym) ||
               opcode == SUB_regI_s12_regI || opcode == SUB_regI_regI_regI ||
               opcode == MUL_regI_regI_s12 || opcode == MUL_regI_regI_regI)
      {
         if (!tcirFrontendTranslateArithmetic(block, method, instruction, state, diagnostic))
            goto failed;
      }
      else if (tcirFrontendIsConditional(opcode))
      {
         if (!tcirFrontendTranslateConditional(block, method, instruction, blocks, state, diagnostic))
            goto failed;
         terminated = 1;
      }
      else if (opcode == JUMP_s24)
      {
         if (!tcirFrontendSetBranch(
                block,
                tcirFrontendBlockAtPC(method, blocks, (unsigned int)instruction->target),
                state,
                method->i32_home_count,
                source,
                diagnostic))
            goto failed;
         terminated = 1;
      }
      else if (tcirFrontendIsReturn(opcode))
      {
         const TCIRValue *value = NULL;
         if (opcode == RETURN_regI)
            value = state[instruction->reg0];
         else if (opcode == RETURN_s24I)
            value = tcirFrontendAppendConst(block, instruction->immediate, source, diagnostic);
         else if (opcode == RETURN_symI)
            value = tcirFrontendAppendConst(
               block, method->i32_constants[instruction->symbol], source, diagnostic);
         if (opcode != RETURN_void && value == NULL)
            goto failed;
         if (!tcirFrontendSetTerminator(
                block, TCIR_TERMINATOR_RETURN, value, NULL, 0, source, diagnostic))
            goto failed;
         terminated = 1;
      }
      else
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
            method->identity,
            instruction->pc,
            "decoder admitted unsupported TC opcode %u (%s)",
            opcode,
            instruction->info->name);
         goto failed;
      }

      instruction_index++;
      if (terminated)
         break;
   }

   if (!terminated)
   {
      TCIRBlock *next = tcirFrontendBlockAtPC(method, blocks, end_pc);
      unsigned int source_pc = end_pc == 0 ? 0 : end_pc - 1U;
      if (next == NULL || !tcirFrontendSetBranch(
             block,
             next,
             state,
             method->i32_home_count,
             tcirFrontendSource(method, source_pc),
             diagnostic))
      {
         if (next == NULL)
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_INVALID_TARGET,
               method->identity,
               source_pc,
               "control falls through past the final code slot");
         goto failed;
      }
   }

   free(state);
   return 1;

failed:
   free(state);
   return 0;
}

static int tcirFrontendValidateSignature(const TCIRMethodView *method, TCIRDiagnostic *diagnostic)
{
   size_t index;
   if (method->return_type != TCIR_TYPE_VOID && method->return_type != TCIR_TYPE_I32)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_TYPE_MERGE,
         method->identity,
         0,
         "POC frontend does not accept return type %s",
         tcirTypeName(method->return_type));
      return 0;
   }
   for (index = 0; index < method->parameter_count; index++)
      if (method->parameters[index].type != TCIR_TYPE_I32)
         return 0;
   return 1;
}

TCIRFrontendResult tcirFrontendBuildFunction(
   TCIRModule *module,
   const TCIRMethodView *method,
   TCIRFunction **function,
   TCIRDiagnostic *diagnostic)
{
   TCIRDecodedMethod decoded;
   TCIRFrontendBlocks blocks;
   TCIRFrontendResult result;
   TCIRType *parameter_types = NULL;
   TCIRFunction *created = NULL;
   size_t index;
   size_t block_index;
   unsigned int block_start;

   tcirDiagnosticClear(diagnostic);
   if (function != NULL)
      *function = NULL;
   if (module == NULL || method == NULL || function == NULL)
   {
      tcirSetDiagnostic(
         diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<frontend>", 0, "frontend argument is null");
      return TCIR_FRONTEND_ERROR;
   }

   result = tcirDecodeMethod(method, &decoded, diagnostic);
   if (result != TCIR_FRONTEND_OK)
      return result;
   if (method->handler_count != 0)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
         method->identity,
         method->handlers[0].handler_pc,
         "exception handlers are not in the Milestone 3 POC; keep method interpreter-eligible");
      tcirDecodedMethodDestroy(&decoded);
      return TCIR_FRONTEND_FALLBACK;
   }
   if (!tcirFrontendValidateSignature(method, diagnostic))
   {
      if (diagnostic != NULL && diagnostic->code == TCIR_DIAGNOSTIC_NONE)
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_TYPE_MERGE,
            method->identity,
            0,
            "POC frontend accepts only i32 parameters");
      tcirDecodedMethodDestroy(&decoded);
      return TCIR_FRONTEND_ERROR;
   }
   if (!tcirFrontendDiscoverBlocks(method, &decoded, &blocks, diagnostic))
   {
      tcirDecodedMethodDestroy(&decoded);
      return TCIR_FRONTEND_ERROR;
   }

   if (method->parameter_count != 0)
   {
      parameter_types = (TCIRType *)malloc(method->parameter_count * sizeof(TCIRType));
      if (parameter_types == NULL)
      {
         tcirSetDiagnostic(
            diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, method->identity, 0, "cannot allocate parameter types");
         goto failed;
      }
      for (index = 0; index < method->parameter_count; index++)
         parameter_types[index] = method->parameters[index].type;
   }
   created = tcirModuleAddFunction(
      module,
      method->identity,
      parameter_types,
      method->parameter_count,
      method->return_type,
      diagnostic);
   free(parameter_types);
   parameter_types = NULL;
   if (created == NULL ||
       tcirFunctionSetHomes(
          created,
          method->i32_home_count,
          method->ref_home_count,
          method->v64_home_count,
          diagnostic) != TCIR_STATUS_OK ||
       tcirFunctionSetSourceSlots(
          created, method->code_slot_count, decoded.instruction_starts, diagnostic) != TCIR_STATUS_OK ||
       !tcirFrontendCreateBlocks(created, method, &blocks, diagnostic))
      goto failed;

   block_index = 0;
   block_start = 0;
   for (index = 1; index <= method->code_slot_count; index++)
   {
      if (index == method->code_slot_count || blocks.leaders[index])
      {
         if (!tcirFrontendTranslateBlock(
                created,
                method,
                &decoded,
                &blocks,
                block_index,
                block_start,
                (unsigned int)index,
                diagnostic))
            goto failed;
         block_start = (unsigned int)index;
         block_index++;
      }
   }
   if (!tcirVerifyFunction(created, diagnostic))
      goto failed;

   *function = created;
   tcirFrontendBlocksDestroy(&blocks);
   tcirDecodedMethodDestroy(&decoded);
   return TCIR_FRONTEND_OK;

failed:
   free(parameter_types);
   tcirFrontendBlocksDestroy(&blocks);
   tcirDecodedMethodDestroy(&decoded);
   return TCIR_FRONTEND_ERROR;
}
