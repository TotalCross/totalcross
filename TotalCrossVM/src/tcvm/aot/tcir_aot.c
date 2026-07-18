// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_aot.h"

#include <ctype.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define TCIR_AOT_FNV_OFFSET UINT64_C(1469598103934665603)
#define TCIR_AOT_FNV_PRIME UINT64_C(1099511628211)

typedef struct TCIRAotBuffer
{
   char *data;
   size_t size;
   size_t capacity;
} TCIRAotBuffer;

typedef struct TCIRAotMethodInfo
{
   const TCIRFunction *function;
   const char *identity;
   char *class_name;
   char *method_name;
   char *signature;
   char *symbol;
   uint64_t content_hash;
   size_t value_count;
   size_t edge_value_count;
} TCIRAotMethodInfo;

static void tcirAotSetDiagnostic(
   TCIRAotDiagnostic *diagnostic,
   TCIRAotDiagnosticCode code,
   const TCIRFunction *function,
   unsigned int tc_pc,
   const char *format,
   ...)
{
   va_list arguments;

   if (diagnostic == NULL)
      return;
   diagnostic->code = code;
   diagnostic->tc_pc = tc_pc;
   if (function != NULL && tcirFunctionIdentity(function) != NULL)
      snprintf(diagnostic->function, sizeof(diagnostic->function), "%s", tcirFunctionIdentity(function));
   va_start(arguments, format);
   vsnprintf(diagnostic->message, sizeof(diagnostic->message), format, arguments);
   va_end(arguments);
}

void tcirAotDiagnosticClear(TCIRAotDiagnostic *diagnostic)
{
   if (diagnostic != NULL)
   {
      memset(diagnostic, 0, sizeof(*diagnostic));
      diagnostic->tc_pc = TCIR_TCPC_NONE;
      tcirDiagnosticClear(&diagnostic->verifier);
   }
}

const char *tcirAotDiagnosticCodeName(TCIRAotDiagnosticCode code)
{
   switch (code)
   {
      case TCIR_AOT_DIAGNOSTIC_NONE: return "none";
      case TCIR_AOT_DIAGNOSTIC_INVALID_ARGUMENT: return "invalid_argument";
      case TCIR_AOT_DIAGNOSTIC_VERIFICATION_FAILED: return "verification_failed";
      case TCIR_AOT_DIAGNOSTIC_INVALID_IDENTITY: return "invalid_identity";
      case TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE: return "ineligible_type";
      case TCIR_AOT_DIAGNOSTIC_INELIGIBLE_OPERATION: return "ineligible_operation";
      case TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TERMINATOR: return "ineligible_terminator";
      case TCIR_AOT_DIAGNOSTIC_DUPLICATE_IDENTITY: return "duplicate_identity";
      case TCIR_AOT_DIAGNOSTIC_OUT_OF_MEMORY: return "out_of_memory";
      case TCIR_AOT_DIAGNOSTIC_EMISSION_FAILED: return "emission_failed";
      default: return "unknown";
   }
}

static int tcirAotBufferReserve(TCIRAotBuffer *buffer, size_t extra)
{
   size_t required;
   size_t capacity;
   char *data;

   if (extra > SIZE_MAX - buffer->size - 1U)
      return 0;
   required = buffer->size + extra + 1U;
   if (required <= buffer->capacity)
      return 1;
   capacity = buffer->capacity == 0U ? 1024U : buffer->capacity;
   while (capacity < required)
   {
      if (capacity > SIZE_MAX / 2U)
      {
         capacity = required;
         break;
      }
      capacity *= 2U;
   }
   data = (char *)realloc(buffer->data, capacity);
   if (data == NULL)
      return 0;
   buffer->data = data;
   buffer->capacity = capacity;
   return 1;
}

static int tcirAotBufferAppendBytes(TCIRAotBuffer *buffer, const char *text, size_t length)
{
   if (!tcirAotBufferReserve(buffer, length))
      return 0;
   if (length != 0U)
      memcpy(buffer->data + buffer->size, text, length);
   buffer->size += length;
   buffer->data[buffer->size] = '\0';
   return 1;
}

static int tcirAotBufferAppend(TCIRAotBuffer *buffer, const char *text)
{
   return tcirAotBufferAppendBytes(buffer, text, strlen(text));
}

static int tcirAotBufferAppendFormat(TCIRAotBuffer *buffer, const char *format, ...)
{
   va_list arguments;
   va_list copy;
   int required;

   va_start(arguments, format);
   va_copy(copy, arguments);
   required = vsnprintf(NULL, 0U, format, copy);
   va_end(copy);
   if (required < 0 || !tcirAotBufferReserve(buffer, (size_t)required))
   {
      va_end(arguments);
      return 0;
   }
   if (vsnprintf(buffer->data + buffer->size, (size_t)required + 1U, format, arguments) != required)
   {
      va_end(arguments);
      return 0;
   }
   va_end(arguments);
   buffer->size += (size_t)required;
   return 1;
}

static int tcirAotAppendJsonString(TCIRAotBuffer *buffer, const char *text)
{
   const unsigned char *cursor = (const unsigned char *)text;

   if (!tcirAotBufferAppend(buffer, "\""))
      return 0;
   while (*cursor != 0U)
   {
      unsigned char value = *cursor++;
      switch (value)
      {
         case '"': if (!tcirAotBufferAppend(buffer, "\\\"")) return 0; break;
         case '\\': if (!tcirAotBufferAppend(buffer, "\\\\")) return 0; break;
         case '\b': if (!tcirAotBufferAppend(buffer, "\\b")) return 0; break;
         case '\f': if (!tcirAotBufferAppend(buffer, "\\f")) return 0; break;
         case '\n': if (!tcirAotBufferAppend(buffer, "\\n")) return 0; break;
         case '\r': if (!tcirAotBufferAppend(buffer, "\\r")) return 0; break;
         case '\t': if (!tcirAotBufferAppend(buffer, "\\t")) return 0; break;
         default:
            if (value < 0x20U)
            {
               if (!tcirAotBufferAppendFormat(buffer, "\\u%04x", (unsigned int)value))
                  return 0;
            }
            else if (!tcirAotBufferAppendBytes(buffer, (const char *)&value, 1U))
               return 0;
            break;
      }
   }
   return tcirAotBufferAppend(buffer, "\"");
}

static char *tcirAotDuplicateRange(const char *text, size_t length)
{
   char *copy = (char *)malloc(length + 1U);
   if (copy == NULL)
      return NULL;
   memcpy(copy, text, length);
   copy[length] = '\0';
   return copy;
}

