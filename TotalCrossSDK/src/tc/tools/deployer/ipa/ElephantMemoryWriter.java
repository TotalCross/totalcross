package tc.tools.deployer.ipa;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class ElephantMemoryWriter implements ElephantMemoryStream
{
   byte[] buffer;
   public int pos;
   Stack<Integer> positions = new Stack<Integer>();

   public ElephantMemoryWriter()
   {
      buffer = new byte[512];
   }

   public ElephantMemoryWriter(byte[] data) throws IOException
   {
      buffer = data;
   }

   public void writeUnsignedInt(long value) throws IOException
   {
      byte[] b = new byte[4];
      int i = (int) value;
      b[3] = (byte) i;
      i >>= 8;
      b[2] = (byte) i;
      i >>= 8;
      b[1] = (byte) i;
      i >>= 8;
      b[0] = (byte) i;
      this.write(b);
   }

   public void writeUnsignedIntLE(long value) throws IOException
   {
      byte[] b = new byte[4];
      int i = (int) value;
      b[0] = (byte) i;
      i >>= 8;
      b[1] = (byte) i;
      i >>= 8;
      b[2] = (byte) i;
      i >>= 8;
      b[3] = (byte) i;
      this.write(b);
   }

   public void write(byte value)
   {
      this.write(new byte[] { value });
   }

   public void write(byte[] b)
   {
      int available = buffer.length - pos;
      if (b.length > available)
         buffer = Arrays.copyOf(buffer, buffer.length + (b.length - available));
      for (int i = 0; i < b.length; i++)
         buffer[pos++] = b[i];
   }

   public void moveBack()
   {
      this.pos = positions.pop();
   }

   public int getPos()
   {
      return pos;
   }

   public void moveTo(int newPosition)
   {
      pos = newPosition;
   }

   public void moveTo(long newPosition)
   {
      pos = (int) newPosition;
   }

   public void memorize()
   {
      positions.push(this.pos);
   }

   public int size()
   {
      return pos == 0 ? buffer.length : pos;
   }

   public byte[] toByteArray()
   {
      if (pos == 0 || buffer.length == pos)
         return buffer;
      else
      {
         byte[] b = new byte[pos];
         System.arraycopy(buffer, 0, b, 0, pos);
         return b;
      }
   }
}
