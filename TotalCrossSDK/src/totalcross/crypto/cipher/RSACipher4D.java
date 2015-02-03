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

public class RSACipher4D extends Cipher
{
   public RSACipher4D()
   {
      nativeCreate();
   }
   
   public final String getAlgorithm()
   {
      return "RSA";
   }

   public final int getBlockLength()
   {
      return 0;
   }
   
   protected final boolean isKeySupported(Key key, int operation)
   {
      return (operation == OPERATION_ENCRYPT && key instanceof RSAPublicKey) || (operation == OPERATION_DECRYPT && key instanceof RSAPrivateKey);
   }
   
   protected final boolean isChainingSupported(int chaining)
   {
      return chaining == CHAINING_ECB;
   }
   
   protected final boolean isPaddingSupported(int padding)
   {
      return padding == PADDING_PKCS1;
   }
   
   native void nativeCreate();
   
   native protected final void finalize();
   
   native protected final void doReset() throws NoSuchAlgorithmException, CryptoException;
   
   native protected byte[] process(byte[] data) throws CryptoException;
}
