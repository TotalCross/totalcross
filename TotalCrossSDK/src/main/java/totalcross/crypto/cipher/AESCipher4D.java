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

import totalcross.crypto.CryptoException;
import totalcross.crypto.NoSuchAlgorithmException;

public class AESCipher4D extends Cipher
{
  public AESCipher4D()
  {
    nativeCreate();
  }

  @Override
  public final String getAlgorithm()
  {
    return "AES";
  }

  @Override
  public final int getBlockLength()
  {
    return 16;
  }

  @Override
  protected final boolean isKeySupported(Key key, int operation)
  {
    return key instanceof AESKey;
  }

  @Override
  protected final boolean isChainingSupported(int chaining)
  {
    return chaining == CHAINING_ECB || chaining == CHAINING_CBC;
  }

  @Override
  protected final boolean isPaddingSupported(int padding)
  {
    return padding == PADDING_PKCS5;
  }

  native void nativeCreate();

  @Override
  native protected final void doReset() throws NoSuchAlgorithmException, CryptoException;

  @Override
  native protected byte[] process(byte[] data) throws CryptoException;
}
