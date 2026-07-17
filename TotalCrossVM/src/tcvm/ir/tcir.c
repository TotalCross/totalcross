// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_internal.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if defined(_MSC_VER) && _MSC_VER < 1900
#define snprintf _snprintf
#define vsnprintf _vsnprintf
#endif

static void *tcirDefaultAllocate(void *user_data, size_t size)
{
   (void)user_data;
   return malloc(size);
}

static void *tcirDefaultReallocate(void *user_data, void *pointer, size_t size)
{
   (void)user_data;
   return realloc(pointer, size);
}

static void tcirDefaultFree(void *user_data, void *pointer)
{
   (void)user_data;
   free(pointer);
}

static int tcirTypeIsParameter(TCIRType type)
{
   return type >= TCIR_TYPE_I1 && type <= TCIR_TYPE_NON_NULL_REF;
}

static void tcirDestroyBlock(TCIRBlock *block)
{
   TCIRModule *module;
   size_t index;

   if (block == NULL)
      return;

   module = block->function->module;
   for (index = 0; index < block->operation_count; index++)
   {
      tcirFree(module, (void *)block->operations[index].operands);
      tcirFree(module, block->operations[index].gc_homes);
   }
   if (block->has_terminator)
   {
      TCIREdge *edges = (TCIREdge *)block->terminator.edges;
      for (index = 0; index < block->terminator.edge_count; index++)
         tcirFree(module, (void *)edges[index].arguments);
      tcirFree(module, edges);
   }
   tcirFree(module, block->arguments);
   tcirFree(module, block->operations);
   tcirFree(module, block);
}

static void tcirDestroyFunction(TCIRFunction *function)
{
   TCIRModule *module;
   size_t index;

   if (function == NULL)
      return;

   module = function->module;
   for (index = 0; index < function->block_count; index++)
      tcirDestroyBlock(function->blocks[index]);
   for (index = 0; index < function->value_count; index++)
      tcirFree(module, function->values[index]);
   tcirFree(module, function->identity);
   tcirFree(module, function->parameters);
   tcirFree(module, function->blocks);
   tcirFree(module, function->values);
   tcirFree(module, function->instruction_starts);
   tcirFree(module, function);
}

static void tcirDestroySymbol(TCIRSymbol *symbol)
{
   TCIRModule *module;

   if (symbol == NULL)
      return;

   module = symbol->module;
   tcirFree(module, symbol->owner);
   tcirFree(module, symbol->name);
   tcirFree(module, symbol->descriptor);
   tcirFree(module, symbol);
}

static TCIRValue *tcirCreateValue(
   TCIRFunction *function,
   TCIRBlock *block,
   TCIRType type,
   TCIRValueDefinitionKind definition_kind,
   size_t definition_index,
   TCIRDiagnostic *diagnostic)
{
   TCIRValue *value;

   if (!tcirGrowArray(
          function->module,
          (void **)&function->values,
          &function->value_capacity,
          sizeof(TCIRValue *),
          function->value_count + 1))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         block == NULL ? 0U : block->source.tc_pc,
         "unable to allocate a TCIR value");
      return NULL;
   }

   value = (TCIRValue *)tcirAllocate(function->module, sizeof(TCIRValue));
   if (value == NULL)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         block == NULL ? 0U : block->source.tc_pc,
         "unable to allocate a TCIR value");
      return NULL;
   }

   memset(value, 0, sizeof(TCIRValue));
   value->function = function;
   value->block = block;
   value->type = type;
   value->id = function->next_value_id++;
   value->definition_kind = definition_kind;
   value->definition_index = definition_index;
   function->values[function->value_count++] = value;
   return value;
}

void *tcirAllocate(const TCIRModule *module, size_t size)
{
   if (module == NULL || size == 0)
      return NULL;
   return module->allocator.allocate(module->allocator.user_data, size);
}

void *tcirReallocate(const TCIRModule *module, void *pointer, size_t size)
{
   if (module == NULL || size == 0)
      return NULL;
   return module->allocator.reallocate(module->allocator.user_data, pointer, size);
}

