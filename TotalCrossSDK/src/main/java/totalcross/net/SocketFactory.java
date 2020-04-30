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

package totalcross.net;

import totalcross.io.IOException;

/**
 * This class creates sockets. It may be subclassed by other factories, which create particular subclasses of sockets
 * and thus provide a general framework for the addition of public socket-level functionality.
 * 
 * Socket factories are a simple way to capture a variety of policies related to the sockets being constructed,
 * producing such sockets in a way which does not require special configuration of the code which asks for the sockets:
 * 
 * <UL>
 * <LI>Due to polymorphism of both factories and sockets, different kinds of sockets can be used by the same application
 * code just by passing it different kinds of factories.
 * 
 * <LI>Factories can themselves be customized with parameters used in socket construction. So for example, factories
 * could be customized to return sockets with different networking timeouts or security parameters already configured.
 * 
 * <LI>The sockets returned to the application can be subclasses of java.net.Socket, so that they can directly expose
 * new APIs for features such as compression, security, record marking, statistics collection, or firewall tunneling.
 * </UL>
 * 
 * Factory classes are specified by environment-specific configuration mechanisms. For example, the getDefault method
 * could return a factory that was appropriate for a particular user or applet, and a framework could use a factory
 * customized to its own purposes.
 * 
 * @since TotalCross 1.13
 */
public class SocketFactory {
  private static SocketFactory instance;

  public static SocketFactory getDefault() {
    if (instance == null) {
      instance = new SocketFactory();
    }
    return instance;
  }

  /**
   * Creates a socket and connects it to the specified remote host at the specified remote port. This socket is
   * configured using the socket options established for this factory.
   * 
   * @param host
   *           the server host
   * @param port
   *           the server port
   * @return the Socket
   * @throws UnknownHostException
   *            if the host is not known
   * @throws IOException
   *            if an I/O error occurs when creating the socket
   * @see totalcross.net.Socket#Socket(String, int)
   * @since TotalCross 1.13
   */
  public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
    return new Socket(host, port);
  }

  /**
   * Creates a socket and connects it to the specified remote host at the specified remote port. This socket is
   * configured using the socket options established for this factory.
   * 
   * @param host
   *           the server host
   * @param port
   *           the server port
   * @return the Socket
   * @throws UnknownHostException
   *            if the host is not known
   * @throws IOException
   *            if an I/O error occurs when creating the socket
   * @see totalcross.net.Socket#Socket(String, int)
   * @since TotalCross 1.22
   */
  public Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException {
    return new Socket(host, port, timeout);
  }
}
