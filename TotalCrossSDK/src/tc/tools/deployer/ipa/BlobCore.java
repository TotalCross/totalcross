package tc.tools.deployer.ipa;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BlobCore
{
   /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
   public static final int CSMAGIC_REQUIREMENTS = 0xfade0c01;
   public static final int CSMAGIC_CODEDIRECTORY = 0xfade0c02;
   public static final int CSMAGIC_EMBEDDED_SIGNATURE = 0xfade0cc0;

   /**
    * https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h
    * http://www.opensource.apple.com/source/libsecurity_utilities/libsecurity_utilities-55010/lib/blob.h
    */
   public static final int CS_MAGIC_BLOB_WRAPPER = 0xfade0b01;

   /** https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h */
   public static final int CS_MAGIC_EMBEDDED_ENTITLEMENTS = 0xfade7171;

   public long magic;

   protected BlobCore()
   {}

   public static BlobCore CreateFromStream(ElephantMemoryReader reader) throws IOException
   {
      BlobCore blob;
      long num = reader.readUnsignedInt();
      long length = reader.readUnsignedInt();
      switch ((int) num)
      {
         case CSMAGIC_EMBEDDED_SIGNATURE:
            blob = SuperBlob.CreateCodeSigningTableBlob();
         break;

         case CS_MAGIC_EMBEDDED_ENTITLEMENTS:
            blob = BlobCore.CreateEntitlementsBlob();
         break;

         case CSMAGIC_REQUIREMENTS:
            blob = SuperBlob.CreateRequirementsBlob();
         break;

         case CSMAGIC_CODEDIRECTORY:
            blob = new CodeDirectoryBlob();
         break;

         case CS_MAGIC_BLOB_WRAPPER:
            blob = new CodeDirectorySignatureBlob();
         break;

         default:
            blob = new BlobCore();
         break;
      }
      blob.magic = num;
      blob.UnpackageData(reader, length);
      return blob;
   }

   public static BlobCore CreateEntitlementsBlob()
   {
      BlobCore blob = new BlobCore();
      blob.magic = CS_MAGIC_EMBEDDED_ENTITLEMENTS;
      return blob;
   }

   public static BlobCore CreateEntitlementsBlob(String EntitlementsText) throws UnsupportedEncodingException
   {
      BlobCore blob = CreateEntitlementsBlob();
      blob.MyData = EntitlementsText.getBytes("UTF-8");
      return blob;
   }

   public byte[] GetBlobBytes() throws IOException
   {
      ElephantMemoryWriter writer = new ElephantMemoryWriter();
      writer.bStreamLittleEndian = false;
      this.Write(writer);
      writer.CompleteWritingAndClose();
      return writer.toByteArray();
   }

   protected byte[] MyData;

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      writer.write(this.MyData);
   }

   protected void UnpackageData(ElephantMemoryReader reader, long length) throws IOException
   {
      reader.skip(length - 8);
   }

   public void Write(ElephantMemoryWriter writer) throws IOException
   {
      writer.writeUnsignedInt(this.magic);
      ReserveSpaceToWriteLength(writer);
      this.PackageData(writer);
      WriteLengthNow(writer);
   }

   protected long AnchorPoint;
   public long WritePoint;

   public void ReserveSpaceToWriteLength(ElephantMemoryWriter writer)
   {
      this.AnchorPoint = writer.pos - 4L;
      this.WritePoint = writer.pos;
      writer.pos += 4L;
   }

   public void WriteLengthNow(ElephantMemoryWriter writer) throws IOException
   {
      long num2 = writer.pos - this.AnchorPoint;
      writer.memorize();
      writer.moveTo(this.WritePoint);
      writer.writeUnsignedInt(num2);
      writer.moveBack();
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
