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

import totalcross.sys.Vm;

public class BufferedReader extends Reader {
	
	private Reader in;
	private int sz;
	private int szRead;
	private int markPosition;
	private char[] buff;
	private int offset = 0;
	private boolean eof = false;
	private boolean closed = false;
	private int leftAlive = -1;
	private int originalLeftAlive;

	public BufferedReader(Reader in, int sz) {
		this.in = in;
		this.sz = sz;
		this.buff = new char[sz];
	}
	
	public BufferedReader(Reader in) {
		this(in, 4096);
	}
	
	@Override
	public void mark(int readAheadLimit) throws IOException {
		if (closed) {
			throw new IOException("Reader closed");
		}
		if (readAheadLimit > sz) {
			// resize
			// deve colocar os bytes anteriores no começo do novo buffer
			char[] newBuff = new char[readAheadLimit];
			int savedChars = szRead - offset;
			Vm.arrayCopy(buff, offset, newBuff, 0, savedChars);
			
			markPosition = 0;
			offset = 0;
			sz = readAheadLimit;
			szRead = savedChars;
			buff = newBuff;
		} else if (offset + readAheadLimit > sz) {
			// deve fazer o shift do não lido para o máximo pra esquerda o possível
			// cuidado com overlaps!!
			markPosition = 0;
			
			int oldOffset = offset;
			int oldSzRead = szRead;
			
			offset = 0;
			szRead = oldSzRead - oldOffset;
			
			if (oldOffset < szRead) {
				// caso não tenha overlaps, só copiar para a nova posição
				Vm.arrayCopy(buff, 0, buff, oldOffset, szRead);
			} else {
				// aqui há overlap, então estou indo pela cópia segura char a char
				// essa não é a técnica mais eficiente, porém garanto a segurança
				for (int i = 0; i < szRead; i++) {
					buff[i] = buff[i + oldOffset];
				}
			}
		} else {
			markPosition = offset;
		}
		originalLeftAlive = leftAlive = readAheadLimit;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void reset() throws IOException {
		if (closed) {
			throw new IOException("Reader closed");
		}
		if (leftAlive >= 0) {
			offset = markPosition;
			leftAlive = originalLeftAlive;
		} else {
			throw new IOException("reset mark is invalid");
		}
	}
	
	public String readLine() throws IOException {
		if (closed) {
			throw new IOException("Reader closed");
		}
		if (eof) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		boolean eol = false;
		
		while (!eol) {
			if (offset >= szRead) {
				resetBuffer();
				if (eof) {
					// chegou no final da leitura
					return builder.toString();
				}
			}
			char c = buff[offset];
			advanceOffset();
			
			if (c == '\r') {
				eol = true;
				if (offset >= szRead) {
					resetBuffer();
					if (eof) {
						// chegou no final da leitura
						return builder.toString();
					}
				}
				char c2 = buff[offset];
				if (c2 == '\n') {
					advanceOffset();
				}
			} else if (c == '\n') {
				eol = true;
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}
	
	private void advanceOffset() {
		offset++;
		leftAlive--;
	}
	
	private void advanceOffset(int n) {
		offset += n;
		leftAlive -= n;
	}

	private void resetBuffer() throws IOException {
		if (eof) {
			return;
		}
		
		if (leftAlive < 0) {
			resetBufferSimple();
		} else {
			resetMarkedBuffer();
			
		}
	}

	private void markEof() {
		eof = true;
		offset = -1;
		szRead = -1;
	}

	private void resetMarkedBuffer() throws IOException {
		int read = in.read(buff, offset, sz - offset);
		if (read == -1) {
			markEof();
		} else {
			szRead = offset + read;
		}
	}
	
	// reseta o offset para 0 e atualiza szRead para a quantidade de bytes de fato lidos
	// se a leitura dos bytes falhar, ou seja, in.read(buff) retornar -1, coloca eof como true
	private void resetBufferSimple() throws IOException {
		int read = in.read(buff);
		if (read == -1) {
			markEof();
		} else {
			offset = 0;
			szRead = read;
		}
	}

	@Override
	public int read() throws IOException {
		if (offset < szRead) {
			int c = buff[offset];
			advanceOffset();
			return c;
		} else {
			resetBuffer();
			if (!eof) {
				int c = buff[offset];
				advanceOffset();
				return c;
			}
		}
		return -1;
	}
	
	// XXX não tenta ler até acabar os caracteres, então não obedece totalmente ao comportamento
	// descrito no javadoc de java.io.BufferedReader.read(char[], int, int)
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int adequateLen;
		
		if (len > szRead - offset) {
			adequateLen = szRead - offset;
		} else {
			adequateLen = len;
		}
		
		if (adequateLen == 0) {
			resetBuffer();
			if (eof) {
				return -1;
			}
			if (len > szRead - offset) {
				adequateLen = szRead - offset;
			} else {
				adequateLen = len;
			}
		}
		if (adequateLen != 0) {
			Vm.arrayCopy(buff, offset, cbuf, off, adequateLen);
			advanceOffset(adequateLen);
		}
		return adequateLen;
	}

	@Override
	public void close() throws IOException {
		this.closed = true;
		in.close();
	}
	
	@Override
	public long skip(long n) throws IOException {
		if (closed) {
			throw new IOException("Reader closed");
		}
		if (n == 0) {
			return 0;
		}
		int delta = szRead - offset;
		if (n < delta) {
			advanceOffset((int)n);
			return n;
		} else {
			if (leftAlive >= 0) {
				if (n >= leftAlive) {
					leftAlive = -1;
					offset = 0;
					szRead = 0;
				} else {
					advanceOffset((int) n);
				}
			} else {
				offset = 0;
				szRead = 0;
			}
			return delta + in.skip(n - delta);
		}
	}

	@Override
	public boolean ready() throws IOException {
		if (closed) {
			throw new IOException("Reader closed");
		}
		return (szRead > offset) || in.ready();
	}
}
