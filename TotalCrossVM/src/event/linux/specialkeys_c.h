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



#include <directfb.h>

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_PAGE_UP        : return DIKS_PAGE_UP;
      case SK_PAGE_DOWN      : return DIKS_PAGE_DOWN;
      case SK_HOME           : return DIKS_HOME;
      case SK_END            : return DIKS_END;
      case SK_UP             : return DIKS_CURSOR_UP;
      case SK_DOWN           : return DIKS_CURSOR_DOWN;
      case SK_LEFT           : return DIKS_CURSOR_LEFT;
      case SK_RIGHT          : return DIKS_CURSOR_RIGHT;
      case SK_INSERT         : return DIKS_INSERT;
      case SK_ENTER          : return DIKS_RETURN;
      case SK_TAB            : return DIKS_TAB;
      case SK_BACKSPACE      : return DIKS_BACKSPACE;
      case SK_ESCAPE         : return DIKS_ESCAPE;
      case SK_DELETE         : return DIKS_DELETE;
      case SK_SCREEN_CHANGE  : return DIKS_F9;
      default: // avoid warning "enumeration value 'XXX' not handled in switch"
         break;
   }
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   switch (key)
   {
      case DIKS_PAGE_UP       : return SK_PAGE_UP;
      case DIKS_PAGE_DOWN     : return SK_PAGE_DOWN;
      case DIKS_HOME          : return SK_HOME;
      case DIKS_END           : return SK_END;
      case DIKS_CURSOR_UP     : return SK_UP;
      case DIKS_CURSOR_DOWN   : return SK_DOWN;
      case DIKS_CURSOR_LEFT   : return SK_LEFT;
      case DIKS_CURSOR_RIGHT  : return SK_RIGHT;
      case DIKS_INSERT        : return SK_INSERT;
      case DIKS_RETURN        : return SK_ENTER;
      case DIKS_TAB           : return SK_TAB;
      case DIKS_BACKSPACE     : return SK_BACKSPACE;
      case DIKS_ESCAPE        : return SK_ESCAPE;
      case DIKS_DELETE        : return SK_DELETE;
      case DIKS_F9            : return SK_SCREEN_CHANGE;
   }
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   if (mods == -1)
      return PM_NONE;
   return ((mods & (1 << DIMKI_SHIFT))   ? PM_SHIFT   : PM_NONE) |
          ((mods & (1 << DIMKI_CONTROL)) ? PM_CONTROL : PM_NONE) |
          ((mods & (1 << DIMKI_ALT))     ? PM_ALT     : PM_NONE) ;
}