void tcirFree(const TCIRModule *module, void *pointer)
{
   if (module != NULL && pointer != NULL)
      module->allocator.free(module->allocator.user_data, pointer);
}

char *tcirDuplicateString(const TCIRModule *module, const char *text)
{
   char *copy;
   size_t length;

   if (module == NULL || text == NULL)
      return NULL;
   length = strlen(text) + 1;
   copy = (char *)tcirAllocate(module, length);
   if (copy != NULL)
      memcpy(copy, text, length);
   return copy;
}

int tcirGrowArray(const TCIRModule *module, void **items, size_t *capacity, size_t item_size, size_t required)
{
   size_t new_capacity;
   void *new_items;

   if (required <= *capacity)
      return 1;

   new_capacity = *capacity == 0 ? 4 : *capacity;
   while (new_capacity < required)
   {
      if (new_capacity > ((size_t)-1) / 2)
         return 0;
      new_capacity *= 2;
   }
   if (item_size != 0 && new_capacity > ((size_t)-1) / item_size)
      return 0;

   new_items = tcirReallocate(module, *items, new_capacity * item_size);
   if (new_items == NULL)
      return 0;

   *items = new_items;
   *capacity = new_capacity;
   return 1;
}

void tcirSetDiagnostic(
   TCIRDiagnostic *diagnostic,
   TCIRDiagnosticCode code,
   const char *function,
   unsigned int tc_pc,
   const char *format,
   ...)
{
   va_list arguments;

   if (diagnostic == NULL)
      return;

   diagnostic->code = code;
   diagnostic->tc_pc = tc_pc;
   snprintf(diagnostic->function, sizeof(diagnostic->function), "%s", function == NULL ? "<unknown>" : function);
   va_start(arguments, format);
   vsnprintf(diagnostic->message, sizeof(diagnostic->message), format, arguments);
   va_end(arguments);
}

void tcirDiagnosticClear(TCIRDiagnostic *diagnostic)
{
   if (diagnostic == NULL)
      return;
   memset(diagnostic, 0, sizeof(TCIRDiagnostic));
   diagnostic->tc_pc = TCIR_TCPC_NONE;
}

const char *tcirDiagnosticCodeName(TCIRDiagnosticCode code)
{
   static const char *const names[] = {
      "none",
      "out_of_memory",
      "invalid_argument",
      "duplicate_block",
      "missing_block",
      "missing_terminator",
      "invalid_terminator",
      "undefined_value",
      "value_order",
      "block_argument_count",
      "block_argument_type",
      "operand_count",
      "operand_type",
      "result_type",
      "return_type",
      "handler_signature",
      "symbol_kind",
      "source_target",
      "unchecked_array_proof",
      "helper_effects",
      "gc_home",
      "internal_address_lifetime",
      "unreachable_block",
      "opcode_registry",
      "unsupported_opcode",
      "malformed_continuation",
      "invalid_target",
      "invalid_register",
      "invalid_symbol",
      "invalid_handler",
      "type_merge"
   };
   size_t count = sizeof(names) / sizeof(names[0]);
   return (unsigned int)code < count ? names[(unsigned int)code] : "unknown";
}

const char *tcirTypeName(TCIRType type)
{
   static const char *const names[] = {
      "void", "i1", "i8", "i16", "i32", "i64", "f64", "ref", "ref!", "token", "internal_address"
   };
   size_t count = sizeof(names) / sizeof(names[0]);
   return (unsigned int)type < count ? names[(unsigned int)type] : "invalid";
}

const char *tcirEffectName(unsigned int single_effect)
{
   switch (single_effect)
   {
      case TCIR_EFFECT_READS_HEAP: return "reads_heap";
      case TCIR_EFFECT_WRITES_HEAP: return "writes_heap";
      case TCIR_EFFECT_MAY_THROW: return "may_throw";
      case TCIR_EFFECT_MAY_GC: return "may_gc";
      case TCIR_EFFECT_MAY_LOCK: return "may_lock";
      case TCIR_EFFECT_RESOLVES_SYMBOL: return "resolves_symbol";
      case TCIR_EFFECT_CALLS_UNKNOWN: return "calls_unknown";
      default: return "none";
   }
}

