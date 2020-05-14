// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
 * A wrapper around the unmanaged interface to give a semi-decent Java API
 */

package totalcross.net.ssl;

/**
 * A holder for data read in an SSL read.
 */
public class SSLReadHolder {
  /**
   * Contruct a new read holder object.
   */
  public SSLReadHolder() {
    m_buf = null;
  }

  /**
   * Retrieve the reference to the read data.
   */
  public byte[] getData() {
    return m_buf;
  }

  protected byte[] m_buf;
}
