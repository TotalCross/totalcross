// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifdef __cplusplus
extern "C" {
#endif

void windowSetSIP(Context currentContext, int32 sipOption, TCObject control, bool secret);
bool windowGetSIP();

/*static void windowSetDeviceTitle(TCObject titleObj)
{
   UNUSED(titleObj)
}*/

#ifdef __cplusplus
}
#endif
