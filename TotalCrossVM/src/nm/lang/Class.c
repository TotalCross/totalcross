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

//////////////////////////////////////////////////////////////////////////
TC_API void jlC_forName_s(NMParams p) // java/lang/Class native public static Class forName(String className) throws java.lang.ClassNotFoundException;
{
   TCObject classNameObj,ptrObj=null;
   classNameObj = p->obj[0];
   if (classNameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   {
      TCClass c;
      CharP className = String2CharP(classNameObj);
      if (className == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         replaceChar(className, '/', '.');
         c = loadClass(p->currentContext, className, true);
         if (c != null &&
            (p->retO = createObject(p->currentContext, "java.lang.Class")) != null &&
            (ptrObj = createByteArray(p->currentContext, PTRSIZE)) != null)
         {
            xmoveptr(ARRAYOBJ_START(ptrObj), &c);
            Class_targetName(p->retO) = classNameObj;
         }
         if (p->retO != null)
            setObjectLock(p->retO, UNLOCKED);
         if (ptrObj != null)
            setObjectLock(Class_targetClass(p->retO) = ptrObj, UNLOCKED);
         xfree(className);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_newInstance(NMParams p) // java/lang/Class native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException;
{
   TCObject me;
   TCClass target;

   me = p->obj[0];
   xmoveptr(&target, ARRAYOBJ_START(Class_targetClass(me)));
   if (target->flags.isInterface || target->flags.isAbstract || target->flags.isArray)
      throwException(p->currentContext, InstantiationException, target->name);
   else
   {
      // check if the default constructor is public
      Method m = getMethod(target, false, CONSTRUCTOR_NAME, 0);
      if (!m || !m->flags.isPublic)
         throwException(p->currentContext, IllegalAccessException, target->name);
      else
      {
         p->retO = createObject(p->currentContext, target->name);
         setObjectLock(p->retO, UNLOCKED);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isInstance_o(NMParams p) // java/lang/Class native public boolean isInstance(Object obj);
{
   TCObject me, other;

   me = p->obj[0];
   other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = areClassesCompatible(p->currentContext, OBJ_CLASS(me), OBJ_CLASS(other)->name) == COMPATIBLE;
}

#ifdef ENABLE_TEST_SUITE
#include "Class_test.h"
#endif
