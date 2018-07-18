package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public abstract class DoubleCharacterCommand implements Command {

  int prefix;
  int value;

  DoubleCharacterCommand(int prefix, int value) {
    this.prefix = prefix;
    this.value = value;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    byte[] v = new byte[] {(byte) prefix, (byte) value};
    out.write(v);
  }
}
