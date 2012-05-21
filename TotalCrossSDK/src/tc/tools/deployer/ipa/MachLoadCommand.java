package tc.tools.deployer.ipa;
import java.io.IOException;

/**
 * More information in "Mac OS X Internals: A Systems Approach, by Amit Singh"
 */
public abstract class MachLoadCommand
{
   public static final int LC_SEGMENT = 1;
   public static final int LC_SYMTAB = 2;
   public static final int LC_DYSYMTAB = 11;
   public static final int LC_CODE_SIGNATURE = 0x1d;
   public static final int LC_ENCRYPTION_INFO = 0x21;
   public static final int LC_DYLD_INFO = 0x22;
   public boolean RequiredForDynamicLoad;

   protected MachLoadCommand()
   {}

   public static MachLoadCommand readFromStream(ElephantMemoryReader reader) throws IOException
   {
      MachLoadCommand command = null;
      long num2 = reader.readUnsignedInt();
      long num3 = reader.readUnsignedInt();
      long num4 = num2 & 0x7fffffff;
      switch ((int) num4)
      {
         case LC_SEGMENT:
            command = new MachLoadCommandSegment();
         break;

         case LC_SYMTAB:
            num3 = 24;
         break;

         case LC_DYSYMTAB:
            num3 = 80;
         break;

         case LC_CODE_SIGNATURE:
            command = new MachLoadCommandCodeSignature();
         break;

         case LC_ENCRYPTION_INFO:
            num3 = 20;
         break;

         case LC_DYLD_INFO:
            num3 = 48;
         break;
      }

      if (command == null) // skip commands we don't really care about
         reader.skip(num3 - 8);
      else
         command.unpackageData(reader);
      return command;
   }

   abstract protected void unpackageData(ElephantMemoryReader reader) throws IOException;
}
