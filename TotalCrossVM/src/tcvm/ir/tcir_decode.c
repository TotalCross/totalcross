// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_decode.h"

#include "../opcodes.h"
#include "tcir_internal.h"

#include <limits.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#define TCIR_NO_INSTRUCTION ((size_t)-1)

static int tcirSignExtend(unsigned int value, unsigned int bits)
{
   unsigned int sign = 1U << (bits - 1U);
   return (int)((value ^ sign) - sign);
}

static unsigned int tcirBits(unsigned int slot, unsigned int shift, unsigned int bits)
{
   return (slot >> shift) & ((1U << bits) - 1U);
}

static int tcirDecodeWidth(
   const TCIRMethodView *method,
   const TCIROpcodeInfo *info,
   unsigned int slot,
   size_t pc,
   size_t *width,
   TCIRDiagnostic *diagnostic)
{
   size_t payload;

   *width = 1;
   switch (info->decoder_class)
   {
      case TCIR_DECODER_SINGLE:
         return 1;

      case TCIR_DECODER_SWITCH:
      {
         size_t keys = tcirBits(slot, 16, 16);
         if (keys > (SIZE_MAX - 2U) / 2U)
            goto malformed;
         payload = 1U + keys + ((keys + 1U) / 2U);
         if (payload > SIZE_MAX - 1U)
            goto malformed;
         *width = 1U + payload;
         break;
      }

      case TCIR_DECODER_MULTIARRAY:
      {
         size_t dimensions = tcirBits(slot, 26, 6);
         payload = (dimensions + 3U) / 4U;
         *width = 1U + payload;
         break;
      }

      case TCIR_DECODER_CALL:
      {
         TCIRCallShape shape;
         size_t packed_values;
         unsigned int symbol = tcirBits(slot, 8, 12);
         if (method->resolve_call_shape == NULL ||
             !method->resolve_call_shape(method->resolve_call_shape_user_data, symbol, &shape) ||
             (shape.returns_value != 0 && shape.returns_value != 1))
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_MALFORMED_CONTINUATION,
               method->identity,
               (unsigned int)pc,
               "cannot resolve continuation width for %s symbol %u",
               info->name,
               symbol);
            return 0;
         }
         packed_values = (size_t)shape.parameter_count + (shape.returns_value ? 1U : 0U);
         payload = packed_values > 1U ? (packed_values - 1U + 3U) / 4U : 0U;
         *width = 1U + payload;
         break;
      }
   }

   if (*width > method->code_slot_count - pc)
      goto malformed;
   return 1;

malformed:
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_MALFORMED_CONTINUATION,
      method->identity,
      (unsigned int)pc,
      "%s continuation extends past %u code slots",
      info->name,
      (unsigned int)method->code_slot_count);
   return 0;
}

