package tc.tools.deployer.ipa;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import tc.tools.deployer.ipa.blob.BlobCore;
import tc.tools.deployer.ipa.blob.BlobIndex;

public class ElephantMemoryWriter implements ElephantMemoryStream
{
   byte[] buffer;
   public int pos;
   Stack positions = new Stack();

   public WritingPhase CurrentPhase = new WritingPhase();
   private Stack PendingPhases = new Stack();

   public boolean bStreamLittleEndian = true;

   public ElephantMemoryWriter()
   {
      buffer = new byte[512];
   }

   public ElephantMemoryWriter(byte[] data) throws IOException
   {
      buffer = data;
   }

   public void writeInt(int value) throws IOException
   {
      byte[] b = new byte[4];
      if (bStreamLittleEndian)
      {
         b[3] = (byte) value;
         value >>= 8;
         b[2] = (byte) value;
         value >>= 8;
         b[1] = (byte) value;
         value >>= 8;
         b[0] = (byte) value;
      }
      else
      {
         b[0] = (byte) value;
         value >>= 8;
         b[1] = (byte) value;
         value >>= 8;
         b[2] = (byte) value;
         value >>= 8;
         b[3] = (byte) value;
      }
      this.write(b);
   }

   public void writeUnsignedInt(long value) throws IOException
   {
      byte[] b = new byte[4];
      int i = (int) value;
      if (bStreamLittleEndian)
      {
         b[0] = (byte) i;
         i >>= 8;
         b[1] = (byte) i;
         i >>= 8;
         b[2] = (byte) i;
         i >>= 8;
         b[3] = (byte) i;
      }
      else
      {
         b[3] = (byte) i;
         i >>= 8;
         b[2] = (byte) i;
         i >>= 8;
         b[1] = (byte) i;
         i >>= 8;
         b[0] = (byte) i;
      }
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
      this.pos = ((Integer) positions.pop()).intValue();
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
      positions.push(Integer.valueOf(this.pos));
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

   public void CompleteWritingAndClose() throws IOException
   {
      while (ProcessEntirePhase())
         ;
   }

   public void CreateNewPhase()
   {
      this.PendingPhases.push(this.CurrentPhase);
      this.CurrentPhase = new WritingPhase();
   }

   public boolean ProcessEntirePhase() throws IOException
   {
      while (!CurrentPhase.pending.isEmpty())
      {
         BlobIndex item = (BlobIndex) CurrentPhase.pending.remove();
         BlobCore blob = (BlobCore) item.blob;
         blob.WriteOffsetNow1(this);
         blob.Write(this);
      }
      if (!PendingPhases.isEmpty())
      {
         this.CurrentPhase = (WritingPhase) PendingPhases.pop();
         return true;
      }
      return false;
   }
}
