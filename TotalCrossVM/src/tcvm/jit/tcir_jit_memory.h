// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCIR_JIT_MEMORY_H
#define TCIR_JIT_MEMORY_H

#include <sljitLir.h>

#include <stddef.h>

void *tcirJitExecutableMemoryFinalize(struct sljit_compiler *compiler, size_t *code_size);
void tcirJitExecutableMemoryDispose(void *code);
const char *tcirJitExecutableMemoryPlatformName(void);

#endif
