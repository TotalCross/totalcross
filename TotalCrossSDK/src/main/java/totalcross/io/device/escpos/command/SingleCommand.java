package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public enum SingleCommand implements Command {
  BEL(0x07),
  HT(0x09),
  LF(0x0A),
  FF(0x0C),
  CR(0x0C),
  CAN(0x00);

  int value;

  SingleCommand(int value) {
    this.value = value;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    out.write(value);
  }
}
