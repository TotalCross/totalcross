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

//////////////////////////////////////////////////////////////////////////
TC_API void tucL_create(NMParams p) // totalcross/util/concurrent/Lock native void create();
{
   TCObject obj = p->obj[0];
   TCObject mutexObj;
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
   TCObject obj = p->obj[0];
   TCObject mutexObj = Lock_mutex(obj);
   if (mutexObj != null)
   {
      MUTEX_TYPE *mo = (MUTEX_TYPE *)ARRAYOBJ_START(mutexObj);
      DESTROY_MUTEX_VAR(*mo);
      setObjectLock(mutexObj, UNLOCKED);
      Lock_mutex(obj) = null;
   }
}
