package tc.tools.deployer.ipa;
import java.io.IOException;

public class MachLoadCommand
{
   public final int LC_CODE_SIGNATURE = 0x1d;
   public final int LC_DYLD_INFO = 0x22;
   public final int LC_DYSYMTAB = 11;
   public final int LC_ENCRYPTION_INFO = 0x21;
   public final int LC_ID_DYLIB = 13;
   public final int LC_ID_DYLINKER = 15;
   public final int LC_LOAD_DYLIB = 12;
   public final int LC_LOAD_DYLINKER = 14;
   public final int LC_LOAD_WEAK_DYLIB = 0x18;
   public final int LC_SEGMENT = 1;
   public final int LC_SYMTAB = 2;
   public final int LC_THREAD = 4;
   public final int LC_UNIXTHREAD = 5;
   public final int LC_UUID = 0x1b;
   public boolean RequiredForDynamicLoad;
   public long StartingLoadOffset = -1L;

   protected MachLoadCommand()
   {}

   public static MachLoadCommand readFromStream(ElephantMemoryReader reader) throws IOException
   {
      MachLoadCommand command = null;
      long position = reader.getPos();
      long num2 = reader.readUnsignedInt();
      long num3 = reader.readUnsignedInt();
      long num4 = num2 & 0x7fffffff;
      switch ((int) num4)
      {
         case 1:
            command = new MachLoadCommandSegment();
         break;

         case 2:
            num3 = 24;
         break;

         case 11:
            num3 = 80;
         break;

         case 0x1d:
            command = new MachLoadCommandCodeSignature();
         break;

         case 0x21:
            num3 = 20;
         break;

         case 0x22:
            num3 = 48;
         break;

         default:
         break;
      }
      if (command == null)
         reader.moveTo(reader.getPos() + (num3 - 8));
      else
      {
         command.StartingLoadOffset = position;
         command.UnpackageData(reader, (int) num3);
      }
      return command;
   }

   protected void UnpackageData(ElephantMemoryReader reader, int CommandSize) throws IOException
   {
      reader.skip(CommandSize - 8);
   }
}
