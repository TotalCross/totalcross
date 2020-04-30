// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.concurrent;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class ConcurrentLinkedQueue4D<E> extends AbstractQueue<E> implements Queue<E>, Serializable {

  private static final long serialVersionUID = 1365616274026915983L;
  /*
   * TODO use a real concurrent stuff, not just this placeholder here
   */
  private LinkedList<E> list;

  public ConcurrentLinkedQueue4D() {
    this(new LinkedList<E>());
  }

  public ConcurrentLinkedQueue4D(Collection<? extends E> c) {
    list = new LinkedList<>(c);
  }

  @Override
  public boolean offer(E e) {
    list.add(e);
    return true;
  }

  @Override
  public E poll() {
    return list.poll();
  }

  @Override
  public E peek() {
    return list.peek();
  }

  @Override
  public Iterator<E> iterator() {
    return list.iterator();
  }

  @Override
  public int size() {
    return list.size();
  }

}
