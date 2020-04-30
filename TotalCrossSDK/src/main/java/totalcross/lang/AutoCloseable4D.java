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

package totalcross.lang;

/**
 * An object that may hold resources (such as file or socket handles) until it
 * is closed. The <a href=
 * "../../totalcross/lang/AutoCloseable.html#close--"><code>close()</code></a>
 * method of an <code>AutoCloseable</code> object is called automatically when
 * exiting a <code>try</code>-with-resources block for which the object has been
 * declared in the resource specification header. This construction ensures
 * prompt release, avoiding resource exhaustion exceptions and errors that may
 * otherwise occur.
 * <dl>
 * <dt><span class="simpleTagLabel">API Note:</span></dt>
 * <dd>
 * <p>
 * It is possible, and in fact common, for a base class to implement
 * AutoCloseable even though not all of its subclasses or instances will hold
 * releasable resources. For code that must operate in complete generality, or
 * when it is known that the <code>AutoCloseable</code> instance requires
 * resource release, it is recommended to use <code>try</code>-with-resources
 * constructions.</dd>
 */
public interface AutoCloseable4D {

  /**
   * Closes this resource, relinquishing any underlying resources. This method
   * is invoked automatically on objects managed by the
   * <code>try</code>-with-resources statement.
   * 
   * <p>
   * While this interface method is declared to throw <code>Exception</code>,
   * implementers are <em>strongly</em> encouraged to declare concrete
   * implementations of the <code>close</code> method to throw more specific
   * exceptions, or to throw no exception at all if the close operation cannot
   * fail.
   * 
   * <p>
   * Cases where the close operation may fail require careful attention by
   * implementers. It is strongly advised to relinquish the underlying resources
   * and to internally <em>mark</em> the resource as closed, prior to throwing
   * the exception. The <code>close</code> method is unlikely to be invoked more
   * than once and so this ensures that the resources are released in a timely
   * manner. Furthermore it reduces problems that could arise when the resource
   * wraps, or is wrapped, by another resource.
   * 
   * <p>
   * Note that unlike the
   * <a href="../../totalcross/io/Closeable.html#close--"><code>close</code></a>
   * method of <a href="../../totalcross/io/Closeable.html" title="interface in
   * totalcross.io"><code>Closeable</code></a>, this <code>close</code> method
   * is <em>not</em> required to be idempotent. In other words, calling this
   * <code>close</code> method more than once may have some visible side effect,
   * unlike <code>Closeable.close</code> which is required to have no effect if
   * called more than once.
   * 
   * However, implementers of this interface are strongly encouraged to make
   * their <code>close</code> methods idempotent.
   * <dl>
   * 
   * @throws Exception
   *           if this resource cannot be
   */
  public void close() throws Exception;
}