static void tcirDecodeOperands(TCIRDecodedInstruction *instruction, unsigned int slot)
{
   unsigned int opcode = instruction->info->value;

   switch (opcode)
   {
      case MOV_regI_regI:
      case MOV_regO_regO:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->reg1 = tcirBits(slot, 16, 8);
         break;
      case MOV_regO_null:
      case TEST_regO:
         instruction->reg0 = tcirBits(slot, 8, 8);
         break;
      case MOV_regI_sym:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->symbol = tcirBits(slot, 16, 16);
         break;
      case MOV_regI_s18:
         instruction->reg0 = tcirBits(slot, 8, 6);
         instruction->immediate = tcirSignExtend(tcirBits(slot, 14, 18), 18);
         break;
      case MOV_reg64_reg64:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->reg1 = tcirBits(slot, 16, 8);
         break;
      case MOV_regD_sym:
      case MOV_regL_sym:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->symbol = tcirBits(slot, 16, 16);
         break;
      case MOV_regD_s18:
      case MOV_regL_s18:
         instruction->reg0 = tcirBits(slot, 8, 6);
         instruction->immediate = tcirSignExtend(tcirBits(slot, 14, 18), 18);
         break;
      case INC_regI:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->immediate = tcirSignExtend(tcirBits(slot, 16, 16), 16);
         break;
      case ADD_regI_regI_regI:
      case SUB_regI_regI_regI:
      case MUL_regI_regI_regI:
      case DIV_regI_regI_regI:
      case MOD_regI_regI_regI:
      case SHR_regI_regI_regI:
      case SHL_regI_regI_regI:
      case USHR_regI_regI_regI:
      case AND_regI_regI_regI:
      case OR_regI_regI_regI:
      case XOR_regI_regI_regI:
      case ADD_regL_regL_regL:
      case SUB_regL_regL_regL:
      case MUL_regL_regL_regL:
      case DIV_regL_regL_regL:
      case MOD_regL_regL_regL:
      case SHR_regL_regL_regL:
      case SHL_regL_regL_regL:
      case USHR_regL_regL_regL:
      case AND_regL_regL_regL:
      case OR_regL_regL_regL:
      case XOR_regL_regL_regL:
      case ADD_regD_regD_regD:
      case SUB_regD_regD_regD:
      case MUL_regD_regD_regD:
      case DIV_regD_regD_regD:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->reg1 = tcirBits(slot, 16, 8);
         instruction->reg2 = tcirBits(slot, 24, 8);
         break;
      case ADD_regI_s12_regI:
      case SUB_regI_s12_regI:
      case MUL_regI_regI_s12:
      case DIV_regI_regI_s12:
      case MOD_regI_regI_s12:
      case SHR_regI_regI_s12:
      case SHL_regI_regI_s12:
      case USHR_regI_regI_s12:
      case AND_regI_regI_s12:
      case OR_regI_regI_s12:
      case XOR_regI_regI_s12:
         instruction->reg0 = tcirBits(slot, 8, 6);
         instruction->reg1 = tcirBits(slot, 14, 6);
         instruction->immediate = tcirSignExtend(tcirBits(slot, 20, 12), 12);
         break;
      case CONV_regIb_regI:
      case CONV_regIc_regI:
      case CONV_regIs_regI:
      case CONV_regI_regL:
      case CONV_regL_regI:
      case CONV_regD_regI:
      case CONV_regD_regL:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->reg1 = tcirBits(slot, 16, 8);
         break;
      case ADD_regI_regI_sym:
         instruction->symbol = tcirBits(slot, 8, 12);
         instruction->reg0 = tcirBits(slot, 20, 6);
         instruction->reg1 = tcirBits(slot, 26, 6);
         break;
      case JEQ_regI_regI:
      case JNE_regI_regI:
      case JLT_regI_regI:
      case JLE_regI_regI:
      case JGT_regI_regI:
      case JGE_regI_regI:
      case JEQ_regL_regL:
      case JNE_regL_regL:
      case JLT_regL_regL:
      case JLE_regL_regL:
      case JGT_regL_regL:
      case JGE_regL_regL:
      case JEQ_regD_regD:
      case JNE_regD_regD:
      case JLT_regD_regD:
      case JLE_regD_regD:
      case JGT_regD_regD:
      case JGE_regD_regD:
      case JEQ_regO_regO:
      case JEQ_regO_null:
      case JNE_regO_regO:
      case JNE_regO_null:
         instruction->reg0 = tcirBits(slot, 8, 6);
         instruction->reg1 = tcirBits(slot, 14, 6);
         instruction->target = (int)instruction->pc +
            tcirSignExtend(tcirBits(slot, 20, 12), 12);
         break;
      case JEQ_regI_s6:
      case JNE_regI_s6:
      case JLT_regI_s6:
      case JLE_regI_s6:
      case JGT_regI_s6:
      case JGE_regI_s6:
         instruction->reg0 = tcirBits(slot, 8, 6);
         instruction->immediate = tcirSignExtend(tcirBits(slot, 14, 6), 6);
         instruction->target = (int)instruction->pc +
            tcirSignExtend(tcirBits(slot, 20, 12), 12);
         break;
      case JEQ_regI_sym:
      case JNE_regI_sym:
         instruction->symbol = tcirBits(slot, 8, 12);
         instruction->reg0 = tcirBits(slot, 20, 6);
         instruction->target = (int)instruction->pc +
            tcirSignExtend(tcirBits(slot, 26, 6), 6);
         break;
      case DECJGTZ_regI:
      case DECJGEZ_regI:
         instruction->reg0 = tcirBits(slot, 8, 8);
         instruction->target = (int)instruction->pc +
            tcirSignExtend(tcirBits(slot, 16, 16), 16);
         break;
      case JUMP_s24:
         instruction->target = (int)instruction->pc +
            tcirSignExtend(tcirBits(slot, 8, 24), 24);
         break;
      case RETURN_regI:
      case RETURN_reg64:
      case RETURN_regO:
         instruction->reg0 = tcirBits(slot, 8, 8);
         break;
      case RETURN_s24I:
      case RETURN_s24D:
      case RETURN_s24L:
         instruction->immediate = tcirSignExtend(tcirBits(slot, 8, 24), 24);
         break;
      case RETURN_symI:
      case RETURN_symD:
      case RETURN_symL:
         instruction->symbol = tcirBits(slot, 16, 16);
         break;
      default:
         break;
   }
}

