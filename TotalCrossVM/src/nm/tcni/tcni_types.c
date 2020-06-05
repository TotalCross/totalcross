#include "tcvm.h"
#include <ffi.h>


#define MK_CONVERT_TO_TC(type, Type)                                    \
static TCObject convert_to_tc_##type(Context context, void * v) {       \
    TCObject ret = createObject(context, "java.lang." #Type);           \
    Type##_v(ret) = *((type *)v);                                       \
    return ret;                                                         \
}

#define MK_CONVERT_FROM_TC(type, Type)                                  \
void convert_from_tc_##type (TCObject o, void * v) {                    \
    *((type *)v) = Type##_v(o);                                         \
}

typedef struct TCNIType
{
    char * name;
    int size;
    ffi_type * f_type;
    bool is_array;
    bool is_void;
    TCObject (*convert_to_tc)(Context, ...);
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
MK_CONVERT_TO_TC(int, Integer)
MK_CONVERT_TO_TC(double, Double)
MK_CONVERT_TO_TC(float, Float)
MK_CONVERT_TO_TC(long, Long)
MK_CONVERT_TO_TC(short, Short)
MK_CONVERT_TO_TC(byte, Byte)
MK_CONVERT_TO_TC(char, Character)
MK_CONVERT_TO_TC(bool, Boolean)

void convert_from_tc_string(TCObject o, void * v);
MK_CONVERT_FROM_TC(int, Integer)
MK_CONVERT_FROM_TC(double, Double)
MK_CONVERT_FROM_TC(float, Float)
MK_CONVERT_FROM_TC(long, Long)
MK_CONVERT_FROM_TC(short, Short)
MK_CONVERT_FROM_TC(byte, Byte)
MK_CONVERT_FROM_TC(char, Character)
MK_CONVERT_FROM_TC(bool, Boolean)

void init(Hashtable * ht) {
    if(ht->initialized == 0) {
        *ht = htNew(0xff, NULL);
    } else {
        return;
    }
    //array
    createElement(ht, "[B", 1, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[I", 4, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[S", 2, &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[L", sizeof(long int), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[D", sizeof(double), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[F", sizeof(float), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "[C", sizeof(char), &ffi_type_pointer, NULL, NULL);
    createElement(ht, "java.lang.String", -1, &ffi_type_pointer, &convert_to_tc_string, &convert_from_tc_string);
    createElement(ht, "java.lang.Void", 1, &ffi_type_void, NULL, NULL);
    createElement(ht, "java.lang.Integer", 8, &ffi_type_sint32, &convert_to_tc_int, &convert_from_tc_int);
    createElement(ht, "java.lang.Double", sizeof(double), &ffi_type_double, &convert_to_tc_double, &convert_from_tc_double);
    createElement(ht, "java.lang.Float", sizeof(float), &ffi_type_float, &convert_to_tc_float, &convert_from_tc_float);
    createElement(ht, "java.lang.Long", sizeof(long int), &ffi_type_sint64, &convert_to_tc_long, &convert_from_tc_long);
    createElement(ht, "java.lang.Short", sizeof(short), &ffi_type_sint16, &convert_to_tc_short, &convert_from_tc_short);
    createElement(ht, "java.lang.Byte", sizeof(byte), &ffi_type_sint8, &convert_to_tc_byte, &convert_from_tc_byte);
    createElement(ht, "java.lang.Character", sizeof(char), &ffi_type_schar, &convert_to_tc_char, &convert_from_tc_char);
    createElement(ht, "java.lang.Boolean", sizeof(char), &ffi_type_schar, &convert_to_tc_bool, &convert_from_tc_bool);
    
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
    element->is_array = typeName[0] == '[';
    element->is_void = strEq(typeName, "java.lang.Void");

    htPutPtr(ht, hashCode(typeName), element); 
}


static TCObject convert_to_tc_string(Context context, void * v) {
    int32 len = xstrlen(v);
    return createStringObjectFromCharP(context, v, len);
}

TCObject convert_to_tc_array(Context context, void * value, char * type_name, int size, int len) {
    
    TCObject ret = createArrayObject(context, type_name, len);
    u_int8_t *aux = ARRAYOBJ_START(ret);
    xmemmove(aux, value, size);
    return ret;
}

void convert_from_tc_string(TCObject o, void * v) {
    String2CharPBuf(o, (char *)v);
}

