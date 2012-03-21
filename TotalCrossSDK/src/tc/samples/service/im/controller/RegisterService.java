package tc.samples.service.im.controller;

import totalcross.*;
import totalcross.sys.*;

public class RegisterService implements MainClass
{
   public RegisterService()
   {
      Vm.exec("register service",null,0,true);
   }
   
   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
   {
   }

   public void appStarting(int timeAvail)
   {
   }

   public void appEnding()
   {
   }

   public void _onTimerTick(boolean canUpdate)
   {
   }
}