static int tcirValidateRegister(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   unsigned int reg,
   const char *role,
   TCIRDiagnostic *diagnostic)
{
   if (reg < method->i32_home_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_REGISTER,
      method->identity,
      instruction->pc,
      "%s %s uses i32 register %u but the method declares %u",
      instruction->info->name,
      role,
      reg,
      method->i32_home_count);
   return 0;
}

static int tcirValidateV64Register(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   unsigned int reg,
   const char *role,
   TCIRType expected_type,
   TCIRDiagnostic *diagnostic)
{
   if (reg < method->v64_home_count && method->v64_home_types[reg] == expected_type)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_REGISTER,
      method->identity,
      instruction->pc,
      "%s %s uses %s register %u but the method does not declare that typed v64 home",
      instruction->info->name,
      role,
      tcirTypeName(expected_type),
      reg);
   return 0;
}

static int tcirValidateRefRegister(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   unsigned int reg,
   const char *role,
   TCIRDiagnostic *diagnostic)
{
   if (reg < method->ref_home_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_REGISTER,
      method->identity,
      instruction->pc,
      "%s %s uses reference register %u but the method declares %u",
      instruction->info->name,
      role,
      reg,
      method->ref_home_count);
   return 0;
}

static int tcirValidateV64Index(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   unsigned int reg,
   const char *role,
   TCIRDiagnostic *diagnostic)
{
   if (reg < method->v64_home_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_REGISTER,
      method->identity,
      instruction->pc,
      "%s %s uses v64 register %u but the method declares %u",
      instruction->info->name,
      role,
      reg,
      method->v64_home_count);
   return 0;
}

static int tcirValidateSymbol(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   if (instruction->symbol < method->i32_constant_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_SYMBOL,
      method->identity,
      instruction->pc,
      "%s uses i32 symbol %u but the pool has %u entries",
      instruction->info->name,
      instruction->symbol,
      (unsigned int)method->i32_constant_count);
   return 0;
}

static int tcirValidateI64Symbol(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   if (instruction->symbol < method->i64_constant_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_SYMBOL,
      method->identity,
      instruction->pc,
      "%s uses i64 symbol %u but the pool has %u entries",
      instruction->info->name,
      instruction->symbol,
      (unsigned int)method->i64_constant_count);
   return 0;
}

static int tcirValidateF64Symbol(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   if (instruction->symbol < method->f64_constant_count)
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_SYMBOL,
      method->identity,
      instruction->pc,
      "%s uses f64 symbol %u but the pool has %u entries",
      instruction->info->name,
      instruction->symbol,
      (unsigned int)method->f64_constant_count);
   return 0;
}

static int tcirValidateV64Copy(
   const TCIRMethodView *method,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   if (instruction->reg0 < method->v64_home_count &&
       instruction->reg1 < method->v64_home_count &&
       method->v64_home_types[instruction->reg0] == method->v64_home_types[instruction->reg1] &&
       (method->v64_home_types[instruction->reg0] == TCIR_TYPE_I64 ||
        method->v64_home_types[instruction->reg0] == TCIR_TYPE_F64))
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_TYPE_MERGE,
      method->identity,
      instruction->pc,
      "%s requires matching i64 or f64 source and destination homes",
      instruction->info->name);
   return 0;
}

