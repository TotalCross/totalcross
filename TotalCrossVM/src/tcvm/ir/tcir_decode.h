// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_DECODE_H
#define TCIR_DECODE_H

#include "tcir_frontend.h"
#include "tcir_opcode_map.h"

typedef struct TCIRDecodedInstruction
{
   const TCIROpcodeInfo *info;
   unsigned int pc;
   unsigned int width;
   unsigned int reg0;
   unsigned int reg1;
   unsigned int reg2;
   unsigned int symbol;
   int immediate;
   int target;
} TCIRDecodedInstruction;

typedef struct TCIRDecodedMethod
{
   TCIRDecodedInstruction *instructions;
   size_t instruction_count;
   unsigned char *instruction_starts;
   size_t *instruction_indexes;
} TCIRDecodedMethod;

TCIRFrontendResult tcirDecodeMethod(
   const TCIRMethodView *method,
   TCIRDecodedMethod *decoded,
   TCIRDiagnostic *diagnostic);
void tcirDecodedMethodDestroy(TCIRDecodedMethod *decoded);

#endif
