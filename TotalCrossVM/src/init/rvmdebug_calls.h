//
// Created by Panayotis on 1/8/20.
//

#ifndef TCVM_RVMDEBUG_CALLS_H
#define TCVM_RVMDEBUG_CALLS_H

#include "rvmdebug.h"
#include "rvmdebug_types.h"

int rvmLog(int level, const char* tag, const char* text);

int rvmLogf(int level, const char* tag, const char* format, ...);
jlong rvmRTGetThreadId(Env* env, Object* threadObj);
jint rvmLockMutex(Mutex* mutex);
jint rvmUnlockMutex(Mutex* mutex);
jint rvmInitMutex(Mutex* mutex);
void gcAddRoot(void* ptr);
jboolean rvmExceptionCheck(Env* env);
void* gcAllocate(size_t size);
jboolean rvmThrowIllegalArgumentException(Env* env, const char* message);
Object* rvmExceptionClear(Env* env);
Object* rvmAllocateObject(Env* env, Class* clazz);
jboolean rvmThrowInstantiationError(Env* env, const char* message);
void rvmThrow(Env* env, Object* e);
void rvmPushGatewayFrame(Env* env);
void rvmPopGatewayFrame(Env* env);
CallStackFrame* rvmGetNextCallStackMethod(Env* env, CallStack* callStack, jint* index);


//jboolean rvmInitStrings(Env* env);
//Object* rvmNewStringNoCopy(Env* env, CharArray* value, jint offset, jint length);
//Object* rvmNewString(Env* env, const jchar* chars, jint length);
Object* rvmNewStringUTF(Env* env, const char* s, jint length);
//Object* rvmNewStringAscii(Env* env, const char* s, jint length);
//Object* rvmNewInternedStringUTF(Env* env, const char* s, jint length);
//Object* rvmInternString(Env* env, Object* str);
//jint rvmGetStringLength(Env* env, Object* str);
//jchar* rvmGetStringChars(Env* env, Object* str);
//jint rvmGetStringUTFLength(Env* env, Object* str);
//char* rvmGetStringUTFChars(Env* env, Object* str);
//void rvmGetStringRegion(Env* env, Object* str, jint start, jint len, jchar* buf);
//void rvmGetStringUTFRegion(Env* env, Object* str, jint start, jint len, char* buf);


jbooleanArray* rvmNewBooleanArray(Env* env, jint length);
jbyteArray* rvmNewByteArray(Env* env, jint length);
jcharArray* rvmNewCharArray(Env* env, jint length);
jshortArray* rvmNewShortArray(Env* env, jint length);
jintArray* rvmNewIntArray(Env* env, jint length);
jlongArray* rvmNewLongArray(Env* env, jint length);
jfloatArray* rvmNewFloatArray(Env* env, jint length);
jdoubleArray* rvmNewDoubleArray(Env* env, jint length);
jobjectArray* rvmNewObjectArray(Env* env, jint length, Class* elementClass, Class* arrayClass, Object* init);
//extern Array* rvmNewMultiArray(Env* env, jint dims, jint* lengths, Class* type);
//extern Array* rvmCloneArray(Env* env, Array* array);
//extern jint rvmGetArrayDimensions(Env* env, Array* array);
//extern jint rvmGetArrayElementSize(Env* env, Class* arrayClass);
//extern jlong rvmGetArraySize(Env* env, Class* arrayClass, jint length);


