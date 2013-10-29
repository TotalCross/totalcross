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

typedef char NameBuf[256];
Object *booleanTYPE, *byteTYPE, *shortTYPE, *intTYPE, *longTYPE, *floatTYPE, *doubleTYPE, *charTYPE;
static void loadTYPEs(Context currentContext)
{
   booleanTYPE = getStaticFieldObject(loadClass(currentContext, "java.lang.Boolean",   false), "TYPE");
   byteTYPE    = getStaticFieldObject(loadClass(currentContext, "java.lang.Byte",      false), "TYPE");
   shortTYPE   = getStaticFieldObject(loadClass(currentContext, "java.lang.Short",     false), "TYPE");
   intTYPE     = getStaticFieldObject(loadClass(currentContext, "java.lang.Integer",   false), "TYPE");
   longTYPE    = getStaticFieldObject(loadClass(currentContext, "java.lang.Long",      false), "TYPE");
   floatTYPE   = getStaticFieldObject(loadClass(currentContext, "java.lang.Float",     false), "TYPE");
   doubleTYPE  = getStaticFieldObject(loadClass(currentContext, "java.lang.Double",    false), "TYPE");
   charTYPE    = getStaticFieldObject(loadClass(currentContext, "java.lang.Character", false), "TYPE");
}
CharP getTargetArrayClass(Object o, NameBuf namebuf, Context currentContext)
{
   CharP name = null;
   int32 len=0;
   TCClass target;
   if (booleanTYPE == null)
      loadTYPEs(currentContext);
   // test if this is a primitive array
   if (o == *booleanTYPE) name = BOOLEAN_ARRAY; else
   if (o == *byteTYPE   ) name = BYTE_ARRAY; else
   if (o == *shortTYPE  ) name = SHORT_ARRAY; else
   if (o == *intTYPE    ) name = INT_ARRAY; else
   if (o == *longTYPE   ) name = LONG_ARRAY; else
   if (o == *floatTYPE  ) name = DOUBLE_ARRAY; else
   if (o == *doubleTYPE ) name = DOUBLE_ARRAY; else
   if (o == *charTYPE   ) name = CHAR_ARRAY; 
   else
   {
      xmoveptr(&target, ARRAYOBJ_START(Class_targetClass(o)));
      len = xstrlen(target->name);
      if (len > sizeof(NameBuf)-2)
      {
         throwException(currentContext, IllegalArgumentException, "Class name too long: %d", len);
         name = target->name; // just to return
      }
      else
      {
         namebuf[0] = '[';
         xstrcpy(&namebuf[1],target->name);
      }
      name = namebuf;
   }
   return name;
}
static bool canWideConvert(Type from, Type to)
{
   if (from == to)
      return true;
	switch (from)
	{
      case Type_Byte:  return to == Type_Short || to == Type_Int || to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Char: 
      case Type_Short: return to == Type_Int || to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Int:   return to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Long:  return to == Type_Float || to == Type_Double;
      case Type_Float: return to == Type_Double;
   }
   return false;
}
static Type checkPrimitiveArray(NMParams p, Type from, bool isGet)
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (array == null)
      throwException(p->currentContext, NullPointerException, "Argument array is null");
   else
   {
      TCClass c = OBJ_CLASS(array);
      Type to = type2javaType(c->name);
      if (!c->flags.isArray)
         throwException(p->currentContext, IllegalArgumentException, "Object is not an array");
      else
      if (!checkArrayRange(p->currentContext, array, 0, index))
         ;
      else
      if (from != Type_Null && from != Type_Object && !canWideConvert(!isGet ? from : to, !isGet ? to : from))
         throwException(p->currentContext, IllegalArgumentException, "Argument type mismatch");
      else
	     return to;
   }
   return Type_Null;
}
static void getRetI(NMParams p, Type targetType)
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   switch (checkPrimitiveArray(p, targetType, true))
   {
      case Type_Byte: 
      case Type_Boolean: p->retI = ((int8*)ARRAYOBJ_START(array))[index]; break;
      case Type_Short: 
      case Type_Char:    p->retI = ((int16*)ARRAYOBJ_START(array))[index]; break;
      case Type_Int:     p->retI = ((int32*)ARRAYOBJ_START(array))[index]; break;
   }
}
static void setI32(NMParams p, Type srcType)
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1]; // I32
   switch (checkPrimitiveArray(p, srcType, false))
   {
      case Type_Byte: 
      case Type_Boolean: ((int8*)ARRAYOBJ_START(array))[index] = (int8)value; break;
      case Type_Short: 
      case Type_Char:    ((int16*)ARRAYOBJ_START(array))[index] = (int16)value; break;
      case Type_Int:     ((int32*)ARRAYOBJ_START(array))[index] = (int32)value; break;
   }
}

