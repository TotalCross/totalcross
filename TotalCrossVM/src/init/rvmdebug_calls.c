#include "rvmdebug_calls.h"

int rvmLog(int level, const char* tag, const char* text)
{
}

int rvmLogf(int level, const char* tag, const char* format, ...)
{
}

void gcAddRoot(void* ptr)
{
}

void* gcAllocate(size_t size)
{
}

Object* rvmAllocateObject(Env* env, Class* clazz)
{
}

CallStack* rvmCaptureCallStack(Env* env)
{
}

jboolean rvmExceptionCheck(Env* env)
{
}

Object* rvmExceptionClear(Env* env)
{
}

CallStackFrame* rvmResolveCallStackFrame(Env* env, CallStackFrame* frame) {
//    if (frame->pc == NULL && frame->method == NULL) {
//        // We've already tried to resolve this frame but
//        // it doesn't correspond to any method
//        return NULL;
//    }
//    if (frame->method != NULL) {
//        // We've already resolved this frame successfully or
//        // the method is a ProxyMethod so no call to rvmFindMethodAtAddress()
//        // is required
//        return frame;
//    }
//    frame->method = rvmFindMethodAtAddress(env, frame->pc);
//    if (!frame->method) {
//        frame->pc = NULL;
//        return NULL;
//    }
//    frame->lineNumber = METHOD_IS_NATIVE(frame->method) ? -2 : getLineNumber(frame);
//    return frame;
}


CallStackFrame* rvmGetNextCallStackMethod(Env* env, CallStack* callStack, jint* index)
{
    while (*index < callStack->length) {
        CallStackFrame* frame = rvmResolveCallStackFrame(env, &callStack->frames[*index]);
        *index += 1;
        if (frame && frame->method) {
            return frame;
        }
    }
    return NULL;
}

jint rvmInitMutex(Mutex* mutex) {
    pthread_mutexattr_t mutexAttrs;
    pthread_mutexattr_init(&mutexAttrs);
    pthread_mutexattr_settype(&mutexAttrs, PTHREAD_MUTEX_RECURSIVE);
    return pthread_mutex_init(mutex, &mutexAttrs);
}

jint rvmLockMutex(Mutex* mutex) {
    return pthread_mutex_lock(mutex);
}

jint rvmUnlockMutex(Mutex* mutex) {
    return pthread_mutex_unlock(mutex);
}

void rvmPopGatewayFrame(Env* env)
{
}

void rvmPushGatewayFrame(Env* env)
{
}

jlong rvmRTGetThreadId(Env* env, Object* threadObj) {
    return ((Thread*) threadObj)->threadId;
}

void rvmThrow(Env* env, Object* e)
{
}

jboolean rvmThrowIllegalArgumentException(Env* env, const char* message)
{
}

jboolean rvmThrowInstantiationError(Env* env, const char* message)
{
}

Object* rvmNewStringUTF(Env* env, const char* s, jint length)
{
}

jfloat rvmCallFloatInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

Object* rvmNewBooleanArray(Env* env, jint length)
{
}

Object* rvmNewByteArray(Env* env, jint length)
{
}

Object* rvmNewCharArray(Env* env, jint length)
{
}

Object* rvmNewShortArray(Env* env, jint length)
{
}

Object* rvmNewIntArray(Env* env, jint length)
{
}

Object* rvmNewLongArray(Env* env, jint length)
{
}

Object* rvmNewFloatArray(Env* env, jint length)
{
}

Object* rvmNewDoubleArray(Env* env, jint length)
{
}

Object* rvmNewObjectArray(Env* env, jint length, Class* elementClass, Class* arrayClass, Object* init)
{
}


jboolean rvmCallBooleanInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jbyte rvmCallByteInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jchar rvmCallCharInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jshort rvmCallShortInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jint rvmCallIntInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jlong rvmCallLongInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jdouble rvmCallDoubleInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

jboolean rvmCallBooleanClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jbyte rvmCallByteClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jchar rvmCallCharClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jshort rvmCallShortClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jint rvmCallIntClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jlong rvmCallLongClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jfloat rvmCallFloatClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

jdouble rvmCallDoubleClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}
void rvmCallVoidClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

void rvmCallVoidInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

Object* rvmCallObjectClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

Object* rvmCallObjectInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

Class* rvmFindClass(Env* env, const char* className)
{
}

Method* rvmGetMethod(Env* env, Class* clazz, const char* name, const char* desc)
{
}
