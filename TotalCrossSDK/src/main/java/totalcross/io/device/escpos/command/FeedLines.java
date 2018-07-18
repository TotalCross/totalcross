package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum FeedLines implements Command {
  Instance;

  @Override
  public void write(OutputStream out) throws IOException {
    write(out, 1);
  }

  public void write(OutputStream out, int lines) throws IOException {
    out.write(0x1B);
    out.write(0x64);
    out.write(lines);
  }
}
