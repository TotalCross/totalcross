/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



/*
 *  Copyright(C) 2006 Cameron Rich
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * A wrapper around the unmanaged interface to give a semi-decent Java API
 */

package totalcross.net.ssl;

import totalcross.sys.Vm;

/**
 * Some global helper functions.
 */
public class SSLUtil
{
   public static final int CONFIG_SSL_MAX_CERTS = 16;
   public static final int CONFIG_X509_MAX_CA_CERTS = 128;

   public static int getConfig(int which)
   {
      switch (which)
      {
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
   native public static int getConfig4D(int which);

   /**
    * Return the build mode of the axTLS project.
    * @return The build mode is one of:
    * - SSL_BUILD_SERVER_ONLY
    * - SSL_BUILD_ENABLE_VERIFICATION
    * - SSL_BUILD_ENABLE_CLIENT
    * - SSL_BUILD_FULL_MODE
    */
   public static int buildMode()
   {
      return getConfig(Constants.SSL_BUILD_MODE);
   }

   /**
    * Return the number of chained certificates that the client/server
    * supports.
    * @return The number of supported client/server certificates.
    */
   public static int maxCerts()
   {
      return getConfig(Constants.SSL_MAX_CERT_CFG_OFFSET);
   }

   /**
    * Return the number of CA certificates that the client/server
    * supports.
    * @return The number of supported CA certificates.
    */
   public static int maxCACerts()
   {
      return getConfig(Constants.SSL_MAX_CA_CERT_CFG_OFFSET);
   }

   /**
    * Indicate if PEM is supported.
    * @return true if PEM supported.
    */
   public static boolean hasPEM()
   {
      return getConfig(Constants.SSL_HAS_PEM) > 0;
   }

   /**
    * Display the text string of the error.
    * See ssl.h for the error code list.
    * @param error_code [in] The integer error code.
    */
    public static void displayError(int error_code)
    {
       Vm.debug("ssl error: " + error_code);
    }
    native public static void displayError4D(int error_code);

   /**
    * Return the version of the axTLS project.
    */
    public static String version()
    {
       return "1.0/1.1.5";
    }
    native public static String version4D();
}