const char *tcirOperationName(TCIROperation operation)
{
   static const char *const names[] = {
      "const.i32",
      "copy",
      "add.i32",
      "sub.i32",
      "mul.i32",
      "cmp.eq.i32",
      "cmp.lt.s.i32",
      "cmp.le.s.i32",
      "cmp.gt.s.i32",
      "cmp.ge.s.i32",
      "load.slot",
      "store.slot",
      "null.check",
      "bounds.check",
      "array.load.unchecked",
      "array.store.unchecked",
      "field.load",
      "field.store",
      "runtime.call",
      "internal.address"
   };
   size_t count = sizeof(names) / sizeof(names[0]);
   return (unsigned int)operation < count ? names[(unsigned int)operation] : "invalid.op";
}

const char *tcirTerminatorName(TCIRTerminatorKind terminator)
{
   static const char *const names[] = { "br", "br_if", "switch", "ret", "throw", "unreachable" };
   size_t count = sizeof(names) / sizeof(names[0]);
   return (unsigned int)terminator < count ? names[(unsigned int)terminator] : "invalid.terminator";
}

TCIRModule *tcirModuleCreate(const TCIRAllocator *allocator, TCIRDiagnostic *diagnostic)
{
   TCIRAllocator selected;
   TCIRModule *module;

   tcirDiagnosticClear(diagnostic);
   if (allocator == NULL)
   {
      selected.user_data = NULL;
      selected.allocate = tcirDefaultAllocate;
      selected.reallocate = tcirDefaultReallocate;
      selected.free = tcirDefaultFree;
   }
   else
   {
      if (allocator->allocate == NULL || allocator->reallocate == NULL || allocator->free == NULL)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
            "<module>",
            0,
            "a custom allocator must provide allocate, reallocate, and free callbacks");
         return NULL;
      }
      selected = *allocator;
   }

   module = (TCIRModule *)selected.allocate(selected.user_data, sizeof(TCIRModule));
   if (module == NULL)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         "<module>",
         0,
         "unable to allocate a TCIR module");
      return NULL;
   }
   memset(module, 0, sizeof(TCIRModule));
   module->allocator = selected;
   return module;
}

void tcirModuleDestroy(TCIRModule *module)
{
   TCIRAllocator allocator;
   size_t index;

   if (module == NULL)
      return;
   allocator = module->allocator;
   for (index = 0; index < module->function_count; index++)
      tcirDestroyFunction(module->functions[index]);
   for (index = 0; index < module->symbol_count; index++)
      tcirDestroySymbol(module->symbols[index]);
   tcirFree(module, module->functions);
   tcirFree(module, module->symbols);
   allocator.free(allocator.user_data, module);
}

TCIRFunction *tcirModuleAddFunction(
   TCIRModule *module,
   const char *identity,
   const TCIRType *parameter_types,
   size_t parameter_count,
   TCIRType return_type,
   TCIRDiagnostic *diagnostic)
{
   TCIRFunction *function;
   size_t index;

   tcirDiagnosticClear(diagnostic);
   if (module == NULL || identity == NULL || identity[0] == '\0' ||
       (parameter_count != 0 && parameter_types == NULL) ||
       (return_type != TCIR_TYPE_VOID && !tcirTypeIsParameter(return_type)))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         identity,
         0,
         "invalid TCIR function signature");
      return NULL;
   }
   for (index = 0; index < parameter_count; index++)
   {
      if (!tcirTypeIsParameter(parameter_types[index]))
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
            identity,
            0,
            "parameter %u has invalid type %s",
            (unsigned int)index,
            tcirTypeName(parameter_types[index]));
         return NULL;
      }
   }

   if (!tcirGrowArray(
          module,
          (void **)&module->functions,
          &module->function_capacity,
          sizeof(TCIRFunction *),
          module->function_count + 1))
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, identity, 0, "unable to grow the function table");
      return NULL;
   }

   function = (TCIRFunction *)tcirAllocate(module, sizeof(TCIRFunction));
   if (function == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, identity, 0, "unable to allocate a TCIR function");
      return NULL;
   }
   memset(function, 0, sizeof(TCIRFunction));
   function->module = module;
   function->return_type = return_type;
   function->identity = tcirDuplicateString(module, identity);
   if (function->identity == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, identity, 0, "unable to copy the function identity");
      tcirDestroyFunction(function);
      return NULL;
   }

   if (parameter_count != 0)
   {
      function->parameters = (TCIRValue **)tcirAllocate(module, parameter_count * sizeof(TCIRValue *));
      if (function->parameters == NULL)
      {
         tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, identity, 0, "unable to allocate parameters");
         tcirDestroyFunction(function);
         return NULL;
      }
      memset(function->parameters, 0, parameter_count * sizeof(TCIRValue *));
   }

   for (index = 0; index < parameter_count; index++)
   {
      TCIRValue *parameter = tcirCreateValue(
         function,
         NULL,
         parameter_types[index],
         TCIR_VALUE_PARAMETER,
         index,
         diagnostic);
      if (parameter == NULL)
      {
         tcirDestroyFunction(function);
         return NULL;
      }
      function->parameters[index] = parameter;
      function->parameter_count++;
   }

   module->functions[module->function_count++] = function;
   return function;
}

