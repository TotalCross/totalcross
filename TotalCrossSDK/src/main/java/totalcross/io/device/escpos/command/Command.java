package totalcross.io.device.escpos.command;

import java.io.IOException;
import java.io.OutputStream;

public interface Command {
  void write(OutputStream out) throws IOException;
}
