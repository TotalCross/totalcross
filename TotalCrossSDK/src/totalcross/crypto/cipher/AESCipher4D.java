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

// $Id: AESCipher4D.java,v 1.6 2011-01-04 13:19:15 guich Exp $

package totalcross.crypto.cipher;

import totalcross.crypto.CryptoException;

public class AESCipher4D extends Cipher
{
   public AESCipher4D()
   {
      nativeCreate();
   }
   
   public final String getAlgorithm()
   {
      return "AES";
   }

   public final int getBlockLength()
   {
      return 16;
   }
   
   protected final boolean isKeySupported(Key key, int operation)
   {
      return key instanceof AESKey;
   }
   
   protected final boolean isChainingSupported(int chaining)
   {
      return chaining == CHAINING_CBC;
   }
   
   protected final boolean isPaddingSupported(int padding)
   {
      return padding == PADDING_PKCS5;
   }
   
   native void nativeCreate();
   
   native protected final void doReset() throws CryptoException;
   
   native protected byte[] process(byte[] data) throws CryptoException;
}