static int tcirIsConditional(unsigned int opcode)
{
   return opcode == JEQ_regO_regO || opcode == JEQ_regO_null ||
          opcode == JNE_regO_regO || opcode == JNE_regO_null ||
          opcode == JEQ_regI_regI || opcode == JEQ_regI_s6 || opcode == JEQ_regI_sym ||
          opcode == JNE_regI_regI || opcode == JNE_regI_s6 || opcode == JNE_regI_sym ||
          opcode == JLT_regI_regI || opcode == JLT_regI_s6 ||
          opcode == JLE_regI_regI || opcode == JLE_regI_s6 ||
          opcode == JGT_regI_regI || opcode == JGT_regI_s6 ||
          opcode == JGE_regI_regI || opcode == JGE_regI_s6 ||
          opcode == JEQ_regL_regL || opcode == JNE_regL_regL ||
          opcode == JLT_regL_regL || opcode == JLE_regL_regL ||
          opcode == JGT_regL_regL || opcode == JGE_regL_regL ||
          opcode == JEQ_regD_regD || opcode == JNE_regD_regD ||
          opcode == JLT_regD_regD || opcode == JLE_regD_regD ||
          opcode == JGT_regD_regD || opcode == JGE_regD_regD ||
          opcode == DECJGTZ_regI || opcode == DECJGEZ_regI;
}

static int tcirValidateTarget(
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   unsigned int source_pc,
   int target,
   const char *description,
   TCIRDiagnostic *diagnostic)
{
   if (target >= 0 && (size_t)target < method->code_slot_count && decoded->instruction_starts[target])
      return 1;
   tcirSetDiagnostic(
      diagnostic,
      TCIR_DIAGNOSTIC_INVALID_TARGET,
      method->identity,
      source_pc,
      "%s target %d is outside the code or points to a continuation slot",
      description,
      target);
   return 0;
}

static int tcirValidateSwitchTargets(
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   size_t keys = tcirBits(method->code[instruction->pc], 16, 16);
   size_t index;
   int target;

   target = (int)instruction->pc + (int)(method->code[instruction->pc + 1U] & 0xffffU);
   if (!tcirValidateTarget(method, decoded, instruction->pc, target, "SWITCH default", diagnostic))
      return 0;
   for (index = 0; index < keys; index++)
   {
      size_t target_slot = instruction->pc + 2U + keys + (index / 2U);
      unsigned int shift = (unsigned int)(index % 2U) * 16U;
      target = (int)instruction->pc + (int)tcirBits(method->code[target_slot], shift, 16);
      if (!tcirValidateTarget(method, decoded, instruction->pc, target, "SWITCH case", diagnostic))
         return 0;
   }
   return 1;
}

