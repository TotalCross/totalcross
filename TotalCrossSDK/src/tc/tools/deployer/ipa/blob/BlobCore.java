package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class BlobCore
{
   public long magic;
   public long length;
   public long offset;

   protected byte[] data;

   protected BlobCore(long magic)
   {
      this.magic = magic;
   }

   public byte[] GetBlobBytes() throws IOException
   {
      ElephantMemoryWriter writer = new ElephantMemoryWriter();
      writer.bStreamLittleEndian = false;
      BlobHandler.writeToStream(this, writer);
      return writer.toByteArray();
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      writer.write(this.data);
   }

   protected void readFromStream(ElephantMemoryReader reader) throws IOException, InstantiationException,
         IllegalAccessException
   {
      reader.skip(length - 8);
   }

   protected long AnchorPointOffset1;
   public long WritePointOffset1;

   public void ReserveSpaceToWriteOffset1(ElephantMemoryWriter writer, long position)
   {
      this.AnchorPointOffset1 = position;
      this.WritePointOffset1 = writer.pos;
      writer.pos += 4L;
   }

   public void WriteOffsetNow1(ElephantMemoryWriter writer) throws IOException
   {
      long num2 = writer.pos - this.AnchorPointOffset1;
      writer.memorize();
      writer.moveTo(this.WritePointOffset1);
      writer.writeUnsignedInt(num2);
      writer.moveBack();
   }

   protected long AnchorPointOffset2;
   public long WritePointOffset2;

   public void ReserveSpaceToWriteOffset2(ElephantMemoryWriter writer, long position)
   {
      this.AnchorPointOffset2 = position;
      this.WritePointOffset2 = writer.pos;
      writer.pos += 4L;
   }

   public void WriteOffsetNow2(ElephantMemoryWriter writer) throws IOException
   {
      long num2 = writer.pos - this.AnchorPointOffset2;
      writer.memorize();
      writer.moveTo(this.WritePointOffset2);
      writer.writeUnsignedInt(num2);
      writer.moveBack();
   }
}
