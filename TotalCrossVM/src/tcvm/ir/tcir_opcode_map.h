// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_OPCODE_MAP_H
#define TCIR_OPCODE_MAP_H

#include "tcir.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum TCIRDecoderClass
{
   TCIR_DECODER_SINGLE = 0,
   TCIR_DECODER_CALL,
   TCIR_DECODER_SWITCH,
   TCIR_DECODER_MULTIARRAY
} TCIRDecoderClass;

typedef enum TCIRLoweringClass
{
   TCIR_LOWERING_DIRECT = 0,
   TCIR_LOWERING_LOWERED,
   TCIR_LOWERING_RUNTIME_HELPER,
   TCIR_LOWERING_UNSUPPORTED_IN_POC,
   TCIR_LOWERING_FUTURE,
   TCIR_LOWERING_OBSOLETE,
   TCIR_LOWERING_PLATFORM_SPECIFIC,
   TCIR_LOWERING_NEEDS_INVESTIGATION
} TCIRLoweringClass;

typedef enum TCIRPOCStatus
{
   TCIR_POC_SUPPORTED = 0,
   TCIR_POC_FALLBACK,
   TCIR_POC_INVESTIGATE
} TCIRPOCStatus;

typedef struct TCIROpcodeInfo
{
   unsigned int value;
   const char *name;
   TCIRDecoderClass decoder_class;
   TCIRLoweringClass lowering_class;
   TCIRPOCStatus poc_status;
} TCIROpcodeInfo;

size_t tcirOpcodeCount(void);
const TCIROpcodeInfo *tcirOpcodeAt(size_t index);
const TCIROpcodeInfo *tcirOpcodeLookup(unsigned int value);
int tcirOpcodeRegistryValidate(TCIRDiagnostic *diagnostic);
const char *tcirDecoderClassName(TCIRDecoderClass decoder_class);
const char *tcirLoweringClassName(TCIRLoweringClass lowering_class);
const char *tcirPOCStatusName(TCIRPOCStatus status);

#ifdef __cplusplus
}
#endif

#endif
