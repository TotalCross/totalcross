// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_internal.h"

#include <string.h>

static int tcirIsReference(TCIRType type)
{
   return type == TCIR_TYPE_REF || type == TCIR_TYPE_NON_NULL_REF;
}

static int tcirBlockBelongsToFunction(const TCIRBlock *block, const TCIRFunction *function)
{
   size_t index;

   if (block == NULL || function == NULL || block->function != function)
      return 0;
   for (index = 0; index < function->block_count; index++)
      if (function->blocks[index] == block)
         return 1;
   return 0;
}

static int tcirVerifySource(
   const TCIRFunction *function,
   TCIRSourceLocation source,
   const char *item,
   TCIRDiagnostic *diagnostic)
{
   if (tcirSourceIsInstructionStart(function, source))
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_SOURCE_TARGET,
      function->identity,
      source.tc_pc,
      "%s points outside the source method or into a continuation slot",
      item);
   return 0;
}

static int tcirValueAvailable(
   const TCIRValue *value,
   const TCIRFunction *function,
   const TCIRBlock *block,
   size_t operation_index,
   unsigned int tc_pc,
   TCIRDiagnostic *diagnostic)
{
   if (!tcirValueBelongsToFunction(value, function))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
         function->identity,
         tc_pc,
         "an operand is null or belongs to another function");
      return 0;
   }

   if (value->definition_kind == TCIR_VALUE_PARAMETER)
   {
      if (function->block_count != 0 && block != function->blocks[0])
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
            function->identity,
            tc_pc,
            "parameter %%v%u must be passed through a block argument outside the entry block",
            value->id);
         return 0;
      }
      return 1;
   }

   if (value->block != block)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
         function->identity,
         tc_pc,
         "value %%v%u is not defined in bb%u",
         value->id,
         block->id);
      return 0;
   }

   if (value->definition_kind == TCIR_VALUE_BLOCK_ARGUMENT)
      return 1;

   if (value->definition_kind != TCIR_VALUE_OPERATION || value->definition_index >= operation_index)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_VALUE_ORDER,
         function->identity,
         tc_pc,
         "value %%v%u is used before its definition",
         value->id);
      return 0;
   }
   return 1;
}

static int tcirRequireOperandCount(
   const TCIRFunction *function,
   const TCIROperationData *operation,
   size_t expected,
   TCIRDiagnostic *diagnostic)
{
   if (operation->operand_count == expected)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_OPERAND_COUNT,
      function->identity,
      operation->source.tc_pc,
      "%s expects %u operands but has %u",
      tcirOperationName(operation->opcode),
      (unsigned int)expected,
      (unsigned int)operation->operand_count);
   return 0;
}

static int tcirRequireOperandType(
   const TCIRFunction *function,
   const TCIROperationData *operation,
   size_t index,
   TCIRType expected,
   TCIRDiagnostic *diagnostic)
{
   TCIRType actual;

   if (index >= operation->operand_count)
      return tcirRequireOperandCount(function, operation, index + 1, diagnostic);
   actual = operation->operands[index]->type;
   if (actual == expected)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_OPERAND_TYPE,
      function->identity,
      operation->source.tc_pc,
      "%s operand %u must be %s but is %s",
      tcirOperationName(operation->opcode),
      (unsigned int)index,
      tcirTypeName(expected),
      tcirTypeName(actual));
   return 0;
}

static int tcirRequireReferenceOperand(
   const TCIRFunction *function,
   const TCIROperationData *operation,
   size_t index,
   TCIRDiagnostic *diagnostic)
{
   if (index < operation->operand_count && tcirIsReference(operation->operands[index]->type))
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_OPERAND_TYPE,
      function->identity,
      operation->source.tc_pc,
      "%s operand %u must be a managed reference",
      tcirOperationName(operation->opcode),
      (unsigned int)index);
   return 0;
}

