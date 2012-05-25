package tc.tools.deployer.ipa;

import java.io.IOException;
import java.util.*;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

public class MachObjectFile
{
   private byte[] data;

   // the only value we use is commandCount, all others are kept only for future reference.
   protected long magic;
   protected long cpuType;
   protected long cpuSubType;
   protected long fileType;
   protected long commandCount;
   protected long unknownValue;
   protected long flags;

   public List commands = new ArrayList();

   public MachLoadCommandCodeSignature lc_signature = null;
   public MachLoadCommandSegment lc_segment = null;

   public MachObjectFile(byte[] data) throws IOException, InstantiationException, IllegalAccessException
   {
      this.data = data;
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
         {
            this.commands.add(command);
            if (lc_signature == null && command instanceof MachLoadCommandCodeSignature)
               lc_signature = (MachLoadCommandCodeSignature) command;
            if (lc_segment == null && command instanceof MachLoadCommandSegment)
            {
               lc_segment = (MachLoadCommandSegment) command;
               if (!lc_segment.segname.startsWith("__LINKEDIT"))
                  lc_segment = null;
            }
         }
      }
      reader.close();

      if (lc_segment == null)
         throw new RuntimeException("Did not find a Mach segment load command for the __LINKEDIT segment");
      if (lc_signature == null)
         throw new RuntimeException("Did not find a Code Signing LC. Injecting one into a fresh executable is not currently supported.");
      if ((lc_signature.blobFileOffset + lc_signature.blobFileSize) != (lc_segment.fileoff + lc_segment.filesize))
         throw new RuntimeException("Code Signing LC was present but not at the end of the __LINKEDIT segment, unable to replace it");
   }

   public ElephantMemoryWriter writer;

   public void resign(EmbeddedSignature signature) throws IOException
   {
      ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
      this.writer = writer;

      byte[] blobBytes = signature.GetBlobBytes();

      // original size - size of the original signature + size of the new signature
      lc_segment.updateFileSize(writer, lc_segment.filesize - lc_signature.blobFileSize + blobBytes.length);
      lc_signature.updateFileSize(writer, blobBytes.length);
   }
}
