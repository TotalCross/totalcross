// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.json;

/**
 * The <code>JSONString</code> interface allows a <code>toJSONString()</code>
 * method so that a class can change the behavior of
 * <code>JSONObject.toString()</code>, <code>JSONArray.toString()</code>,
 * and <code>JSONWriter.value(</code>Object<code>)</code>. The
 * <code>toJSONString</code> method will be used instead of the default behavior
 * of using the Object's <code>toString()</code> method and quoting the result.
 */
public interface JSONString {
  /**
   * The <code>toJSONString</code> method allows a class to produce its own JSON
   * serialization.
   *
   * @return A strictly syntactically correct JSON text.
   */
  public String toJSONString();
}