static int tcirRequireResultType(
   const TCIRFunction *function,
   const TCIROperationData *operation,
   TCIRType expected,
   TCIRDiagnostic *diagnostic)
{
   if (operation->result_type == expected &&
       ((expected == TCIR_TYPE_VOID && operation->result == NULL) ||
        (expected != TCIR_TYPE_VOID && operation->result != NULL && operation->result->type == expected)))
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_RESULT_TYPE,
      function->identity,
      operation->source.tc_pc,
      "%s must produce %s but produces %s",
      tcirOperationName(operation->opcode),
      tcirTypeName(expected),
      tcirTypeName(operation->result_type));
   return 0;
}

static int tcirVerifyThrowingOperation(
   const TCIRFunction *function,
   const TCIROperationData *operation,
   TCIRDiagnostic *diagnostic)
{
   int may_throw = (operation->effects & TCIR_EFFECT_MAY_THROW) != 0;
   int has_target = operation->exception_target != NULL;

   if (!may_throw && !has_target && !operation->propagates_exception)
      return 1;
   if (!may_throw || has_target == operation->propagates_exception)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_HELPER_EFFECTS,
         function->identity,
         operation->source.tc_pc,
         "%s must declare may_throw and exactly one exception destination",
         tcirOperationName(operation->opcode));
      return 0;
   }
   if (has_target &&
       (!tcirBlockBelongsToFunction(operation->exception_target, function) ||
        !operation->exception_target->is_exception_handler))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_HANDLER_SIGNATURE,
         function->identity,
         operation->source.tc_pc,
         "%s has an invalid exception-handler edge",
         tcirOperationName(operation->opcode));
      return 0;
   }
   return 1;
}

static int tcirValueUsedAfter(
   const TCIRValue *value,
   const TCIRBlock *block,
   size_t operation_index)
{
   size_t index;
   size_t operand_index;

   for (index = operation_index + 1; index < block->operation_count; index++)
      for (operand_index = 0; operand_index < block->operations[index].operand_count; operand_index++)
         if (block->operations[index].operands[operand_index] == value)
            return 1;

   if (!block->has_terminator)
      return 0;
   if (block->terminator.value == value)
      return 1;
   for (index = 0; index < block->terminator.edge_count; index++)
      for (operand_index = 0; operand_index < block->terminator.edges[index].argument_count; operand_index++)
         if (block->terminator.edges[index].arguments[operand_index] == value)
            return 1;
   return 0;
}

static int tcirHasGCHome(const TCIROperationData *operation, const TCIRValue *value)
{
   size_t index;

   for (index = 0; index < operation->gc_home_count; index++)
      if (operation->gc_homes[index].value == value)
         return 1;
   return 0;
}

static int tcirVerifyGC(
   const TCIRFunction *function,
   const TCIRBlock *block,
   size_t operation_index,
   const TCIROperationData *operation,
   TCIRDiagnostic *diagnostic)
{
   size_t index;
   size_t other;

   if ((operation->effects & TCIR_EFFECT_MAY_GC) == 0)
      return 1;

   for (index = 0; index < operation->gc_home_count; index++)
   {
      const TCIRGCHome *home = &operation->gc_homes[index];
      if (!tcirValueAvailable(
             home->value,
             function,
             block,
             operation_index,
             operation->source.tc_pc,
             diagnostic))
         return 0;
      if (!tcirIsReference(home->value->type) || home->home_index >= function->home_counts[TCIR_HOME_REF])
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_GC_HOME,
            function->identity,
            operation->source.tc_pc,
            "GC home %u does not map a managed reference to a valid ref slot",
            (unsigned int)index);
         return 0;
      }
      for (other = index + 1; other < operation->gc_home_count; other++)
      {
         if (operation->gc_homes[other].home_index == home->home_index &&
             operation->gc_homes[other].value != home->value)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_GC_HOME,
               function->identity,
               operation->source.tc_pc,
               "ref home %u is assigned to multiple live values",
               home->home_index);
            return 0;
         }
      }
   }

   for (index = 0; index < function->value_count; index++)
   {
      const TCIRValue *value = function->values[index];
      int defined_before = value->definition_kind != TCIR_VALUE_OPERATION ||
                           (value->block == block && value->definition_index < operation_index);
      int available_here = value->definition_kind == TCIR_VALUE_PARAMETER
                              ? block == function->blocks[0]
                              : value->block == block;
      if (!defined_before || !available_here || !tcirValueUsedAfter(value, block, operation_index))
         continue;
      if (value->type == TCIR_TYPE_INTERNAL_ADDRESS)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_INTERNAL_ADDRESS_LIFETIME,
            function->identity,
            operation->source.tc_pc,
            "internal address %%v%u is live across a may_gc operation",
            value->id);
         return 0;
      }
      if (tcirIsReference(value->type) && !tcirHasGCHome(operation, value))
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_GC_HOME,
            function->identity,
            operation->source.tc_pc,
            "live reference %%v%u has no visible Context.regO home",
            value->id);
         return 0;
      }
   }
   return 1;
}

