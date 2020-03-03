/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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
