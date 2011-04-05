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



package totalcross.crypto;

/** Thrown when something wrong occurs at the cryptographic algorithms.
    <p>
    If you get a <code>totalcross.crypto.CryptoException: Illegal key size</code>, you must
    download the strong cryptography files from <a href='http://www.totalcross.com/etc/securejars.zip' class=mail>here</a> <b>AFTER</b>
    understanding that you are elligible to do so as stated in <a href='http://java.sun.com/j2se/1.4.2/jre/README' class=mail>here</a> 
    (search for 'Unlimited Strength Java Cryptography Extension' - installation instructions are inside that topic).
 */

public class CryptoException extends Exception
{
   /** Constructs an empty Exception. */
   public CryptoException()
   {
   }
   
   /** Constructs an exception with the given message. */
   public CryptoException(String msg)
   {
      super(msg);
   }
}
