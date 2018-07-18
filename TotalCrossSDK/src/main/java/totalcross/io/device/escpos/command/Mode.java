package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Mode implements Command {
  PAGE(0x4C);

  private final int code;

  Mode(int code) {
    this.code = code;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0x1B);
    out.write(code);
  }
}