TCIRSymbol *tcirModuleAddSymbol(
   TCIRModule *module,
   TCIRSymbolKind kind,
   const char *owner,
   const char *name,
   const char *descriptor,
   unsigned int constant_pool_index,
   unsigned int helper_effects,
   TCIRDiagnostic *diagnostic)
{
   TCIRSymbol *symbol;

   tcirDiagnosticClear(diagnostic);
   if (module == NULL || owner == NULL || name == NULL || descriptor == NULL ||
       (unsigned int)kind > (unsigned int)TCIR_SYMBOL_HELPER)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<module>", 0, "invalid TCIR symbol");
      return NULL;
   }
   if (!tcirGrowArray(
          module,
          (void **)&module->symbols,
          &module->symbol_capacity,
          sizeof(TCIRSymbol *),
          module->symbol_count + 1))
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, "<module>", 0, "unable to grow the symbol table");
      return NULL;
   }

   symbol = (TCIRSymbol *)tcirAllocate(module, sizeof(TCIRSymbol));
   if (symbol == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, "<module>", 0, "unable to allocate a symbol");
      return NULL;
   }
   memset(symbol, 0, sizeof(TCIRSymbol));
   symbol->module = module;
   symbol->kind = kind;
   symbol->constant_pool_index = constant_pool_index;
   symbol->helper_effects = helper_effects;
   symbol->owner = tcirDuplicateString(module, owner);
   symbol->name = tcirDuplicateString(module, name);
   symbol->descriptor = tcirDuplicateString(module, descriptor);
   if (symbol->owner == NULL || symbol->name == NULL || symbol->descriptor == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_OUT_OF_MEMORY, "<module>", 0, "unable to copy symbol identity");
      tcirDestroySymbol(symbol);
      return NULL;
   }
   module->symbols[module->symbol_count++] = symbol;
   return symbol;
}

size_t tcirModuleFunctionCount(const TCIRModule *module)
{
   return module == NULL ? 0 : module->function_count;
}

const TCIRFunction *tcirModuleFunctionAt(const TCIRModule *module, size_t index)
{
   return module == NULL || index >= module->function_count ? NULL : module->functions[index];
}

size_t tcirModuleSymbolCount(const TCIRModule *module)
{
   return module == NULL ? 0 : module->symbol_count;
}

const TCIRSymbol *tcirModuleSymbolAt(const TCIRModule *module, size_t index)
{
   return module == NULL || index >= module->symbol_count ? NULL : module->symbols[index];
}

const char *tcirFunctionIdentity(const TCIRFunction *function)
{
   return function == NULL ? NULL : function->identity;
}

TCIRType tcirFunctionReturnType(const TCIRFunction *function)
{
   return function == NULL ? TCIR_TYPE_VOID : function->return_type;
}

size_t tcirFunctionParameterCount(const TCIRFunction *function)
{
   return function == NULL ? 0 : function->parameter_count;
}

