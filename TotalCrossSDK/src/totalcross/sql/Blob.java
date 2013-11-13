package totalcross.sql;

public interface Blob
{
   public long length() throws SQLWarning;

   public byte[] getBytes(long pos, int length) throws SQLWarning;

   public totalcross.io.Stream getBinaryStream() throws SQLWarning;

   public long position(byte[] pattern, long start) throws SQLWarning;

   public long position(Blob pattern, long start) throws SQLWarning;
}
