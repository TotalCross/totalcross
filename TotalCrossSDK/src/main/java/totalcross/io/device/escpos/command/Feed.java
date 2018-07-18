package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Feed implements Command {
  Instance;

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0xA);
  }
}
