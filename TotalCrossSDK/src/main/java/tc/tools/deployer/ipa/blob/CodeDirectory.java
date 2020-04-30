// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;

import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

/**
 * http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-32568/lib/codedirectory.h
 */
public class CodeDirectory extends BlobCore {
  /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
  public static final long CSMAGIC_CODEDIRECTORY = 0xfade0c02;

  /**
   * Types of hashes supported. Actually, right now, only SHA1 is really supported.
   */
  public static final int cdHashTypeSHA1 = 1;
  public static final int cdHashTypeSHA256 = 1;

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
  public long scatterOffset;

  public String identifier;
  public int actualPageSize;
  public byte[] hashes;

  protected GeneralDigest hashDigest = new SHA1Digest();

  public CodeDirectory() {
    super(CSMAGIC_CODEDIRECTORY);
  }

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
    this.version = 0x20100;

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
      hashDigest.reset();
      hashDigest.update(data, 0, data.length);
      hashDigest.doFinal(this.hashes, startIndex);
    }
  }

  public void setCodeSlotsHashes(byte[] data) {
    for (int i = 0; i < this.nCodeSlots; i++) {
      int offset = i * actualPageSize;
      int pageSize = Math.min((int) (this.codeLimit - offset), actualPageSize);
      hashDigest.reset();
      hashDigest.update(data, offset, pageSize);
      hashDigest.doFinal(this.hashes, (int) ((this.nSpecialSlots + i) * this.hashSize));
    }
  }

  @Override
  protected void writeToStream(ElephantMemoryWriter writer) throws IOException {
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
    this.scatterOffset = reader.readUnsignedInt();

    reader.memorize();
    reader.moveTo(offset + identOffset);
    this.identifier = reader.readString();
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
