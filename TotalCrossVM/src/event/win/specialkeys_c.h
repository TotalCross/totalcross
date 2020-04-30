// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifdef WINCE // guich@400_72
 #define FF1  193
 #define FF2  194
 #define FF3  195
 #define FF4  196
 #define FF12 134
#else
 #define FF1  VK_F1
 #define FF2  VK_F2
 #define FF3  VK_F3
 #define FF4  VK_F4
 #define FF12 VK_F12
#endif

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_PAGE_UP  : return VK_PRIOR;
      case SK_PAGE_DOWN: return VK_NEXT;
      case SK_HOME     : return VK_HOME;
      case SK_END      : return VK_END;
      case SK_UP       : return VK_UP;
      case SK_DOWN     : return VK_DOWN;
      case SK_LEFT     : return VK_LEFT;
      case SK_RIGHT    : return VK_RIGHT;
      case SK_INSERT   : return VK_INSERT;
      case SK_ENTER    : return VK_RETURN;
      case SK_TAB      : return VK_TAB;
      case SK_BACKSPACE: return VK_BACK;
      case SK_ESCAPE   : return VK_ESCAPE;
      case SK_DELETE   : return VK_DELETE;
      case SK_HARD1    : return FF1;
      case SK_HARD2    : return FF2;
      case SK_HARD3    : return FF3;
      case SK_HARD4    : return FF4;
      case SK_ACTION   : return FF12;
#ifdef WINCE
      case SK_MENU     : return VK_F1; // guich@tc114_86
      case SK_F1       : return VK_F1; // guich@tc126_73: all SK_Fn keys
      case SK_F2       : return VK_F2; 
      case SK_F3       : return VK_F3; 
      case SK_F4       : return VK_F4; 
      case SK_F5       : return VK_F5; 
      case SK_F6       : return VK_F6; 
      case SK_F7       : return VK_F7; 
      case SK_F8       : return VK_F8; 
      case SK_F9       : return VK_F9; 
      case SK_F10      : return VK_F10;
      case SK_F11      : return VK_F11;
      case SK_F12      : return VK_F12;
      case SK_F13      : return VK_F13;
      case SK_F14      : return VK_F14;
      case SK_F15      : return VK_F15;
      case SK_F16      : return VK_F16;
      case SK_F17      : return VK_F17;
      case SK_F18      : return VK_F18;
      case SK_F19      : return VK_F19;
      case SK_F20      : return VK_F20;
#endif
   }
   return key < 0 ? -key : key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   switch (key)
   {
      case VK_PRIOR  : return SK_PAGE_UP;
      case VK_NEXT   : return SK_PAGE_DOWN;
      case VK_HOME   : return SK_HOME;
      case VK_END    : return SK_END;
      case VK_UP     : return SK_UP;
      case VK_DOWN   : return SK_DOWN;
      case VK_LEFT   : return SK_LEFT;
      case VK_RIGHT  : return SK_RIGHT;
      case VK_INSERT : return SK_INSERT;
      case VK_RETURN : return SK_ENTER;
      case VK_TAB    : return SK_TAB;
      case VK_BACK   : return SK_BACKSPACE;
      case VK_ESCAPE : return SK_ESCAPE;
      case VK_DELETE : return SK_DELETE;
      case FF1       : return SK_HARD1;
      case FF2       : return SK_HARD2;
      case FF3       : return SK_HARD3;
      case FF4       : return SK_HARD4;
      case FF12      : return SK_ACTION;
#ifndef WINCE  // other emulation keys
      case VK_F6     : return SK_MENU;
      case VK_F7     : return SK_CALC;
      case VK_F8     : return SK_FIND;
      case VK_F10    : return SK_HOME;
      case VK_F11    : return SK_KEYBOARD_ABC;
      case VK_F9     : return SK_SCREEN_CHANGE;
#else
      case VK_F1     : return isWindowsMobile ? SK_MENU : SK_F1;   // guich@tc114_86
      case VK_F2     : return isWindowsMobile ? SK_ESCAPE : SK_F2; // guich@tc114_86
      case VK_F3     : return SK_F3; // guich@tc126_73: all SK_Fn keys
      case VK_F4     : return SK_F4;
      case VK_F5     : return SK_F5;
      case VK_F6     : return SK_F6;
      case VK_F7     : return SK_F7;
      case VK_F8     : return SK_F8;
      case VK_F9     : return SK_F9;
      case VK_F10    : return SK_F10;
      case VK_F11    : return SK_F11;
      case VK_F12    : return SK_F12;
      case VK_F13    : return SK_F13;
      case VK_F14    : return SK_F14;
      case VK_F15    : return SK_F15;
      case VK_F16    : return SK_F16;
      case VK_F17    : return SK_F17;
      case VK_F18    : return SK_F18;
      case VK_F19    : return SK_F19;
      case VK_F20    : return SK_F20;
#endif
   }
   return key;
}

#define ISDOWN(x) ((GetAsyncKeyState(x) & 0x8000) != 0)

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   UNUSED(mods)
   return ((ISDOWN(VK_LSHIFT)   || ISDOWN(VK_RSHIFT))   ? PM_SHIFT   : PM_NONE) |
          ((ISDOWN(VK_LMENU)    || ISDOWN(VK_RMENU))    ? PM_ALT     : PM_NONE) |
          ((ISDOWN(VK_LCONTROL) || ISDOWN(VK_RCONTROL)) ? PM_CONTROL : PM_NONE);
}
