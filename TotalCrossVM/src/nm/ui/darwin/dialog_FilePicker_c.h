/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif

  char* iphone_documentPickerStart(int w, int h, int t, char** fileName);

#ifdef __cplusplus
};
#endif
#endif // darwin


static void nativePickFile(NMParams p) {
#ifdef darwin    
  char* ret = NULL;

  iphone_documentPickerStart(0, 0, 0, &ret);
  if (ret != NULL) {
    TCObject s = createStringObjectFromCharP(p->currentContext, ret, -1);
    if (s != null) {
      TCObject o = createObjectWithoutCallingDefaultConstructor(p->currentContext, "totalcross.net.URI");
      if (o != null) {
        Method m = getMethod(OBJ_CLASS(o), false, CONSTRUCTOR_NAME, 1, "java.lang.String");
        if (m != null) {
          executeMethod(p->currentContext, m, o, s);
          p->retO = o;
        }
        setObjectLock(o, UNLOCKED);
      }
      setObjectLock(s, UNLOCKED);
    }
    xfree(ret);
  }
#endif
}
