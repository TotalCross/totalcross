package totalcross.io;

public abstract class XRandomAccessStream extends XStream {

   public abstract void reset() throws java.io.IOException;

   public abstract long pos() throws java.io.IOException;

   public abstract void seek(long pos) throws java.io.IOException;

   public abstract long size() throws java.io.IOException;
   
}