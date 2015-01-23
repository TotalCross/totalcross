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



package totalcross.crypto.cipher;

import totalcross.crypto.*;

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
      return chaining == CHAINING_ECB || chaining == CHAINING_CBC;
   }
   
   protected final boolean isPaddingSupported(int padding)
   {
      return padding == PADDING_PKCS5;
   }
   
   native void nativeCreate();
   
   native protected final void doReset() throws NoSuchAlgorithmException, CryptoException;
   
   native protected byte[] process(byte[] data) throws CryptoException;
}
