// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

public final class TcmFormat {
  public static final byte[] MAGIC = { 'T', 'C', 'M', '1' };
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;
  public static final int REQUIRED = 0x8000;
  public static final int STRING_TABLE = 1;
  public static final int ARTIFACT_MANIFEST = 2;
  public static final int CLASSES = 3;
  public static final int FIELDS = 4;
  public static final int METHODS = 5;
  public static final int CALL_SITES = 6;
  public static final int ORIGIN_MAP = 7;
  public static final int ALLOCATION_AND_SYNTHETIC_ORIGINS = 8;
  public static final int DYNAMIC_ACCESS = 9;
  public static final int TYPE_FRAMES = 10;
  public static final int SECTION_VERSION = 1;
  public static final int NULL_STRING = -1;

  private TcmFormat() {
  }
}
