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

typedef char NameBuf[256];

CharP getTargetArrayClass(TCObject o, NameBuf namebuf, Context currentContext)
{
   CharP name = null;
   int32 len=0;
   TCClass target;
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
      xmoveptr(&target, ARRAYOBJ_START(Class_nativeStruct(o)));
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
      default: return false;
   }
}
static Type checkPrimitiveArray(NMParams p, Type from, bool isGet)
{
   TCObject array = p->obj[0];
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
      if (TYPE_IS_PRIMITIVE(from) && !canWideConvert(!isGet ? from : to, !isGet ? to : from))
         throwException(p->currentContext, IllegalArgumentException, "Argument type mismatch");
      else
	      return to;
   }
   return Type_Null;
}
static Type checkPrimitiveType(NMParams p, TCClass cto, Type from, bool isGet) // check if the field's type can be wide converted to the given src type
{
   Type to;
   if (strEq(cto->name, "java.lang.Boolean"))   to = Type_Boolean; else
   if (strEq(cto->name, "java.lang.Byte"))      to = Type_Byte;    else
   if (strEq(cto->name, "java.lang.Short"))     to = Type_Short;   else
   if (strEq(cto->name, "java.lang.Integer"))   to = Type_Int;     else
   if (strEq(cto->name, "java.lang.Long"))      to = Type_Long;    else
   if (strEq(cto->name, "java.lang.Float"))     to = Type_Float;   else
   if (strEq(cto->name, "java.lang.Double"))    to = Type_Double;  else
   if (strEq(cto->name, "java.lang.Character")) to = Type_Char; 
   else to = type2javaType(cto->name);
   
   if (TYPE_IS_PRIMITIVE(from) && !canWideConvert(!isGet ? from : to, !isGet ? to : from))
      throwException(p->currentContext, IllegalArgumentException, "Argument type mismatch");
   else
      return to;
   return Type_Null;
}
void jlC_forName_s(NMParams p);
static Type checkPrimitiveField(NMParams p, Type from, bool isGet) // check if the field's type can be wide converted to the given src type
{
   TCObject o = p->obj[0];
   TCClass cto;
   if (o == null)
      throwException(p->currentContext, NullPointerException, "Argument array is null");
   else
   {
      if (OBJ_CLASS(Field_type(o))->flags.isString)
      {
         TNMParams params;

         tzero(params);
         params.currentContext = p->currentContext;
         params.obj = &Field_type(o);
         jlC_forName_s(&params);
         xmoveptr(&cto, ARRAYOBJ_START(Class_nativeStruct(params.retO)));
      }
      else
         xmoveptr(&cto, ARRAYOBJ_START(Class_nativeStruct(Field_type(o))));
      return checkPrimitiveType(p, cto, from, isGet);
   }
   return Type_Null;
}
static void getArrayI32(NMParams p, Type targetType)
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   switch (checkPrimitiveArray(p, targetType, true))
   {
      case Type_Byte: 
      case Type_Boolean: p->retI = ((int8*)ARRAYOBJ_START(array))[index]; break;
      case Type_Short: 
      case Type_Char:    p->retI = ((int16*)ARRAYOBJ_START(array))[index]; break;
      case Type_Int:     p->retI = ((int32*)ARRAYOBJ_START(array))[index]; break;
      default: break;
   }
}
static void setArrayI32(NMParams p, Type srcType)
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1]; // I32
   switch (checkPrimitiveArray(p, srcType, false))
   {
      case Type_Byte: 
      case Type_Boolean: ((int8*)ARRAYOBJ_START(array))[index] = (int8)value; break;
      case Type_Short: 
      case Type_Char:    ((int16*)ARRAYOBJ_START(array))[index] = (int16)value; break;
      case Type_Int:     ((int32*)ARRAYOBJ_START(array))[index] = (int32)value; break;
      default: break;
   }
}
static void field(NMParams p, Type srcType, bool isGet, void* value)
{
   TCObject f = p->obj[0];
   TCObject o = p->obj[1];
   Type destType = TYPE_IS_PRIMITIVE(srcType) ? checkPrimitiveField(p, srcType, isGet) : Type_Object;
   if (destType != Type_Null)
   {
      bool isStatic;
      Field target;
      TCClass fieldsClass, objparamClass;
      xmoveptr(&target, ARRAYOBJ_START(Field_nativeStruct(f)));
      xmoveptr(&fieldsClass, ARRAYOBJ_START(Class_nativeStruct(Field_declaringClass(f))));
      isStatic = target->flags.isStatic;
      if (isStatic) // o is ignored for static fields
         o = null;
      objparamClass = o == null ? null : OBJ_CLASS(o);
      // instance fields require the object
      if (!isStatic && o == null) 
         throwException(p->currentContext, NullPointerException, "Object is null");
      else
      // for instance fields, check if the object given is compatible with the class that contains this field x.getClass().getDeclaredField("doubleField").get(new String()) -> String instanceof x
      if (!isStatic && areClassesCompatible(p->currentContext, objparamClass, fieldsClass->name) == NOT_COMPATIBLE)
         throwException(p->currentContext, IllegalArgumentException, "Invalid argument class: %s",OBJ_CLASS(o)->name);
      else
      // dont allow access of a private field
      if (target->flags.isPrivate)
         throwException(p->currentContext, IllegalAccessException, "Private fields cannot be accessed by reflection");
      else
      if (!isGet && target->flags.isFinal)
         throwException(p->currentContext, IllegalAccessException, "Final fields cannot be set by reflection");
      else
      {
         // access allowed!
         int32 index = Field_index(f);
         switch (srcType)
         {
            case Type_Object:
            {
               TCObject* field = isStatic ? &(fieldsClass->objStaticValues[index]) : &FIELD_OBJ(o, objparamClass, index);
               TCObject* ovalue = (TCObject*)value;
               if (isGet) 
                  *ovalue = *field; 
               else 
                  *field = *ovalue;
               break;
            }
            case Type_Long:
            {
               int64* field = isStatic ? (int64*)&(fieldsClass->v64StaticValues[index]) : (int64*)&FIELD_I64(o, objparamClass, index);
               int64* ivalue = (int64*)value;
               if (isGet) 
                  *ivalue = *field; 
               else 
                  *field = *ivalue;
               break;
            }
            case Type_Double:
            case Type_Float:
            {
               double* field = isStatic ? &(fieldsClass->v64StaticValues[index]) : &FIELD_DBL(o, objparamClass, index);
               double* dvalue = (double*)value;
               if (isGet) 
                  *dvalue = *field; 
               else 
                  *field = *dvalue;
               break;
            }
            default:
            {
               int32* field = isStatic ? &(fieldsClass->i32StaticValues[index]) : &FIELD_I32(o, index);
               int32* ivalue = (int32*)value;
               if (isGet) 
                  *ivalue = *field; 
               else 
                  *field = *ivalue;
               break;
            }
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_newInstance_ci(NMParams p) // totalcross/lang/reflect/Array public static native Object newInstance(Class componentType, int length) throws NegativeArraySizeException;
{
   // short o[] = (short[])Array.newInstance(Short.TYPE, 2);
   // Short o[] = (Short[])Array.newInstance(java.lang.Short.class, 2);
   char namebuf[256];
   TCObject componentType = p->obj[0];
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
   TCObject componentType = p->obj[0];
   TCObject dimensions = p->obj[1];
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
      xmoveptr(&target, ARRAYOBJ_START(Class_nativeStruct(componentType)));
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
   TCObject array = p->obj[0];
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
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   TCObject o = null;
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
      default:           p->retO = ((TCObject*)ARRAYOBJ_START(array))[index]; return;
   }
   setObjectLock(p->retO = o, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getBoolean_oi(NMParams p) // totalcross/lang/reflect/Array public static native boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   if (checkPrimitiveArray(p, Type_Boolean, true) != Type_Null)
      p->retI = ((uint8*)ARRAYOBJ_START(array))[index]; // boolean has no widening convertion
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getByte_oi(NMParams p) // totalcross/lang/reflect/Array public static native byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getArrayI32(p, Type_Byte);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getChar_oi(NMParams p) // totalcross/lang/reflect/Array public static native char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getArrayI32(p, Type_Char);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getShort_oi(NMParams p) // totalcross/lang/reflect/Array public static native short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getArrayI32(p, Type_Short);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getInt_oi(NMParams p) // totalcross/lang/reflect/Array public static native int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   getArrayI32(p, Type_Int);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getLong_oi(NMParams p) // totalcross/lang/reflect/Array public static native long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
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
      default: break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_getDouble_oi(NMParams p) // totalcross/lang/reflect/Array public static native double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
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
      default: break;
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
   TCObject array = p->obj[0];
   TCObject value = p->obj[1];
   if (array == null)
      throwException(p->currentContext, IllegalArgumentException, "Array is null");
   else
   {
      int32 index = p->i32[0];
      TCClass c = OBJ_CLASS(array);
      if (c->name[1] != '&') // object array? - note: we assume that it is valid to set the object array with a null value
      {
         if (checkArrayRange(p->currentContext, array, 0, index))
            ((TCObject*)ARRAYOBJ_START(array))[index] = value;
      }
      else // primitive array
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
            default:           break;
         }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setBoolean_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setBoolean(Object array, int index, boolean z) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   int32 value = p->i32[1];
   if (checkPrimitiveArray(p, Type_Boolean, false) != Type_Null)
      ((uint8*)ARRAYOBJ_START(array))[index] = value;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setByte_oib(NMParams p) // totalcross/lang/reflect/Array public static native void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setArrayI32(p, Type_Byte);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setChar_oic(NMParams p) // totalcross/lang/reflect/Array public static native void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setArrayI32(p, Type_Char);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setShort_ois(NMParams p) // totalcross/lang/reflect/Array public static native void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setArrayI32(p, Type_Short);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setInt_oii(NMParams p) // totalcross/lang/reflect/Array public static native void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   setArrayI32(p, Type_Int);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setLong_oil(NMParams p) // totalcross/lang/reflect/Array public static native void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   int64 value = p->i64[0];
   switch (checkPrimitiveArray(p, Type_Long, false))
   {
      case Type_Float: 
      case Type_Double: ((double*)ARRAYOBJ_START(array))[index] = (double)value; break;
      case Type_Long:   ((int64*)ARRAYOBJ_START(array))[index] = value; break;
      default: break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setDouble_oid(NMParams p) // totalcross/lang/reflect/Array public static native void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
   TCObject array = p->obj[0];
   int32 index = p->i32[0];
   double value = p->dbl[0];
   switch (checkPrimitiveArray(p, Type_Double, false))
   {
      case Type_Float: 
      case Type_Double: ((double*)ARRAYOBJ_START(array))[index] = value; break;
      case Type_Long:   ((int64*)ARRAYOBJ_START(array))[index] = (int64)value; break;
      default: break;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrA_setFloat_oid(NMParams p) // totalcross/lang/reflect/Array public static native void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
{
	jlrA_setDouble_oid(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getBoolean_o(NMParams p) // totalcross/lang/reflect/Field public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Boolean, true,&p->retI);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getByte_o(NMParams p) // totalcross/lang/reflect/Field public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Byte, true,&p->retI);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getChar_o(NMParams p) // totalcross/lang/reflect/Field public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Char, true,&p->retI);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getShort_o(NMParams p) // totalcross/lang/reflect/Field public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Short, true,&p->retI);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getInt_o(NMParams p) // totalcross/lang/reflect/Field public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Int, true,&p->retI);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getLong_o(NMParams p) // totalcross/lang/reflect/Field public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Long, true, &p->retL);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getDouble_o(NMParams p) // totalcross/lang/reflect/Field public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Double, true, &p->retD);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_getFloat_o(NMParams p) // totalcross/lang/reflect/Field public native double getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Float, true, &p->retD);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setBoolean_ob(NMParams p) // totalcross/lang/reflect/Field public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Boolean, false, &p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setByte_ob(NMParams p) // totalcross/lang/reflect/Field public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Byte, false, &p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setChar_oc(NMParams p) // totalcross/lang/reflect/Field public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Char, false, &p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setShort_os(NMParams p) // totalcross/lang/reflect/Field public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Short, false, &p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setInt_oi(NMParams p) // totalcross/lang/reflect/Field public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Int, false, &p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setLong_ol(NMParams p) // totalcross/lang/reflect/Field public native void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Long, false, &p->i64[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setDouble_od(NMParams p) // totalcross/lang/reflect/Field public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Double, false, &p->i64[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_setFloat_od(NMParams p) // totalcross/lang/reflect/Field public native void setFloat(Object obj, double f) throws IllegalArgumentException, IllegalAccessException;
{
   field(p, Type_Float, false, &p->i64[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_get_o(NMParams p) // totalcross/lang/reflect/Field public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;
{
   TCObject o = null;
   switch (checkPrimitiveField(p, Type_Null, true))
   {
      case Type_Byte:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Byte");      if (o) field(p,Type_Byte,    true, &Byte_v     (o)); break;
      case Type_Boolean: o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Boolean");   if (o) field(p,Type_Boolean, true, &Boolean_v  (o)); break;
      case Type_Short:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Short");     if (o) field(p,Type_Short,   true, &Short_v    (o)); break;
      case Type_Char:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Character"); if (o) field(p,Type_Char,    true, &Character_v(o)); break;
      case Type_Int:     o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Integer");   if (o) field(p,Type_Int,     true, &Integer_v  (o)); break;
      case Type_Long:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Long");      if (o) field(p,Type_Long,    true, &Long_v     (o)); break;
      case Type_Float:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Float");     if (o) field(p,Type_Float,   true, &Float_v    (o)); break;
      case Type_Double:  o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Double");    if (o) field(p,Type_Double,  true, &Double_v   (o)); break;
      case Type_Null:    break;
      default:           field(p, Type_Object, true, &p->retO); return;
   }
   setObjectLock(p->retO = o, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrF_set_oo(NMParams p) // totalcross/lang/reflect/Field public native void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;
{
   TCObject value = p->obj[2];
   if (p->obj[1] == null)
      throwException(p->currentContext, IllegalArgumentException, "Object is null");
   else
   {
      Type srcType = checkPrimitiveField(p, Type_Null, false);
      if (srcType != Type_Null && (value == null || checkPrimitiveType(p, OBJ_CLASS(value), srcType, true) != Type_Null)) // keep true in checkPrimitiveType!
      switch (srcType)
      {
         case Type_Byte:    field(p, Type_Byte   , false, &Byte_v     (value)); break;
         case Type_Boolean: field(p, Type_Boolean, false, &Boolean_v  (value)); break;
         case Type_Short:   field(p, Type_Short  , false, &Short_v    (value)); break;
         case Type_Char:    field(p, Type_Char   , false, &Character_v(value)); break;
         case Type_Int:     field(p, Type_Int    , false, &Integer_v  (value)); break;
         case Type_Long:    field(p, Type_Long   , false, &Long_v    (value)); break;
         case Type_Float:   field(p, Type_Float  , false, &Float_v   (value)); break;
         case Type_Double:  field(p, Type_Double , false, &Double_v  (value)); break;
         case Type_Null:    break;
         default:           field(p, Type_Object, false, &value); break;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
CharP getParameterType(TCClass c, Type t);

static void invoke(NMParams p, TCObject m, TCObject obj, TCObject args)
{
   int32 n = args == null ? 0 : ARRAYOBJ_LEN(args), i;
   TCObject* argObjs = args == null ? null : (TCObject*)ARRAYOBJ_START(args);
   Method target;
   TValue* aargs=null;
   TValue ret;
   Type from,to;

   xmoveptr(&target, ARRAYOBJ_START(Method_nativeStruct(m)));
   if (!target->flags.isStatic && obj == null)
      throwException(p->currentContext, NullPointerException, "Object is null");
   else
   if (!target->flags.isStatic && areClassesCompatible(p->currentContext, OBJ_CLASS(obj), target->class_->name) != COMPATIBLE)
      throwException(p->currentContext, IllegalArgumentException, "Object type mismatch", target->paramCount, n);
   else
   if (target->paramCount != n)
      throwException(p->currentContext, IllegalArgumentException, "Parameter count mismatch. Needs %d, but only %d was given", target->paramCount, n);
   else
   //if (argObjs != null)
   {
      aargs = n == 0 ? null : newArrayOf(Value, n, null);
      if (n > 0 && !aargs)
         throwException(p->currentContext, OutOfMemoryError, "For TValue array");
      else
      {
         TCClass c = target->class_;
         for (i = 0; i < n; i++)
         {
            TCClass oic;
            TCObject oi = argObjs[i];
            if (oi == null)
               aargs[i].asObj = null;
            else
            {
               CharP targetClass = getParameterType(c, target->cpParams[i]);
               oic = OBJ_CLASS(oi);
               from = target->cpParams[i];
               if (TYPE_IS_PRIMITIVE(from))
               {
                  to = checkPrimitiveType(p, oic, from, true);
                  if (to == Type_Null)
                     break;
               }
               else
               if (areClassesCompatible(p->currentContext, oic, targetClass) != COMPATIBLE)
               {
                  throwException(p->currentContext, /*IllegalArgumentException*/InvocationTargetException, "Incompatible classes: %s and %s", OBJ_CLASS(oi)->name, targetClass);
                  break;
               }
               switch (from)
               {
                  case Type_Byte:    aargs[i].asInt32 = Byte_v     (oi); break;
                  case Type_Boolean: aargs[i].asInt32 = Boolean_v  (oi); break;
                  case Type_Short:   aargs[i].asInt32 = Short_v    (oi); break;
                  case Type_Char:    aargs[i].asInt32 = Character_v(oi); break;
                  case Type_Int:     aargs[i].asInt32 = Integer_v  (oi); break;
                  case Type_Long:    aargs[i].asInt64 = Long_v     (oi); break;
                  case Type_Float:   aargs[i].asDouble = Float_v   (oi); break;
                  case Type_Double:  aargs[i].asDouble = Double_v  (oi); break;
                  case Type_Null:    break;
                  default:           aargs[i].asObj = oi;                break;
               }
            }
         }
         if (p->currentContext->thrownException == null)
         {
            TCObject o;
            if (target->paramCount > 0)
               p->currentContext->parametersInArray = true;

            ret.asInt64 = executeMethod(p->currentContext, target, obj, aargs).asInt64;

            if (p->currentContext->thrownException != null)
            {
               CharP msg;
               TCObject original = p->currentContext->thrownException, omsg = *Throwable_msg(original);
               p->currentContext->thrownException = null;
               msg = omsg ? JCharP2CharP(String_charsStart(omsg),String_charsLen(omsg)) : null;
               throwException(p->currentContext, InvocationTargetException, "Exception %s thrown: %s", OBJ_CLASS(original)->name, msg == null ? "" : msg);
               xfree(msg);
            }
            else
            {
               bool unlockIt = true;
               switch (target->cpReturn)
               {
                  case Type_Null:    o = null; unlockIt = false; break;
                  case Type_Byte:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Byte");      if (o) Byte_v     (o) = ret.asInt32; break;
                  case Type_Boolean: o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Boolean");   if (o) Boolean_v  (o) = ret.asInt32; break;
                  case Type_Short:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Short");     if (o) Short_v    (o) = ret.asInt32; break;
                  case Type_Char:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Character"); if (o) Character_v(o) = ret.asInt32; break;
                  case Type_Int:     o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Integer");   if (o) Integer_v  (o) = ret.asInt32; break;
                  case Type_Long:    o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Long");      if (o) Long_v     (o) = ret.asInt64; break;
                  case Type_Float:   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Float");     if (o) Float_v    (o) = ret.asDouble; break;
                  case Type_Double:  o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "java.lang.Double");    if (o) Double_v   (o) = ret.asDouble; break;
                  default:           o = ret.asObj; unlockIt = false; break;
               }
               if (unlockIt)
                  setObjectLock(p->retO = o, UNLOCKED);
               else
                  p->retO = o;
            }
         }
         freeArray(aargs);
      }
   }
}
TC_API void jlrM_invoke_oO(NMParams p) // totalcross/lang/reflect/Method public native Object invoke(Object obj, Object []args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
   TCObject m = p->obj[0];
   TCObject obj = p->obj[1];
   TCObject args = p->obj[2];
   invoke(p, m, obj, args);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlrC_newInstance_O(NMParams p) // totalcross/lang/reflect/Constructor public native Object newInstance(Object []initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
{
   TCObject targetClassObj = Method_declaringClass(p->obj[0]), o;
   TCClass target;
   xmoveptr(&target, ARRAYOBJ_START(Class_nativeStruct(targetClassObj)));
   o = createObjectWithoutCallingDefaultConstructor(p->currentContext, target->name);
   if (o)
      invoke(p, p->obj[0], o, p->obj[1]);
   setObjectLock(p->retO = o, UNLOCKED);
}

//#ifdef ENABLE_TEST_SUITE
//#include "Class_test.h"
//#endif
