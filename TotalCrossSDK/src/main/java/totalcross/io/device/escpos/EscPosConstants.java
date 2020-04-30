// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.escpos;

public interface EscPosConstants {

  public static final int PAPER_A7 = 384;
  public static final int PAPER_A8 = 576;

  public static final byte ALIGN_LEFT = 0x00;
  public static final byte ALIGN_CENTER = 0x01;
  public static final byte ALIGN_RIGHT = 0x02;

  public static final byte LOGO_NORMAL = 0x00;
  public static final byte LOGO_DOUBLE_WIDTH = 0x01;
  public static final byte LOGO_DOUBLE_HEIGHT = 0x02;
  public static final byte LOGO_QUADRUPLE = 0x03;

  public static final byte CUT_FULL = 0x00;
  public static final byte CUT_PART = 0x01;

  public static final int TEXTPRINTMODE_FONT_1 = 0x00;
  public static final int TEXTPRINTMODE_FONT_2 = 0x01;
  public static final int TEXTPRINTMODE_EMPHASIZED = 0x08;
  public static final int TEXTPRINTMODE_DOUBLE_HEIGHT = 0x10;
  public static final int TEXTPRINTMODE_DOUBLE_WIDTH = 0x20;
  public static final int TEXTPRINTMODE_UNDERLINE = 0x80;

  public static final byte CHARACTERSIZE_WIDTH_1 = 0x00;
  public static final byte CHARACTERSIZE_WIDTH_2 = 0x10;
  public static final byte CHARACTERSIZE_WIDTH_3 = 0x20;
  public static final byte CHARACTERSIZE_WIDTH_4 = 0x30;
  public static final byte CHARACTERSIZE_WIDTH_5 = 0x40;
  public static final byte CHARACTERSIZE_WIDTH_6 = 0x50;
  public static final byte CHARACTERSIZE_WIDTH_7 = 0x60;
  public static final byte CHARACTERSIZE_WIDTH_8 = 0x70;

  public static final byte CHARACTERSIZE_HEIGHT_1 = 0x00;
  public static final byte CHARACTERSIZE_HEIGHT_2 = 0x01;
  public static final byte CHARACTERSIZE_HEIGHT_3 = 0x02;
  public static final byte CHARACTERSIZE_HEIGHT_4 = 0x03;
  public static final byte CHARACTERSIZE_HEIGHT_5 = 0x04;
  public static final byte CHARACTERSIZE_HEIGHT_6 = 0x05;
  public static final byte CHARACTERSIZE_HEIGHT_7 = 0x06;
  public static final byte CHARACTERSIZE_HEIGHT_8 = 0x07;
}
