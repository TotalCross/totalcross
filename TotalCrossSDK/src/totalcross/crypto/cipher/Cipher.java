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

import totalcross.crypto.*;
import totalcross.io.ByteArrayStream;

/**
 * This class provides the functionality of a cryptographic cipher for encryption
 * and decryption.
    <p>
    If you get a <code>totalcross.crypto.CryptoException: Illegal key size</code>, you must
    download the strong cryptography files from <a href='http://www.totalcross.com/etc/securejars.zip' class=mail>here</a> <b>AFTER</b>
    understanding that you are elligible to do so as stated in <a href='http://java.sun.com/j2se/1.4.2/jre/README' class=mail>here</a> 
    (search for 'Unlimited Strength Java Cryptography Extension' - installation instructions are inside that topic).
 */
public abstract class Cipher
{
   Object cipherRef;
   Object keyRef;
   
   protected int operation = -1;
   protected Key key;
   protected int chaining;
   protected byte[] iv;
   protected int padding;
   protected Object nativeHeap;
   
   private ByteArrayStream input = new ByteArrayStream(128);
   private byte[] oneByte = new byte[1];
   
   /** Constant used to initialize cipher to encrypt. */
   public static final int OPERATION_ENCRYPT = 0;
   
   /** Constant used to initialize cipher to decrypt. */
   public static final int OPERATION_DECRYPT = 1;
   
   /** Constant used to initialize cipher using no chaining. */
   public static final int CHAINING_NONE = 0;
   
   /** Constant used to initialize cipher using ECB chaining. */
   public static final int CHAINING_ECB = 1;
   
   /** Constant used to initialize cipher using CBC chaining. */
   public static final int CHAINING_CBC = 2;
   
   /** Constant used to initialize cipher using no padding. */
   public static final int PADDING_NONE = 0;
   
   /** Constant used to initialize cipher using PKCS #1 padding. */
   public static final int PADDING_PKCS1 = 1;
   
   /** Constant used to initialize cipher using PKCS #5 padding.<br> 
    * Comment about PKCS#5 padding:<br><br> 
    * 
    * PKCS#5 padding is a padding scheme that always adds padding bytes even if the input 
    * size is a multiple of the blocksize (ie. 16 bytes). The value of the padding bytes is the number
    * of bytes added to get a multiple of the block size. Thus, once deciphered, you can immediately
    * find out how many padding bytes have been added by looking the value of the last byte of 
    * the output buffer. This is always true since there must always be at least one padding byte.<br><br>
    *   
    * See http://tools.ietf.org/html/rfc3852#section-6.3 for more details.<br><br>
    * 
    * For instance, if you use PKCS#5 with a 16 bytes input buffer, 16 values of 0x10 will be added resulting in 2
    * input blocks of 16 bytes each (32 bytes). Once deciphered, the 16 bytes padding block will be removed 
    * to resume the initial 16 bytes input buffer.
    */
   public static final int PADDING_PKCS5 = 2;
   
   public final String toString()
   {
      return getAlgorithm();
   }
   
   /**
    * Returns the initialization vector (IV) in a new buffer. This is useful in the
    * case where a random IV was created.
    *  
    * @return the initialization vector in a new buffer, or null if the underlying
    * algorithm does not use an IV, or if the IV has not yet been set.
    */
   public final byte[] getIV()
   {
      return iv;
   }
   
   /**
    * Initializes this cipher in encryption or decryption mode, without chaining
    * or padding. If this algorithm requires an initialization vector, it will be
    * generated using random values. Calling this method will also reset the input
    * data buffer.
    * 
    * @param operation the operation mode of this cipher (OPERATION_ENCRYPT or
    * OPERATION_DECRYPT).
    * @param key the key.
    * 
    * @throws CryptoException if one or more initialization parameters are invalid
    * or the cipher fails to initialize with the given parameters. 
    */
   public final void reset(int operation, Key key) throws CryptoException
   {
      reset(operation, key, CHAINING_NONE, null, PADDING_NONE);
   }
   
   /**
    * Initializes this cipher in encryption or decryption mode, with the given chaining
    * mode and without a padding. If this algorithm requires an initialization vector,
    * it will be generated using random values. Calling this method will also reset the input
    * data buffer.
    * 
    * @param operation the operation mode of this cipher (OPERATION_ENCRYPT or
    * OPERATION_DECRYPT).
    * @param key the key.
    * @param chaining the chaining mode of this cipher (CHAINING_NONE, CHAINING_ECB or
    * CHAINING_CBC).
    * 
    * @throws CryptoException if one or more initialization parameters are invalid
    * or the cipher fails to initialize with the given parameters.
    */
   public final void reset(int operation, Key key, int chaining) throws CryptoException
   {
      reset(operation, key, chaining, null, PADDING_NONE);
   }
   
