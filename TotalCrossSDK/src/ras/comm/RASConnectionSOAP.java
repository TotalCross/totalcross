package ras.comm;

import ras.*;
import ras.ui.*;

import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.util.*;
import totalcross.xml.soap.*;

public class RASConnectionSOAP extends RASConnection
{
   private String uri = ActivationClient.defaultServerURI;
   private String namespace;
   private int openTimeout;
   private int readWriteTimeout;

   private int helloHashCode;
   private int version;
   private String encodedResponse;

   private Hashtable userDefinedParams;

   RASConnectionSOAP(int openTimeout, int readWriteTimeout) throws IOException
   {
      if (Settings.activationServerURI != null)
      {
         this.uri = Settings.activationServerURI;
         this.namespace = Settings.activationServerNamespace;
         this.userDefinedParams = ActivationHtml.getUserDefinedParams();
         
         //flsobral@tc125: send fields for the user defined webservice.
         userDefinedParams.put("Settings.deviceId", Settings.deviceId);
         userDefinedParams.put("Settings.platform", Settings.platform);
         userDefinedParams.put("Settings.romVersion", Convert.toString(Settings.romVersion));
         userDefinedParams.put("Settings.version", Convert.toString(Settings.version));
         userDefinedParams.put("Settings.versionStr", Settings.versionStr);
         if (Settings.applicationId != null)
            userDefinedParams.put("Settings.applicationId", Settings.applicationId);
         if (Settings.appVersion != null)
            userDefinedParams.put("Settings.appVersion", Settings.appVersion);
         userDefinedParams.put("Settings.activationId", Settings.activationId);
         userDefinedParams.put("Settings.imei", Settings.imei != null ? Settings.imei : "");
      }
      this.openTimeout = openTimeout;
      this.readWriteTimeout = readWriteTimeout;
   }

   public void sayHello() throws CommException
   {
      Hello hello = new Hello(ActivationClient.version);
      helloHashCode = hello.getClass().getName().hashCode();
      version = hello.getVersion();
   }

   public void send(Packet packet) throws CommException
   {
      SOAP soap = new SOAP(packet.webServiceMethod, uri);
      soap.openTimeout = openTimeout;
      soap.readTimeout = readWriteTimeout;
      soap.writeTimeout = readWriteTimeout;
      if (namespace != null)
         soap.namespace = namespace;
      
      try
      {
         ByteArrayStream bas = new ByteArrayStream(256);
         DataStream ds = new DataStream(bas, true);

         // hello
         ds.writeInt(helloHashCode);
         ds.writeInt(version);
         ds.writeInt(packet.getClass().getName().hashCode());
         packet.write(ds);
         String encodedPacket = Base64.encode(bas.toByteArray());
         soap.setParam(encodedPacket, "request");

         if (uri != ActivationClient.defaultServerURI && userDefinedParams != null)
         {
            soap.setParam((String[]) userDefinedParams.getKeys().toObjectArray(), "keys");
            soap.setParam((String[]) userDefinedParams.getValues().toObjectArray(), "values");
         }
         
         soap.execute();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new CommException("Cannot send packet", ex);
      }

      Object answer = soap.getAnswer();
      
      if (uri != ActivationClient.defaultServerURI && userDefinedParams != null) // user defined activation
      {
         if (answer instanceof String)
            throw new CommException((String) answer);
         String[] result = (String[]) answer;
         if (result.length == 1)
            throw new CommException(result[0]);
         else
            encodedResponse = result[1];
      }
      else
      {
         if (answer instanceof String)
            encodedResponse = (String) answer; 
      }
   }

   public Packet receive() throws CommException
   {
      byte[] decodedResponse = Base64.decode(encodedResponse);
      ByteArrayStream bas = new ByteArrayStream(decodedResponse);

      int id = 0;
      Class<?> packetClass;
      Packet packet = null;

      // Receive packet
      DataStream ds = new DataStream(bas);
      try
      {
         id = ds.readInt();
         packetClass = (Class<?>) Packet.packetClasses.get(id);
         if (packetClass == null)
            throw new CommException("Unsupported packet received: " + id);
         packet = (Packet) packetClass.newInstance();
         packet.read(ds);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         throw new CommException("Cannot receive packet", ex);
      }

      return packet;
   }
}
