// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_INTERP_H
#define TCIR_INTERP_H

#include "tcir.h"

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define TCIR_INTERPRETER_DEFAULT_STEP_LIMIT 1000000U

typedef union TCIRRuntimeValue
{
   int i1;
   int32_t i32;
   int64_t i64;
   double f64;
   void *ref;
} TCIRRuntimeValue;

typedef union TCIRV64Home
{
   int64_t i64;
   double f64;
} TCIRV64Home;

typedef enum TCIRRuntimeExceptionKind
{
   TCIR_RUNTIME_EXCEPTION_ARITHMETIC = 0,
   TCIR_RUNTIME_EXCEPTION_NULL_POINTER
} TCIRRuntimeExceptionKind;

typedef void (*TCIRRaiseExceptionFunction)(
   void *runtime_context,
   TCIRRuntimeExceptionKind kind,
   unsigned int tc_pc);

typedef enum TCIRMethodCallStatus
{
   TCIR_METHOD_CALL_RETURNED = 0,
   TCIR_METHOD_CALL_THROWN,
   TCIR_METHOD_CALL_REJECTED,
   TCIR_METHOD_CALL_OUT_OF_MEMORY
} TCIRMethodCallStatus;

typedef TCIRMethodCallStatus (*TCIRMethodCallFunction)(
   void *runtime_context,
   const TCIRSymbol *symbol,
   TCIRCallKind kind,
   void *receiver,
   const TCIRRuntimeValue *arguments,
   size_t argument_count,
   TCIRRuntimeValue *result);

typedef enum TCIRObjectAllocationStatus
{
   TCIR_OBJECT_ALLOCATION_RETURNED = 0,
   TCIR_OBJECT_ALLOCATION_THROWN,
   TCIR_OBJECT_ALLOCATION_REJECTED,
   TCIR_OBJECT_ALLOCATION_OUT_OF_MEMORY
} TCIRObjectAllocationStatus;

typedef TCIRObjectAllocationStatus (*TCIRAllocateObjectFunction)(
   void *runtime_context,
   const TCIRSymbol *symbol,
   void **ref_homes,
   size_t ref_home_count,
   unsigned int destination_home,
   TCIRRuntimeValue *result);

typedef struct TCIRInterpreterFrame
{
   int32_t *i32_homes;
   size_t i32_home_count;
   void **ref_homes;
   size_t ref_home_count;
   TCIRV64Home *v64_homes;
   size_t v64_home_count;
   const TCIRRuntimeValue *arguments;
   size_t argument_count;
   unsigned int tc_pc;
   void *runtime_context;
   TCIRRaiseExceptionFunction raise_exception;
   TCIRMethodCallFunction call_method;
   TCIRAllocateObjectFunction allocate_object;
} TCIRInterpreterFrame;

typedef struct TCIRInterpreterOptions
{
   size_t max_steps;
} TCIRInterpreterOptions;

typedef enum TCIRInterpreterStatus
{
   TCIR_INTERPRETER_RETURNED = 0,
   TCIR_INTERPRETER_THROWN,
   TCIR_INTERPRETER_REJECTED,
   TCIR_INTERPRETER_STEP_LIMIT,
   TCIR_INTERPRETER_OUT_OF_MEMORY
} TCIRInterpreterStatus;

typedef struct TCIRInterpreterResult
{
   TCIRInterpreterStatus status;
   TCIRType type;
   TCIRRuntimeValue value;
   unsigned int tc_pc;
   size_t steps;
} TCIRInterpreterResult;

TCIRInterpreterStatus tcirInterpretFunction(
   const TCIRFunction *function,
   TCIRInterpreterFrame *frame,
   const TCIRInterpreterOptions *options,
   TCIRInterpreterResult *result,
   TCIRDiagnostic *diagnostic);

#ifdef __cplusplus
}
#endif

#endif
