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

#ifndef FIELD_H
#define FIELD_H

#ifdef __cplusplus
extern "C" {
#endif

#define UNBOUND 65535U
#define UNBOUND_ERROR 65534U
#define UNBOUND_FIELD_ERROR 65535U
#define UNBOUND_CLASS_ERROR 65534U

#define SF_FIELD_ERROR  ((VoidP)-1)
#define SF_CLASS_ERROR  ((VoidP)-2)
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
TC_API TCObject* getStaticFieldObject(Context context, TCClass c, CharP fieldName);
typedef TCObject* (*getStaticFieldObjectFunc)(Context context, TCClass c, CharP fieldName);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API int32* getInstanceFieldInt(TCObject instance, CharP fieldName, CharP className);
typedef int32* (*getInstanceFieldIntFunc)(TCObject instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API double* getInstanceFieldDouble(TCObject instance, CharP fieldName, CharP className);
typedef double* (*getInstanceFieldDoubleFunc)(TCObject instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API int64* getInstanceFieldLong(TCObject instance, CharP fieldName, CharP className);
typedef int64* (*getInstanceFieldLongFunc)(TCObject instance, CharP fieldName, CharP className);
/** Returns a reference to an instance field of the given Java type.  Class name is the class where
    the field is declared, or null if from current class. It may be necessary to pass it if there's
    more than one field with the same name in the class hierarchy. */
TC_API TCObject* getInstanceFieldObject(TCObject instance, CharP fieldName, CharP className);
typedef TCObject* (*getInstanceFieldObjectFunc)(TCObject instance, CharP fieldName, CharP className);

/// Returns the field and the class name of an instance field
void getIField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className);
/// Returns the field and the class name of a static field
void getSField_Names(ConstantPool cp, int32 sym, CharP* fieldName, CharP* className);

/// Returns the instance field index of a field in a class.
extern uint16 getIField_Index(ConstantPool cp, TCObject o, int32 classAndField, RegType t);
/// Returns a reference to a static field.
extern VoidP getSField_Ref(Context currentContext, TCClass c, int32 classAndField, RegType t);

/// Returns the index to the given instance field.
extern uint16 getInstanceFieldIndex(CharP fieldName, CharP fieldClassName, TCObject o, RegType t);

#ifdef __cplusplus
}
#endif
#endif