static char *tcirAotMakeSymbol(const char *identity, uint64_t hash)
{
   static const char hexadecimal[] = "0123456789abcdef";
   TCIRAotBuffer buffer;
   const unsigned char *cursor = (const unsigned char *)identity;
   char suffix[32];

   memset(&buffer, 0, sizeof(buffer));
   if (!tcirAotBufferAppend(&buffer, "tc_aot_"))
      return NULL;
   while (*cursor != 0U)
   {
      unsigned char value = *cursor++;
      if (isalnum(value) || value == '_')
      {
         if (!tcirAotBufferAppendBytes(&buffer, (const char *)&value, 1U))
            goto failure;
      }
      else
      {
         char escaped[3];
         escaped[0] = '_';
         escaped[1] = hexadecimal[value >> 4U];
         escaped[2] = hexadecimal[value & 0x0fU];
         if (!tcirAotBufferAppendBytes(&buffer, escaped, sizeof(escaped)))
            goto failure;
      }
   }
   snprintf(suffix, sizeof(suffix), "_%016llx", (unsigned long long)hash);
   if (!tcirAotBufferAppend(&buffer, suffix))
      goto failure;
   return buffer.data;

failure:
   free(buffer.data);
   return NULL;
}

static uint64_t tcirAotHashBytes(uint64_t hash, const void *data, size_t length)
{
   const unsigned char *bytes = (const unsigned char *)data;
   size_t index;
   for (index = 0U; index < length; ++index)
   {
      hash ^= bytes[index];
      hash *= TCIR_AOT_FNV_PRIME;
   }
   return hash;
}

static uint64_t tcirAotHashU64(uint64_t hash, uint64_t value)
{
   unsigned char bytes[8];
   size_t index;
   for (index = 0U; index < sizeof(bytes); ++index)
      bytes[index] = (unsigned char)(value >> (index * 8U));
   return tcirAotHashBytes(hash, bytes, sizeof(bytes));
}

static uint64_t tcirAotHashString(uint64_t hash, const char *text)
{
   size_t length = text == NULL ? 0U : strlen(text);
   hash = tcirAotHashU64(hash, (uint64_t)length);
   return text == NULL ? hash : tcirAotHashBytes(hash, text, length);
}

static uint64_t tcirAotHashValue(uint64_t hash, const TCIRValue *value)
{
   if (value == NULL)
      return tcirAotHashU64(hash, UINT64_MAX);
   hash = tcirAotHashU64(hash, tcirValueId(value));
   return tcirAotHashU64(hash, (uint64_t)tcirValueType(value));
}

static uint64_t tcirAotFunctionHash(const TCIRFunction *function)
{
   uint64_t hash = TCIR_AOT_FNV_OFFSET;
   size_t index;

   hash = tcirAotHashString(hash, tcirFunctionIdentity(function));
   hash = tcirAotHashU64(hash, (uint64_t)tcirFunctionReturnType(function));
   hash = tcirAotHashU64(hash, (uint64_t)tcirFunctionParameterCount(function));
   for (index = 0U; index < tcirFunctionParameterCount(function); ++index)
      hash = tcirAotHashValue(hash, tcirFunctionParameter(function, index));
   for (index = 0U; index < 3U; ++index)
      hash = tcirAotHashU64(hash, tcirFunctionHomeCount(function, (TCIRHomeBank)index));
   hash = tcirAotHashU64(hash, (uint64_t)tcirFunctionBlockCount(function));
   for (index = 0U; index < tcirFunctionBlockCount(function); ++index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(function, index);
      TCIRTerminatorView terminator;
      size_t item;
      hash = tcirAotHashU64(hash, tcirBlockId(block));
      hash = tcirAotHashU64(hash, tcirBlockSource(block).tc_pc);
      hash = tcirAotHashU64(hash, (uint64_t)(uint32_t)tcirBlockSource(block).source_line);
      hash = tcirAotHashU64(hash, (uint64_t)tcirBlockIsExceptionHandler(block));
      hash = tcirAotHashU64(hash, (uint64_t)tcirBlockArgumentCount(block));
      for (item = 0U; item < tcirBlockArgumentCount(block); ++item)
         hash = tcirAotHashValue(hash, tcirBlockArgumentAt(block, item));
      hash = tcirAotHashU64(hash, (uint64_t)tcirBlockOperationCount(block));
      for (item = 0U; item < tcirBlockOperationCount(block); ++item)
      {
         TCIROperationView operation;
         size_t operand;
         (void)tcirBlockOperationAt(block, item, &operation);
         hash = tcirAotHashU64(hash, (uint64_t)operation.opcode);
         hash = tcirAotHashValue(hash, operation.result);
         hash = tcirAotHashU64(hash, (uint64_t)operation.operand_count);
         for (operand = 0U; operand < operation.operand_count; ++operand)
            hash = tcirAotHashValue(hash, operation.operands[operand]);
         hash = tcirAotHashU64(hash, (uint64_t)(uint32_t)operation.immediate_i32);
         hash = tcirAotHashU64(hash, (uint64_t)operation.immediate_i64);
         hash = tcirAotHashU64(hash, operation.immediate_f64_bits);
         hash = tcirAotHashU64(hash, (uint64_t)operation.home_bank);
         hash = tcirAotHashU64(hash, operation.home_index);
         hash = tcirAotHashU64(hash, operation.effects);
         hash = tcirAotHashU64(hash, operation.source.tc_pc);
         hash = tcirAotHashU64(hash, (uint64_t)(uint32_t)operation.source.source_line);
         if (operation.symbol != NULL)
         {
            hash = tcirAotHashU64(hash, (uint64_t)tcirSymbolKind(operation.symbol));
            hash = tcirAotHashString(hash, tcirSymbolOwner(operation.symbol));
            hash = tcirAotHashString(hash, tcirSymbolName(operation.symbol));
            hash = tcirAotHashString(hash, tcirSymbolDescriptor(operation.symbol));
            hash = tcirAotHashU64(hash, tcirSymbolConstantPoolIndex(operation.symbol));
         }
         else
            hash = tcirAotHashU64(hash, UINT64_MAX);
      }
      (void)tcirBlockTerminator(block, &terminator);
      hash = tcirAotHashU64(hash, (uint64_t)terminator.kind);
      hash = tcirAotHashValue(hash, terminator.value);
      hash = tcirAotHashU64(hash, terminator.source.tc_pc);
      hash = tcirAotHashU64(hash, (uint64_t)(uint32_t)terminator.source.source_line);
      hash = tcirAotHashU64(hash, (uint64_t)terminator.edge_count);
      for (item = 0U; item < terminator.edge_count; ++item)
      {
         const TCIREdge *edge = &terminator.edges[item];
         size_t argument;
         hash = tcirAotHashU64(hash, edge->target == NULL ? UINT64_MAX : tcirBlockId(edge->target));
         hash = tcirAotHashU64(hash, (uint64_t)edge->argument_count);
         hash = tcirAotHashU64(hash, (uint64_t)edge->has_case_value);
         hash = tcirAotHashU64(hash, (uint64_t)(uint32_t)edge->case_value);
         for (argument = 0U; argument < edge->argument_count; ++argument)
            hash = tcirAotHashValue(hash, edge->arguments[argument]);
      }
   }
   return hash;
}

