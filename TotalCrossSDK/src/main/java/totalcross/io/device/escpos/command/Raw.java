package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum Raw implements Command {
  Instance;

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(0);
  }

  public void write(OutputStream out, int val) throws IOException {
    out.write(val);
  }

  public void write(OutputStream out, byte val) throws IOException {
    out.write(val);
  }

  public void write(OutputStream out, String string) throws IOException {
    out.write(string.getBytes());
  }

  public void write(OutputStream out, byte[] vals) throws IOException {
    out.write(vals);
  }
}
