// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef CLASS_H
#define CLASS_H

#include "../tcvm/tcapi.h"
#include "xtypes.h"
#include "mem.h"

#ifdef __cplusplus
extern "C" {
#endif

/** IMPORTANT: there's a main difference between the XXX and XXXInfo structures:
 * the XXXInfo are PACKED at 2 bytes, which make it use less memory main memory,
 * but it makes the C compiled code bigger and slower. Due to this, we decided to
 * keep the structures separetely, even if most of them are the same.
 */

#pragma pack(2)  // make sure structure members are aligned at 2 bytes

/// Flags for a Java class
typedef struct
{
   uint16 bits2shift     : 2; /// the number of bits to shift to compute the array's size for the object/primitive type that this class represents.
   uint16 isArray        : 1; /// is this class an array?
   uint16 isObjectArray  : 1; /// is this array NOT an array of primitives?
   uint16 isString       : 1; /// true if class is java.lang.String
   uint16 isPublic       : 1;
   uint16 isStatic       : 1;
   uint16 isFinal        : 1;
   uint16 isAbstract     : 1;
   uint16 isSynthetic    : 1;
   uint16 isInterface    : 1;
   uint16 isStrict       : 1;
   uint16 __available    : 4; // prefer to fill the gap to prevent structure size optimization
} __attribute_packed__ ClassFlags;

/// Flags for a Java method
typedef struct
{
   uint16 isPublic       : 1;
   uint16 isPrivate      : 1;
   uint16 isProtected    : 1;
   uint16 isStatic       : 1;
   uint16 isFinal        : 1;
   uint16 isNative       : 1;
   uint16 isAbstract     : 1;
   uint16 isSynchronized : 1;
   uint16 __available    : 8; // prefer to fill the gap to prevent structure size optimization
} __attribute_packed__ MethodFlags;

/// Flags for a Java field
typedef struct
{
   uint16 isArray        : 1;
   uint16 type           : 4; // the Type, ranging from Type_Null to Type_Object
   uint16 isPublic       : 1;
   uint16 isPrivate      : 1;
   uint16 isProtected    : 1;
   uint16 isStatic       : 1;
   uint16 isVolatile     : 1;
   uint16 isTransient    : 1;
   uint16 isFinal        : 1;
   uint16 isInherited    : 1;
   uint16 __available    : 3; // prefer to fill the gap to prevent structure size optimization
} __attribute_packed__ FieldFlags;

#pragma pack()  // restore structure member alignment to default

typedef enum // this order can't be changed, it impacts the compiler and the ObjectMemoryManager.
{
   Type_Null,
   Type_Void,
   Type_Boolean,
   Type_Byte,
   Type_Char,
   Type_Short,
   Type_Int,
   Type_Long,
   Type_Float,
   Type_Double,
   Type_String,
   Type_Object,
   Type_BooleanArray,
   Type_ByteArray,
   Type_CharArray,
   Type_ShortArray,
   Type_IntArray,
   Type_LongArray,
   Type_FloatArray,
   Type_DoubleArray,
   Type_StringArray,
   Type_ObjectArray,
} Type;
#define TYPE_IS_PRIMITIVE(t) (Type_Boolean <= t && t <= Type_Double)

typedef enum
{
   RegI,
   RegO,
   RegD, // can't join into Reg64 !
   RegL,
} RegType;

typedef union TValue TValue;
typedef TValue* Value;
typedef TValue* ValueArray;

typedef struct TObjectProperties TObjectProperties; // An object is prefixed with its properties
typedef TObjectProperties* ObjectProperties;

typedef ValueArray TCObject; // an TCObject is an array of TValues
typedef TCObject* TCObjectArray; // different from the other arrays! this one is a pointer array

typedef struct TField TField;
typedef TField* Field;
typedef TField* FieldArray;

typedef struct TMethod TMethod;
typedef TMethod* Method;
typedef TMethod* MethodArray;
typedef Method* MethodPtrArray;

typedef struct TTCClass TTCClass;
typedef TTCClass* TCClass;
typedef TCClass* TCClassArray;
typedef TCClass* TCClassPtrArray;

typedef struct TException TException;
typedef TException* Exception;
typedef TException* ExceptionArray;

typedef struct TConstantPool TConstantPool;
typedef TConstantPool* ConstantPool;

typedef struct TContext TContext;
#if !defined Context
#if !defined __OBJC__
typedef TContext* Context;
#else
typedef id Context;
#endif
#endif

typedef JCharP TJCharP;
typedef CharP TCharP;

typedef union TCode TCode;
typedef TCode* Code;

/// just to make sure that these are identifiers in the constant pool
#define SYM12 12
#define SYM16 16

/// A TCode is an union with all kinds of parameters used by a single code.
union TCode
{
   struct
   {
      uint32 u32;
   } u32;
   struct
   {
      int32 i32;
   } i32;
   struct
   {
      uint32 v1: 16;
      uint32 v2: 16;
   } two16; // two 16-bit values
   struct
   {
      uint32 op   : 8;
      int32 desloc: 24;
   } s24;
   struct
   {
      uint32 op : 8;
      uint32 rest: 24;
   } op;
   struct
   {
      uint32 op: 8;
      uint32 reg: 8;
      int32 s16: 16; // inc value
   } inc;
   struct
   {
      uint32 param1: 8;
      uint32 param2: 8;
      uint32 param3: 8;
      uint32 param4: 8;
   } params;
   struct
   {
      uint32 dim1: 8;
      uint32 dim2: 8;
      uint32 dim3: 8;
      uint32 dim4: 8;
   } dims;
   struct
   {
      uint32 op   : 8;
      uint32 reg0 : 8; // 6
      uint32 reg1 : 8; // 6
   } reg_reg; // reg <-> reg
   struct
   {
      uint32 op   : 8;
      uint32 sym  : SYM12; // class.fields
      uint32 this_ : 6;
      uint32 reg  : 6; // 6
   } field_reg; // reg <-> regO[reg] / sym12 - instance field
   struct
   {
      uint32 op   : 8;
      uint32 reg  : 8; // 6
      uint32 sym  : SYM16; // cp.sfield - index in constant pool for the full definition of the field (class and name)
   } static_reg; // reg <-> const[sym] - static field
   struct
   {
      uint32 op   : 8;
      uint32 sym  : SYM12; // class.methods
      uint32 this_ : 6;  // this instance, if any
      uint32 retOr1stParam : 6;  // may be the return reg or the first parameter
   } mtd; // method call: the slots may be: the instance, the return value, the parameters
   struct
   {
      uint32 op   : 8;
      uint32 base : 8; // 6 - regO
      uint32 idx  : 8; // 6 - regI
      uint32 reg  : 8; // 6
   } reg_ar; // reg <-> regO[regI]
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 8; // 6
      uint32 sym   : SYM16; // cp.i32 / cp.obj / cp.dbl / cp.i64 / cp.identObj
   } reg_sym; // reg <-> sym
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 6;
      int32 s18    : 18; // signed values must go last!
   } s18_reg; // reg <- s18
   struct
   {
      uint32 op    : 8;
      uint32 reg0  : 8; // 6
      uint32 reg1  : 8; // 6
      uint32 reg2  : 8; // 6
   } reg_reg_reg; // reg <- reg op reg
   struct
   {
      uint32 op    : 8;
      uint32 reg0  : 6;
      uint32 reg1  : 6;
      int32 s12    : 12; // signed values must go last!
   } reg_reg_s12; // reg <- reg op s12 (or s12 op reg)
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 6;
      uint32 base  : 6; // regO
      uint32 idx   : 6; // regI
      int32  s6    : 6; // signed values must go last!
   } reg_s6_ar; // reg <-> base[idx] op s6
   struct
   {
      uint32 op    : 8;
      uint32 sym   : SYM12; // cp.i32
      uint32 reg0  : 6;
      uint32 reg1  : 6;
   } reg_reg_sym; // reg <- reg op sym
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 6;
      int32  s6    : 6;  // signed values must go last!
      int32  desloc: 12;
   } reg_s6_desloc; // if (reg op s6) ip += desloc
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 8; // 6
      int32  desloc: 16;
   } reg_desloc; // if (reg op s6) ip += desloc
   struct
   {
      uint32 op    : 8;
      uint32 sym   : SYM12; // cp.i32
      uint32 reg   : 6;
      int32 desloc : 6;
   } reg_sym_sdesloc; // if (reg op sym) ip += desloc
   struct
   {
      uint32 op    : 8;
      uint32 regI  : 6;
      uint32 base  : 6; // regO
      int32  desloc: 12; // signed values must go last!
   } reg_arl_s12; // if (regI <= base.length) ip += desloc
   struct
   {
      uint32 op    : 8;
      uint32 reg   : 8; // 6
   } reg;
   struct
   {
      uint32 op    : 8;
      uint32 unused: 8;
      uint32 sym   : SYM16; // cp.i32 / cp.obj / cp.dbl / cp.i64
   } sym;
   struct
   {
      uint32 op    : 8;
      uint32 sym   : SYM12; // cp.cls or cp.identPrimitive
      uint32 regO  : 6;
      uint32 lenOrRegIOrDims : 6; // array len or reg number or dimensions
   } newarray;
   struct
   {
      uint32 op    : 8;
      uint32 key   : 8;  // 6 - regI
      uint32 n     : 16; // number of keys
   } switch_reg;
   struct
   {
      uint32 op    : 8;
      uint32 sym   : SYM12; // cp.cls
      uint32 regI  : 6;
      uint32 regO  : 6;
   } instanceof;
};


