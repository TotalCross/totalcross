/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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
  
