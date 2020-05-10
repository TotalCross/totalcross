// Copyright (C) 2001-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/** Control used to add a space between controls. It shows nothing on screen.
 * Here's a sample of how to use it:
 * 
 * <pre>
 * Spacer s;
 * add(s = new Spacer("  "),CENTER,AFTER+2);
 * add(btnClear = new Button("Clear"), AFTER,SAME, s);
 * add(btnOK = new Button("OK"), BEFORE, SAME, SAME, SAME, s);
 * </pre>
 * 
 * This sample will place two buttons centered on screen, like this:
 * <pre>
 * ==========  ===========
 * |   Ok   |  |  Clear  |
 * ==========  ===========
 * </pre>
 * 
 * @since TotalCross 1.22
 */

public class Spacer extends Control {
  private int prefW, prefH;
  private String s;

  /** Constructs a new Spacer with width and height = 0. */
  public Spacer() {
    this(0, 0);
  }

  /** Constructs a new Spacer using the given x and y values. */
  public Spacer(int x, int y) {
    this.prefW = x;
    this.prefH = y;
    eventsEnabled = false;
    focusTraversable = false; // guich@tc123_12
  }

  /** Constructs a new Spacer using the given String, whose width will be the horizontal spacement. The vertical spacement will be the font's height. */
  public Spacer(String s) {
    this.s = s;
    eventsEnabled = false;
    focusTraversable = false; // guich@tc123_12
  }

  @Override
  public int getPreferredWidth() {
    return s == null ? prefW : fm.stringWidth(s);
  }

  @Override
  public int getPreferredHeight() {
    return s == null ? prefH : fmH;
  }
}
