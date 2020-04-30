// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
	switch (key)
	{
   	case VK_RETURN: return SK_ENTER;
   	case VK_TAB: return SK_TAB;
   	case VK_BACK: return SK_BACKSPACE;
/*   	case VK_PRIOR: return SK_PAGE_UP;
   	case VK_NEXT: return SK_PAGE_DOWN;
   	case VK_HOME: return SK_HOME;
   	case VK_END: return SK_END;
   	case VK_UP: return SK_UP;
   	case VK_DOWN: return SK_DOWN;
   	case VK_LEFT: return SK_LEFT;
   	case VK_RIGHT: return SK_RIGHT;
   	case VK_INSERT: return SK_INSERT;
   	case VK_ESCAPE: return SK_ESCAPE;
   	case VK_DELETE: return SK_DELETE;*/
	}
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   return PM_NONE;
}