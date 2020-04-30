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

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.X509Store;

/**
 * Abstract class that represents an apple binary, currently supports ARM, ARM64 and FAT (Universal) binaries.
 * Subclasses should not be instantiated directly, use instead the method {@link #create(byte[])}.
 * 
 * @author Fabio Sobral
 */
public abstract class AppleBinary {
  enum Type {
    MH_MAGIC(0xFEEDFACEL), MH_CIGAM(0xCEFAEDFEL), MH_MAGIC_64(0xFEEDFACFL), MH_CIGAM_64(0xCFFAEDFEL), FAT_MAGIC(
        0xCAFEBABEL), FAT_CIGAM(0xBEBAFECAL);

    private final long magic;

    Type(long magic) {
      this.magic = magic;
    }

    public static Type valueOf(long magic) {
      for (Type type : Type.values()) {
        if (type.magic == magic) {
          return type;
        }
      }
      throw new RuntimeException();
    }
  }

  protected Type type;
  protected byte[] data;

  public static AppleBinary create(byte[] data) throws IOException, InstantiationException, IllegalAccessException {
    ElephantMemoryReader reader = new ElephantMemoryReader(data);

    try {
      switch (Type.valueOf(reader.readUnsignedInt())) {
      case MH_CIGAM:
        return new MachObjectFile(data);
      case MH_CIGAM_64:
        return new MachObjectFile64(data);
      case FAT_MAGIC:
        return new FatBinary(data);
      default:
        throw new RuntimeException();
      }
    } finally {
      reader.close();
    }
  }

  protected AppleBinary(byte[] data) {
    this.data = data;
  }

  abstract public byte[] resign(KeyStore ks, X509Store certStore, String bundleIdentifier, byte[] entitlementsBytes,
      byte[] info, byte[] sourceData) throws IOException, CMSException, UnrecoverableKeyException,
      CertificateEncodingException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException;
}
