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



package totalcross.crypto.cipher;

import java.io.ByteArrayInputStream;

import net.rim.device.api.crypto.AESDecryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.BlockDecryptor;
import net.rim.device.api.crypto.BlockDecryptorEngine;
import net.rim.device.api.crypto.BlockEncryptor;
import net.rim.device.api.crypto.BlockEncryptorEngine;
import net.rim.device.api.crypto.CBCDecryptorEngine;
import net.rim.device.api.crypto.CBCEncryptorEngine;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.crypto.PKCS5FormatterEngine;
import net.rim.device.api.crypto.PKCS5UnformatterEngine;
import totalcross.Launcher4B;
import totalcross.crypto.CryptoException;
import totalcross.io.ByteArrayStream;

public class AESCipher4B extends Cipher
{
   private ByteArrayStream output = new ByteArrayStream(128);
   private byte[] buf = new byte[1024];
   
   public final String getAlgorithm()
   {
      return "AES";
   }

   public final int getBlockLength()
   {
      return 16;
   }
   
   protected final void doReset() throws CryptoException
   {
      keyRef = new net.rim.device.api.crypto.AESKey(((AESKey)key).getData());
      
      try
      {
         if (operation == OPERATION_ENCRYPT)
         {
            BlockEncryptorEngine engine = new AESEncryptorEngine((net.rim.device.api.crypto.AESKey)keyRef);
            
            if (chaining == CHAINING_CBC)
            {
               engine = new CBCEncryptorEngine(engine, iv == null ? null : new InitializationVector(iv));
               if (iv == null)
                  iv = ((CBCEncryptorEngine)engine).getIV().getData();
            }
            
            cipherRef = new PKCS5FormatterEngine(engine);
         }
         else
         {
            BlockDecryptorEngine engine = new AESDecryptorEngine((net.rim.device.api.crypto.AESKey)keyRef);
            if (chaining == CHAINING_CBC)
               engine = new CBCDecryptorEngine(engine, new InitializationVector(iv));
            
            cipherRef = new PKCS5UnformatterEngine(engine);
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
            BlockEncryptor encryptor = new BlockEncryptor((PKCS5FormatterEngine)cipherRef, new Launcher4B.S2OS(output));
            encryptor.write(data);
            encryptor.close();
         }
         else
         {
            int r;
            
            BlockDecryptor decryptor = new BlockDecryptor((PKCS5UnformatterEngine)cipherRef, new ByteArrayInputStream(data));
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
}
