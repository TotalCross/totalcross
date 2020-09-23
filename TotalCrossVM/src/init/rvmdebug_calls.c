#include "rvmdebug_calls.h"
#include <stdarg.h>

#define LOG_BUF_SIZE 1024
#define IS_ENABLED(level) (_logLevel < LOG_LEVEL_SILENT && _logLevel <= level)

jint _logLevel = LOG_LEVEL_TRACE;
static const char* levels[] = {
        "TRACE",
        "DEBUG",
        "INFO",
        "WARN",
        "ERROR",
        "FATAL"
};

static const char* level2String(int level) {
    if (level < LOG_LEVEL_TRACE) level = LOG_LEVEL_TRACE;
    return levels[level - LOG_LEVEL_TRACE];
}

static inline int logwrite(int level, const char* tag, const char* text) {
    return fprintf(stderr, "[%s] %s: %s\n", level2String(level), tag, text); \
}


int rvmLog(int level, const char* tag, const char* text) {
    if (IS_ENABLED(level)) {
        return logwrite(level, tag, text);
    }
    return 0;
}

int rvmLogf(int level, const char* tag, const char* format, ...) {
    va_list ap;
    char buf[LOG_BUF_SIZE];
    if (IS_ENABLED(level)) {
        va_start(ap, format);
        vsnprintf(buf, LOG_BUF_SIZE, format, ap);
        va_end(ap);
        return logwrite(level, tag, buf);
    }
    return 0;
}

void gcAddRoot(void* ptr)
{
}

void* gcAllocate(size_t size)
{
    return 0;
}

Object* rvmAllocateObject(Env* env, Class* clazz)
{
    return 0;
}

CallStack* rvmCaptureCallStack(Env* env)
{
    return 0;
}

jboolean rvmExceptionCheck(Env* env)
{
    return 0;
}

Object* rvmExceptionClear(Env* env)
{
    return 0;
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
    return 0;
}

Method* rvmFindMethodAtAddress(Env* env, void* address) {
//    Class* clazz = env->vm->options->findClassAt(env, address);
//    if (!clazz) return NULL;
//    Method* method = rvmGetMethods(env, clazz);
//    if (rvmExceptionCheck(env)) return NULL;
//    for (; method != NULL; method = method->next) {
//        void* start = method->impl;
//        void* end = start + method->size;
//        if (start && address >= start && address < end) {
//            return method;
//        }
//    }
//    // TODO: We should never end up here
    return NULL;
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
    return 0;
}

jboolean rvmThrowInstantiationError(Env* env, const char* message)
{
    return 0;
}

Object* rvmNewStringUTF(Env* env, const char* s, jint length)
{
    return 0;
}

jfloat rvmCallFloatInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jbooleanArray* rvmNewBooleanArray(Env* env, jint length)
{
    return 0;
}

jbyteArray* rvmNewByteArray(Env* env, jint length)
{
    return 0;
}

jcharArray* rvmNewCharArray(Env* env, jint length)
{
    return 0;
}

jshortArray* rvmNewShortArray(Env* env, jint length)
{
    return 0;
}

jintArray* rvmNewIntArray(Env* env, jint length)
{
    return 0;
}

jlongArray* rvmNewLongArray(Env* env, jint length)
{
    return 0;
}

jfloatArray* rvmNewFloatArray(Env* env, jint length)
{
    return 0;
}

jdoubleArray* rvmNewDoubleArray(Env* env, jint length)
{
    return 0;
}

jobjectArray* rvmNewObjectArray(Env* env, jint length, Class* elementClass, Class* arrayClass, Object* init)
{
    return 0;
}


jboolean rvmCallBooleanInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jbyte rvmCallByteInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jchar rvmCallCharInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jshort rvmCallShortInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jint rvmCallIntInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jlong rvmCallLongInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jdouble rvmCallDoubleInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

jboolean rvmCallBooleanClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jbyte rvmCallByteClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jchar rvmCallCharClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jshort rvmCallShortClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jint rvmCallIntClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jlong rvmCallLongClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jfloat rvmCallFloatClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

jdouble rvmCallDoubleClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

void rvmCallVoidClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
}

void rvmCallVoidInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
}

Object* rvmCallObjectClassMethodA(Env* env, Class* clazz, Method* method, jvalue* args)
{
    return 0;
}

Object* rvmCallObjectInstanceMethodA(Env* env, Object* obj, Method* method, jvalue* args)
{
    return 0;
}

Class* rvmFindClass(Env* env, const char* className)
{
    return 0;
}

Method* rvmGetMethod(Env* env, Class* clazz, const char* name, const char* desc)
{
    return 0;
}
