package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class BlobCore
{
   protected long magic;
   protected long length;
   protected long offset;

   protected byte[] data;

   protected BlobCore(long magic)
   {
      this.magic = magic;
   }

   public byte[] getBytes() throws IOException
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
}
