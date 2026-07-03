// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java17FeatureSmokeTest extends FeatureSmokeTest {
  public Java17FeatureSmokeTest() {
    super("Java 17");
  }

  @Override
  public void initUI() {
    testSealedClassMetadata();
    finish();
  }

  private void testSealedClassMetadata() {
    JavaRelease release = new LtsRelease(17);
    checkEquals(Integer.valueOf(17), Integer.valueOf(release.version()), "sealed class metadata");
  }

  sealed interface JavaRelease permits LtsRelease {
    int version();
  }

  static final class LtsRelease implements JavaRelease {
    private final int version;

    LtsRelease(int version) {
      this.version = version;
    }

    @Override
    public int version() {
      return version;
    }
  }
}
