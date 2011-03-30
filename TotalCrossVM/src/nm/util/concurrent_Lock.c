/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tucL_create(NMParams p) // totalcross/util/concurrent/Lock native void create();
{
   Object obj = p->obj[0];
   Object mutexObj;
   mutexObj = Lock_mutex(obj) = createByteArray(p->currentContext, sizeof(MUTEX_TYPE));
   if (mutexObj != null)
   {
      MUTEX_TYPE *mo = (MUTEX_TYPE *)ARRAYOBJ_START(mutexObj);
      SETUP_MUTEX;
      INIT_MUTEX_VAR(*mo);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tucL_destroy(NMParams p) // totalcross/util/concurrent/Lock native void destroy();
{
   Object obj = p->obj[0];
   Object mutexObj = Lock_mutex(obj);
   if (mutexObj != null)
   {
      MUTEX_TYPE *mo = (MUTEX_TYPE *)ARRAYOBJ_START(mutexObj);
      DESTROY_MUTEX_VAR(*mo);
      setObjectLock(mutexObj, UNLOCKED);
      Lock_mutex(obj) = null;
   }
}
