// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.Stream;

public class GZipStream4D extends CompressedStream4D {
  public GZipStream4D(Stream stream, int mode) throws IOException {
    super(stream, mode, GZIP_COMPRESSION); //flsobral@tc114_82: Subclasses of CompressedStream4D must now inform the type of compression to be used by the ZLib library.
  }
}
