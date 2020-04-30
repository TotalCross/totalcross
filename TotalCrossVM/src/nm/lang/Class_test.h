// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



static TCObject jlC_JavaLangClassOfJavaLangString;
TESTCASE(jlC_forName_s) // java/lang/Class native public static Class forName(String className) throws java.lang.ClassNotFoundException;
{
   TNMParams p;
   TCObject obj,jls;
   TCClass c1,c2,c3;

   // Test if the Class of "java.lang.String" is the class of the created instance
   tzero(p);
   p.currentContext = currentContext;
   jls = createStringObjectFromCharP(currentContext, "java.lang.String",-1);
   setObjectLock(jls, UNLOCKED);
   ASSERT1_EQUALS(NotNull, jls);
   c1 = OBJ_CLASS(jls);
   p.obj = &jls;
   jlC_forName_s(&p);
   obj = p.retO;
   ASSERT1_EQUALS(NotNull, obj);
   c2 = OBJ_CLASS(obj);
   ASSERT2_EQUALS(Sz, c2->name, "java.lang.Class");
   c3 = *((TCClass*)ARRAYOBJ_START(Class_nativeStruct(obj)));
   ASSERT3_EQUALS(Block, c1, c3, TSIZE);
   jlC_JavaLangClassOfJavaLangString = obj;
   finish: ;
}
TESTCASE(jlC_newInstance) // java/lang/Class native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException; #DEPENDS(jlC_forName_s)
{
   TNMParams p;
   TCObject obj;
   TCClass c;

   // create an instance of the String class using the default constructor {"chars = new char[0]"}
   tzero(p);
   p.currentContext = currentContext;
   ASSERT1_EQUALS(NotNull, jlC_JavaLangClassOfJavaLangString);
   p.obj = &jlC_JavaLangClassOfJavaLangString;
   jlC_newInstance(&p);
   obj = p.retO;
   c = OBJ_CLASS(obj);
   ASSERT1_EQUALS(NotNull, obj);
   ASSERT2_EQUALS(Sz, c->name, "java.lang.String");
   ASSERT1_EQUALS(NotNull, String_chars(obj));
   ASSERT2_EQUALS(I32, String_charsLen(obj), 0);
   finish: ;
}
TESTCASE(jlC_isInstance_o) // java/lang/Class native public boolean isInstance(Object obj); #DEPENDS(jlC_newInstance)
{
   TCObject obj[2];
   TNMParams p;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = obj;

   // instance of itself?
   obj[1] = obj[0] = jlC_JavaLangClassOfJavaLangString;
   jlC_isInstance_o(&p);
   ASSERT1_EQUALS(True, p.retI);

   // instance of Object?
   p.retI = 0;
   obj[0] = jlC_JavaLangClassOfJavaLangString;
   obj[1] = createObject(currentContext, "java.lang.Object");
   setObjectLock(obj[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj[1]);
   jlC_isInstance_o(&p);
   ASSERT1_EQUALS(True, p.retI);

   // instance of Array?
   p.retI = 0;
   obj[0] = jlC_JavaLangClassOfJavaLangString;
   obj[1] = createObjectWithoutCallingDefaultConstructor(currentContext, "java.lang.Array");
   setObjectLock(obj[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj[1]);
   jlC_isInstance_o(&p);
   ASSERT1_EQUALS(False, p.retI);

   finish: ;
}
