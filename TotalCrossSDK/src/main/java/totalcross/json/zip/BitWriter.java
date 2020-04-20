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
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.json.zip;

import totalcross.io.IOException;

/**
 * A bitwriter is a an interface that allows for doing output at the bit level.
 * Most IO interfaces only allow for writing at the byte level or higher.
 */
public interface BitWriter {

  /**
   * Write a 1 bit.
   *
   * @throws IOException
   */
  public void one() throws IOException;

  /**
   * Pad the rest of the block with zeros and flush.
   *
   * @param width
   *            The size in bits of the block to pad. This will typically be
   *            8, 16, 32, 64, 128, 256, etc.
   * @throws IOException
   */
  public void pad(int width) throws IOException;

  /**
   * Write some bits. Up to 32 bits can be written at a time.
   *
   * @param bits
   *            The bits to be written.
   * @param width
   *            The number of bits to write. (0..32)
   * @throws IOException
   */
  public void write(int bits, int width) throws IOException;

  /**
   * Write a 0 bit.
   *
   * @throws IOException
   */
  public void zero() throws IOException;
}
