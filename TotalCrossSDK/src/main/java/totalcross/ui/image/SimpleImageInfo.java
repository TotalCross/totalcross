package totalcross.ui.image;

import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * https://gist.github.com/hushi55/e36009bd31e87a7a83bf 
 */
public class SimpleImageInfo {

	private int height;
	private int width;
	private String mimeType;

//	public SimpleImageInfo(File file) throws IOException {
//		InputStream is = new FileInputStream(file);
//		try {
//			processStream(is);
//		} finally {
//			is.close();
//		}
//	}

	public SimpleImageInfo(InputStream is) throws IOException {
		processStream(is);
	}

	public SimpleImageInfo(byte[] bytes) throws IOException {
		InputStream is = new ByteArrayInputStream(bytes);
		try {
			processStream(is);
		} finally {
			is.close();
		}
	}
	
	/**
	 * Some examples:

    Compiled Java class files (bytecode) and Mach-O binaries start with hex CAFEBABE. When compressed with Pack200 the bytes are changed to CAFED00D.
    GIF image files have the ASCII code for "GIF89a" (47 49 46 38 39 61) or "GIF87a" (47 49 46 38 37 61)
    JPEG image files begin with FF D8 and end with FF D9. JPEG/JFIF files contain the ASCII code for "JFIF" (4A 46 49 46) as a null terminated string. JPEG/Exif files contain the ASCII code for "Exif" (45 78 69 66) also as a null terminated string, followed by more metadata about the file.
    PNG image files begin with an 8-byte signature which identifies the file as a PNG file and allows detection of common file transfer problems: \211 P N G \r \n \032 \n (89 50 4E 47 0D 0A 1A 0A). That signature contains various newline characters to permit detecting unwarranted automated newline conversions, such as transferring the file using FTP with the ASCII transfer mode instead of the binary mode.[5]
    Standard MIDI audio files have the ASCII code for "MThd" (4D 54 68 64) followed by more metadata.
    Unix or Linux scripts may start with a "shebang" (#!, 23 21) followed by the path to an interpreter, if the interpreter is likely to be different from the one from which the script was invoked.
    ELF executables start with 7F E L F
    PostScript files and programs start with "%!" (25 21).
    PDF files start with "%PDF" (hex 25 50 44 46).
    DOS MZ executable files and the EXE stub of the Microsoft Windows PE (Portable Executable) files start with the characters "MZ" (4D 5A), the initials of the designer of the file format, Mark Zbikowski. The definition allows "ZM" (5A 4D) as well, but this is quite uncommon.
    The Berkeley Fast File System superblock format is identified as either 19 54 01 19 or 01 19 54 depending on version; both represent the birthday of the author, Marshall Kirk McKusick.
    The Master Boot Record of bootable storage devices on almost all IA-32 IBM PC compatibles has a code of 55 AA as its last two bytes.
    Executables for the Game Boy and Game Boy Advance handheld video game systems have a 48-byte or 156-byte magic number, respectively, at a fixed spot in the header. This magic number encodes a bitmap of the Nintendo logo.
    Amiga software executable Hunk files running on Amiga classic 68000 machines all started with the hexadecimal number $000003f3, nicknamed the "Magic Cookie."
    In the Amiga, the only absolute address in the system is hex $0000 0004 (memory location 4), which contains the start location called SysBase, a pointer to exec.library, the so-called kernel of Amiga.
    PEF files, used by Mac OS and BeOS for PowerPC executables, contain the ASCII code for "Joy!" (4A 6F 79 21) as a prefix.
    TIFF files begin with either II or MM followed by 42 as a two-byte integer in little or big endian byte ordering. II is for Intel, which uses little endian byte ordering, so the magic number is 49 49 2A 00. MM is for Motorola, which uses big endian byte ordering, so the magic number is 4D 4D 00 2A.
    Unicode text files encoded in UTF-16 often start with the Byte Order Mark to detect endianness (FE FF for big endian and FF FE for little endian). And on Microsoft Windows, UTF-8 text files often start with the UTF-8 encoding of the same character, EF BB BF.
    LLVM Bitcode files start with BC (0x42, 0x43)
    WAD files start with IWAD or PWAD (for Doom), WAD2 (for Quake) and WAD3 (for Half-Life).
    Microsoft Compound File Binary Format (mostly known as one of the older formats of Microsoft Office documents) files start with D0 CF 11 E0, which is visually suggestive of the word "DOCFILE0".
    Headers in ZIP files begin with "PK" (50 4B), the initials of Phil Katz, author of DOS compression utility PKZIP.

	 * @param is
	 * @throws IOException
	 */
	private void processStream(InputStream is) throws IOException {
		int c1 = is.read();
		int c2 = is.read();
		int c3 = is.read();
		
		mimeType = null;
		width = height = -1;

		if (c1 == 'G' && c2 == 'I' && c3 == 'F') { // GIF
			is.skip(3);
			width = readInt(is,2,false);
			height = readInt(is,2,false);
			mimeType = "image/gif";
		} else if (c1 == 0xFF && c2 == 0xD8) { // JPG
			while (c3 == 255) {
				int marker = is.read();
				int len = readInt(is,2,true);
				if (marker == 192 || marker == 193 || marker == 194) {
					is.skip(1);
					height = readInt(is,2,true);
					width = readInt(is,2,true);
					mimeType = "image/jpeg";
					break;
				}
				is.skip(len - 2);
				c3 = is.read();
			}
		} else if ((c1 == 137 && c2 == 80 && c3 == 78) || (c1 == 0x89  && c2 == 0x50 && c3 == 0x41)) { // PNG
			is.skip(15);
			width = readInt(is,2,true);
			is.skip(2);
			height = readInt(is,2,true);
			mimeType = "image/png";
		} else if (c1 == 66 && c2 == 77) { // BMP
			is.skip(15);
			width = readInt(is,2,false);
			is.skip(2);
			height = readInt(is,2,false);
			mimeType = "image/bmp";
		} else {
			int c4 = is.read();
			if ((c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42)
					|| (c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0)) { //TIFF
				boolean bigEndian = c1 == 'M';
				int ifd = 0;
				int entries;
				ifd = readInt(is,4,bigEndian);
				is.skip(ifd - 8);
				entries = readInt(is,2,bigEndian);
				for (int i = 1; i <= entries; i++) {
					int tag = readInt(is,2,bigEndian);
					int fieldType = readInt(is,2,bigEndian);
					long count = readInt(is,4,bigEndian);
					int valOffset;
					if ((fieldType == 3 || fieldType == 8)) {
						valOffset = readInt(is,2,bigEndian);
						is.skip(2);
					} else {
						valOffset = readInt(is,4,bigEndian);
					}
					if (tag == 256) {
						width = valOffset;
					} else if (tag == 257) {
						height = valOffset;
					}
					if (width != -1 && height != -1) {
						mimeType = "image/tiff";
						break;
					}
				}
				
			// https://en.wikipedia.org/wiki/ICO_%28file_format%29
			//TODO ico bug
			} else if ((c1 == 0 && c2 == 0 && (c3 == 1 || c3 == 2))) { // ico 
				is.skip(2);
				width = readInt(is, 1, false);
				height = readInt(is, 1, false);
				if (width == 0) {
					width = 256;
				}
				if (height == 0) {
					height = 256;
				}
				mimeType = "image/x-icon";
			}
		}
		if (mimeType == null) {
			throw new IOException("Unsupported image type");
		}
	}
	
	private int readInt(InputStream is, int noOfBytes, boolean bigEndian) throws IOException {
		int ret = 0;
		int sv = bigEndian ? ((noOfBytes - 1) * 8) : 0;
		int cnt = bigEndian ? -8 : 8;
		for(int i=0;i<noOfBytes;i++) {
			ret |= is.read() << sv;
			sv += cnt;
		}
		return ret;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String toString() {
		return "MIME Type : " + mimeType + "\t Width : " + width + "\t Height : " + height; 
	}
}
