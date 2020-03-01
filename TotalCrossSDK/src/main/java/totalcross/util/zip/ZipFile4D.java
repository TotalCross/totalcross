/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.util.zip;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.Stream;

public class ZipFile4D {
  private Object nativeFile;
  private String name;
  private int size;

  public ZipFile4D(String name) throws IOException {
    this.name = name;
    File file = new File(name, File.READ_WRITE);
    createZipFile(file);
  }

  native private ZipFile createZipFile(File file) throws IOException;

  native public void close() throws IOException;

  native public ZipEntry[] entries();

  native public String getEntry(String name);

  native public Stream getEntryStream(String name) throws IOException;

  public String getName() {
    return name;
  }

  public int size() {
    return size;
  }

  @Override
  protected void finalize() {
    try {
      if (nativeFile != null) {
        this.close();
      }
    } catch (totalcross.io.IOException e) {
    }
  }
}
