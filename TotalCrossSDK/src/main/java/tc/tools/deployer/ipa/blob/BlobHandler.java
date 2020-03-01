/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

 package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tc.tools.deployer.ipa.ElephantMemoryReader;
import tc.tools.deployer.ipa.ElephantMemoryWriter;

public abstract class BlobHandler {
  private static Map<Long, Class<?>> knownBlobs = new HashMap<Long, Class<?>>();

  static {
    knownBlobs.put(Long.valueOf(EmbeddedSignature.CSMAGIC_EMBEDDED_SIGNATURE), EmbeddedSignature.class);
    knownBlobs.put(Long.valueOf(Requirements.CSMAGIC_REQUIREMENTS), Requirements.class);
    knownBlobs.put(Long.valueOf(Entitlements.CSMAGIC_EMBEDDED_ENTITLEMENTS), Entitlements.class);
    knownBlobs.put(Long.valueOf(BlobWrapper.CSMAGIC_BLOB_WRAPPER), BlobWrapper.class);
    knownBlobs.put(Long.valueOf(CodeDirectory.CSMAGIC_CODEDIRECTORY), CodeDirectory.class);
  }

  public static BlobCore readBlob(ElephantMemoryReader reader)
      throws IOException, InstantiationException, IllegalAccessException {
    BlobCore blob;
    long magic = reader.readUnsignedInt();
    long length = reader.readUnsignedInt();

    Class<?> blobClass = (Class<?>) knownBlobs.get(Long.valueOf((int) magic));
    if (blobClass == null) {
      blob = new BlobCore(magic);
    } else {
      blob = (BlobCore) blobClass.newInstance();
    }
    blob.length = length;
    blob.offset = reader.getPos() - 8L;
    blob.readFromStream(reader);

    return blob;
  }

  public static void writeBlob(BlobCore blob, ElephantMemoryWriter writer) throws IOException {
    blob.offset = writer.pos;
    writer.writeUnsignedInt(blob.magic);
    writer.pos += 4L;
    blob.writeToStream(writer);
    blob.length = writer.pos - blob.offset;
    writer.memorize();
    writer.moveTo(blob.offset + 4L);
    writer.writeUnsignedInt(blob.length);
    writer.moveBack();
  }
}
