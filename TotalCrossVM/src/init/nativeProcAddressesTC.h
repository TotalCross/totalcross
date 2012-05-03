#if defined (darwin) && !defined (THEOS)
#define initNativeProcAddresses() do{ htNativeProcAddresses = htNew(512, null); fillNativeProcAddressesTC(); }while(0)
#define destroyNativeProcAddresses() htFree(&htNativeProcAddresses, null)
#else
#define initNativeProcAddresses()
#define destroyNativeProcAddresses()
#endif
