package jdkcompat.io;

import java.io.IOException;

public interface Closeable4D extends AutoCloseable {
  @Override
  public void close() throws IOException;
}
