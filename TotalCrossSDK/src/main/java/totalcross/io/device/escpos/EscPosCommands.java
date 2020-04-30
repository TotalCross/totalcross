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

public interface EscPosCommands {
  public static final byte BEL = 0x07;
  public static final byte HT = 0x09;
  public static final byte LF = 0x0A;
  public static final byte FF = 0x0C;
  public static final byte CR = 0x0C;
  public static final byte NULL = 0x00;

  public static final byte ESC = 0x1B;
  public static final byte[] ESC_INIT = new byte[] {ESC, (byte) '@'};
  public static final byte[] ESC_FEED_PAPER = new byte[] {ESC, (byte) 'J'};
  public static final byte[] ESC_FEED_LINES = new byte[] {ESC, (byte) 'd'};
  public static final byte[] ESC_PRINT_MODE = new byte[] {ESC, (byte) '!'};
  public static final byte[] ESC_UNDERLINE = new byte[] {ESC, (byte) '-'};
  public static final byte[] ESC_EMPHASIZE = new byte[] {ESC, (byte) 'E'};
  public static final byte[] ESC_DOUBLESTRIKE = new byte[] {ESC, (byte) 'G'};
  public static final byte[] ESC_ROTATE = new byte[] {ESC, (byte) 'V'};
  public static final byte[] ESC_PRINT_POSITION = new byte[] {ESC, (byte) '$'};
  public static final byte[] ESC_HORIZONTAL_TAB_POSITION = new byte[] {ESC, (byte) 'D'};
  public static final byte[] ESC_DEFAULT_LINE_SPACING = new byte[] {ESC, (byte) '2'};
  public static final byte[] ESC_LINE_SPACING = new byte[] {ESC, (byte) '3'};
  public static final byte[] ESC_CHARACTER_SPACING = new byte[] {ESC, (byte) ' '};
  public static final byte[] ESC_JUSTIFICATION = new byte[] {ESC, (byte) 'a'};
  public static final byte[] ESC_TURN_OFF = new byte[] {ESC, (byte) '+'};
  public static final byte[] ESC_SELF_TEST = new byte[] {ESC, (byte) '.'};
  public static final byte[] ESC_SHORT_SELF_TEST = new byte[] {ESC, (byte) 'T'};
  public static final byte[] ESC_PAGE_MODE = new byte[] {ESC, (byte) 'L'};

  public static final byte GS = 0x1D;
  public static final byte[] GS_CHARACTER_SIZE = new byte[] {GS, (byte) '!'};
  public static final byte[] GS_REVERSE_BW = new byte[] {GS, (byte) 'B'};
  public static final byte[] GS_LEFT_MARGIN = new byte[] {GS, (byte) 'L'};
  public static final byte[] GS_CUT = new byte[] {GS, (byte) 'V'};
  public static final byte[] GS_PRINT_DOWNLOADED_BIT_IMAGE = new byte[] {GS, (byte) '/'};

  // Barcode
  public static final byte[] GS_HRI = new byte[] {GS, (byte) 'H'};
  public static final byte[] GS_BARCODE_HEIGHT = new byte[] {GS, (byte) 'h'};
  public static final byte[] GS_BARCODE_WIDTH = new byte[] {GS, (byte) 'w'};
}
