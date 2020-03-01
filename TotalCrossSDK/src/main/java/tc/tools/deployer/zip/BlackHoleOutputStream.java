/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.              *
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