static int tcirAotTypeIsI32Like(TCIRType type)
{
   return type == TCIR_TYPE_I1 || type == TCIR_TYPE_I8 ||
      type == TCIR_TYPE_I16 || type == TCIR_TYPE_I32;
}

static int tcirAotTypeIsSupported(TCIRType type)
{
   return tcirAotTypeIsI32Like(type) || type == TCIR_TYPE_I64 || type == TCIR_TYPE_F64;
}

static int tcirAotOperationIsEligible(const TCIROperationView *operation)
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
         return operation->result != NULL && tcirAotTypeIsI32Like(operation->result_type)
            && operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_COPY:
         return operation->result != NULL && tcirAotTypeIsSupported(operation->result_type) &&
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
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I64 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_TRUNC_I64_I32:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I32 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_SEXT_I32_I64:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I64 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CMP_EQ_I64:
      case TCIR_OP_CMP_LT_I64:
      case TCIR_OP_CMP_LE_I64:
      case TCIR_OP_CMP_GT_I64:
      case TCIR_OP_CMP_GE_I64:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I1 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CONST_F64:
      case TCIR_OP_ADD_F64:
      case TCIR_OP_SUB_F64:
      case TCIR_OP_MUL_F64:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_F64 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_CMP_EQ_F64:
      case TCIR_OP_CMP_LT_F64:
      case TCIR_OP_CMP_LE_F64:
      case TCIR_OP_CMP_GT_F64:
      case TCIR_OP_CMP_GE_F64:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_I1 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_I32_TO_F64:
      case TCIR_OP_I64_TO_F64:
         return operation->result != NULL && operation->result_type == TCIR_TYPE_F64 &&
            operation->effects == TCIR_EFFECT_NONE;
      case TCIR_OP_LOAD_SLOT:
         return operation->result != NULL && operation->effects == TCIR_EFFECT_NONE &&
            ((operation->result_type == TCIR_TYPE_I32 && operation->home_bank == TCIR_HOME_I32) ||
             ((operation->result_type == TCIR_TYPE_I64 || operation->result_type == TCIR_TYPE_F64) &&
              operation->home_bank == TCIR_HOME_V64));
      case TCIR_OP_STORE_SLOT:
         return operation->result == NULL && operation->operand_count == 1U
            && operation->effects == TCIR_EFFECT_NONE &&
            (operation->home_bank == TCIR_HOME_I32 || operation->home_bank == TCIR_HOME_V64);
      default:
         return 0;
   }
}

static int tcirAotUpdateValueCount(const TCIRValue *value, size_t *value_count)
{
   size_t candidate;
   if (value == NULL)
      return 1;
   candidate = (size_t)tcirValueId(value) + 1U;
   if (candidate == 0U || candidate > SIZE_MAX / sizeof(TCIRRuntimeValue))
      return 0;
   if (candidate > *value_count)
      *value_count = candidate;
   return 1;
}

static TCIRAotGenerateStatus tcirAotCheckEligibility(
   const TCIRFunction *function,
   size_t *value_count,
   size_t *edge_value_count,
   TCIRAotDiagnostic *diagnostic)
{
   size_t block_index;
   size_t parameter_index;

   if (!tcirVerifyFunction(function, diagnostic == NULL ? NULL : &diagnostic->verifier))
   {
      unsigned int tc_pc = diagnostic == NULL ? TCIR_TCPC_NONE : diagnostic->verifier.tc_pc;
      tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_VERIFICATION_FAILED, function, tc_pc,
                           "TCIR verification failed before portable-C generation");
      return TCIR_AOT_GENERATE_VERIFICATION_FAILED;
   }
   if (tcirFunctionReturnType(function) != TCIR_TYPE_I32 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_I64 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_F64 &&
       tcirFunctionReturnType(function) != TCIR_TYPE_VOID)
   {
      tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function, TCIR_TCPC_NONE,
                           "portable-C baseline supports only i32, i64, f64, and void returns");
      return TCIR_AOT_GENERATE_INELIGIBLE;
   }
   *value_count = 0U;
   *edge_value_count = 0U;
   for (parameter_index = 0U; parameter_index < tcirFunctionParameterCount(function); ++parameter_index)
   {
      const TCIRValue *parameter = tcirFunctionParameter(function, parameter_index);
      if (!tcirAotTypeIsSupported(tcirValueType(parameter)) ||
          !tcirAotUpdateValueCount(parameter, value_count))
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function, TCIR_TCPC_NONE,
                              "portable-C baseline supports only bounded scalar parameters");
         return TCIR_AOT_GENERATE_INELIGIBLE;
      }
   }
   for (block_index = 0U; block_index < tcirFunctionBlockCount(function); ++block_index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(function, block_index);
      TCIRTerminatorView terminator;
      size_t index;
      if (tcirBlockIsExceptionHandler(block))
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TERMINATOR, function,
                              tcirBlockSource(block).tc_pc,
                              "portable-C baseline does not emit exception-handler blocks");
         return TCIR_AOT_GENERATE_INELIGIBLE;
      }
      if (tcirBlockArgumentCount(block) > *edge_value_count)
         *edge_value_count = tcirBlockArgumentCount(block);
      for (index = 0U; index < tcirBlockArgumentCount(block); ++index)
      {
         const TCIRValue *argument = tcirBlockArgumentAt(block, index);
         if (!tcirAotTypeIsSupported(tcirValueType(argument)) ||
             !tcirAotUpdateValueCount(argument, value_count))
         {
            tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function,
                                 tcirBlockSource(block).tc_pc,
                                 "portable-C baseline supports only bounded scalar block arguments");
            return TCIR_AOT_GENERATE_INELIGIBLE;
         }
      }
      for (index = 0U; index < tcirBlockOperationCount(block); ++index)
      {
         TCIROperationView operation;
         size_t operand;
         if (tcirBlockOperationAt(block, index, &operation) != TCIR_STATUS_OK
             || !tcirAotOperationIsEligible(&operation))
         {
            unsigned int tc_pc = tcirBlockSource(block).tc_pc;
            if (tcirBlockOperationAt(block, index, &operation) == TCIR_STATUS_OK)
               tc_pc = operation.source.tc_pc;
            tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_OPERATION,
                                 function, tc_pc,
                                 "function contains an operation unsupported by the portable-C baseline");
            return TCIR_AOT_GENERATE_INELIGIBLE;
         }
         if (!tcirAotUpdateValueCount(operation.result, value_count))
         {
            tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function,
                                 operation.source.tc_pc, "portable-C value table is too large");
            return TCIR_AOT_GENERATE_INELIGIBLE;
         }
         for (operand = 0U; operand < operation.operand_count; ++operand)
            if (!tcirAotTypeIsSupported(tcirValueType(operation.operands[operand])))
            {
               tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function,
                                    operation.source.tc_pc,
                                    "portable-C operation has an unsupported operand");
               return TCIR_AOT_GENERATE_INELIGIBLE;
            }
      }
      if (tcirBlockTerminator(block, &terminator) != TCIR_STATUS_OK
          || (terminator.kind != TCIR_TERMINATOR_BRANCH
              && terminator.kind != TCIR_TERMINATOR_BRANCH_IF
              && terminator.kind != TCIR_TERMINATOR_RETURN))
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TERMINATOR, function,
                              tcirBlockSource(block).tc_pc,
                              "function contains a terminator unsupported by the portable-C baseline");
         return TCIR_AOT_GENERATE_INELIGIBLE;
      }
      if (terminator.value != NULL && !tcirAotTypeIsSupported(tcirValueType(terminator.value)))
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function,
                              terminator.source.tc_pc,
                              "portable-C terminator has an unsupported value");
         return TCIR_AOT_GENERATE_INELIGIBLE;
      }
      for (index = 0U; index < terminator.edge_count; ++index)
      {
         size_t argument;
         for (argument = 0U; argument < terminator.edges[index].argument_count; ++argument)
            if (!tcirAotTypeIsSupported(tcirValueType(terminator.edges[index].arguments[argument])))
            {
               tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE, function,
                                    terminator.source.tc_pc,
                                    "portable-C edge has an unsupported argument");
               return TCIR_AOT_GENERATE_INELIGIBLE;
            }
      }
   }
   return TCIR_AOT_GENERATE_READY;
}

