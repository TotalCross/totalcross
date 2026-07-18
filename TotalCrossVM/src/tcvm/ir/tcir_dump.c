// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_internal.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if defined(_MSC_VER) && _MSC_VER < 1900
#define vsnprintf _vsnprintf
#endif

typedef struct TCIRTextBuffer
{
   const TCIRModule *module;
   char *data;
   size_t length;
   size_t capacity;
   int failed;
} TCIRTextBuffer;

static int tcirBufferReserve(TCIRTextBuffer *buffer, size_t additional)
{
   size_t required;

   if (buffer->failed)
      return 0;
   if (additional > (size_t)-1 - buffer->length - 1)
   {
      buffer->failed = 1;
      return 0;
   }
   required = buffer->length + additional + 1;
   if (!tcirGrowArray(buffer->module, (void **)&buffer->data, &buffer->capacity, sizeof(char), required))
   {
      buffer->failed = 1;
      return 0;
   }
   return 1;
}

static void tcirBufferAppend(TCIRTextBuffer *buffer, const char *text)
{
   size_t length;

   if (text == NULL)
      text = "";
   length = strlen(text);
   if (!tcirBufferReserve(buffer, length))
      return;
   memcpy(buffer->data + buffer->length, text, length);
   buffer->length += length;
   buffer->data[buffer->length] = '\0';
}

static void tcirBufferAppendFormat(TCIRTextBuffer *buffer, const char *format, ...)
{
   char formatted[512];
   va_list arguments;
   int required;

   if (buffer->failed)
      return;
   va_start(arguments, format);
   required = vsnprintf(formatted, sizeof(formatted), format, arguments);
   va_end(arguments);
   if (required < 0 || (size_t)required >= sizeof(formatted) ||
       !tcirBufferReserve(buffer, (size_t)required))
   {
      buffer->failed = 1;
      return;
   }
   memcpy(buffer->data + buffer->length, formatted, (size_t)required);
   buffer->length += (size_t)required;
   buffer->data[buffer->length] = '\0';
}

static void tcirBufferAppendEscaped(TCIRTextBuffer *buffer, const char *text)
{
   const unsigned char *cursor = (const unsigned char *)text;

   while (*cursor != 0)
   {
      switch (*cursor)
      {
         case '\\': tcirBufferAppend(buffer, "\\\\"); break;
         case '"': tcirBufferAppend(buffer, "\\\""); break;
         case '\n': tcirBufferAppend(buffer, "\\n"); break;
         case '\r': tcirBufferAppend(buffer, "\\r"); break;
         case '\t': tcirBufferAppend(buffer, "\\t"); break;
         default:
            if (*cursor < 0x20 || *cursor >= 0x7f)
               tcirBufferAppendFormat(buffer, "\\x%02x", (unsigned int)*cursor);
            else
            {
               char character[2];
               character[0] = (char)*cursor;
               character[1] = '\0';
               tcirBufferAppend(buffer, character);
            }
            break;
      }
      cursor++;
   }
}

static void tcirBufferAppendQuoted(TCIRTextBuffer *buffer, const char *text)
{
   tcirBufferAppend(buffer, "\"");
   tcirBufferAppendEscaped(buffer, text);
   tcirBufferAppend(buffer, "\"");
}

static void tcirBufferAppendValue(TCIRTextBuffer *buffer, const TCIRValue *value)
{
   if (value == NULL)
      tcirBufferAppend(buffer, "<null>");
   else
      tcirBufferAppendFormat(buffer, "%%v%u", value->id);
}

static void tcirBufferAppendSource(TCIRTextBuffer *buffer, TCIRSourceLocation source)
{
   tcirBufferAppendFormat(buffer, " ; tcpc=%u", source.tc_pc);
   if (source.source_line >= 0)
      tcirBufferAppendFormat(buffer, " line=%d", source.source_line);
}

