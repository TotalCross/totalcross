// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.zip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that acts as a null device: discards any written data but always reports success.<br>
 * Originally named as NullOutputStream, but renamed to not be confused with the Apache class that writes to /dev/null,
 * which essentially is the same thing but I'm a nitpicker. :)
 * 
 * @author Fabio Sobral
 * 
 */
public class BlackHoleOutputStream extends OutputStream {
  public BlackHoleOutputStream() {
    super();
  }

  @Override
  public void write(int b) throws IOException {
    // do nothing
  }
}
