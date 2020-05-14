// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.event;

public class ValueChangeEvent<T> extends Event<ValueChangeHandler<T>> {

  private T value;

  public ValueChangeEvent(Object target, T value) {
    super(0, target, 0);
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public void dispatch(ValueChangeHandler<T> listener) {
    listener.onValueChange(this);
  }
}
  