/** A value held by an TCObject. The object contains an array of Values.
 * The type is known by the program and does not need to be stored here.
 */
union TValue
{
   int32 asInt32;
   double asDouble;
   int64 asInt64;
   uint32 asUInt32;
   TCObject asObj;
   VoidP asVoidP;
   uint32 arrayLen;
};

/** This structure is passed to each native method. */
typedef struct
{
   // input values to the native method
   int32 *i32;
   TCObject *obj;
   union // old v64
   {
      double *dbl;
      int64  *i64;
   };
   // output values from the native method
   union
   {
      int32 retI;
      double retD;
      int64 retL;
   };
   TCObject retO; // retO is outside the union
   // the context for the current thread
   Context currentContext;
} *NMParams, TNMParams;


/// Definition of a NativeMethod: returns void and receives a NMParams structure.
typedef void (*NativeMethod) (NMParams p);

/** Used to find the method reference on a virtual method call. */
typedef struct METHOD_AND_CLASS // guich@tc110_67: create a linked list of bound methods
{
   TCClass c; // in
   Method m; // out
   struct METHOD_AND_CLASS* next;
} TMethodAndClass, *MethodAndClass;
typedef MethodAndClass** MethodAndClassPtrArray;

/** This is the constant pool (CP) loaded from a file. A CP may be related
 * to a single file or to a set of files.
 * . i32    stores the numeric constants of type int
 * . dbl    stores the numeric constants of type double
 * . i64    stores the numeric constants of type long
 * . obj    stores the constants of type String (char16). These are real String
 *          objects and can be directly assigned by the code; no conversion is needed.
 *          These objects are never collected by the garbage collector.
 * . cls    stores the class package+name identifiers, which are char arrays
 * . mf     stores the method names and field names
 * . mtd    stores the fully qualified names of methods
 * . sfield stores the fully qualified names of static fields
 * . ifield stores the fully qualified names of instance fields
 * Important: position 0 of the Contant Heap must not be used.
 * Symbols must start from index 1.
 */
