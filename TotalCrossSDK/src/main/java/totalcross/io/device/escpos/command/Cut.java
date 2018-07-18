package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Cut implements Command {
  FULL(0x00),
  PART(0x01);

  private final int code;

  Cut(int code) {
    this.code = code;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0x1D);
    out.write(0x56);
    out.write(code);
  }
}
