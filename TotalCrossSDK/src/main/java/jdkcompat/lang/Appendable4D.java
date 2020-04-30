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

import java.io.IOException;

public interface Appendable4D {
  Appendable append(char c) throws IOException;

  Appendable append(CharSequence c) throws IOException;

  Appendable append(CharSequence c, int start, int end) throws IOException;
}
