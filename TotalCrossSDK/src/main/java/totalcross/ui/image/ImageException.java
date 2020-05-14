// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** This class is used to throw an exception when an image could not be loaded for any reason.
 * @since SuperWaba 5.8.
 */

public class ImageException extends Exception {
  public ImageException(String msg) {
    super(msg);
  }
}