const TCIRValue *tcirFunctionParameter(const TCIRFunction *function, size_t index)
{
   return function == NULL || index >= function->parameter_count ? NULL : function->parameters[index];
}

TCIRStatus tcirFunctionSetHomes(
   TCIRFunction *function,
   unsigned int i32_count,
   unsigned int ref_count,
   unsigned int v64_count,
   TCIRDiagnostic *diagnostic)
{
   tcirDiagnosticClear(diagnostic);
   if (function == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<function>", 0, "function is null");
      return TCIR_STATUS_INVALID_ARGUMENT;
   }
   function->home_counts[TCIR_HOME_I32] = i32_count;
   function->home_counts[TCIR_HOME_REF] = ref_count;
   function->home_counts[TCIR_HOME_V64] = v64_count;
   return TCIR_STATUS_OK;
}

TCIRStatus tcirFunctionSetSourceSlots(
   TCIRFunction *function,
   size_t slot_count,
   const unsigned char *instruction_starts,
   TCIRDiagnostic *diagnostic)
{
   unsigned char *copy = NULL;

   tcirDiagnosticClear(diagnostic);
   if (function == NULL || (slot_count != 0 && instruction_starts == NULL))
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<function>", 0, "invalid source slot map");
      return TCIR_STATUS_INVALID_ARGUMENT;
   }
   if (slot_count != 0)
   {
      copy = (unsigned char *)tcirAllocate(function->module, slot_count);
      if (copy == NULL)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
            function->identity,
            0,
            "unable to allocate the source slot map");
         return TCIR_STATUS_OUT_OF_MEMORY;
      }
      memcpy(copy, instruction_starts, slot_count);
   }
   tcirFree(function->module, function->instruction_starts);
   function->instruction_starts = copy;
   function->source_slot_count = slot_count;
   return TCIR_STATUS_OK;
}

unsigned int tcirFunctionHomeCount(const TCIRFunction *function, TCIRHomeBank bank)
{
   if (function == NULL || (unsigned int)bank > (unsigned int)TCIR_HOME_V64)
      return 0;
   return function->home_counts[bank];
}

size_t tcirFunctionSourceSlotCount(const TCIRFunction *function)
{
   return function == NULL ? 0 : function->source_slot_count;
}

int tcirFunctionSourceSlotIsInstructionStart(const TCIRFunction *function, size_t slot_index)
{
   return function != NULL && slot_index < function->source_slot_count &&
          function->instruction_starts[slot_index] != 0;
}

TCIRBlock *tcirFunctionAppendBlock(
   TCIRFunction *function,
   unsigned int id,
   TCIRSourceLocation source,
   int is_exception_handler,
   TCIRDiagnostic *diagnostic)
{
   TCIRBlock *block;
   size_t index;

   tcirDiagnosticClear(diagnostic);
   if (function == NULL)
   {
      tcirSetDiagnostic(diagnostic, TCIR_DIAGNOSTIC_INVALID_ARGUMENT, "<function>", source.tc_pc, "function is null");
      return NULL;
   }
   for (index = 0; index < function->block_count; index++)
   {
      if (function->blocks[index]->id == id)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_DUPLICATE_BLOCK,
            function->identity,
            source.tc_pc,
            "block bb%u already exists",
            id);
         return NULL;
      }
   }
   if (!tcirGrowArray(
          function->module,
          (void **)&function->blocks,
          &function->block_capacity,
          sizeof(TCIRBlock *),
          function->block_count + 1))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         source.tc_pc,
         "unable to grow the block table");
      return NULL;
   }

   block = (TCIRBlock *)tcirAllocate(function->module, sizeof(TCIRBlock));
   if (block == NULL)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         function->identity,
         source.tc_pc,
         "unable to allocate block bb%u",
         id);
      return NULL;
   }
   memset(block, 0, sizeof(TCIRBlock));
   block->function = function;
   block->id = id;
   block->source = source;
   block->is_exception_handler = is_exception_handler != 0;
   function->blocks[function->block_count++] = block;
   return block;
}

