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



package tc.tools.converter.bytecode;

public class BC171_lookupswitch extends Switch
{
   public int npairs;
   public int[] values;

   public BC171_lookupswitch()
   {
      super(-1);
      int npc = pc+1;
      npc += ((4 - ((npc/* -pc0*/) & 3)) & 3); // padding
      def = readInt32(npc);
      npairs = readInt32(npc+4);
      npc += 8;

      values = new int[npairs];
      jumps  = new int[npairs];
      for (int i =0; i < jumps.length; i++,npc+=8)
      {
         values[i] = readInt32(npc);
         jumps[i] = readInt32(npc+4) + pc;
      }
      pcInc = npc - pc;
   }
   public void exec()
   {
      int low,high,mid,i,key;
      key = stack[stackPtr-1].asInt;
      // binary search
      if (npairs > 0)
      {
          low = 0;
          high = npairs;
          while (true)
          {
              mid = (high + low) / 2;
              i = values[mid];
              if (key == i)
              {
                  pcInc = jumps[i]; // found
                  break;
              }
              if (mid == low)
              {
                  pcInc = def; // not found
                  break;
              }
              if (key < i)
                  high = mid;
              else
                  low = mid;
          }
      }
      else
          pcInc = def; // no pairs
   }
}