static int tcirValidateInstruction(
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   const TCIRDecodedInstruction *instruction,
   TCIRDiagnostic *diagnostic)
{
   unsigned int opcode = instruction->info->value;

   if ((opcode == RETURN_void && method->return_type != TCIR_TYPE_VOID) ||
       ((opcode == RETURN_regI || opcode == RETURN_s24I || opcode == RETURN_symI) &&
        method->return_type != TCIR_TYPE_I32) ||
       ((opcode == RETURN_s24L || opcode == RETURN_symL) && method->return_type != TCIR_TYPE_I64) ||
       ((opcode == RETURN_s24D || opcode == RETURN_symD) && method->return_type != TCIR_TYPE_F64) ||
       (opcode == RETURN_reg64 && method->return_type != TCIR_TYPE_I64 &&
        method->return_type != TCIR_TYPE_F64) ||
       ((opcode == RETURN_regO || opcode == RETURN_null) && method->return_type != TCIR_TYPE_REF))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_TYPE_MERGE,
         method->identity,
         instruction->pc,
         "%s does not match method return type %s",
         instruction->info->name,
         tcirTypeName(method->return_type));
      return 0;
   }

   switch (opcode)
   {
      case MOV_regI_regI:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "source", diagnostic);
      case MOV_regO_regO:
         return tcirValidateRefRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRefRegister(method, instruction, instruction->reg1, "source", diagnostic);
      case MOV_regO_null:
      case RETURN_regO:
      case TEST_regO:
         return tcirValidateRefRegister(method, instruction, instruction->reg0, "operand", diagnostic);
      case MOV_regI_sym:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateSymbol(method, instruction, diagnostic);
      case MOV_regI_s18:
      case INC_regI:
      case RETURN_regI:
      case DECJGTZ_regI:
      case DECJGEZ_regI:
         if (!tcirValidateRegister(method, instruction, instruction->reg0, "operand", diagnostic))
            return 0;
         break;
      case MOV_reg64_reg64:
         return tcirValidateV64Copy(method, instruction, diagnostic);
      case MOV_regL_sym:
         return tcirValidateV64Register(
                   method, instruction, instruction->reg0, "destination", TCIR_TYPE_I64, diagnostic) &&
                tcirValidateI64Symbol(method, instruction, diagnostic);
      case MOV_regD_sym:
         return tcirValidateV64Register(
                   method, instruction, instruction->reg0, "destination", TCIR_TYPE_F64, diagnostic) &&
                tcirValidateF64Symbol(method, instruction, diagnostic);
      case MOV_regL_s18:
         if (!tcirValidateV64Register(
                method, instruction, instruction->reg0, "operand", TCIR_TYPE_I64, diagnostic))
            return 0;
         break;
      case MOV_regD_s18:
         if (!tcirValidateV64Register(
                method, instruction, instruction->reg0, "operand", TCIR_TYPE_F64, diagnostic))
            return 0;
         break;
      case RETURN_reg64:
         if (!tcirValidateV64Index(method, instruction, instruction->reg0, "operand", diagnostic))
            return 0;
         break;
      case ADD_regI_regI_regI:
      case SUB_regI_regI_regI:
      case MUL_regI_regI_regI:
      case DIV_regI_regI_regI:
      case MOD_regI_regI_regI:
      case SHR_regI_regI_regI:
      case SHL_regI_regI_regI:
      case USHR_regI_regI_regI:
      case AND_regI_regI_regI:
      case OR_regI_regI_regI:
      case XOR_regI_regI_regI:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "left operand", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg2, "right operand", diagnostic);
      case ADD_regL_regL_regL:
      case SUB_regL_regL_regL:
      case MUL_regL_regL_regL:
      case DIV_regL_regL_regL:
      case MOD_regL_regL_regL:
      case SHR_regL_regL_regL:
      case SHL_regL_regL_regL:
      case USHR_regL_regL_regL:
      case AND_regL_regL_regL:
      case OR_regL_regL_regL:
      case XOR_regL_regL_regL:
         return tcirValidateV64Register(
                   method, instruction, instruction->reg0, "destination", TCIR_TYPE_I64, diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg1, "left operand", TCIR_TYPE_I64, diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg2, "right operand", TCIR_TYPE_I64, diagnostic);
      case ADD_regD_regD_regD:
      case SUB_regD_regD_regD:
      case MUL_regD_regD_regD:
      case DIV_regD_regD_regD:
         return tcirValidateV64Register(
                   method, instruction, instruction->reg0, "destination", TCIR_TYPE_F64, diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg1, "left operand", TCIR_TYPE_F64, diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg2, "right operand", TCIR_TYPE_F64, diagnostic);
      case ADD_regI_s12_regI:
      case SUB_regI_s12_regI:
      case MUL_regI_regI_s12:
      case DIV_regI_regI_s12:
      case MOD_regI_regI_s12:
      case SHR_regI_regI_s12:
      case SHL_regI_regI_s12:
      case USHR_regI_regI_s12:
      case AND_regI_regI_s12:
      case OR_regI_regI_s12:
      case XOR_regI_regI_s12:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "operand", diagnostic);
      case CONV_regIb_regI:
      case CONV_regIc_regI:
      case CONV_regIs_regI:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "source", diagnostic);
      case CONV_regI_regL:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg1, "source", TCIR_TYPE_I64, diagnostic);
      case CONV_regL_regI:
         return tcirValidateV64Register(
                   method, instruction, instruction->reg0, "destination", TCIR_TYPE_I64, diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "source", diagnostic);
      case CONV_regD_regI:
         return tcirValidateV64Index(
                   method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "source", diagnostic);
      case CONV_regD_regL:
         return tcirValidateV64Index(
                   method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateV64Register(
                   method, instruction, instruction->reg1, "source", TCIR_TYPE_I64, diagnostic);
      case ADD_regI_regI_sym:
         return tcirValidateRegister(method, instruction, instruction->reg0, "destination", diagnostic) &&
                tcirValidateRegister(method, instruction, instruction->reg1, "operand", diagnostic) &&
                tcirValidateSymbol(method, instruction, diagnostic);
      case JEQ_regI_regI:
      case JNE_regI_regI:
      case JLT_regI_regI:
      case JLE_regI_regI:
      case JGT_regI_regI:
      case JGE_regI_regI:
         if (!tcirValidateRegister(method, instruction, instruction->reg0, "left operand", diagnostic) ||
             !tcirValidateRegister(method, instruction, instruction->reg1, "right operand", diagnostic))
            return 0;
         break;
      case JEQ_regL_regL:
      case JNE_regL_regL:
      case JLT_regL_regL:
      case JLE_regL_regL:
      case JGT_regL_regL:
      case JGE_regL_regL:
         if (!tcirValidateV64Register(
                method, instruction, instruction->reg0, "left operand", TCIR_TYPE_I64, diagnostic) ||
             !tcirValidateV64Register(
                method, instruction, instruction->reg1, "right operand", TCIR_TYPE_I64, diagnostic))
            return 0;
         break;
      case JEQ_regD_regD:
      case JNE_regD_regD:
      case JLT_regD_regD:
      case JLE_regD_regD:
      case JGT_regD_regD:
      case JGE_regD_regD:
         if (!tcirValidateV64Register(
                method, instruction, instruction->reg0, "left operand", TCIR_TYPE_F64, diagnostic) ||
             !tcirValidateV64Register(
                method, instruction, instruction->reg1, "right operand", TCIR_TYPE_F64, diagnostic))
            return 0;
         break;
      case JEQ_regO_regO:
      case JNE_regO_regO:
         if (!tcirValidateRefRegister(method, instruction, instruction->reg0, "left operand", diagnostic) ||
             !tcirValidateRefRegister(method, instruction, instruction->reg1, "right operand", diagnostic))
            return 0;
         break;
      case JEQ_regO_null:
      case JNE_regO_null:
         if (!tcirValidateRefRegister(method, instruction, instruction->reg0, "operand", diagnostic))
            return 0;
         break;
      case JEQ_regI_s6:
      case JNE_regI_s6:
      case JLT_regI_s6:
      case JLE_regI_s6:
      case JGT_regI_s6:
      case JGE_regI_s6:
         if (!tcirValidateRegister(method, instruction, instruction->reg0, "operand", diagnostic))
            return 0;
         break;
      case JEQ_regI_sym:
      case JNE_regI_sym:
         if (!tcirValidateRegister(method, instruction, instruction->reg0, "operand", diagnostic) ||
             !tcirValidateSymbol(method, instruction, diagnostic))
            return 0;
         break;
      case RETURN_symI:
         return tcirValidateSymbol(method, instruction, diagnostic);
      case RETURN_symL:
         return tcirValidateI64Symbol(method, instruction, diagnostic);
      case RETURN_symD:
         return tcirValidateF64Symbol(method, instruction, diagnostic);
      case SWITCH:
         return tcirValidateSwitchTargets(method, decoded, instruction, diagnostic);
      default:
         break;
   }

   if (opcode == JUMP_s24 || tcirIsConditional(opcode))
   {
      if (!tcirValidateTarget(
             method, decoded, instruction->pc, instruction->target, instruction->info->name, diagnostic))
         return 0;
   }
   if (tcirIsConditional(opcode) &&
       !tcirValidateTarget(
          method,
          decoded,
          instruction->pc,
          (int)(instruction->pc + instruction->width),
          "conditional fallthrough",
          diagnostic))
      return 0;
   return 1;
}

