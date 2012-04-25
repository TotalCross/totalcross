#if defined (darwin) && !defined (THEOS)
void initNativeHT();
void destroyNativeHT();
#else
#define destroyNativeHT()
#define initNativeHT()
#endif
