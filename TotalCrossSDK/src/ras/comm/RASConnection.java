package ras.comm;

import ras.ActivationClient;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.net.ConnectionManager;
import totalcross.util.Logger;

public class RASConnection
{
   Stream stream;

   private static Logger logger = Logger.getLogger("ras.ActivationClient", Logger.WARNING | Logger.SEVERE, Logger.DEBUG_CONSOLE);

   RASConnection()
   {
   }

   private RASConnection(Stream stream)
   {
      this.stream = stream;
   }

   public static RASConnection connect(int openTimeout, int readWriteTimeout) throws IOException
   {
      RASConnection connection = null;
      if (!ConnectionManager.isInternetAccessible())
         ConnectionManager.open();

      try
      {
         connection = new RASConnectionSOAP(openTimeout, readWriteTimeout);
         if (connection != null)
            return connection;
      }
      catch (Exception e)
      {
         connection = null;
         logger.warning("Failed to connect to the activation webservice.");
         logger.warning("Reason: " + e);
      }

      return null;
   }

   public static RASConnection connect(Stream stream) throws IOException
   {
      return new RASConnection(stream);
   }
   
   public void sayHello() throws CommException
   {
      send(new Hello(ActivationClient.version));
   }

   public void send(Packet packet) throws CommException
   {
      int id = packet.getClass().getName().hashCode();
      DataStream ds = new DataStream(stream, true);

      try
      {
         ds.writeInt(id);
         packet.write(ds);
      }
      catch (IOException ex)
      {
         throw new CommException("Cannot send packet", ex);
      }
   }

   public Packet receive() throws CommException
   {
      int id = 0;
      Class packetClass;
      Packet packet = null;

      // Receive packet
      DataStream ds = new DataStream(stream);
      try
      {
         id = ds.readInt();
         packetClass = (Class) Packet.packetClasses.get(id);
         if (packetClass == null)
            throw new CommException("Unsupported packet received: " + id);

         packet = (Packet) packetClass.newInstance();
         packet.read(ds);
      }
      catch (Exception ex)
      {
         throw new CommException("Cannot receive packet", ex);
      }

      return packet;
   }

   public void close() throws IOException
   {
   }
}
