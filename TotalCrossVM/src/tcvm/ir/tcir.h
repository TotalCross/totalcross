// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_H
#define TCIR_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

#define TC_IR_VERSION 1U
#define TCIR_TCPC_NONE (~0U)

typedef struct TCIRModule TCIRModule;
typedef struct TCIRFunction TCIRFunction;
typedef struct TCIRBlock TCIRBlock;
typedef struct TCIRValue TCIRValue;
typedef struct TCIRSymbol TCIRSymbol;

typedef enum TCIRStatus
{
   TCIR_STATUS_OK = 0,
   TCIR_STATUS_INVALID_ARGUMENT,
   TCIR_STATUS_OUT_OF_MEMORY,
   TCIR_STATUS_ALREADY_TERMINATED
} TCIRStatus;

typedef enum TCIRDiagnosticCode
{
   TCIR_DIAGNOSTIC_NONE = 0,
   TCIR_DIAGNOSTIC_OUT_OF_MEMORY,
   TCIR_DIAGNOSTIC_INVALID_ARGUMENT,
   TCIR_DIAGNOSTIC_DUPLICATE_BLOCK,
   TCIR_DIAGNOSTIC_MISSING_BLOCK,
   TCIR_DIAGNOSTIC_MISSING_TERMINATOR,
   TCIR_DIAGNOSTIC_INVALID_TERMINATOR,
   TCIR_DIAGNOSTIC_UNDEFINED_VALUE,
   TCIR_DIAGNOSTIC_VALUE_ORDER,
   TCIR_DIAGNOSTIC_BLOCK_ARGUMENT_COUNT,
   TCIR_DIAGNOSTIC_BLOCK_ARGUMENT_TYPE,
   TCIR_DIAGNOSTIC_OPERAND_COUNT,
   TCIR_DIAGNOSTIC_OPERAND_TYPE,
   TCIR_DIAGNOSTIC_RESULT_TYPE,
   TCIR_DIAGNOSTIC_RETURN_TYPE,
   TCIR_DIAGNOSTIC_HANDLER_SIGNATURE,
   TCIR_DIAGNOSTIC_SYMBOL_KIND,
   TCIR_DIAGNOSTIC_SOURCE_TARGET,
   TCIR_DIAGNOSTIC_UNCHECKED_ARRAY_PROOF,
   TCIR_DIAGNOSTIC_HELPER_EFFECTS,
   TCIR_DIAGNOSTIC_GC_HOME,
   TCIR_DIAGNOSTIC_INTERNAL_ADDRESS_LIFETIME,
   TCIR_DIAGNOSTIC_UNREACHABLE_BLOCK,
   TCIR_DIAGNOSTIC_OPCODE_REGISTRY
} TCIRDiagnosticCode;

typedef struct TCIRDiagnostic
{
   TCIRDiagnosticCode code;
   unsigned int tc_pc;
   char function[128];
   char message[256];
} TCIRDiagnostic;

typedef void *(*TCIRAllocateFunction)(void *user_data, size_t size);
typedef void *(*TCIRReallocateFunction)(void *user_data, void *pointer, size_t size);
typedef void (*TCIRFreeFunction)(void *user_data, void *pointer);

typedef struct TCIRAllocator
{
   void *user_data;
   TCIRAllocateFunction allocate;
   TCIRReallocateFunction reallocate;
   TCIRFreeFunction free;
} TCIRAllocator;

typedef enum TCIRType
{
   TCIR_TYPE_VOID = 0,
   TCIR_TYPE_I1,
   TCIR_TYPE_I8,
   TCIR_TYPE_I16,
   TCIR_TYPE_I32,
   TCIR_TYPE_I64,
   TCIR_TYPE_F64,
   TCIR_TYPE_REF,
   TCIR_TYPE_NON_NULL_REF,
   TCIR_TYPE_TOKEN,
   TCIR_TYPE_INTERNAL_ADDRESS
} TCIRType;

typedef enum TCIRHomeBank
{
   TCIR_HOME_I32 = 0,
   TCIR_HOME_REF,
   TCIR_HOME_V64
} TCIRHomeBank;

