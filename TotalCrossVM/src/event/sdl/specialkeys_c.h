// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if __APPLE__
 #include "SDL.h"
#else
 #include "SDL2/SDL.h"
#endif

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_PAGE_UP       : return SDLK_PAGEUP;
      case SK_PAGE_DOWN     : return SDLK_PAGEDOWN;
      case SK_HOME          : return SDLK_HOME;
      case SK_END           : return SDLK_END;
      case SK_UP            : return SDLK_UP;
      case SK_DOWN          : return SDLK_DOWN;
      case SK_LEFT          : return SDLK_LEFT;
      case SK_RIGHT         : return SDLK_RIGHT;
      case SK_INSERT        : return SDLK_INSERT;
      case SK_ENTER         : return SDLK_RETURN;
      case SK_TAB           : return SDLK_TAB;
      case SK_BACKSPACE     : return SDLK_BACKSPACE;
      case SK_ESCAPE        : return SDLK_ESCAPE;
      case SK_DELETE        : return SDLK_DELETE;
      case SK_MENU          : return SDLK_F6;
      case SK_KEYBOARD_ABC  : return SDLK_F11;
      case SK_HARD1         : return SDLK_F1;
      case SK_HARD2         : return SDLK_F2;
      case SK_HARD3         : return SDLK_F3;
      case SK_HARD4         : return SDLK_F4;
      case SK_CALC          : return SDLK_F7;
      case SK_FIND          : return SDLK_F8;
      case SK_ACTION        : return SDLK_F12;
      case SK_SCREEN_CHANGE : return SDLK_F9;
      default: // avoid warning "enumeration value 'XXX' not handled in switch"
         break;
   }
   return key < 0 ? -key : key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   switch (key)
   {
      case SDLK_PAGEUP    : return SK_PAGE_UP;
      case SDLK_PAGEDOWN  : return SK_PAGE_DOWN;
      case SDLK_HOME      : return SK_HOME;
      case SDLK_END       : return SK_END;
      case SDLK_UP        : return SK_UP;
      case SDLK_DOWN      : return SK_DOWN;
      case SDLK_LEFT      : return SK_LEFT;
      case SDLK_RIGHT     : return SK_RIGHT;
      case SDLK_INSERT    : return SK_INSERT;
      case SDLK_RETURN    : return SK_ENTER;
      case SDLK_TAB       : return SK_TAB;
      case SDLK_BACKSPACE : return SK_BACKSPACE;
      case SDLK_ESCAPE    : return SK_ESCAPE;
      case SDLK_DELETE    : return SK_DELETE;
      case SDLK_F1        : return SK_HARD1;
      case SDLK_F2        : return SK_HARD2;
      case SDLK_F3        : return SK_HARD3;
      case SDLK_F4        : return SK_HARD4;
      case SDLK_F6        : return SK_MENU;
      case SDLK_F7        : return SK_CALC;
      case SDLK_F8        : return SK_FIND;
      case SDLK_F9        : return SK_SCREEN_CHANGE;
      case SDLK_F10       : return SK_HOME;
      case SDLK_F11       : return SK_KEYBOARD_ABC;
      case SDLK_F12       : return SK_ACTION;
   }
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   if (mods == -1)
      return PM_NONE;
   return (((mods & KMOD_LSHIFT) || (mods & KMOD_RSHIFT)) ? PM_SHIFT   : PM_NONE) |
          (((mods & KMOD_LCTRL) || (mods & KMOD_RCTRL)) ? PM_CONTROL : PM_NONE) |
          (((mods & KMOD_LALT) || (mods & KMOD_RALT)) ? PM_ALT     : PM_NONE);
}
