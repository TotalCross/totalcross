// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.profiling;

import totalcross.sys.Vm;

/**
 * This class counts how many times it was called per second.
 * This can be really useful to profile whether a certain block of 
 * code is being executed too often.
 * 
 * Usage:
 * private static CallsCounter counter;
 *
 * public void someMethod() {
 *   if (counter == null) {
 *      counter = new CallsCounter("someMethod");
 *   }
 *   counter.Update();
 *   // some method's code...
 * }
 * 
 * @since TotalCross 4.2
 */
public class CallsCounter {
   private static final long ONE_SECOND = 1000;
   private String identifier;
   private long start = -1;
   private int calls = 0;
   
   /**
    * Creates a new CallsCounter object with the given identifier.
    * @param identifier text attached to the regular calls/s reports
    */
   public CallsCounter(String identifier) {
      this.identifier = identifier;
   }
   
   public void Update() {
      if (start < 0) {
         start = Vm.getTimeStamp();
         calls = 1;
      } else {
         calls++;
         if (Vm.getTimeStamp() - start > ONE_SECOND) {
            if (calls <= 2) {
               Vm.debug(identifier + ": < 1 calls/s");
               start = -1;
               calls = 0;
            } else {
               Vm.debug(identifier + ": " + calls + " calls/s");
               start = -1;
               calls = 0;
            }
         }
      }
   }
}
