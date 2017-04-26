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



package totalcross.phone;

/** Used to send and receive SMS messages. Currently supports only WP8.
 * See the SmsSample for an example.
 */
public class SMS
{
   /** Sends the given message to the destination.
    * @param destination The number to send to.
    * @param message The message to send. Note that some phones will strip unicode characters.
    * @throws totalcross.io.IOException if an error occurs.
    */
   public static void send(String destination, String message) throws totalcross.io.IOException
   {
   }
   
   /** Supposed to block until a message becomes available.
    *  Does not work on the currently supported platforms.
    *  @deprecated
    */
   public static String[] receive() throws totalcross.io.IOException
   {
      return null;
   }
}
