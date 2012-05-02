#if defined (darwin) && !defined (THEOS)
#define initNativeProcAddresses() do{ htNativeProcAddresses = htNew(512, null); fillNativeProcAddresses(); }while(0)
#define destroyNativeProcAddresses() htFree(&htNativeProcAddresses, null)
#else
#define initNativeProcAddresses()
#define destroyNativeProcAddresses()
#endif
