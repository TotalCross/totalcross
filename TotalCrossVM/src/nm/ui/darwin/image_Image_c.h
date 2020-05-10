// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined (darwin)
 #ifdef __cplusplus
  extern "C" {
 #endif

 void resizeImageAtPath(char* src, char* dst, int maxPixelSize);

 #ifdef __cplusplus
  };
 #endif
#endif // darwin
