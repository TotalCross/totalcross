// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.sync;

import totalcross.io.DataStream;

/** This class represent a Remote PDBFile Record. */

public abstract class RemotePDBRecord {
  protected RemotePDBFile rc;
  protected int size;

  protected abstract void write(DataStream ds);

  protected abstract void read(DataStream ds) throws totalcross.io.IOException;
}
