// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc;

import totalcross.io.DataStream;
import totalcross.io.EOFException;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.BorderContainer;
import totalcross.ui.Check;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.MultiEdit;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.ScrollContainer;
import totalcross.ui.ShadedButton;
import totalcross.ui.Spinner;
import totalcross.ui.Switch;
import totalcross.ui.TabbedContainer;
import totalcross.ui.Toast;
import totalcross.ui.UIColors;
import totalcross.ui.dialog.FileChooserBox;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.MouseEvent;
import totalcross.ui.event.MouseListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class Help extends MainWindow {
  public static void main(String[] args) {
    totalcross.Launcher.main(new String[] { "/help" });
  }

  public Help() {
    setUIStyle(Settings.Android);
    setBackColor(UIColors.controlsBack = Color.WHITE);
    Settings.uiAdjustmentsBasedOnFontHeight = true;
  }

  private static final int COLOR = 0x188AD0;
  private final int Orange = 0xF68E16, GREEN = 0x75A827;
  private boolean isEn = true;

  private String x(String en, String pt) {
    return isEn || pt == null ? en : pt;
  }

  TabbedContainer tc;
  Label lstatus;
  Edit edclass, edkey, edpath;
  Radio rEn, rPt;
  RunContainer rc;
  DepContainer dc;
  RadioGroupController rgLang;
  ShadedButton btpath, btHtml, btBlog, btPdf;
  Spinner spin;
  Image fimg;

  @Override
  public void initUI() {
    Settings.disableScreenRotation = true; // valid only on JavaSE
    Toast.backColor = Color.YELLOW;
    Toast.foreColor = 0;
    Toast.posY = BOTTOM - 100;
    reload(true);
  }

  private void reload(boolean load) {
    try {
      fimg = new Image("btnh.png");
      Label l;
      if (load) {
        loadConfig();
      } else {
        saveConfig();
      }
      removeAll();
      ImageControl ic;
      Container c0 = new Container();
      add(c0, LEFT + 50, TOP + 50, FILL - 50, FILL - 50);

      c0.add(ic = new ImageControl(new Image("logoh.png")), LEFT, TOP);
      c0.add(new Label(x("Helper application to Run/Deploy", "Aplicação de ajuda para Executar / Empacotar"), CENTER,
          Color.BLACK, true), LEFT, AFTER + 50, ic);

      c0.add(btHtml = new ShadedButton("Javadocs", fimg, Color.WHITE, Orange), RIGHT, TOP, PARENTSIZE + 30,
          PREFERRED + 50);
      tip(btHtml, "Opens the html javadocs in your browser", "Abre os javadocs em html no seu navegador");
      c0.add(btBlog = new ShadedButton("Blog", fimg, Color.WHITE, Orange), SAME, AFTER + 50, SAME, SAME);
      btBlog.setBackColor(Orange);
      tip(btBlog, "Opens the TotalCross blog, a very useful technical source",
          "Abre o blog do TotalCross, uma fonte extremamente útil de informações");
      c0.add(btPdf = new ShadedButton("Companion", fimg, Color.WHITE, Orange), SAME, AFTER + 50, SAME, SAME);
      btPdf.setBackColor(Orange);
      tip(btPdf, "Opens the TotalCross Companion.pdf", "Abre o TotalCross Companion.pdf");
      btHtml.setFont(font.asBold());
      btBlog.setFont(font.asBold());
      btPdf.setFont(font.asBold());

      rgLang = new RadioGroupController();
      c0.add(rPt = new Radio("Português", rgLang), RIGHT - 150, AFTER + 50);
      tip(rPt, "Click here to set the user interface to Portuguese",
          "Clique aqui para mudar a linguagem para português");
      c0.add(rEn = new Radio("English", rgLang), BEFORE - 50, SAME);
      tip(rEn, "Click here to set the user interface to English", "Clique aqui para mudar a linguagem para inglês");
      c0.add(l = new Label(x("Language: ", "Linguagem: ")), BEFORE - 100, SAME);
      l.transparentBackground = rPt.transparentBackground = rEn.transparentBackground = true;

      Spinner.spinnerType = Spinner.IPHONE;
      c0.add(spin = new Spinner(), LEFT + 50, SAME - 50, FONTSIZE + 150, FONTSIZE + 150);
      spin.setVisible(false);

      c0.add(new Label(x("Class name: ", "Nome da classe: ")), LEFT, AFTER + 25);
      c0.add(edclass = new Edit(), AFTER, SAME);
      tip(edclass,
          "Type the full class name of the class that extends MainWindow. Don't forget to include the package.",
          "Digite o nome (com o pacote) da classe que estende MainWindow");
      c0.add(l = new Label(x("Class folder: ", "Pasta dos .class: ")), LEFT, AFTER + 25);
      c0.add(btpath = new ShadedButton(x(" Select ", " Selecionar "), fimg, Color.BLACK, GREEN), RIGHT, SAME);
      tip(btpath, "Press this button to select the folder where the .class is located",
          "Pressione esse botão para selecionar a pasta onde os arquivos .class estão localizados");
      c0.add(edpath = new Edit(), AFTER, SAME, FIT - 25, PREFERRED, l);
      tip(edpath, "Type the .class folder or press the Select button",
          "Digite a pasta dos .class ou clique no botão Selecionar");

      c0.add(new Label(x("Key: ", "Chave: ")), LEFT, AFTER + 25);
      c0.add(edkey = new Edit(), AFTER, SAME);
      tip(edkey, "Type the 24-characters registration key that you received by email",
          "Digite a chave com 24 caracteres que você recebeu por email");

      lstatus = new Label("", CENTER);
      lstatus.setBackForeColors(Color.brighter(COLOR, 32), 0);
      lstatus.setFont(font.asBold());
      lstatus.autoSplit = true;
      c0.add(lstatus, LEFT, BOTTOM, FILL, FONTSIZE + 200);

      tc = new TabbedContainer(new String[] { x("Run", "Executar"), x("Deploy", "Empacotar"), "Console" });
      tc.useBorder2 = true;
      tc.setBackColor(0xAAAAAA);
      tc.activeTabBackColor = COLOR;
      tc.setCaptionColor(Color.WHITE);
      tc.unselectedTextColor = 0;
      tc.allSameWidth = true;
      c0.add(tc, LEFT, AFTER + 25, FILL, FIT, edkey);
      tc.setContainer(0, rc = new RunContainer());
      tc.setContainer(1, dc = new DepContainer());
      tc.setContainer(2, cc = new ConsoleContainer());

      if (load) {
        tc.setActiveTab(tab);
      }
      configs[sel].toUI();
      rgLang.setSelectedIndex(isEn ? 1 : 0, false);
    } catch (Exception e) {
      MessageBox.showException(e, true);
      exit(1);
    }
  }

  Config[] configs;
  int tab, sel;
  ConsoleContainer cc;

  private void loadConfig() {
    File f = null;
    try {
      f = new File(Settings.appPath + "/help.dat", File.READ_WRITE);
      DataStream ds = new DataStream(f);
      /*int ver = */ds.readByte();
      int lang = ds.readByte();
      tab = ds.readByte();
      int n = ds.readByte();
      sel = ds.readByte();
      configs = new Config[n];
      for (int i = 0; i < n; i++) {
        configs[i] = new Config(ds);
      }
      isEn = lang == 0;
    } catch (FileNotFoundException | EOFException fnfe) {
      configs = new Config[] { new Config() };
    } catch (Exception ee) {
      handleException(ee);
    }
    if (f != null) {
      try {
        f.close();
      } catch (Exception e) {
      }
    }
  }

  private void saveConfig() {
    File f = null;
    try {
      f = new File(Settings.appPath + "/help.dat", File.CREATE_EMPTY);
      DataStream ds = new DataStream(f);
      ds.writeByte(1);
      ds.writeByte(isEn ? 0 : 1);
      int t = tc.getActiveTab();
      if (t == 2) {
        t = tc.lastActiveTab; // dont save console as active tab
      }
      ds.writeByte(t);
      int n = 1;
      ds.writeByte(n); // number of configs
      ds.writeByte(0); // active config
      for (int i = 0; i < n; i++) {
        configs[i].toFile(ds);
      }
      f.close();
    } catch (FileNotFoundException fnfe) {
    } catch (Exception ee) {
      handleException(ee);
    }
    if (f != null) {
      try {
        f.close();
      } catch (Exception e) {
      }
    }
  }

  private void tip(Control c, String en, String pt) {
    final String s = x(en, pt);
    c.addMouseListener(new MouseListener() {
      @Override
      public void mouseWheel(MouseEvent e) {
      }

      @Override
      public void mouseMove(MouseEvent e) {
      }

      @Override
      public void mouseOut(MouseEvent e) {
        lstatus.setText("");
      }

      @Override
      public void mouseIn(MouseEvent e) {
        lstatus.setText(s);
      }
    });
  }

  @Override
  public void onExit() {
    saveConfig();
  }

  @Override
  public void onEvent(Event e) {
    try {
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == btpath) {
          if (edclass.getTrimmedLength() == 0) {
            Toast.show(x("Please type the class name before selecting the path",
                "Digite o nome da classe antes de selecionar a pasta"), 3000);
          } else {
            selectPath(edpath, x("Select the .class' path", "Selecione a pasta dos arquivos .class"));
          }
        } else if (e.target == rEn || e.target == rPt) {
          isEn = e.target == rEn;
          reload(false);
        } else if (e.target == btHtml) {
          Vm.exec("viewer", getTCPath() + "/docs/html/index.html", 0, true);
        } else if (e.target == btPdf) {
          java.awt.Desktop.getDesktop().open(new java.io.File(getTCPath() + "/docs/TotalCross Companion.pdf"));
        } else if (e.target == btBlog) {
          Vm.exec("viewer", "http://www.totalcross.com/blog", 0, true);
        }
        break;
      }
    } catch (Exception ee) {
      handleException(ee);
    }
  }

  private String getTCPath() {
    String ret = System.getenv("TOTALCROSS3_HOME");
    if (ret == null) {
      ret = System.getenv("TOTALCROSS_HOME");
    }
    if (ret == null) {
      String tcjar = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
      if (tcjar.startsWith("/")) {
        tcjar = tcjar.substring(1);
      }
      ret = "\\TotalCross3";
      try {
        ret = new java.io.File(tcjar).getParent().toString();
      } catch (Exception ee) {
        handleException(ee);
      }
    }
    return ret.replace('\\', '/');
  }

  class Config {
    // run
    String path, className, key, xpos, ypos, width, height, fontsize;
    int rdsel = -1;
    boolean showMouse, fast;
    String scale, bpp, cmdline;
    // deploy
    boolean wmo, w32, lin, apl, ios, and, all = true, inst, pack;
    String pathd;

    public Config() {
    }

    public Config(DataStream ds) throws IOException {
      // run
      path = ds.readString();
      className = ds.readString();
      key = ds.readString();
      xpos = ds.readString();
      ypos = ds.readString();
      rdsel = ds.readByte();
      width = ds.readString();
      height = ds.readString();
      bpp = ds.readString();
      fontsize = ds.readString();
      scale = ds.readString();
      fast = ds.readBoolean();
      cmdline = ds.readString();
      showMouse = ds.readBoolean();
      // deploy
      w32 = ds.readBoolean();
      wmo = ds.readBoolean();
      lin = ds.readBoolean();
      apl = ds.readBoolean();
      ios = ds.readBoolean();
      and = ds.readBoolean();
      all = ds.readBoolean();
      inst = ds.readBoolean();
      pack = ds.readBoolean();
      pathd = ds.readString();
    }

    public void toFile(DataStream ds) throws IOException {
      fromUI();
      // run
      ds.writeString(path);
      ds.writeString(className);
      ds.writeString(key);
      ds.writeString(xpos);
      ds.writeString(ypos);
      ds.writeByte(rdsel);
      ds.writeString(width);
      ds.writeString(height);
      ds.writeString(bpp);
      ds.writeString(fontsize);
      ds.writeString(scale);
      ds.writeBoolean(fast);
      ds.writeString(cmdline);
      ds.writeBoolean(showMouse);
      // deploy
      ds.writeBoolean(w32);
      ds.writeBoolean(wmo);
      ds.writeBoolean(lin);
      ds.writeBoolean(apl);
      ds.writeBoolean(ios);
      ds.writeBoolean(and);
      ds.writeBoolean(all);
      ds.writeBoolean(inst);
      ds.writeBoolean(pack);
      ds.writeString(pathd);
    }

    public void toUI() {
      // run
      edpath.setText(path);
      edclass.setText(className);
      edkey.setText(key);
      rc.edX.setText(xpos);
      rc.edY.setText(ypos);
      rc.rg.setSelectedIndex(rdsel, false);
      rc.edW.setText(width);
      rc.edH.setText(height);
      rc.cbBpp.setSelectedItem(bpp);
      rc.edFS.setText(fontsize);
      rc.cbSc.setSelectedItem(scale);
      rc.swSc.setOn(fast);
      rc.edCmd.setText(cmdline);
      rc.chM.setChecked(showMouse);
      if (rdsel == -1) {
        rc.rg.setSelectedIndex(3, true);
      }
      // deploy
      dc.w32.setChecked(w32);
      dc.wmo.setChecked(wmo);
      dc.lin.setChecked(lin);
      dc.apl.setChecked(apl);
      dc.ios.setChecked(ios);
      dc.and.setChecked(and);
      dc.all.setChecked(all);
      dc.inst.setChecked(inst);
      dc.pack.setChecked(pack);
      dc.edpathd.setText(pathd);
    }

    public void fromUI() {
      // run
      path = edpath.getText();
      className = edclass.getText();
      key = edkey.getText();
      xpos = rc.edX.getText();
      ypos = rc.edY.getText();
      rdsel = rc.rg.getSelectedIndex();
      width = rc.edW.getText();
      height = rc.edH.getText();
      bpp = (String) rc.cbBpp.getSelectedItem();
      fontsize = rc.edFS.getText();
      scale = (String) rc.cbSc.getSelectedItem();
      fast = rc.swSc.isOn();
      cmdline = rc.edCmd.getText();
      showMouse = rc.chM.isChecked();
      // deploy
      w32 = dc.w32.isChecked();
      wmo = dc.wmo.isChecked();
      lin = dc.lin.isChecked();
      apl = dc.apl.isChecked();
      ios = dc.ios.isChecked();
      and = dc.and.isChecked();
      all = dc.all.isChecked();
      inst = dc.inst.isChecked();
      pack = dc.pack.isChecked();
      pathd = dc.edpathd.getText();
    }

    private String initRD(String path) {
      StringBuilder sb = new StringBuilder(256);
      String javapath = System.getenv("JAVA_HOME");
      if (javapath != null) {
        sb.append(javapath);
        if (!javapath.contains("bin")) {
          sb.append("\\bin\\");
        } else {
          sb.append("\\");
        }
      }
      sb.append("java.exe");
      String tcjar = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
      if (tcjar.startsWith("/")) {
        tcjar = tcjar.substring(1);
      }
      sb.append(" -classpath " + path + java.io.File.pathSeparator + tcjar);
      return sb.toString();
    }

    private String getKey() {
      return key.startsWith("%") ? System.getenv(key.substring(1, key.length() - 1)) : key;
    }

    private void deploy() {
      try {
        fromUI();
        // to run: classname=tc.samples.api.TotalCrossAPI, class folder=G:\TotalCross\TotalCrossSDK
        // to deploy: classname = TotalCrossAPI, class folder=G:\TotalCross\TotalCrossSDK\tc\samples\api
        int idx = className.lastIndexOf('.');
        String cn = idx == -1 ? className : className.substring(idx + 1);
        String pn = idx == -1 ? path : Convert.appendPath(path, className.substring(0, idx).replace('.', '/'));

        String java = initRD(pn);
        StringBuilder sb = new StringBuilder(512);
        sb.append(" ").append(cn).append(".class");
        sb.append(" /r " + getKey());
        if (all) {
          sb.append(" -all");
        } else {
          if (lin) {
            sb.append(" -linux");
          }
          if (apl) {
            sb.append(" -applet");
          }
          if (ios) {
            sb.append(" -ios");
          }
          if (and) {
            sb.append(" -android");
          }
          if (w32) {
            sb.append(" -win32");
          }
          if (wmo) {
            sb.append(" -winmo");
          }
        }
        if ((ios || all) && !pathd.isEmpty()) {
          sb.append(" /m ").append(pathd);
        }
        if ((and || all) && inst) {
          sb.append(" /i android");
        }
        if (pack) {
          sb.append(" /p");
        }
        exec(java, " tc.Deploy ", sb, pn, true);
      } catch (Exception ee) {
        handleException(ee);
      }
    }

    private void run() {
      try {
        fromUI();
        String java = initRD(path);
        StringBuilder sb = new StringBuilder(512);
        sb.append(" /r " + getKey());
        switch (rdsel) {
        case 1:
          sb.append(" /scr win32");
          break;
        case 2:
          sb.append(" /scr wince");
          break;
        case 3:
          sb.append(" /scr android");
          break;
        case 4:
          sb.append(" /scr iphone");
          break;
        case 0:
          if (!width.isEmpty() && !height.isEmpty() && !bpp.isEmpty()) {
            sb.append(" /scr " + width + "x" + height + "x" + bpp);
          }
          if (!fontsize.isEmpty()) {
            sb.append(" /fontsize ").append(fontsize);
          }
          break;
        }
        if (!xpos.isEmpty() && !ypos.isEmpty()) {
          sb.append(" /pos " + xpos + "," + ypos);
        }
        if (!scale.equals("1")) {
          sb.append(fast ? " /fastscale " + scale : " /scale " + scale);
        }
        if (showMouse) {
          sb.append(" /showmousepos");
        }
        if (!cmdline.isEmpty()) {
          sb.append(" /cmd " + cmdline);
        }
        sb.append(" " + className.replace('/', '.'));

        exec(java, " totalcross.Launcher ", sb, path, false);
      } catch (Exception ee) {
        handleException(ee);
      }
    }
  }

  private void handleException(Throwable t) {
    t.printStackTrace();
    tc.setActiveTab(2);
    if (isEn) {
      cc.me.setText("Exception: " + t.getClass() + "\nMessage: " + t.getMessage() + "\n\n" + Vm.getStackTrace(t));
    } else {
      cc.me.setText("Exceção: " + t.getClass() + "\nMensagem: " + t.getMessage() + "\n\n" + Vm.getStackTrace(t));
    }
  }

  private void println(String s) {
    System.out.println(s);
    cc.me.setText(cc.me.getText() + s + "\n");
    cc.me.scrollToBottom();
  }

  public void exec(final String java, final String command, final StringBuilder sb, final String path,
      final boolean deploy) {
    spin.setVisible(true);
    spin.start();
    new Thread() // thread is needed to let spin run
    {
      @Override
      public void run() {
        try {
          String args = sb.toString();
          cc.me.setText("");
          println("To run from Eclipse: ");
          println(" Main class: " + command + "\n");
          println(" Arguments: " + args + "\n");
          println(" Working directory: " + path + "\n");
          tc.setActiveTab(2);
          String cmd = java + command + args;
          Process process = Runtime.getRuntime().exec(cmd, null, new java.io.File(path));
          java.io.InputStream inputStream = process.getInputStream();
          java.io.InputStream errorStream = process.getErrorStream();

          for (int i = 0, n = deploy ? 60000 / 250 : 5000 / 250; i < n; i++) {
            dump(inputStream, errorStream);
            try {
              process.exitValue();
              dump(inputStream, errorStream);
              break;
            } catch (Throwable throwable) {
              Thread.sleep(250);
            }
          }
          spin.stop();
          spin.setVisible(false);
          if (!deploy) {
            println(x(
                "NOTE THAT WHEN YOU RUN A PROGRAM OUTSIDE THE IDE, YOU ARE NOT ABLE TO DEBUG THE PROGRAM. YOU CAN USE THE COMMANDS ON THE TOP OF THIS CONSOLE TO CONFIGURE YOUR IDE TO CREATE A RUN/DEBUG CONFIGURATION ON YOUR FAVORITE IDE",
                "NOTE QUE QUANDO VOC� EXECUTA O APLICATIVO FORA DE UM AMBIENTE DE DESENVOLVIMENTO (IDE), VOCÊ NÃO CONSEGUE DEPURAR. VOCÊ PODE USAR OS COMANDOS DESCRITOS NO TOPO DESSE CONSOLE PARA CRIAR UMA CONFIGURAÇÃO DE RUN/DEBUG NA SUA IDE FAVORITA"));
          }
        } catch (Exception ee) {
          handleException(ee);
        }
      }
    }.start();
  }

  private void dump(java.io.InputStream inputStream, java.io.InputStream errorStream) throws Exception {
    String lineIn;
    if (inputStream.available() > 0) {
      while (inputStream.available() > 0 && (lineIn = readStream(inputStream)) != null) {
        println(lineIn);
      }
    }
    if (errorStream.available() > 0) {
      while (errorStream.available() > 0 && (lineIn = readStream(errorStream)) != null) {
        println("ERROR: " + lineIn + "\n");
      }
    }
  }

  public static byte bytebuf[] = new byte[4096];

  public static String readStream(java.io.InputStream is) throws Exception {
    int avail = is.available();
    byte[] buf = bytebuf.length >= avail ? bytebuf : new byte[avail];
    is.read(buf, 0, avail);
    return new String(buf, 0, avail).trim();
  }

  private boolean checkFilled() {
    String error = "";
    if (edpath.getTrimmedLength() == 0) {
      error += ", path to .class";
    }
    if (edclass.getTrimmedLength() == 0) {
      error += ", class name";
    }
    if (edkey.getTrimmedLength() == 0) {
      error += ", key";
    }
    if (error.startsWith(", ")) {
      error = error.substring(2);
    }
    if (!error.isEmpty()) {
      Toast.show(
          x("The following fields must be filled: " + error, "Os seguintes campos devem ser preenchidos: " + error),
          4000);
    }
    boolean ok = error.isEmpty();
    if (ok) // check if filename exists
    {
      String fn = Convert.appendPath(edpath.getText(), edclass.getText().replace('.', '/'));
      try {
        ok = new File(fn + ".class").exists();
      } catch (Exception ee) {
        handleException(ee);
      }
      if (!ok) {
        new MessageBox("Error",
            x("The class folder + class name does not lead to an existing filename: " + fn
                + ". Be sure that the class name contains the complete package and that the path name does NOT contains the package part. Examples: class name = my.app.MyApp and class folder = c:\\myapp\\bin",
                "A pasta da classe + nome da classe não resulta em um arquivo existente: " + fn
                    + ". Certifique-se que o nome da classe contém o pacote completo e que o caminho NÃO contém parte do pacote. Exemplo: nome da classe = meu.app.MeuAplicativo e pacote da classe = c:\\meuapp\\bin"))
                        .popup();
      }
    }
    return ok;
  }

  class ConsoleContainer extends ScrollContainer {
    MultiEdit me;

    public ConsoleContainer() {
      super(false, true);
    }

    @Override
    public void initUI() {
      add(me = new MultiEdit(), LEFT, TOP, FILL, FILL);
      me.drawDots = false;
      me.transparentBackground = true;
    }
  }

  class RunContainer extends ScrollContainer {
    Edit edX, edY, edW, edH, edFS, edCmd;
    ComboBox cbSc, cbBpp;
    Switch swSc;
    Radio rdC, rdA, rdI, rd32, rdCE;
    Check chM;
    ShadedButton btRun;
    RadioGroupController rg;

    public RunContainer() {
      super(false, true);
    }

    @Override
    public void initUI() {
      BorderContainer c = new BorderContainer(x("Screen settings", "Configurações da janela"));
      c.borderColor = COLOR;
      c.fillW = true;
      add(c, LEFT, TOP + 50, FILL, FONTSIZE + 900);

      BorderContainer bc1 = new BorderContainer(x("Position", "Posição"));
      c.add(bc1, LEFT, AFTER);
      bc1.add(new Label("X: "), LEFT, AFTER);
      bc1.add(edX = new Edit("99999"), AFTER, SAME);
      tip(edX, "Type the X position for the window. Leave blank to let the system set it",
          "Digite a posição X para a janela. Deixe em branco pro sistema posicionar");
      bc1.add(new Label("Y: "), LEFT, AFTER + 50);
      bc1.add(edY = new Edit("99999"), AFTER, SAME);
      tip(edY, "Type the Y position for the window. Leave blank to let the system set it",
          "Digite a posição Y para a janela. Deixe em branco pro sistema posicionar");
      bc1.finish();

      BorderContainer bc2 = new BorderContainer(x("Size", "Tamanho"));
      c.add(bc2, AFTER + 100, SAME, bc1);
      rg = new RadioGroupController();
      bc2.add(rdC = new Radio(x("Custom", "Customizado"), rg), SAME, AFTER);
      rdC.appId = 0;
      tip(rdC, "Fill the fields at right", "Preencha os campos ao lado");
      bc2.add(rd32 = new Radio("Win32", rg), SAME, AFTER + 25);
      rd32.appId = 1;
      tip(rd32, "Width=240, height=320, bpp=24", "Largura=240, altura=320, bpp=24");
      bc2.add(rdCE = new Radio("WinCE", rg), SAME, AFTER + 25);
      rdCE.appId = 2;
      tip(rdCE, "Width=240, height=320, bpp=8", "Largura=240, altura=320, bpp=8");
      bc2.add(rdA = new Radio("Android", rg), SAME, AFTER + 25);
      rdA.appId = 3;
      tip(rdA, "Width=320, height=480, bpp=24", "Largura=320, altura=480, bpp=24");
      bc2.add(rdI = new Radio("iOS", rg), SAME, AFTER + 25);
      rdI.appId = 4;
      tip(rdI, "Width=640, height=960, bpp=24, scale=0.75", "Largura=640, altura=960, bpp=24, escala=0.75");

      Label l1, l2, l3, l4; // store labels to make sure all fields are in the same X position
      bc2.add(l1 = new Label(x("Width: ", "Largura: ")), AFTER + 100, SAME, rdC);
      bc2.add(edW = new Edit("99999"), AFTER, SAME);
      tip(edW, "Type the width for the window", "Digite a largura da janela");
      bc2.add(l2 = new Label(x("Height: ", "Altura: ")), SAME, AFTER + 25, l1);
      bc2.add(edH = new Edit("99999"), AFTER, SAME);
      tip(edH, "Type the height for the window", "Digite a altura da janela");

      bc2.add(l3 = new Label(x("BitsPerPixel: ", "BitsPorPixel: ")), SAME, AFTER + 25, l2);
      bc2.add(cbBpp = new ComboBox(new String[] { "8", "16", "24", "32" }), AFTER, SAME);
      tip(cbBpp,
          "Select the bits per pixel to be used in the Window. All platforms uses 24bpp, except WinCE, which uses 8bpp",
          "Selecione os bits por pixel usado na janela. Todas as plataformas, menos WinCE, usam 24bpp");
      cbBpp.clearValueStr = "32";

      bc2.add(l4 = new Label(x("Font size: ", "Tam da letra: ")), SAME, AFTER + 25, l3);
      bc2.add(edFS = new Edit("99999"), AFTER, SAME);
      tip(edFS, "Type the font size. Leave it blank to use the default one",
          "Digite o tamanho da fonte. Deixe vazio pra usar o tamanho padrão");

      bc2.add(new Label(x("Scale: ", "Escala: ")), SAME, AFTER + 25, l4);
      bc2.add(cbSc = new ComboBox(new String[] { "0.25", "0.5", "0.75", "1", "2", "4", "6", "8" }), AFTER, SAME);
      tip(cbSc,
          "Select the scale to apply in the Window. Useful when you test screen sizes above your monitor's resolution",
          "Selecione a escala pra aplicar na janela. Útil quando usar tamanho de janela maior que a resolução do seu monitor");
      cbSc.setSelectedItem(cbSc.clearValueStr = "1");
      swSc = new Switch(true);
      swSc.setForeColor(GREEN);
      tip(swSc, "Scale slow has a good appearance, while fast scale has a worst appearance",
          "Escala lenta tem boa aparência, e escala devagar tem uma pior aparência");
      swSc.textBackOn = x(" fast and worst", "rápida");
      swSc.textBackOff = x(" slow and good", "lenta");
      bc2.add(swSc, AFTER + 50, SAME);

      bc2.finish();
      c.finish();
      add(new Label(x("Command line to pass to application: ", "Linha de comandos para aplicação")), LEFT, AFTER + 50);
      add(edCmd = new Edit(), LEFT, AFTER);
      tip(edCmd, "You can pass extra arguments to application and retrieve it using MainWindow.getCommandLine()",
          "Você pode passar argumentos extras pra aplicação e recuperá-los usando MainWindow.getCommandLine()");
      add(chM = new Check(x("Show mouse position", "Mostrar posição do mouse")), LEFT, AFTER + 25);
      tip(chM, "Show the mouse position on window's title area", "Mostra a posição do mouse no título da janela");
      btRun = new ShadedButton(x("Run the application", "Executar a aplicação"), fimg, Color.WHITE, COLOR);
      btRun.setFont(font.asBold());
      add(btRun, CENTER, AFTER + 100, PARENTSIZE + 80, PREFERRED + 50);
      tip(btRun, "Fill the fields above and press this button to run the application",
          "Preencha os campos acima e clique nesse botão para executar a aplicação");
    }

    @Override
    public void onEvent(Event e) {
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == btRun && checkFilled()) {
          configs[sel].run();
        } else if (e.target instanceof Radio) {
          int w, h, bpp = 24;
          String s = "1";
          switch (((Control) e.target).appId) {
          case 2: // wince
            bpp = 8;
          case 1: // win32
            w = 240;
            h = 320;
            break;
          case 3: // android
            w = 320;
            h = 480;
            break;
          case 4: // ios
            w = 640;
            h = 960;
            s = "0.75";
            break;
          default:
            edW.clear();
            edH.clear();
            cbBpp.clear();
            cbSc.clear();
            edFS.clear();
            return;
          }
          edW.setText(String.valueOf(w));
          edH.setText(String.valueOf(h));
          cbBpp.setSelectedItem(String.valueOf(bpp));
          cbSc.setSelectedItem(s);
        }
        break;
      }
    }
  }

  class DepContainer extends ScrollContainer {
    Check wmo, w32, lin, apl, ios, and, all, inst, pack;
    Edit edpathd;
    ShadedButton btpath, btDep, btfol;

    public DepContainer() {
      super(false, true);
    }

    @Override
    public void initUI() {
      Label l;
      BorderContainer c = new BorderContainer(x("Platforms", "Plataformas"));
      c.borderColor = COLOR;
      c.fillW = true;
      add(c, LEFT, TOP + 50);

      c.add(all = new Check(x("All the ones listed below", "Todas as listadas abaixo")), LEFT + 100, AFTER);
      all.appId = 1;
      tip(all, "Deploys for all platforms", "Faz o deploy para todas as plataformas");

      BorderContainer b = new BorderContainer("Desktop");
      c.add(b, LEFT + 100, AFTER + 50);
      b.add(w32 = new Check("Win32 "), LEFT, AFTER);
      w32.appId = 2;
      tip(w32, "Deploys for Windows 32 desktop", "Faz o deploy para Windows 32 desktop");
      b.add(lin = new Check("Linux "), AFTER, SAME);
      lin.appId = 2;
      tip(lin, "Deploys for linux desktop", "Faz o deploy para Linux desktop");
      b.add(apl = new Check("Applet"), LEFT, AFTER + 25);
      apl.appId = 2;
      tip(apl, "Deploys for applet to run in a browser", "Faz o deploy para executar no navegador");
      b.finish();

      BorderContainer b2 = new BorderContainer(x("Mobile", "M�vel"));
      c.add(b2, AFTER + 100, SAME, b);
      b2.add(ios = new Check("iOS"), LEFT + 50, AFTER);
      ios.appId = 2;
      tip(ios, "Deploys for iOS", "Faz o deploy para iOS");
      b2.add(wmo = new Check("Win Mobile"), AFTER + 50, SAME);
      wmo.appId = 2;
      tip(wmo, "Deploys for Windows Mobile 5, 6 and 7", "Faz o deploy para Windows Mobile 5, 6 e 7");
      b2.add(and = new Check("Android"), SAME, BEFORE - 25);
      and.appId = 2;
      tip(and, "Deploys for Android 2.3 or greater", "Faz o deploy para Android 2.3 ou superior");
      b2.finish();
      c.finish();

      add(l = new Label(x("Mobile provision folder: ", "Pasta do mobile provision: ")), LEFT, AFTER + 50);
      add(btpath = new ShadedButton(x(" Select ", " Selecionar "), fimg, Color.BLACK, GREEN), RIGHT, SAME);
      tip(btpath, "Selects the folder where the iOS mobile provision is (required for iOS)",
          "Seleciona a pasta onde est�o os arquivos do mobile provision (requerido para o iOS)");
      add(edpathd = new Edit(), AFTER, SAME, FIT - 25, PREFERRED, l);

      add(inst = new Check(x("Install on device (android)", "Instalar no equipamento (android)")), LEFT,
          AFTER + 50);
      tip(inst, "Install the package if device is connected. You must only select android",
          "Instala o pacote se o equipamento estiver conectado. Voc� dever� selecionar somente android");

      add(pack = new Check(x("Package the vm with application", "Empacota a VM com a aplica��o")), LEFT, AFTER + 50);
      tip(pack,
          "Inserts the VM inside the application's package (android and Windows Mobile); always true for other platforms",
          "Embute a VM no pacote da aplica��o (android e Windows Mobile); sempre embute para as outras plataformas");

      btDep = new ShadedButton(x("Deploy the application", "Empacotar a aplica��o"), fimg, Color.WHITE, COLOR);
      btDep.setBackColor(COLOR);
      add(btDep, LEFT, AFTER + 100, PARENTSIZE + 60, PREFERRED + 50);
      tip(btDep, "Fill the fields above and press this button to deploy the application",
          "Preencha os campos acima e clique nesse bot�o para empacotar a aplica��o");

      btfol = new ShadedButton(x("Install path", "Pasta da instala��o"), fimg, Color.WHITE, GREEN);
      btfol.setFont(font.asBold());
      btfol.setBackColor(0xAAFF00);
      add(btfol, RIGHT, SAME, PARENTSIZE + 30, PREFERRED + 50);
      tip(btfol, "Opens the path where the packages were created", "Abre a pasta onde est�o os pacotes para instalar");
    }

    @Override
    public void onEvent(Event e) {
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == all && all.isChecked()) {
          w32.clear();
          lin.clear();
          apl.clear();
          ios.clear();
          and.clear();
          wmo.clear();
        } else if (e.target instanceof Check && ((Check) e.target).appId == 2) {
          if (((Check) e.target).isChecked()) {
            all.clear();
          }
          if (e.target == ios && edpathd.getLength() == 0) {
            Toast.show(x("You must set the Mobile Provision path", "Voc� deve especificar a pasta do Mobile Provision"),
                3000);
          }
        } else if (e.target == btpath) {
          selectPath(edpathd, x("Select the Mobile Provision's path", "Selecione a pasta do Mobile Provision"));
        } else if (e.target == btDep && checkFilled()) {
          configs[sel].deploy();
        } else if (e.target == btfol && !edpath.getText().isEmpty()) {
          Vm.exec("viewer", edpath.getText().replace('\\', '/') + "/install", 0, true);
        }
        break;
      }
    }
  }

  private void selectPath(Edit ed, String tit) {
    FileChooserBox fb = new FileChooserBox(tit, new String[] { x("Select", "Selecionar"), x("Cancel", "Cancelar") },
        new FileChooserBox.Filter() {
          @Override
          public boolean accept(File f) throws IOException {
            return f.isDir();
          }
        });
    fb.popup();
    ed.clear();
    if (fb.getPressedButtonIndex() == 0) {
      String ret = fb.getAnswer();
      ed.setText(ret);
    }
  }
}
