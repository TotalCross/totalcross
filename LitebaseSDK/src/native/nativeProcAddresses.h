#if defined (darwin) && !defined (THEOS)
void fillNativeProcAddresses();
#else
#define fillNativeProcAddresses()
#endif