static int tcirValidateParameters(const TCIRMethodView *method, TCIRDiagnostic *diagnostic)
{
   unsigned char *seen;
   size_t total_homes = (size_t)method->i32_home_count + method->ref_home_count + method->v64_home_count;
   size_t index;

   if (method->parameter_count == 0)
      return 1;
   seen = (unsigned char *)calloc(total_homes == 0U ? 1U : total_homes, 1);
   if (seen == NULL)
   {
      tcirSetDiagnostic(
         diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, method->identity, 0, "cannot validate parameter homes");
      return 0;
   }
   for (index = 0; index < method->parameter_count; index++)
   {
      const TCIRMethodParameter *parameter = &method->parameters[index];
      size_t seen_index = parameter->home_bank == TCIR_HOME_I32
         ? parameter->home_index
         : (parameter->home_bank == TCIR_HOME_REF
            ? (size_t)method->i32_home_count + parameter->home_index
            : (size_t)method->i32_home_count + method->ref_home_count + parameter->home_index);
      int valid_i32 = parameter->type == TCIR_TYPE_I32 && parameter->home_bank == TCIR_HOME_I32 &&
         parameter->home_index < method->i32_home_count;
      int valid_i64 = parameter->type == TCIR_TYPE_I64 && parameter->home_bank == TCIR_HOME_V64 &&
         parameter->home_index < method->v64_home_count &&
         method->v64_home_types[parameter->home_index] == TCIR_TYPE_I64;
      int valid_f64 = parameter->type == TCIR_TYPE_F64 && parameter->home_bank == TCIR_HOME_V64 &&
         parameter->home_index < method->v64_home_count &&
         method->v64_home_types[parameter->home_index] == TCIR_TYPE_F64;
      int valid_ref = parameter->type == TCIR_TYPE_REF && parameter->home_bank == TCIR_HOME_REF &&
         parameter->home_index < method->ref_home_count;
      if ((!valid_i32 && !valid_i64 && !valid_f64 && !valid_ref) || seen[seen_index])
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_TYPE_MERGE,
            method->identity,
            0,
            "parameter %u cannot seed home %u with type %s",
            (unsigned int)index,
            parameter->home_index,
            tcirTypeName(parameter->type));
         free(seen);
         return 0;
      }
      seen[seen_index] = 1;
   }
   free(seen);
   return 1;
}

