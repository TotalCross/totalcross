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

// $Id: SMS4B.java,v 1.5 2011-01-04 13:19:17 guich Exp $

package totalcross.phone;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class SMS4B
{
   public static void send(String destination, String message) throws totalcross.io.IOException
   {
      try
      {
         MessageConnection connection = (MessageConnection) Connector.open("sms://" + destination);
         TextMessage msg = (TextMessage) connection.newMessage(MessageConnection.TEXT_MESSAGE);
         msg.setPayloadText(message);
         connection.send(msg);
         connection.close();
      }
      catch (IOException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }

   public static String[] receive() throws totalcross.io.IOException
   {
      try
      {
         DatagramConnection connection = (DatagramConnection) Connector.open("sms://");
         Datagram datagram = connection.newDatagram(connection.getMaximumLength());
         connection.receive(datagram);
         String[] answer = new String[2];
         answer[0] = datagram.getAddress().substring(2);
         answer[1] = new String(datagram.getData());
         connection.close();
         return answer;
      }
      catch (IOException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }
}
