package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public class CodeDirectory extends BlobCore
{
   /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
   public static final long CSMAGIC_CODEDIRECTORY = 0xfade0c02;

   private long version;
   private long flags;
   private long hashOffset;
   private long identOffset;
   private long nSpecialSlots;
   private long nCodeSlots;
   private long codeLimit;
   private byte hashSize;
   private byte hashType;
   private byte spare1;
   private byte pageSize;
   private long spare2;
   private long scatterOffset;

   public final int cdApplicationSlot = 4;
   public final int cdEntitlementSlot = 5;
   public final int cdInfoSlot = 1;
   public final int cdRequirementsSlot = 2;
   public final int cdResourceDirSlot = 3;
   public final int cdSlotMax = 5;

   private byte[] Hashes;
   protected GeneralDigest HashProvider = new SHA1Digest();

   private String Identifier;

   public CodeDirectory()
   {
      super(CSMAGIC_CODEDIRECTORY);
   }

   public CodeDirectory(String ApplicationID, long SignedFileLength)
   {
      super(CSMAGIC_CODEDIRECTORY);
      this.Identifier = ApplicationID;
      this.version = 0x20100;
      this.flags = 0;
      this.spare1 = 0;
      this.spare2 = 0;
      this.scatterOffset = 0;
      this.pageSize = 12;
      int num = ((int) 1) << this.pageSize;
      this.hashType = 1;
      this.hashSize = (byte) this.HashProvider.getDigestSize();
      this.codeLimit = SignedFileLength;
      this.nSpecialSlots = 5;
      this.nCodeSlots = (long) (((this.codeLimit + num) - ((long) 1L)) / ((long) num));
      this.Hashes = new byte[(int) ((this.nSpecialSlots + this.nCodeSlots) * this.hashSize)];
   }

   public void ComputeImageHashes(byte[] SignedFileData)
   {
      for (int i = 0; i < this.nCodeSlots; i++)
      {
         int offset = i * this.PageSize();
         int num3 = ((int) this.codeLimit) - offset;
         HashProvider.reset();
         HashProvider.update(SignedFileData, offset, Math.min(num3, this.PageSize()));
         HashProvider.doFinal(this.Hashes, (int) ((this.nSpecialSlots + i) * this.hashSize));
      }
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex)
   {
      for (int i = 0; i < this.hashSize; i++)
         this.Hashes[((5 - SpecialSlotIndex) * this.hashSize) + i] = 0;
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex, byte[] SourceData)
   {
      HashProvider.reset();
      HashProvider.update(SourceData, 0, SourceData.length);
      HashProvider.doFinal(this.Hashes, (int) ((this.nSpecialSlots - SpecialSlotIndex) * this.hashSize));
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      writer.writeUnsignedInt(this.version);
      writer.writeUnsignedInt(this.flags);
      ReserveSpaceToWriteOffset1(writer, offset - (this.hashSize * this.nSpecialSlots));
      ReserveSpaceToWriteOffset2(writer, offset);
      writer.writeUnsignedInt(this.nSpecialSlots);
      writer.writeUnsignedInt(this.nCodeSlots);
      writer.writeUnsignedInt(this.codeLimit);
      writer.write(this.hashSize);
      writer.write(this.hashType);
      writer.write(this.spare1);
      writer.write(this.pageSize);
      writer.writeUnsignedInt(this.spare2);
      writer.writeUnsignedInt(this.scatterOffset);
      WriteOffsetNow2(writer);
      byte[] b1 = this.Identifier.getBytes("US-ASCII");
      byte[] b2 = new byte[b1.length + 1];
      System.arraycopy(b1, 0, b2, 0, b1.length);
      b2[b2.length - 1] = 0;
      writer.write(b2);
      WriteOffsetNow1(writer);
      writer.write(this.Hashes);
   }

   protected void readFromStream(ElephantMemoryReader reader) throws IOException
   {
      this.version = reader.readUnsignedInt();
      this.flags = reader.readUnsignedInt();
      this.hashOffset = reader.readUnsignedInt();
      this.identOffset = reader.readUnsignedInt();
      this.nSpecialSlots = reader.readUnsignedInt();
      this.nCodeSlots = reader.readUnsignedInt();
      this.codeLimit = reader.readUnsignedInt();
      this.hashSize = (byte) reader.read();
      this.hashType = (byte) reader.read();
      this.spare1 = (byte) reader.read();
      this.pageSize = (byte) reader.read();
      this.spare2 = reader.readUnsignedInt();
      this.scatterOffset = reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(offset + identOffset);
      this.Identifier = reader.readString();
      reader.moveBack();
      long num4 = this.nSpecialSlots + this.nCodeSlots;
      this.Hashes = new byte[(int) (num4 * this.hashSize)];
      reader.memorize();
      reader.moveTo((offset + hashOffset) - (this.hashSize * this.nSpecialSlots));
      for (long i = 0L; i < num4; i += 1L)
      {
         byte[] b = new byte[(int) this.hashSize];
         reader.read(b);
         System.arraycopy(b, 0, this.Hashes, (int) (i * this.hashSize), (int) this.hashSize);
      }
      reader.moveBack();
   }

   public int PageSize()
   {
      return (((int) 1) << this.pageSize);
   }
}
