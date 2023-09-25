// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifdef __cplusplus
extern "C" {
#endif

void windowSetSIP(Context currentContext, int32 sipOption, TCObject control, bool secret);
bool windowGetSIP();
void windowGetSafeAreaInsets(int32* top, int32* left, int32* bottom, int32* right);

/*static void windowSetDeviceTitle(TCObject titleObj)
{
   UNUSED(titleObj)
}*/

#ifdef __cplusplus
}
#endif
