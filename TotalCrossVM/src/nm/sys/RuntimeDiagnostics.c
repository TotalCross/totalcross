// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined(TC_ENABLE_RUNTIME_DIAGNOSTICS)

enum
{
   RUNTIME_DIAGNOSTIC_NATIVE_COUNTER = 0x2001,
   RUNTIME_DIAGNOSTIC_NATIVE_GAUGE = 0x2002,
   RUNTIME_DIAGNOSTIC_RUNTIME_GROUP = 1
};

static int64 runtimeDiagnosticNativeCounter;
static int64 runtimeDiagnosticNativeGauge;

/* The Java bridge serializes these accesses after its runtime group gate. */

static bool readRuntimeDiagnosticMetric(int32 metricId, int64 *value)
{
   switch (metricId)
   {
      case RUNTIME_DIAGNOSTIC_NATIVE_COUNTER:
         *value = runtimeDiagnosticNativeCounter;
         return true;
      case RUNTIME_DIAGNOSTIC_NATIVE_GAUGE:
         *value = runtimeDiagnosticNativeGauge;
         return true;
      default:
         return false;
   }
}

TC_API void tsRDS_readMetricNative_i(NMParams p) // totalcross/sys/RuntimeDiagnosticsSupport native private static long readMetricNative(int metricId);
{
   int64 value;
   if (!readRuntimeDiagnosticMetric(p->i32[0], &value))
   {
      throwException(p->currentContext, IllegalArgumentException, "Unknown runtime diagnostic metric id");
      return;
   }
   p->retL = value;
}

TC_API void tsRDS_readMetricsNative_IL(NMParams p) // totalcross/sys/RuntimeDiagnosticsSupport native private static void readMetricsNative(int []metricIds, long []values);
{
   TCObject metricIds = p->obj[0];
   TCObject values = p->obj[1];
   int32 count;
   int32 i;
   int32 *idValues;
   int64 *outputValues;

   if (metricIds == null || values == null)
   {
      throwException(p->currentContext, NullPointerException, "Metric id and output arrays are required");
      return;
   }
   count = ARRAYOBJ_LEN(metricIds);
   if (count != ARRAYOBJ_LEN(values))
   {
      throwException(p->currentContext, IllegalArgumentException, "Metric id and output lengths differ");
      return;
   }

   idValues = (int32 *)ARRAYOBJ_START(metricIds);
   outputValues = (int64 *)ARRAYOBJ_START(values);
   for (i = 0; i < count; i++)
   {
      int64 ignored;
      if (!readRuntimeDiagnosticMetric(idValues[i], &ignored))
      {
         throwException(p->currentContext, IllegalArgumentException, "Unknown runtime diagnostic metric id");
         return;
      }
   }
   for (i = 0; i < count; i++)
      readRuntimeDiagnosticMetric(idValues[i], &outputValues[i]);
}

TC_API void tsRDS_resetMetricsNative_i(NMParams p) // totalcross/sys/RuntimeDiagnosticsSupport native private static void resetMetricsNative(int groupMask);
{
   if (p->i32[0] & RUNTIME_DIAGNOSTIC_RUNTIME_GROUP)
      runtimeDiagnosticNativeCounter = 0;
}

TC_API void tsRDS_addNativeCountNative_l(NMParams p) // totalcross/sys/RuntimeDiagnosticsSupport native private static void addNativeCountNative(long delta);
{
   runtimeDiagnosticNativeCounter += p->i64[0];
}

TC_API void tsRDS_setNativeGaugeNative_l(NMParams p) // totalcross/sys/RuntimeDiagnosticsSupport native private static void setNativeGaugeNative(long value);
{
   runtimeDiagnosticNativeGauge = p->i64[0];
}

#endif
