/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
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