static int tcirVerifyUncheckedArrayProof(
   const TCIRFunction *function,
   const TCIRBlock *block,
   size_t operation_index,
   const TCIROperationData *operation,
   size_t proof_index,
   TCIRDiagnostic *diagnostic)
{
   const TCIRValue *proof;
   const TCIROperationData *definition;

   if (proof_index >= operation->operand_count)
      return tcirRequireOperandCount(function, operation, proof_index + 1, diagnostic);
   proof = operation->operands[proof_index];
   if (proof->type != TCIR_TYPE_TOKEN || proof->definition_kind != TCIR_VALUE_OPERATION ||
       proof->block != block || proof->definition_index >= operation_index)
      goto invalid_proof;

   definition = &block->operations[proof->definition_index];
   if (definition->opcode != TCIR_OP_BOUNDS_CHECK || definition->operand_count != 2 ||
       definition->operands[0] != operation->operands[0] ||
       definition->operands[1] != operation->operands[1])
      goto invalid_proof;
   return 1;

invalid_proof:
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_UNCHECKED_ARRAY_PROOF,
      function->identity,
      operation->source.tc_pc,
      "%s lacks a dominating bounds-check proof for the same array and index",
      tcirOperationName(operation->opcode));
   return 0;
}

static int tcirVerifyOperation(
   const TCIRFunction *function,
   const TCIRBlock *block,
   size_t operation_index,
   TCIRDiagnostic *diagnostic)
{
   const TCIROperationData *operation = &block->operations[operation_index];
   size_t operand_index;
   unsigned int bank_count;

   if (!tcirVerifySource(function, operation->source, tcirOperationName(operation->opcode), diagnostic))
      return 0;
   if ((unsigned int)operation->opcode > (unsigned int)TCIR_OP_INTERNAL_ADDRESS)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         function->identity,
         operation->source.tc_pc,
         "unknown operation %u",
         (unsigned int)operation->opcode);
      return 0;
   }
   if ((operation->effects & ~(TCIR_EFFECT_READS_HEAP | TCIR_EFFECT_WRITES_HEAP |
                              TCIR_EFFECT_MAY_THROW | TCIR_EFFECT_MAY_GC |
                              TCIR_EFFECT_MAY_LOCK | TCIR_EFFECT_RESOLVES_SYMBOL |
                              TCIR_EFFECT_CALLS_UNKNOWN)) != 0)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_HELPER_EFFECTS,
         function->identity,
         operation->source.tc_pc,
         "%s declares unknown effect bits 0x%x",
         tcirOperationName(operation->opcode),
         operation->effects);
      return 0;
   }
   for (operand_index = 0; operand_index < operation->operand_count; operand_index++)
      if (!tcirValueAvailable(
             operation->operands[operand_index],
             function,
             block,
             operation_index,
             operation->source.tc_pc,
             diagnostic))
         return 0;

   if (operation->result_type == TCIR_TYPE_VOID)
   {
      if (operation->result != NULL)
         return tcirRequireResultType(function, operation, TCIR_TYPE_VOID, diagnostic);
   }
   else if (operation->result == NULL || operation->result->type != operation->result_type ||
            operation->result->function != function || operation->result->block != block)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_RESULT_TYPE,
         function->identity,
         operation->source.tc_pc,
         "%s has an invalid result value",
         tcirOperationName(operation->opcode));
      return 0;
   }

   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
         if (!tcirRequireOperandCount(function, operation, 0, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_I32, diagnostic))
            return 0;
         break;

      case TCIR_OP_COPY:
         if (!tcirRequireOperandCount(function, operation, 1, diagnostic))
            return 0;
         if (operation->result_type == TCIR_TYPE_VOID || operation->result_type != operation->operands[0]->type)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_RESULT_TYPE,
               function->identity,
               operation->source.tc_pc,
               "copy result type must match its operand");
            return 0;
         }
         break;

      case TCIR_OP_ADD_I32:
      case TCIR_OP_SUB_I32:
      case TCIR_OP_MUL_I32:
         if (!tcirRequireOperandCount(function, operation, 2, diagnostic) ||
             !tcirRequireOperandType(function, operation, 0, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireOperandType(function, operation, 1, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_I32, diagnostic))
            return 0;
         break;

      case TCIR_OP_CMP_EQ_I32:
      case TCIR_OP_CMP_LT_I32:
      case TCIR_OP_CMP_LE_I32:
      case TCIR_OP_CMP_GT_I32:
      case TCIR_OP_CMP_GE_I32:
         if (!tcirRequireOperandCount(function, operation, 2, diagnostic) ||
             !tcirRequireOperandType(function, operation, 0, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireOperandType(function, operation, 1, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_I1, diagnostic))
            return 0;
         break;

      case TCIR_OP_LOAD_SLOT:
      case TCIR_OP_STORE_SLOT:
         if ((unsigned int)operation->home_bank > (unsigned int)TCIR_HOME_V64)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_OPERAND_TYPE,
               function->identity,
               operation->source.tc_pc,
               "slot operation uses an invalid home bank");
            return 0;
         }
         bank_count = function->home_counts[operation->home_bank];
         if (operation->home_index >= bank_count)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_OPERAND_TYPE,
               function->identity,
               operation->source.tc_pc,
               "slot %u is outside its home bank",
               operation->home_index);
            return 0;
         }
         if (operation->opcode == TCIR_OP_LOAD_SLOT)
         {
            TCIRType type = operation->result_type;
            if (!tcirRequireOperandCount(function, operation, 0, diagnostic) ||
                (operation->home_bank == TCIR_HOME_I32 && type != TCIR_TYPE_I32) ||
                (operation->home_bank == TCIR_HOME_REF && !tcirIsReference(type)) ||
                (operation->home_bank == TCIR_HOME_V64 && type != TCIR_TYPE_I64 && type != TCIR_TYPE_F64))
            {
               tcirSetDiagnostic(
                  diagnostic,
                  TCIR_DIAGNOSTIC_RESULT_TYPE,
                  function->identity,
                  operation->source.tc_pc,
                  "load.slot result does not match its home bank");
               return 0;
            }
         }
         else
         {
            TCIRType type;
            if (!tcirRequireOperandCount(function, operation, 1, diagnostic) ||
                !tcirRequireResultType(function, operation, TCIR_TYPE_VOID, diagnostic))
               return 0;
            type = operation->operands[0]->type;
            if ((operation->home_bank == TCIR_HOME_I32 && type != TCIR_TYPE_I32) ||
                (operation->home_bank == TCIR_HOME_REF && !tcirIsReference(type)) ||
                (operation->home_bank == TCIR_HOME_V64 && type != TCIR_TYPE_I64 && type != TCIR_TYPE_F64))
            {
               tcirSetDiagnostic(
                  diagnostic,
                  TCIR_DIAGNOSTIC_OPERAND_TYPE,
                  function->identity,
                  operation->source.tc_pc,
                  "store.slot operand does not match its home bank");
               return 0;
            }
         }
         break;

      case TCIR_OP_NULL_CHECK:
         if (!tcirRequireOperandCount(function, operation, 1, diagnostic) ||
             !tcirRequireReferenceOperand(function, operation, 0, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_NON_NULL_REF, diagnostic))
            return 0;
         if ((operation->effects & TCIR_EFFECT_MAY_THROW) == 0)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_HELPER_EFFECTS,
               function->identity,
               operation->source.tc_pc,
               "null.check must declare may_throw");
            return 0;
         }
         break;

      case TCIR_OP_BOUNDS_CHECK:
         if (!tcirRequireOperandCount(function, operation, 2, diagnostic) ||
             !tcirRequireReferenceOperand(function, operation, 0, diagnostic) ||
             !tcirRequireOperandType(function, operation, 1, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_TOKEN, diagnostic))
            return 0;
         if ((operation->effects & TCIR_EFFECT_MAY_THROW) == 0)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_HELPER_EFFECTS,
               function->identity,
               operation->source.tc_pc,
               "bounds.check must declare may_throw");
            return 0;
         }
         break;

      case TCIR_OP_ARRAY_LOAD_UNCHECKED:
         if (!tcirRequireOperandCount(function, operation, 3, diagnostic) ||
             !tcirRequireReferenceOperand(function, operation, 0, diagnostic) ||
             !tcirRequireOperandType(function, operation, 1, TCIR_TYPE_I32, diagnostic) ||
             operation->result_type == TCIR_TYPE_VOID ||
             !tcirVerifyUncheckedArrayProof(function, block, operation_index, operation, 2, diagnostic))
            return 0;
         break;

      case TCIR_OP_ARRAY_STORE_UNCHECKED:
         if (!tcirRequireOperandCount(function, operation, 4, diagnostic) ||
             !tcirRequireReferenceOperand(function, operation, 0, diagnostic) ||
             !tcirRequireOperandType(function, operation, 1, TCIR_TYPE_I32, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_VOID, diagnostic) ||
             !tcirVerifyUncheckedArrayProof(function, block, operation_index, operation, 3, diagnostic))
            return 0;
         break;

      case TCIR_OP_FIELD_LOAD:
      case TCIR_OP_FIELD_STORE:
         if (operation->symbol == NULL || operation->symbol->module != function->module ||
             operation->symbol->kind != TCIR_SYMBOL_FIELD)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_SYMBOL_KIND,
               function->identity,
               operation->source.tc_pc,
               "%s requires a field symbol from the same module",
               tcirOperationName(operation->opcode));
            return 0;
         }
         if (operation->opcode == TCIR_OP_FIELD_LOAD)
         {
            if (operation->operand_count != 0 && operation->operand_count != 1)
            {
               tcirSetDiagnostic(
                  diagnostic,
                  TCIR_DIAGNOSTIC_OPERAND_COUNT,
                  function->identity,
                  operation->source.tc_pc,
                  "field.load expects zero or one operand but has %u",
                  (unsigned int)operation->operand_count);
               return 0;
            }
            if (operation->operand_count == 1 &&
                !tcirRequireReferenceOperand(function, operation, 0, diagnostic))
               return 0;
            if (operation->result_type == TCIR_TYPE_VOID)
            {
               tcirSetDiagnostic(
                  diagnostic,
                  TCIR_DIAGNOSTIC_RESULT_TYPE,
                  function->identity,
                  operation->source.tc_pc,
                  "field.load must produce a value");
               return 0;
            }
         }
         else
         {
            if (operation->operand_count != 1 && operation->operand_count != 2)
            {
               tcirSetDiagnostic(
                  diagnostic,
                  TCIR_DIAGNOSTIC_OPERAND_COUNT,
                  function->identity,
                  operation->source.tc_pc,
                  "field.store expects one or two operands but has %u",
                  (unsigned int)operation->operand_count);
               return 0;
            }
            if (operation->operand_count == 2 &&
                !tcirRequireReferenceOperand(function, operation, 0, diagnostic))
               return 0;
            if (!tcirRequireResultType(function, operation, TCIR_TYPE_VOID, diagnostic))
               return 0;
         }
         break;

      case TCIR_OP_RUNTIME_CALL:
         if (operation->symbol == NULL || operation->symbol->module != function->module ||
             operation->symbol->kind != TCIR_SYMBOL_HELPER)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_SYMBOL_KIND,
               function->identity,
               operation->source.tc_pc,
               "runtime.call requires a helper symbol from the same module");
            return 0;
         }
         if (operation->effects != operation->symbol->helper_effects)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_HELPER_EFFECTS,
               function->identity,
               operation->source.tc_pc,
               "runtime.call effects 0x%x do not match helper effects 0x%x",
               operation->effects,
               operation->symbol->helper_effects);
            return 0;
         }
         break;

      case TCIR_OP_INTERNAL_ADDRESS:
         if (!tcirRequireOperandCount(function, operation, 1, diagnostic) ||
             !tcirRequireReferenceOperand(function, operation, 0, diagnostic) ||
             !tcirRequireResultType(function, operation, TCIR_TYPE_INTERNAL_ADDRESS, diagnostic))
            return 0;
         break;
   }

   if (!tcirVerifyThrowingOperation(function, operation, diagnostic))
      return 0;
   return tcirVerifyGC(function, block, operation_index, operation, diagnostic);
}

