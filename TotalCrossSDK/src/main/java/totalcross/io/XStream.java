package totalcross.io;

import java.io.Closeable;

public abstract class XStream implements Closeable {
   protected static final byte skipBuffer[] = new byte[128];

   public abstract CharSequence readLine() throws java.io.IOException;

   public abstract char[] readLine(long maxSize) throws java.io.IOException;
   
   public abstract long read(byte buf[], long start, long count) throws java.io.IOException;

   public abstract long write(byte buf[], long start, long count) throws java.io.IOException;

   public long skip(long n) throws java.io.IOException {
      long readBytesRet;
      long bytesSkipped = 0;
  
      while (n > 0) {
        long c = n > skipBuffer.length ? skipBuffer.length : n;
        readBytesRet = read(skipBuffer, 0, c);
        if (readBytesRet <= 0) {
          break;
        }
        bytesSkipped += readBytesRet;
        n -= c;
      }
  
      return bytesSkipped;
    }
}