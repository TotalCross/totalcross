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



#include "P:/extlibs/PalmOS5RE/Incs/System/EventPrv.h"

int32 privateKeyPortable2Device(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_PAGE_UP        : return pageUpChr;
      case SK_PAGE_DOWN      : return pageDownChr;
      case SK_UP             : return upArrowChr;
      case SK_DOWN           : return downArrowChr;
      case SK_LEFT           : return leftArrowChr;
      case SK_RIGHT          : return rightArrowChr;
      case SK_ENTER          : return returnChr;
      case SK_TAB            : return tabChr;
      case SK_BACKSPACE      : return backspaceChr;
      case SK_ESCAPE         : return escapeChr;
      case SK_MENU           : return menuChr;
      case SK_COMMAND        : return commandChr;
      case SK_KEYBOARD_ABC   : return keyboardAlphaChr;
      case SK_KEYBOARD_123   : return keyboardNumericChr;
      case SK_KEYBOARD       : return keyboardChr;
      case SK_HARD1          : return hard1Chr;
      case SK_HARD2          : return hard2Chr;
      case SK_HARD3          : return hard3Chr;
      case SK_HARD4          : return hard4Chr;
      case SK_CALC           : return calcChr;
      case SK_FIND           : return findChr;
      case SK_LAUNCH         : return launchChr;
      case SK_ACTION         : return vchrHardRockerCenter;
      case SK_CONTRAST       : return contrastChr;
      case SK_CLOCK          : return 0x20E;
      case SK_SYNC           : return vchrHardCradle;
      default                : return key;
   }
}

PortableSpecialKeys privateKeyDevice2Portable(int32 key)
{
   switch (key)
   {
      case pageUpChr          : return SK_PAGE_UP;
      case pageDownChr        : return SK_PAGE_DOWN;
      //case DK_HOME          : *dk = ; break;
      //case DK_END           : *dk = ; break;
      case vchrRockerUp       :
      case upArrowChr         : return SK_UP;
      case vchrRockerDown     :
      case downArrowChr       : return SK_DOWN;
      case vchrRockerLeft     :
      case leftArrowChr       : return SK_LEFT;
      case vchrRockerRight    :
      case rightArrowChr      : return SK_RIGHT;
      //case DK_INSERT        : return sysK == ;
      case commandChr         : return SK_COMMAND;
      case returnChr          :
      case 10                 : return SK_ENTER;
      case tabChr             : return SK_TAB;
      case backspaceChr       : return SK_BACKSPACE;
      case escapeChr          : return SK_ESCAPE;
      //case DK_DELETE        : return sysK == ;
      case menuChr            : return SK_MENU;
      case 0x1609             :  // Treo's alt key
      case keyboardAlphaChr   : return SK_KEYBOARD_ABC;
      case keyboardNumericChr : return SK_KEYBOARD_123;
      case keyboardChr        : return SK_KEYBOARD;
      case hard1Chr           : return SK_HARD1;
      case hard2Chr           : return SK_HARD2;
      case hard3Chr           : return SK_HARD3;
      case hard4Chr           : return SK_HARD4;
      case calcChr            : return SK_CALC;
      case findChr            : return SK_FIND;
      case launchChr          : return SK_LAUNCH;
      case vchrHardRockerCenter: return SK_ACTION;
      case contrastChr        :
      case brightnessChr      :
      case (vchrPalmMin + 2)  : return SK_CONTRAST;
      case (vchrPalmMin + 0)  :
      case 0x20E              : return SK_CLOCK;
      case vchrHardCradle     : return SK_SYNC;
   }
   return key;
}

PortableModifiers privateKeyGetPortableModifiers(int32 mods)
{
   if (mods == -1) // detect current state
   {
      Boolean caps=false, numlock=false, autoshift=false;
      UInt16 shift=false;
      GrfGetState(&caps,&numlock,&shift,&autoshift);
      return shift ? PM_SHIFT : PM_NONE;
   }
   else // gotten from key event
   {
      return ((mods & shiftKeyMask)   ? PM_SHIFT   : PM_NONE) |
             ((mods & commandKeyMask) ? PM_CONTROL : PM_NONE);
   }
}