static int tcirAotParseIdentity(TCIRAotMethodInfo *info)
{
   const char *colon = strrchr(info->identity, ':');
   const char *separator;
   if (colon == NULL || colon == info->identity || colon[1] == '\0')
      return 0;
   separator = colon;
   while (separator > info->identity && separator[-1] != '.')
      --separator;
   if (separator == info->identity || separator == colon)
      return 0;
   info->class_name = tcirAotDuplicateRange(info->identity, (size_t)(separator - info->identity - 1));
   info->method_name = tcirAotDuplicateRange(separator, (size_t)(colon - separator));
   info->signature = tcirAotDuplicateRange(colon + 1, strlen(colon + 1));
   return info->class_name != NULL && info->method_name != NULL && info->signature != NULL;
}

static int tcirAotCompareMethods(const void *left, const void *right)
{
   const TCIRAotMethodInfo *left_info = (const TCIRAotMethodInfo *)left;
   const TCIRAotMethodInfo *right_info = (const TCIRAotMethodInfo *)right;
   return strcmp(left_info->identity, right_info->identity);
}

static void tcirAotFreeMethods(TCIRAotMethodInfo *methods, size_t count)
{
   size_t index;
   if (methods == NULL)
      return;
   for (index = 0U; index < count; ++index)
   {
      free(methods[index].class_name);
      free(methods[index].method_name);
      free(methods[index].signature);
      free(methods[index].symbol);
   }
   free(methods);
}

static size_t tcirAotBlockIndex(const TCIRFunction *function, const TCIRBlock *block)
{
   size_t index;
   for (index = 0U; index < tcirFunctionBlockCount(function); ++index)
      if (tcirFunctionBlockAt(function, index) == block)
         return index;
   return SIZE_MAX;
}

static const char *tcirAotValueArray(TCIRType type)
{
   if (type == TCIR_TYPE_I64)
      return "v64_values";
   if (type == TCIR_TYPE_F64)
      return "f64_values";
   return "values";
}

static const char *tcirAotEdgeValueArray(TCIRType type)
{
   if (type == TCIR_TYPE_I64)
      return "v64_edge_values";
   if (type == TCIR_TYPE_F64)
      return "f64_edge_values";
   return "edge_values";
}

static int tcirAotEmitEdge(
   TCIRAotBuffer *source,
   const TCIRFunction *function,
   const TCIREdge *edge,
   const char *indent)
{
   size_t index;
   size_t target = tcirAotBlockIndex(function, edge->target);
   if (target == SIZE_MAX)
      return 0;
   for (index = 0U; index < edge->argument_count; ++index)
   {
      TCIRType type = tcirValueType(edge->arguments[index]);
      const char *values = tcirAotValueArray(type);
      const char *edge_values = tcirAotEdgeValueArray(type);
      if (!tcirAotBufferAppendFormat(source, "%s%s[%lu] = %s[%u];\n", indent,
                                     edge_values, (unsigned long)index, values,
                                     tcirValueId(edge->arguments[index])))
         return 0;
   }
   for (index = 0U; index < edge->argument_count; ++index)
   {
      const TCIRValue *argument = tcirBlockArgumentAt(edge->target, index);
      TCIRType type = tcirValueType(argument);
      const char *values = tcirAotValueArray(type);
      const char *edge_values = tcirAotEdgeValueArray(type);
      if (!tcirAotBufferAppendFormat(source, "%s%s[%u] = %s[%lu];\n", indent,
                                     values, tcirValueId(argument), edge_values,
                                     (unsigned long)index))
         return 0;
   }
   return tcirAotBufferAppendFormat(source, "%sblock = %luU;\n%scontinue;\n", indent,
                                    (unsigned long)target, indent);
}

