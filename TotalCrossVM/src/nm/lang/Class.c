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

TC_API void jlC_forName_s(NMParams p);

static TCClass getTargetClass(TCObject o)
{
   TCClass ret = o ? OBJ_CLASS(o) : null;
   if (ret && strEq(ret->name, "java.lang.Class"))
	  xmoveptr(&ret, ARRAYOBJ_START(Class_nativeStruct(o)));
   return ret;
}
void createClassObject(Context currentContext, CharP className, Type type, TCObject* ret, bool* isNew)
{
   TCObject ptrObj=null;
   TCClass c = null;
   switch (type)
   {
      case Type_Void:    *ret = *voidTYPE;    return;
      case Type_Byte:    *ret = *byteTYPE;    return;
      case Type_Boolean: *ret = *booleanTYPE; return;
      case Type_Short:   *ret = *shortTYPE;   return;
      case Type_Char:    *ret = *charTYPE;    return;
      case Type_Int:     *ret = *intTYPE;     return;
      case Type_Long:    *ret = *longTYPE;    return;
      case Type_Float:   *ret = *floatTYPE;   return;
      case Type_Double:  *ret = *doubleTYPE;  return;
      default: break;
   }                                   
   if (*ret == null && strEqn(className, "java.lang.", 10))
   {
      char *p = className+10;            
      if (strEq(p, "Void")      && voidTYPE    != null) {*ret = *voidTYPE;    return;}
      if (strEq(p, "Byte")      && byteTYPE    != null) {*ret = *byteTYPE;    return;}
      if (strEq(p, "Boolean")   && booleanTYPE != null) {*ret = *booleanTYPE; return;}
      if (strEq(p, "Short")     && shortTYPE   != null) {*ret = *shortTYPE;   return;}
      if (strEq(p, "Character") && charTYPE    != null) {*ret = *charTYPE;    return;}
      if (strEq(p, "Integer")   && intTYPE     != null) {*ret = *intTYPE;     return;}
      if (strEq(p, "Long")      && longTYPE    != null) {*ret = *longTYPE;    return;}
      if (strEq(p, "Float")     && floatTYPE   != null) {*ret = *floatTYPE;   return;}
      if (strEq(p, "Double")    && doubleTYPE  != null) {*ret = *doubleTYPE;  return;}
   }      
   if (*ret == null)
   {
      c = loadClass(currentContext, className, true);
      if (c != null && c->classObj != null) // if the object was already created, reuse it.
         *ret = c->classObj; // no need to unlock it
   }
   if (*ret == null)
   {
      if (c != null &&
         (*ret = createObject(currentContext, "java.lang.Class")) != null &&
         (ptrObj = createByteArray(currentContext, TSIZE)) != null)
      {
         xmoveptr(ARRAYOBJ_START(ptrObj), &c);
         setObjectLock(Class_targetName(*ret) = createStringObjectFromCharP(currentContext,className,-1),UNLOCKED);
      }
      if (ptrObj != null)
      {
         setObjectLock(Class_nativeStruct(*ret) = ptrObj, UNLOCKED);
         if (*ret != null)
         {
            if (isNew) *isNew = true;
            c->classObj = *ret; // dont unlock the returning object since its cached!
         }
      }
   }
   if (*ret != null && isNew && c != null && c->name == className)
      *isNew = false;
}
static void createFieldObject(Context currentContext, Field f, int32 idx, TCObject* ret)
{
   TCObject ptrObj=null;
   *ret = null;
   if ((*ret = createObject(currentContext, "java.lang.reflect.Field")) != null)
   {
      // modifiers and index
      int32 mod=0;
      if (f->flags.isFinal    ) mod |= JFLAG_FINAL;
      if (f->flags.isPrivate  ) mod |= JFLAG_PRIVATE;
      if (f->flags.isProtected) mod |= JFLAG_PROTECTED;
      if (f->flags.isPublic   ) mod |= JFLAG_PUBLIC;
      if (f->flags.isStatic   ) mod |= JFLAG_STATIC;
      if (f->flags.isTransient) mod |= JFLAG_TRANSIENT;
      if (f->flags.isVolatile ) mod |= JFLAG_VOLATILE;
      Field_mod(*ret) = mod;
      Field_index(*ret) = idx;
      Field_primitiveType(*ret) = f->flags.type;
      // field ptr
      ptrObj = createByteArray(currentContext, TSIZE);
      if (ptrObj)
         xmoveptr(ARRAYOBJ_START(ptrObj), &f);
      setObjectLock(Field_nativeStruct(*ret) = ptrObj, UNLOCKED);
      // name, type and declaring class
      setObjectLock(Field_name(*ret) = createStringObjectFromCharP(currentContext,f->name,-1),UNLOCKED);
      createClassObject(currentContext, f->targetClassName, f->flags.type, &Field_type(*ret),null);
      createClassObject(currentContext, f->sourceClassName, Type_Null, &Field_declaringClass(*ret),null);
      setObjectLock(*ret, UNLOCKED);
   }
}

