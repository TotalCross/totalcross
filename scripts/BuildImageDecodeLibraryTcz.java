// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tc.tools.converter.Storage;
import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.util.Vector;
import totalcross.util.zip.TCZ;

/** Builds one library-only image corpus TCZ through the SDK's TCZ APIs. */
public final class BuildImageDecodeLibraryTcz {
  private static final int EXPECTED_IMAGES = 663;

  private BuildImageDecodeLibraryTcz() {
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      throw new IllegalArgumentException(
          "usage: BuildImageDecodeLibraryTcz <variant-dir> <variant> <output.tcz>");
    }
    Path variantDir = Path.of(args[0]).toAbsolutePath().normalize();
    String variant = args[1];
    Path output = Path.of(args[2]).toAbsolutePath().normalize();
    List<Path> files = new ArrayList<>();
    try (java.util.stream.Stream<Path> paths = Files.walk(variantDir)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jpg"))
          .forEach(files::add);
    }
    files.sort((left, right) -> relativeName(variantDir, left)
        .compareTo(relativeName(variantDir, right)));
    if (files.size() != EXPECTED_IMAGES) {
      throw new IllegalStateException(
          variant + " has " + files.size() + " .jpg-named files, expected " + EXPECTED_IMAGES);
    }

    Vector entries = new Vector(files.size());
    List<String> expectedNames = new ArrayList<>(files.size());
    List<String> expectedHashes = new ArrayList<>(files.size());
    for (Path path : files) {
      byte[] raw = Files.readAllBytes(path);
      String name = "decode/" + variant + "/" + relativeName(variantDir, path);
      ByteArrayStream source = new ByteArrayStream(raw);
      ByteArrayStream compressed = new ByteArrayStream(raw.length + 64);
      Storage.compressAndWrite(source, compressed);
      byte[] compressedBytes = Arrays.copyOf(compressed.getBuffer(), compressed.getPos());
      entries.addElement(new TCZ.Entry(compressedBytes, name, raw.length));
      expectedNames.add(name);
      expectedHashes.add(sha256(raw));
    }

    Files.createDirectories(output.getParent());
    new TCZ(entries, output.toString(), TCZ.ATTR_LIBRARY);
    verify(output, expectedNames, expectedHashes);
    System.out.println("library TCZ passed,variant=" + variant + ",entries=" + files.size());
  }

  private static void verify(Path tczPath, List<String> expectedNames,
      List<String> expectedHashes) throws Exception {
    TCZ tcz = new TCZ(new File(tczPath.toString(), File.READ_ONLY));
    if (tcz.attr != TCZ.ATTR_LIBRARY || tcz.numberOfChunks != EXPECTED_IMAGES) {
      throw new IllegalStateException("TCZ is not a 663-entry library: " + tczPath);
    }
    if (!Arrays.equals(expectedNames.toArray(new String[0]), tcz.names)) {
      throw new IllegalStateException("TCZ resource order or names differ: " + tczPath);
    }
    for (int index = 0; index < expectedNames.size(); index++) {
      ByteArrayStream decoded = new ByteArrayStream(tcz.uncompressedSizes[index]);
      tcz.readNextChunk(decoded);
      byte[] bytes = Arrays.copyOf(decoded.getBuffer(), decoded.getPos());
      if (bytes.length != tcz.uncompressedSizes[index]
          || !expectedHashes.get(index).equals(sha256(bytes))) {
        throw new IllegalStateException("TCZ resource failed round-trip: "
            + expectedNames.get(index));
      }
    }
  }

  private static String relativeName(Path root, Path path) {
    return root.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
  }

  private static String sha256(byte[] bytes) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder value = new StringBuilder(digest.length * 2);
    for (byte item : digest) {
      value.append(String.format("%02x", item & 0xff));
    }
    return value.toString();
  }
}
