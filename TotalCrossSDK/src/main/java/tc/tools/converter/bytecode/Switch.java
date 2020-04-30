// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class Switch extends ByteCode {
  public int[] jumps;
  public int def; // pc for the "default" instruction. if there's no default, it points to the end of the switch.
  public int paramStackPos;

  public Switch(int paramStackPos) {
    this.paramStackPos = paramStackPos;
  }
  /*
   The code below does not work. This sample:
  public class AddressBook
  {
    public void onEvent()
    {
      int type = 300;
        switch(type)
        {
         default: return;
         case 300:
            getActiveTab();
            break;
        }
        clear();
    }
    private int getActiveTab() {return 1;}
   private void clear() {}
  }
  
  results on this:
    public void onEvent()
    {
    //    0    0:sipush          300
    //    1    3:istore_1
    //    2    4:iload_1
    //    3    5:tableswitch     300 300: default 24
    //                   300 25
    //    4   24:return
    //    5   25:aload_0
    //    6   26:invokespecial   #5   <Method int getActiveTab()>
    //    7   29:pop
   * //    8   30:aload_0
    //    9   31:invokespecial   #4   <Method void clear()>
    //   10   34:return
    }
  Which proves that is impossible to find where the switch ends. (marked by *)
  
   public int getPosAfterSwitch()
   {
      // here we scan the code from the first case to the last one and find the farest goto
      ByteCode []bcs = jc.bcs;
      int pcAfterSwitch = pcInMethod + pcInc;
      ByteCode posAfterSwitch = jc.getAtPC(pcAfterSwitch);
      int maxGoto = max(jumps,def);
      for (int i = posAfterSwitch.posInMethod; i < bcs.length; i++)
      {
         ByteCode current = bcs[i];
         if (current.pcInMethod >= maxGoto || current instanceof Return)
            break;
         if (current.bc == GOTO)
         {
            Branch b = (Branch)current;
            int gotoPC = b.jumpTo;
            if (gotoPC > maxGoto)
               maxGoto = gotoPC;
         }
      }
      ByteCode mg = jc.getAtPC(maxGoto);
      maxGoto = mg.posInMethod; // convert pc into pos
      return maxGoto;
   }
   private int max(int[] j, int k)
   {
      int m = k;
      for (int i = 0; i < j.length; i++)
         if (j[i] > m)
            m = j[i];
      return m;
   }
   */}
