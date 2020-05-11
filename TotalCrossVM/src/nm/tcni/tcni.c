#include "tcni.h"
#include "tcvm.h"
#include <ffi.h>
#include <dlfcn.h>

ffi_type getffiType(Context context, TCObject * arg) {
    if(arg == NULL) return ffi_type_pointer;
    TCClass c = (TCClass)OBJ_CLASS(arg);

    if(areClassesCompatible (context, c, "java.lang.String") == COMPATIBLE) {
        return ffi_type_pointer;
    }
    if(areClassesCompatible (context, c, "java.lang.Integer") == COMPATIBLE) {
        return ffi_type_sint32;
    }
    if(areClassesCompatible (context, c, "java.lang.Double") == COMPATIBLE) {
        return ffi_type_double;
    }
    if(areClassesCompatible (context, c, "java.lang.Float") == COMPATIBLE) { 
        return ffi_type_float;
    }

    if(areClassesCompatible (context, c, "java.lang.Long") == COMPATIBLE) { 
        return ffi_type_float;
    }
    
    return ffi_type_void;
}



TC_API void tnTCNI_invokeMethod_sscO (NMParams p) {
    
    if(p->obj[0] == NULL) {
        throwNullArgumentException(p->currentContext, "module");
        return;
    }

    if(p->obj[1] == NULL) {
        throwNullArgumentException(p->currentContext, "method");
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
    char* className = p->obj[2] == NULL ? NULL : String2CharPHeap(p->obj[2], heap);
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

    void* add_data_fn = dlsym(handle, method);
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
    if(p->obj[3] && ARRAYOBJ_LEN(p->obj[3]) > 1) { // if there is some arg 

        TCObject *tcArgs = ARRAYOBJ_START(p->obj[3]);
        argArrLen = ARRAYLEN(tcArgs);
        argTypes = heapAlloc(heap, (argArrLen)* sizeof(ffi_type *));
        args = heapAlloc(heap, (argArrLen + 1) * sizeof(void *));

        for (int32 i = 0; i < argArrLen; i++)
        {
            TCObject o = tcArgs[i];
            ffi_type argType = getffiType(p->currentContext, o);
            if(argType.type == ffi_type_pointer.type) {
                argTypes[i] = &ffi_type_pointer;
                char *strArg = o == NULL ? NULL : String2CharPHeap(o, heap);
                args[i] = &strArg;
            }
            else if(argType.type == ffi_type_float.type) {
                argTypes[i] = &ffi_type_float;
                float *fValue = heapAlloc(heap, sizeof(float));
                (*fValue) = Float_v(o); 
                args[i] = fValue;
            }
            else if(argType.type == ffi_type_double.type) {
                argTypes[i] = &ffi_type_double;
                double *dValue = heapAlloc(heap, sizeof(double));
                (*dValue) = Double_v(o);
                args[i] = dValue;

            }
            else if(argType.type == ffi_type_sint.type) {
                argTypes[i] = &ffi_type_sint;
                int *iValue = heapAlloc(heap, sizeof(int));
                (*iValue) = Integer_v(o);
                args[i] = iValue;
            }
            else {
                throwException(p->currentContext, RuntimeException, "Invalid type. Accepted types are: String, int, double and float.");
                goto cleanup;
            }
        }
    }
    
    // Describe the interface of add_data to libffi.
    if(className == NULL) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_void, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        ffi_call(&cif, FFI_FN(add_data_fn), NULL, args);
    }
    else if(strEq(className, "java/lang/Integer")) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_sint, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        ret = createObject(p->currentContext, "java.lang.Integer");
        ffi_call(&cif, FFI_FN(add_data_fn), &Integer_v(ret), args);
    }
    else if(strEq(className, "java/lang/Double")) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_double, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        ret = createObject(p->currentContext, "java.lang.Double");
        ffi_call(&cif, FFI_FN(add_data_fn), &Double_v(ret), args);
    }
    else if(strEq(className, "java/lang/String")) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_pointer, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        char* strRet; 
        ffi_call(&cif, FFI_FN(add_data_fn), &strRet, args);
        int32 len = xstrlen(strRet);
        ret = createStringObjectFromCharP(p->currentContext, strRet, len);
        free(strRet);
    }
    else if(strEq(className, "java/lang/Float")) {
        ffi_cif cif;
        ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_float, argTypes);
        if (status != FFI_OK) {
            throwException(p->currentContext, RuntimeException, "failed to prepare cif from libffi");
            goto cleanup;
        }
        float* f = heapAlloc(heap, sizeof(float));
        ret = createObject(p->currentContext, "java.lang.Float");
        ffi_call(&cif, FFI_FN(add_data_fn), f, args);
        Float_v(ret) = *f;
    }
    else {
        throwException(
            p->currentContext, 
            RuntimeException, 
            "Invalid type for return. Valid types are: String, Integer, Double, Float and null"
            );
    }

    cleanup:
        heapDestroy(heap);
        if (ret != NULL) {
            setObjectLock(p->retO = ret, UNLOCKED);
        }
}