static void createMethodObject(Context currentContext, Method m, TCClass declaringClass, TCObject* ret, bool isConstructor) // also valid for Constructors
{
   TCObject ptrObj=null;
   *ret = null;
   if ((*ret = createObject(currentContext, isConstructor ? "java.lang.reflect.Constructor" : "java.lang.reflect.Method")) != null)
   {
      // modifiers
      int32 mod=0,i,n;
      if (m->flags.isFinal    ) mod |= JFLAG_FINAL;
      if (m->flags.isPrivate  ) mod |= JFLAG_PRIVATE;
      if (m->flags.isProtected) mod |= JFLAG_PROTECTED;
      if (m->flags.isPublic   ) mod |= JFLAG_PUBLIC;
      if (m->flags.isStatic   ) mod |= JFLAG_STATIC;
      if (m->flags.isAbstract ) mod |= JFLAG_ABSTRACT;
      Method_mod(*ret) = mod;
      // ptr
      ptrObj = createByteArray(currentContext, TSIZE);
      if (ptrObj)
         xmoveptr(ARRAYOBJ_START(ptrObj), &m);
      setObjectLock(Method_nativeStruct(*ret) = ptrObj, UNLOCKED);
      // name and declaring class
      setObjectLock(Method_name(*ret) = createStringObjectFromCharP(currentContext,isConstructor ? declaringClass->name : m->name,-1),UNLOCKED);
      createClassObject(currentContext, declaringClass->name, Type_Null, &Method_declaringClass(*ret),null);
      // parameters and exceptions
      Method_parameterTypes(*ret) = createArrayObject(currentContext, "[java.lang.Class", n = m->paramCount);
      if (Method_parameterTypes(*ret) && n > 0)
      {
         TCObject* oa = (TCObject*)ARRAYOBJ_START(Method_parameterTypes(*ret));
         for (i=0; i < n; i++)
            createClassObject(currentContext, declaringClass->cp->cls[m->cpParams[i]], m->cpParams[i] < Type_Object ? m->cpParams[i] : Type_Null, oa++, null);
      }
      Method_exceptionTypes(*ret) = createArrayObject(currentContext, "[java.lang.Class", n = 0); // thrown exceptions is not stored in TCClass!
      if (Method_exceptionTypes(*ret) && n > 0)
      {
         TCObject* oa = (TCObject*)ARRAYOBJ_START(Method_exceptionTypes(*ret));
         for (i=0; i < n; i++)
            createClassObject(currentContext, m->exceptionHandlers[i].className, Type_Null, oa++, null);
      }

      // return and type
      if (!isConstructor)
      {
         CharP nn = m->class_->cp->cls[m->cpReturn];
         createClassObject(currentContext, nn, m->cpReturn == Type_Null ? Type_Void : m->cpReturn < Type_Object ? m->cpReturn : Type_Null, &Method_returnType(*ret),null);
         createClassObject(currentContext, m->class_->name, Type_Null, &Method_type(*ret),null);
      }
      setObjectLock(*ret, UNLOCKED);
   }
}