struct TConstantPool
{
   UInt16Array boundIField;
   VoidPArray boundSField;  // will store a pointer directly to the static field inside the class
   MethodPtrArray boundNormal;
   MethodAndClassPtrArray boundVirtualMethod;

   uint16 i32Count;
   uint16 i64Count;
   uint16 dblCount;
   uint16 clsCount;
   uint16 sfieldCount;
   uint16 ifieldCount;
   uint16 mtdCount;
   uint16 mtdfldCount;
   uint16 strCount;

   Int32Array    i32;
   TCObjectArray str;
   DoubleArray   dbl;
   Int64Array    i64;
   CharPArray    cls; // primitive types and class names (full package name)
   CharPArray    mtdfld; // method and field names
   UInt16Matrix mtd;
   UInt8Array   mtdLens;
   Int32Array hashNames,hashParams; // int64 or double is slower
   UInt16Array sfieldField;
   UInt16Array sfieldClass;
   UInt16Array ifieldField;
   UInt16Array ifieldClass;
   Heap heap;
};

/** Represents an Exception (try/catch) declared in the code */
struct TException
{
   // The class name to whom this Exception belongs to
   CharP className;
   // The starting program counter
   Code startPC;
   // The ending program counter
   Code endPC;
   // The program counter of the code that handles this exception
   Code handlerPC;
   // The regO that stores the instance of this Exception
   uint16 regO;
};

