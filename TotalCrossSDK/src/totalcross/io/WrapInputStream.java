package totalcross.io;

import java.io.IOException;
import java.io.InputStream;

class WrapInputStream extends InputStream {
	private Stream stream;

	WrapInputStream(Stream stream) {
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		if (read(b) == -1) {
			return -1;
		}
		
		return b[0];
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		try {
			return stream.readBytes(b, off, len);
		} catch (totalcross.io.IOException e) {
			throw new java.io.IOException(e);
		}
	}
	

}