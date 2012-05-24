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

   private byte BytesPerHash;
   public final int cdApplicationSlot = 4;
   public final int cdEntitlementSlot = 5;
   public final int cdInfoSlot = 1;
   public final int cdRequirementsSlot = 2;
   public final int cdResourceDirSlot = 3;
   public final int cdSlotMax = 5;
   private long CodeSlotCount;
   private long Flags;
   private byte[] Hashes;
   protected GeneralDigest HashProvider = new SHA1Digest();
   private byte HashType;
   private String Identifier;
   private byte LogPageSize;
   private long MainImageSignatureLimit;
   private long ScatterCount;
   private byte Spare1;
   private long Spare2;
   private long SpecialSlotCount;
   private long Version;

   public CodeDirectory()
   {
      super(CSMAGIC_CODEDIRECTORY);
   }

   public void Allocate(String ApplicationID, int SignedFileLength)
   {
      this.Identifier = ApplicationID;
      this.Version = 0x20100;
      this.Flags = 0;
      this.Spare1 = 0;
      this.Spare2 = 0;
      this.ScatterCount = 0;
      this.LogPageSize = 12;
      int num = ((int) 1) << this.LogPageSize;
      this.HashType = 1;
      this.BytesPerHash = (byte) this.HashProvider.getDigestSize();
      this.MainImageSignatureLimit = (long) SignedFileLength;
      this.SpecialSlotCount = 5;
      this.CodeSlotCount = (long) (((this.MainImageSignatureLimit + num) - ((long) 1L)) / ((long) num));
      this.Hashes = new byte[(int) ((this.SpecialSlotCount + this.CodeSlotCount) * this.BytesPerHash)];
   }

   public void ComputeImageHashes(byte[] SignedFileData)
   {
      for (int i = 0; i < this.CodeSlotCount; i++)
      {
         int offset = i * this.PageSize();
         int num3 = ((int) this.MainImageSignatureLimit) - offset;
         HashProvider.reset();
         HashProvider.update(SignedFileData, offset, Math.min(num3, this.PageSize()));
         HashProvider.doFinal(this.Hashes, (int) ((this.SpecialSlotCount + i) * this.BytesPerHash));
      }
   }

   public static CodeDirectory Create(String ApplicationID, int SignedFileLength)
   {
      CodeDirectory blob = new CodeDirectory();
      blob.Allocate(ApplicationID, SignedFileLength);
      return blob;
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex)
   {
      for (int i = 0; i < this.BytesPerHash; i++)
         this.Hashes[((5 - SpecialSlotIndex) * this.BytesPerHash) + i] = 0;
   }

   public void GenerateSpecialSlotHash(int SpecialSlotIndex, byte[] SourceData)
   {
      HashProvider.reset();
      HashProvider.update(SourceData, 0, SourceData.length);
      HashProvider.doFinal(this.Hashes, (int) ((this.SpecialSlotCount - SpecialSlotIndex) * this.BytesPerHash));
   }

   protected void PackageData(ElephantMemoryWriter writer) throws IOException
   {
      writer.writeUnsignedInt(this.Version);
      writer.writeUnsignedInt(this.Flags);
      ReserveSpaceToWriteOffset1(writer, offset - (this.BytesPerHash * this.SpecialSlotCount));
      ReserveSpaceToWriteOffset2(writer, offset);
      writer.writeUnsignedInt(this.SpecialSlotCount);
      writer.writeUnsignedInt(this.CodeSlotCount);
      writer.writeUnsignedInt(this.MainImageSignatureLimit);
      writer.write(this.BytesPerHash);
      writer.write(this.HashType);
      writer.write(this.Spare1);
      writer.write(this.LogPageSize);
      writer.writeUnsignedInt(this.Spare2);
      writer.writeUnsignedInt(this.ScatterCount);
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
      this.Version = reader.readUnsignedInt();
      this.Flags = reader.readUnsignedInt();
      long num2 = reader.readUnsignedInt();
      long num3 = reader.readUnsignedInt();
      this.SpecialSlotCount = reader.readUnsignedInt();
      this.CodeSlotCount = reader.readUnsignedInt();
      this.MainImageSignatureLimit = reader.readUnsignedInt();
      this.BytesPerHash = (byte) reader.read();
      this.HashType = (byte) reader.read();
      this.Spare1 = (byte) reader.read();
      this.LogPageSize = (byte) reader.read();
      this.Spare2 = reader.readUnsignedInt();
      this.ScatterCount = reader.readUnsignedInt();
      reader.memorize();
      reader.moveTo(offset + num3);
      this.Identifier = reader.readString();
      reader.moveBack();
      long num4 = this.SpecialSlotCount + this.CodeSlotCount;
      this.Hashes = new byte[(int) (num4 * this.BytesPerHash)];
      reader.memorize();
      reader.moveTo((offset + num2) - (this.BytesPerHash * this.SpecialSlotCount));
      for (long i = 0L; i < num4; i += 1L)
      {
         byte[] b = new byte[(int) this.BytesPerHash];
         reader.read(b);
         System.arraycopy(b, 0, this.Hashes, (int) (i * this.BytesPerHash), (int) this.BytesPerHash);
      }
      reader.moveBack();
   }

   public int PageSize()
   {
      return (((int) 1) << this.LogPageSize);
   }
}