static const char *tcirHomeBankName(TCIRHomeBank bank)
{
   static const char *const names[] = { "i32", "ref", "v64" };
   return (unsigned int)bank < 3U ? names[(unsigned int)bank] : "invalid";
}

static void tcirBufferAppendSymbol(TCIRTextBuffer *buffer, const TCIRSymbol *symbol)
{
   if (symbol == NULL)
   {
      tcirBufferAppend(buffer, "<no-symbol>");
      return;
   }
   tcirBufferAppend(buffer, "@");
   tcirBufferAppend(buffer, "\"");
   tcirBufferAppendEscaped(buffer, symbol->owner);
   if (symbol->owner[0] != '\0')
      tcirBufferAppend(buffer, ".");
   tcirBufferAppendEscaped(buffer, symbol->name);
   tcirBufferAppend(buffer, ":");
   tcirBufferAppendEscaped(buffer, symbol->descriptor);
   tcirBufferAppendFormat(buffer, "#%u\"", symbol->constant_pool_index);
}

static void tcirBufferAppendEffects(TCIRTextBuffer *buffer, unsigned int effects)
{
   static const unsigned int ordered_effects[] = {
      TCIR_EFFECT_READS_HEAP,
      TCIR_EFFECT_WRITES_HEAP,
      TCIR_EFFECT_MAY_THROW,
      TCIR_EFFECT_MAY_GC,
      TCIR_EFFECT_MAY_LOCK,
      TCIR_EFFECT_RESOLVES_SYMBOL,
      TCIR_EFFECT_CALLS_UNKNOWN
   };
   size_t index;
   int first = 1;

   if (effects == TCIR_EFFECT_NONE)
      return;
   tcirBufferAppend(buffer, " effects[");
   for (index = 0; index < sizeof(ordered_effects) / sizeof(ordered_effects[0]); index++)
   {
      if ((effects & ordered_effects[index]) == 0)
         continue;
      if (!first)
         tcirBufferAppend(buffer, ",");
      tcirBufferAppend(buffer, tcirEffectName(ordered_effects[index]));
      first = 0;
   }
   tcirBufferAppend(buffer, "]");
}

static void tcirBufferAppendGCHomes(TCIRTextBuffer *buffer, const TCIROperationData *operation)
{
   size_t index;

   if (operation->gc_home_count == 0)
      return;
   tcirBufferAppend(buffer, " gc[");
   for (index = 0; index < operation->gc_home_count; index++)
   {
      if (index != 0)
         tcirBufferAppend(buffer, ",");
      tcirBufferAppendValue(buffer, operation->gc_homes[index].value);
      tcirBufferAppendFormat(buffer, "@ref%u", operation->gc_homes[index].home_index);
   }
   tcirBufferAppend(buffer, "]");
}

