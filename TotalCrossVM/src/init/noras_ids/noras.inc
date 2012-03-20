#if NORASID == 20
   #include "../init/noras_ids/noras_20.inc"
#elif NORASID == 274
   #include "../init/noras_ids/noras_274.inc"
#elif NORASID == 338
   #include "../init/noras_ids/noras_338.inc"
#else
   #error "Must define a valid NORASID for DISABLE_RAS"
#endif
