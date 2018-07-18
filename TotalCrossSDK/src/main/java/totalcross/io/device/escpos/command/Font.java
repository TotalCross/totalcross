package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Font implements Command {
  REGULAR(0x00),
  DH(0x10),
  DW(0x20),
  DWDH(0x30),
  EMPHASIZED(0x08),
  DH_EMPHASIZED(0x18),
  DW_EMPHASIZED(0x28),
  DWDH_EMPHASIZED(0x38);

  private final int code;

  Font(int code) {
    this.code = code;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0x1B);
    out.write(0x21);
    out.write(code);
  }
}
