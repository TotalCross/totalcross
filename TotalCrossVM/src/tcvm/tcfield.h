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



#ifndef FIELD_H
#define FIELD_H

#ifdef __cplusplus
extern "C" {
#endif

#define UNBOUND 65535U
#define UNBOUND_ERROR 65534U
#define UNBOUND_FIELD_ERROR 65535U
#define UNBOUND_CLASS_ERROR 65534U

// IMPORTANT: to support 64-bit processors, these two macros must be redefined accordingly to 1<<32-1, 1<<32-2
#define SF_FIELD_ERROR  ((VoidP)0xFFFFFFFF)
#define SF_CLASS_ERROR  ((VoidP)0xFFFFFFFE)
#define SF_ERROR_ANY    SF_CLASS_ERROR

/// Returns a reference to a static field of the given Java type.
TC_API int32* getStaticFieldInt(TCClass c, CharP fieldName);
typedef int32* (*getStaticFieldIntFunc)(TCClass c, CharP fieldName);
/// Returns a reference to a static field of the given Java type.
TC_API double* getStaticFieldDouble(TCClass c, CharP fieldName);
typedef double* (*getStaticFieldDoubleFunc)(TCClass c, CharP fieldName);
/// Returns a reference to a static field of the given Java type.
TC_API int64* getStaticFieldLong(TCClass c, CharP fieldName);
typedef int64* (*getStaticFieldLongFunc)(TCClass c, CharP fieldName);
/// Returns a reference to a static field of the given Java type.
TC_API Object* getStaticFieldObject(TCClass c, CharP fieldName);
typedef Object* (*getStaticFieldObjectFunc)(TCClass c, CharP fieldName);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API int32* getInstanceFieldInt(Object instance, CharP fieldName, CharP className);
typedef int32* (*getInstanceFieldIntFunc)(Object instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API double* getInstanceFieldDouble(Object instance, CharP fieldName, CharP className);
typedef double* (*getInstanceFieldDoubleFunc)(Object instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API int64* getInstanceFieldLong(Object instance, CharP fieldName, CharP className);
typedef int64* (*getInstanceFieldLongFunc)(Object instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API Object* getInstanceFieldObject(Object instance, CharP fieldName, CharP className);
typedef Object* (*getInstanceFieldObjectFunc)(Object instance, CharP fieldName, CharP className);

/// Returns the field and the class name of an instance field
void getIField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className);
/// Returns the field and the class name of a static field
void getSField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className);

/// Returns the instance field index of a field in a class.
extern inline uint16 getIField_Index(ConstantPool cp, Object o, int32 classAndField, RegType t);
/// Returns a reference to a static field.
extern VoidP getSField_Ref(Context currentContext, TCClass c, int32 classAndField, RegType t);

/// Returns the index to the given instance field.
extern inline uint16 getInstanceFieldIndex(CharP fieldName, CharP fieldClassName, Object o, RegType t);

#ifdef __cplusplus
}
#endif
#endif
