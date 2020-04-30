// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang;

import java.io.OutputStream;
import java.io.PrintStream;

import totalcross.sys.Vm;
import totalcross.util.concurrent.Lock;

public class System4D {
  private static class VmDebugStream extends PrintStream {
    Lock lock = new Lock();
    StringBuffer acc = new StringBuffer();

    public VmDebugStream() {
      super((OutputStream) new OutputStream() {

        @Override
        public void write(int b) {
        }
      });
    }

    @Override
    public void flush() {
      synchronized (lock) {
        Vm.debug(acc.toString());
        acc.setLength(0);
      }
    }

    @Override
    public void write(int b) {
      char c = (char) b;

      if (c == '\n') {
        flush();
      } else {
        acc.append(c);
      }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
      acc.append(new String(buf, off, len));

      int idx = acc.lastIndexOf("\n");

      if (idx != -1) {
        String trailing = acc.substring(idx);

        // flushes only the first part, ignores trailing part for now...
        acc.setLength(idx);
        flush();

        acc.append(trailing);
      }
    }

    @Override
    public void print(String s) {
      acc.append(s);
    }

    @Override
    public void println(String str) {
      synchronized (lock) {
        print(str);
        flush();
      }
    }

    @Override
    public void println() {
      flush();
    }
  }

  private static final VmDebugStream instance = new VmDebugStream();

  public static final PrintStream err = instance;
  public static final PrintStream out = instance;
  
    /**
     * Copies the number of {@code length} elements of the Array {@code src}
     * starting at the offset {@code srcPos} into the Array {@code dest} at the
     * position {@code destPos}.
     *
     * @param src     the source array to copy the content.
     * @param srcPos  the starting index of the content in {@code src}.
     * @param dest    the destination array to copy the data into.
     * @param destPos the starting index for the copied content in {@code dest}.
     * @param length  the number of elements of the {@code array1} content they have
     *                to be copied.
     */
    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        Vm.arrayCopy(src, srcPos, dest, destPos, length);
    }
}
