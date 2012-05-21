package tc.tools.deployer.ipa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MachObjectFile
{
   // the only value we use is commandCount, all others are kept only for future reference.
   protected long magic;
   protected long cpuType;
   protected long cpuSubType;
   protected long fileType;
   protected long commandCount;
   protected long unknownValue;
   protected long flags;

   public List commands = new ArrayList();

   public MachObjectFile(byte[] data) throws IOException
   {
      ElephantMemoryReader reader = new ElephantMemoryReader(data);

      this.magic = reader.readUnsignedInt();
      this.cpuType = reader.readUnsignedInt();
      this.cpuSubType = reader.readUnsignedInt();
      this.fileType = reader.readUnsignedInt();
      this.commandCount = reader.readUnsignedInt();
      this.unknownValue = reader.readUnsignedInt();
      this.flags = reader.readUnsignedInt();
      this.commands.clear();
      for (int i = 0; i < commandCount; i++)
      {
         MachLoadCommand command = MachLoadCommand.readFromStream(reader);
         if (command != null)
            this.commands.add(command);
      }
      reader.close();
   }
}
