// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

static Err cieloPrintManagerPrintText (TCObject textToPrint, TCObject printerAttributes, TCObject printerListener) {
   JNIEnv* env = getJNIEnv();
   jmethodID cieloPrintManagerPrintTextMethod = (*env)->GetStaticMethodID(env, jCieloPrinterManager4A, "cieloPrintManagerPrintText", "(Ljava/lang/String;Ljava/lang/String;)V");
   jstring jTextToPrint = (*env)->NewString(env, String_charsStart(textToPrint), String_charsLen(textToPrint));
   jstring jPrinterAttributes = (printerAttributes != null) ? (*env)->NewString(env, String_charsStart(printerAttributes), String_charsLen(printerAttributes)) : null;
   
   (*env)->CallStaticObjectMethod(env, jCieloPrinterManager4A, cieloPrintManagerPrintTextMethod, jTextToPrint, jPrinterAttributes);
   (*env)->DeleteLocalRef(env, jTextToPrint);
   if (jPrinterAttributes != null) {
      (*env)->DeleteLocalRef(env, jPrinterAttributes);
   }
   return NO_ERROR;
}
