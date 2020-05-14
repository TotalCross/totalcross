// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa.blob;

public class Requirements extends SuperBlob {
  /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
  public static final long CSMAGIC_REQUIREMENTS = 0xfade0c01;

  public Requirements() {
    super(CSMAGIC_REQUIREMENTS);
  }
}
