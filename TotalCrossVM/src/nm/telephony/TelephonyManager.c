// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "TelephonyManager.h"

#if defined(ANDROID)
#include "android/TelephonyManager_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void ttTM_nativeInitialize(NMParams p) // totalcross/telephony/TelephonyManager public void nativeInitialize();
{
    TCObject telephonyManager = p->obj[0];
    TCObject* deviceIds = &TelephonyManager_deviceIds(telephonyManager);
    TCObject* simSerialNumbers = &TelephonyManager_simSerialNumbers(telephonyManager);
    TCObject* lineNumbers = &TelephonyManager_lineNumbers(telephonyManager);

#if defined (ANDROID)
    p->retI =
        android_ttTM_nativeInitialize(
            p->currentContext,
            deviceIds,
            simSerialNumbers,
            lineNumbers)
        == NO_ERROR;
#endif
}
#ifdef ENABLE_TEST_SUITE
//#include "TelephonyManager_test.h"
#endif
