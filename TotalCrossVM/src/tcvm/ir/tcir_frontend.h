// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_FRONTEND_H
#define TCIR_FRONTEND_H

#include "tcir.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct TCIRMethodParameter
{
   TCIRType type;
   TCIRHomeBank home_bank;
   unsigned int home_index;
} TCIRMethodParameter;

typedef struct TCIRMethodHandler
{
   unsigned int start_pc;
   unsigned int end_pc;
   unsigned int handler_pc;
   unsigned int exception_home;
} TCIRMethodHandler;

typedef struct TCIRCallShape
{
   unsigned int parameter_count;
   int returns_value;
   TCIRCallKind kind;
   const TCIRType *parameter_types;
   TCIRType return_type;
   const char *owner;
   const char *name;
   const char *descriptor;
} TCIRCallShape;

typedef int (*TCIRResolveCallShapeFunction)(
   void *user_data,
   unsigned int symbol,
   TCIRCallShape *shape);

typedef int (*TCIRResolveClassNameFunction)(
   void *user_data,
   unsigned int symbol,
   const char **class_name);

typedef struct TCIRMethodView
{
   const char *identity;
   const unsigned int *code;
   size_t code_slot_count;
   unsigned int i32_home_count;
   unsigned int ref_home_count;
   unsigned int v64_home_count;
   const TCIRType *v64_home_types;
   const TCIRMethodParameter *parameters;
   size_t parameter_count;
   TCIRType return_type;
   const int *i32_constants;
   size_t i32_constant_count;
   const int64_t *i64_constants;
   size_t i64_constant_count;
   const double *f64_constants;
   size_t f64_constant_count;
   const int *source_lines;
   const TCIRMethodHandler *handlers;
   size_t handler_count;
   TCIRResolveCallShapeFunction resolve_call_shape;
   void *resolve_call_shape_user_data;
   TCIRResolveClassNameFunction resolve_class_name;
   void *resolve_class_name_user_data;
} TCIRMethodView;

typedef enum TCIRFrontendResult
{
   TCIR_FRONTEND_OK = 0,
   TCIR_FRONTEND_FALLBACK,
   TCIR_FRONTEND_ERROR
} TCIRFrontendResult;

TCIRFrontendResult tcirFrontendBuildFunction(
   TCIRModule *module,
   const TCIRMethodView *method,
   TCIRFunction **function,
   TCIRDiagnostic *diagnostic);

#ifdef __cplusplus
}
#endif

#endif
