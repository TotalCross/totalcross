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

import totalcross.io.IOException;
import totalcross.net.Socket;

/**
 * A base object for SSLServer/SSLClient.
 */
public class SSLCTX4D
{
   /**
    * A reference to the real client/server context. For internal use only.
    */
   protected long m_ctx;
   
   boolean dontFinalize; //flsobral@tc114_36: finalize support.
   
   protected Object nativeHeap;

   protected SSLCTX4D(int options, int num_sessions)
   {
      create4D(options, num_sessions);
   }

   native void create4D(int options, int num_sessions);

   native public void dispose4D();

   native public SSL find4D(Socket s);

   native public int objLoad4D(int obj_type, totalcross.io.Stream material, String password) throws IOException;

   native public int objLoad4D(int obj_type, byte[] data, int len, String password);


   native public SSL newClient4D(Socket socket, byte[] session_id);

   native public SSL newServer4D(Socket socket);
   
   protected final void finalize() //flsobral@tc114_36: finalize support.
   {
      try
      {
         if (dontFinalize != true)
            dispose4D();
      }
      catch (Throwable t)
      {
      }
   }     
}
