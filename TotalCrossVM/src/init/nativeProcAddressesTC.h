#if defined darwin || defined ANDROID

#ifdef ANDROID
#define FILL_LB fillNativeProcAddressesLB();
#else
#define FILL_LB 
#endif

#define initNativeProcAddresses() do{ htNativeProcAddresses = htNew(512, null); fillNativeProcAddressesTC(); FILL_LB }while(0)
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
