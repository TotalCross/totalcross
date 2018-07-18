package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum DrawerKick implements Command {
  PIN2(0),
  PIN5(1);

  private final int code;

  DrawerKick(int code) {
    this.code = code;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    write(out, 50, 50);
  }

  public void write(OutputStream out, int t1Pulse, int t2Pulse) throws IOException {
    out.write(0x1B);
    out.write(0x70);
    out.write(code);
    out.write(t1Pulse);
    out.write(t2Pulse);
  }
}
