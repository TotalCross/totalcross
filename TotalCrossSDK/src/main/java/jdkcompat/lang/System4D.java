// Copyright (C) 2017-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang;

import jdkcompat.io.PrintStream4D;
import totalcross.sys.Vm;
import totalcross.sys.VmStandardOutputStream;

public class System4D {
  public static final PrintStream4D out =
      new PrintStream4D(new VmStandardOutputStream(VmStandardOutputStream.OUT), true);
  public static final PrintStream4D err =
      new PrintStream4D(new VmStandardOutputStream(VmStandardOutputStream.ERR), true);

  public static native long nanoTime();
  
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
