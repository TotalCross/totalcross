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



#include "tcvm.h"

#if defined WP8

#elif defined WINCE || defined WIN32
 #include "win/CellInfo_c.h"
#elif defined(ANDROID)
 #include "android/CellInfo_c.h"
#else
 //#include "posix/CellInfo_c.h"
#endif

// static fields
static TCClass cellInfoClass;
#define ConnectionManager_connRef(c)      getStaticFieldObject(c, "connRef")

#define CellInfo_cellId(c)                *getStaticFieldObject(c, "cellId")
#define CellInfo_mnc(c)                   *getStaticFieldObject(c, "mnc")
#define CellInfo_mcc(c)                   *getStaticFieldObject(c, "mcc")
#define CellInfo_lac(c)                   *getStaticFieldObject(c, "lac")
#define CellInfo_signal(c)                *getStaticFieldInt(c, "signal")

//////////////////////////////////////////////////////////////////////////
TC_API void tpCI_loadResources(NMParams p) // totalcross/phone/CellInfo native private void loadResources();
{
   cellInfoClass = loadClass(p->currentContext, "totalcross.phone.CellInfo", false);
#if defined (WINCE)
   CellInfoLoadResources(p->currentContext);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpCI_releaseResources(NMParams p) // totalcross/phone/CellInfo native private void releaseResources();
{
#if defined (WINCE)
  CellInfoReleaseResources();
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpCI_update(NMParams p) // totalcross/phone/CellInfo native public static void update();
{                                        
#if defined (WINCE) || defined (ANDROID)
   int32 mcc=0, mnc=0, lac=0, cellId=0, signal=0;
   IntBuf ibuf;
   
   CellInfoUpdate(&mcc, &mnc, &lac, &cellId, &signal);
   CellInfo_mcc(cellInfoClass) = mcc == 0 ? null : createStringObjectFromCharP(p->currentContext, int2str(mcc, ibuf), -1);
   CellInfo_mnc(cellInfoClass) = mnc == 0 ? null : createStringObjectFromCharP(p->currentContext, int2str(mnc, ibuf), -1);
   CellInfo_lac(cellInfoClass) = lac == 0 ? null : createStringObjectFromCharP(p->currentContext, int2str(lac, ibuf), -1);
   CellInfo_cellId(cellInfoClass) = cellId == 0 ? null : createStringObjectFromCharP(p->currentContext, int2str(cellId, ibuf), -1);
   CellInfo_signal(cellInfoClass) = signal;       
#endif
}
