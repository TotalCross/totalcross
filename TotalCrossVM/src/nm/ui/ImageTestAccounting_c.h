// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef IMAGE_TEST_ACCOUNTING_C_H
#define IMAGE_TEST_ACCOUNTING_C_H

static int32* imageTestAccountingField(Context context, const char* fieldName) {
   TCClass imageClass = loadClass(context, "totalcross.ui.image.Image", false);
   int32* enabled = imageClass == null ? null
      : getStaticFieldInt(imageClass, "imageOperationAccountingForTest");
   if (!enabled || !*enabled) {
      return null;
   }
   return getStaticFieldInt(imageClass, (CharP)fieldName);
}

static void imageRecordTestCounter(Context context, const char* fieldName) {
   int32* counter = imageTestAccountingField(context, fieldName);
   if (counter) {
      (*counter)++;
   }
}

#endif
