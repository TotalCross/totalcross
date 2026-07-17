// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_jit_memory.h"

#if !defined(SLJIT_WX_EXECUTABLE_ALLOCATOR) || !SLJIT_WX_EXECUTABLE_ALLOCATOR
#error "The TotalCross SLJIT backend requires the W^X executable allocator"
#endif

#if defined(SLJIT_PROT_EXECUTABLE_ALLOCATOR) && SLJIT_PROT_EXECUTABLE_ALLOCATOR
#error "The TotalCross SLJIT backend expects the page-transition W^X allocator"
#endif

void *tcirJitExecutableMemoryFinalize(struct sljit_compiler *compiler, size_t *code_size)
{
   void *code;

   if (compiler == NULL || code_size == NULL)
      return NULL;
   code = sljit_generate_code(compiler, 0, NULL);
   *code_size = code == NULL ? 0U : (size_t)sljit_get_generated_code_size(compiler);
   return code;
}

void tcirJitExecutableMemoryDispose(void *code)
{
   if (code != NULL)
      sljit_free_code(code, NULL);
}

const char *tcirJitExecutableMemoryPlatformName(void)
{
   return sljit_get_platform_name();
}
