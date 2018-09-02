package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for ESC/POS printable objects.
 * <p>
 * A printable object is usually some sort of graphical data (image, bar code or
 * QR code) that have its own set of ESC/POS configuration commands.
 * <p>
 * 
 * @author Fábio Sobral
 */
public interface EscPosPrintObject {

  /**
   * Writes this printable object to the given output stream.
   * 
   * @param out
   *          the output stream
   * @throws IOException
   *           if an I/O error occurs.
   */
  void write(OutputStream out) throws IOException;
}
