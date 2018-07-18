package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Align implements Command {
  LEFT(0x00),
  CENTER(0x01),
  RIGHT(0x02);

  public final int code;

  Align(int code) {
    this.code = code;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0x1B);
    out.write(0x61);
    out.write(code);
  }
}
