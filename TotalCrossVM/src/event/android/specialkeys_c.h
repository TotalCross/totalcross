// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#define KEY_BACKSPACE       67
#define KEY_MENU            82
#define KEY_BACK             4
#define KEY_FIND            84
#define KEY_VOL_UP          24
#define KEY_VOL_DOWN        25
#define KEY_ALT_LEFT        57
#define KEY_ALT_RIGHT       58
#define KEY_SHIFT_LEFT      59
#define KEY_SHIFT_RIGHT     60
#define KEY_CAMERA          80  
#define KEY_CALL            5
#define KEY_TRACKBALL_UP    19
#define KEY_TRACKBALL_DOWN  20
#define KEY_TRACKBALL_LEFT  21
#define KEY_TRACKBALL_RIGHT 22
#define KEY_TRACKBALL_PRESS 23   
#define KEY_ENTER           66

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_BACKSPACE    : return KEY_BACKSPACE;
      case SK_ESCAPE       : return KEY_BACK;
      case SK_MENU         : return KEY_MENU;
      case SK_FIND         : return KEY_FIND;
      case SK_HARD1        : return KEY_CAMERA;
      case SK_HARD2        : return KEY_CALL;
      case SK_UP           : return KEY_TRACKBALL_UP;   
      case SK_DOWN         : return KEY_TRACKBALL_DOWN; 
      case SK_LEFT         : return KEY_TRACKBALL_LEFT;
      case SK_RIGHT        : return KEY_TRACKBALL_RIGHT;
      case SK_ACTION       : return KEY_TRACKBALL_PRESS;
      case SK_ENTER        : return KEY_ENTER;
   }
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   switch (key)
   {
      case KEY_BACKSPACE       : return SK_BACKSPACE;
      case KEY_BACK            : return SK_ESCAPE;
      case KEY_MENU            : return SK_MENU;
      case KEY_FIND            : return SK_FIND;
      case KEY_VOL_UP          : return SK_UP;
      case KEY_VOL_DOWN        : return SK_DOWN;
      case KEY_CAMERA          : return SK_HARD1;
      case KEY_CALL            : return SK_HARD2;
      case KEY_TRACKBALL_UP    : return SK_UP;
      case KEY_TRACKBALL_DOWN  : return SK_DOWN;
      case KEY_TRACKBALL_LEFT  : return SK_LEFT;
      case KEY_TRACKBALL_RIGHT : return SK_RIGHT;
      case KEY_TRACKBALL_PRESS : return SK_ACTION;
      case KEY_ENTER           : return SK_ENTER;
   }
   return key;
}

bool isEssentialKey(int32 portableKey) // android-only: return keys that are essential (must be intercepted)
{                                  
   switch (portableKey)
   {
      case SK_MENU      :
      case SK_BACKSPACE : 
      case SK_ESCAPE    : 
      case SK_UP        : 
      case SK_DOWN      : 
      case SK_LEFT      : 
      case SK_RIGHT     : 
      case SK_ACTION    : 
      case SK_ENTER     : return true;
   }
   return false;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{                                         
   return ((mods & 1) ? PM_SHIFT   : PM_NONE) |
          ((mods & 4) ? PM_CONTROL : PM_NONE) |
          ((mods & 2) ? PM_ALT     : PM_NONE) |
          ((mods & 8) ? PM_SYSTEM  : PM_NONE) ;
}
