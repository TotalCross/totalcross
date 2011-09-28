/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



static int32 vmGetRemainingBattery() // ()I
{
   int32 percentP;
   SysBatteryState stateP;

   // guich@561_7: for some reason, the battery percentage is being returned in 7th parameter (SysBatteryState) instead of in the percent.
   // {char buf[100]; xstrprintf(buf, "%ld %ld %ld %ld %ld %ld %ld %ld",(long)u1,(long)u2,(long)u3,(long)u4,(long)u5,(long)u6,(long)u7,(long)perc); debug(buf);}

   SysBatteryInfo(false,0,0,0,0,0,0,&stateP,0);
   percentP = (int32)stateP;
   return percentP;
}

void vmSetAutoOff(bool enable)
{
   if ((!enable && oldAutoOffValue == 0) || (enable && oldAutoOffValue != 0))
   {
      if (enable)
         EvtResetAutoOffTimer(); // reset the auto-off timer so it don't sleep immediately
      oldAutoOffValue = (int32)SysSetAutoOffTime(oldAutoOffValue);
   }
}

static void vmSetTime(Object time) // (Lwaba/sys/Time;)V
{
   DateTimeType sysTime;
   int32 seconds;

   sysTime.year = Time_year(time);
   sysTime.month = Time_month(time);
   sysTime.day = Time_day(time);
   sysTime.hour = Time_hour(time);
   sysTime.minute = Time_minute(time);
   sysTime.second = Time_second(time);
   
   seconds = TimDateTimeToSeconds(&sysTime); // guich@tc126_61
   TimSetSeconds(seconds);
}

static void vmClipboardCopy(CharP str, int32 sLen) // (Ljava/lang/String;)V
{
   xpto
   ClipboardAddItem(clipboardText, str, sLen);
}

static Object vmClipboardPaste(Context currentContext) // ()Ljava/lang/String;
{
   UInt16 len;
   Object o=null;
   VoidHand ch = ClipboardGetItem(clipboardText, &len);
   if (ch && len)
   {
      o = createStringObjectFromCharP(currentContext, MemHandleLock(ch),len);
      MemHandleUnlock(ch);
   }
   else o = createStringObjectFromCharP(currentContext, "", 0);
   return o;
}

static bool vmIsKeyDown(int32 k)
{
   int32 state = KeyCurrentState();
   switch (k)
   {
      case SK_PAGE_UP:   return (state & keyBitPageUp)   != 0;
      case SK_PAGE_DOWN: return (state & keyBitPageDown) != 0;
      case SK_HARD1:     return (state & keyBitHard1)    != 0;
      case SK_HARD2:     return (state & keyBitHard1)    != 0;
      case SK_HARD3:     return (state & keyBitHard1)    != 0;
      case SK_HARD4:     return (state & keyBitHard1)    != 0;
      case SK_SYNC:      return (state & keyBitCradle)   != 0;
      case SK_CONTRAST:  return (state & keyBitContrast) != 0;
   }
   return false;
}

void rebootDevice()
{
   SysReset();
}

static void vmInterceptSpecialKeys(int32* keys, int32 len)
{
   if (interceptedSpecialKeys != null)
      freeArray(interceptedSpecialKeys);
   if (len == 0)
      interceptedSpecialKeys = null;
   else
   {
      int32 *dk;
      dk = interceptedSpecialKeys = newPtrArrayOf(Int32, len, null);
      if (interceptedSpecialKeys != null)
         for (; len-- > 0; keys++, dk++)
            *dk = keyPortable2Device(*keys);
   }
}

static int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   int32 ret = -1;
   MemHandle localID;
   UInt32 resultP = 0;
   MemPtr cmdPBP = null;
   UInt16 cmd = (UInt16) launchCode;
   UInt32 type;

   localID = (MemHandle) DmFindDatabase(szCommand);

   // guich@tc110_83: check if it exists and is an application
   if (localID == 0 || DmDatabaseInfo(localID, null, null, null, null, null, null, null, null, null, &type, null) != errNone || type != 'appl')
      return -999;

   if (szArgs != null && *szArgs)
   {
      cmdPBP = MemPtrNew(xstrlen(szArgs)+1);
      xstrcpy(cmdPBP, szArgs);
   }

   if (localID)
   {
      if (cmdPBP == null || MemPtrSetOwner(cmdPBP, 0) == errNone) // guich@tc110_81: check if cmdPBP is null
      {
         if (wait)
         {
            ret = SysAppLaunch(localID, 0, cmd, cmdPBP, &resultP);
            ret = resultP;
         }
         else
            ret = SysUIAppSwitch(localID, cmd, cmdPBP);
      }
   }

   return ret;
}

static void vmShowKeyCodes(bool show)
{
   UNUSED(show)
}


static bool vmTurnScreenOn(bool on)
{                                      
   Err err;
   if (on)
      err = HALDisplayWake();
   else
      err = HALDisplaySleep(false,false);
   return err = errNone;
}


bool isTreo();
extern Err HsIndicatorState (UInt16 count, UInt16 indicatorType, UInt16* stateP);
#define kIndicatorAlertAlert 0x301
typedef enum 
{ 
   kIndicatorTypeLed, 
   kIndicatorTypeVibrator, 
   // This must be last 
   kIndicatorTypeCount 
} HsIndicatorTypeEnum;

static void vmVibrate(int32 ms)
{
   if (isTreo())
   {
      UInt16 uintIndicatorState=kIndicatorAlertAlert;
      UInt16 count = (UInt16)max32(ms / 1000, 1);
      HsIndicatorState(count,kIndicatorTypeVibrator,&uintIndicatorState);
   }
}
