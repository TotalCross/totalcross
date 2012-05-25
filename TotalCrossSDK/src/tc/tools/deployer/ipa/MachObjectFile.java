package tc.tools.deployer.ipa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cms.CMSException;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

public class MachObjectFile
{
   public byte[] data;

   // the only value we use is commandCount, all others are kept only for future reference.
   protected long magic;
   protected long cpuType;
   protected long cpuSubType;
   protected long fileType;
   protected long commandCount;
   protected long unknownValue;
   protected long flags;

   private List commands = new ArrayList();

   private MachLoadCommandCodeSignature lc_signature = null;
   private MachLoadCommandSegment lc_segment = null;

   private byte[] signatureTemplate;

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
         throw new RuntimeException(
               "Did not find a Code Signing LC. Injecting one into a fresh executable is not currently supported.");
      if ((lc_signature.blobFileOffset + lc_signature.blobFileSize) != (lc_segment.fileoff + lc_segment.filesize))
         throw new RuntimeException(
               "Code Signing LC was present but not at the end of the __LINKEDIT segment, unable to replace it");
   }

   public EmbeddedSignature getEmbeddedSignature()
   {
      return lc_signature.signature;
   }

   public void setEmbeddedSignature(EmbeddedSignature signature) throws IOException
   {
      ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
      signatureTemplate = signature.GetBlobBytes();

      lc_signature.signature = signature;
      // original size - size of the original signature + size of the new signature
      lc_segment.updateFileSize(writer, lc_segment.filesize - lc_signature.blobFileSize + signatureTemplate.length);
      lc_signature.updateFileSize(writer, signatureTemplate.length);

      this.data = writer.buffer;
   }

   public void resign() throws IOException, CMSException
   {
      lc_signature.signature.sign();
      byte[] resignedData = lc_signature.signature.GetBlobBytes();
      if (signatureTemplate.length != resignedData.length)
         throw new IllegalStateException(
               "CMS signature blob changed size between practice run and final run, unable to create useful code signing data");

      ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
      writer.memorize();
      writer.moveTo(lc_signature.blobFileOffset);
      writer.write(resignedData);
      writer.moveBack();

      int actualSize = writer.size();
      int expectedSize = (int) (lc_segment.filesize + lc_segment.fileoff);
      if (actualSize < expectedSize)
         throw new IllegalStateException("Data written is smaller than expected, unable to finish signing process");
      else if (actualSize == expectedSize)
         this.data = writer.buffer;
      else
      {
         this.data = new byte[expectedSize];
         System.arraycopy(writer.buffer, 0, this.data, 0, expectedSize);
      }
   }
}
