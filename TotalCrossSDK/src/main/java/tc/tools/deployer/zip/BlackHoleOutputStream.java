package tc.tools.deployer.zip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that acts as a null device: discards any written data but always reports success.<br>
 * Originally named as NullOutputStream, but renamed to not be confused with the Apache class that writes to /dev/null,
 * which essentially is the same thing but I'm a nitpicker. :)
 * 
 * @author Fabio Sobral
 * 
 */
public class BlackHoleOutputStream extends OutputStream
{
  public BlackHoleOutputStream()
  {
    super();
  }

  @Override
  public void write(int b) throws IOException
  {
    // do nothing
  }
}