/// to be used when a class is passed as parameter
#define FIELD_I32_OFFSET(o) ((uint8*)(o))
#define FIELD_OBJ_OFFSET(o,c) (((uint8*)(o))+c->objOfs)
#define FIELD_V64_OFFSET(o,c) (((uint8*)(o))+c->v64Ofs)

#define FIELD_I32(o,idx) (((int32*)(FIELD_I32_OFFSET(o)))[idx])
#define FIELD_OBJ(o,c,idx) (((TCObject*)(FIELD_OBJ_OFFSET(o,c)))[idx])
#define FIELD_I64(o,c,idx) (((int64*)(FIELD_V64_OFFSET(o,c)))[idx])  // i64 and dbl point to the same structure
#define FIELD_DBL(o,c,idx) (((double*)(FIELD_V64_OFFSET(o,c)))[idx])

/** This structure represents a Java class. */
struct TTCClass
{
   // The offsets to the object where each instance field type starts in an TCObject
   uint16 objOfs, v64Ofs; // there's no i32Ofs because int32's offset is always 0
   // The constant pool assigned to this class.
   ConstantPool cp;
   // The instance object's size
   uint32 objSize;
   // A string representing this class.
   CharP name;
   // The access flags of this class (e.g.: if this class is public, static, etc)
   ClassFlags flags;
   // The fields that will be copied into each instance (an TCObject) of this class.
   FieldArray i32InstanceFields;
   FieldArray v64InstanceFields;
   FieldArray objInstanceFields;
   FieldArray instanceFields[4]; // for faster indexed access by RegType
   // The fields that will be shared among all the instances of this class
   FieldArray i32StaticFields;
   FieldArray v64StaticFields;
   FieldArray objStaticFields;
   FieldArray staticFields[4]; // for faster indexed access by RegType
   // The current values for static fields
   Int32Array  i32StaticValues;
   DoubleArray v64StaticValues;
   TCObjectArray objStaticValues;
   // The methods declared in this class
   MethodArray methods;
   // The interfaces that this class implements
   TCClassArray interfaces;
   // The superclass of this class. The only class that has a null superClass is java.lang.Object
   TCClass superClass;
   // The original source code for each line of this class
   CharPArray lines;
   Heap heap;
   // The finalize method, if any
   Method finalizeMethod;
   // If present and true, don't call finalize
   uint16 dontFinalizeFieldIndex;
   // Index in the vLoadedClasses array
   uint32 index;
   uint32 hash;
   // Used in reflection
   TCObject classObj;
};

