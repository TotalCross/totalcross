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
TC_API void jlO_toStringNative(NMParams p) // java/lang/Object native private String toStringNative();
{
   TCClass c;
   TCObject obj;
   int32 len;

   obj = p->obj[0]; // class object
   c = OBJ_CLASS(obj);
   len = xstrlen(c->name);
   if ((p->retO = createStringObjectWithLen(p->currentContext, len + 1 + 8)) != null)
   {
      char hex[9];
      JCharP rc = String_charsStart(p->retO);
      CharP2JCharPBuf(c->name, len, rc, false);
      rc += len;
      *rc++ = '@';
      int2hex((int32)obj, 8, hex); // use the address as the number that goes after the @
      CharP2JCharPBuf(hex, 8, rc, false);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlO_getClass(NMParams p) // java/lang/Object native public final Class getClass();
{
   TCClass c;
   TCObject thisObj, classObj, ptrObj;

   p->retO = classObj = createObject(p->currentContext, "java.lang.Class");
   if (classObj != null && (Class_nativeStruct(classObj) = ptrObj = createByteArray(p->currentContext, PTRSIZE)) != null)
   {
      thisObj = p->obj[0];
      c = OBJ_CLASS(thisObj);
      xmoveptr(ARRAYOBJ_START(ptrObj), &c);
      Class_targetName(classObj) = createStringObjectFromCharP(p->currentContext, c->name, -1);
      setObjectLock(ptrObj, UNLOCKED);
      setObjectLock(Class_targetName(classObj), UNLOCKED);
   }
   if (p->retO != null)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlO_nativeHashCode(NMParams p) // java/lang/Object native private int nativeHashCode();
{
   p->retI = (int32)p->obj[0]; // return the address as the hashcode
}

#ifdef ENABLE_TEST_SUITE
#include "Object_test.h"
#endif
