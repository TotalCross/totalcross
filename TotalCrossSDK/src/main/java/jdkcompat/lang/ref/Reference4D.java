// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang.ref;

public abstract class Reference4D<T> {
  T ref;

  public T get() {
    return ref;
  }

  public boolean enqueue() {
    return false;
  }

  public void clear() {
    ref = null;
  }

  public boolean isEnqueued() {
    return false;
  }

  Reference4D(T ref) {
    this.ref = ref;
  }
}