size_t tcirFunctionBlockCount(const TCIRFunction *function)
{
   return function == NULL ? 0 : function->block_count;
}

const TCIRBlock *tcirFunctionBlockAt(const TCIRFunction *function, size_t index)
{
   return function == NULL || index >= function->block_count ? NULL : function->blocks[index];
}

unsigned int tcirBlockId(const TCIRBlock *block)
{
   return block == NULL ? 0 : block->id;
}

TCIRSourceLocation tcirBlockSource(const TCIRBlock *block)
{
   TCIRSourceLocation source = { TCIR_TCPC_NONE, -1 };
   return block == NULL ? source : block->source;
}

int tcirBlockIsExceptionHandler(const TCIRBlock *block)
{
   return block != NULL && block->is_exception_handler;
}

TCIRValue *tcirBlockAppendArgument(TCIRBlock *block, TCIRType type, TCIRDiagnostic *diagnostic)
{
   TCIRValue *argument;

   tcirDiagnosticClear(diagnostic);
   if (block == NULL || !tcirTypeIsParameter(type))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         block == NULL ? "<block>" : block->function->identity,
         block == NULL ? 0 : block->source.tc_pc,
         "invalid block argument type %s",
         tcirTypeName(type));
      return NULL;
   }
   if (!tcirGrowArray(
          block->function->module,
          (void **)&block->arguments,
          &block->argument_capacity,
          sizeof(TCIRValue *),
          block->argument_count + 1))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         block->function->identity,
         block->source.tc_pc,
         "unable to grow the argument table for bb%u",
         block->id);
      return NULL;
   }
   argument = tcirCreateValue(
      block->function,
      block,
      type,
      TCIR_VALUE_BLOCK_ARGUMENT,
      block->argument_count,
      diagnostic);
   if (argument == NULL)
      return NULL;
   block->arguments[block->argument_count++] = argument;
   return argument;
}

size_t tcirBlockArgumentCount(const TCIRBlock *block)
{
   return block == NULL ? 0 : block->argument_count;
}

const TCIRValue *tcirBlockArgumentAt(const TCIRBlock *block, size_t index)
{
   return block == NULL || index >= block->argument_count ? NULL : block->arguments[index];
}

TCIRStatus tcirBlockAppendOperation(
   TCIRBlock *block,
   const TCIROperationSpec *spec,
   TCIRValue **result,
   TCIRDiagnostic *diagnostic)
{
   TCIROperationData operation;
   TCIRValue *operation_result = NULL;
   size_t operation_index;

   tcirDiagnosticClear(diagnostic);
   if (result != NULL)
      *result = NULL;
   if (block == NULL || spec == NULL || (spec->operand_count != 0 && spec->operands == NULL) ||
       (spec->gc_home_count != 0 && spec->gc_homes == NULL))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         block == NULL ? "<block>" : block->function->identity,
         block == NULL ? 0 : block->source.tc_pc,
         "invalid operation specification");
      return TCIR_STATUS_INVALID_ARGUMENT;
   }
   if (block->has_terminator)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         block->function->identity,
         spec->source.tc_pc,
         "cannot append an operation after the terminator in bb%u",
         block->id);
      return TCIR_STATUS_ALREADY_TERMINATED;
   }
   if (!tcirGrowArray(
          block->function->module,
          (void **)&block->operations,
          &block->operation_capacity,
          sizeof(TCIROperationData),
          block->operation_count + 1))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
         block->function->identity,
         spec->source.tc_pc,
         "unable to grow the operation table for bb%u",
         block->id);
      return TCIR_STATUS_OUT_OF_MEMORY;
   }

   memset(&operation, 0, sizeof(operation));
   operation.opcode = spec->opcode;
   operation.result_type = spec->result_type;
   operation.operand_count = spec->operand_count;
   operation.immediate_i32 = spec->immediate_i32;
   operation.home_bank = spec->home_bank;
   operation.home_index = spec->home_index;
   operation.symbol = spec->symbol;
   operation.effects = spec->effects;
   operation.gc_home_count = spec->gc_home_count;
   operation.exception_target = spec->exception_target;
   operation.propagates_exception = spec->propagates_exception != 0;
   operation.source = spec->source;

   if (spec->operand_count != 0)
   {
      operation.operands = (const TCIRValue **)tcirAllocate(
         block->function->module,
         spec->operand_count * sizeof(TCIRValue *));
      if (operation.operands == NULL)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
            block->function->identity,
            spec->source.tc_pc,
            "unable to copy operation operands");
         return TCIR_STATUS_OUT_OF_MEMORY;
      }
      memcpy((void *)operation.operands, spec->operands, spec->operand_count * sizeof(TCIRValue *));
   }
   if (spec->gc_home_count != 0)
   {
      operation.gc_homes = (TCIRGCHome *)tcirAllocate(
         block->function->module,
         spec->gc_home_count * sizeof(TCIRGCHome));
      if (operation.gc_homes == NULL)
      {
         tcirFree(block->function->module, (void *)operation.operands);
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
            block->function->identity,
            spec->source.tc_pc,
            "unable to copy GC homes");
         return TCIR_STATUS_OUT_OF_MEMORY;
      }
      memcpy(operation.gc_homes, spec->gc_homes, spec->gc_home_count * sizeof(TCIRGCHome));
   }

   operation_index = block->operation_count;
   if (spec->result_type != TCIR_TYPE_VOID)
   {
      operation_result = tcirCreateValue(
         block->function,
         block,
         spec->result_type,
         TCIR_VALUE_OPERATION,
         operation_index,
         diagnostic);
      if (operation_result == NULL)
      {
         tcirFree(block->function->module, (void *)operation.operands);
         tcirFree(block->function->module, operation.gc_homes);
         return TCIR_STATUS_OUT_OF_MEMORY;
      }
   }
   operation.result = operation_result;
   block->operations[block->operation_count++] = operation;
   if (result != NULL)
      *result = operation_result;
   return TCIR_STATUS_OK;
}