   /**
    * Initializes this cipher in encryption or decryption mode, with the given chaining
    * mode, initialization vector and without a padding. If this algorithm requires an
    * initialization vector and an invalid value was supplied, it will be generated using
    * random values. Calling this method will also reset the input data buffer.
    * 
    * @param operation the operation mode of this cipher (OPERATION_ENCRYPT or
    * OPERATION_DECRYPT).
    * @param key the key.
    * @param chaining the chaining mode of this cipher (CHAINING_NONE, CHAINING_ECB or
    * CHAINING_CBC).
    * @param iv the initialization vector.
    * 
    * @throws CryptoException if one or more initialization parameters are invalid
    * or the cipher fails to initialize with the given parameters.
    */
   public final void reset(int operation, Key key, int chaining, byte[] iv) throws CryptoException
   {
      reset(operation, key, chaining, iv, PADDING_NONE);
   }
   
   /**
    * Initializes this cipher in encryption or decryption mode, with the given chaining
    * mode, initialization vector padding. If this algorithm requires an initialization
    * vector and an invalid value was supplied, it will be generated using random values.
    * Calling this method will also reset the input data buffer.
    * 
    * @param operation the operation mode of this cipher (OPERATION_ENCRYPT or
    * OPERATION_DECRYPT).
    * @param key the key.
    * @param chaining the chaining mode of this cipher (CHAINING_NONE, CHAINING_ECB or
    * CHAINING_CBC).
    * @param iv the initialization vector.
    * @param padding the padding mode of this cipher (PADDING_NONE, PADDING_PKCS1 or
    * PADDING_PKCS5).
    * 
    * @throws CryptoException if one or more initialization parameters are invalid
    * or the cipher fails to initialize with the given parameters.
    */
   public final void reset(int operation, Key key, int chaining, byte[] iv, int padding) throws CryptoException
   {
      if (operation < OPERATION_ENCRYPT || operation > OPERATION_DECRYPT)
         throw new CryptoException("Invalid or unsupported cipher operation: " + operation);
      if (key == null || !isKeySupported(key, operation))
         throw new CryptoException("Invalid or unsupported cipher key: " + key);
      if (chaining < CHAINING_NONE || chaining > CHAINING_CBC || !isChainingSupported(chaining))
         throw new CryptoException("Invalid or unsupported cipher chaining: " + chaining);
      if (padding < PADDING_NONE || padding > PADDING_PKCS5 || !isPaddingSupported(padding))
         throw new CryptoException("Invalid or unsupported cipher padding: " + padding);
      if (operation == OPERATION_DECRYPT && chaining == CHAINING_CBC && iv == null)
         throw new CryptoException("The initialization vector must be specified in the CBC DECRYPT mode.");
      
      this.operation = operation;
      this.key = key;
      this.chaining = chaining;
      this.iv = iv;
      this.padding = padding;
      
      input.reset();
      doReset();
   }
   
   /**
    * Updates the input data that will be processed by this cipher algorithm. The data
    * will be accumulated in an input buffer to be processed when {@link #getOutput()}
    * is finally called.
    * 
    * @param data the input data.
    */
   public final void update(int data)
   {
      oneByte[0] = (byte)(data & 0xFF);
      input.writeBytes(oneByte, 0, 1);
   }
   
   /**
    * Updates the input data that will be processed by this cipher algorithm. The data
    * will be accumulated in an input buffer to be processed when {@link #getOutput()}
    * is finally called.
    * 
    * @param data the input data.
    */
   public final void update(byte[] data)
   {
      input.writeBytes(data, 0, data.length);
   }
   
   /**
    * Updates the input data that will be processed by this cipher algorithm. The data
    * will be accumulated in an input buffer to be processed when {@link #getOutput()}
    * is finally called.
    * 
    * @param data the input data.
    * @param start the offset in <code>data</code> where the data starts.
    * @param count the input length.
    */
   public final void update(byte[] data, int start, int count)
   {
      input.writeBytes(data, start, count);
   }
   
   /**
    * Finalizes the encryption or decryption operation (depending on how this cipher
    * was initialized) by processing all the accumulated input data and returning the
    * result in a new buffer.
    * 
    * @return the operation result in a new buffer.
    */
   public byte[] getOutput() throws CryptoException
   {
      if (operation != OPERATION_ENCRYPT && operation != OPERATION_DECRYPT)
         throw new CryptoException("Cipher is not initialized");
      
      return process(input.toByteArray());
   }
   
   /**
    * @return the algorithm name of this cipher. 
    */
   public abstract String getAlgorithm();
   
   /**
    * @return the block length (in bytes), or 0 if the underlying algorithm is not a
    * block cipher.
    */
   public abstract int getBlockLength();
   
   protected abstract void doReset() throws NoSuchAlgorithmException, CryptoException;
   
   protected abstract byte[] process(byte[] data) throws CryptoException;
   
   protected abstract boolean isKeySupported(Key key, int operation);
   
   protected abstract boolean isChainingSupported(int chaining);
   
   protected abstract boolean isPaddingSupported(int padding);
}
