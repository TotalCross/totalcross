// Copyright (C) 2012-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

public class FatBinaryEntry {
  protected long cputype;
  protected long cpusubtype;
  protected long offset;
  protected long size;
  protected long alignment;
  protected AppleBinary file;

  public FatBinaryEntry(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException {
    cputype = reader.readUnsignedInt();
    cpusubtype = reader.readUnsignedInt();
    offset = reader.readUnsignedInt();
    size = reader.readUnsignedInt();
    alignment = reader.readUnsignedInt();

    file = AppleBinary.create(reader.copy((int) offset, (int) (offset + size)));
  }

  public void resign(ElephantMemoryWriter writer, KeyStore ks, Store<X509CertificateHolder> certStore, String bundleIdentifier,
      byte[] entitlementsBytes, byte[] info, byte[] sourceData)
      throws UnrecoverableKeyException, CertificateEncodingException, KeyStoreException, NoSuchAlgorithmException,
      OperatorCreationException, IOException, CMSException {
    writer.align((int) offset);
    byte[] resignedFile = file.resign(ks, certStore, bundleIdentifier, entitlementsBytes, info, sourceData);
    size = resignedFile.length;
    writer.write(resignedFile);
  }

  public void writeHeader(ElephantMemoryWriter writer) throws IOException {
    writer.writeUnsignedInt(cputype);
    writer.writeUnsignedInt(cpusubtype);
    writer.writeUnsignedInt(offset);
    writer.writeUnsignedInt(size);
    writer.writeUnsignedInt(alignment);
  }
}