//extern jboolean rvmInitMethods(Env* env);
//extern const char* rvmGetReturnType(const char* desc);
//extern const char* rvmGetNextParameterType(const char** desc);
//extern jint rvmGetParameterCount(Method* method);
Method* rvmGetMethod(Env* env, Class* clazz, const char* name, const char* desc);
//extern jboolean rvmHasMethod(Env* env, Class* clazz, const char* name, const char* desc);
//extern Method* rvmGetClassMethod(Env* env, Class* clazz, const char* name, const char* desc);
//extern Method* rvmGetClassInitializer(Env* env, Class* clazz);
//extern Method* rvmGetInstanceMethod(Env* env, Class* clazz, const char* name, const char* desc);
//extern jboolean rvmRegisterNative(Env* env, NativeMethod* method, void* impl);
//extern jboolean rvmUnregisterNative(Env* env, NativeMethod* method);
//extern void* rvmResolveNativeMethodImpl(Env* env, NativeMethod* method, const char* shortMangledName, const char* longMangledName, Object* classLoader, void** ptr);
//extern jboolean rvmLoadNativeLibrary(Env* env, const char* path, Object* classLoader);
Method* rvmFindMethodAtAddress(Env* env, void* address);
//extern Method* rvmGetCallingMethod(Env* env);
CallStack* rvmCaptureCallStack(Env* env);
//extern CallStack* rvmCaptureCallStackForThread(Env* env, Thread* thread);
//extern CallStackFrame* rvmResolveCallStackFrame(Env* env, CallStackFrame* frame);
//extern ObjectArray* rvmCallStackToStackTraceElements(Env* env, CallStack* callStack, jint first);
//extern void rvmCallVoidInstanceMethod(Env* env, Object* obj, Method* method, ...);
void rvmCallVoidInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern void rvmCallVoidInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern Object* rvmCallObjectInstanceMethod(Env* env, Object* obj, Method* method, ...);
Object* rvmCallObjectInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern Object* rvmCallObjectInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jboolean rvmCallBooleanInstanceMethod(Env* env, Object* obj, Method* method, ...);
jboolean rvmCallBooleanInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jboolean rvmCallBooleanInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jbyte rvmCallByteInstanceMethod(Env* env, Object* obj, Method* method, ...);
jbyte rvmCallByteInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jbyte rvmCallByteInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jchar rvmCallCharInstanceMethod(Env* env, Object* obj, Method* method, ...);
jchar rvmCallCharInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jchar rvmCallCharInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jshort rvmCallShortInstanceMethod(Env* env, Object* obj, Method* method, ...);
jshort rvmCallShortInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jshort rvmCallShortInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jint rvmCallIntInstanceMethod(Env* env, Object* obj, Method* method, ...);
jint rvmCallIntInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jint rvmCallIntInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jlong rvmCallLongInstanceMethod(Env* env, Object* obj, Method* method, ...);
jlong rvmCallLongInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jlong rvmCallLongInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jfloat rvmCallFloatInstanceMethod(Env* env, Object* obj, Method* method, ...);
jfloat rvmCallFloatInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jfloat rvmCallFloatInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jdouble rvmCallDoubleInstanceMethod(Env* env, Object* obj, Method* method, ...);
jdouble rvmCallDoubleInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jdouble rvmCallDoubleInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern void rvmCallNonvirtualVoidInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern void rvmCallNonvirtualVoidInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern void rvmCallNonvirtualVoidInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern Object* rvmCallNonvirtualObjectInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern Object* rvmCallNonvirtualObjectInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern Object* rvmCallNonvirtualObjectInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jboolean rvmCallNonvirtualBooleanInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jboolean rvmCallNonvirtualBooleanInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jboolean rvmCallNonvirtualBooleanInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jbyte rvmCallNonvirtualByteInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jbyte rvmCallNonvirtualByteInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jbyte rvmCallNonvirtualByteInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jchar rvmCallNonvirtualCharInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jchar rvmCallNonvirtualCharInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jchar rvmCallNonvirtualCharInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jshort rvmCallNonvirtualShortInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jshort rvmCallNonvirtualShortInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jshort rvmCallNonvirtualShortInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jint rvmCallNonvirtualIntInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jint rvmCallNonvirtualIntInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jint rvmCallNonvirtualIntInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jlong rvmCallNonvirtualLongInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jlong rvmCallNonvirtualLongInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jlong rvmCallNonvirtualLongInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jfloat rvmCallNonvirtualFloatInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jfloat rvmCallNonvirtualFloatInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jfloat rvmCallNonvirtualFloatInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern jdouble rvmCallNonvirtualDoubleInstanceMethod(Env* env, Object* obj, Method* method, ...);
//extern jdouble rvmCallNonvirtualDoubleInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args);
//extern jdouble rvmCallNonvirtualDoubleInstanceMethodV(Env* env, Object* obj, Method* method, va_list args);
//extern void rvmCallVoidClassMethod(Env* env, Class* clazz, Method* method, ...);
void rvmCallVoidClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern void rvmCallVoidClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern Object* rvmCallObjectClassMethod(Env* env, Class* clazz, Method* method, ...);
Object* rvmCallObjectClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern Object* rvmCallObjectClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jboolean rvmCallBooleanClassMethod(Env* env, Class* clazz, Method* method, ...);
jboolean rvmCallBooleanClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jboolean rvmCallBooleanClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jbyte rvmCallByteClassMethod(Env* env, Class* clazz, Method* method, ...);
jbyte rvmCallByteClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jbyte rvmCallByteClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jchar rvmCallCharClassMethod(Env* env, Class* clazz, Method* method, ...);
jchar rvmCallCharClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jchar rvmCallCharClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jshort rvmCallShortClassMethod(Env* env, Class* clazz, Method* method, ...);
jshort rvmCallShortClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jshort rvmCallShortClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jint rvmCallIntClassMethod(Env* env, Class* clazz, Method* method, ...);
jint rvmCallIntClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jint rvmCallIntClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jlong rvmCallLongClassMethod(Env* env, Class* clazz, Method* method, ...);
jlong rvmCallLongClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jlong rvmCallLongClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jfloat rvmCallFloatClassMethod(Env* env, Class* clazz, Method* method, ...);
jfloat rvmCallFloatClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jfloat rvmCallFloatClassMethodV(Env* env, Class* clazz, Method* method, va_list args);
//extern jdouble rvmCallDoubleClassMethod(Env* env, Class* clazz, Method* method, ...);
jdouble rvmCallDoubleClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args);
//extern jdouble rvmCallDoubleClassMethodV(Env* env, Class* clazz, Method* method, va_list args);



Class* rvmFindClass(Env* env, const char* className);
//extern Class* rvmFindClassInClasspathForLoader(Env* env, const char* className, Object* classLoader);
//extern Class* rvmFindClassUsingLoader(Env* env, const char* className, Object* classLoader);
//extern Class* rvmFindClassByDescriptor(Env* env, const char* desc, Object* classLoader);
//extern Class* rvmFindLoadedClass(Env* env, const char* className, Object* classLoader);
//extern Object* rvmGetSystemClassLoader(Env* env);

#endif //TCVM_RVMDEBUG_CALLS_H
