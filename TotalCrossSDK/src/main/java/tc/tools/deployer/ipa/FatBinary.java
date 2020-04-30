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

public class FatBinary extends AppleBinary {
  protected long magic;
  private long narchives;

  private List<FatBinaryEntry> entries = new ArrayList<FatBinaryEntry>();

  protected FatBinary(byte[] data) throws IOException, InstantiationException, IllegalAccessException {
    super(data);
    ElephantMemoryReader reader = new ElephantMemoryReader(data);

    this.magic = reader.readUnsignedInt();
    this.narchives = reader.readUnsignedInt();

    for (int i = 0; i < this.narchives; i++) {
      entries.add(new FatBinaryEntry(reader));
    }
    reader.close();
  }

  @Override
  public byte[] resign(KeyStore ks, X509Store certStore, String bundleIdentifier, byte[] entitlementsBytes, byte[] info,
      byte[] sourceData) throws IOException, CMSException, UnrecoverableKeyException, CertificateEncodingException,
      KeyStoreException, NoSuchAlgorithmException, OperatorCreationException {
    ElephantMemoryWriter writer = new ElephantMemoryWriter(this.data.length);
    writer.write(this.data, 0, 8 + (int) this.narchives * 20);

    for (FatBinaryEntry fatBinaryEntry : entries) {
      fatBinaryEntry.resign(writer, ks, certStore, bundleIdentifier, entitlementsBytes, info, sourceData);
    }

    writer.moveTo(8);
    for (FatBinaryEntry fatBinaryEntry : entries) {
      fatBinaryEntry.writeHeader(writer);
    }

    return writer.buffer;
  }
}
