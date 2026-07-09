// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.io.ByteArrayStream;
import totalcross.util.zip.CRC32;
import totalcross.util.zip.CompressedStream;
import totalcross.util.zip.ZipEntry;
import totalcross.util.zip.ZipStream;

public class ZipStreamSmokeTest extends FeatureSmokeTest {
  private static final byte[] DEFLATED = new byte[] { 100, 101, 102, 108, 97, 116, 101, 100 };
  private static final byte[] STORED = new byte[] { 115, 116, 111, 114, 101, 100 };

  public ZipStreamSmokeTest() {
    super("minizip-ng");
  }

  @Override
  public void initUI() {
    try {
      ByteArrayStream archive = new ByteArrayStream(128);
      writeArchive(archive);
      archive.mark();
      readArchive(archive);
      pass("ZIP DEFLATE and STORED stream round trip");
    } catch (Exception e) {
      throw new RuntimeException("minizip-ng smoke failed", e);
    }
  }

  private void writeArchive(ByteArrayStream archive) throws Exception {
    ZipStream output = new ZipStream(archive, CompressedStream.DEFLATE);
    writeEntry(output, "deflated.txt", DEFLATED, false);
    writeEntry(output, "stored.txt", STORED, true);
    output.close();
  }

  private void writeEntry(ZipStream output, String name, byte[] content, boolean stored) throws Exception {
    ZipEntry entry = new ZipEntry(name);
    if (stored) {
      CRC32 crc = new CRC32();
      crc.update(content);
      entry.setMethod(ZipStream.STORED);
      entry.setSize(content.length);
      entry.setCompressedSize(content.length);
      entry.setCrc(crc.getValue());
    }
    output.putNextEntry(entry);
    output.writeBytes(content, 0, content.length);
    output.closeEntry();
  }

  private void readArchive(ByteArrayStream archive) throws Exception {
    ZipStream input = new ZipStream(archive, CompressedStream.INFLATE);
    assertEntry(input, "deflated.txt", DEFLATED);
    assertEntry(input, "stored.txt", STORED);
    if (input.getNextEntry() != null) {
      throw new RuntimeException("unexpected ZIP entry");
    }
    input.close();
  }

  private void assertEntry(ZipStream input, String expectedName, byte[] expectedContent) throws Exception {
    ZipEntry entry = input.getNextEntry();
    if (entry == null || !expectedName.equals(entry.getName())) {
      throw new RuntimeException("unexpected ZIP entry name");
    }

    byte[] actualContent = new byte[expectedContent.length];
    int count = input.readBytes(actualContent, 0, actualContent.length);
    if (count != expectedContent.length) {
      throw new RuntimeException("unexpected ZIP entry length");
    }
    for (int i = 0; i < expectedContent.length; i++) {
      if (actualContent[i] != expectedContent[i]) {
        throw new RuntimeException("unexpected ZIP entry content");
      }
    }
    if (input.readBytes(actualContent, 0, actualContent.length) != -1) {
      throw new RuntimeException("ZIP entry did not reach EOF");
    }
    input.closeEntry();
  }
}
