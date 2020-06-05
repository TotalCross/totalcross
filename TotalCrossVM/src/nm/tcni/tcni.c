#include "tcni.h"
#include "tcvm.h"

#if defined(linux) && !defined(darwin)

#include "tcni_types.h"
#include <ffi.h>
#include <dlfcn.h>

#endif

Hashtable validTypes = {0};

TC_API void tnTCNI_invokeMethod_sscO (NMParams p) {
#if defined(linux) && !defined(darwin)
    init(&validTypes);
    if(p->obj[0] == NULL) {
        throwNullArgumentException(p->currentContext, "module");
        return;
    }

    if(p->obj[1] == NULL) {
        throwNullArgumentException(p->currentContext, "method");
        return;
    }

    if(p->obj[2] == NULL) {
        throwNullArgumentException(p->currentContext, "rClass");
        return;
    }
    
    TCObject ret = NULL;
    Heap heap = heapCreate();

    IF_HEAP_ERROR(heap)
    {
        throwException(p->currentContext, OutOfMemoryError, NULL);
//        goto cleanup;
    }

    // for static calls args begin from 0
    char* module = String2CharPHeap(p->obj[0], heap);
    char* method = String2CharPHeap(p->obj[1], heap);
    char * className = getTargetJavaClass(p->obj[2])->name;

    TCNIType *tc_type = (TCNIType*)htGetPtr(&validTypes, hashCode(className));
    if(!tc_type) {
        throwException(
            p->currentContext, 
            RuntimeException, 
            "Invalid type for return."
            );
        goto cleanup;
    }
    void* handle = NULL;
    ffi_type **argTypes = NULL;
    void **args = NULL;
    
    handle = htGetPtr(&htLoadedLibraries, hashCode(module));
    
    if(!handle) {
        char errorMessage[PATH_MAX];
        xstrprintf(
            errorMessage,
            "Could not find lib%s.so in loaded libraries. Try to use Runtime.getRuntime().loadLibrary(\"libname\") before.",
            module
            );
        throwException(p->currentContext, RuntimeException, errorMessage);
        goto cleanup;
    }

    void* fn = dlsym(handle, method);
    char* err = dlerror();
    if (err) {
        char errorMessage[PATH_MAX];
        xstrprintf(
            errorMessage,
            "Could not find symbol %s in lib%s.so",
            method, module
        );
        throwException(p->currentContext, RuntimeException, errorMessage);
        goto cleanup;
    }

    int32 argArrLen = 0;
    if(tc_type->is_array || (p->obj[3] && ARRAYOBJ_LEN(p->obj[3]) > 1)) { // if there is some arg 
        TCObject *tcArgs = NULL;
        if(p->obj[3] && ARRAYOBJ_LEN(p->obj[3]) > 1) {
            tcArgs = ARRAYOBJ_START(p->obj[3]);
        }
        int32 i = 0;
        if(tcArgs) {
            argArrLen = ARRAYLEN(tcArgs);
        }
        if(tc_type->is_array)  { // increase args size and reserves first  argument to the array length
            argArrLen++;
            i++;
        }
        argTypes = heapAlloc(heap, (argArrLen)* sizeof(ffi_type *));
        args = heapAlloc(heap, (argArrLen + 1) * sizeof(void *));
        
        for (; i < argArrLen; i++)
        {
            TCObject o = tcArgs[i];
            args[i] = NULL;
            if(o != NULL) {
                TCNIType *arg_tc_type = htGetPtr(&validTypes, hashCode(OBJ_CLASS(o)->name));
                if(arg_tc_type == NULL) {
                    throwException(p->currentContext, RuntimeException, "Invalid type. Accepted types are: String, int, double and float.");
                    goto cleanup;
                }
                argTypes[i] = arg_tc_type->f_type;
                // if arg tc type is java.lang.String 
                int size  = arg_tc_type->size == -1 ? ((String_charsLen(o) + 1) * sizeof(char)) : arg_tc_type->size;
                void *value = heapAlloc(heap, size);
                (*arg_tc_type->convert_from_tc)(o, value);
                args[i] = value;
            }
            
        }
    }

    if(tc_type) { // compatible type
        int size = 0;
        int *size_p = &size;
        bool is_f_type_pointer = tc_type->f_type->type == ffi_type_pointer.type;
        if(tc_type->is_array) {
            argTypes[0] = &ffi_type_pointer;
            args[0] = &size_p;
        }
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, tc_type->f_type, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        void* r_value = tc_type->is_void || is_f_type_pointer ? NULL 
            : heapAlloc(heap, tc_type->size);
        ffi_call(&cif, FFI_FN(fn), is_f_type_pointer ? &r_value : r_value, args);
        
        if(tc_type->is_array && size != 0)  {
            int len = size/tc_type->size;
            ret = convert_to_tc_array (p->currentContext, r_value, tc_type->name, size, len);     
        }
        else if(!tc_type->is_void) {
            ret = (*tc_type->convert_to_tc)(p->currentContext, r_value);
        }
    }
    else {
        throwException(p->currentContext, RuntimeException, "Imcompatible return type");
    }
    cleanup:
        heapDestroy(heap);
        if (ret != NULL) {
            setObjectLock(p->retO = ret, UNLOCKED);
        }
#else
    throwExceptionNamed(p->currentContext, "java.lang.UnsupportedOperationException", "this method only works on Linux");
#endif
}

