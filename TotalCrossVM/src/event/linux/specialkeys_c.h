// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef HEADLESS
#include <directfb.h>
#else
#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
#endif

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
#ifndef HEADLESS
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
#else
   switch (key)
   {
      case SK_PAGE_UP        : return SDLK_PAGEUP   ;
      case SK_PAGE_DOWN      : return SDLK_PAGEDOWN ;
      case SK_HOME           : return SDLK_HOME     ;
      case SK_END            : return SDLK_END      ;
      case SK_UP             : return SDLK_UP       ;
      case SK_DOWN           : return SDLK_DOWN     ;
      case SK_LEFT           : return SDLK_LEFT     ;
      case SK_RIGHT          : return SDLK_RIGHT    ;
      case SK_INSERT         : return SDLK_INSERT   ;
      case SK_ENTER          : return SDLK_RETURN   ;
      case SK_TAB            : return SDLK_TAB      ;
      case SK_BACKSPACE      : return SDLK_BACKSPACE;
      case SK_ESCAPE         : return SDLK_ESCAPE   ;
      case SK_DELETE         : return SDLK_DELETE   ;
      case SK_SCREEN_CHANGE  : return SDLK_F9       ;
      default: // avoid warning "enumeration value 'XXX' not handled in switch"
         break;
   }     
#endif
   return key;
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
#ifndef HEADLESS
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
#else
   switch (key) {
      case SDLK_PAGEUP: return SK_PAGE_UP;
      case SDLK_PAGEDOWN: return SK_PAGE_DOWN;
      case SDLK_HOME: return SK_HOME;
      case SDLK_END: return SK_END;
      case SDLK_UP: return SK_UP;
      case SDLK_DOWN: return SK_DOWN;
      case SDLK_LEFT: return SK_LEFT;
      case SDLK_RIGHT: return SK_RIGHT;
      case SDLK_INSERT: return SK_INSERT;
      case SDLK_RETURN: return SK_ENTER;
      case SDLK_TAB: return SK_TAB;
      case SDLK_BACKSPACE: return SK_BACKSPACE;
      case SDLK_DELETE: return SK_DELETE;
      case SDLK_F9: return SK_SCREEN_CHANGE;
   }
#endif
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
#ifndef HEADLESS
   if (mods == -1)
      return PM_NONE;
   return ((mods & (1 << DIMKI_SHIFT))   ? PM_SHIFT   : PM_NONE) |
          ((mods & (1 << DIMKI_CONTROL)) ? PM_CONTROL : PM_NONE) |
          ((mods & (1 << DIMKI_ALT))     ? PM_ALT     : PM_NONE) ;
#else
   if(mods & KMOD_NONE)  return PM_NONE;
   return (((mods & KMOD_LSHIFT) || (mods & KMOD_RSHIFT)) ? PM_SHIFT   : PM_NONE) |
          (((mods & KMOD_LCTRL) || (mods & KMOD_RCTRL)) ? PM_CONTROL : PM_NONE) |
          (((mods & KMOD_LALT) || (mods & KMOD_RALT)) ? PM_ALT     : PM_NONE) ;
#endif
}
