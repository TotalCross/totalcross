package tc.tools.deployer.ipa;

import java.io.IOException;

public class MachObjectFile64 extends MachObjectFile {
  protected long reserved;

  protected MachObjectFile64(byte[] data) throws IOException, InstantiationException, IllegalAccessException {
    super(data);
  }

  @Override
  protected void readHeader(ElephantMemoryReader reader) throws IOException {
    super.readHeader(reader);
    this.reserved = reader.readUnsignedIntLE();
  }
}