static int tcirAotEmitOperation(TCIRAotBuffer *source, const TCIROperationView *operation)
{
   unsigned int result = operation->result == NULL ? 0U : tcirValueId(operation->result);
   unsigned int left = operation->operand_count > 0U ? tcirValueId(operation->operands[0]) : 0U;
   unsigned int right = operation->operand_count > 1U ? tcirValueId(operation->operands[1]) : 0U;
   if (!tcirAotBufferAppendFormat(source, "         frame->tc_pc = %uU;\n", operation->source.tc_pc))
      return 0;
   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32(UINT32_C(%u));\n",
            result, (unsigned int)(uint32_t)operation->immediate_i32);
      case TCIR_OP_COPY:
         if (operation->result_type == TCIR_TYPE_I64)
            return tcirAotBufferAppendFormat(
               source, "         v64_values[%u] = v64_values[%u];\n", result, left);
         if (operation->result_type == TCIR_TYPE_F64)
            return tcirAotBufferAppendFormat(
               source, "         f64_values[%u] = f64_values[%u];\n", result, left);
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u];\n", result, left);
      case TCIR_OP_ADD_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] + (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_SUB_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] - (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_MUL_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] * (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_SHL_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] << ((uint32_t)values[%u] & UINT32_C(31)));\n",
            result, left, right);
      case TCIR_OP_SHR_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_shr_i32(values[%u], values[%u]);\n",
            result, left, right);
      case TCIR_OP_USHR_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] >> ((uint32_t)values[%u] & UINT32_C(31)));\n",
            result, left, right);
      case TCIR_OP_AND_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] & (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_OR_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] | (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_XOR_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] ^ (uint32_t)values[%u]);\n",
            result, left, right);
      case TCIR_OP_TRUNC_I32_I8:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] & UINT32_C(0xff));\n",
            result, left);
      case TCIR_OP_TRUNC_I32_I16:
      case TCIR_OP_ZEXT_I16_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)values[%u] & UINT32_C(0xffff));\n",
            result, left);
      case TCIR_OP_SEXT_I8_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((((uint32_t)values[%u] & UINT32_C(0xff)) ^ UINT32_C(0x80)) - UINT32_C(0x80));\n",
            result, left);
      case TCIR_OP_SEXT_I16_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((((uint32_t)values[%u] & UINT32_C(0xffff)) ^ UINT32_C(0x8000)) - UINT32_C(0x8000));\n",
            result, left);
      case TCIR_OP_CMP_EQ_I32:
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u] == values[%u];\n",
                                          result, left, right);
      case TCIR_OP_CMP_LT_I32:
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u] < values[%u];\n",
                                          result, left, right);
      case TCIR_OP_CMP_LE_I32:
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u] <= values[%u];\n",
                                          result, left, right);
      case TCIR_OP_CMP_GT_I32:
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u] > values[%u];\n",
                                          result, left, right);
      case TCIR_OP_CMP_GE_I32:
         return tcirAotBufferAppendFormat(source, "         values[%u] = values[%u] >= values[%u];\n",
                                          result, left, right);
      case TCIR_OP_CONST_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64(UINT64_C(%llu));\n",
            result, (unsigned long long)(uint64_t)operation->immediate_i64);
      case TCIR_OP_ADD_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] + (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_SUB_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] - (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_MUL_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] * (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_SHL_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] << ((uint64_t)v64_values[%u] & UINT64_C(63)));\n",
            result, left, right);
      case TCIR_OP_SHR_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_shr_i64(v64_values[%u], v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_USHR_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] >> ((uint64_t)v64_values[%u] & UINT64_C(63)));\n",
            result, left, right);
      case TCIR_OP_AND_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] & (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_OR_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] | (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_XOR_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = tc_aot_i64_from_u64((uint64_t)v64_values[%u] ^ (uint64_t)v64_values[%u]);\n",
            result, left, right);
      case TCIR_OP_TRUNC_I64_I32:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = tc_aot_i32_from_u32((uint32_t)(uint64_t)v64_values[%u]);\n",
            result, left);
      case TCIR_OP_SEXT_I32_I64:
         return tcirAotBufferAppendFormat(source,
            "         v64_values[%u] = (int64_t)values[%u];\n", result, left);
      case TCIR_OP_CMP_EQ_I64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = v64_values[%u] == v64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_LT_I64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = v64_values[%u] < v64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_LE_I64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = v64_values[%u] <= v64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_GT_I64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = v64_values[%u] > v64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_GE_I64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = v64_values[%u] >= v64_values[%u];\n", result, left, right);
      case TCIR_OP_CONST_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = tc_aot_f64_from_u64(UINT64_C(%llu));\n",
            result, (unsigned long long)operation->immediate_f64_bits);
      case TCIR_OP_ADD_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = f64_values[%u] + f64_values[%u];\n",
            result, left, right);
      case TCIR_OP_SUB_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = f64_values[%u] - f64_values[%u];\n",
            result, left, right);
      case TCIR_OP_MUL_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = f64_values[%u] * f64_values[%u];\n",
            result, left, right);
      case TCIR_OP_CMP_EQ_F64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = f64_values[%u] == f64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_LT_F64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = f64_values[%u] < f64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_LE_F64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = f64_values[%u] <= f64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_GT_F64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = f64_values[%u] > f64_values[%u];\n", result, left, right);
      case TCIR_OP_CMP_GE_F64:
         return tcirAotBufferAppendFormat(source,
            "         values[%u] = f64_values[%u] >= f64_values[%u];\n", result, left, right);
      case TCIR_OP_I32_TO_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = (double)values[%u];\n", result, left);
      case TCIR_OP_I64_TO_F64:
         return tcirAotBufferAppendFormat(source,
            "         f64_values[%u] = (double)v64_values[%u];\n", result, left);
      case TCIR_OP_LOAD_SLOT:
         if (operation->home_bank == TCIR_HOME_V64)
         {
            if (operation->result_type == TCIR_TYPE_F64)
               return tcirAotBufferAppendFormat(source,
                  "         f64_values[%u] = frame->v64_homes[%u].f64;\n",
                  result, operation->home_index);
            return tcirAotBufferAppendFormat(source,
               "         v64_values[%u] = frame->v64_homes[%u].i64;\n",
               result, operation->home_index);
         }
         return tcirAotBufferAppendFormat(source, "         values[%u] = frame->i32_homes[%u];\n",
                                          result, operation->home_index);
      case TCIR_OP_STORE_SLOT:
         if (operation->home_bank == TCIR_HOME_V64)
         {
            if (tcirValueType(operation->operands[0]) == TCIR_TYPE_F64)
               return tcirAotBufferAppendFormat(source,
                  "         frame->v64_homes[%u].f64 = f64_values[%u];\n",
                  operation->home_index, left);
            return tcirAotBufferAppendFormat(source,
               "         frame->v64_homes[%u].i64 = v64_values[%u];\n",
               operation->home_index, left);
         }
         return tcirAotBufferAppendFormat(source, "         frame->i32_homes[%u] = values[%u];\n",
                                          operation->home_index, left);
      default:
         return 0;
   }
}

