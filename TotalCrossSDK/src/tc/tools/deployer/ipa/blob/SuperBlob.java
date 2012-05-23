package tc.tools.deployer.ipa.blob;
import java.io.IOException;
import java.util.Vector;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class SuperBlob extends BlobCore
{
   Vector index = new Vector();

   protected SuperBlob()
   {}

   public static SuperBlob CreateCodeSigningTableBlob()
   {
      SuperBlob blob = new SuperBlob();
      blob.magic = CSMAGIC_EMBEDDED_SIGNATURE;
      return blob;
   }

   public static SuperBlob CreateRequirementsBlob()
   {
      SuperBlob blob = new SuperBlob();
      blob.magic = CSMAGIC_REQUIREMENTS;
      return blob;
   }

   public void Add(long Key, BlobCore Value)
   {
      index.addElement(new BlobIndex(Key, Value));
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      long basePosition = writer.getPos() - 8L;
      writer.CreateNewPhase();
      int count = index.size();
      writer.writeUnsignedInt(count);

      for (int i = 0 ; i < count ; i++)
      {
         BlobIndex item = (BlobIndex) index.elementAt(i);
         writer.writeUnsignedInt(item.blobType);
         item.blob.ReserveSpaceToWriteOffset1(writer, basePosition);
         writer.CurrentPhase.pending.add(item);
      }
      writer.ProcessEntirePhase();
   }

   protected void UnpackageData(ElephantMemoryReader reader, long Length) throws IOException
   {
      long baseOffset = reader.getPos() - 8L;
      long count = reader.readUnsignedInt();
      for (long i = 0; i < count; i++)
         index.addElement(new BlobIndex(reader, baseOffset));
   }
}