static void getFieldByName(NMParams p, bool onlyPublic)
{
   TCObject me = p->obj[0];
   TCObject nameObj = p->obj[1];
   CharP name;
   int32 i;
   TCClass o;
   FieldArray ff;
   Field found=null;
   if (nameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   if ((name = String2CharP(nameObj)) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      o = getTargetClass(me);
      for (ff = o->i32InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
      for (ff = o->objInstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
      for (ff = o->v64InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
      for (ff = o->i32StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
      for (ff = o->objStaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
      for (ff = o->v64StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if (((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) && strEq(ff->name,name)) {found = ff; goto cont;}
cont:
      if (found)
         createFieldObject(p->currentContext, found, i, &p->retO);
      else
         throwException(p->currentContext, NoSuchFieldException, "Field not found: %s",name);
      xfree(name);
   }
}
CharP getParameterType(TCClass c, Type t)
{
   switch (t)
   {
      case Type_Byte:    return "java.lang.Byte"; 
      case Type_Boolean: return "java.lang.Boolean";
      case Type_Short:   return "java.lang.Short";  
      case Type_Char:    return "java.lang.Character";
      case Type_Int:     return "java.lang.Integer";  
      case Type_Long:    return "java.lang.Long";     
      case Type_Float:   return "java.lang.Float";    
      case Type_Double:  return "java.lang.Double";   // if (strEq(pt,"java.lang.Float")) po = pt; 
      default: return c->cp->cls[t];
   }
}
static void getMCbyName(NMParams p, CharP methodName, bool isConstructor, bool onlyPublic)
{
   TCObject me = p->obj[0];
   TCObject classesObj = p->obj[isConstructor ? 1 : 2];
   TCClass c;
   bool found=false;
   int32 i,j,n;
   int32 nparams = classesObj == null ? 0 : ARRAYOBJ_LEN(classesObj);
   TCObject* classes = classesObj == null ? null : (TCObject*)ARRAYOBJ_START(classesObj);
   c = getTargetClass(me);
   do
   {
      n = ARRAYLENV(c->methods);
      for (i = 0; i < n; i++)
      {
         Method mm = &c->methods[i];
         CharP mn = mm->name;
         if (onlyPublic && !mm->flags.isPublic)
            continue;
         if (strEq(methodName,mn) && nparams == mm->paramCount)
         {
            bool found = true;
            for (j = 0; j < nparams; j++)  // do NOT invert the loop!
            {
               TCClass target;
               CharP pt, po;

               if (OBJ_CLASS(classes[j])->flags.isString)
               {
                  TNMParams params;

                  tzero(params);
                  params.currentContext = p->currentContext;
                  params.obj = &classes[j];
                  jlC_forName_s(&params);
                  target = getTargetClass(params.retO);
               }
               else
                  target = getTargetClass(classes[j]);
               pt = target->name;
               po = getParameterType(c,mm->cpParams[j]);
               if (!strEq(pt,po))
               {
                  found = false;
                  break;
               }
            }
            if (found) // not an abstract class?
            {
               createMethodObject(p->currentContext, mm, c, &p->retO, isConstructor);
               return;
            }
         }
      }
      c = c->superClass;
   } while (c && !found);
   throwException(p->currentContext,NoSuchMethodException,"Method not found: %s", methodName);
}
static void getFields(NMParams p, bool onlyPublic)
{
   TCObject cls = p->obj[0], ret=null;
   TCClass o;
   int32 count=0,i;
   FieldArray ff;
   o = getTargetClass(cls);

   // fields array already include the ones from superclasses, so we don't need to crawl the hierarchy
   if (o->flags.isInterface)
   {
      // count how many fields have in this interface and super interfaces
      // an interface has only public static fields.
      count += ARRAYLENV(o->i32StaticFields) + ARRAYLENV(o->objStaticFields) + ARRAYLENV(o->v64StaticFields);
      ret = createArrayObject(p->currentContext, "[java.lang.reflect.Field", count);
      if (ret)
      {
         TCObject* oa = (TCObject*)ARRAYOBJ_START(ret);
         for (ff = o->i32StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->objStaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->v64StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) createFieldObject(p->currentContext, ff, i, oa++);
      }                                                           
   }                                                              
   else 
   {
      // count how many PUBLIC fields have in this class and super classes, depending on the flag
      for (ff = o->i32InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      for (ff = o->objInstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      for (ff = o->v64InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      for (ff = o->i32StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      for (ff = o->objStaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      for (ff = o->v64StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) count++;
      ret = createArrayObject(p->currentContext, "[java.lang.reflect.Field", count);
      if (ret)
      {
         TCObject* oa = (TCObject*)ARRAYOBJ_START(ret);
         for (ff = o->i32InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->objInstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->v64InstanceFields, i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->i32StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->objStaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
         for (ff = o->v64StaticFields  , i=ARRAYLENV(ff), ff += i-1; --i >= 0; --ff) if ((onlyPublic && ff->flags.isPublic) || (!onlyPublic && !ff->flags.isInherited)) createFieldObject(p->currentContext, ff, i, oa++);
      }
   }
   setObjectLock(p->retO = ret, UNLOCKED);
}
static void getMCarray(NMParams p, bool isConstructor, bool onlyPublic)
{
   TCObject me = p->obj[0], ret=null;
   TCClass c, o;
   int32 count=0,n;
   MethodArray ff;
   c = getTargetClass(me);
   // count how many PUBLIC fields have in this class and super classes
   for (o = c; o != null; o = o->superClass)
   {
      for (ff = o->methods, n = ARRAYLENV(ff); --n >= 0; ff++) 
         if (!onlyPublic || ff->flags.isPublic) 
         {
            bool isC = strEq(ff->name, CONSTRUCTOR_NAME);
            if (isConstructor == isC)
               count++;
         }
      if (isConstructor || !onlyPublic) break;
   }
   ret = createArrayObject(p->currentContext, "[java.lang.reflect.Method", count);
   if (ret)
   {
      TCObject* oa = (TCObject*)ARRAYOBJ_START(ret);
      for (o = c; o != null; o = o->superClass)
      {
         for (ff = o->methods, n = ARRAYLENV(ff); --n >= 0; ff++) 
            if (!onlyPublic || ff->flags.isPublic) 
            {
               bool isC = strEq(ff->name, CONSTRUCTOR_NAME);
               if (isConstructor == isC)
                  createMethodObject(p->currentContext, ff, o, oa++, isConstructor);
            }
         if (isConstructor || !onlyPublic) break;
      }
   }
   setObjectLock(p->retO = ret, UNLOCKED);
}

//////////////////////////////////////////////////////////////////////////
TC_API void jlC_forName_s(NMParams p) // java/lang/Class native public static Class forName(String className) throws java.lang.ClassNotFoundException;
{
   TCObject classNameObj = p->obj[0];
   if (classNameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   {
      CharP className = String2CharP(classNameObj);
      if (className == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         bool isObj = false, isNew0=false;
         bool isNew = true, ok = false;
         CharP pv, start;
         replaceChar(className, '/', '.');
         // check correctness
         for (start = className; !ok && *start != 0; start++)
            if (('a' <= *start && *start <= 'z') || ('A' <= *start && *start <= 'Z'))
               ok = true;
         if (!ok)
            throwException(p->currentContext, ClassNotFoundException, "Class '%s' not found", className);
         else
         {
            // check if user is passing an object in java's format [Ljava.lang.Object; -> [java.lang.Object
            pv = xstrchr(className, ';');
            if (pv)
            {
               start = className;
               while (*start == '[') start++;
               for (; *start; start++) *start = start[1]; // remove L
               *(pv-1) = 0;
               isObj = true;
            }
            // check if its a primitive type passed in Java's format
            if (className[0] == '[' && className[1] != '&')
            {
               // check if the class that will be instantiated exists
               CharP cc = className;
               while (*cc && *cc == '[')
                  cc++;
               if (isObj && loadClass(p->currentContext, cc, true) == null)
                  goto fail;
               if (!isObj) // convert totalcross' primitive type format to java's
                  switch (*cc)
                  {
                     case 'Z': 
                     case 'C': 
                     case 'B': 
                     case 'S': 
                     case 'I': 
                     case 'J': 
                     case 'F':
                     case 'D': 
                        break;
                     default:
                        throwException(p->currentContext, ClassNotFoundException, "Class '%s' not found", className);
                        goto fail;
                  }
            }
            createClassObject(p->currentContext, className, Type_Null, &p->retO, &isNew);
            // if failed or if the class was already loaded, free the name
         }
fail:
         if (p->retO == null || (isNew && !isNew0)) xfree(className); // the classname is stored in the structure!
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_newInstance(NMParams p) // java/lang/Class native public Object newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException;
{
   TCClass target;
   TCObject me = p->obj[0];

   target = getTargetClass(me);
   if (target->flags.isInterface || target->flags.isAbstract || target->flags.isArray)
      throwException(p->currentContext, InstantiationException, target->name);
   else
   {
      Method m = getMethod(target, false, CONSTRUCTOR_NAME, 0);
      if (!m) // check if the default constructor exists
         throwException(p->currentContext, InstantiationException, "Class %s has no default constructor", target->name);
      else
      if (!m->flags.isPublic)
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
   TCClass target;
   TCObject me = p->obj[0],other;
   target = getTargetClass(me);
   other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = areClassesCompatible(p->currentContext, target, OBJ_CLASS(other)->name) == COMPATIBLE;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isAssignableFrom_c(NMParams p) // java/lang/Class public native boolean isAssignableFrom(Class cls);
{
   TCClass metarget,clstarget;
   TCObject me = p->obj[0];
   TCObject cls = p->obj[1];
   if (cls == null)
      throwException(p->currentContext, NullPointerException, "Argument cls");
   else
   {
      clstarget = getTargetClass(cls);
	  metarget = getTargetClass(me);
      p->retI = areClassesCompatible(p->currentContext, clstarget, metarget->name) == COMPATIBLE;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isInterface(NMParams p) // java/lang/Class public native boolean isInterface();
{
   TCClass target;
   TCObject me = p->obj[0];
   target = getTargetClass(me);
   p->retI = target->flags.isInterface;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_isArray(NMParams p) // java/lang/Class public native boolean isArray();
{
   TCClass target;
   TCObject me = p->obj[0];
   target = getTargetClass(me);
   p->retI = target->flags.isArray;
}
//////////////////////////////////////////////////////////////////////////
static bool isPrimitive(TCObject me)
{
   return me == *booleanTYPE || me == *byteTYPE || me == *shortTYPE || me == *intTYPE || me == *longTYPE || me == *floatTYPE || me == *doubleTYPE || me == *charTYPE;
}
TC_API void jlC_isPrimitive(NMParams p) // java/lang/Class public native boolean isPrimitive();
{
   TCObject me = p->obj[0];
   p->retI = isPrimitive(me);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getSuperclass(NMParams p) // java/lang/Class public native Class getSuperclass();
{
   TCClass target;
   TCObject me = p->obj[0];
   target = getTargetClass(me);
   if (target->superClass != null && !target->flags.isInterface && !isPrimitive(me))
      createClassObject(p->currentContext, target->superClass->name, Type_Null, &p->retO,null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getInterfaces(NMParams p) // java/lang/Class public native java.lang.Class[] getInterfaces();
{
   TCClass target;
   TCObject me = p->obj[0], ret;
   int32 n,i;
   target = getTargetClass(me);
   n = ARRAYLENV(target->interfaces);
   if ((ret = createArrayObject(p->currentContext, "[java.lang.Class", n)) != null)
   {
      TCObject* objs = (TCObject*)ARRAYOBJ_START(ret);
      for (i = 0; i < n; i++)
         createClassObject(p->currentContext, target->interfaces[i]->name, Type_Null, &objs[i],null);
      setObjectLock(p->retO = ret, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getComponentType(NMParams p) // java/lang/Class public native Class getComponentType();
{
   TCClass target;
   TCObject me = p->obj[0];
   target = getTargetClass(me);
   if (target->flags.isArray)
   {
      CharP name = target->name + 1; // skip [
	  if (*name == '&') // primitive array?
         switch (*++name)
         {
	        case 'I': p->retO = *intTYPE; return;
		    case 'B': p->retO = *byteTYPE; return;
		    case 'S': p->retO = *shortTYPE; return;
		    case 'C': p->retO = *charTYPE; return;
		    case 'b': p->retO = *booleanTYPE; return;
		    case 'L': p->retO = *longTYPE; return;
		    case 'D': p->retO = *doubleTYPE; return;
		    case 'F': p->retO = *floatTYPE; return;
	     }

     createClassObject(p->currentContext, name, Type_Null, &p->retO,null);
     if (p->currentContext->thrownException != null)
        p->retO = p->currentContext->thrownException = null;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getModifiers(NMParams p) // java/lang/Class public native int getModifiers();
{
   TCClass target;
   TCObject me = p->obj[0];
   ClassFlags f;
   int32 ret = 0;
   target = getTargetClass(me);
   f = target->flags;
   if (f.isPublic   ) ret |= JFLAG_PUBLIC;
   if (f.isStatic   ) ret |= JFLAG_STATIC;
   if (f.isFinal    )  ret |= JFLAG_FINAL;
   if (f.isAbstract ) ret |= JFLAG_ABSTRACT;
   if (f.isInterface) ret |= JFLAG_INTERFACE;
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getFields(NMParams p) // java/lang/Class public native java.lang.reflect.Field[] getFields() throws SecurityException;
{
   getFields(p, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getMethods(NMParams p) // java/lang/Class public native java.lang.reflect.Method[] getMethods() throws SecurityException;
{
   getMCarray(p,false,true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getConstructors(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor[] getConstructors() throws SecurityException;
{
   getMCarray(p,true,true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getField_s(NMParams p) // java/lang/Class public native java.lang.reflect.Field getField(String name) throws NoSuchFieldException, SecurityException;
{
   getFieldByName(p, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getMethod_sC(NMParams p) // java/lang/Class public native java.lang.reflect.Method getMethod(String name, Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   TCObject nameObj = p->obj[1];
   CharP name;
   if (nameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   if ((name = String2CharP(nameObj)) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      getMCbyName(p, name, false,true);
      xfree(name);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getConstructor_C(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor getConstructor(Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   getMCbyName(p, CONSTRUCTOR_NAME, true,true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredFields(NMParams p) // java/lang/Class public native java.lang.reflect.Field[] getDeclaredFields() throws SecurityException;
{
   getFields(p, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredMethods(NMParams p) // java/lang/Class public native java.lang.reflect.Method[] getDeclaredMethods() throws SecurityException;
{
   getMCarray(p, false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredConstructors(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor[] getDeclaredConstructors() throws SecurityException;
{
   getMCarray(p, true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredField_s(NMParams p) // java/lang/Class public native java.lang.reflect.Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException;
{
   getFieldByName(p, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredMethod_sC(NMParams p) // java/lang/Class public native java.lang.reflect.Method getDeclaredMethod(String name, Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   TCObject nameObj = p->obj[1];
   CharP name;
   if (nameObj == NULL)
      throwException(p->currentContext, NullPointerException,null);
   else
   if ((name = String2CharP(nameObj)) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      getMCbyName(p, name, false,false);
      xfree(name);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlC_getDeclaredConstructor_C(NMParams p) // java/lang/Class public native java.lang.reflect.Constructor getDeclaredConstructor(Class []parameterTypes) throws NoSuchMethodException, SecurityException;
{
   getMCbyName(p, CONSTRUCTOR_NAME, true,false);
}

#ifdef ENABLE_TEST_SUITE
#include "Class_test.h"
#endif
