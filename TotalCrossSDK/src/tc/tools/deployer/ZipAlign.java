package tc.tools.deployer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZipAlign.
 * <p>
 * This file is ported from <a href=
 * "https://android.googlesource.com/platform/build/+/master/tools/zipalign/"
 * >AOSP's ZipAlign</a> tool.
 * </p>
 * <p>
 * <h1>Quote from original README</h1>
 * </p>
 * <p>
 * 
 * <pre>
 * The purpose of zipalign is to ensure that all uncompressed data starts
 * with a particular alignment relative to the start of the file.  This
 * allows those portions to be accessed directly with mmap() even if they
 * contain binary data with alignment restrictions.
 * 
 * Some data needs to be word-aligned for easy access, others might benefit
 * from being page-aligned.  The adjustment is made by altering the size of
 * the "extra" field in the zip Local File Header sections.  Existing data
 * in the "extra" fields may be altered by this process.
 * 
 * Compressed data isn't very useful until it's uncompressed, so there's no
 * need to adjust its alignment.
 * 
 * Alterations to the archive, such as renaming or deleting entries, will
 * potentially disrupt the alignment of the modified entry and all later
 * entries.  Files added to an "aligned" archive will not be aligned.
 * </pre>
 * 
 * </p>
 * <p>
 * <h1>Notes</h1>
 * </p>
 * <p>
 * <ul>
 * <li>The tool modifies the "extra" field of all entries which are not
 * compressed ({@link ZipEntry#STORED}).</li>
 * 
 * <li>Only the "extra" fields in local file headers are modified. The ones in
 * central directory are not touched.</li>
 * </ul>
 * </p>
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/Zip_(file_format)">Zip (file
 * format) - Wikipedia</a> for further information..
 * </p>
 * 
 * @author Hai Bison
 * @since v1.6.9 beta
 */
public class ZipAlign {

    /**
     * The minimum size of a ZIP entry's header.
     */
    public static final int ZIP_ENTRY_HEADER_LEN = 30;

    /**
     * Default version to work with ZIP files.
     */
    public static final int ZIP_ENTRY_VERSION = 20;

    /**
     * The offset of extra field length in a ZIP entry's header.
     */
    public static final int ZIP_ENTRY_OFFSET_EXTRA_LEN = 28;

    /**
     * The size of field extra length, in a ZIP entry's header.
     */
    public static final int ZIP_ENTRY_FIELD_EXTRA_LEN_SIZE = 2;

    /**
     * @see <a
     *      href="https://android.googlesource.com/platform/build/+/master/tools/zipalign/ZipEntry.h">ZipEntry.h</a>
     */
    public static final int ZIP_ENTRY_USES_DATA_DESCR = 0x0008;

    /**
     * @see <a
     *      href="https://android.googlesource.com/platform/build/+/master/tools/zipalign/ZipEntry.h">ZipEntry.h</a>
     */
    public static final int ZIP_ENTRY_DATA_DESCRIPTOR_LEN = 16;

    /**
     * Default alignment value.
     * <p>
     * See <a
     * href="http://developer.android.com/tools/help/zipalign.html">zipalign
     * </a>.
     * </p>
     */
    public static final int DEFAULT_ALIGNMENT = 4;

    /**
     * Used to append to newly aligned APK's file name.
     */
    public static final String ALIGNED = "ALIGNED";

    /**
     * Private helper class.
     * 
     * @author Hai Bison
     * @since v1.6.9 beta
     */
    class XEntry {

        public final ZipEntry entry;
        public final long headerOffset;
        public final int flags;
        public final int padding;

        /**
         * Creates new instance.
         * 
         * @param entry
         *            the entry.
         * @param headerOffset
         *            the offset of the header.
         * @param flags
         *            the flags.
         * @param padding
         *            the padding of the "extra" field.
         */
        public XEntry(ZipEntry entry, long headerOffset, int flags, int padding) {
            this.entry = entry;
            this.headerOffset = headerOffset;
            this.flags = flags;
            this.padding = padding;
        }// XEntry()
    }// XEntry

    /**
     * Extended class of {@link FilterOutputStream}, which has some helper
     * methods for writing data to ZIP stream.
     * 
     * @author Hai Bison
     * @since v1.6.9 beta
     */
    private static class FilterOutputStreamEx extends FilterOutputStream {

        private long totalWritten = 0;

        /**
         * Creates new instance.
         * 
         * @param out
         *            {@link OutputStream}.
         */
        public FilterOutputStreamEx(OutputStream out) {
            super(out);
        }// FilterOutputStreamEx()

        
        public void write(byte[] b) throws IOException {
            out.write(b);
            totalWritten += b.length;
        }// write()

        
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            totalWritten += len;
        }// write()

        
        public void write(int b) throws IOException {
            out.write(b);
            totalWritten += 1;
        }// write()

        
        public void close() throws IOException {
            // l("\t\tclose() >> totalWritten = %,d", totalWritten);
            super.close();
        }// close()

        /**
         * Writes a 32-bit int to the output stream in little-endian byte order.
         * 
         * @param v
         *            the data to write.
         * @throws IOException
         */
        public void writeInt(long v) throws IOException {
            write((int) ((v >>> 0) & 0xff));
            write((int) ((v >>> 8) & 0xff));
            write((int) ((v >>> 16) & 0xff));
            write((int) ((v >>> 24) & 0xff));
        }// writeInt()

        /**
         * Writes a 16-bit short to the output stream in little-endian byte
         * order.
         * 
         * @param v
         *            the data to write.
         * @throws IOException
         */
        public void writeShort(int v) throws IOException {
            write((v >>> 0) & 0xff);
            write((v >>> 8) & 0xff);
        }// writeShort()

    }// FilterOutputStreamEx

    /**
     * To align ZIP files :-)
     * 
     * @author Hai Bison
     * @since v1.6.9 beta
     */
        private File mInputFile;
        private int mAlignment;
        private File mOutputFile;
        private ZipFile mZipFile;
        private RandomAccessFile mRafInput;
        private FilterOutputStreamEx mOutputStream;
        private List mXEntries = new ArrayList();
        private long mInputFileOffset = 0;
        private int mTotalPadding = 0;

        /**
         * Creates new instance with alignment value of
         * {@link ZipAlign#DEFAULT_ALIGNMENT}.
         * 
         * @param input
         *            the input file.
         * @param output
         *            the output file.
         */
        public void zipAlign(File input, File output) {
            mInputFile = input;
            mAlignment = DEFAULT_ALIGNMENT;
            mOutputFile = output;

            try {
                openFiles();
                copyAllEntries();
                buildCentralDirectory();
            } catch (Exception e) {
                mOutputFile.delete();
            } finally {
                try {
                    closeFiles();
                } catch (Exception e) {
                    mOutputFile.delete();
                }
            }
        }// run()

        /**
         * Opens files.
         * <p>
         * This takes 5% of total.
         * </p>
         * 
         * @throws IOException
         */
        private void openFiles() throws IOException {
            mZipFile = new ZipFile(mInputFile);
            mRafInput = new RandomAccessFile(mInputFile, "r");
            mOutputStream = new FilterOutputStreamEx(new BufferedOutputStream(
                    new FileOutputStream(mOutputFile), 32 * 1024));
        }// openFiles()

        /**
         * Copies all entries, aligning them if needed.
         * <p>
         * This takes 80% of total.
         * </p>
         * 
         * @throws IOException
         */
        private void copyAllEntries() throws IOException {
            final int entryCount = mZipFile.size();
            if (entryCount == 0) {
                return;
            }

            final Enumeration entries = mZipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry)entries.nextElement();

                int flags = entry.getMethod() == ZipEntry.STORED ? 0 : 1 << 3;
                flags |= 1 << 11;

                final long outputEntryHeaderOffset = mOutputStream.totalWritten;

                final int inputEntryHeaderSize = ZIP_ENTRY_HEADER_LEN
                        + (entry.getExtra() != null ? entry.getExtra().length
                                : 0)
                        + entry.getName().getBytes("UTF-8").length;
                final long inputEntryDataOffset = mInputFileOffset
                        + inputEntryHeaderSize;

                final int padding;

                if (entry.getMethod() != ZipEntry.STORED) {
                    /*
                     * The entry is compressed, copy it without padding.
                     */
                    padding = 0;
                } else {
                    /*
                     * Copy the entry, adjusting as required. We assume that the
                     * file position in the new file will be equal to the file
                     * position in the original.
                     */
                    long newOffset = inputEntryDataOffset + mTotalPadding;
                    padding = (int) ((mAlignment - (newOffset % mAlignment)) % mAlignment);
                    mTotalPadding += padding;
                }

                final XEntry xentry = new XEntry(entry,
                        outputEntryHeaderOffset, flags, padding);
                mXEntries.add(xentry);

                /*
                 * Modify the original header, add padding to `extra` field and
                 * copy it to output.
                 */
                byte[] extra = entry.getExtra();
                if (extra == null) {
                    extra = new byte[padding];
                    Arrays.fill(extra, (byte) 0);
                } else {
                    byte[] newExtra = new byte[extra.length + padding];
                    System.arraycopy(extra, 0, newExtra, 0, extra.length);
                    Arrays.fill(newExtra, extra.length, newExtra.length,
                            (byte) 0);
                    extra = newExtra;
                }
                entry.setExtra(extra);

                /*
                 * Now write the header to output.
                 */

                mOutputStream.writeInt(ZipOutputStream.LOCSIG);
                mOutputStream.writeShort(ZIP_ENTRY_VERSION);
                mOutputStream.writeShort(flags);
                mOutputStream.writeShort(entry.getMethod());

                int modDate;
                int time;
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(entry.getTime()));
                int year = cal.get(Calendar.YEAR);
                if (year < 1980) {
                    modDate = 0x21;
                    time = 0;
                } else {
                    modDate = cal.get(Calendar.DATE);
                    modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                    modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                    time = cal.get(Calendar.SECOND) >> 1;
                    time = (cal.get(Calendar.MINUTE) << 5) | time;
                    time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
                }

                mOutputStream.writeShort(time);
                mOutputStream.writeShort(modDate);

                mOutputStream.writeInt(entry.getCrc());
                mOutputStream.writeInt(entry.getCompressedSize());
                mOutputStream.writeInt(entry.getSize());

                mOutputStream
                        .writeShort(entry.getName().getBytes("UTF-8").length);
                mOutputStream.writeShort(entry.getExtra().length);
                mOutputStream.write(entry.getName().getBytes("UTF-8"));
                mOutputStream.write(entry.getExtra(), 0,
                        entry.getExtra().length);

                /*
                 * Copy raw data.
                 */

                mInputFileOffset += inputEntryHeaderSize;

                final long sizeToCopy;
                if ((flags & ZIP_ENTRY_USES_DATA_DESCR) != 0)
                    sizeToCopy = (entry.isDirectory() ? 0 : entry
                            .getCompressedSize())
                            + ZIP_ENTRY_DATA_DESCRIPTOR_LEN;
                else
                    sizeToCopy = entry.isDirectory() ? 0 : entry
                            .getCompressedSize();

                if (sizeToCopy > 0) {
                    mRafInput.seek(mInputFileOffset);

                    long totalSizeCopied = 0;
                    final byte[] buf = new byte[32 * 1024];
                    while (totalSizeCopied < sizeToCopy) {
                        int read = mRafInput.read(
                                buf,
                                0,
                                (int) Math.min(32 * 1024, sizeToCopy
                                        - totalSizeCopied));
                        if (read <= 0)
                            break;

                        mOutputStream.write(buf, 0, read);
                        totalSizeCopied += read;
                    }// while
                }// if

                mInputFileOffset += sizeToCopy;
            }// while
        }// copyAllEntries()

        /**
         * Builds central directory.
         * <p>
         * This takes 10% of total.
         * </p>
         * 
         * @throws IOException
         */
        private void buildCentralDirectory() throws IOException {
            final long centralDirOffset = mOutputStream.totalWritten;

            for (int i = 0, n = mXEntries.size(); i < n; i++){

               XEntry xentry = (XEntry) mXEntries.get(i); 
                /*
                 * Write entry.
                 */
                final ZipEntry entry = xentry.entry;

                int modDate;
                int time;
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(entry.getTime()));
                int year = cal.get(Calendar.YEAR);
                if (year < 1980) {
                    modDate = 0x21;
                    time = 0;
                } else {
                    modDate = cal.get(Calendar.DATE);
                    modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                    modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                    time = cal.get(Calendar.SECOND) >> 1;
                    time = (cal.get(Calendar.MINUTE) << 5) | time;
                    time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
                }

                mOutputStream.writeInt(ZipFile.CENSIG); // CEN header signature
                mOutputStream.writeShort(ZIP_ENTRY_VERSION); // version made by
                mOutputStream.writeShort(ZIP_ENTRY_VERSION); // version needed
                                                             // to
                // extract
                mOutputStream.writeShort(xentry.flags); // general purpose bit
                                                        // flag
                mOutputStream.writeShort(entry.getMethod()); // compression
                                                             // method
                mOutputStream.writeShort(time);
                mOutputStream.writeShort(modDate);
                mOutputStream.writeInt(entry.getCrc()); // crc-32
                mOutputStream.writeInt(entry.getCompressedSize()); // compressed
                                                                   // size
                mOutputStream.writeInt(entry.getSize()); // uncompressed size
                final byte[] nameBytes = entry.getName().getBytes("UTF-8");
                mOutputStream.writeShort(nameBytes.length);
                mOutputStream.writeShort(entry.getExtra() != null ? entry
                        .getExtra().length - xentry.padding : 0);
                final byte[] commentBytes;
                if (entry.getComment() != null) {
                    commentBytes = entry.getComment().getBytes("UTF-8");
                    mOutputStream.writeShort(Math.min(commentBytes.length,
                            0xffff));
                } else {
                    commentBytes = null;
                    mOutputStream.writeShort(0);
                }
                mOutputStream.writeShort(0); // starting disk number
                mOutputStream.writeShort(0); // internal file attributes
                                             // (unused)
                mOutputStream.writeInt(0); // external file attributes (unused)
                mOutputStream.writeInt(xentry.headerOffset); // relative offset
                                                             // of
                // local
                // header
                mOutputStream.write(nameBytes);
                if (entry.getExtra() != null)
                    mOutputStream.write(entry.getExtra(), 0,
                            entry.getExtra().length - xentry.padding);
                if (commentBytes != null)
                    mOutputStream.write(commentBytes, 0,
                            Math.min(commentBytes.length, 0xffff));
            }// for xentry


            /*
             * Write the end of central directory.
             */
            final long centralDirSize = mOutputStream.totalWritten
                    - centralDirOffset;

            final int entryCount = mXEntries.size();

            mOutputStream.writeInt(ZipFile.ENDSIG); // END record signature
            mOutputStream.writeShort(0); // number of this disk
            mOutputStream.writeShort(0); // central directory start disk
            mOutputStream.writeShort(entryCount); // number of directory entries
                                                  // on
            // disk
            mOutputStream.writeShort(entryCount); // total number of directory
                                                  // entries
            mOutputStream.writeInt(centralDirSize); // length of central
                                                    // directory
            mOutputStream.writeInt(centralDirOffset); // offset of central
            // directory
/*            if (mZipFile.getComment() != null) { // zip file comment
                final byte[] bytes = mZipFile.getComment().getBytes("UTF-8");
                mOutputStream.writeShort(bytes.length);
                mOutputStream.write(bytes);
            } else {
*/                mOutputStream.writeShort(0);
//            }

            mOutputStream.flush();

        }// buildCentralDirectory()

        /**
         * Closes all files.
         * <p>
         * This takes 5% of total.
         * </p>
         * 
         * @throws IOException
         */
        private void closeFiles() throws IOException {
            try {
                mZipFile.close();
            } finally {
                try {
                    mRafInput.close();
                } finally {
                    mOutputStream.close();
                }
            }

        }// closeFiles()
}
