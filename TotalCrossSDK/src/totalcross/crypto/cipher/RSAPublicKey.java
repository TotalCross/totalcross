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
 * This class implements the RSA cryptographic cipher public key.
 */
public class RSAPublicKey extends Key
{
   private byte[] e;
   private byte[] n;
   
   /**
    * Creates a new RSAPublicKey object, given the public exponent and the modulus.
    * 
    * @param e A byte array containing the public exponent.
    * @param n A byte array containing the modulus.
    */
   public RSAPublicKey(byte[] e, byte[] n)
   {
      this.e = e;
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
}
