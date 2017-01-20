package totalcross.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public abstract class Writer4D implements Closeable, Flushable {
	
	public void write(int charEquiv) throws IOException {
		char buff[] = new char[] { (char) (charEquiv & 0xffff) };
		write(buff, 0, 1);
	}
	
	public void write(char[] buff) throws IOException {
		write(buff, 0, buff.length);
	}

	public abstract void write(char[] buff, int offset, int length) throws IOException;
}
