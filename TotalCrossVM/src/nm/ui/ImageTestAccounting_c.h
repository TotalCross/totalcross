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
   int32* detachedDecodeOptimizationMask;
   int32* fullDecodeInvocationCount;
   int32* nativeGeometryMaterializationCount;
   int32* nativeColorReadbackCount;
   int32* directDrawPlanExecutionCount;
   int32* zeroCopyDecodeCount;
   int32* copiedDecodeCount;
   int32* decodeCopiedBytes;
   int32* decodeFinalBufferBytes;
   int32* opacityKnownFromSource;
   int32* opacityDeterminedDuringDecode;
   int32* opacityFallbackScans;
   int32* opacityFallbackPixels;
   int32* directColorMaterializationCount;
   int64* jpegNativeDecodeCount;
   int64* jpegNativeDecodeNs;
   int64* jpegNativeDecodeFullCount;
   int64* jpegNativeDecodeFullNs;
   int64* jpegNativeDecodeHalfCount;
   int64* jpegNativeDecodeHalfNs;
   int64* jpegNativeDecodeQuarterCount;
   int64* jpegNativeDecodeQuarterNs;
   int64* jpegNativeDecodeEighthCount;
   int64* jpegNativeDecodeEighthNs;
   int64* jpegNativeDecodeOtherCount;
   int64* jpegNativeDecodeOtherNs;
   int64* jpegNativeDecodeRequestedFullCount;
   int64* jpegNativeDecodeRequestedTargetCount;
   int64* jpegNativeDecodeRequestedExplicitRatioCount;
   int64* jpegNativeDecodeRequestedBestFitCount;
   int64* jpegNativeDecodeFailureCount;
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
   imageTestAccountingState.detachedDecodeOptimizationMask = null;
   imageTestAccountingState.fullDecodeInvocationCount = null;
   imageTestAccountingState.nativeGeometryMaterializationCount = null;
   imageTestAccountingState.nativeColorReadbackCount = null;
   imageTestAccountingState.directDrawPlanExecutionCount = null;
   imageTestAccountingState.zeroCopyDecodeCount = null;
   imageTestAccountingState.copiedDecodeCount = null;
   imageTestAccountingState.decodeCopiedBytes = null;
   imageTestAccountingState.decodeFinalBufferBytes = null;
   imageTestAccountingState.opacityKnownFromSource = null;
   imageTestAccountingState.opacityDeterminedDuringDecode = null;
   imageTestAccountingState.opacityFallbackScans = null;
   imageTestAccountingState.opacityFallbackPixels = null;
   imageTestAccountingState.directColorMaterializationCount = null;
   imageTestAccountingState.jpegNativeDecodeCount = null;
   imageTestAccountingState.jpegNativeDecodeNs = null;
   imageTestAccountingState.jpegNativeDecodeFullCount = null;
   imageTestAccountingState.jpegNativeDecodeFullNs = null;
   imageTestAccountingState.jpegNativeDecodeHalfCount = null;
   imageTestAccountingState.jpegNativeDecodeHalfNs = null;
   imageTestAccountingState.jpegNativeDecodeQuarterCount = null;
   imageTestAccountingState.jpegNativeDecodeQuarterNs = null;
   imageTestAccountingState.jpegNativeDecodeEighthCount = null;
   imageTestAccountingState.jpegNativeDecodeEighthNs = null;
   imageTestAccountingState.jpegNativeDecodeOtherCount = null;
   imageTestAccountingState.jpegNativeDecodeOtherNs = null;
   imageTestAccountingState.jpegNativeDecodeRequestedFullCount = null;
   imageTestAccountingState.jpegNativeDecodeRequestedTargetCount = null;
   imageTestAccountingState.jpegNativeDecodeRequestedExplicitRatioCount = null;
   imageTestAccountingState.jpegNativeDecodeRequestedBestFitCount = null;
   imageTestAccountingState.jpegNativeDecodeFailureCount = null;
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
   imageTestAccountingState.detachedDecodeOptimizationMask =
      getStaticFieldInt(imageClass, "detachedDecodeOptimizationMaskForTest");
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
   imageTestAccountingState.opacityKnownFromSource =
      getStaticFieldInt(imageClass, "opacityKnownFromSourceForTest");
   imageTestAccountingState.opacityDeterminedDuringDecode =
      getStaticFieldInt(imageClass, "opacityDeterminedDuringDecodeForTest");
   imageTestAccountingState.opacityFallbackScans =
      getStaticFieldInt(imageClass, "opacityFallbackScansForTest");
   imageTestAccountingState.opacityFallbackPixels =
      getStaticFieldInt(imageClass, "opacityFallbackPixelsForTest");
   imageTestAccountingState.directColorMaterializationCount =
      getStaticFieldInt(imageClass, "directColorMaterializationCountForTest");
   imageTestAccountingState.jpegNativeDecodeCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeCountForTest");
   imageTestAccountingState.jpegNativeDecodeNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeNsForTest");
   imageTestAccountingState.jpegNativeDecodeFullCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeFullCountForTest");
   imageTestAccountingState.jpegNativeDecodeFullNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeFullNsForTest");
   imageTestAccountingState.jpegNativeDecodeHalfCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeHalfCountForTest");
   imageTestAccountingState.jpegNativeDecodeHalfNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeHalfNsForTest");
   imageTestAccountingState.jpegNativeDecodeQuarterCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeQuarterCountForTest");
   imageTestAccountingState.jpegNativeDecodeQuarterNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeQuarterNsForTest");
   imageTestAccountingState.jpegNativeDecodeEighthCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeEighthCountForTest");
   imageTestAccountingState.jpegNativeDecodeEighthNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeEighthNsForTest");
   imageTestAccountingState.jpegNativeDecodeOtherCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeOtherCountForTest");
   imageTestAccountingState.jpegNativeDecodeOtherNs =
      getStaticFieldLong(imageClass, "jpegNativeDecodeOtherNsForTest");
   imageTestAccountingState.jpegNativeDecodeRequestedFullCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeRequestedFullCountForTest");
   imageTestAccountingState.jpegNativeDecodeRequestedTargetCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeRequestedTargetCountForTest");
   imageTestAccountingState.jpegNativeDecodeRequestedExplicitRatioCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeRequestedExplicitRatioCountForTest");
   imageTestAccountingState.jpegNativeDecodeRequestedBestFitCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeRequestedBestFitCountForTest");
   imageTestAccountingState.jpegNativeDecodeFailureCount =
      getStaticFieldLong(imageClass, "jpegNativeDecodeFailureCountForTest");
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
   if (strcmp(fieldName, "detachedDecodeOptimizationMaskForTest") == 0) {
      return imageTestAccountingState.detachedDecodeOptimizationMask;
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
   if (strcmp(fieldName, "opacityKnownFromSourceForTest") == 0) {
      return imageTestAccountingState.opacityKnownFromSource;
   }
   if (strcmp(fieldName, "opacityDeterminedDuringDecodeForTest") == 0) {
      return imageTestAccountingState.opacityDeterminedDuringDecode;
   }
   if (strcmp(fieldName, "opacityFallbackScansForTest") == 0) {
      return imageTestAccountingState.opacityFallbackScans;
   }
   if (strcmp(fieldName, "opacityFallbackPixelsForTest") == 0) {
      return imageTestAccountingState.opacityFallbackPixels;
   }
   if (strcmp(fieldName, "directColorMaterializationCountForTest") == 0) {
      return imageTestAccountingState.directColorMaterializationCount;
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

static int64* imageTestAccountingLongField(const char* fieldName) {
   if (!imageTestAccountingState.enabled) {
      return null;
   }
   if (strcmp(fieldName, "jpegNativeDecodeCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeFullCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeFullCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeFullNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeFullNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeHalfCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeHalfCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeHalfNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeHalfNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeQuarterCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeQuarterCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeQuarterNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeQuarterNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeEighthCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeEighthCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeEighthNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeEighthNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeOtherCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeOtherCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeOtherNsForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeOtherNs;
   }
   if (strcmp(fieldName, "jpegNativeDecodeRequestedFullCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeRequestedFullCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeRequestedTargetCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeRequestedTargetCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeRequestedExplicitRatioCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeRequestedExplicitRatioCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeRequestedBestFitCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeRequestedBestFitCount;
   }
   if (strcmp(fieldName, "jpegNativeDecodeFailureCountForTest") == 0) {
      return imageTestAccountingState.jpegNativeDecodeFailureCount;
   }
   return null;
}

static void imageRecordTestLongCounter(const char* fieldName) {
   int64* counter = imageTestAccountingLongField(fieldName);
   if (counter) {
      (*counter)++;
   }
}

static void imageAddTestLongCounter(const char* fieldName, int64 amount) {
   int64* counter = imageTestAccountingLongField(fieldName);
   if (counter) {
      (*counter) += amount;
   }
}

#endif
