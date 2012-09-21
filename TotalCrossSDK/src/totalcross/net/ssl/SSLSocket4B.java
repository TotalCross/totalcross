package totalcross.net.ssl;

import totalcross.crypto.CryptoException;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.sys.Vm;

public class SSLSocket4B extends Socket
{
   private SSLClient sslClient;
   private SSL sslConnection;
   private SSLReadHolder sslReader;
   private ByteArrayStream buffer = null;

   public SSLSocket4B(String host, int port, int timeout) throws UnknownHostException, IOException
   {
      super(host, port, timeout);
   }

   protected SSLClient prepareContext() throws CryptoException
   {
      return new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
   }

   public void startHandshake() throws IOException
   {
      try
      {
         sslClient = prepareContext();
         sslConnection = sslClient.connect(this, null);
         Exception e = sslConnection.getLastException();
         if (e != null)
            throw new IOException(e.getMessage());
         int status;
         while ((status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS)
            Vm.sleep(25);
         if (status != Constants.SSL_OK)
            throw new IOException("SSL handshake failed");
         sslReader = new SSLReadHolder();
         buffer = new ByteArrayStream(256);
         buffer.mark();
      }
      catch (Exception e)
      {
         try
         {
            this.close();
         }
         catch (IOException e2)
         {
         }
         if (e instanceof IOException)
            throw (IOException) e;
         else
            throw new IOException(e.getMessage());
      }
   }

   public int readBytes(byte[] buf, int start, int count) throws IOException
   {
      if (buffer == null)
         return super.readBytes(buf, start, count);
      if (buffer.available() == 0)
      {
         int sslReadBytes = sslConnection.read(sslReader);
         buffer.reuse();
         if (sslReadBytes > 0)
            buffer.writeBytes(sslReader.getData(), 0, sslReadBytes);
         buffer.mark();
      }
      int readBytes = buffer.readBytes(buf, start, count);

      return readBytes;
   }

   public int readBytes(byte[] buf) throws IOException
   {
      return this.readBytes(buf, 0, buf.length);
   }

   public int writeBytes(byte[] buf, int start, int count) throws IOException
   {
      if (buffer == null)
         return super.writeBytes(buf, start, count);
      if (start > 0)
      {
         byte[] buf2 = new byte[count];
         Vm.arrayCopy(buf, start, buf2, 0, count);
         buf = buf2;
      }
      return sslConnection.write(buf, count);
   }
   
   public int superWriteBytes(byte[] buf, int start, int count) throws IOException
   {
      return super.writeBytes(buf, start, count);
   }
   
   public int superReadBytes(byte[] buf, int start, int count) throws IOException
   {
      return super.readBytes(buf, start, count);
   }

   public void close() throws IOException
   {
      if (buffer != null)
         buffer = null;
      if (sslConnection != null)
         sslConnection.dispose();
      if (sslClient != null)
         sslClient.dispose();
      super.close();
   }
}