static int tcirVerifyEdge(
   const TCIRFunction *function,
   const TCIRBlock *source,
   const TCIREdge *edge,
   size_t edge_index,
   TCIRDiagnostic *diagnostic)
{
   size_t argument_index;

   if (!tcirBlockBelongsToFunction(edge->target, function))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_MISSING_BLOCK,
         function->identity,
         source->terminator.source.tc_pc,
         "edge %u of bb%u targets a block outside the function",
         (unsigned int)edge_index,
         source->id);
      return 0;
   }
   if (edge->argument_count != edge->target->argument_count)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_BLOCK_ARGUMENT_COUNT,
         function->identity,
         source->terminator.source.tc_pc,
         "edge from bb%u to bb%u passes %u arguments but the target expects %u",
         source->id,
         edge->target->id,
         (unsigned int)edge->argument_count,
         (unsigned int)edge->target->argument_count);
      return 0;
   }
   for (argument_index = 0; argument_index < edge->argument_count; argument_index++)
   {
      const TCIRValue *argument = edge->arguments[argument_index];
      if (!tcirValueAvailable(
             argument,
             function,
             source,
             source->operation_count,
             source->terminator.source.tc_pc,
             diagnostic))
         return 0;
      if (argument->type == TCIR_TYPE_INTERNAL_ADDRESS)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_INTERNAL_ADDRESS_LIFETIME,
            function->identity,
            source->terminator.source.tc_pc,
            "internal address %%v%u cannot cross a block edge",
            argument->id);
         return 0;
      }
      if (argument->type != edge->target->arguments[argument_index]->type)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_BLOCK_ARGUMENT_TYPE,
            function->identity,
            source->terminator.source.tc_pc,
            "edge argument %u from bb%u is %s but bb%u expects %s",
            (unsigned int)argument_index,
            source->id,
            tcirTypeName(argument->type),
            edge->target->id,
            tcirTypeName(edge->target->arguments[argument_index]->type));
         return 0;
      }
   }
   return 1;
}