static int tcirValidateV64Homes(const TCIRMethodView *method, TCIRDiagnostic *diagnostic)
{
   size_t index;
   for (index = 0U; index < method->v64_home_count; ++index)
      if (method->v64_home_types[index] != TCIR_TYPE_I64 &&
          method->v64_home_types[index] != TCIR_TYPE_F64)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_TYPE_MERGE,
            method->identity,
            0U,
            "v64 home %u must be declared i64 or f64",
            (unsigned int)index);
         return 0;
      }
   return 1;
}

static int tcirValidateHandlers(
   const TCIRMethodView *method,
   const TCIRDecodedMethod *decoded,
   TCIRDiagnostic *diagnostic)
{
   size_t index;
   for (index = 0; index < method->handler_count; index++)
   {
      const TCIRMethodHandler *handler = &method->handlers[index];
      if (handler->start_pc > handler->end_pc || handler->end_pc >= method->code_slot_count ||
          !decoded->instruction_starts[handler->start_pc] || !decoded->instruction_starts[handler->end_pc] ||
          handler->handler_pc >= method->code_slot_count || !decoded->instruction_starts[handler->handler_pc] ||
          handler->exception_home >= method->ref_home_count)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_INVALID_HANDLER,
            method->identity,
            handler->handler_pc,
            "handler %u has invalid range %u..%u, target %u, or exception home %u",
            (unsigned int)index,
            handler->start_pc,
            handler->end_pc,
            handler->handler_pc,
            handler->exception_home);
         return 0;
      }
   }
   return 1;
}

