package totalcross.io.device.escpos.command;

public interface TextPrintMode {
  public static final int FONT_1 = 0x00;
  public static final int FONT_2 = 0x01;
  public static final int EMPHASIZED = 0x08;
  public static final int DOUBLE_HEIGHT = 0x10;
  public static final int DOUBLE_WIDTH = 0x20;
  public static final int UNDERLINE = 0x80;
}
