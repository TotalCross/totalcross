// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_AOT_H
#define TCIR_AOT_H

#include "tcir_compiled.h"

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

#define TC_AOT_GENERATOR_VERSION 1U
#define TC_AOT_MANIFEST_SCHEMA_VERSION 1U

typedef enum TCIRAotGenerateStatus
{
   TCIR_AOT_GENERATE_READY = 0,
   TCIR_AOT_GENERATE_VERIFICATION_FAILED,
   TCIR_AOT_GENERATE_INELIGIBLE,
   TCIR_AOT_GENERATE_OUT_OF_MEMORY,
   TCIR_AOT_GENERATE_EMISSION_FAILED
} TCIRAotGenerateStatus;

typedef enum TCIRAotDiagnosticCode
{
   TCIR_AOT_DIAGNOSTIC_NONE = 0,
   TCIR_AOT_DIAGNOSTIC_INVALID_ARGUMENT,
   TCIR_AOT_DIAGNOSTIC_VERIFICATION_FAILED,
   TCIR_AOT_DIAGNOSTIC_INVALID_IDENTITY,
   TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TYPE,
   TCIR_AOT_DIAGNOSTIC_INELIGIBLE_OPERATION,
   TCIR_AOT_DIAGNOSTIC_INELIGIBLE_TERMINATOR,
   TCIR_AOT_DIAGNOSTIC_DUPLICATE_IDENTITY,
   TCIR_AOT_DIAGNOSTIC_OUT_OF_MEMORY,
   TCIR_AOT_DIAGNOSTIC_EMISSION_FAILED
} TCIRAotDiagnosticCode;

typedef struct TCIRAotDiagnostic
{
   TCIRAotDiagnosticCode code;
   unsigned int tc_pc;
   TCIRDiagnostic verifier;
   char function[128];
   char message[256];
} TCIRAotDiagnostic;

typedef struct TCIRAotGenerateOptions
{
   const char *target_options;
} TCIRAotGenerateOptions;

typedef struct TCIRAotOutput
{
   char *source;
   size_t source_size;
   char *header;
   size_t header_size;
   char *manifest;
   size_t manifest_size;
   char input_hash[17];
} TCIRAotOutput;

typedef struct TCIRAotRegistryEntry
{
   const char *class_name;
   const char *method_name;
   const char *signature;
   const char *content_hash;
   TCCompiledEntry entry;
} TCIRAotRegistryEntry;

void tcirAotDiagnosticClear(TCIRAotDiagnostic *diagnostic);
const char *tcirAotDiagnosticCodeName(TCIRAotDiagnosticCode code);

TCIRAotGenerateStatus tcirAotGenerate(
   const TCIRFunction *const *functions,
   size_t function_count,
   const TCIRAotGenerateOptions *options,
   TCIRAotOutput *output,
   TCIRAotDiagnostic *diagnostic);
void tcirAotOutputDestroy(TCIRAotOutput *output);

const TCIRAotRegistryEntry *tcirAotRegistryFind(
   const TCIRAotRegistryEntry *entries,
   size_t entry_count,
   const char *class_name,
   const char *method_name,
   const char *signature,
   const char *content_hash);

#ifdef __cplusplus
}
#endif

#endif
