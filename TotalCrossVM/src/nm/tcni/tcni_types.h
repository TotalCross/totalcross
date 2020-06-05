#ifndef TCNI_TYPES_H
#define TCNI_TYPES_H

#include <ffi.h>
#include "tcvm.h"

void init(Hashtable * ht);

typedef struct TCNIType
{
    char * name;
    int size;
    ffi_type * f_type;
    bool is_array;
    bool is_void;
    TCObject (*convert_to_tc)(Context, void*);
    void (*convert_from_tc)(TCObject, void*);   
} TCNIType;

TCObject convert_to_tc_array(Context context, void * value, char * type_name, int size, int len);

#endif


