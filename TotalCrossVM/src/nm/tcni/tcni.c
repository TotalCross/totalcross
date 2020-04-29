#include "tcni.h"
#include "tcvm.h"
#include <ffi.h>
#include <dlfcn.h>
#include "../lang/Runtime.h"

ffi_type getffiType(Context context, TCObject * arg) {
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
    
    return ffi_type_void;
}



TC_API void tnTCNI_invokeMethod_ssO (NMParams p) {

    // for static calls args begin from 0
    char * module = String2CharP(p->obj[0]);
    char * method = String2CharP(p->obj[1]);
    TCObject * tcArgs = ARRAYOBJ_START(p->obj[2]);
    int argArrLen = ARRAYLEN(tcArgs);
    ffi_type **argTypes;
    argTypes = xmalloc((argArrLen)* sizeof(ffi_type *));
    void **args;
    args = xmalloc((argArrLen + 1) * sizeof(void *));
    
    for (size_t i = 0; i < argArrLen; i++)
    {
        TCObject o = tcArgs[i];
        ffi_type argType = getffiType(p->currentContext, o);
        
        if(argType.type == ffi_type_pointer.type) {
            argTypes[i] = &ffi_type_pointer;
            char *strArg = String2CharP(o);
            args[i] = &strArg;
            printf("\n");
        }
        if(argType.type == ffi_type_float.type) {
            argTypes[i] = &ffi_type_float;
            float *fValue = xmalloc(sizeof(float));
            (*fValue) = Float_v(o); 
            args[i] = fValue;
        }
        if(argType.type == ffi_type_double.type) {
            argTypes[i] = &ffi_type_double;
            double *dValue = xmalloc(sizeof(double));
            (*dValue) = Double_v(o);
            args[i] = dValue;

        }
        if(argType.type == ffi_type_sint.type) {
            argTypes[i] = &ffi_type_sint;
            int *iValue = xmalloc(sizeof(int));
            (*iValue) = Integer_v(o);
            args[i] = iValue;
        }
    }
    
    void* add_data_fn = dlsym(handle, method);
    char* err = dlerror();
    if (err) {
        fprintf(stderr, "dlsym failed: %s\n", err);
        exit(1);
    }

    // Describe the interface of add_data to libffi.
    ffi_cif cif;
    ffi_status status = ffi_prep_cif(&cif, FFI_DEFAULT_ABI, argArrLen, &ffi_type_void,
                                     argTypes);
    if (status != FFI_OK) {
        fprintf(stderr, "ffi_prep_cif failed: %d\n", status);
        exit(1);
    }

    ffi_call(&cif, FFI_FN(add_data_fn), NULL, args);
    return 0;
}