static void tcirDumpOperation(TCIRTextBuffer *buffer, const TCIROperationData *operation)
{
   size_t operand_index;

   tcirBufferAppend(buffer, "  ");
   if (operation->result != NULL)
   {
      tcirBufferAppendValue(buffer, operation->result);
      tcirBufferAppend(buffer, " = ");
   }
   tcirBufferAppend(buffer, tcirOperationName(operation->opcode));

   switch (operation->opcode)
   {
      case TCIR_OP_CONST_I32:
         tcirBufferAppendFormat(buffer, " %d", operation->immediate_i32);
         break;
      case TCIR_OP_CONST_I64:
         tcirBufferAppendFormat(buffer, " %lld", (long long)operation->immediate_i64);
         break;
      case TCIR_OP_CONST_F64:
         tcirBufferAppendFormat(
            buffer, " 0x%016llx", (unsigned long long)operation->immediate_f64_bits);
         break;
      case TCIR_OP_LOAD_SLOT:
      case TCIR_OP_STORE_SLOT:
         tcirBufferAppendFormat(
            buffer,
            ".%s %u",
            tcirHomeBankName(operation->home_bank),
            operation->home_index);
         break;
      case TCIR_OP_FIELD_LOAD:
      case TCIR_OP_FIELD_STORE:
      case TCIR_OP_METHOD_CALL:
      case TCIR_OP_RUNTIME_CALL:
         tcirBufferAppend(buffer, " ");
         tcirBufferAppendSymbol(buffer, operation->symbol);
         break;
      default:
         break;
   }

   for (operand_index = 0; operand_index < operation->operand_count; operand_index++)
   {
      tcirBufferAppend(buffer, operand_index == 0 && operation->opcode != TCIR_OP_CONST_I32 ? " " : ", ");
      tcirBufferAppendValue(buffer, operation->operands[operand_index]);
   }
   if (operation->result != NULL && operation->opcode != TCIR_OP_CONST_I32)
      tcirBufferAppendFormat(buffer, " -> %s", tcirTypeName(operation->result_type));
   tcirBufferAppendEffects(buffer, operation->effects);
   tcirBufferAppendGCHomes(buffer, operation);
   if (operation->exception_target != NULL)
      tcirBufferAppendFormat(buffer, " exception bb%u", operation->exception_target->id);
   else if (operation->propagates_exception)
      tcirBufferAppend(buffer, " exception propagate");
   tcirBufferAppendSource(buffer, operation->source);
   tcirBufferAppend(buffer, "\n");
}

static void tcirDumpEdgeArguments(TCIRTextBuffer *buffer, const TCIREdge *edge)
{
   size_t argument_index;

   tcirBufferAppendFormat(buffer, "bb%u(", edge->target == NULL ? 0U : edge->target->id);
   for (argument_index = 0; argument_index < edge->argument_count; argument_index++)
   {
      if (argument_index != 0)
         tcirBufferAppend(buffer, ", ");
      tcirBufferAppendValue(buffer, edge->arguments[argument_index]);
   }
   tcirBufferAppend(buffer, ")");
}

static void tcirDumpTerminator(TCIRTextBuffer *buffer, const TCIRBlock *block)
{
   const TCIRTerminatorView *terminator = &block->terminator;
   size_t edge_index;

   tcirBufferAppend(buffer, "  ");
   tcirBufferAppend(buffer, tcirTerminatorName(terminator->kind));
   switch (terminator->kind)
   {
      case TCIR_TERMINATOR_BRANCH:
         if (terminator->edge_count != 0)
         {
            tcirBufferAppend(buffer, " ");
            tcirDumpEdgeArguments(buffer, &terminator->edges[0]);
         }
         break;
      case TCIR_TERMINATOR_BRANCH_IF:
         tcirBufferAppend(buffer, " ");
         tcirBufferAppendValue(buffer, terminator->value);
         for (edge_index = 0; edge_index < terminator->edge_count; edge_index++)
         {
            tcirBufferAppend(buffer, edge_index == 0 ? ", " : ", ");
            tcirDumpEdgeArguments(buffer, &terminator->edges[edge_index]);
         }
         break;
      case TCIR_TERMINATOR_SWITCH:
         tcirBufferAppend(buffer, " ");
         tcirBufferAppendValue(buffer, terminator->value);
         for (edge_index = 0; edge_index < terminator->edge_count; edge_index++)
         {
            tcirBufferAppend(buffer, ", ");
            if (terminator->edges[edge_index].has_case_value)
               tcirBufferAppendFormat(buffer, "%d: ", terminator->edges[edge_index].case_value);
            else
               tcirBufferAppend(buffer, "default: ");
            tcirDumpEdgeArguments(buffer, &terminator->edges[edge_index]);
         }
         break;
      case TCIR_TERMINATOR_RETURN:
      case TCIR_TERMINATOR_THROW:
         if (terminator->value != NULL)
         {
            tcirBufferAppend(buffer, " ");
            tcirBufferAppendValue(buffer, terminator->value);
         }
         break;
      case TCIR_TERMINATOR_UNREACHABLE:
         break;
   }
   tcirBufferAppendSource(buffer, terminator->source);
   tcirBufferAppend(buffer, "\n");
}

