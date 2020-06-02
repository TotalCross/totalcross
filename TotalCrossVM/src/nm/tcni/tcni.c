#include "tcni.h"
#include "tcvm.h"

#ifdef HEADLESS

#include "tcni_types.h"
#include <ffi.h>
#include <dlfcn.h>

#endif

Hashtable validTypes = {0};


TCObject callArrayReturnFunc (char *className, int argsLen, void **args, ffi_type **argTypes, void * func, NMParams p) {
    TCObject ret = NULL;
    ffi_cif cif;
    // set array size as first argument
    argTypes[0] = &ffi_type_pointer;
    int size = 0;
    int *size_p = &size;
    args[0] = &size_p; 

    ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argsLen, &ffi_type_pointer, argTypes);
    if (status != FFI_OK) {
        throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
        return NULL;
    }
    // prepare to call
    void *pointer = NULL;
    ffi_call(&cif, FFI_FN(func), &pointer, args);

    if(size > 0 ) {
        TCNIType *type = htGetPtr(&validTypes, hashCode(className));
        int len = size/type->size; // number of elements of the java array
        ret = createArrayObject(p->currentContext, type->name, len);
        u_int8_t *aux = ARRAYOBJ_START(ret);
        xmemmove(aux, pointer, size);
    }
    return ret;

}

TC_API void tnTCNI_invokeMethod_sscO (NMParams p) {
#ifdef linux
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
        goto cleanup;
    }

    // for static calls args begin from 0
    char* module = String2CharPHeap(p->obj[0], heap);
    char* method = String2CharPHeap(p->obj[1], heap);
    printf("method: %s\n", method);
    
    char * className = getTargetClass(p->obj[2])->name;
    printf("class name: %s\n", className);
    TCNIType *tc_type = (TCNIType*)htGetPtr(&validTypes, hashCode(className));
    if(!tc_type) {
        throwException(
            p->currentContext, 
            RuntimeException, 
            "Invalid type for return."
            );
    }
    bool isReturnTypeArr = className[0] == '['; // arrays class name starts with [
    bool isReturnTypeVoid = strEq(className, "java.lang.Void");
    printf("is return type arr: %s\n", isReturnTypeArr ? "true" : "false");
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
    if(isReturnTypeArr || (p->obj[3] && ARRAYOBJ_LEN(p->obj[3]) > 1)) { // if there is some arg 
        TCObject *tcArgs = NULL;
        if(p->obj[3] && ARRAYOBJ_LEN(p->obj[3]) > 1) {
            tcArgs = ARRAYOBJ_START(p->obj[3]);
        }
        int32 i = 0;
        if(tcArgs) {
            argArrLen = ARRAYLEN(tcArgs);
        }
        if(isReturnTypeArr)  { // increase args size and reserves first  argument to the array length
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
                printf("arg tc type: %s\n", OBJ_CLASS(o)->name);
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
    printf("return type: %s\n", className);
    // Describe the interface of add_data to libffi.
    if(isReturnTypeVoid) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_void, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        ffi_call(&cif, FFI_FN(fn), NULL, args);
        goto cleanup;
    }
    if(isReturnTypeArr) { // return type array
        ret = callArrayReturnFunc(className, argArrLen, args, argTypes, fn, p);
    }
    else { // return type primitive
        printf("p return 1\n");
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, tc_type->f_type, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        printf("p return 2\n");
        bool isReturnTypeString = strEq(className, "java.lang.String");
        void* r_value = isReturnTypeVoid || isReturnTypeString ? NULL : heapAlloc(heap, tc_type->size);
        printf("p return 3\n");
        ffi_call(&cif, FFI_FN(fn), isReturnTypeString ? &r_value : r_value, args);
        printf("p return 4\n");
        
        if(!isReturnTypeVoid) 
            ret = (*tc_type->convert_to_tc)(p->currentContext, r_value);
        printf("p return 5\n");

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