/** Structure representing a method of a class. */
struct TMethod
{
   // How many registers of each type are used in this method, including
   // the method parameters, but excluding the instance reference (if any)
   uint8 iCount,oCount, v64Count, paramSkip; // 3
   // an array of instructions.
   Code code; // 4
   // The class to whom this method belongs to
   TCClass class_; // 8
   // Signature of this method, containing indexes to the CP: name, number of parameters, and the parameters
   CharP name; // 0xC
   uint16 paramCount;
   UInt16Array cpParams; // indexes to the constant pool of the parameters - 0x14
   UInt8Array paramRegs; // the RegType to where the parameters must go on - two bits would be enough, but we use more to improve speed
   int32 hashName, hashParams; // hash for the name and parameters
   // The return type, as an index to the constant pool. Zero for constructors and methods that return void.
   uint16 cpReturn;
   RegType returnReg; // register used for return - 0x28
   // The method's access flags (public, static, private, etc)
   MethodFlags flags;
   // The exception handlers defining which parts of the code have a try/catch
   ExceptionArray exceptionHandlers; // 0x30
   // Used in debug info - the line numbers for each set of instructions
   UInt16Array lineNumberLine; // The line number itself; - 0x34
   UInt16Array lineNumberStartPC; // The pc where this line number goes - 0x38
   // if this method is native, this holds the native signature
   CharP nativeSignature; // 0x3C
   NativeMethod boundNM; // 0x40
   uint32 ref; // library reference
};

/** This structure represents a Java int, double, long and TCObject class field. */
struct TField
{
   // The field's name
   CharP name;
   // The access flags of this field (isPublic, isPrivate, isObject, isArray, etc) and the Type
   FieldFlags flags;
   // The fully qualified class name
   CharP sourceClassName, targetClassName; // class where the field is, class type of the field
};

// java flags (used during reflection)
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

/// String representing a constructor
#define CONSTRUCTOR_NAME "<C>" // name of the constructor
/// String representing a static initializer
#define STATIC_INIT_NAME "<S>" // name of the static initializer
/// Index in the default constants for the constructor string
#define CONSTRUCTOR_CP 23
/// Index in the default constants for the static initializer string
#define STATIC_INIT_CP 24
/// Current length of the default constants array.
#define DEFAULT_CONSTANTS_LEN 25

/// A string representing a Java char array
#define CHAR_ARRAY    "[&C"
/// A string representing a Java byte array
#define BYTE_ARRAY    "[&B"
/// A string representing a Java short array
#define SHORT_ARRAY   "[&S"
/// A string representing a Java int array
#define INT_ARRAY     "[&I"
/// A string representing a Java long array
#define LONG_ARRAY    "[&L"
/// A string representing a Java double array
#define DOUBLE_ARRAY  "[&D"
/// A string representing a Java boolean array
#define BOOLEAN_ARRAY "[&b"
#define BOOLEAN_MATRIX "[[&b"
/// A string representing a Java float array
#define FLOAT_ARRAY   "[&F"
/// A string representing a Java int
#define J_INT "I"
/// A string representing a Java double
#define J_DOUBLE "D"
/// A string representing a Java long
#define J_LONG "L"
/// A string representing a Java byte
#define J_BYTE "B"
/// A string representing a Java short
#define J_SHORT "S"
/// A string representing a Java char
#define J_CHAR "C"
/// A string representing a Java boolean
#define J_BOOLEAN "b"

/// Returns the index of a given identifier in the constant pool
int32 getIndexInCP(ConstantPool cp, CharP s);
/// Loads the given class name, throwing a ClassNotFoundException if desired.
TC_API TCClass loadClass(Context currentContext, CharP className, bool throwClassNotFound);
typedef TCClass (*loadClassFunc)(Context currentContext, CharP className, bool throwClassNotFound);

bool initClassInfo();
void destroyClassInfo();

/// Determines the possible results for the areClassesCompatible function.
typedef enum { NOT_COMPATIBLE, COMPATIBLE, TARGET_CLASS_NOT_FOUND} CompatibilityResult;
/// Check if the class c is a compatible instance of the given class name.
TC_API CompatibilityResult areClassesCompatible(Context currentContext, TCClass c, CharP className);
typedef CompatibilityResult (*areClassesCompatibleFunc)(Context currentContext, TCClass c, CharP className);

/// Checks if the methods have the same parameters
bool paramsEq(ConstantPool cp1, UInt16Array params1, int32 n1, ConstantPool cp2, UInt16Array params2);

Type type2javaType(CharP type);
bool isSuperClass(TCClass s, TCClass t);

#define CLASS_OUT_OF_MEMORY ((TCClass)-1)

#ifdef __cplusplus
}
#endif
#endif
