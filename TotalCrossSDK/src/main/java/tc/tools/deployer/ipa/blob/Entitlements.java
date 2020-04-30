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

public class Entitlements extends BlobCore {
  /** https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h */
  public static final long CSMAGIC_EMBEDDED_ENTITLEMENTS = 0xfade7171;

  public Entitlements() {
    super(CSMAGIC_EMBEDDED_ENTITLEMENTS);
  }

  public Entitlements(byte[] data) {
    this();
    this.data = data;
  }
}
