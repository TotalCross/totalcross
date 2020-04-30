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

#ifndef OBJECTMEMORYMANAGER_H
#define OBJECTMEMORYMANAGER_H

#ifdef __cplusplus
extern "C" {
#endif

typedef enum
{
   UNLOCKED,
   LOCKED,
} LockState;

/// ALWAYS call createObject instead of this one, unless you will call another constructor besides of the default one
TC_API void preallocateArray(Context currentContext, TCObject sample, int32 length);
typedef void (*preallocateArrayFunc)(Context currentContext, TCObject sample, int32 length);
TC_API TCObject createObjectWithoutCallingDefaultConstructor(Context currentContext, CharP className);
typedef TCObject (*createObjectWithoutCallingDefaultConstructorFunc)(Context currentContext, CharP className);
TC_API TCObject createObject(Context currentContext, CharP className);
typedef TCObject (*createObjectFunc)(Context currentContext, CharP className);
TC_API TCObject createByteArrayObject(Context currentContext, int32 len, const char *file, int32 line);
typedef TCObject (*createByteArrayObjectFunc)(Context currentContext, int32 len);
TC_API TCObject createArrayObject(Context currentContext, CharP type, int32 len);
typedef TCObject (*createArrayObjectFunc)(Context currentContext, CharP type, int32 len);
TC_API TCObject createArrayObjectMulti(Context currentContext, CharP type, int32 count, uint8* dims, int32* regI); // always call passing target as a held variable!
typedef TCObject (*createArrayObjectMultiFunc)(Context currentContext, CharP type, int32 count, uint8* dims, int32* regI); // always call passing target as a held variable!
/// only allocate space, you must transfer the char array by your own
TC_API TCObject createStringObjectWithLen(Context currentContext, int32 len);
typedef TCObject (*createStringObjectWithLenFunc)(Context currentContext, int32 len);
/// if len<0, len is computed from srcChars length
TC_API TCObject createStringObjectFromJCharP(Context currentContext, JCharP srcChars, int32 len);
typedef TCObject (*createStringObjectFromJCharPFunc)(Context currentContext, JCharP srcChars, int32 len);
/// if len<0, len is computed from srcChars length
TC_API TCObject createStringObjectFromTCHARP(Context currentContext, TCHARP srcChars, int32 len);
typedef TCObject (*createStringObjectFromTCHARPFunc)(Context currentContext, TCHARP srcChars, int32 len);
/// if len<0, len is computed from srcChars length
TC_API TCObject createStringObjectFromCharP(Context currentContext, CharP srcChars, int32 len);
typedef TCObject (*createStringObjectFromCharPFunc)(Context currentContext, CharP srcChars, int32 len);
/// if len<0, len is computed from srcChars length, use when creating code for both WinCE and Win32
#if defined(UNICODE)
#define createStringObjectFromTCHAR createStringObjectFromJCharP
#else
#define createStringObjectFromTCHAR createStringObjectFromCharP
#endif

/// Creates an array of Java chars
#define createCharArray(currentContext, len)   TCAPI_FUNC(createArrayObject)(currentContext, CHAR_ARRAY, len) // these are
/// Creates an array of Java bytes
#define createByteArray(currentContext, len)   TCAPI_FUNC(createByteArrayObject)(currentContext, len,__FILE__,__LINE__) // java types
/// Creates an array of Java ints
#define createIntArray(currentContext, len)    TCAPI_FUNC(createArrayObject)(currentContext, INT_ARRAY, len)
/// Creates an array of Java Strings
#define createStringArray(currentContext, len) TCAPI_FUNC(createArrayObject)(currentContext, "[java.lang.String", len)

/// Calls the garbage collector
TC_API void gc(Context currentContext);
typedef void (*gcFunc)(Context currentContext);

bool initObjectMemoryManager();
void destroyObjectMemoryManager();
void runFinalizers();
void visitImages(VisitElementFunc onImage, int32 param);

/// Changes the object lock state, in a NON-RECURSIVE way.
/// Locking an object: The object will be removed from the used list and will never be garbage collected.
/// Unlocking an object: The object will be placed back in the used list and will be eligible to be garbage collected
/// If you lock an object, all objects inside of it are still visited during a GC, so you have to lock only the root of
/// an object tree to keep all them in memory.
TC_API void setObjectLock(TCObject o, LockState lock);
typedef void (*setObjectLockFunc)(TCObject o, LockState lock);

/// Returns a pointer to the Object properties given an Object
#define OBJ_PROPERTIES(o) ((ObjectProperties)(((uint8*)(o))-sizeof(TObjectProperties)))
#define OBJ_ISLOCKED(o)     (OBJ_PROPERTIES(o)->lock  == 1)

/** A Java Object is a Class instance.
 *
 * The instance field values are stored here, while all the other members
 * (including the static fields and the instance field definitions) are stored
 * in the Class member.
 * The next,prev and temp fields are used by the memory manager.
 *
 * An Object is composed by its ObjectProperties, followed by a Value array.
 */
struct TObjectProperties
{
   TCClass class_;
   TCObject next,prev;
   struct
   {
      uint32 size: 30; // object's size
      uint32 lock: 1;  // lock the object, preventing it from being gc'd. The initial purpose of locking an object was to lock all constant pool strings and speedup the garbage collector process.
      uint32 mark: 1;  // mark the object during a garbage collect.
#if TBITS==64
      uint32 dumb;
#endif
   };
};

typedef uint8* Chunk;

/// Returns the Class of a given Object
#define OBJ_CLASS(o) OBJ_PROPERTIES(o)->class_ // get class from an object

/// Gets the length of a Java array Object. The array's type is stored in the OBJ_CLASS(o)->name ("[&B","[java.lang.String", etc)
#define ARRAYOBJ_LEN(o) ((o)->arrayLen)
/// Gets the start of a Java array Object. The array's type is stored in the OBJ_CLASS(o)->name ("[&B","[java.lang.String", etc)
#define ARRAYOBJ_START(o) (((uint8*)(o))+TSIZE)

// Gets the size in bytes of an array. Added to support 64-bit without needing to change tcz structure
#define TC_ARRAYSIZE(c, len) (c->flags.isObjectArray ? len * TSIZE : len << c->flags.bits2shift)

#ifdef __cplusplus
}
#endif
#endif