typedef enum TCIREffect
{
   TCIR_EFFECT_NONE = 0,
   TCIR_EFFECT_READS_HEAP = 1U << 0,
   TCIR_EFFECT_WRITES_HEAP = 1U << 1,
   TCIR_EFFECT_MAY_THROW = 1U << 2,
   TCIR_EFFECT_MAY_GC = 1U << 3,
   TCIR_EFFECT_MAY_LOCK = 1U << 4,
   TCIR_EFFECT_RESOLVES_SYMBOL = 1U << 5,
   TCIR_EFFECT_CALLS_UNKNOWN = 1U << 6
} TCIREffect;

typedef enum TCIRSymbolKind
{
   TCIR_SYMBOL_CLASS = 0,
   TCIR_SYMBOL_FIELD,
   TCIR_SYMBOL_METHOD,
   TCIR_SYMBOL_STRING,
   TCIR_SYMBOL_HELPER
} TCIRSymbolKind;

typedef enum TCIROperation
{
   TCIR_OP_CONST_I32 = 0,
   TCIR_OP_COPY,
   TCIR_OP_ADD_I32,
   TCIR_OP_SUB_I32,
   TCIR_OP_MUL_I32,
   TCIR_OP_CMP_EQ_I32,
   TCIR_OP_CMP_LT_I32,
   TCIR_OP_CMP_LE_I32,
   TCIR_OP_CMP_GT_I32,
   TCIR_OP_CMP_GE_I32,
   TCIR_OP_LOAD_SLOT,
   TCIR_OP_STORE_SLOT,
   TCIR_OP_NULL_CHECK,
   TCIR_OP_BOUNDS_CHECK,
   TCIR_OP_ARRAY_LOAD_UNCHECKED,
   TCIR_OP_ARRAY_STORE_UNCHECKED,
   TCIR_OP_FIELD_LOAD,
   TCIR_OP_FIELD_STORE,
   TCIR_OP_RUNTIME_CALL,
   TCIR_OP_INTERNAL_ADDRESS
} TCIROperation;

typedef enum TCIRTerminatorKind
{
   TCIR_TERMINATOR_BRANCH = 0,
   TCIR_TERMINATOR_BRANCH_IF,
   TCIR_TERMINATOR_SWITCH,
   TCIR_TERMINATOR_RETURN,
   TCIR_TERMINATOR_THROW,
   TCIR_TERMINATOR_UNREACHABLE
} TCIRTerminatorKind;

typedef struct TCIRSourceLocation
{
   unsigned int tc_pc;
   int source_line;
} TCIRSourceLocation;

typedef struct TCIRGCHome
{
   const TCIRValue *value;
   unsigned int home_index;
} TCIRGCHome;

typedef struct TCIROperationSpec
{
   TCIROperation opcode;
   TCIRType result_type;
   const TCIRValue *const *operands;
   size_t operand_count;
   int immediate_i32;
   TCIRHomeBank home_bank;
   unsigned int home_index;
   const TCIRSymbol *symbol;
   unsigned int effects;
   const TCIRGCHome *gc_homes;
   size_t gc_home_count;
   TCIRBlock *exception_target;
   int propagates_exception;
   TCIRSourceLocation source;
} TCIROperationSpec;

typedef struct TCIROperationView
{
   TCIROperation opcode;
   TCIRType result_type;
   const TCIRValue *result;
   const TCIRValue *const *operands;
   size_t operand_count;
   int immediate_i32;
   TCIRHomeBank home_bank;
   unsigned int home_index;
   const TCIRSymbol *symbol;
   unsigned int effects;
   const TCIRGCHome *gc_homes;
   size_t gc_home_count;
   const TCIRBlock *exception_target;
   int propagates_exception;
   TCIRSourceLocation source;
} TCIROperationView;

typedef struct TCIREdge
{
   TCIRBlock *target;
   const TCIRValue *const *arguments;
   size_t argument_count;
   int has_case_value;
   int case_value;
} TCIREdge;

typedef struct TCIRTerminatorSpec
{
   TCIRTerminatorKind kind;
   const TCIRValue *value;
   const TCIREdge *edges;
   size_t edge_count;
   TCIRSourceLocation source;
} TCIRTerminatorSpec;

typedef struct TCIRTerminatorView
{
   TCIRTerminatorKind kind;
   const TCIRValue *value;
   const TCIREdge *edges;
   size_t edge_count;
   TCIRSourceLocation source;
} TCIRTerminatorView;

