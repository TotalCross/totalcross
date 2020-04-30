// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined(ANDROID)
#include "android/PrinterManager_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tcspPM_internalPrintText_ssp(NMParams p) // totalcross/cielo/sdk/printer/PrinterManager private native void internalPrintText(String textToPrint, String printerAttributes, totalcross.cielo.printer.PrinterManager.PrinterListenerInternal printerListener);
{
    TCObject printerManager = p->obj[0];
    TCObject textToPrint = p->obj[1];
    TCObject printerAttributes = p->obj[2];
    TCObject printerListener = p->obj[3];
    
#if defined (ANDROID)
    // first, lock the printerListener to make sure it won't be collected by the GC
    if (printerListener != null) {
        setObjectLock(printerListener, LOCKED);
    }
    // make native android call
    cieloPrintManagerPrintText(textToPrint, printerAttributes, printerListener);
#endif
}
#ifdef ENABLE_TEST_SUITE
//#include "PrinterManager_test.h"
#endif
