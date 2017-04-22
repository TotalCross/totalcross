package totalcross.net.ssl;

import totalcross.io.IOException;
import totalcross.net.*;

/**
 * SSLSocketFactory creates SSLSockets.
 */
public class SSLSocketFactory extends SocketFactory
{
   private static SSLSocketFactory instance;

   public static SocketFactory getDefault()
   {
      if (instance == null)
         instance = new SSLSocketFactory();
      return instance;
   }

   public Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException
   {
      return new SSLSocket(host, port, timeout);
   }
}
