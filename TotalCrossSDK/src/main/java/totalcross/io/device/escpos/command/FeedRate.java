package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum FeedRate implements Command {
  Instance;

  @Override
  public void write(OutputStream out) throws IOException {
    write(out, (byte) 0x22);
  }

  public void write(OutputStream out, int rate) throws IOException {
    out.write(27);
    out.write(51);
    out.write(rate);
  }
}
