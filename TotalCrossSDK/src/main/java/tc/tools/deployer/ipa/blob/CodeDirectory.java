package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

/**
 * http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-32568/lib/codedirectory.h
 */
public class CodeDirectory extends BlobCore {
  /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
  public static final long CSMAGIC_CODEDIRECTORY = 0xfade0c02;

  private static final int VERSION_1 = 0x20100;
  private static final int VERSION_2 = 0x20200;
  private static final int VERSION_3 = 0x20300;
  private static final int VERSION_4 = 0x20400;
  
  /**
   * Types of hashes supported.
   */
  public static final int cdHashTypeSHA1 = 1;
  public static final int cdHashTypeSHA256 = 2;

  /**
   * Special hash slot values. In a CodeDirectory, these show up at negative slot indices. This enumeration is also
   * used widely in various internal APIs, and as type values in embedded SuperBlobs.
   */
  public static final int cdInfoSlot = 1; // Info.plist
  public static final int cdRequirementsSlot = 2; // internal requirements
  public static final int cdResourceDirSlot = 3; // resource directory
  public static final int cdApplicationSlot = 4; // Application specific slot
  public static final int cdEntitlementsSlot = 5; // entitlements
  public static final int cdSlotMax = 5;

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

  /* Version 0x20100 */
  public long scatterOffset = 0; /* offset of optional scatter vector */

  /* Version 0x20200 */
  public long teamIdOffset = 0; /* offset of optional team identifier */
      
  /* Version 0x20300 */
  public long spare3;                /* unused (must be zero) */
  public long codeLimit64;            /* limit to main image signature range, 64 bits */
      
  /* Version 0x20400 */
  public long execSegBase;            /* offset of executable segment */
  public long execSegLimit;            /* limit of executable segment */
  public long execSegFlags;            /* executable segment flags */

  public String identifier;
  public String teamId;
  public int actualPageSize;
  public byte[] hashes;

  protected GeneralDigest hashDigest/*  = new SHA1Digest() */;

  enum HashType {
    NOHASH(null),
    SHA1(new SHA1Digest()),
    SHA256(new SHA256Digest());
    // HASHTYPE_SHA256           hashType = 2
    // HASHTYPE_SHA256_TRUNCATED hashType = 3
    // HASHTYPE_SHA384           hashType = 4
    // HASHTYPE_SHA512           hashType = 5

    public final GeneralDigest digest;

    HashType(GeneralDigest digest) {
      this.digest = digest;
    }

    public void update(byte[] input, int inputOffset, int length, byte[] output, int outputOffset) {
      if(digest != null) {
        digest.reset();
        digest.update(input, inputOffset, length);
        digest.doFinal(output, outputOffset);
      }
    }
  }

  public CodeDirectory() {
    super(CSMAGIC_CODEDIRECTORY);
  }

  /**
   * We no longer create code directories from scratch, now we just update
   * the original one.
   * 
   * @param identifier
   * @param codeLimit
   */
  @Deprecated
  public CodeDirectory(String identifier, long codeLimit) {
    super(CSMAGIC_CODEDIRECTORY);
    this.scatterOffset = 0;
    this.spare2 = 0;
    this.pageSize = 12;
    this.actualPageSize = 1 << this.pageSize;
    this.spare1 = 0;
    this.hashType = cdHashTypeSHA1;
    this.hashSize = (byte) this.hashDigest.getDigestSize();
    this.codeLimit = codeLimit;
    this.nCodeSlots = (long) ((this.codeLimit + actualPageSize - 1) / actualPageSize);
    this.nSpecialSlots = cdSlotMax;
    this.flags = 0;
    this.version = VERSION_1;

    this.identifier = identifier;
    this.hashes = new byte[(int) ((this.nSpecialSlots + this.nCodeSlots) * this.hashSize)];
  }

  public void setSpecialSlotsHashes(byte[] info, byte[] requirements, byte[] resourceDir, byte[] application,
      byte[] entitlements) {
    setSpecialSlotHash(cdInfoSlot, info);
    setSpecialSlotHash(cdRequirementsSlot, requirements);
    setSpecialSlotHash(cdResourceDirSlot, resourceDir);
    setSpecialSlotHash(cdApplicationSlot, application);
    setSpecialSlotHash(cdEntitlementsSlot, entitlements);
  }

