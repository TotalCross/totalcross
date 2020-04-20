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

/*
 * A wrapper around the unmanaged interface to give a semi-decent Java API
 */

package totalcross.net.ssl;

import totalcross.io.IOException;
import totalcross.net.Socket;

/**
 * A base object for SSLServer/SSLClient.
 */
public class SSLCTX4D {
  /**
   * A reference to the real client/server context. For internal use only.
   */
  protected long m_ctx;

  boolean dontFinalize; //flsobral@tc114_36: finalize support.

  protected Object nativeHeap;

  protected SSLCTX4D(int options, int num_sessions) {
    create4D(options, num_sessions);
  }

  native void create4D(int options, int num_sessions);

  native public void dispose4D();

  native public SSL find4D(Socket s);

  native public int objLoad4D(int obj_type, totalcross.io.Stream material, String password) throws IOException;

  native public int objLoad4D(int obj_type, byte[] data, int len, String password);

  native public SSL newClient4D(Socket socket, byte[] session_id);

  native public SSL newServer4D(Socket socket);

  @Override
  protected final void finalize() //flsobral@tc114_36: finalize support.
  {
    try {
      if (dontFinalize != true) {
        dispose4D();
      }
    } catch (Throwable t) {
    }
  }
}
