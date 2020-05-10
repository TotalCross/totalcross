// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.io.device.gps;

/** Exception is thrown when the GPS is disabled. */
public class GPSDisabledException extends totalcross.io.IOException {
  /** Constructs an empty Exception. */
  public GPSDisabledException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public GPSDisabledException(String msg) {
    super(msg);
  }

}