static int tcirAotEmitMethod(TCIRAotBuffer *source, const TCIRAotMethodInfo *method)
{
   const TCIRFunction *function = method->function;
   size_t block_index;
   size_t parameter_index;
   size_t values = method->value_count == 0U ? 1U : method->value_count;
   size_t edges = method->edge_value_count == 0U ? 1U : method->edge_value_count;

   if (!tcirAotBufferAppendFormat(source,
      "static TCCompiledStatus %s(TCCompiledFrame *frame, TCCompiledResult *result)\n"
      "{\n"
      "   int32_t values[%lu] = { 0 };\n"
      "   int32_t edge_values[%lu] = { 0 };\n"
      "   int64_t v64_values[%lu] = { 0 };\n"
      "   int64_t v64_edge_values[%lu] = { 0 };\n"
      "   double f64_values[%lu] = { 0 };\n"
      "   double f64_edge_values[%lu] = { 0 };\n"
      "   unsigned int block = 0U;\n\n"
      "   (void)edge_values;\n"
      "   (void)v64_edge_values;\n"
      "   (void)f64_edge_values;\n"
      "   if (result != NULL)\n"
      "   {\n"
      "      memset(result, 0, sizeof(*result));\n"
      "      result->status = TC_COMPILED_REJECTED;\n"
      "      result->type = TCIR_TYPE_VOID;\n"
      "      result->tc_pc = TCIR_TCPC_NONE;\n"
      "   }\n"
      "   if (frame == NULL || result == NULL\n"
      "       || frame->argument_count != %luU\n"
      "       || (frame->argument_count != 0U && frame->arguments == NULL)\n"
      "       || tc_aot_count_too_small(frame->i32_home_count, %uU)\n"
      "       || (frame->i32_home_count != 0U && frame->i32_homes == NULL)\n"
      "       || tc_aot_count_too_small(frame->ref_home_count, %uU)\n"
      "       || (frame->ref_home_count != 0U && frame->ref_homes == NULL)\n"
      "       || tc_aot_count_too_small(frame->v64_home_count, %uU)\n"
      "       || (frame->v64_home_count != 0U && frame->v64_homes == NULL))\n"
      "      return TC_COMPILED_REJECTED;\n"
      "   frame->tc_pc = TCIR_TCPC_NONE;\n",
      method->symbol,
      (unsigned long)values,
      (unsigned long)edges,
      (unsigned long)values,
      (unsigned long)edges,
      (unsigned long)values,
      (unsigned long)edges,
      (unsigned long)tcirFunctionParameterCount(function),
      tcirFunctionHomeCount(function, TCIR_HOME_I32),
      tcirFunctionHomeCount(function, TCIR_HOME_REF),
      tcirFunctionHomeCount(function, TCIR_HOME_V64)))
      return 0;
   for (parameter_index = 0U; parameter_index < tcirFunctionParameterCount(function); ++parameter_index)
   {
      const TCIRValue *parameter = tcirFunctionParameter(function, parameter_index);
      TCIRType parameter_type = tcirValueType(parameter);
      const char *values_name = tcirAotValueArray(parameter_type);
      const char *member = parameter_type == TCIR_TYPE_I64 ? "i64" :
         (parameter_type == TCIR_TYPE_F64 ? "f64" : "i32");
      if (!tcirAotBufferAppendFormat(source, "   %s[%u] = frame->arguments[%lu].%s;\n",
                                     values_name, tcirValueId(parameter),
                                     (unsigned long)parameter_index, member))
         return 0;
   }
   if (!tcirAotBufferAppend(source, "   for (;;)\n   {\n      switch (block)\n      {\n"))
      return 0;
   for (block_index = 0U; block_index < tcirFunctionBlockCount(function); ++block_index)
   {
      const TCIRBlock *block = tcirFunctionBlockAt(function, block_index);
      TCIRTerminatorView terminator;
      size_t operation_index;
      if (!tcirAotBufferAppendFormat(source, "         case %luU:\n", (unsigned long)block_index))
         return 0;
      for (operation_index = 0U; operation_index < tcirBlockOperationCount(block); ++operation_index)
      {
         TCIROperationView operation;
         if (tcirBlockOperationAt(block, operation_index, &operation) != TCIR_STATUS_OK
             || !tcirAotEmitOperation(source, &operation))
            return 0;
      }
      if (tcirBlockTerminator(block, &terminator) != TCIR_STATUS_OK
          || !tcirAotBufferAppendFormat(source, "         frame->tc_pc = %uU;\n", terminator.source.tc_pc))
         return 0;
      switch (terminator.kind)
      {
         case TCIR_TERMINATOR_BRANCH:
            if (!tcirAotEmitEdge(source, function, &terminator.edges[0], "         "))
               return 0;
            break;
         case TCIR_TERMINATOR_BRANCH_IF:
            if (!tcirAotBufferAppendFormat(source, "         if (values[%u] != 0)\n         {\n",
                                           tcirValueId(terminator.value))
                || !tcirAotEmitEdge(source, function, &terminator.edges[0], "            ")
                || !tcirAotBufferAppend(source, "         }\n         else\n         {\n")
                || !tcirAotEmitEdge(source, function, &terminator.edges[1], "            ")
                || !tcirAotBufferAppend(source, "         }\n"))
               return 0;
            break;
         case TCIR_TERMINATOR_RETURN:
            if (!tcirAotBufferAppend(source,
               "         result->status = TC_COMPILED_RETURNED;\n"
               "         result->tc_pc = frame->tc_pc;\n"))
               return 0;
            if (terminator.value == NULL)
            {
               if (!tcirAotBufferAppend(source,
                  "         result->type = TCIR_TYPE_VOID;\n"
                  "         return TC_COMPILED_RETURNED;\n"))
                  return 0;
            }
            else if (tcirValueType(terminator.value) == TCIR_TYPE_I64)
            {
               if (!tcirAotBufferAppendFormat(source,
                  "         result->type = TCIR_TYPE_I64;\n"
                  "         result->value.i64 = v64_values[%u];\n"
                  "         return TC_COMPILED_RETURNED;\n", tcirValueId(terminator.value)))
                  return 0;
            }
            else if (tcirValueType(terminator.value) == TCIR_TYPE_F64)
            {
               if (!tcirAotBufferAppendFormat(source,
                  "         result->type = TCIR_TYPE_F64;\n"
                  "         result->value.f64 = f64_values[%u];\n"
                  "         return TC_COMPILED_RETURNED;\n", tcirValueId(terminator.value)))
                  return 0;
            }
            else if (!tcirAotBufferAppendFormat(source,
               "         result->type = TCIR_TYPE_I32;\n"
               "         result->value.i32 = values[%u];\n"
               "         return TC_COMPILED_RETURNED;\n", tcirValueId(terminator.value)))
               return 0;
            break;
         default:
            return 0;
      }
   }
   return tcirAotBufferAppend(source,
      "         default:\n"
      "            return TC_COMPILED_REJECTED;\n"
      "      }\n"
      "   }\n"
      "}\n\n");
}