void tcirDiagnosticClear(TCIRDiagnostic *diagnostic);
const char *tcirDiagnosticCodeName(TCIRDiagnosticCode code);
const char *tcirTypeName(TCIRType type);
const char *tcirEffectName(unsigned int single_effect);

TCIRModule *tcirModuleCreate(const TCIRAllocator *allocator, TCIRDiagnostic *diagnostic);
void tcirModuleDestroy(TCIRModule *module);

TCIRFunction *tcirModuleAddFunction(
   TCIRModule *module,
   const char *identity,
   const TCIRType *parameter_types,
   size_t parameter_count,
   TCIRType return_type,
   TCIRDiagnostic *diagnostic);

TCIRSymbol *tcirModuleAddSymbol(
   TCIRModule *module,
   TCIRSymbolKind kind,
   const char *owner,
   const char *name,
   const char *descriptor,
   unsigned int constant_pool_index,
   unsigned int helper_effects,
   TCIRDiagnostic *diagnostic);

size_t tcirModuleFunctionCount(const TCIRModule *module);
const TCIRFunction *tcirModuleFunctionAt(const TCIRModule *module, size_t index);

const char *tcirFunctionIdentity(const TCIRFunction *function);
TCIRType tcirFunctionReturnType(const TCIRFunction *function);
size_t tcirFunctionParameterCount(const TCIRFunction *function);
const TCIRValue *tcirFunctionParameter(const TCIRFunction *function, size_t index);
TCIRStatus tcirFunctionSetHomes(
   TCIRFunction *function,
   unsigned int i32_count,
   unsigned int ref_count,
   unsigned int v64_count,
   TCIRDiagnostic *diagnostic);
TCIRStatus tcirFunctionSetSourceSlots(
   TCIRFunction *function,
   size_t slot_count,
   const unsigned char *instruction_starts,
   TCIRDiagnostic *diagnostic);

TCIRBlock *tcirFunctionAppendBlock(
   TCIRFunction *function,
   unsigned int id,
   TCIRSourceLocation source,
   int is_exception_handler,
   TCIRDiagnostic *diagnostic);
size_t tcirFunctionBlockCount(const TCIRFunction *function);
const TCIRBlock *tcirFunctionBlockAt(const TCIRFunction *function, size_t index);

unsigned int tcirBlockId(const TCIRBlock *block);
TCIRSourceLocation tcirBlockSource(const TCIRBlock *block);
int tcirBlockIsExceptionHandler(const TCIRBlock *block);
TCIRValue *tcirBlockAppendArgument(TCIRBlock *block, TCIRType type, TCIRDiagnostic *diagnostic);
size_t tcirBlockArgumentCount(const TCIRBlock *block);
const TCIRValue *tcirBlockArgumentAt(const TCIRBlock *block, size_t index);
TCIRStatus tcirBlockAppendOperation(
   TCIRBlock *block,
   const TCIROperationSpec *spec,
   TCIRValue **result,
   TCIRDiagnostic *diagnostic);
size_t tcirBlockOperationCount(const TCIRBlock *block);
TCIRStatus tcirBlockOperationAt(const TCIRBlock *block, size_t index, TCIROperationView *view);
TCIRStatus tcirBlockSetTerminator(
   TCIRBlock *block,
   const TCIRTerminatorSpec *spec,
   TCIRDiagnostic *diagnostic);
TCIRStatus tcirBlockTerminator(const TCIRBlock *block, TCIRTerminatorView *view);

unsigned int tcirValueId(const TCIRValue *value);
TCIRType tcirValueType(const TCIRValue *value);

TCIRSymbolKind tcirSymbolKind(const TCIRSymbol *symbol);
const char *tcirSymbolOwner(const TCIRSymbol *symbol);
const char *tcirSymbolName(const TCIRSymbol *symbol);
const char *tcirSymbolDescriptor(const TCIRSymbol *symbol);
unsigned int tcirSymbolConstantPoolIndex(const TCIRSymbol *symbol);
unsigned int tcirSymbolHelperEffects(const TCIRSymbol *symbol);

int tcirVerifyFunction(const TCIRFunction *function, TCIRDiagnostic *diagnostic);
char *tcirFunctionDump(const TCIRFunction *function, TCIRDiagnostic *diagnostic);
void tcirFreeText(const TCIRModule *module, char *text);

#ifdef __cplusplus
}
#endif

#endif
