/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.net.mail;

/**
 * This class represents an Internet e-mail address using the syntax of RFC822. The typical address syntax is of the form
 * <code>user@host.domain</code> or <code>Personal Name <user@host.domain></code>.
 * 
 * @since TotalCross 1.13
 */
public class Address
{
   /**
    * The e-mail address in the form <code>user@host.domain</code>.
    */
   public String address;
   
   /**
    * The personal name.
    */
   public String personal;
   
   String type;

   /**
    * The e-mail address format used.
    */
   public static final String RFC822 = "rfc822";

   /**
    * Creates a new <code>Address</code> object, which represents an Internet email address.
    * 
    * @param address The address in the RFC822 format.
    * @param personal The personal name.
    * @throws AddressException If the given address is <code>null</code>, or does not contain the character <code>@</code>.
    * @since TotalCross 1.13
    */
   public Address(String address, String personal) throws AddressException
   {
      if (address == null || address.indexOf('@') == -1)
         throw new AddressException();
      this.address = address;
      this.personal = personal;
      type = RFC822;
   }

   /**
    * Returns a string representation of this address object.
    * 
    * @return A string representation of this address.
    */
   public String toString()
   {
      return ((personal != null ? personal : address) + " <" + address + ">");
   }
}
