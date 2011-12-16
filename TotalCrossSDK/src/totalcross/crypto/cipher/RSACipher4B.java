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

import java.io.ByteArrayInputStream;

import net.rim.device.api.crypto.BlockDecryptor;
import net.rim.device.api.crypto.BlockEncryptor;
import net.rim.device.api.crypto.PKCS1FormatterEngine;
import net.rim.device.api.crypto.PKCS1UnformatterEngine;
import net.rim.device.api.crypto.RSACryptoSystem;
import net.rim.device.api.crypto.RSADecryptorEngine;
import net.rim.device.api.crypto.RSAEncryptorEngine;
import totalcross.Launcher4B;
import totalcross.crypto.*;
import totalcross.io.ByteArrayStream;

public class RSACipher4B extends Cipher
{
   private ByteArrayStream output = new ByteArrayStream(128);
   private byte[] buf = new byte[1024];
   
   public final String getAlgorithm()
   {
      return "RSA";
   }

   public final int getBlockLength()
   {
      return 0;
   }
   
   protected final void doReset() throws NoSuchAlgorithmException, CryptoException
   {
      try
      {
         if (operation == OPERATION_ENCRYPT)
         {
            RSAPublicKey pubKey = (RSAPublicKey)key;
            byte[] e = pubKey.getPublicExponent();
            byte[] n = pubKey.getModulus();
            
            keyRef = new net.rim.device.api.crypto.RSAPublicKey(new RSACryptoSystem(getModulusBitLength(n)), e, n);
            RSAEncryptorEngine engine = new RSAEncryptorEngine((net.rim.device.api.crypto.RSAPublicKey)keyRef);
            
            cipherRef = new PKCS1FormatterEngine(engine);
         }
         else
         {
            RSAPrivateKey privKey = (RSAPrivateKey)key;
            byte[] e = privKey.getPublicExponent();
            byte[] d = privKey.getPrivateExponent();
            byte[] n = privKey.getModulus();
            
            keyRef = new net.rim.device.api.crypto.RSAPrivateKey(new RSACryptoSystem(getModulusBitLength(n)), e, d, n);
            RSADecryptorEngine engine = new RSADecryptorEngine((net.rim.device.api.crypto.RSAPrivateKey)keyRef);
            
            cipherRef = new PKCS1UnformatterEngine(engine);
         }
      }
      catch (net.rim.device.api.crypto.CryptoException e)
      {
         throw new CryptoException(e.getMessage());
      }
   }
   
   protected final byte[] process(byte[] data) throws CryptoException
   {
      try
      {
         output.reset();
         
         if (operation == OPERATION_ENCRYPT)
         {
            BlockEncryptor encryptor = new BlockEncryptor((PKCS1FormatterEngine)cipherRef, new Launcher4B.S2OS(output));
            encryptor.write(data);
            encryptor.close();
         }
         else
         {
            int r;
            
            BlockDecryptor decryptor = new BlockDecryptor((PKCS1UnformatterEngine)cipherRef, new ByteArrayInputStream(data));
            while ((r = decryptor.read(buf)) > 0)
               output.writeBytes(buf, 0, r);
            
            decryptor.close();
         }
         
         return output.toByteArray();
      }
      catch (java.io.IOException e)
      {
         throw new CryptoException(e.getMessage());
      }
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
   
   private int getModulusBitLength(byte[] n)
   {
      int i = 0, count = n.length;
      while (i < count && n[i] == 0)
         i++;
      
      return (count - i) * 8;
   }
}