static int tcirVerifyTerminator(
   const TCIRFunction *function,
   const TCIRBlock *block,
   TCIRDiagnostic *diagnostic)
{
   const TCIRTerminatorView *terminator = &block->terminator;
   size_t edge_index;
   size_t other;
   size_t default_count = 0;

   if (!block->has_terminator)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_MISSING_TERMINATOR,
         function->identity,
         block->source.tc_pc,
         "bb%u has no terminator",
         block->id);
      return 0;
   }
   if (!tcirVerifySource(function, terminator->source, tcirTerminatorName(terminator->kind), diagnostic))
      return 0;
   if ((unsigned int)terminator->kind > (unsigned int)TCIR_TERMINATOR_UNREACHABLE)
      goto invalid;
   if (terminator->value != NULL &&
       !tcirValueAvailable(
          terminator->value,
          function,
          block,
          block->operation_count,
          terminator->source.tc_pc,
          diagnostic))
      return 0;
   for (edge_index = 0; edge_index < terminator->edge_count; edge_index++)
      if (!tcirVerifyEdge(function, block, &terminator->edges[edge_index], edge_index, diagnostic))
         return 0;

   switch (terminator->kind)
   {
      case TCIR_TERMINATOR_BRANCH:
         if (terminator->value != NULL || terminator->edge_count != 1 || terminator->edges[0].has_case_value)
            goto invalid;
         break;

      case TCIR_TERMINATOR_BRANCH_IF:
         if (terminator->value == NULL || terminator->value->type != TCIR_TYPE_I1 || terminator->edge_count != 2 ||
             terminator->edges[0].has_case_value || terminator->edges[1].has_case_value)
            goto invalid;
         break;

      case TCIR_TERMINATOR_SWITCH:
         if (terminator->value == NULL || terminator->value->type != TCIR_TYPE_I32 || terminator->edge_count == 0)
            goto invalid;
         for (edge_index = 0; edge_index < terminator->edge_count; edge_index++)
         {
            if (!terminator->edges[edge_index].has_case_value)
               default_count++;
            for (other = edge_index + 1; other < terminator->edge_count; other++)
               if (terminator->edges[edge_index].has_case_value && terminator->edges[other].has_case_value &&
                   terminator->edges[edge_index].case_value == terminator->edges[other].case_value)
                  goto invalid;
         }
         if (default_count != 1)
            goto invalid;
         break;

      case TCIR_TERMINATOR_RETURN:
         if (terminator->edge_count != 0)
            goto invalid;
         if (function->return_type == TCIR_TYPE_VOID)
         {
            if (terminator->value != NULL)
               goto wrong_return;
         }
         else if (terminator->value == NULL || terminator->value->type != function->return_type)
            goto wrong_return;
         if (terminator->value != NULL && terminator->value->type == TCIR_TYPE_INTERNAL_ADDRESS)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_INTERNAL_ADDRESS_LIFETIME,
               function->identity,
               terminator->source.tc_pc,
               "an internal address cannot be returned");
            return 0;
         }
         break;

      case TCIR_TERMINATOR_THROW:
         if (terminator->edge_count != 0 || terminator->value == NULL || !tcirIsReference(terminator->value->type))
            goto invalid;
         break;

      case TCIR_TERMINATOR_UNREACHABLE:
         if (terminator->edge_count != 0 || terminator->value != NULL)
            goto invalid;
         break;
   }
   return 1;

