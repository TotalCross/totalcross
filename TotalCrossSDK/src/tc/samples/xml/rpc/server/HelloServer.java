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



package tc.samples.xml.rpc.server;

/** This class must be ran as a standard java app with the commandline:
 * <pre>
 * java -classpath /TotalCross/dist/tc.jar;/TotalCross/dist/samples/applet/ext/webservice/server/HelloServer.jar;/TotalCross/bin/tools/apache/xmlrpc-1.2-b1.jar tc.samples.ext.webservice.server.HelloServer
 * </pre>
 * Check the bat and sh files located at /TotalCross/dist/samples/applet/ext/webservice/server
 */

import org.apache.xmlrpc.*;

public class HelloServer
{
/*   static class RobustRpcServer extends XmlRpcServer
   {
      public RobustRpcServer()
      {
      }
      public byte[] execute(InputStream is, XmlRpcContext context)
      {
         try
         {
            System.out.println("arrived with "+is.available());
         }
         catch (IOException e)
         {
            System.out.println("is returned: "+e);
         }
         return super.execute(is, context);
      }
   }*/

   public static void main(String args[])
   {
      // Check if portnumber was supplied.
      try
      {
         // Use the Apache Xerces SAX driver.
         XmlRpc.setDriver("uk.co.wilson.xml.MinML");

         int port = 9090;

         // Start the server.
         System.out.println("Server: starting XML-RPC server on port " + port );
         WebServer server = new WebServer(port/*,null,new RobustRpcServer()*/);

         // Register our handler classes.
         server.addHandler("hello", new HelloHandler());
         System.out.println("Server: registered HelloHandler class to 'hello'");

         server.addHandler("securehello", new SecureHelloHandler());
         System.out.println("Server: registered SecureHelloHandler class to 'securehello'");

         System.out.println("Server: now accepting requests...");
         server.start();
      }
      catch (ClassNotFoundException e)
      {
         System.out.println("Server: could not locate Apache Xerces SAX driver.\nDo you have /TotalCross/etc/tools/apache/xmlrpc-1.2-b1.jar in the classpath?");
      }
   }
}
