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

   public long version;
   public long flags;
   public long hashOffset;
   public long identOffset;
   public long nSpecialSlots;
   public long nCodeSlots;
   public long codeLimit;
   public byte hashSize;
   public byte hashType;
   public byte spare1;
   public byte pageSize;
   public long spare2;
   public long scatterOffset;

   public String identifier;
   public byte[] hashes;

   protected GeneralDigest hashDigest = new SHA1Digest();

   public CodeDirectory()
   {
      super(CSMAGIC_CODEDIRECTORY);
   }

   public CodeDirectory(String identifier, long codeLimit)
   {
      super(CSMAGIC_CODEDIRECTORY);
      this.scatterOffset = 0;
      this.spare2 = 0;
      this.pageSize = 12;
      this.spare1 = 0;
      this.hashType = 1;
      this.hashSize = (byte) this.hashDigest.getDigestSize();
      this.codeLimit = codeLimit;
      this.nCodeSlots = (long) ((this.codeLimit + actualPageSize() - 1L) / actualPageSize());
      this.nSpecialSlots = 5;
      this.flags = 0;
      this.version = 0x20100;

      this.identifier = identifier;
      this.hashes = new byte[(int) ((this.nSpecialSlots + this.nCodeSlots) * this.hashSize)];
   }

   public void ComputeImageHashes(byte[] SignedFileData)
   {
      for (int i = 0; i < this.nCodeSlots; i++)
      {
         int offset = i * this.actualPageSize();
         int num3 = ((int) this.codeLimit) - offset;
         hashDigest.reset();
         hashDigest.update(SignedFileData, offset, Math.min(num3, this.actualPageSize()));
         hashDigest.doFinal(this.hashes, (int) ((this.nSpecialSlots + i) * this.hashSize));
      }
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex)
   {
      for (int i = 0; i < this.hashSize; i++)
         this.hashes[((5 - SpecialSlotIndex) * this.hashSize) + i] = 0;
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex, byte[] SourceData)
   {
      hashDigest.reset();
      hashDigest.update(SourceData, 0, SourceData.length);
      hashDigest.doFinal(this.hashes, (int) ((this.nSpecialSlots - SpecialSlotIndex) * this.hashSize));
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      // identOffset starts after the next 40 bytes;
      this.identOffset = (writer.pos - offset) + 40;
      // hashOffset starts after the identifier and the hashes of the special slots
      this.hashOffset = this.identOffset + (identifier.length() + 1) + (this.hashSize * this.nSpecialSlots);
      writer.writeUnsignedInt(this.version);
      writer.writeUnsignedInt(this.flags);
      writer.writeUnsignedInt(this.hashOffset);
      writer.writeUnsignedInt(this.identOffset);
      writer.writeUnsignedInt(this.nSpecialSlots);
      writer.writeUnsignedInt(this.nCodeSlots);
      writer.writeUnsignedInt(this.codeLimit);
      writer.write(this.hashSize);
      writer.write(this.hashType);
      writer.write(this.spare1);
      writer.write(this.pageSize);
      writer.writeUnsignedInt(this.spare2);
      writer.writeUnsignedInt(this.scatterOffset);
      writer.write(this.identifier.getBytes("US-ASCII"));
      writer.write((byte) 0); // write string delimiter
      writer.write(this.hashes);
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
      this.identifier = reader.readString();
      reader.moveBack();
      long num4 = this.nSpecialSlots + this.nCodeSlots;
      this.hashes = new byte[(int) (num4 * this.hashSize)];
      reader.memorize();
      reader.moveTo((offset + hashOffset) - (this.hashSize * this.nSpecialSlots));
      for (long i = 0L; i < num4; i += 1L)
      {
         byte[] b = new byte[(int) this.hashSize];
         reader.read(b);
         System.arraycopy(b, 0, this.hashes, (int) (i * this.hashSize), (int) this.hashSize);
      }
      reader.moveBack();
   }

   public int actualPageSize()
   {
      return (1 << this.pageSize);
   }
}
