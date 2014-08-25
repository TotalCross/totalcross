package tc.tools.deployer.ipa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cms.CMSException;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

/**
 * http://llvm.org/docs/doxygen/html/MachOFormat_8h_source.html
 * http://comments.gmane.org/gmane.comp.programming.garbage-collection.boehmgc/4987
 */
public class MachObjectFile
{
   protected long magic;
   protected long cputype;
   protected long cpusubtype;
   protected long filetype;
   protected long ncmds;
   protected long sizeofcmds;
   protected long flags;

   public byte[] data;

   private List<MachLoadCommand> commands = new ArrayList<MachLoadCommand>();

   private MachLoadCommandCodeSignature lc_signature = null;
   private MachLoadCommandSegment lc_segment = null;

   private byte[] signatureTemplate;

   public MachObjectFile(byte[] data) throws IOException, InstantiationException, IllegalAccessException
   {
      this.data = data;
      ElephantMemoryReader reader = new ElephantMemoryReader(data);

      this.magic = reader.readUnsignedIntLE();
      this.cputype = reader.readUnsignedIntLE();
      this.cpusubtype = reader.readUnsignedIntLE();
      this.filetype = reader.readUnsignedIntLE();
      this.ncmds = reader.readUnsignedIntLE();
      this.sizeofcmds = reader.readUnsignedIntLE();
      this.flags = reader.readUnsignedIntLE();
      this.commands.clear();
      for (int i = 0; i < ncmds; i++)
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

      if (lc_segment == null || lc_signature == null || (lc_signature.blobFileOffset + lc_signature.blobFileSize) != (lc_segment.fileoff + lc_segment.filesize))
         throw new RuntimeException("Template IPA files appears to be corrupted, please reinstall the SDK and try again");
   }

   public EmbeddedSignature getEmbeddedSignature()
   {
      return lc_signature.signature;
   }

   public void setEmbeddedSignature(EmbeddedSignature signature) throws IOException
   {
      ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
      signatureTemplate = signature.getBytes();

      lc_signature.signature = signature;
      // original size - size of the original signature + size of the new signature
      lc_segment.updateFileSize(writer, lc_segment.filesize - lc_signature.blobFileSize + signatureTemplate.length);
      lc_signature.updateFileSize(writer, signatureTemplate.length);

      this.data = writer.buffer;
   }

   public void resign() throws IOException, CMSException
   {
      lc_signature.signature.sign();
      byte[] resignedData = lc_signature.signature.getBytes();
      if (signatureTemplate.length != resignedData.length)
         throw new IllegalStateException("Failed to resign the file, please try again.");

      ElephantMemoryWriter writer = new ElephantMemoryWriter(data);
      writer.memorize();
      writer.moveTo(lc_signature.blobFileOffset);
      writer.write(resignedData);
      writer.moveBack();

      int actualSize = writer.size();
      int expectedSize = (int) (lc_segment.filesize + lc_segment.fileoff);
      if (actualSize < expectedSize)
         throw new IllegalStateException("Generated file appears to be missing data, please try again.");
      else if (actualSize == expectedSize)
         this.data = writer.buffer;
      else
      {
         this.data = new byte[expectedSize];
         System.arraycopy(writer.buffer, 0, this.data, 0, expectedSize);
      }
   }
}
