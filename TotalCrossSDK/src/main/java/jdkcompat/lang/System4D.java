package jdkcompat.lang;

import java.io.OutputStream;
import java.io.PrintStream;

import totalcross.sys.Vm;
import totalcross.util.concurrent.Lock;

public class System4D {
  private static class VmDebugStream extends PrintStream {
    Lock lock = new Lock();
    StringBuffer acc = new StringBuffer();

    public VmDebugStream() {
      super((OutputStream) new OutputStream() {

        @Override
        public void write(int b) {
        }
      });
    }

    @Override
    public void flush() {
      synchronized (lock) {
        Vm.debug(acc.toString());
        acc.setLength(0);
      }
    }

    @Override
    public void write(int b) {
      char c = (char) b;

      if (c == '\n') {
        flush();
      } else {
        acc.append(c);
      }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
      acc.append(new String(buf, off, len));

      int idx = acc.lastIndexOf("\n");

      if (idx != -1) {
        String trailing = acc.substring(idx);

        // flushes only the first part, ignores trailing part for now...
        acc.setLength(idx);
        flush();

        acc.append(trailing);
      }
    }

    @Override
    public void print(String s) {
      acc.append(s);
    }

    @Override
    public void println(String str) {
      synchronized (lock) {
        print(str);
        flush();	
      }
    }

    @Override
    public void println() {
      flush();
    }
  }
  private static final VmDebugStream instance = new VmDebugStream();

  public static final PrintStream err = instance;
  public static final PrintStream out = instance;
}
