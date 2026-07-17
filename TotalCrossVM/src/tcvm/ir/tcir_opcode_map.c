// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_opcode_map.h"

#include "../opcodes.h"
#include "tcir_internal.h"

#include <string.h>

#define TCIR_OPCODE(value, name, decoder, lowering, poc) \
   typedef char tcir_opcode_value_check_##name[((name) == (value)) ? 1 : -1];
#include "tcir_opcode_registry.def"
#undef TCIR_OPCODE

typedef char tcir_opcode_count_check[(OPCODE_LENGTH == 160) ? 1 : -1];

static const TCIROpcodeInfo tcir_opcodes[] = {
#define TCIR_OPCODE(value, name, decoder, lowering, poc) \
   { (value), #name, TCIR_DECODER_##decoder, TCIR_LOWERING_##lowering, TCIR_POC_##poc },
#include "tcir_opcode_registry.def"
#undef TCIR_OPCODE
};

size_t tcirOpcodeCount(void)
{
   return sizeof(tcir_opcodes) / sizeof(tcir_opcodes[0]);
}

const TCIROpcodeInfo *tcirOpcodeAt(size_t index)
{
   return index < tcirOpcodeCount() ? &tcir_opcodes[index] : NULL;
}

const TCIROpcodeInfo *tcirOpcodeLookup(unsigned int value)
{
   return value < tcirOpcodeCount() && tcir_opcodes[value].value == value ? &tcir_opcodes[value] : NULL;
}

const char *tcirDecoderClassName(TCIRDecoderClass decoder_class)
{
   static const char *const names[] = { "single", "call", "switch", "multiarray" };
   return (unsigned int)decoder_class < 4U ? names[(unsigned int)decoder_class] : "invalid";
}

const char *tcirLoweringClassName(TCIRLoweringClass lowering_class)
{
   static const char *const names[] = {
      "direct",
      "lowered",
      "runtime-helper",
      "unsupported-in-poc",
      "future",
      "obsolete",
      "platform-specific",
      "needs-investigation"
   };
   return (unsigned int)lowering_class < 8U ? names[(unsigned int)lowering_class] : "invalid";
}

const char *tcirPOCStatusName(TCIRPOCStatus status)
{
   static const char *const names[] = { "supported", "fallback", "investigate" };
   return (unsigned int)status < 3U ? names[(unsigned int)status] : "invalid";
}

int tcirOpcodeRegistryValidate(TCIRDiagnostic *diagnostic)
{
   size_t count = tcirOpcodeCount();
   size_t index;
   size_t other;

   tcirDiagnosticClear(diagnostic);
   if (count != OPCODE_LENGTH)
   {
      tcirSetDiagnostic(
         diagnostic,
         TCIR_DIAGNOSTIC_OPCODE_REGISTRY,
         "<opcode-registry>",
         0,
         "registry has %u entries but opcodes.h declares %u",
         (unsigned int)count,
         (unsigned int)OPCODE_LENGTH);
      return 0;
   }

   for (index = 0; index < count; index++)
   {
      const TCIROpcodeInfo *info = &tcir_opcodes[index];
      if (info->value != index || info->name == NULL || info->name[0] == '\0' ||
          (unsigned int)info->decoder_class > (unsigned int)TCIR_DECODER_MULTIARRAY ||
          (unsigned int)info->lowering_class > (unsigned int)TCIR_LOWERING_NEEDS_INVESTIGATION ||
          (unsigned int)info->poc_status > (unsigned int)TCIR_POC_INVESTIGATE)
      {
         tcirSetDiagnostic(
            diagnostic,
            TCIR_DIAGNOSTIC_OPCODE_REGISTRY,
            "<opcode-registry>",
            (unsigned int)index,
            "opcode %u has an invalid or missing disposition",
            (unsigned int)index);
         return 0;
      }
      for (other = index + 1; other < count; other++)
      {
         if (info->value == tcir_opcodes[other].value || strcmp(info->name, tcir_opcodes[other].name) == 0)
         {
            tcirSetDiagnostic(
               diagnostic,
               TCIR_DIAGNOSTIC_OPCODE_REGISTRY,
               "<opcode-registry>",
               info->value,
               "opcode %s is duplicated",
               info->name);
            return 0;
         }
      }
   }
   return 1;
}
