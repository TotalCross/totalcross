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

public class BC196_wide extends ByteCode
{
   public ByteCode widen;

   public BC196_wide()
   {
      switch (readUInt8(pc+1)) // get the next instruction
      {
         case ILOAD:  widen = new BC021_iload(true);  break;
         case LLOAD:  widen = new BC022_lload(true);  break;
         case FLOAD:  widen = new BC023_fload(true);  break;
         case DLOAD:  widen = new BC024_dload(true);  break;
         case ALOAD:  widen = new BC025_aload(true);  break;
         case ISTORE: widen = new BC054_istore(true); break;
         case LSTORE: widen = new BC055_lstore(true); break;
         case FSTORE: widen = new BC056_fstore(true); break;
         case DSTORE: widen = new BC057_dstore(true); break;
         case ASTORE: widen = new BC058_astore(true); break;
         case RET:    widen = new BC169_ret(true);    break;
         case IINC:   widen = new BC132_iinc(true);   break;
      }
      pcInc = 1 + widen.pcInc;
   }
   public void exec()
   {
      widen.exec();
   }
}
