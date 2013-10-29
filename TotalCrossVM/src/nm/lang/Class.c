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

#define JFLAG_PUBLIC       1
#define JFLAG_PRIVATE      2
#define JFLAG_PROTECTED    4
#define JFLAG_STATIC       8
#define JFLAG_FINAL        16
#define JFLAG_SYNCHRONIZED 32
#define JFLAG_VOLATILE     64
#define JFLAG_TRANSIENT    128
#define JFLAG_NATIVE       256
#define JFLAG_INTERFACE    512
#define JFLAG_ABSTRACT     1024

static void createFieldObject(Context currentContext, Field f, Object* ret)
{
}

static void createClassObject(Context currentContext, CharP className, Object* ret)
{
   Object ptrObj=null;
   TCClass c = loadClass(currentContext, className, true);
   if (c != null && c->classObj != null) // if the object was already created, reuse it.
      *ret = c->classObj; // no need to unlock it
   else
   {
      *ret = null;
      if (c != null &&
         (*ret = createObject(currentContext, "java.lang.Class")) != null &&
         (ptrObj = createByteArray(currentContext, PTRSIZE)) != null)
      {
         xmoveptr(ARRAYOBJ_START(ptrObj), &c);
         setObjectLock(Class_targetName(*ret) = createStringObjectFromCharP(currentContext,className,-1),UNLOCKED);
      }
      if (ptrObj != null)
      {
         setObjectLock(Class_targetClass(*ret) = ptrObj, UNLOCKED);
         if (*ret != null)
            c->classObj = *ret;
      }
      if (*ret != null) // unlock the returning object
         setObjectLock(*ret, UNLOCKED);
   }
}

