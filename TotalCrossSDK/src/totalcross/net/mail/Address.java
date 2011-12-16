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
 * This class represents an Internet e-mail address using the syntax of RFC822. Typical address syntax is of the form
 * "user@host.domain" or "Personal Name <user@host.domain>".
 * 
 * @since TotalCross 1.13
 */
public class Address
{
   public String address;
   public String personal;
   String type;

   public static final String RFC822 = "rfc822";

   /**
    * Creates a new Address object, which represents an Internet email address.
    * 
    * @param address
    *           the address in RFC822 format
    * @param personal
    *           the personal name
    * @throws AddressException
    *            if the given address is null, or does not contain the character '@'
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
    * Return a String representation of this address object.
    * 
    * @return string representation of this address
    */
   public String toString()
   {
      return ((personal != null ? personal : address) + " <" + address + ">");
   }
}
