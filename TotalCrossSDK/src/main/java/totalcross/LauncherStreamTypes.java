// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import tc.tools.JarClassPathLoader;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;
import totalcross.preview.AppletPreviewSurface;
import totalcross.preview.AwtWindowBackend;
import totalcross.preview.PreviewRuntime;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;

@SuppressWarnings({"deprecation", "removal"})
abstract class LauncherStreamTypes extends Panel implements WindowListener, KeyListener,
    java.awt.event.MouseListener, MouseWheelListener, MouseMotionListener, ComponentListener {
  protected class AlertBox extends Frame implements java.awt.event.ActionListener {
    protected java.awt.Button ok;
    protected java.awt.TextArea ta;

    public AlertBox() {
      super("Alert");
      setLayout(new BorderLayout());
      add("Center", ta = new java.awt.TextArea());
      Panel p = new Panel();
      p.setLayout(new FlowLayout());
      p.add(ok = new java.awt.Button("Ok"));
      ok.addActionListener(this);
      add("South", p);
      pack();
      Dimension d = getToolkit().getScreenSize();
      setLocation(d.width / 3, d.height / 3);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ae) {
      if (ae.getSource() == ok) {
        setVisible(false);
      }
    }

    public void setText(String s) {
      ta.setText(s);
    }
  }

  public static class IS2S extends totalcross.io.Stream {
    InputStream is;

    public IS2S(InputStream is) {
      this.is = is;
    }

    @Override
    public void close() {
      try {
        is.close();
      } catch (Exception e) {
      }
      is = null;
    }

    @Override
    public int readBytes(byte[] buf, int start, int count) {
      try {
        return is.read(buf, start, count);
      } catch (Exception e) {
        return -1;
      }
    }

    @Override
    public int writeBytes(byte[] buf, int start, int count) {
      return 0; // not supported
    }
  }

  public static class S2FIS extends java.io.FilterInputStream {
    protected RandomAccessStream s;
    protected int pos = -1;
    protected int readLimit = -1;

    public S2FIS(RandomAccessStream s) {
      this(s, -1, true);
    }

    public S2FIS(RandomAccessStream s, int max) {
      this(s, max, true);
    }

    public S2FIS(RandomAccessStream s, int max, boolean closeUnderlying) {
      super(new S2IS(s, max, closeUnderlying));
      this.s = s;
    }

    @Override
    public synchronized void mark(int readlimit) {
      try {
        this.pos = s.getPos();
        this.readLimit = readlimit;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public synchronized void reset() throws java.io.IOException {
      if (this.pos == -1) {
        throw new java.io.IOException("the stream has not been marked");
      }
      if (s.getPos() - this.pos > readLimit) {
        throw new java.io.IOException("the mark has been invalidated");
      }
      s.setPos(this.pos);
    }

    @Override
    public boolean markSupported() {
      return true;
    }
  }

  public static class S2IS extends java.io.InputStream {
    protected Stream s;
    protected byte[] oneByte = new byte[1];
    protected int left;
    protected boolean closeUnderlying;

    public S2IS(Stream s) {
      this(s, -1, true);
    }

    public S2IS(Stream s, int max) {
      this(s, max, true);
    }

    public S2IS(Stream s, int max, boolean closeUnderlying) {
      this.s = s;
      this.left = max;
      this.closeUnderlying = closeUnderlying;
    }

    @Override
    public int read() throws java.io.IOException {
      if (left == 0) {
        return -1;
      }

      try {
        int r = s.readBytes(oneByte, 0, 1);

        if (left != -1 && r == 1) {
          left--;
        }

        return r > 0 ? ((int) oneByte[0] & 0xFF) : -1;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws java.io.IOException {
      if (left == 0) {
        return -1;
      }

      try {
        if (left != -1 && len > left) {
          len = left;
        }

        int r = s.readBytes(buf, off, len);

        if (left != -1 && r > 0) {
          left -= r;
        }

        return r;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void close() throws java.io.IOException {
      if (closeUnderlying) {
        try {
          s.close();
        } catch (IOException e) {
          throw new java.io.IOException(e.getMessage());
        }
      }
    }
  }

  public static class S2OS extends java.io.OutputStream {
    protected Stream s;
    protected byte[] oneByte = new byte[1];
    protected int count;
    protected boolean closeUnderlying;

    public S2OS(Stream s) {
      this(s, true);
    }

    public S2OS(Stream s, boolean closeUnderlying) {
      this.s = s;
      this.closeUnderlying = closeUnderlying;
    }

    public int count() {
      return count;
    }

    @Override
    public void write(int b) throws java.io.IOException {
      try {
        oneByte[0] = (byte) (b & 0xFF);

        int c = s.writeBytes(oneByte, 0, 1);
        if (c < 0) {
          throw new java.io.IOException("Unknown error when writing to stream");
        }
        count++;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws java.io.IOException {
      try {
        int c = s.writeBytes(b, off, len);
        if (c < 0) {
          throw new java.io.IOException("Unknown error when writing to stream");
        }
        count += c;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void close() throws java.io.IOException {
      if (closeUnderlying) {
        try {
          s.close();
        } catch (IOException e) {
          throw new java.io.IOException(e.getMessage());
        }
      }
    }
  }


}