wrong_return:
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_RETURN_TYPE,
      function->identity,
      terminator->source.tc_pc,
      "return value does not match function return type %s",
      tcirTypeName(function->return_type));
   return 0;

invalid:
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_TERMINATOR,
      function->identity,
      terminator->source.tc_pc,
      "bb%u has an invalid %s terminator",
      block->id,
      tcirTerminatorName(terminator->kind));
   return 0;
}

static size_t tcirBlockIndex(const TCIRFunction *function, const TCIRBlock *block)
{
   size_t index;

   for (index = 0; index < function->block_count; index++)
      if (function->blocks[index] == block)
         return index;
   return function->block_count;
}

static int tcirVerifyReachability(const TCIRFunction *function, TCIRDiagnostic *diagnostic)
{
   unsigned char *reachable;
   size_t *worklist;
   size_t read_index = 0;
   size_t write_index = 0;
   size_t block_index;

   reachable = (unsigned char *)tcirAllocate(function->module, function->block_count);
   worklist = (size_t *)tcirAllocate(function->module, function->block_count * sizeof(size_t));
   if (reachable == NULL || worklist == NULL)
   {
      tcirFree(function->module, reachable);
      tcirFree(function->module, worklist);
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         0,
         "unable to allocate verifier reachability state");
      return 0;
   }
   memset(reachable, 0, function->block_count);
   reachable[0] = 1;
   worklist[write_index++] = 0;

   while (read_index < write_index)
   {
      const TCIRBlock *block = function->blocks[worklist[read_index++]];
      size_t edge_index;
      size_t operation_index;

      for (edge_index = 0; edge_index < block->terminator.edge_count; edge_index++)
      {
         block_index = tcirBlockIndex(function, block->terminator.edges[edge_index].target);
         if (block_index < function->block_count && !reachable[block_index])
         {
            reachable[block_index] = 1;
            worklist[write_index++] = block_index;
         }
      }
      for (operation_index = 0; operation_index < block->operation_count; operation_index++)
      {
         block_index = tcirBlockIndex(function, block->operations[operation_index].exception_target);
         if (block_index < function->block_count && !reachable[block_index])
         {
            reachable[block_index] = 1;
            worklist[write_index++] = block_index;
         }
      }
   }

   for (block_index = 0; block_index < function->block_count; block_index++)
   {
      if (!reachable[block_index])
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_UNREACHABLE_BLOCK,
            function->identity,
            function->blocks[block_index]->source.tc_pc,
            "bb%u is unreachable",
            function->blocks[block_index]->id);
         tcirFree(function->module, reachable);
         tcirFree(function->module, worklist);
         return 0;
      }
   }

   tcirFree(function->module, reachable);
   tcirFree(function->module, worklist);
   return 1;
}

