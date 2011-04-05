/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

/** Used to send and receive SMS messages. Currently supports Windows CE and Blackberry.
 * See the SmsSample for an example.
 */
public class SMS
{
   /** Sends the given message to the destination.
    * @param destination The number to send to.
    * @param message The message to send. Note that some phones will strip unicode characters.
    * @throws totalcross.io.IOException If any error occurs. For Windows CE errors, refer to 
    * <a href='http://msdn.microsoft.com/en-us/library/aa455072.aspx'>http://msdn.microsoft.com/en-us/library/aa455072.aspx</a>.
    */
   public static void send(String destination, String message) throws totalcross.io.IOException
   {
   }
   
   /** Blocks until a message becomes available. You may want to call this from within a thread, otherwise your
    * program will block forever.
    * <p>
    * On Windows CE, if you get an error 0x105, there's another program that is listening to SMS. In this case you will have to close that program.
    * Only one program can receive SMS messages at a time. All messages that are received are not visualized by the phone's SMS program
    * (in other words, if you don't save them, they will be lost).
    * @return A String array where the first string contains the number and the second string contains the message.
    * @throws totalcross.io.IOException If any error occurs. For Windows CE errors, refer to 
    * <a href='http://msdn.microsoft.com/en-us/library/aa455072.aspx'>http://msdn.microsoft.com/en-us/library/aa455072.aspx</a>.
    * 
    */
   public static String[] receive() throws totalcross.io.IOException
   {
      return null;
   }
}