static int tcirCompareBlocks(const void *left, const void *right)
{
   const TCIRBlock *left_block = *(const TCIRBlock *const *)left;
   const TCIRBlock *right_block = *(const TCIRBlock *const *)right;
   if (left_block->id < right_block->id)
      return -1;
   return left_block->id > right_block->id ? 1 : 0;
}

char *tcirFunctionDump(const TCIRFunction *function, TCIRDiagnostic *diagnostic)
{
   TCIRTextBuffer buffer;
   TCIRBlock **ordered_blocks;
   size_t index;

   tcirDiagnosticClear(diagnostic);
   if (function == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<function>", 0, "function is null");
      return NULL;
   }

   memset(&buffer, 0, sizeof(buffer));
   buffer.module = function->module;
   ordered_blocks = (TCIRBlock **)tcirAllocate(function->module, function->block_count * sizeof(TCIRBlock *));
   if (ordered_blocks == NULL && function->block_count != 0)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         0,
         "unable to allocate canonical block ordering");
      return NULL;
   }
   if (function->block_count != 0)
   {
      memcpy(ordered_blocks, function->blocks, function->block_count * sizeof(TCIRBlock *));
      qsort(ordered_blocks, function->block_count, sizeof(TCIRBlock *), tcirCompareBlocks);
   }

   tcirBufferAppendFormat(&buffer, "tcir %u\nfunc @", TC_IR_VERSION);
   tcirBufferAppendQuoted(&buffer, function->identity);
   tcirBufferAppend(&buffer, "(");
   for (index = 0; index < function->parameter_count; index++)
   {
      if (index != 0)
         tcirBufferAppend(&buffer, ", ");
      tcirBufferAppendValue(&buffer, function->parameters[index]);
      tcirBufferAppendFormat(&buffer, ": %s", tcirTypeName(function->parameters[index]->type));
   }
   tcirBufferAppendFormat(&buffer, ") -> %s\n", tcirTypeName(function->return_type));
   tcirBufferAppendFormat(
      &buffer,
      "  homes i32 %u, ref %u, v64 %u\n",
      function->home_counts[TCIR_HOME_I32],
      function->home_counts[TCIR_HOME_REF],
      function->home_counts[TCIR_HOME_V64]);

   for (index = 0; index < function->block_count; index++)
   {
      const TCIRBlock *block = ordered_blocks[index];
      size_t argument_index;
      size_t operation_index;

      tcirBufferAppendFormat(&buffer, "bb%u(", block->id);
      for (argument_index = 0; argument_index < block->argument_count; argument_index++)
      {
         if (argument_index != 0)
            tcirBufferAppend(&buffer, ", ");
         tcirBufferAppendValue(&buffer, block->arguments[argument_index]);
         tcirBufferAppendFormat(&buffer, ": %s", tcirTypeName(block->arguments[argument_index]->type));
      }
      tcirBufferAppend(&buffer, ")");
      if (block->is_exception_handler)
         tcirBufferAppend(&buffer, " handler");
      tcirBufferAppendSource(&buffer, block->source);
      tcirBufferAppend(&buffer, "\n");
      for (operation_index = 0; operation_index < block->operation_count; operation_index++)
         tcirDumpOperation(&buffer, &block->operations[operation_index]);
      if (block->has_terminator)
         tcirDumpTerminator(&buffer, block);
      else
         tcirBufferAppend(&buffer, "  <missing terminator>\n");
   }
   tcirBufferAppend(&buffer, "end\n");
   tcirFree(function->module, ordered_blocks);

   if (buffer.failed)
   {
      tcirFree(function->module, buffer.data);
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         0,
         "unable to allocate the canonical TCIR dump");
      return NULL;
   }
   return buffer.data;
}
