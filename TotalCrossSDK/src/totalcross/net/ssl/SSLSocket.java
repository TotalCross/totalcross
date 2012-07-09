package totalcross.net.ssl;

import totalcross.crypto.CryptoException;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.Socket;
import totalcross.net.UnknownHostException;
import totalcross.sys.Vm;

public class SSLSocket extends Socket
{
   private SSLClient sslClient;
   private SSL sslConnection;
   private SSLReadHolder sslReader;
   private ByteArrayStream buffer = null;

   public SSLSocket(String host, int port, int timeout) throws UnknownHostException, IOException
   {
      super(host, port, timeout);
   }

   public void startHandshake() throws IOException, CryptoException
   {
      sslClient = new SSLClient(Constants.SSL_SERVER_VERIFY_LATER, 0);
      sslConnection = sslClient.connect(this, null);
      Exception e = sslConnection.getLastException();
      if (e != null)
         throw new IOException(e.getMessage());
      int status;
      while ((status = sslConnection.handshakeStatus()) == Constants.SSL_HANDSHAKE_IN_PROGRESS)
         Vm.sleep(25);
      if (status != Constants.SSL_OK)
         throw new CryptoException("SSL handshake failed");
      sslReader = new SSLReadHolder();
      buffer = new ByteArrayStream(256);
      buffer.mark();
   }

   public int readBytes(byte[] buf, int start, int count) throws IOException
   {
      if (buffer == null)
         return super.readBytes(buf, start, count);
      if (buffer.available() == 0)
      {
         int sslReadBytes = sslConnection.read(sslReader);
         if (sslReadBytes > 0)
            buffer.writeBytes(sslReader.getData(), 0, sslReadBytes);
         buffer.mark();
      }
      int readBytes = buffer.readBytes(buf, start, count);
      buffer.reuse();
      buffer.mark();

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

   public void close() throws IOException
   {
      if (buffer != null)
      {
         buffer = null;
         sslConnection.dispose();
         sslClient.dispose();
      }
      super.close();
   }
}
