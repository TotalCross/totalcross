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

package tc.tools.deployer.ipa;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.X509Store;

import tc.tools.deployer.ipa.blob.BlobWrapper;
import tc.tools.deployer.ipa.blob.CodeDirectory;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;
import tc.tools.deployer.ipa.blob.Entitlements;
import tc.tools.deployer.ipa.blob.Requirements;

/**
 * http://llvm.org/docs/doxygen/html/MachOFormat_8h_source.html
 * http://comments.gmane.org/gmane.comp.programming.garbage-collection.boehmgc/4987
 */
public class MachObjectFile extends AppleBinary {
  protected long magic;
  protected long cputype;
  protected long cpusubtype;
  protected long filetype;
  protected long ncmds;
  protected long sizeofcmds;
  protected long flags;

  private List<MachLoadCommand> commands = new ArrayList<MachLoadCommand>();

  private MachLoadCommandCodeSignature lc_signature = null;
  private MachLoadCommandSegment lc_segment = null;

  private byte[] signatureTemplate;

  protected MachObjectFile(byte[] data) throws IOException, InstantiationException, IllegalAccessException {
    super(data);
    ElephantMemoryReader reader = new ElephantMemoryReader(data);

    this.readHeader(reader);
    this.commands.clear();
    for (int i = 0; i < ncmds; i++) {
      MachLoadCommand command = MachLoadCommand.readFromStream(reader);
      if (command != null) {
        this.commands.add(command);
        if (lc_signature == null && command instanceof MachLoadCommandCodeSignature) {
          lc_signature = (MachLoadCommandCodeSignature) command;
        }
        if (lc_segment == null && command instanceof MachLoadCommandSegment) {
          lc_segment = (MachLoadCommandSegment) command;
          if (!lc_segment.segname.startsWith("__LINKEDIT")) {
            lc_segment = null;
          }
        }
      }
    }
    reader.close();

    if (lc_segment == null || lc_signature == null
        || (lc_signature.blobFileOffset + lc_signature.blobFileSize) != (lc_segment.fileoff + lc_segment.filesize)) {
      throw new RuntimeException("Template IPA files appears to be corrupted, please reinstall the SDK and try again");
    }
  }

  protected void readHeader(ElephantMemoryReader reader) throws IOException {
    this.magic = reader.readUnsignedIntLE();
    this.cputype = reader.readUnsignedIntLE();
    this.cpusubtype = reader.readUnsignedIntLE();
    this.filetype = reader.readUnsignedIntLE();
    this.ncmds = reader.readUnsignedIntLE();
    this.sizeofcmds = reader.readUnsignedIntLE();
    this.flags = reader.readUnsignedIntLE();
  }

  public EmbeddedSignature getEmbeddedSignature() {
    return lc_signature.signature;
  }

  public void setEmbeddedSignature(EmbeddedSignature signature) throws IOException {
    ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
    signatureTemplate = signature.getBytes();

    lc_signature.signature = signature;
    // original size - size of the original signature + size of the new signature
    lc_segment.updateFileSize(writer, lc_segment.filesize - lc_signature.blobFileSize + signatureTemplate.length);
    lc_signature.updateFileSize(writer, signatureTemplate.length);

    this.data = writer.buffer;
  }

  @Override
  public byte[] resign(KeyStore ks, X509Store certStore, String bundleIdentifier, byte[] entitlementsBytes, byte[] info,
      byte[] sourceData) throws IOException, CMSException, UnrecoverableKeyException, CertificateEncodingException,
      KeyStoreException, NoSuchAlgorithmException, OperatorCreationException {
    // create a new codeDirectory with the new identifier, but keeping the same codeLimit
    CodeDirectory codeDirectory = new CodeDirectory(bundleIdentifier, lc_signature.signature.codeDirectory.codeLimit);
    // now create brand new entitlements and requirements
    Entitlements entitlements = new Entitlements(entitlementsBytes);
    Requirements requirements = new Requirements();

    // now create the blob wrapper
    BlobWrapper blobWrapper = new BlobWrapper(ks, certStore, codeDirectory);

    // finally create the template of our new signature
    EmbeddedSignature newSignature = new EmbeddedSignature(codeDirectory, entitlements, requirements, blobWrapper);

    // add the new signature to the file
    this.setEmbeddedSignature(newSignature);

    // recalculate hashes
    codeDirectory.setSpecialSlotsHashes(info, requirements.getBytes(), sourceData, null, entitlements.getBytes());
    codeDirectory.setCodeSlotsHashes(this.data);

    lc_signature.signature.sign();
    byte[] resignedData = lc_signature.signature.getBytes();
    if (signatureTemplate.length != resignedData.length) {
      throw new IllegalStateException("Failed to resign the file, please try again.");
    }

    ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
    writer.memorize();
    writer.moveTo(lc_signature.blobFileOffset);
    writer.write(resignedData);
    writer.moveBack();

    int actualSize = writer.size();
    int expectedSize = (int) (lc_segment.filesize + lc_segment.fileoff);
    if (actualSize < expectedSize) {
      throw new IllegalStateException("Generated file appears to be missing data, please try again.");
    } else if (actualSize == expectedSize) {
      this.data = writer.buffer;
    } else {
      this.data = new byte[expectedSize];
      System.arraycopy(writer.buffer, 0, this.data, 0, expectedSize);
    }

    return this.data;
  }
}
