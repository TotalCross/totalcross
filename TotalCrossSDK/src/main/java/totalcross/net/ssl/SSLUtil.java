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

/*
 * A wrapper around the unmanaged interface to give a semi-decent Java API
 */

package totalcross.net.ssl;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.sys.Vm;

/**
 * Some global helper functions.
 */
public class SSLUtil {
  public static final int CONFIG_SSL_MAX_CERTS = 16;
  public static final int CONFIG_X509_MAX_CA_CERTS = 128;

  @ReplacedByNativeOnDeploy
  public static int getConfig(int which) {
    switch (which) {
    /* return the appropriate build mode */
    case Constants.SSL_BUILD_MODE:
      return Constants.SSL_BUILD_FULL_MODE;
    case Constants.SSL_MAX_CERT_CFG_OFFSET:
      return CONFIG_SSL_MAX_CERTS;
    case Constants.SSL_MAX_CA_CERT_CFG_OFFSET:
      return CONFIG_X509_MAX_CA_CERTS;
    case Constants.SSL_HAS_PEM:
      return 1;
    }
    return -1;
  }

  /**
   * Return the build mode of the axTLS project.
   * @return The build mode is one of:
   * - SSL_BUILD_SERVER_ONLY
   * - SSL_BUILD_ENABLE_VERIFICATION
   * - SSL_BUILD_ENABLE_CLIENT
   * - SSL_BUILD_FULL_MODE
   */
  public static int buildMode() {
    return getConfig(Constants.SSL_BUILD_MODE);
  }

  /**
   * Return the number of chained certificates that the client/server
   * supports.
   * @return The number of supported client/server certificates.
   */
  public static int maxCerts() {
    return getConfig(Constants.SSL_MAX_CERT_CFG_OFFSET);
  }

  /**
   * Return the number of CA certificates that the client/server
   * supports.
   * @return The number of supported CA certificates.
   */
  public static int maxCACerts() {
    return getConfig(Constants.SSL_MAX_CA_CERT_CFG_OFFSET);
  }

  /**
   * Indicate if PEM is supported.
   * @return true if PEM supported.
   */
  public static boolean hasPEM() {
    return getConfig(Constants.SSL_HAS_PEM) > 0;
  }

  /**
   * Display the text string of the error.
   * See ssl.h for the error code list.
   * @param error_code [in] The integer error code.
   */
  @ReplacedByNativeOnDeploy
  public static void displayError(int error_code) {
    Vm.debug("ssl error: " + error_code);
  }

  /**
   * Return the version of the axTLS project.
   */
  @ReplacedByNativeOnDeploy
  public static String version() {
    return "1.0/1.1.5";
  }
}