//////////////////////////////////////////////////////////////////////////
TC_API void jlC_forName_s(NMParams p) // java/lang/Class native public static Class forName(String className) throws java.lang.ClassNotFoundException;
{
   Object classNameObj;
   classNameObj = p->obj[0];
   if (classNameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   {
      CharP className = String2CharP(classNameObj);
      if (className == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         createClassObject(p->currentContext, className, &p->retO);
         xfree(className);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_newInstance(NMParams p) // java/lang/Class native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException;
{
   TCClass target;
   Object me = p->obj[0];

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
   Object me = p->obj[0];
   Object other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = areClassesCompatible(p->currentContext, OBJ_CLASS(me), OBJ_CLASS(other)->name) == COMPATIBLE;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isAssignableFrom_c(NMParams p) // java/lang/Class public native boolean isAssignableFrom(Class cls);
{
   Object me = p->obj[0];
   Object cls = p->obj[1];
   if (cls == null)
      throwException(p->currentContext, NullPointerException, "Argument cls");
   else
      p->retI = areClassesCompatible(p->currentContext, OBJ_CLASS(me), OBJ_CLASS(cls)->name) == COMPATIBLE;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isInterface(NMParams p) // java/lang/Class public native boolean isInterface();
{
   Object me = p->obj[0];
   p->retI = OBJ_CLASS(me)->flags.isInterface;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isArray(NMParams p) // java/lang/Class public native boolean isArray();
{
   Object me = p->obj[0];
   p->retI = OBJ_CLASS(me)->flags.isArray;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isPrimitive(NMParams p) // java/lang/Class public native boolean isPrimitive();
{
   Object me = p->obj[0];
   CharP name = OBJ_CLASS(me)->name;
   p->retI = strEq(name,"java.lang.Boolean") || strEq(name,"java.lang.Byte") || strEq(name,"java.lang.Short") ||
      strEq(name,"java.lang.Integer") || strEq(name,"java.lang.Long") || strEq(name,"java.lang.Float") || strEq(name,"java.lang.Double") || strEq(name,"java.lang.Character");
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getSuperclass(NMParams p) // java/lang/Class public native Class getSuperclass();
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
   if (c->superClass != null && !c->flags.isInterface)
      createClassObject(p->currentContext, c->superClass->name, &p->retO);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getInterfaces(NMParams p) // java/lang/Class public native java.lang.Class[] getInterfaces();
{
   Object me = p->obj[0], ret;
   TCClass c = OBJ_CLASS(me);
   int32 n = ARRAYLENV(c->interfaces),i;
   if (n > 0 && (ret = createArrayObject(p->currentContext, "java.lang.Class[", n)) != null)
   {
      Object* objs = (Object*)ARRAYOBJ_START(ret);
      for (i = 0; i < n; i++)
         createClassObject(p->currentContext, c->interfaces[i]->name, &objs[i]);
      setObjectLock(p->retO = ret, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
extern Object *booleanTYPE, *byteTYPE, *shortTYPE, *intTYPE, *longTYPE, *floatTYPE, *doubleTYPE, *charTYPE;
TC_API void jlC_getComponentType(NMParams p) // java/lang/Class public native Class getComponentType();
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
   if (c->flags.isArray)
   {
      CharP name = c->name;
      Type to = type2javaType(c->name);
      switch (to)
      {
         case Type_Byte:    p->retO = *byteTYPE; break;
         case Type_Boolean: p->retO = *booleanTYPE; break;
         case Type_Short:   p->retO = *shortTYPE; break;
         case Type_Char:    p->retO = *charTYPE; break;
         case Type_Int:     p->retO = *intTYPE; break;
         case Type_Long:    p->retO = *longTYPE; break;
         case Type_Float:   p->retO = *floatTYPE; break;
         case Type_Double:  p->retO = *doubleTYPE; break;
         default:
         {
            int32 len = xstrlen(name);
            CharP temp = xmalloc(len);
            if (!temp)
               throwException(p->currentContext, OutOfMemoryError, "To allocate %d bytes", len);
            else
            {
               xmemmove(temp, name, len-1); // cut the last [
               createClassObject(p->currentContext, temp, &p->retO);
               xfree(temp);
            }
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getModifiers(NMParams p) // java/lang/Class public native int getModifiers();
{
   Object me = p->obj[0];
   ClassFlags f = OBJ_CLASS(me)->flags;
   int32 ret = 0;
   if (f.isPublic) ret |= JFLAG_PUBLIC;
   if (f.isStatic) ret |= JFLAG_STATIC;
   if (f.isFinal)  ret |= JFLAG_FINAL;
   if (f.isAbstract) ret |= JFLAG_ABSTRACT;
   if (f.isInterface) ret |= JFLAG_INTERFACE;
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getFields(NMParams p) // java/lang/Class public native java.lang.reflect.Field[] getFields() throws SecurityException;
{
   Object me = p->obj[0], ret=null;
   TCClass c = OBJ_CLASS(me), o;
   int32 count=0,i,n;
   FieldArray ff;
   if (c->flags.isInterface)
   {
      // count how many fields have in this interface and super interfaces
      // an interface has only public static fields.
      for (o = c; o != null; o = o->superClass)
         count += ARRAYLENV(o->i32StaticFields) + ARRAYLENV(o->objStaticFields) + ARRAYLENV(o->v64StaticFields);
      ret = createArrayObject(p->currentContext, "java.lang.reflect.Field[", count);
      if (ret)
      {
         Object* oa = (Object*)ARRAYOBJ_START(ret);
         for (o = c; o != null; o = o->superClass)
         {
            for (ff = o->i32StaticFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->objStaticFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->v64StaticFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) createFieldObject(p->currentContext, &ff[i], oa);
         }
      }
   }
   else
   {
      // count how many PUBLIC fields have in this class and super classes
      for (o = c; o != null; o = o->superClass)
      {
         for (ff = o->i32StaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
         for (ff = o->objStaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
         for (ff = o->v64StaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
         for (ff = o->i32InstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
         for (ff = o->objInstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
         for (ff = o->v64InstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++) if (ff[i].flags.isPublic) count++;
      }
      ret = createArrayObject(p->currentContext, "java.lang.reflect.Field[", count);
      if (ret)
      {
         Object* oa = (Object*)ARRAYOBJ_START(ret);
         for (o = c; o != null; o = o->superClass)
         {
            for (ff = o->i32StaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->objStaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->v64StaticFields  , i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->i32InstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->objInstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
            for (ff = o->v64InstanceFields, i = 0, n = ARRAYLENV(ff); i < n; i++, oa++) if (ff[i].flags.isPublic) createFieldObject(p->currentContext, &ff[i], oa);
         }
      }
   }
   setObjectLock(p->retO = ret, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getMethods(NMParams p) // java/lang/Class public native java.lang.reflect.Method[] getMethods() throws SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getConstructors(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor[] getConstructors() throws SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getField_s(NMParams p) // java/lang/Class public native java.lang.reflect.Field getField(String name) throws NoSuchFieldException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getMethod_sC(NMParams p) // java/lang/Class public native java.lang.reflect.Method getMethod(String name, Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getConstructor_C(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor getConstructor(Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredFields(NMParams p) // java/lang/Class public native java.lang.reflect.Field[] getDeclaredFields() throws SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredMethods(NMParams p) // java/lang/Class public native java.lang.reflect.Method[] getDeclaredMethods() throws SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredConstructors(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor[] getDeclaredConstructors() throws SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredField_s(NMParams p) // java/lang/Class public native java.lang.reflect.Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredMethod_sC(NMParams p) // java/lang/Class public native java.lang.reflect.Method getDeclaredMethod(String name, Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredConstructor_C(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor getDeclaredConstructor(Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   Object me = p->obj[0];
   TCClass c = OBJ_CLASS(me);
}

#ifdef ENABLE_TEST_SUITE
#include "Class_test.h"
#endif
