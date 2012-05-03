package tc.tools.deployer.ipa;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MachObjectFile
{
   public List Commands = new ArrayList();
   public final int CPU_TYPE_ARM = 12;
   public final int CPU_TYPE_I386 = 7;
   public final int CPU_TYPE_POWERPC = 0x12;
   public long CpuSubType;
   public long CpuType;
   public long FileType;
   public long Flags;
   public final int MachHeaderPad = 0x1a60;
   public long Magic;
   public final int MH_CIGAM = 0xcefaedfe;
   public final int MH_EXECUTE = 2;
   public final int MH_MAGIC = 0xfeedface;

   public MachObjectFile(byte[] data) throws IOException
   {
      ElephantMemoryReader reader = new ElephantMemoryReader(data);

//      long streamPosition = 0L;
      this.Magic = reader.readUnsignedInt();
      this.CpuType = reader.readUnsignedInt();
      this.CpuSubType = reader.readUnsignedInt();
      this.FileType = reader.readUnsignedInt();
      long num2 = reader.readUnsignedInt();
      long num3 = reader.readUnsignedInt();
      this.Flags = reader.readUnsignedInt();
      //      SR.VerifyStreamPosition(ref streamPosition, 0x1cL);
      this.Commands.clear();
      for (int i = 0; i < num2; i++)
      {
         MachLoadCommand item = MachLoadCommand.CreateFromStream(reader);
         this.Commands.add(item);
      }
      //      SR.VerifyStreamPosition(ref streamPosition, (long) num3);

      reader.close();
   }
}