  public void setSpecialSlotHash(int slotIndex, byte[] data) {
    int startIndex = (int) ((this.nSpecialSlots - slotIndex) * this.hashSize);
    if (data == null) {
      Arrays.fill(this.hashes, startIndex, startIndex + this.hashSize, (byte) 0);
    } else {
      HashType.values()[hashType].update(data, 0, data.length, this.hashes, startIndex);
    }
  }

  public void setCodeSlotsHashes(byte[] data) {
    for (int i = 0; i < this.nCodeSlots; i++) {
      int offset = i * actualPageSize;
      int pageSize = Math.min((int) (this.codeLimit - offset), actualPageSize);
      HashType.values()[hashType].update(data, offset, pageSize, this.hashes, (int) ((this.nSpecialSlots + i) * this.hashSize));
    }
  }

  @Override
  protected void writeToStream(ElephantMemoryWriter writer) throws IOException {
    // identOffset starts after the header
    this.identOffset = (writer.pos - offset) + Integer.SIZE * 8 + Byte.SIZE + 4;
    
    // The implicit fallthrough is intended
    switch ((int) version) {
      case VERSION_4: this.identOffset += 3 * Long.SIZE;
      case VERSION_3: this.identOffset += Long.SIZE + Integer.SIZE;
      case VERSION_2: this.identOffset += Integer.SIZE;
      case VERSION_1: this.identOffset += Integer.SIZE;
      break;
    }
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

    if (this.version >= VERSION_1) {
      writer.writeUnsignedInt(0); // scatter is being ignored
    }
    if (this.version >= VERSION_2) {
      this.teamIdOffset = this.hashOffset + (this.hashSize * this.nCodeSlots);
      writer.writeUnsignedInt(this.teamIdOffset);
    }
    if (this.version >= VERSION_3) {
      writer.writeUnsignedInt(this.spare3);
      writer.writeUnsignedLong(this.codeLimit64);
    }
    if (this.version >= VERSION_4) {
      writer.writeUnsignedLong(this.execSegBase);
      writer.writeUnsignedLong(this.execSegLimit);
      writer.writeUnsignedLong(this.execSegFlags);
    }

    writer.write(this.identifier.getBytes("US-ASCII"));
    writer.write((byte) 0); // write string delimiter
    writer.write(this.hashes);
    if (this.teamIdOffset > 0) {
      writer.write(this.teamId.getBytes("US-ASCII"));
      writer.write((byte) 0); // write string delimiter
    }
  }

  @Override
  protected void readFromStream(ElephantMemoryReader reader) throws IOException {
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
    this.actualPageSize = 1 << this.pageSize;
    this.spare2 = reader.readUnsignedInt();
    if (this.version >= VERSION_1) {
      this.scatterOffset = reader.readUnsignedInt();
    }
    if (this.version >= VERSION_2) {
      this.teamIdOffset = reader.readUnsignedInt();
    }
    if (this.version >= VERSION_3) {
      this.spare3 = reader.readUnsignedInt();
      this.codeLimit64 = reader.readUnsignedLong();
    }
    if (this.version >= VERSION_4) {
      this.execSegBase = reader.readUnsignedLong();
      this.execSegLimit = reader.readUnsignedLong();
      this.execSegFlags = reader.readUnsignedLong();
    }

    reader.memorize();
    reader.moveTo(offset + identOffset);
    this.identifier = reader.readString();
    if (this.teamIdOffset > 0) {
      reader.moveTo(offset + teamIdOffset);
      this.teamId = reader.readString();
    }
    reader.moveBack();
    long num4 = this.nSpecialSlots + this.nCodeSlots;
    this.hashes = new byte[(int) (num4 * this.hashSize)];
    reader.memorize();
    reader.moveTo((offset + hashOffset) - (this.hashSize * this.nSpecialSlots));
    for (long i = 0L; i < num4; i += 1L) {
      byte[] b = new byte[(int) this.hashSize];
      reader.read(b);
      System.arraycopy(b, 0, this.hashes, (int) (i * this.hashSize), (int) this.hashSize);
    }
    reader.moveBack();
  }
}
