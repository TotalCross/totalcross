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
package totalcross.crypto.digest;

import java.security.MessageDigest;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.crypto.NoSuchAlgorithmException;

/**
 * This class implements the MD5 message digest algorithm.
 */
public class MD5Digest extends Digest {
  /**
   * Creates a new MD5Digest object.
   * 
   * @throws NoSuchAlgorithmException If no Provider supports a <code>MessageDigestSpi</code> implementation for the specified algorithm.
   */
  public MD5Digest() throws NoSuchAlgorithmException {
    init();
  }

  /**
   * Returns the name of the algorithm.
   * 
   * @return "MD5".
   */
  @Override
  public final String getAlgorithm() {
    return "MD5";
  }

  /**
   * Returns the block length.
   * 
   * @return 64.
   */
  @Override
  public final int getBlockLength() {
    return 64;
  }

  /**
   * Returns the message digest length.
   * 
   * @return 16.
   */
  @Override
  public final int getDigestLength() {
    return 16;
  }

  @Override
  @ReplacedByNativeOnDeploy
  protected final byte[] process(byte[] data) {
    MessageDigest digest = (MessageDigest) digestRef;
    digest.reset();
    digest.update(data);

    return ((MessageDigest) digestRef).digest();
  }

  @ReplacedByNativeOnDeploy
  private void init() throws NoSuchAlgorithmException {
    try {
      digestRef = MessageDigest.getInstance("MD5");
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    }
  }
}
