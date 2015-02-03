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

import totalcross.crypto.*;
import totalcross.io.*;
import totalcross.net.Socket;

/**
 * The server context.
 * All server connections are started within a server context.
 */
public class SSLServer extends SSLCTX
{
   /**
    * Start a new server context.
    * @throws NoSuchAlgorithmException 
    * @see SSLCTX for details.
    */
   public SSLServer(int options, int num_sessions) throws NoSuchAlgorithmException
   {
      super(options, num_sessions);
   }

   /**
    * Establish a new SSL connection to an SSL client.
    * It is up to the application to establish the initial socket connection.
    *
    * Call dispose() when the connection is to be removed.
    *
    * @param socket [in] A reference to a totalcross.net.Socket.
    * @return An SSL object reference.
    * @throws IOException 
    * @throws CryptoException 
    * @throws NoSuchAlgorithmException 
    */
   public SSL connect(Socket socket) throws IOException, NoSuchAlgorithmException, CryptoException
   {
      return newServer(socket);
   }
}
