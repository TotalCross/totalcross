/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package totalcross.util.concurrent;

/** Class used with the <code>synchronize</code> keyword, representing a lock to avoid concurrent access.
 * Here's a sample:
 * <pre>
 * Lock lock1 = new Lock(); // usually a class' field
 * 
 * synchronized (lock1)
 * {
 *    ...
 * }
 * </pre>
 * Only a Lock object can be used with <code>synchronize</code>. Using <code>this</code> will abort the tc.Deploy and
 * using other objects will cause a RuntimeException during code execution under the TCVM.
 * @since TotalCross 1.2
 */

public class Lock
{
   Object mutex;

   public Lock()
   {
      create();
   }
   
   protected void finalize()
   {
      destroy();
   }
   
   final void create() 
   {
   }
   
   final void destroy()
   {
   }
   
   native void create4D();
   native void destroy4D();
}