int tcirVerifyFunction(const TCIRFunction *function, TCIRDiagnostic *diagnostic)
{
   size_t block_index;
   size_t other;
   size_t operation_index;

   tcirDiagnosticClear(diagnostic);
   if (function == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<function>", 0, "function is null");
      return 0;
   }
   if (function->block_count == 0)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_MISSING_BLOCK,
         function->identity,
         0,
         "function has no entry block");
      return 0;
   }

   for (block_index = 0; block_index < function->block_count; block_index++)
   {
      const TCIRBlock *block = function->blocks[block_index];
      if (!tcirVerifySource(function, block->source, "block", diagnostic))
         return 0;
      for (other = block_index + 1; other < function->block_count; other++)
      {
         if (block->id == function->blocks[other]->id)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_DUPLICATE_BLOCK,
               function->identity,
               block->source.tc_pc,
               "block id bb%u occurs more than once",
               block->id);
            return 0;
         }
      }
      if (block->is_exception_handler &&
          (block->argument_count == 0 || !tcirIsReference(block->arguments[0]->type)))
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_HANDLER_SIGNATURE,
            function->identity,
            block->source.tc_pc,
            "handler bb%u must receive a managed reference as its first argument",
            block->id);
         return 0;
      }
      for (operation_index = 0; operation_index < block->operation_count; operation_index++)
         if (!tcirVerifyOperation(function, block, operation_index, diagnostic))
            return 0;
      if (!tcirVerifyTerminator(function, block, diagnostic))
         return 0;
   }

   return tcirVerifyReachability(function, diagnostic);
}
