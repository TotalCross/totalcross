package totalcross.io;

import java.io.IOException;
import java.io.InputStream;

import totalcross.net.Base64;
import totalcross.sys.Vm;

public class Base64DecoderStream extends InputStream {
	InputStream base64encodedStream;
	
	boolean eos = false;
	
	private static final int BYTES_ENCODED_SIZE = 4 * 1024;
	private static final int BYTES_DECODED_SIZE = 3 * 1024;
	byte[] bytesEncodedRead = new byte[BYTES_ENCODED_SIZE];
	byte[] bytesDecodedRead = new byte[BYTES_DECODED_SIZE];
	
	byte[] overflowEncodedRead = new byte[4];
	int overflowSize;
	// When decodedSize == -1, end of stream has been reached
	int decodedSize;
	int decodedReadPos;
	
	public Base64DecoderStream(InputStream base64encodedStream) {
		this.base64encodedStream = base64encodedStream;
		
		overflowSize = 0;
		decodedSize = 0;
		decodedReadPos = 0;
	}

	@Override
	public int read() throws IOException {
		ensureFetch();
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ensureFetch();

		// TODO Auto-generated method stub
		return super.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException("totalcross.io.Base64Decoder.skip use is not supported");
		// TODO Auto-generated method stub
		// return super.skip(n);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}

	private void ensureFetch() throws IOException {
		if (eos) {
			markEos();
			return;
		}
		// If read position equals size, then it must fetch more data
		if (decodedReadPos == decodedSize) {
			int oldOverflowSize = overflowSize;
			boolean hasPreviousOverflow;
			switch (oldOverflowSize) {
			case 3:
				bytesEncodedRead[2] = overflowEncodedRead[2];
			case 2:
				bytesEncodedRead[1] = overflowEncodedRead[1];
			case 1:
				bytesEncodedRead[0] = overflowEncodedRead[0];
				hasPreviousOverflow = true;
				break;
			case 0:
			default: // should not have a default case
				hasPreviousOverflow = false;
			}
			int readFromStream = base64encodedStream.read(bytesEncodedRead, oldOverflowSize, BYTES_ENCODED_SIZE);
			
			// Has reached end of stream?
			if (readFromStream < 0) {
				eos = true;
				if (!hasPreviousOverflow) {
					// Real end of stream, not even a single byte from a previous overflow ='(
					markEos();
					return;
				} else {
					// We still have some leftover from a previous overflow, let's use it...
					overflowSize = 0;
					for (int i = oldOverflowSize; i < 4; i++) {
						bytesEncodedRead[i] = (byte) ('=' & 0xff);
					}
					decodedReadPos = 0;
					decodedSize = Base64.decode(bytesEncodedRead, 0, 4, bytesDecodedRead, 0);
				}
			} else {
				int consideredRead = readFromStream + oldOverflowSize;
				overflowSize = consideredRead % 4;
				decodedReadPos = 0;
				
				if (overflowSize != 0) {
					Vm.arrayCopy(bytesEncodedRead, consideredRead - overflowSize, overflowEncodedRead, 0, overflowSize);
				}
				
				int usableReadData = consideredRead - overflowSize;
				decodedSize = Base64.decode(bytesEncodedRead, 0, usableReadData, bytesDecodedRead, 0);
			}
		}
	}

	private void markEos() {
		eos = true;
		overflowSize = -1;
		decodedReadPos = -1;
		decodedSize = -1;
	}	
}
