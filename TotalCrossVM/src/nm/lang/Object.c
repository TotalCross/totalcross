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
   if (classObj != null && (Class_nativeStruct(classObj) = ptrObj = createByteArray(p->currentContext, TSIZE)) != null)
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
TC_API void jlO_clone(NMParams p) // java/lang/Object native protected Object clone() throws CloneNotSupportedException;
{
   TCObject thisObj = p->obj[0];
   TCClass thisClass = OBJ_CLASS(thisObj);
   TCObject cloneObj;
   int32 length;

   // If the class does not implement Cloneable, throws CloneNotSupportedException.
   if (!isSuperClass(thisClass, cloneable))
      throwExceptionNamed(p->currentContext, "java.lang.CloneNotSupportedException", "");
   else if (thisClass->flags.isArray)
   {
      // Array.clone().
      if (!(p->retO = cloneObj = createArrayObject(p->currentContext, thisClass->name, length = ARRAYOBJ_LEN(thisObj))))
         return;
      xmemmove(ARRAYOBJ_START(cloneObj), ARRAYOBJ_START(thisObj), TC_ARRAYSIZE(thisClass, length));
      setObjectLock(cloneObj, UNLOCKED);
   }
   else if ((p->retO = cloneObj = createObject(p->currentContext, thisClass->name)) != null)
   {
      FieldArray* allFields = thisClass->instanceFields;

      // int
      FieldArray fields = allFields[RegI];
      length = ARRAYLENV(fields);
      while (--length >= 0)
         FIELD_I32(cloneObj, length) = FIELD_I32(thisObj, length);

      // long
      fields = allFields[RegD];
      length = ARRAYLENV(fields);
      while (--length >= 0)
         FIELD_DBL(cloneObj, thisClass, length) = FIELD_DBL(thisObj, thisClass, length);

      // double
      fields = allFields[RegD];
      length = ARRAYLENV(fields);
      while (--length >= 0)
         FIELD_I64(cloneObj, thisClass, length) = FIELD_I64(thisObj, thisClass, length);

      // object
      fields = allFields[RegO];
      length = ARRAYLENV(fields);
      while (--length >= 0)
         FIELD_OBJ(cloneObj, thisClass, length) = FIELD_OBJ(thisObj, thisClass, length);
      
      setObjectLock(cloneObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////

TC_API void jlO_nativeHashCode(NMParams p) // java/lang/Object native private int nativeHashCode();
{
   p->retI = (int32)p->obj[0]; // return the address as the hashcode
}

#ifdef ENABLE_TEST_SUITE
#include "Object_test.h"
#endif