size_t tcirBlockOperationCount(const TCIRBlock *block)
{
   return block == NULL ? 0 : block->operation_count;
}

TCIRStatus tcirBlockOperationAt(const TCIRBlock *block, size_t index, TCIROperationView *view)
{
   const TCIROperationData *operation;

   if (block == NULL || view == NULL || index >= block->operation_count)
      return TCIR_STATUS_INVALID_ARGUMENT;
   operation = &block->operations[index];
   view->opcode = operation->opcode;
   view->result_type = operation->result_type;
   view->result = operation->result;
   view->operands = operation->operands;
   view->operand_count = operation->operand_count;
   view->immediate_i32 = operation->immediate_i32;
   view->home_bank = operation->home_bank;
   view->home_index = operation->home_index;
   view->symbol = operation->symbol;
   view->effects = operation->effects;
   view->gc_homes = operation->gc_homes;
   view->gc_home_count = operation->gc_home_count;
   view->exception_target = operation->exception_target;
   view->propagates_exception = operation->propagates_exception;
   view->source = operation->source;
   return TCIR_STATUS_OK;
}

TCIRStatus tcirBlockSetTerminator(
   TCIRBlock *block,
   const TCIRTerminatorSpec *spec,
   TCIRDiagnostic *diagnostic)
{
   TCIREdge *edges = NULL;
   size_t edge_index;

   tcirDiagnosticClear(diagnostic);
   if (block == NULL || spec == NULL || (spec->edge_count != 0 && spec->edges == NULL))
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         block == NULL ? "<block>" : block->function->identity,
         block == NULL ? 0 : block->source.tc_pc,
         "invalid terminator specification");
      return TCIR_STATUS_INVALID_ARGUMENT;
   }
   if (block->has_terminator)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
         block->function->identity,
         spec->source.tc_pc,
         "bb%u already has a terminator",
         block->id);
      return TCIR_STATUS_ALREADY_TERMINATED;
   }

   if (spec->edge_count != 0)
   {
      edges = (TCIREdge *)tcirAllocate(block->function->module, spec->edge_count * sizeof(TCIREdge));
      if (edges == NULL)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
            block->function->identity,
            spec->source.tc_pc,
            "unable to copy terminator edges");
         return TCIR_STATUS_OUT_OF_MEMORY;
      }
      memset(edges, 0, spec->edge_count * sizeof(TCIREdge));
   }

   for (edge_index = 0; edge_index < spec->edge_count; edge_index++)
   {
      const TCIREdge *source = &spec->edges[edge_index];
      edges[edge_index] = *source;
      edges[edge_index].arguments = NULL;
      if (source->argument_count != 0)
      {
         const TCIRValue **arguments;
         if (source->arguments == NULL)
         {
            size_t cleanup;
            for (cleanup = 0; cleanup < edge_index; cleanup++)
               tcirFree(block->function->module, (void *)edges[cleanup].arguments);
            tcirFree(block->function->module, edges);
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
               block->function->identity,
               spec->source.tc_pc,
               "edge %u has a null argument array",
               (unsigned int)edge_index);
            return TCIR_STATUS_INVALID_ARGUMENT;
         }
         arguments = (const TCIRValue **)tcirAllocate(
            block->function->module,
            source->argument_count * sizeof(TCIRValue *));
         if (arguments == NULL)
         {
            size_t cleanup;
            for (cleanup = 0; cleanup < edge_index; cleanup++)
               tcirFree(block->function->module, (void *)edges[cleanup].arguments);
            tcirFree(block->function->module, edges);
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
               block->function->identity,
               spec->source.tc_pc,
               "unable to copy edge arguments");
            return TCIR_STATUS_OUT_OF_MEMORY;
         }
         memcpy((void *)arguments, source->arguments, source->argument_count * sizeof(TCIRValue *));
         edges[edge_index].arguments = arguments;
      }
   }

   block->terminator.kind = spec->kind;
   block->terminator.value = spec->value;
   block->terminator.edges = edges;
   block->terminator.edge_count = spec->edge_count;
   block->terminator.source = spec->source;
   block->has_terminator = 1;
   return TCIR_STATUS_OK;
}

