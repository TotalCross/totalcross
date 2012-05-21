package tc.tools.deployer.ipa;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MachObjectFile
{
   public List Commands = new ArrayList();
   public long CpuSubType;
   public long CpuType;
   public long FileType;
   public long Flags;
   public long Magic;

   public MachObjectFile(byte[] data) throws IOException
   {
      ElephantMemoryReader reader = new ElephantMemoryReader(data);

      this.Magic = reader.readUnsignedInt();
      this.CpuType = reader.readUnsignedInt();
      this.CpuSubType = reader.readUnsignedInt();
      this.FileType = reader.readUnsignedInt();
      long num2 = reader.readUnsignedInt();
      long num3 = reader.readUnsignedInt();
      this.Flags = reader.readUnsignedInt();
      this.Commands.clear();
      for (int i = 0; i < num2; i++)
      {
         MachLoadCommand item = MachLoadCommand.CreateFromStream(reader);
         this.Commands.add(item);
      }
      reader.close();
   }
}
