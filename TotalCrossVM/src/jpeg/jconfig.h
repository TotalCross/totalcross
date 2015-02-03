/* jconfig.mc6 --- jconfig.h for Microsoft C on MS-DOS, version 6.00A & up. */
/* see jconfig.doc for explanations */

#if defined TOTALCROSS
#define NO_GETENV
#endif

#define HAVE_PROTOTYPES
#define HAVE_UNSIGNED_CHAR
#define HAVE_UNSIGNED_SHORT
/* #define void char */
/* #define const */
#undef CHAR_IS_UNSIGNED
#ifndef HAVE_STDDEF_H
 #define HAVE_STDDEF_H
#endif
#ifndef HAVE_STDLIB_H
 #define HAVE_STDLIB_H
#endif
#undef NEED_BSD_STRINGS
#undef NEED_SYS_TYPES_H
#undef NEED_FAR_POINTERS        /* for small or medium memory model */
#undef NEED_SHORT_EXTERNAL_NAMES
#undef INCOMPLETE_TYPES_BROKEN
#define JDCT_DEFAULT JDCT_IFAST

#ifdef JPEG_INTERNALS
#undef RIGHT_SHIFT_IS_UNSIGNED
#undef USE_MSDOS_MEMMGR         /* Define this if you use jmemdos.c */
#if defined TOTALCROSS
#define MAX_ALLOC_CHUNK 64000   /* Maximum request to malloc() */
#else
#define MAX_ALLOC_CHUNK 1000000000L   /* Maximum request to malloc() */
#endif
#undef USE_FMEM                 /* Microsoft has _fmemcpy() and _fmemset() */
#undef NEED_FHEAPMIN            /* far heap management routines are broken */
#endif /* JPEG_INTERNALS */

#ifdef JPEG_CJPEG_DJPEG
#define BMP_SUPPORTED           /* BMP image file format */
#undef  GIF_SUPPORTED           /* GIF image file format */
// #define PPM_SUPPORTED           /* PBMPLUS PPM/PGM image file format */
#undef RLE_SUPPORTED            /* Utah RLE image file format */
// #define TARGA_SUPPORTED         /* Targa image file format */
#define TWO_FILE_COMMANDLINE
#undef USE_SETMODE              /* Microsoft has setmode() */
#undef NEED_SIGNAL_CATCHER      /* Define this if you use jmemdos.c */
#undef DONT_USE_B_MODE
#undef PROGRESS_REPORT          /* optional */
#endif /* JPEG_CJPEG_DJPEG */
