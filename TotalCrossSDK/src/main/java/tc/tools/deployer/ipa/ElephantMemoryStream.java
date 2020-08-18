// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

public interface ElephantMemoryStream {
  public abstract int getPos();

  public abstract void moveTo(int newPosition);

  public abstract void memorize();

  public abstract void moveBack();
}