//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_newInstance_ci(NMParams p) // totalcross/lang/reflect/Array public static native Object newInstance(Class componentType, int length) throws NegativeArraySizeException;
{
   // short o[] = (short[])Array.newInstance(Short.TYPE, 2);
   // Short o[] = (Short[])Array.newInstance(java.lang.Short.class, 2);
   char namebuf[256];
   Object componentType = p->obj[0];
   int32 length = p->i32[0];
   if (length < 0)
      throwException(p->currentContext, NegativeArraySizeException, "Invalid array size: %d", length);
   else
   if (componentType == null)
      throwException(p->currentContext, NullPointerException, "Argument componentType is null");
   else
   {
      CharP name = getTargetArrayClass(componentType, namebuf, p->currentContext);
      setObjectLock(p->retO = createArrayObject(p->currentContext, name, length), UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_newInstance_cI(NMParams p) // totalcross/lang/reflect/Array public static native Object newInstance(Class componentType, int []dimensions) throws IllegalArgumentException, NegativeArraySizeException;
{
   char namebuf[256];
   Object componentType = p->obj[0];
   Object dimensions = p->obj[1];
   if (componentType == null)
      throwException(p->currentContext, NullPointerException, "Argument componentType is null");
   else
   if (dimensions == null)
      throwException(p->currentContext, NullPointerException, "Argument dimensions is null");
   else
   {
      TCClass target;
      int32 dimsLen = ARRAYOBJ_LEN(dimensions), i;
      int32* dims = (int32*)ARRAYOBJ_START(dimensions);
      xmoveptr(&target, ARRAYOBJ_START(Class_targetClass(componentType)));
      if (dimsLen == 0)
         throwException(p->currentContext, IllegalArgumentException, "dimensions length cannot be 0");
      else
      if (dimsLen > 255)
         throwException(p->currentContext, IllegalArgumentException, "dimensions length cannot be greater than 255");
      else
      {
         CharP name;
         for (i = dimsLen; --i >= 0;)
            if (dims[i] < 0)
            {
               throwException(p->currentContext, NegativeArraySizeException, "Invalid array size %d at dimension position %d", dims[i],i);
               return;
            }    
         name = getTargetArrayClass(componentType, namebuf, p->currentContext);
         setObjectLock(p->retO = createArrayObjectMulti(p->currentContext, namebuf, dimsLen, null, dims), UNLOCKED);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getLength_o(NMParams p) // totalcross/lang/reflect/Array public static native int getLength(Object array) throws IllegalArgumentException;
{
   Object array = p->obj[0];
   if (array == null)
      throwException(p->currentContext, NullPointerException, "Argument array is null");
   else
   {
      TCClass c = OBJ_CLASS(array);
      if (!c->flags.isArray)
         throwException(p->currentContext, IllegalArgumentException, "Object is not an array");
      else
         p->retI = ARRAYOBJ_LEN(array);   
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_get_oi(NMParams p) // totalcross/lang/reflect/Array public static native Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   Object o = null;
   switch (checkPrimitiveArray(p, Type_Null, true))
   {
      case Type_Byte:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Byte");      if (o) Byte_v     (o) = ((int8  *)ARRAYOBJ_START(array))[index]; break;
      case Type_Boolean: o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Boolean");   if (o) Boolean_v  (o) = ((int8  *)ARRAYOBJ_START(array))[index]; break;
      case Type_Short:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Short");     if (o) Short_v    (o) = ((int16 *)ARRAYOBJ_START(array))[index]; break;
      case Type_Char:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Character"); if (o) Character_v(o) = ((int16 *)ARRAYOBJ_START(array))[index]; break;
      case Type_Int:     o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Integer");   if (o) Integer_v  (o) = ((int32 *)ARRAYOBJ_START(array))[index]; break;
      case Type_Long:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Long");      if (o) Long_v     (o) = ((int64 *)ARRAYOBJ_START(array))[index]; break;
      case Type_Float:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Float");     if (o) Float_v    (o) = ((double*)ARRAYOBJ_START(array))[index]; break;
      case Type_Double:  o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Double");    if (o) Double_v   (o) = ((double*)ARRAYOBJ_START(array))[index]; break;
      case Type_Null:    break;
      default:           p->retO = ((Object*)ARRAYOBJ_START(array))[index]; return;
   }
   setObjectLock(p->retO = o, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getBoolean_oi(NMParams p) // totalcross/lang/reflect/Array public static native boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Boolean, true) != Type_Null)
      p->retI = ((uint8*)ARRAYOBJ_START(array))[index]; // boolean has no widening convertion
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getByte_oi(NMParams p) // totalcross/lang/reflect/Array public static native byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getRetI(p, Type_Byte);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getChar_oi(NMParams p) // totalcross/lang/reflect/Array public static native char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getRetI(p, Type_Char);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getShort_oi(NMParams p) // totalcross/lang/reflect/Array public static native short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getRetI(p, Type_Short);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getInt_oi(NMParams p) // totalcross/lang/reflect/Array public static native int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getRetI(p, Type_Int);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getLong_oi(NMParams p) // totalcross/lang/reflect/Array public static native long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   switch (checkPrimitiveArray(p, Type_Long, true))
   {
      case Type_Byte: 
      case Type_Boolean: p->retL = ((int8*)ARRAYOBJ_START(array))[index]; break;
      case Type_Short: 
      case Type_Char:    p->retL = ((int16*)ARRAYOBJ_START(array))[index]; break;
      case Type_Int:     p->retL = ((int32*)ARRAYOBJ_START(array))[index]; break;
      case Type_Long:    p->retL = ((int64*)ARRAYOBJ_START(array))[index]; break;
      case Type_Float:
      case Type_Double:  p->retL = (int64)((double*)ARRAYOBJ_START(array))[index]; break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getDouble_oi(NMParams p) // totalcross/lang/reflect/Array public static native double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   switch (checkPrimitiveArray(p, Type_Double, true))
   {
      case Type_Byte: 
      case Type_Boolean: p->retD = ((int8*)ARRAYOBJ_START(array))[index]; break;
      case Type_Short: 
      case Type_Char:    p->retD = ((int16*)ARRAYOBJ_START(array))[index]; break;
      case Type_Int:     p->retD = ((int32*)ARRAYOBJ_START(array))[index]; break;
      case Type_Long:    p->retD = (double)((int64*)ARRAYOBJ_START(array))[index]; break;
      case Type_Float:
      case Type_Double:  p->retD = ((double*)ARRAYOBJ_START(array))[index]; break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getFloat_oi(NMParams p) // totalcross/lang/reflect/Array public static native float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
	jlrA_getDouble_oi(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_set_oio(NMParams p) // totalcross/lang/reflect/Array public static native void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   Object value = p->obj[1];
   if (value == null)
      throwException(p->currentContext, IllegalArgumentException, "Value is null");
   else
      switch (checkPrimitiveArray(p, Type_Null, false))
      {
         case Type_Byte:    ((int8  *)ARRAYOBJ_START(array))[index] = (int8)  Byte_v     (value); break;
         case Type_Boolean: ((int8  *)ARRAYOBJ_START(array))[index] = (int8)  Boolean_v  (value); break;
         case Type_Short:   ((int16 *)ARRAYOBJ_START(array))[index] = (int16) Short_v    (value); break;
         case Type_Char:    ((int16 *)ARRAYOBJ_START(array))[index] = (int16) Character_v(value); break;
         case Type_Int:     ((int32 *)ARRAYOBJ_START(array))[index] = (int32) Integer_v  (value); break;
         case Type_Long:    ((int64 *)ARRAYOBJ_START(array))[index] = (int64) Long_v     (value); break;
         case Type_Float:   ((double*)ARRAYOBJ_START(array))[index] = (double)Float_v    (value); break;
         case Type_Double:  ((double*)ARRAYOBJ_START(array))[index] = (double)Double_v   (value); break;
         case Type_Null:    break;
         default:           ((Object*)ARRAYOBJ_START(array))[index] = value;                      break;
      }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setBoolean_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setBoolean(Object array, int index, boolean z) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Boolean, false) != Type_Null)
      ((uint8*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setByte_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setI32(p, Type_Byte);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setChar_oic(NMParams p) // totalcross/lang/reflect/Array public static native void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setI32(p, Type_Char);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setShort_ois(NMParams p) // totalcross/lang/reflect/Array public static native void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setI32(p, Type_Short);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setInt_oii(NMParams p) // totalcross/lang/reflect/Array public static native void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setI32(p, Type_Int);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setLong_oil(NMParams p) // totalcross/lang/reflect/Array public static native void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int64 value = p->i64[0];
   switch (checkPrimitiveArray(p, Type_Long, false))
   {
      case Type_Float: 
      case Type_Double: ((double*)ARRAYOBJ_START(array))[index] = (double)value; break;
      case Type_Long:   ((int64*)ARRAYOBJ_START(array))[index] = value; break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setDouble_oid(NMParams p) // totalcross/lang/reflect/Array public static native void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   double value = p->dbl[0];
   switch (checkPrimitiveArray(p, Type_Double, false))
   {
      case Type_Float: 
      case Type_Double: ((double*)ARRAYOBJ_START(array))[index] = value; break;
      case Type_Long:   ((int64*)ARRAYOBJ_START(array))[index] = (int64)value; break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setFloat_oid(NMParams p) // totalcross/lang/reflect/Array public static native void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
	jlrA_setDouble_oid(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Method public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getName(NMParams p) // totalcross/lang/reflect/Method public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getModifiers(NMParams p) // totalcross/lang/reflect/Method public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getReturnType(NMParams p) // totalcross/lang/reflect/Method public native Class getReturnType();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getParameterTypes(NMParams p) // totalcross/lang/reflect/Method public native java.lang.Class[] getParameterTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_getExceptionTypes(NMParams p) // totalcross/lang/reflect/Method public native java.lang.Class[] getExceptionTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_equals_o(NMParams p) // totalcross/lang/reflect/Method public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_hashCode(NMParams p) // totalcross/lang/reflect/Method public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_toString(NMParams p) // totalcross/lang/reflect/Method public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrM_invoke_oO(NMParams p) // totalcross/lang/reflect/Method public native Object invoke(Object obj, Object []args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Constructor public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_getName(NMParams p) // totalcross/lang/reflect/Constructor public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_getModifiers(NMParams p) // totalcross/lang/reflect/Constructor public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_getParameterTypes(NMParams p) // totalcross/lang/reflect/Constructor public native java.lang.Class[] getParameterTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_getExceptionTypes(NMParams p) // totalcross/lang/reflect/Constructor public native java.lang.Class[] getExceptionTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_equals_o(NMParams p) // totalcross/lang/reflect/Constructor public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_hashCode(NMParams p) // totalcross/lang/reflect/Constructor public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_toString(NMParams p) // totalcross/lang/reflect/Constructor public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_newInstance_O(NMParams p) // totalcross/lang/reflect/Constructor public native Object newInstance(Object []initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Field public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getName(NMParams p) // totalcross/lang/reflect/Field public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getModifiers(NMParams p) // totalcross/lang/reflect/Field public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getType(NMParams p) // totalcross/lang/reflect/Field public native Class getType();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_equals_o(NMParams p) // totalcross/lang/reflect/Field public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_hashCode(NMParams p) // totalcross/lang/reflect/Field public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_toString(NMParams p) // totalcross/lang/reflect/Field public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_get_o(NMParams p) // totalcross/lang/reflect/Field public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getBoolean_o(NMParams p) // totalcross/lang/reflect/Field public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getByte_o(NMParams p) // totalcross/lang/reflect/Field public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getChar_o(NMParams p) // totalcross/lang/reflect/Field public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getShort_o(NMParams p) // totalcross/lang/reflect/Field public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getInt_o(NMParams p) // totalcross/lang/reflect/Field public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getLong_o(NMParams p) // totalcross/lang/reflect/Field public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getFloat_o(NMParams p) // totalcross/lang/reflect/Field public native float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getDouble_o(NMParams p) // totalcross/lang/reflect/Field public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_set_oo(NMParams p) // totalcross/lang/reflect/Field public native void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setBoolean_ob(NMParams p) // totalcross/lang/reflect/Field public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setByte_ob(NMParams p) // totalcross/lang/reflect/Field public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setChar_oc(NMParams p) // totalcross/lang/reflect/Field public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setShort_os(NMParams p) // totalcross/lang/reflect/Field public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setInt_oi(NMParams p) // totalcross/lang/reflect/Field public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setLong_ol(NMParams p) // totalcross/lang/reflect/Field public native void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setFloat_of(NMParams p) // totalcross/lang/reflect/Field public native void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setDouble_od(NMParams p) // totalcross/lang/reflect/Field public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;
{
}

//#ifdef ENABLE_TEST_SUITE
//#include "Class_test.h"
//#endif
