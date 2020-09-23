//
// Created by Panayotis on 15/9/20.
//

#ifndef TCVM_RVMDEBUG_TYPES_H
#define TCVM_RVMDEBUG_TYPES_H

#define JNI_FALSE  0
#define JNI_TRUE   1
#undef FALSE
#undef TRUE
#define FALSE JNI_FALSE
#define TRUE JNI_TRUE

typedef struct Options Options;
typedef struct Class Class;
typedef struct Object Object;
typedef struct Method Method;
typedef struct VM VM;
typedef struct Thread Thread;
typedef struct Env Env;
typedef struct CallStackFrame CallStackFrame;
typedef struct CallStack CallStack;
typedef pthread_mutex_t Mutex;

typedef enum LogLevel {
    LOG_LEVEL_TRACE = 2,
    LOG_LEVEL_DEBUG,
    LOG_LEVEL_INFO,
    LOG_LEVEL_WARN,
    LOG_LEVEL_ERROR,
    LOG_LEVEL_FATAL,
    LOG_LEVEL_SILENT,
} LogLevel;
#define LOG(level, text) rvmLog(level, LOG_TAG, text)
#define LOGF(level, format, ...) rvmLogf(level, LOG_TAG, format, __VA_ARGS__)
#define DEBUG(text) LOG(LOG_LEVEL_DEBUG, text)
#define DEBUGF(format, ...) LOGF(LOG_LEVEL_DEBUG, format, __VA_ARGS__)
#define ERROR(text) LOG(LOG_LEVEL_ERROR, text)
#define ERRORF(format, ...) LOGF(LOG_LEVEL_ERROR, format, __VA_ARGS__)

struct _jobject;
typedef struct _jobject*  jobject;
typedef jobject           jclass;
typedef jobject           jstring;
typedef jobject           jarray;
typedef jarray        jobjectArray;
typedef jarray        jbooleanArray;
typedef jarray        jbyteArray;
typedef jarray        jcharArray;
typedef jarray        jshortArray;
typedef jarray        jintArray;
typedef jarray        jlongArray;
typedef jarray        jfloatArray;
typedef jarray        jdoubleArray;
typedef jobject           jthrowable;
typedef jobject           jweak;

typedef unsigned char jboolean;
typedef signed char jbyte;
typedef unsigned short jchar;
typedef signed short jshort;
typedef signed int jint;
typedef float jfloat;
typedef double jdouble;
typedef jint jsize;
typedef signed long long jlong;
typedef union jvalue {
    jboolean    z;
    jbyte       b;
    jchar       c;
    jshort      s;
    jint        i;
    jlong       j;
    jfloat      f;
    jdouble     d;
    jobject     l;
} jvalue;

#endif //TCVM_RVMDEBUG_TYPES_H