TCIRFrontendResult tcirDecodeMethod(
   const TCIRMethodView *method,
   TCIRDecodedMethod *decoded,
   TCIRDiagnostic *diagnostic)
{
   size_t pc;
   size_t count = 0;
   size_t width;
   size_t index;

   tcirDiagnosticClear(diagnostic);
   if (decoded != NULL)
      memset(decoded, 0, sizeof(*decoded));
   if (method == NULL || decoded == NULL || method->identity == NULL || method->code == NULL ||
       method->code_slot_count == 0 || sizeof(unsigned int) != 4U ||
       (method->parameter_count != 0 && method->parameters == NULL) ||
       (method->i32_constant_count != 0 && method->i32_constants == NULL) ||
       (method->i64_constant_count != 0 && method->i64_constants == NULL) ||
       (method->f64_constant_count != 0 && method->f64_constants == NULL) ||
       (method->v64_home_count != 0 && method->v64_home_types == NULL) ||
       (method->handler_count != 0 && method->handlers == NULL))
   {
      tcirSetDiagnostic(
         diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<frontend>", 0, "method view is incomplete");
      return TCIR_FRONTEND_ERROR;
   }

   decoded->instructions = (TCIRDecodedInstruction *)calloc(
      method->code_slot_count, sizeof(TCIRDecodedInstruction));
   decoded->instruction_starts = (unsigned char *)calloc(method->code_slot_count, 1);
   decoded->instruction_indexes = (size_t *)malloc(method->code_slot_count * sizeof(size_t));
   if (decoded->instructions == NULL || decoded->instruction_starts == NULL ||
       decoded->instruction_indexes == NULL)
   {
      tcirSetDiagnostic(
         diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, method->identity, 0, "cannot allocate decoded code map");
      tcirDecodedMethodDestroy(decoded);
      return TCIR_FRONTEND_ERROR;
   }
   for (index = 0; index < method->code_slot_count; index++)
      decoded->instruction_indexes[index] = TCIR_NO_INSTRUCTION;

   for (pc = 0; pc < method->code_slot_count; pc += width)
   {
      unsigned int slot = method->code[pc];
      unsigned int opcode = slot & 0xffU;
      const TCIROpcodeInfo *info = tcirOpcodeLookup(opcode);
      TCIRDecodedInstruction *instruction;
      if (info == NULL)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
            method->identity,
            (unsigned int)pc,
            "unsupported TC opcode %u (<unknown>) at TC PC %u",
            opcode,
            (unsigned int)pc);
         tcirDecodedMethodDestroy(decoded);
         return TCIR_FRONTEND_FALLBACK;
      }
      if (!tcirDecodeWidth(method, info, slot, pc, &width, diagnostic))
      {
         tcirDecodedMethodDestroy(decoded);
         return TCIR_FRONTEND_ERROR;
      }
      instruction = &decoded->instructions[count];
      instruction->info = info;
      instruction->pc = (unsigned int)pc;
      instruction->width = (unsigned int)width;
      tcirDecodeOperands(instruction, slot);
      decoded->instruction_starts[pc] = 1;
      decoded->instruction_indexes[pc] = count;
      count++;
   }
   decoded->instruction_count = count;

   if (!tcirValidateV64Homes(method, diagnostic) ||
       !tcirValidateParameters(method, diagnostic) ||
       !tcirValidateHandlers(method, decoded, diagnostic))
   {
      tcirDecodedMethodDestroy(decoded);
      return TCIR_FRONTEND_ERROR;
   }
   for (index = 0; index < count; index++)
   {
      if (!tcirValidateInstruction(method, decoded, &decoded->instructions[index], diagnostic))
      {
         tcirDecodedMethodDestroy(decoded);
         return TCIR_FRONTEND_ERROR;
      }
   }
   if (decoded->instructions[count - 1U].info->poc_status == TCIR_POC_SUPPORTED &&
       decoded->instructions[count - 1U].info->value != JUMP_s24 &&
       decoded->instructions[count - 1U].info->value != RETURN_regI &&
       decoded->instructions[count - 1U].info->value != RETURN_void &&
       decoded->instructions[count - 1U].info->value != RETURN_s24I &&
       decoded->instructions[count - 1U].info->value != RETURN_symI &&
       decoded->instructions[count - 1U].info->value != RETURN_regO &&
       decoded->instructions[count - 1U].info->value != RETURN_null &&
       decoded->instructions[count - 1U].info->value != RETURN_reg64 &&
       decoded->instructions[count - 1U].info->value != RETURN_s24L &&
       decoded->instructions[count - 1U].info->value != RETURN_symL)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_TARGET,
         method->identity,
         decoded->instructions[count - 1U].pc,
         "control falls through past the final code slot");
      tcirDecodedMethodDestroy(decoded);
      return TCIR_FRONTEND_ERROR;
   }
   for (index = 0; index < count; index++)
   {
      const TCIRDecodedInstruction *instruction = &decoded->instructions[index];
      if (instruction->info->poc_status != TCIR_POC_SUPPORTED)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_UNSUPPORTED_OPCODE,
            method->identity,
            instruction->pc,
            "unsupported TC opcode %u (%s) at TC PC %u; keep method interpreter-eligible",
            instruction->info->value,
            instruction->info->name,
            instruction->pc);
         return TCIR_FRONTEND_FALLBACK;
      }
   }
   return TCIR_FRONTEND_OK;
}

void tcirDecodedMethodDestroy(TCIRDecodedMethod *decoded)
{
   if (decoded == NULL)
      return;
   free(decoded->instructions);
   free(decoded->instruction_starts);
   free(decoded->instruction_indexes);
   memset(decoded, 0, sizeof(*decoded));
}
