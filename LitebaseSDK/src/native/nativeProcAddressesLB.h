#if defined (darwin) && !defined (THEOS)
void fillNativeProcAddressesLB();
#else
#define fillNativeProcAddressesLB()
#endif
