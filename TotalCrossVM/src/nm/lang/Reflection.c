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
TC_API void tlrA_newInstance_ci(NMParams p) // totalcross/lang/reflect/Array public static native Object newInstance(Class componentType, int length) throws NegativeArraySizeException;
{
   Object componentType = p->obj[0];
   int32 length = p->i32[0];
   if (length < 0)
      throwException(p->currentContext, NegativeArraySizeException, "Invalid array size: %d", length);
   else
   if (componentType == null)
      throwException(p->currentContext, NullPointerException, "Argument componentType is null");
   else
   {
      TCClass target;
      xmoveptr(&target, ARRAYOBJ_START(Class_targetClass(componentType)));
      setObjectLock(p->retO = createArrayObject(p->currentContext, target->name, length), UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_newInstance_cI(NMParams p) // totalcross/lang/reflect/Array public static native Object newInstance(Class componentType, int []dimensions) throws IllegalArgumentException, NegativeArraySizeException;
{
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
         for (i = dimsLen; --i >= 0;)
            if (dims[i] < 0)
            {
               throwException(p->currentContext, NegativeArraySizeException, "Invalid array size %d at dimension position %d", dims[i],i);
               return;
            }    
         setObjectLock(p->retO = createArrayObjectMulti(p->currentContext, target->name, dimsLen, null, dims), UNLOCKED);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getLength_o(NMParams p) // totalcross/lang/reflect/Array public static native int getLength(Object array) throws IllegalArgumentException;
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
TC_API void tlrA_get_oi(NMParams p) // totalcross/lang/reflect/Array public static native Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (array == null)
      throwException(p->currentContext, NullPointerException, "Argument array is null");
   else
   {
      TCClass c = OBJ_CLASS(array);
      if (!c->flags.isArray)
         throwException(p->currentContext, IllegalArgumentException, "Object is not an array");
      else
      if (checkArrayRange(p->currentContext, array, 0, index))
      {
         if (c->flags.isObjectArray)
            p->retO = ((Object*)ARRAYOBJ_START(array))[index];
         else
            p->retO = null; // TODO: TEM QUE CRIAR OS WRAPPERS DOS TIPOS PRIMITIVOS
      }
   }
}
//////////////////////////////////////////////////////////////////////////
static bool canWideConvert(CharP sfrom, Type to)
{
   Type from = type2javaType(sfrom);
   if (from == to)
      return true;
	switch (from)
	{
      case Type_Byte: return to == Type_Short || to == Type_Int || to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Char: 
      case Type_Short: return to == Type_Int || to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Int: return to == Type_Long || to == Type_Float || to == Type_Double;
      case Type_Long: return to == Type_Float || to == Type_Double;
      case Type_Float: return to == Type_Double;
   }
   return false;
}
static bool checkPrimitiveArray(NMParams p, Type to)
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (array == null)
      throwException(p->currentContext, NullPointerException, "Argument array is null");
   else
   {
      TCClass c = OBJ_CLASS(array);
      if (!c->flags.isArray)
         throwException(p->currentContext, IllegalArgumentException, "Object is not an array");
      else
      if (!checkArrayRange(p->currentContext, array, 0, index))
         ;
      else
         if (!canWideConvert(c->name, to))
         throwException(p->currentContext, IllegalArgumentException, "Argument type mismatch");
      else
	     return true;
   }
   return false;
}

TC_API void tlrA_getBoolean_oi(NMParams p) // totalcross/lang/reflect/Array public static native boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Boolean))
      p->retI = ((uint8*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getByte_oi(NMParams p) // totalcross/lang/reflect/Array public static native byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Byte))
      p->retI = ((uint8*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getChar_oi(NMParams p) // totalcross/lang/reflect/Array public static native char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Char))
      p->retI = ((uint16*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getShort_oi(NMParams p) // totalcross/lang/reflect/Array public static native short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Short))
      p->retI = ((int16*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getInt_oi(NMParams p) // totalcross/lang/reflect/Array public static native int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Int))
      p->retI = ((int32*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getLong_oi(NMParams p) // totalcross/lang/reflect/Array public static native long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Long))
      p->retL = ((int64*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getDouble_oi(NMParams p) // totalcross/lang/reflect/Array public static native double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Double))
      p->retD = ((double*)ARRAYOBJ_START(array))[index];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_getFloat_oi(NMParams p) // totalcross/lang/reflect/Array public static native float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
	tlrA_getDouble_oi(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_set_oio(NMParams p) // totalcross/lang/reflect/Array public static native void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setBoolean_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setBoolean(Object array, int index, boolean z) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Boolean))
      ((uint8*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setByte_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Byte))
      ((int8*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setChar_oic(NMParams p) // totalcross/lang/reflect/Array public static native void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Char))
      ((uint16*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setShort_ois(NMParams p) // totalcross/lang/reflect/Array public static native void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Short))
      ((int16*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setInt_oii(NMParams p) // totalcross/lang/reflect/Array public static native void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Int))
      ((int32*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setLong_oil(NMParams p) // totalcross/lang/reflect/Array public static native void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   int64 value = p->i64[0];
   if (checkPrimitiveArray(p, Type_Long))
      ((int64*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setDouble_oid(NMParams p) // totalcross/lang/reflect/Array public static native void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   Object array = p->obj[0];
   int32 index = p->i32[0];
   double value = p->dbl[0];
   if (checkPrimitiveArray(p, Type_Double))
      ((double*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrA_setFloat_oif(NMParams p) // totalcross/lang/reflect/Array public static native void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
	tlrA_setDouble_oid(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Method public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getName(NMParams p) // totalcross/lang/reflect/Method public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getModifiers(NMParams p) // totalcross/lang/reflect/Method public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getReturnType(NMParams p) // totalcross/lang/reflect/Method public native Class getReturnType();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getParameterTypes(NMParams p) // totalcross/lang/reflect/Method public native java.lang.Class[] getParameterTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_getExceptionTypes(NMParams p) // totalcross/lang/reflect/Method public native java.lang.Class[] getExceptionTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_equals_o(NMParams p) // totalcross/lang/reflect/Method public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_hashCode(NMParams p) // totalcross/lang/reflect/Method public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_toString(NMParams p) // totalcross/lang/reflect/Method public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrM_invoke_oO(NMParams p) // totalcross/lang/reflect/Method public native Object invoke(Object obj, Object []args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Constructor public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_getName(NMParams p) // totalcross/lang/reflect/Constructor public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_getModifiers(NMParams p) // totalcross/lang/reflect/Constructor public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_getParameterTypes(NMParams p) // totalcross/lang/reflect/Constructor public native java.lang.Class[] getParameterTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_getExceptionTypes(NMParams p) // totalcross/lang/reflect/Constructor public native java.lang.Class[] getExceptionTypes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_equals_o(NMParams p) // totalcross/lang/reflect/Constructor public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_hashCode(NMParams p) // totalcross/lang/reflect/Constructor public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_toString(NMParams p) // totalcross/lang/reflect/Constructor public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrC_newInstance_O(NMParams p) // totalcross/lang/reflect/Constructor public native Object newInstance(Object []initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getDeclaringClass(NMParams p) // totalcross/lang/reflect/Field public native Class getDeclaringClass();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getName(NMParams p) // totalcross/lang/reflect/Field public native String getName();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getModifiers(NMParams p) // totalcross/lang/reflect/Field public native int getModifiers();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getType(NMParams p) // totalcross/lang/reflect/Field public native Class getType();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_equals_o(NMParams p) // totalcross/lang/reflect/Field public native boolean equals(Object obj);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_hashCode(NMParams p) // totalcross/lang/reflect/Field public native int hashCode();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_toString(NMParams p) // totalcross/lang/reflect/Field public native String toString();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_get_o(NMParams p) // totalcross/lang/reflect/Field public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getBoolean_o(NMParams p) // totalcross/lang/reflect/Field public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getByte_o(NMParams p) // totalcross/lang/reflect/Field public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getChar_o(NMParams p) // totalcross/lang/reflect/Field public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getShort_o(NMParams p) // totalcross/lang/reflect/Field public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getInt_o(NMParams p) // totalcross/lang/reflect/Field public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getLong_o(NMParams p) // totalcross/lang/reflect/Field public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getFloat_o(NMParams p) // totalcross/lang/reflect/Field public native float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_getDouble_o(NMParams p) // totalcross/lang/reflect/Field public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_set_oo(NMParams p) // totalcross/lang/reflect/Field public native void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setBoolean_ob(NMParams p) // totalcross/lang/reflect/Field public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setByte_ob(NMParams p) // totalcross/lang/reflect/Field public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setChar_oc(NMParams p) // totalcross/lang/reflect/Field public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setShort_os(NMParams p) // totalcross/lang/reflect/Field public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setInt_oi(NMParams p) // totalcross/lang/reflect/Field public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setLong_ol(NMParams p) // totalcross/lang/reflect/Field public native void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setFloat_of(NMParams p) // totalcross/lang/reflect/Field public native void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tlrF_setDouble_od(NMParams p) // totalcross/lang/reflect/Field public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;
{
}

//#ifdef ENABLE_TEST_SUITE
//#include "Class_test.h"
//#endif
