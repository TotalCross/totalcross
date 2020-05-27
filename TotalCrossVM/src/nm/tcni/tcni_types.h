#ifndef TCNI_TYPES_H
#define TCNI_TYPES_H

#include <ffi.h>

void init(Hashtable * ht);

typedef struct TCNIType
{
    char * name;
    int size;
    ffi_type * f_type;
    TCObject (*convert_to_tc)(Context, void*);
    void (*convert_from_tc)(TCObject, void*);   
} TCNIType;

#endif


