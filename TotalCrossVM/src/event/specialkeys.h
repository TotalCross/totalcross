// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef SPECIALKEYS_H
#define SPECIALKEYS_H

#ifdef __cplusplus
extern "C" {
#endif

/** There are three ranges of keys:

  k > 0 - standard ASCII chars
  -1 <= k < 1000000 - hotkey. In windows, a hotkey is mapped into the ASCII range, so we invert the value.
  k <= 1000000 - portable device keys

 **/


/// Device keys like the ones defined in Java
typedef enum
{
   SK_PAGE_UP        = -1000,
   SK_PAGE_DOWN      = -1001,
   SK_HOME           = -1002,
   SK_END            = -1003,
   SK_UP             = -1004,
   SK_DOWN           = -1005,
   SK_LEFT           = -1006,
   SK_RIGHT          = -1007,
   SK_INSERT         = -1008,
   SK_ENTER          = -1009,
   SK_TAB            = -1010,
   SK_BACKSPACE      = -1011,
   SK_ESCAPE         = -1012,
   SK_DELETE         = -1013,
   SK_MENU           = -1014,
   SK_COMMAND        = -1015,
   SK_KEYBOARD_ABC   = -1016,
   SK_KEYBOARD_123   = -1017,
   SK_KEYBOARD       = -1018,
   SK_HARD1          = -1019,
   SK_HARD2          = -1020,
   SK_HARD3          = -1021,
   SK_HARD4          = -1022,
   SK_CALC           = -1023,
   SK_FIND           = -1024,
   SK_LAUNCH         = -1025,
   SK_ACTION         = -1026,
   SK_CONTRAST       = -1027,
   SK_CLOCK          = -1028,
   SK_SYNC           = -1029,
   SK_SCREEN_CHANGE  = -1030,
   SK_POWER_ON       = -1031,
   SK_F1             = -1041,
   SK_F2             = -1042,
   SK_F3             = -1043,
   SK_F4             = -1044,
   SK_F5             = -1045,
   SK_F6             = -1046,
   SK_F7             = -1047,
   SK_F8             = -1048,
   SK_F9             = -1049,
   SK_F10            = -1050,
   SK_F11            = -1051,
   SK_F12            = -1052,
   SK_F13            = -1053,
   SK_F14            = -1054,
   SK_F15            = -1055,
   SK_F16            = -1056,
   SK_F17            = -1057,
   SK_F18            = -1058,
   SK_F19            = -1059,
   SK_F20            = -1060,
   SK_F21            = -1061,
   SK_F22            = -1062,
   SK_F23            = -1063,
   SK_F24            = -1064,
} PortableSpecialKeys;

typedef enum
{
   PM_NONE    = 0,
   PM_ALT     = 1,
   PM_CONTROL = 2,
   PM_SHIFT   = 4,
   PM_SYSTEM  = 8,
} PortableModifiers;

/// Returns the device key mapped from the portable key, or the portable key itself if it could not be mapped
int32 keyPortable2Device(PortableSpecialKeys key);
/// Returns the portable key mapped from the device key, or the device key itself if it could not be mapped
PortableSpecialKeys keyDevice2Portable(int32 key);
/// Returns the modifiers state (or'ed)
PortableModifiers keyGetPortableModifiers(int32 mods);

#ifdef __cplusplus
}
#endif

#endif
