//
// Created by Panayotis on 1/8/20.
//

#ifndef TCVM_RVMDEBUG_H
#define TCVM_RVMDEBUG_H

// FILE and relatives
#include <stdio.h>

// pthread_cond_t and relatives
#include <pthread.h>

#include "rvmdebug_types.h"
#include "rvmdebug_calls.h"
#include <stdlib.h>


#ifdef DEBUG
#define IS_DEBUG_ENABLED TRUE
#else
#define IS_DEBUG_ENABLED FALSE
#endif

struct Options {
//    char* mainClass;
//    char** commandLineArgs;
//    jint commandLineArgsCount;
//    jint logLevel;
//    jlong maxHeapSize;
//    jlong initialHeapSize;
//    jboolean enableGCHeapStats;
//    jboolean enableHooks;
    jboolean waitForResume;
//    jboolean printPID;
//    char* pidFile;
    jboolean printDebugPort;
    char* debugPortFile;
//    char resourcesPath[PATH_MAX];
//    char imagePath[PATH_MAX];
//    char** rawBootclasspath;
//    char** rawClasspath;
//    SystemProperty* properties;
//    ClasspathEntry* bootclasspath;
//    ClasspathEntry* classpath;
//    char** staticLibs;
//    void* runtimeData;
//    Class* (*loadBootClass)(Env*, const char*, Object*);
//    Class* (*loadUserClass)(Env*, const char*, Object*);
//    void (*classInitialized)(Env*, Class*);
//    Interface* (*loadInterfaces)(Env*, Class*);
//    Field* (*loadFields)(Env*, Class*);
//    Method* (*loadMethods)(Env*, Class*);
//    Class* (*findClassAt)(Env*, void*);
//    jboolean (*exceptionMatch)(Env*, TrycatchContext*);
//    ObjectArray* (*listBootClasses)(Env*, Class*);
//    ObjectArray* (*listUserClasses)(Env*, Class*);
};

typedef const struct JNIInvokeInterface_ *JavaVM;


struct VM {
    JavaVM javaVM;
    Options* options;
    jboolean initialized;
};

struct Env {
//    JNIEnv jni;
    VM* vm;
//    Object* throwable;
    Thread* currentThread;
    void* reserved0; // Used internally
    void* reserved1; // Used internally
//    GatewayFrame* gatewayFrames;
//    TrycatchContext* trycatchContext;
    jint attachCount;
};

struct Thread {
    jint threadId;
    Env* env;
    Object* threadObj;
//    struct Thread* waitNext;
//    struct Thread* prev;
//    struct Thread* next;
//    Monitor* waitMonitor;
//    pthread_t pThread;
//    void* stackAddr;
//    jboolean interrupted;
//    Mutex waitMutex;
//    jint status;
//    pthread_cond_t waitCond;
//    sigset_t signalMask;
};

struct CallStackFrame {
    void* pc;
    void* fp;
    Method* method;
    jint lineNumber;
};

struct CallStack {
    jint length;
    CallStackFrame frames[0];
};

struct JNIInvokeInterface_ {
//    void* reserved0;
//    void* reserved1;
//    void* reserved2;

//    jint (JNICALL *DestroyJavaVM)(JavaVM*);
//
//    jint (JNICALL *AttachCurrentThread)(JavaVM*, void** penv, void* args);
//    jint (JNICALL *DetachCurrentThread)(JavaVM*);
//
//    jint (JNICALL *GetEnv)(JavaVM*, void** penv, jint ver);
//
//    jint (JNICALL *AttachCurrentThreadAsDaemon)(JavaVM*, void** penv, void* args);
};




struct Object {
    Class* clazz;
//#if defined(RVM_X86_64) || defined(RVM_ARM64)
//    uint64_t lock;
//#else
//    uint32_t lock;
//#endif
};


struct Class {
//    Object object;
//    void* _data;             // Reserve the memory needed to store the instance fields for java.lang.Class.
//    // java.lang.Class has a single field, (SoftReference<ClassCache<T>> cacheRef).
//    // void* gives enough space to store that reference.
//    void* gcDescriptor;      // Descriptor used by the GC to find pointers in instances of this class.
//    // NOTE: If the offset of gcDescriptor changes the EXTGC_MARK_DESCR_OFFSET value in the
//    // root CMakeLists.txt MUST also be modified.
//    TypeInfo* typeInfo;      // Info on all types this class implements.
//    VITable* vitable;
//    ITables* itables;
    const char* name;        // The name in modified UTF-8.
//    Object* classLoader;
//    Class* superclass;       // Superclass pointer. Only java.lang.Object, primitive classes and interfaces have NULL here.
//    Class* componentType;
//    void* initializer;       // Points to the <clinit> method implementation of the class. NULL if there is no <clinit>.
//    jint flags;              // Low 16-bits are access flags. High 16-bits are RoboVM specific flags defined in class.h.
//    Thread* initThread;      // The Thread which is currently initializing this class.
//    Interface* _interfaces;  // Lazily loaded linked list of interfaces. Use rvmGetInterfaces() to get this value.
//    Field* _fields;          // Lazily loaded linked list of fields. Use rvmGetFields() to get this value.
//    Method* _methods;        // Lazily loaded linked list of methods. Use rvmGetMethods() to get this value.
//    void* attributes;
//    jint classDataSize;
//    jint instanceDataOffset; // The offset from the base of the Object where the instance fields of this class can be found.
//    jint instanceDataSize;   // The total number of bytes needed to store instances of this class.
//    unsigned short classRefCount;
//    unsigned short instanceRefCount;
//    void* data[0] __attribute__ ((aligned (8)));  // This is where static fields are stored for the class. Must be 8-byte aligned.
};

struct Method {
//    Method* next;
    Class* clazz;
    const char* name;
    const char* desc;
//    jint vitableIndex;
//    jint access;
//    jint size;
//    void* attributes;
    void* impl;
//    void* synchronizedImpl;
//    void* linetable;
};

typedef struct DebugGcRoot {
    Object* root;
    struct DebugGcRoot* next;
} DebugGcRoot;

typedef struct {
    Env env;
    void* pclow;
    void* pchigh;
    void* pclow2;
    void* pchigh2;
    Mutex suspendMutex;
    pthread_cond_t suspendCond;
    jboolean suspended;
    jboolean stepping;
    jboolean ignoreExceptions;

    // used to ignore instrumented bp/stepping
    jboolean ignoreInstrumented;

    // used for invoking methods/creating new
    // instances on a thread
    jbyte command;
    jlong reqId;

    // used for method invocation and new instance
    void* classOrObjectPtr;
    char* methodName;
    char* descriptor;
    jboolean isClassMethod;
    jbyte returnType;
    jvalue* arguments;

    // used for new string
    char* string;
    jint stringLength;

    // used for new array
    jint arrayLength;
    char* elementName;
    jint elementNameLength;

    // used to keep track of GC roots
    // created when instantiating objects
    // or invoking methods. All items
    // are unrooted when a thread is
    // resumed
    DebugGcRoot* gcRoot;
} DebugEnv;



jboolean _rvmHookHandshake(Options* options);
jboolean _rvmHookSetupTCPChannel(Options* options);

#endif //TCVM_RVMDEBUG_H
