// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for ESC/POS printable objects.
 *
 * <p>A printable object is usually some sort of graphical data (image, bar code or QR code) that
 * have its own set of ESC/POS configuration commands.
 *
 * <p>
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public interface EscPosPrintObject {

  /**
   * Writes this printable object to the given output stream.
   *
   * @param out the output stream
   * @throws IOException if an I/O error occurs.
   */
  void write(OutputStream out) throws IOException;
}
