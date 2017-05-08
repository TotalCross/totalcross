package jdkcompat.io;

import java.io.IOException;
import java.io.OutputStream;

public class FilterOutputStream4D extends OutputStream {
	protected OutputStream out;
	
	public FilterOutputStream4D(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}
	
	@Override
 	public void write(byte[] b) throws IOException {
		out.write(b);
	}
	
	@Override
 	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}
}
