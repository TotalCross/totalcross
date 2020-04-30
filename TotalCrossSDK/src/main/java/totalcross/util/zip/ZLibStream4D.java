// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.zip;

import totalcross.io.Stream;

public class ZLibStream4D extends CompressedStream4D {
  public ZLibStream4D(Stream stream, int mode) {
    super(stream, mode, ZLIB_COMPRESSION); //flsobral@tc114_82: Subclasses of CompressedStream4D must now inform the type of compression to be used by the ZLib library.
  }
}
