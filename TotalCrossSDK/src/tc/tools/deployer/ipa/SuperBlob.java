package tc.tools.deployer.ipa;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class SuperBlob extends BlobCore
{
   private Map Slots = new LinkedHashMap();

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
      this.Slots.put(Long.valueOf(Key), Value);
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      long basePosition = writer.getPos() - 8L;
      writer.CreateNewPhase();
      writer.writeUnsignedInt(this.Slots.size());

      Set entrySet = this.Slots.entrySet();
      Object[] entries = entrySet.toArray();
      for (int i = 0 ; i < entries.length ; i++)
      {
         Entry entry = (Entry) entries[i];
         writer.writeUnsignedInt(((Long) entry.getKey()).longValue());
         ((BlobCore) entry.getValue()).ReserveSpaceToWriteOffset1(writer, basePosition);
         writer.CurrentPhase.pending.add(entry);
      }
      writer.ProcessEntirePhase();
   }

   protected void UnpackageData(ElephantMemoryReader reader, long Length) throws IOException
   {
      long num = reader.getPos() - 8L;
      long num2 = reader.readUnsignedInt();
      for (long i = 0; i < num2; i++)
      {
         long key = reader.readUnsignedInt();
         long num5 = reader.readUnsignedInt();

         reader.memorize();
         reader.moveTo(num + num5);
         BlobCore blob = BlobCore.CreateFromStream(reader);
         reader.moveBack();

         this.Slots.put(Long.valueOf(key), blob);
      }
   }
}
