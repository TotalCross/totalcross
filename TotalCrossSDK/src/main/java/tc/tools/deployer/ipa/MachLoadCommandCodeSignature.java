package tc.tools.deployer.ipa;

import java.io.IOException;

import tc.tools.deployer.ipa.blob.BlobHandler;
import tc.tools.deployer.ipa.blob.EmbeddedSignature;

public class MachLoadCommandCodeSignature extends MachLoadCommand
{
  protected EmbeddedSignature signature;
  protected long blobFileOffset;
  protected long blobFileSize;

  private int offset2FileSize;

  void updateFileSize(ElephantMemoryWriter writer, long filesize) throws IOException
  {
    this.blobFileSize = filesize;
    writer.memorize();
    writer.moveTo(offset2FileSize);
    writer.writeUnsignedIntLE(this.blobFileSize);
    writer.moveBack();
  }

  @Override
  protected void parseFromStream(ElephantMemoryReader reader) throws IOException, InstantiationException,
  IllegalAccessException
  {
    this.blobFileOffset = (int) reader.readUnsignedIntLE();
    this.offset2FileSize = reader.getPos();
    this.blobFileSize = (int) reader.readUnsignedIntLE();
    reader.memorize();
    reader.moveTo(this.blobFileOffset);
    this.signature = (EmbeddedSignature) BlobHandler.readBlob(reader);
    reader.moveBack();
  }
}
