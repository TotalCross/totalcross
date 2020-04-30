// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined (WIN32) || defined (WINCE)
#elif defined (ANDROID)
#elif defined (darwin)
#elif defined (HEADLESS) && defined (__arm__)
 #include "posix/Gpiod_c.h"
#endif

typedef struct _NATIVE_HANDLE {
    void* handle;
} NATIVE_HANDLE;

//////////////////////////////////////////////////////////////////////////
TC_API void tidgGC_open_i(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodChipObj = null;
    int32 bank  = p->i32[0];
    TCObject handleObj = null;
    NATIVE_HANDLE* nativeHandle = null;
    
    gpiodChipObj = createObject(p->currentContext, "totalcross.io.device.gpiod.GpiodChip");
    if (gpiodChipObj == null) {
        throwExceptionWithCode(p->currentContext, IOException, -1);
    } else if ((handleObj = createByteArray(p->currentContext, sizeof(NATIVE_HANDLE))) != null) {
        GpiodChip_handle(gpiodChipObj) = handleObj;
        setObjectLock(handleObj, UNLOCKED);
        nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(handleObj);
        
        nativeHandle->handle = gpiod_chip_open_by_number(bank);
    }
    
    p->retO = gpiodChipObj;
    setObjectLock(gpiodChipObj, UNLOCKED);
#else
    UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGL_open_gi(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodChipObj = p->obj[0];
    TCObject gpiodLineObj = null;
    int32 line  = p->i32[0];
    TCObject handleObj = null;
    NATIVE_HANDLE* nativeHandle = null;
    NATIVE_HANDLE* chipHandle = null;
    
    gpiodLineObj = createObject(p->currentContext, "totalcross.io.device.gpiod.GpiodLine");
    if (gpiodLineObj == null) {
        throwExceptionWithCode(p->currentContext, IOException, -2);
    } else if ((handleObj = createByteArray(p->currentContext, sizeof(NATIVE_HANDLE))) != null) {
        GpiodLine_handle(gpiodLineObj) = handleObj;
        setObjectLock(handleObj, UNLOCKED);
        nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(handleObj);
        
        chipHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(GpiodChip_handle(gpiodChipObj));
        nativeHandle->handle = gpiod_chip_get_line(chipHandle->handle, line);
    }
    
    p->retO = gpiodLineObj;
    setObjectLock(gpiodLineObj, UNLOCKED);
#else
    UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGL_requestOutput_si(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodLineObj = p->obj[0];
    NATIVE_HANDLE* lineHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(GpiodLine_handle(gpiodLineObj));
    TCObject consumerObj = p->obj[1];
    int32 defaultValue = p->i32[0];
    char* consumer = String2CharP(consumerObj);
  
    p->retI = gpiod_line_request_output(lineHandle->handle, consumer, defaultValue);
    xfree(consumer);
#else
    UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGL_setValue_i(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodLineObj = p->obj[0];
    NATIVE_HANDLE* lineHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(GpiodLine_handle(gpiodLineObj));
    int32 value = p->i32[0];

    p->retI = gpiod_line_set_value(lineHandle->handle, value);
#else
    UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGL_requestInput_s(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodLineObj = p->obj[0];
    NATIVE_HANDLE* lineHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(GpiodLine_handle(gpiodLineObj));
    TCObject consumerObj = p->obj[1];
    char* consumer = String2CharP(consumerObj);
  
    p->retI = gpiod_line_request_input(lineHandle->handle, consumer);
    xfree(consumer);
#else
    UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidgGL_getValue(NMParams p)
{
#if defined (HEADLESS) && defined (__arm__)
    TCObject gpiodLineObj = p->obj[0];
    NATIVE_HANDLE* lineHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(GpiodLine_handle(gpiodLineObj));

    p->retI = gpiod_line_get_value(lineHandle->handle);
#else
    UNUSED(p);
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "Gpiod_test.h"
#endif