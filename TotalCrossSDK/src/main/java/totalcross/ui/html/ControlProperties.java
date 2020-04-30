// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.html;

import totalcross.ui.Control;

/**
 * Class that holds control attributes.
 *
 * @author Pierre G. Richard / Jim Guistwite
 */
public class ControlProperties {
  /** The name of this control */
  public String name;

  /** The value held by this controll */
  public String value;

  /** The style of this control */
  public Style style;

  /** True to glue the next control in this one. */
  public boolean glue;

  public ControlProperties(String name, String value, Style style) {
    this.name = name;
    this.value = value;
    this.style = style;
  }

  public static Style getStyle(Control c) {
    return ((ControlProperties) c.appObj).style;
  }

}
