package totalcross.io;

public interface OpenMode {
   public static final int NotOpen = 0x0000;
   public static final int ReadOnly	= 0x0001;
   public static final int WriteOnly = 0x0002;
   public static final int ReadWrite = ReadOnly | WriteOnly;
}