#if (defined (darwin) && !defined (THEOS))
#define initNativeProcAddresses() do{ htNativeProcAddresses = htNew(512, null); fillNativeProcAddressesTC(); }while(0)
#define destroyNativeProcAddresses() htFree(&htNativeProcAddresses, null)

#ifdef __cplusplus
extern "C" {
#endif

void fillNativeProcAddressesTC();

#ifdef __cplusplus
}
#endif
#else
#define initNativeProcAddresses()
#define destroyNativeProcAddresses()
#endif