TCIRStatus tcirBlockTerminator(const TCIRBlock *block, TCIRTerminatorView *view)
{
   if (block == NULL || view == NULL || !block->has_terminator)
      return TCIR_STATUS_INVALID_ARGUMENT;
   *view = block->terminator;
   return TCIR_STATUS_OK;
}

unsigned int tcirValueId(const TCIRValue *value)
{
   return value == NULL ? 0 : value->id;
}

TCIRType tcirValueType(const TCIRValue *value)
{
   return value == NULL ? TCIR_TYPE_VOID : value->type;
}

TCIRSymbolKind tcirSymbolKind(const TCIRSymbol *symbol)
{
   return symbol == NULL ? TCIR_SYMBOL_CLASS : symbol->kind;
}

const char *tcirSymbolOwner(const TCIRSymbol *symbol)
{
   return symbol == NULL ? NULL : symbol->owner;
}

const char *tcirSymbolName(const TCIRSymbol *symbol)
{
   return symbol == NULL ? NULL : symbol->name;
}

const char *tcirSymbolDescriptor(const TCIRSymbol *symbol)
{
   return symbol == NULL ? NULL : symbol->descriptor;
}

unsigned int tcirSymbolConstantPoolIndex(const TCIRSymbol *symbol)
{
   return symbol == NULL ? 0 : symbol->constant_pool_index;
}

unsigned int tcirSymbolHelperEffects(const TCIRSymbol *symbol)
{
   return symbol == NULL ? 0 : symbol->helper_effects;
}

int tcirSourceIsInstructionStart(const TCIRFunction *function, TCIRSourceLocation source)
{
   if (function == NULL || source.tc_pc == TCIR_TCPC_NONE)
      return 0;
   if (function->source_slot_count == 0)
      return 1;
   return source.tc_pc < function->source_slot_count && function->instruction_starts[source.tc_pc] != 0;
}

int tcirValueBelongsToFunction(const TCIRValue *value, const TCIRFunction *function)
{
   return value != NULL && function != NULL && value->function == function;
}

void tcirFreeText(const TCIRModule *module, char *text)
{
   tcirFree(module, text);
}
