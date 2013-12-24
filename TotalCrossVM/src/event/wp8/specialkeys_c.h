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



int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
	switch (key)
	{
	case VK_PRIOR: return SK_PAGE_UP;
	case VK_NEXT: return SK_PAGE_DOWN;
	case VK_HOME: return SK_HOME;
	case VK_END: return SK_END;
	case VK_UP: return SK_UP;
	case VK_DOWN: return SK_DOWN;
	case VK_LEFT: return SK_LEFT;
	case VK_RIGHT: return SK_RIGHT;
	case VK_INSERT: return SK_INSERT;
	case VK_RETURN: return SK_ENTER;
	case VK_TAB: return SK_TAB;
	case VK_BACK: return SK_BACKSPACE;
	case VK_ESCAPE: return SK_ESCAPE;
	case VK_DELETE: return SK_DELETE;
#ifndef WINCE  // other emulation keys
	case VK_F6: return SK_MENU;
	case VK_F7: return SK_CALC;
	case VK_F8: return SK_FIND;
	case VK_F10: return SK_HOME;
	case VK_F11: return SK_KEYBOARD_ABC;
	case VK_F9: return SK_SCREEN_CHANGE;
#else
	case VK_F1: return isWindowsMobile ? SK_MENU : SK_F1;   // guich@tc114_86
	case VK_F2: return isWindowsMobile ? SK_ESCAPE : SK_F2; // guich@tc114_86
	case VK_F3: return SK_F3; // guich@tc126_73: all SK_Fn keys
	case VK_F4: return SK_F4;
	case VK_F5: return SK_F5;
	case VK_F6: return SK_F6;
	case VK_F7: return SK_F7;
	case VK_F8: return SK_F8;
	case VK_F9: return SK_F9;
	case VK_F10: return SK_F10;
	case VK_F11: return SK_F11;
	case VK_F12: return SK_F12;
	case VK_F13: return SK_F13;
	case VK_F14: return SK_F14;
	case VK_F15: return SK_F15;
	case VK_F16: return SK_F16;
	case VK_F17: return SK_F17;
	case VK_F18: return SK_F18;
	case VK_F19: return SK_F19;
	case VK_F20: return SK_F20;
#endif
	}
	return key;
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   return PM_NONE;
}