package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.util.Vector;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class SuperBlob extends BlobCore
{
   Vector index = new Vector();

   protected SuperBlob(long magic)
   {
      super(magic);
   }

   public void add(BlobIndex blobIndex)
   {
      index.addElement(blobIndex);
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      int count = index.size();
      writer.writeUnsignedInt(count);

      long idxPos = writer.pos;
      writer.pos += count * 8L;
      for (int i = 0; i < count; i++)
      {
         BlobIndex item = (BlobIndex) index.elementAt(i);
         item.offset = writer.pos - offset;
         writer.memorize();
         writer.moveTo(idxPos);
         writer.writeUnsignedInt(item.blobType);
         writer.writeUnsignedInt(item.offset);
         idxPos = writer.pos;
         writer.moveBack();
         BlobHandler.writeToStream(item.blob, writer);
      }
   }

   protected void readFromStream(ElephantMemoryReader reader) throws IOException, InstantiationException,
         IllegalAccessException
   {
      long count = reader.readUnsignedInt();
      for (long i = 0; i < count; i++)
         add(BlobIndex.readObject(reader, offset));
   }
}
