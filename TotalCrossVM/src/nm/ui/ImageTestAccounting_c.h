// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef IMAGE_TEST_ACCOUNTING_C_H
#define IMAGE_TEST_ACCOUNTING_C_H

#include <string.h>

typedef struct {
   int32 enabled;
   int32* targetedDecodeInvocationCount;
   int32* targetedDecodeRequestWidth;
   int32* targetedDecodeRequestHeight;
   int32* targetedDecodeDenominator;
   int32* targetedDecodeWidth;
   int32* targetedDecodeHeight;
   int32* fullDecodeInvocationCount;
   int32* nativeGeometryMaterializationCount;
   int32* nativeColorReadbackCount;
   int32* directDrawPlanExecutionCount;
   int32* zeroCopyDecodeCount;
   int32* copiedDecodeCount;
   int32* decodeCopiedBytes;
   int32* decodeFinalBufferBytes;
} ImageTestAccountingState;

extern ImageTestAccountingState imageTestAccountingState;

static void imageSetTestAccounting(Context context, int32 enabled) {
   TCClass imageClass;
   imageTestAccountingState.enabled = enabled != 0;
   imageTestAccountingState.targetedDecodeInvocationCount = null;
   imageTestAccountingState.targetedDecodeRequestWidth = null;
   imageTestAccountingState.targetedDecodeRequestHeight = null;
   imageTestAccountingState.targetedDecodeDenominator = null;
   imageTestAccountingState.targetedDecodeWidth = null;
   imageTestAccountingState.targetedDecodeHeight = null;
   imageTestAccountingState.fullDecodeInvocationCount = null;
   imageTestAccountingState.nativeGeometryMaterializationCount = null;
   imageTestAccountingState.nativeColorReadbackCount = null;
   imageTestAccountingState.directDrawPlanExecutionCount = null;
   imageTestAccountingState.zeroCopyDecodeCount = null;
   imageTestAccountingState.copiedDecodeCount = null;
   imageTestAccountingState.decodeCopiedBytes = null;
   imageTestAccountingState.decodeFinalBufferBytes = null;
   if (!imageTestAccountingState.enabled) {
      return;
   }
   imageClass = loadClass(context, "totalcross.ui.image.Image", false);
   if (!imageClass) {
      return;
   }
   imageTestAccountingState.targetedDecodeInvocationCount =
      getStaticFieldInt(imageClass, "targetedDecodeInvocationCountForTest");
   imageTestAccountingState.targetedDecodeRequestWidth =
      getStaticFieldInt(imageClass, "targetedDecodeRequestWidthForTest");
   imageTestAccountingState.targetedDecodeRequestHeight =
      getStaticFieldInt(imageClass, "targetedDecodeRequestHeightForTest");
   imageTestAccountingState.targetedDecodeDenominator =
      getStaticFieldInt(imageClass, "targetedDecodeDenominatorForTest");
   imageTestAccountingState.targetedDecodeWidth =
      getStaticFieldInt(imageClass, "targetedDecodeWidthForTest");
   imageTestAccountingState.targetedDecodeHeight =
      getStaticFieldInt(imageClass, "targetedDecodeHeightForTest");
   imageTestAccountingState.fullDecodeInvocationCount =
      getStaticFieldInt(imageClass, "fullDecodeInvocationCountForTest");
   imageTestAccountingState.nativeGeometryMaterializationCount =
      getStaticFieldInt(imageClass, "nativeGeometryMaterializationCountForTest");
   imageTestAccountingState.nativeColorReadbackCount =
      getStaticFieldInt(imageClass, "nativeColorReadbackCountForTest");
   imageTestAccountingState.directDrawPlanExecutionCount =
      getStaticFieldInt(imageClass, "directDrawPlanExecutionCountForTest");
   imageTestAccountingState.zeroCopyDecodeCount =
      getStaticFieldInt(imageClass, "zeroCopyDecodeCountForTest");
   imageTestAccountingState.copiedDecodeCount =
      getStaticFieldInt(imageClass, "copiedDecodeCountForTest");
   imageTestAccountingState.decodeCopiedBytes =
      getStaticFieldInt(imageClass, "decodeCopiedBytesForTest");
   imageTestAccountingState.decodeFinalBufferBytes =
      getStaticFieldInt(imageClass, "decodeFinalBufferBytesForTest");
}

static int32* imageTestAccountingField(const char* fieldName) {
   if (!imageTestAccountingState.enabled) {
      return null;
   }
   if (strcmp(fieldName, "targetedDecodeInvocationCountForTest") == 0) {
      return imageTestAccountingState.targetedDecodeInvocationCount;
   }
   if (strcmp(fieldName, "targetedDecodeRequestWidthForTest") == 0) {
      return imageTestAccountingState.targetedDecodeRequestWidth;
   }
   if (strcmp(fieldName, "targetedDecodeRequestHeightForTest") == 0) {
      return imageTestAccountingState.targetedDecodeRequestHeight;
   }
   if (strcmp(fieldName, "targetedDecodeDenominatorForTest") == 0) {
      return imageTestAccountingState.targetedDecodeDenominator;
   }
   if (strcmp(fieldName, "targetedDecodeWidthForTest") == 0) {
      return imageTestAccountingState.targetedDecodeWidth;
   }
   if (strcmp(fieldName, "targetedDecodeHeightForTest") == 0) {
      return imageTestAccountingState.targetedDecodeHeight;
   }
   if (strcmp(fieldName, "fullDecodeInvocationCountForTest") == 0) {
      return imageTestAccountingState.fullDecodeInvocationCount;
   }
   if (strcmp(fieldName, "nativeGeometryMaterializationCountForTest") == 0) {
      return imageTestAccountingState.nativeGeometryMaterializationCount;
   }
   if (strcmp(fieldName, "nativeColorReadbackCountForTest") == 0) {
      return imageTestAccountingState.nativeColorReadbackCount;
   }
   if (strcmp(fieldName, "directDrawPlanExecutionCountForTest") == 0) {
      return imageTestAccountingState.directDrawPlanExecutionCount;
   }
   if (strcmp(fieldName, "zeroCopyDecodeCountForTest") == 0) {
      return imageTestAccountingState.zeroCopyDecodeCount;
   }
   if (strcmp(fieldName, "copiedDecodeCountForTest") == 0) {
      return imageTestAccountingState.copiedDecodeCount;
   }
   if (strcmp(fieldName, "decodeCopiedBytesForTest") == 0) {
      return imageTestAccountingState.decodeCopiedBytes;
   }
   if (strcmp(fieldName, "decodeFinalBufferBytesForTest") == 0) {
      return imageTestAccountingState.decodeFinalBufferBytes;
   }
   return null;
}

static void imageRecordTestCounter(const char* fieldName) {
   int32* counter = imageTestAccountingField(fieldName);
   if (counter) {
      (*counter)++;
   }
}

static void imageAddTestCounter(const char* fieldName, int32 amount) {
   int32* counter = imageTestAccountingField(fieldName);
   if (counter) {
      (*counter) += amount;
   }
}

#endif
