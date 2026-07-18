// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_INTERNAL_H
#define TCIR_INTERNAL_H

#include "tcir.h"

#include <stdarg.h>

typedef enum TCIRValueDefinitionKind
{
   TCIR_VALUE_PARAMETER = 0,
   TCIR_VALUE_BLOCK_ARGUMENT,
   TCIR_VALUE_OPERATION
} TCIRValueDefinitionKind;

typedef struct TCIROperationData
{
   TCIROperation opcode;
   TCIRType result_type;
   TCIRValue *result;
   const TCIRValue **operands;
   size_t operand_count;
   int immediate_i32;
   int64_t immediate_i64;
   uint64_t immediate_f64_bits;
   TCIRHomeBank home_bank;
   unsigned int home_index;
   const TCIRSymbol *symbol;
   unsigned int effects;
   TCIRGCHome *gc_homes;
   size_t gc_home_count;
   TCIRBlock *exception_target;
   int propagates_exception;
   TCIRSourceLocation source;
} TCIROperationData;

struct TCIRValue
{
   TCIRFunction *function;
   TCIRBlock *block;
   TCIRType type;
   unsigned int id;
   TCIRValueDefinitionKind definition_kind;
   size_t definition_index;
   TCIROperationData *definition_operation;
};

struct TCIRSymbol
{
   TCIRModule *module;
   TCIRSymbolKind kind;
   char *owner;
   char *name;
   char *descriptor;
   unsigned int constant_pool_index;
   unsigned int helper_effects;
};

struct TCIRBlock
{
   TCIRFunction *function;
   unsigned int id;
   TCIRSourceLocation source;
   int is_exception_handler;
   TCIRValue **arguments;
   size_t argument_count;
   size_t argument_capacity;
   TCIROperationData *operations;
   size_t operation_count;
   size_t operation_capacity;
   TCIRTerminatorView terminator;
   int has_terminator;
};

struct TCIRFunction
{
   TCIRModule *module;
   char *identity;
   TCIRType return_type;
   TCIRValue **parameters;
   size_t parameter_count;
   TCIRBlock **blocks;
   size_t block_count;
   size_t block_capacity;
   TCIRValue **values;
   size_t value_count;
   size_t value_capacity;
   unsigned int next_value_id;
   unsigned int home_counts[3];
   unsigned char *instruction_starts;
   size_t source_slot_count;
};

struct TCIRModule
{
   TCIRAllocator allocator;
   TCIRFunction **functions;
   size_t function_count;
   size_t function_capacity;
   TCIRSymbol **symbols;
   size_t symbol_count;
   size_t symbol_capacity;
};

void *tcirAllocate(const TCIRModule *module, size_t size);
void *tcirReallocate(const TCIRModule *module, void *pointer, size_t size);
void tcirFree(const TCIRModule *module, void *pointer);
char *tcirDuplicateString(const TCIRModule *module, const char *text);
int tcirGrowArray(const TCIRModule *module, void **items, size_t *capacity, size_t item_size, size_t required);
void tcirSetDiagnostic(
   TCIRDiagnostic *diagnostic,
   TCIRDiagnosticCode code,
   const char *function,
   unsigned int tc_pc,
   const char *format,
   ...);
int tcirSourceIsInstructionStart(const TCIRFunction *function, TCIRSourceLocation source);
int tcirValueBelongsToFunction(const TCIRValue *value, const TCIRFunction *function);
const char *tcirOperationName(TCIROperation operation);
const char *tcirTerminatorName(TCIRTerminatorKind terminator);

#endif
