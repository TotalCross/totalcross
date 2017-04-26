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

/**
 * This class implements the RSA cryptographic cipher private key.
 */
public class RSAPrivateKey extends Key
{
   private byte[] e;
   private byte[] d;
   private byte[] n;
   
   /**
    * Creates a new RSAPublicKey object, given the public and private exponents and the modulus.
    * 
    * @param e A byte array containing the public exponent.
    * @param d A byte array containing the private exponent.
    * @param n A byte array containing the modulus.
    */
   public RSAPrivateKey(byte[] e, byte[] d, byte[] n)
   {
      this.e = e;
      this.d = d;
      this.n = n;
   }
   
   /**
    * Returns a copy of the byte array containing the modulus.
    * 
    * @return A copy of the byte array containing the modulus.
    */
   public byte[] getModulus()
   {
      return n;
   }
   
   /**
    * Returns a copy of the byte array containing the public exponent.
    * 
    * @return A copy of the byte array containing the public exponent.
    */
   public byte[] getPublicExponent()
   {
      return e;
   }
   
   /**
    * Returns a copy of the byte array containing the private exponent.
    * 
    * @return A copy of the byte array containing the private exponent.
    */
   public byte[] getPrivateExponent()
   {
      return d;
   }
}
