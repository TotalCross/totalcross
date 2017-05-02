#if defined (darwin) || defined (ANDROID)
void fillNativeProcAddressesLB();
#else
#define fillNativeProcAddressesLB()
#endif
