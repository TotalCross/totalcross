// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.crypto.digest;

import java.security.MessageDigest;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.crypto.NoSuchAlgorithmException;

/**
 * This class implements the SHA-256 message digest algorithm.
 */
public class SHA256Digest extends Digest {
  /**
   * Creates a new SHA256Digest object.
   * 
   * @throws NoSuchAlgorithmException If no Provider supports a <code>MessageDigestSpi</code> implementation for the specified algorithm.
   */
  public SHA256Digest() throws NoSuchAlgorithmException {
    init();
  }

  /**
   * Returns the name of the algorithm.
   * 
   * @return "SHA-256".
   */
  @Override
  public final String getAlgorithm() {
    return "SHA-256";
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
   * @return 32.
   */
  @Override
  public final int getDigestLength() {
    return 32;
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
      digestRef = MessageDigest.getInstance("SHA-256");
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new NoSuchAlgorithmException(e.getMessage());
    }
  }
}
