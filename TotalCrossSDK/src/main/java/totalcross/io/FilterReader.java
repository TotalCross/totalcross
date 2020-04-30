// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.io;

import java.io.IOException;
import java.io.Reader;

public abstract class FilterReader extends Reader {

	protected Reader in;
	
	protected FilterReader(Reader in) {
		super(in);
		this.in = in;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	@Override
	public void mark(int readAheadLimit) throws IOException {
		in.mark(readAheadLimit);
	}
	
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return in.read(cbuf, off, len);
	}
	
	@Override
	public int read(char[] cbuf) throws IOException {
		return in.read(cbuf);
	}
	
	@Override
	public int read() throws IOException {
		return in.read();
	}
	
	@Override
	public boolean ready() throws IOException {
		return in.ready();
	}
	
	@Override
	public void reset() throws IOException {
		in.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
