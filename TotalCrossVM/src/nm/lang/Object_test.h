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



TESTCASE(jlO_toStringNative) // java/lang/Object native private String toStringNative();
{
   TNMParams p;
   Object obj;
   CharP buf=null;

   // test "new Object().toString()"
   tzero(p);
   p.currentContext = currentContext;
   obj = createObject(currentContext, "java.lang.Object");
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   jlO_toStringNative(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   buf = String2CharP(p.retO);
   ASSERT1_EQUALS(NotNull, buf);
   ASSERT3_EQUALS(Block, buf, "java.lang.Object", 16);
   ASSERT2_EQUALS(I32, buf[16], '@');
   ASSERT2_EQUALS(I32, xstrlen(buf), 25);

   finish:
   xfree(buf);
}
TESTCASE(jlO_getClass) // java/lang/Object native public final Class getClass();
{
   TNMParams p;
   Object obj,nameStr;
   CharP buf=null;
   TCClass c;

   // test "Class c = new Object().getClass()"
   tzero(p);
   p.currentContext = currentContext;
   obj = createObject(currentContext, "java.lang.Object");
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   jlO_getClass(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   c = OBJ_CLASS(p.retO);
   ASSERT2_EQUALS(Sz, c->name, "java.lang.Class");
   ASSERT1_EQUALS(NotNull, Class_nativeStruct(p.retO));
   ASSERT3_EQUALS(Block, *((TCClass*)ARRAYOBJ_START(Class_nativeStruct(p.retO))), OBJ_CLASS(obj), PTRSIZE);
   nameStr = Class_targetName(p.retO);
   ASSERT1_EQUALS(NotNull, nameStr);
   buf = String2CharP(nameStr);
   ASSERT2_EQUALS(Sz, buf, "java.lang.Object");
   finish:
   xfree(buf);
}