static int tcirAotEmitHeader(TCIRAotBuffer *header, uint64_t input_hash)
{
   return tcirAotBufferAppendFormat(header,
      "// Copyright (C) 2026 Amalgam Solucoes em TI Ltda\n"
      "//\n"
      "// SPDX-License-Identifier: LGPL-2.1-only\n\n"
      "#ifndef TCIR_AOT_GENERATED_%016llX_H\n"
      "#define TCIR_AOT_GENERATED_%016llX_H\n\n"
      "#include \"tcir_aot.h\"\n\n"
      "extern const TCIRAotRegistryEntry tcir_aot_generated_registry[];\n"
      "extern const size_t tcir_aot_generated_registry_count;\n\n"
      "#endif\n",
      (unsigned long long)input_hash,
      (unsigned long long)input_hash);
}

static int tcirAotEmitSource(
   TCIRAotBuffer *source,
   const TCIRAotMethodInfo *methods,
   size_t function_count)
{
   size_t index;
   if (!tcirAotBufferAppend(source,
      "// Copyright (C) 2026 Amalgam Solucoes em TI Ltda\n"
      "//\n"
      "// SPDX-License-Identifier: LGPL-2.1-only\n\n"
      "#include \"tcir_aot_generated.h\"\n\n"
      "#include <limits.h>\n"
      "#include <stdint.h>\n"
      "#include <string.h>\n\n"
      "static int32_t tc_aot_i32_from_u32(uint32_t bits)\n"
      "{\n"
      "   if (bits <= (uint32_t)INT32_MAX)\n"
      "      return (int32_t)bits;\n"
      "   return (int32_t)(-1 - (int32_t)(UINT32_MAX - bits));\n"
      "}\n\n"
      "static int32_t tc_aot_shr_i32(int32_t value, int32_t distance)\n"
      "{\n"
      "   unsigned int shift = (unsigned int)((uint32_t)distance & UINT32_C(31));\n"
      "   uint32_t bits = (uint32_t)value;\n"
      "   if (shift == 0U || value >= 0)\n"
      "      return tc_aot_i32_from_u32(bits >> shift);\n"
      "   return tc_aot_i32_from_u32((bits >> shift) | (UINT32_MAX << (32U - shift)));\n"
      "}\n\n"
      "static int64_t tc_aot_i64_from_u64(uint64_t bits)\n"
      "{\n"
      "   if (bits <= (uint64_t)INT64_MAX)\n"
      "      return (int64_t)bits;\n"
      "   return (int64_t)(-1 - (int64_t)(UINT64_MAX - bits));\n"
      "}\n\n"
      "static double tc_aot_f64_from_u64(uint64_t bits)\n"
      "{\n"
      "   double value;\n"
      "   memcpy(&value, &bits, sizeof(value));\n"
      "   return value;\n"
      "}\n\n"
      "static int64_t tc_aot_shr_i64(int64_t value, int64_t distance)\n"
      "{\n"
      "   unsigned int shift = (unsigned int)((uint64_t)distance & UINT64_C(63));\n"
      "   uint64_t bits = (uint64_t)value;\n"
      "   if (shift == 0U || value >= 0)\n"
      "      return tc_aot_i64_from_u64(bits >> shift);\n"
      "   return tc_aot_i64_from_u64((bits >> shift) | (UINT64_MAX << (64U - shift)));\n"
      "}\n\n"
      "static int tc_aot_count_too_small(size_t actual, size_t required)\n"
      "{\n"
      "   return actual < required;\n"
      "}\n\n"))
      return 0;
   for (index = 0U; index < function_count; ++index)
      if (!tcirAotEmitMethod(source, &methods[index]))
         return 0;
   if (!tcirAotBufferAppend(source,
      "const TCIRAotRegistryEntry tcir_aot_generated_registry[] = {\n"))
      return 0;
   for (index = 0U; index < function_count; ++index)
   {
      const TCIRAotMethodInfo *method = &methods[index];
      if (!tcirAotBufferAppend(source, "   { ")
          || !tcirAotAppendJsonString(source, method->class_name)
          || !tcirAotBufferAppend(source, ", ")
          || !tcirAotAppendJsonString(source, method->method_name)
          || !tcirAotBufferAppend(source, ", ")
          || !tcirAotAppendJsonString(source, method->signature)
          || !tcirAotBufferAppendFormat(source, ", \"%016llx\", %s }%s\n",
               (unsigned long long)method->content_hash,
               method->symbol,
               index + 1U == function_count ? "" : ","))
         return 0;
   }
   return tcirAotBufferAppendFormat(source,
      "};\n\nconst size_t tcir_aot_generated_registry_count = %luU;\n",
      (unsigned long)function_count);
}

static int tcirAotEmitManifest(
   TCIRAotBuffer *manifest,
   const TCIRAotMethodInfo *methods,
   size_t function_count,
   const char *target_options,
   uint64_t input_hash)
{
   size_t index;
   if (!tcirAotBufferAppendFormat(manifest,
      "{\n  \"schema_version\":%u,\n  \"generator\":\"tcir-portable-c\",\n"
      "  \"generator_version\":%u,\n  \"ir_version\":%u,\n"
      "  \"runtime_abi_version\":%u,\n  \"input_hash_algorithm\":\"fnv1a64\",\n"
      "  \"input_hash\":\"%016llx\",\n  \"target_options\":",
      TC_AOT_MANIFEST_SCHEMA_VERSION,
      TC_AOT_GENERATOR_VERSION,
      TC_IR_VERSION,
      TC_RUNTIME_ABI_VERSION,
      (unsigned long long)input_hash)
       || !tcirAotAppendJsonString(manifest, target_options)
       || !tcirAotBufferAppend(manifest, ",\n  \"source\":\"tcir_aot_generated.c\",\n"
                                      "  \"header\":\"tcir_aot_generated.h\",\n"
                                      "  \"supported_methods\":[\n"))
      return 0;
   for (index = 0U; index < function_count; ++index)
   {
      const TCIRAotMethodInfo *method = &methods[index];
      if (!tcirAotBufferAppend(manifest, "    {\"identity\":")
          || !tcirAotAppendJsonString(manifest, method->identity)
          || !tcirAotBufferAppend(manifest, ",\"class\":")
          || !tcirAotAppendJsonString(manifest, method->class_name)
          || !tcirAotBufferAppend(manifest, ",\"method\":")
          || !tcirAotAppendJsonString(manifest, method->method_name)
          || !tcirAotBufferAppend(manifest, ",\"signature\":")
          || !tcirAotAppendJsonString(manifest, method->signature)
          || !tcirAotBufferAppendFormat(manifest,
               ",\"content_hash\":\"%016llx\",\"symbol\":",
               (unsigned long long)method->content_hash)
          || !tcirAotAppendJsonString(manifest, method->symbol)
          || !tcirAotBufferAppendFormat(manifest,
               ",\"diagnostic\":\"none\"}%s\n",
               index + 1U == function_count ? "" : ","))
         return 0;
   }
   return tcirAotBufferAppend(manifest, "  ],\n  \"rejected_methods\":[]\n}\n");
}

