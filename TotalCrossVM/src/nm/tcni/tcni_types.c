#include "tcvm.h"
#include <ffi.h>

#define CONVERT_TO_TC(type, Type)                                       \
static TCObject convert_to_tc_##type(Context context, void * v) {       \
    TCObject ret = createObject(context, "java.lang." #Type);           \
    Type##_v(ret) = *((type *)v);                                       \
    return ret;                                                         \
}

#define CONVERT_FROM_TC(type, Type)                                     \
void convert_from_tc_##type (TCObject o, void * v) {             \
    *((type *)v) = Type##_v(o);                                         \
}

typedef struct TCNIType
{
    char * name;
    int size;
    ffi_type * f_type;
    TCObject (*convert_to_tc)(Context, void*);
    void (*convert_from_tc)(TCObject, void*);    
} TCNIType;

void createElement(Hashtable *ht, 
    char *typeName, 
    int elementSize, 
    ffi_type * f_type, 
    TCObject (*convert_to_tc)(Context, void*),
    void (*convert_from_tc)(TCObject, void*)
    );

static TCObject convert_to_tc_string(Context context, void * v);
CONVERT_TO_TC(int, Integer)
CONVERT_TO_TC(double, Double)
CONVERT_TO_TC(float, Float)
CONVERT_TO_TC(long, Long)

void convert_from_tc_string(TCObject o, void * v);
CONVERT_FROM_TC(int, Integer)
CONVERT_FROM_TC(double, Double)
CONVERT_FROM_TC(float, Float)
CONVERT_FROM_TC(long, Long)

void init(Hashtable * ht) {
    if(ht->initialized == 0) {
        *ht = htNew(0xff, NULL);
    } else {
        return;
    }
    printf("its addin elements\n");
    //array
    createElement(ht, "[java.lang.Byte", 1, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[java.lang.Integer", 4, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[java.lang.Short", 2, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[java.lang.Long", sizeof(long int), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[java.lang.Double", sizeof(double), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[java.lang.Float", sizeof(float), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "java.lang.String", -1, &ffi_type_pointer, &convert_to_tc_string, &convert_from_tc_string);
    createElement(ht, "java.lang.Void", 1, &ffi_type_void, NULL, NULL);
    createElement(ht, "java.lang.Integer", 8, &ffi_type_sint32, &convert_to_tc_int, &convert_from_tc_int);
    createElement(ht, "java.lang.Double", sizeof(double), &ffi_type_double, &convert_to_tc_double, &convert_from_tc_double);
    createElement(ht, "java.lang.Float", sizeof(float), &ffi_type_float, &convert_to_tc_float, &convert_from_tc_float);
    createElement(ht, "java.lang.Long", sizeof(long int), &ffi_type_sint64, &convert_to_tc_long, &convert_from_tc_long);
}

void createElement(Hashtable *ht, 
    char *typeName, 
    int elementSize, 
    ffi_type * f_type, 
    TCObject (*convert_to_tc)(Context, void*),
    void (*convert_from_tc)(TCObject, void*)
    ) {

    TCNIType *element = (TCNIType*)xmalloc(sizeof(TCNIType));
    
    element->name = typeName;
    element->size = elementSize;
    element->f_type = f_type;
    element->convert_to_tc = convert_to_tc;
    element->convert_from_tc = convert_from_tc;
    htPutPtr(ht, hashCode(typeName), element); 
}


static TCObject convert_to_tc_string(Context context, void * v) {
    printf("hi, %s!\n", v);
    int32 len = xstrlen(v);
    printf("len: %d\n", len);
    return createStringObjectFromCharP(context, v, len);
}
void convert_from_tc_string(TCObject o, void * v) {
    String2CharPBuf(o, (char *)v);
}