TCIRAotGenerateStatus tcirAotGenerate(
   const TCIRFunction *const *functions,
   size_t function_count,
   const TCIRAotGenerateOptions *options,
   TCIRAotOutput *output,
   TCIRAotDiagnostic *diagnostic)
{
   TCIRAotMethodInfo *methods = NULL;
   TCIRAotBuffer source;
   TCIRAotBuffer header;
   TCIRAotBuffer manifest;
   const char *target_options = options == NULL || options->target_options == NULL
      ? "portable-c11" : options->target_options;
   uint64_t input_hash = TCIR_AOT_FNV_OFFSET;
   size_t index;
   TCIRAotGenerateStatus status = TCIR_AOT_GENERATE_READY;

   memset(&source, 0, sizeof(source));
   memset(&header, 0, sizeof(header));
   memset(&manifest, 0, sizeof(manifest));
   tcirAotDiagnosticClear(diagnostic);
   if (output != NULL)
      memset(output, 0, sizeof(*output));
   if (functions == NULL || function_count == 0U || output == NULL)
   {
      tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INVALID_ARGUMENT, NULL, TCIR_TCPC_NONE,
                           "portable-C generation requires functions and an output");
      return TCIR_AOT_GENERATE_INELIGIBLE;
   }
   methods = (TCIRAotMethodInfo *)calloc(function_count, sizeof(*methods));
   if (methods == NULL)
      goto out_of_memory;
   for (index = 0U; index < function_count; ++index)
   {
      TCIRAotMethodInfo *method = &methods[index];
      if (functions[index] == NULL)
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INVALID_ARGUMENT, NULL, TCIR_TCPC_NONE,
                              "portable-C generation received a null function");
         status = TCIR_AOT_GENERATE_INELIGIBLE;
         goto failure;
      }
      method->function = functions[index];
      method->identity = tcirFunctionIdentity(functions[index]);
      status = tcirAotCheckEligibility(functions[index], &method->value_count,
                                       &method->edge_value_count, diagnostic);
      if (status != TCIR_AOT_GENERATE_READY)
         goto failure;
      method->content_hash = tcirAotFunctionHash(functions[index]);
      if (!tcirAotParseIdentity(method))
      {
         if (method->class_name == NULL || method->method_name == NULL || method->signature == NULL)
         {
            if (strchr(method->identity, ':') != NULL && strchr(method->identity, '.') != NULL)
               goto out_of_memory;
         }
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_INVALID_IDENTITY, functions[index],
                              TCIR_TCPC_NONE,
                              "method identity must use class.method:signature");
         status = TCIR_AOT_GENERATE_INELIGIBLE;
         goto failure;
      }
      method->symbol = tcirAotMakeSymbol(method->identity, method->content_hash);
      if (method->symbol == NULL)
         goto out_of_memory;
   }
   qsort(methods, function_count, sizeof(*methods), tcirAotCompareMethods);
   for (index = 1U; index < function_count; ++index)
      if (strcmp(methods[index - 1U].identity, methods[index].identity) == 0)
      {
         tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_DUPLICATE_IDENTITY,
                              methods[index].function, TCIR_TCPC_NONE,
                              "portable-C input contains a duplicate method identity");
         status = TCIR_AOT_GENERATE_INELIGIBLE;
         goto failure;
      }
   input_hash = tcirAotHashU64(input_hash, TC_AOT_GENERATOR_VERSION);
   input_hash = tcirAotHashU64(input_hash, TC_IR_VERSION);
   input_hash = tcirAotHashU64(input_hash, TC_RUNTIME_ABI_VERSION);
   input_hash = tcirAotHashU64(input_hash, (uint64_t)function_count);
   for (index = 0U; index < function_count; ++index)
      input_hash = tcirAotHashU64(input_hash, methods[index].content_hash);

   if (!tcirAotEmitHeader(&header, input_hash)
       || !tcirAotEmitSource(&source, methods, function_count)
       || !tcirAotEmitManifest(&manifest, methods, function_count, target_options, input_hash))
      goto out_of_memory;
   output->source = source.data;
   output->source_size = source.size;
   output->header = header.data;
   output->header_size = header.size;
   output->manifest = manifest.data;
   output->manifest_size = manifest.size;
   snprintf(output->input_hash, sizeof(output->input_hash), "%016llx", (unsigned long long)input_hash);
   tcirAotFreeMethods(methods, function_count);
   return TCIR_AOT_GENERATE_READY;

out_of_memory:
   tcirAotSetDiagnostic(diagnostic, TCIR_AOT_DIAGNOSTIC_OUT_OF_MEMORY, NULL, TCIR_TCPC_NONE,
                        "unable to allocate portable-C generator output");
   status = TCIR_AOT_GENERATE_OUT_OF_MEMORY;
failure:
   free(source.data);
   free(header.data);
   free(manifest.data);
   tcirAotFreeMethods(methods, function_count);
   return status;
}

void tcirAotOutputDestroy(TCIRAotOutput *output)
{
   if (output != NULL)
   {
      free(output->source);
      free(output->header);
      free(output->manifest);
      memset(output, 0, sizeof(*output));
   }
}

const TCIRAotRegistryEntry *tcirAotRegistryFind(
   const TCIRAotRegistryEntry *entries,
   size_t entry_count,
   const char *class_name,
   const char *method_name,
   const char *signature,
   const char *content_hash)
{
   size_t index;
   if (entries == NULL || class_name == NULL || method_name == NULL
       || signature == NULL || content_hash == NULL)
      return NULL;
   for (index = 0U; index < entry_count; ++index)
      if (entries[index].entry != NULL
          && strcmp(entries[index].class_name, class_name) == 0
          && strcmp(entries[index].method_name, method_name) == 0
          && strcmp(entries[index].signature, signature) == 0
          && strcmp(entries[index].content_hash, content_hash) == 0)
         return &entries[index];
   return NULL;
}
