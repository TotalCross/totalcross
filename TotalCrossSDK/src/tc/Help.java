package tc;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class Help extends MainWindow
{
   public Help()
   {
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      Settings.uiAdjustmentsBasedOnFontHeight = true;
   }
   
   private static final int COLOR = 0x05B6EE;
   private boolean isEn=true;
   private String x(String en, String pt)
   {
      return isEn || pt == null ? en : pt; 
   }
   
   TabbedContainer tc;
   Label lstatus;
   Edit edclass,edkey,edpath;
   Radio rEn, rPt;
   RunContainer rc;
   DepContainer dc;
   RadioGroupController rgLang = new RadioGroupController();
   Button btpath;
   
   public void initUI()
   {
      Toast.backColor = Color.YELLOW;
      Toast.foreColor = 0;
      Toast.posY = BOTTOM - 300;
      reload(true);
   }
   
   private void reload(boolean load)
   {
      try
      {
         if (load)
            loadConfig();
         else
            saveConfig();
         removeAll();
         add(new ImageControl(new Image("logoh.png")),CENTER,TOP);
         add(new Label(x("Helper application to Run/Deploy", "Aplicação de ajuda para Executar / Empacotar"),CENTER,Color.BLUE,true),LEFT,AFTER+50,FILL,PREFERRED);
         
         add(new Label(x("Language: ","Linguagem: ")), LEFT,AFTER+25);
         add(rEn = new Radio("English", rgLang), AFTER+25, SAME);    tip(rEn,"Click here to set the user interface to English","Clique aqui para mudar a linguagem para inglês");  
         add(rPt = new Radio("Português", rgLang), AFTER+25, SAME);  tip(rPt,"Click here to set the user interface to Portuguese","Clique aqui para mudar a linguagem para português");
         add(new Label(x("Class name: ","Nome da classe: ")),LEFT,AFTER+25);
         add(edclass = new Edit(),AFTER,SAME); tip(edclass, "Type the full class name of the class that extends MainWindow. Don't forget to include the package.", "Digite o nome (com o pacote) da classe que estende MainWindow");
         Label l;
         add(l = new Label(x("Class folder: ","Pasta dos .class: ")),LEFT,AFTER+25); 
         add(btpath = new Button(x(" Select ", " Selecionar ")), RIGHT,SAME); tip(btpath, "Press this button to select the folder where the .class is located", "Pressione esse botão para selecionar a pasta onde os arquivos .class estão localizados");
         add(edpath = new Edit(), AFTER,SAME,FIT-25,PREFERRED,l); tip(edpath, "Type the .class folder or press the Select button", "Digite a pasta dos .class ou clique no botão Selecionar");
         
         add(new Label(x("Key: ","Chave: ")),LEFT,AFTER+25);
         add(edkey = new Edit(),AFTER,SAME);  tip(edkey, "Type the 24-characters registration key that you received by email", "Digite a chave com 24 caracteres que você recebeu por email");
         
         lstatus = new Label("",CENTER);
         lstatus.setBackForeColors(COLOR,0);
         lstatus.setFont(font.asBold());
         lstatus.autoSplit = true;
         add(lstatus,LEFT,BOTTOM,FILL,fmH*2);
         
         tc = new TabbedContainer(new String[]{x("Run","Executar"),x("Deploy","Empacotar")});
         tc.setBackColor(0xAAAAAA);
         tc.activeTabBackColor = COLOR;
         tc.allSameWidth = true;
         add(tc, LEFT,AFTER+25,FILL,FIT,edkey);
         tc.setContainer(0,rc = new RunContainer());
         tc.setContainer(1,dc = new DepContainer());
         
         if (load)
            tc.setActiveTab(tab);
         configs[sel].toUI();
         rgLang.setSelectedIndex(isEn ? 0 : 1,false);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         exit(1);
      }
   }
   
   Config[] configs;
   int tab,sel;
   
   private void loadConfig()
   {
      File f = null;
      try
      {
         f = new File(Settings.appPath+"/help.dat", File.READ_WRITE);
         DataStream ds = new DataStream(f);
         /*int ver = */ds.readByte();
         int lang = ds.readByte();
         tab = ds.readByte();
         int n = ds.readByte();
         sel = ds.readByte();
         configs = new Config[n];
         for (int i = 0; i < n; i++)
            configs[i] = new Config(ds);
         isEn = lang == 0;
      }
      catch (FileNotFoundException | EOFException fnfe)
      {
         configs = new Config[]{new Config()};
      }
      catch (Exception ee)
      {
         ee.printStackTrace();
      }
      if (f != null) try {f.close();} catch (Exception e) {}
   }
   
   private void saveConfig()
   {
      File f = null;
      try
      {
         f = new File(Settings.appPath+"/help.dat", File.CREATE_EMPTY);
         DataStream ds = new DataStream(f);
         ds.writeByte(1);
         ds.writeByte(isEn ? 0 : 1);
         ds.writeByte(tc.getActiveTab());
         int n = 1;
         ds.writeByte(n); // number of configs
         ds.writeByte(0); // active config
         for (int i = 0; i < n; i++)
            configs[i].toFile(ds);
         f.close();
      }
      catch (FileNotFoundException fnfe)
      {
      }
      catch (Exception ee)
      {
         ee.printStackTrace();
      }
      if (f != null) try {f.close();} catch (Exception e) {}
   }
   
   private void tip(Control c, String en, String pt)
   {
      final String s = x(en,pt);
      c.addMouseListener(new MouseListener()
      {
         public void mouseWheel(MouseEvent e) {}
         public void mouseMove(MouseEvent e)  {}         
         public void mouseOut(MouseEvent e)   {lstatus.setText("");}
         public void mouseIn(MouseEvent e)    {lstatus.setText(s);}
      });
   }

   public void onExit()
   {
      saveConfig();
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btpath)
            {
               if (edclass.getTrimmedLength() == 0)
                  Toast.show(x("Please type the class name before selecting the path", "Digite o nome da classe antes de selecionar a pasta"), 3000);
               else
                  selectPath(edpath, x("Select the .class' path", "Selecione a pasta dos arquivos .class"));
            }
            else
            if (e.target == rEn || e.target == rPt)
            {
               isEn = e.target == rEn;
               reload(false);
            }
            break;
      }
   }
   
   class Config
   {
      // run
      String path,className, key, xpos, ypos, width, height, fontsize;
      int rdsel=-1;
      boolean showMouse, fast;
      String scale, bpp, cmdline;
      // deploy
      boolean wmo, w32, lin, apl, ios, and, wp8, all=true, inst, pack;
      String pathd;

      public Config()
      {
      }
      
      public Config(DataStream ds) throws IOException
      {
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
         wp8 = ds.readBoolean();
         all = ds.readBoolean();
         inst = ds.readBoolean();
         pack = ds.readBoolean();
         pathd = ds.readString();
      }

      public void toFile(DataStream ds) throws IOException
      {
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
         ds.writeBoolean(wp8);
         ds.writeBoolean(all);
         ds.writeBoolean(inst);
         ds.writeBoolean(pack);
         ds.writeString(pathd);
      }
      
      public void toUI()
      {
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
         if (rdsel == -1)
            rc.rg.setSelectedIndex(3,true);
         // deploy
         dc.w32.setChecked(w32);
         dc.wmo.setChecked(wmo);
         dc.lin.setChecked(lin);
         dc.apl.setChecked(apl);
         dc.ios.setChecked(ios);
         dc.and.setChecked(and);
         dc.wp8.setChecked(wp8);
         dc.all.setChecked(all);
         dc.inst.setChecked(inst);
         dc.pack.setChecked(pack);
         dc.edpathd.setText(pathd);
      }
      
      public void fromUI()
      {
         // run
         path = edpath.getText();
         className = edclass.getText();
         key = edkey.getText();
         xpos = rc.edX.getText();
         ypos = rc.edY.getText();
         rdsel = rc.rg.getSelectedIndex();
         width = rc.edW.getText();
         height = rc.edH.getText();
         bpp = (String)rc.cbBpp.getSelectedItem();
         fontsize = rc.edFS.getText();
         scale = (String)rc.cbSc.getSelectedItem();
         fast = rc.swSc.isOn();
         cmdline = rc.edCmd.getText();
         showMouse = rc.chM.isChecked();
         // deploy
         w32   = dc.w32.isChecked();
         wmo   = dc.wmo.isChecked();
         lin   = dc.lin.isChecked();
         apl   = dc.apl.isChecked();
         ios   = dc.ios.isChecked();
         and   = dc.and.isChecked();
         wp8   = dc.wp8.isChecked();
         all   = dc.all.isChecked();
         inst  = dc.inst.isChecked();
         pack  = dc.pack.isChecked();
         pathd = dc.edpathd.getText();
      }
      
      private StringBuilder initRD(String path)
      {
         StringBuilder sb = new StringBuilder(256);
         String javapath = System.getenv("JAVA_HOME");
         if (javapath != null)
         {
            sb.append(javapath);
            if (!javapath.contains("bin"))
               sb.append("\\bin\\");
            else
               sb.append("\\");
         }
         sb.append("java.exe");
         String tcjar = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
         if (tcjar.startsWith("/"))
            tcjar = tcjar.substring(1);
         sb.append(" -classpath "+path+java.io.File.pathSeparator+tcjar);
         return sb;
      }
      
      private String getKey()
      {
         return key.startsWith("%") ? System.getenv(key.substring(1,key.length()-1)) : key;
      }
      
      private void deploy()
      {
         try
         {
            fromUI();
            // to run: classname=tc.samples.api.TotalCrossAPI, class folder=G:\TotalCross\TotalCrossSDK
            // to deploy: classname = TotalCrossAPI, class folder=G:\TotalCross\TotalCrossSDK\tc\samples\api
            int idx = className.lastIndexOf('.');
            String cn = idx == -1 ? className : className.substring(idx+1);
            String pn = idx == -1 ? path : Convert.appendPath(path, className.substring(0,idx).replace('.','/'));
            
            StringBuilder sb = initRD(pn);
            sb.append(" tc.Deploy ");
            sb.append(" ").append(cn).append(".class");
            sb.append(" /r "+getKey());
            if (all)
               sb.append(" -all");
            else
            {
               if (lin) sb.append(" -linux");
               if (apl) sb.append(" -applet");
               if (ios) sb.append(" -ios"); 
               if (and) sb.append(" -android");
               if (wp8) sb.append(" -wp8");
               if (w32) sb.append(" -win32");
               if (wmo) sb.append(" -winmo");
            }
            if ((ios || all) && !pathd.isEmpty()) sb.append(" /m ").append(pathd);
            if ((and || all) && inst) sb.append(" /i android");
            if ((wp8 || all) && inst) sb.append(" /i wp8");
            if (pack) sb.append(" /p");
            exec(sb, pn, true);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
      }
      
      private void run()
      {
         try
         {
            fromUI();
            StringBuilder sb = initRD(path);
            sb.append(" totalcross.Launcher ");
            sb.append(" /r "+getKey());
            switch (rdsel)
            {
               case 1: sb.append(" /scr win32"); break;
               case 2: sb.append(" /scr wince"); break;
               case 3: sb.append(" /scr android"); break;
               case 4: sb.append(" /scr iphone"); break;
               case 0:
                  if (!width.isEmpty() && !height.isEmpty() && !bpp.isEmpty())
                     sb.append(" /scr "+width+"x"+height+"x"+bpp);
                  if (!fontsize.isEmpty())
                     sb.append(" /fontsize ").append(fontsize);
                  break;
            }
            if (!xpos.isEmpty() && !ypos.isEmpty())
               sb.append(" /pos "+xpos+","+ypos);
            if (!scale.equals("1"))
               sb.append(fast ? " /fastscale "+scale : " /scale "+scale);
            if (showMouse)
               sb.append(" /showmousepos");
            if (!cmdline.isEmpty())
               sb.append(" /cmd "+cmdline);
            sb.append(" "+className.replace('/','.'));
            
            exec(sb, path, false);
         }
         catch (Exception ee)
         {
            MessageBox.showException(ee,true);
         }
      }
   }
   
   public static void exec(StringBuilder sb, String path, boolean big) throws Exception
   {
      String cmd = sb.toString();
      System.out.println("Line: "+cmd);
      System.out.println("Path: "+path);
      Process process = Runtime.getRuntime().exec(cmd, null, new java.io.File(path));
      java.io.InputStream inputStream = process.getInputStream();
      java.io.InputStream errorStream = process.getErrorStream();
      StringBuffer message = new StringBuffer(1024);
      String lineIn;
      
      for (int i =0, n = big ? 30 : 5; i < n; i++) // 15 seconds must be enough...
      {
         if (inputStream.available() > 0)
            while (inputStream.available() > 0 && (lineIn = readStream(inputStream)) != null)
               message.append("INPUT:").append(lineIn).append("\n");
         if (errorStream.available() > 0)
            while (errorStream.available() > 0 && (lineIn = readStream(errorStream)) != null)
               message.append("ERROR: ").append(lineIn).append("\n");
         try
         {
            process.exitValue();
            break;
         }
         catch (Throwable throwable)
         {
            Thread.sleep(500);
         }
      }
      System.out.println(message.toString());
   }
   public static byte bytebuf[] = new byte[4096];
   public static String readStream(java.io.InputStream is) throws Exception
   {
      int avail = is.available();
      byte[] buf = bytebuf.length >= avail ? bytebuf : new byte[avail];
      is.read(buf,0,avail);
      return new String(buf,0,avail).trim();
   }

   private boolean checkFilled()
   {
      String error = "";
      if (edpath.getTrimmedLength() == 0)
         error += ", path to .class";
      if (edclass.getTrimmedLength() == 0)
         error += ", class name";
      if (edkey.getTrimmedLength() == 0)
         error += ", key";
      if (error.startsWith(", "))
         error = error.substring(2);
      if (!error.isEmpty())
         Toast.show(x("The following fields must be filled: "+error, "Os seguintes campos devem ser preenchidos: "+error), 4000);
      return error.isEmpty();
   }
   
   class RunContainer extends ScrollContainer
   {
      Edit edX, edY, edW, edH, edFS, edCmd;
      ComboBox cbSc, cbBpp;
      Switch swSc;
      Radio rdC, rdA, rdI, rd32, rdCE;
      Check chM;
      Button btRun;
      RadioGroupController rg = new RadioGroupController();
      
      public RunContainer()
      {
         super(false,true);
      }
      public void initUI()
      {
         add(new Label(x("Screen settings","Configurações da janela")),LEFT,TOP);
         add(new Label(x("Position - X: ","Posição - X: ")),LEFT,AFTER+25);
         add(edX = new Edit("99999"),AFTER,SAME); tip(edX, "Type the X position for the window. Leave blank to let the system set it", "Digite a posição X para a janela. Deixe em branco pro sistema posicionar");
         add(new Label("Y: "),AFTER+50,SAME);
         add(edY = new Edit("99999"),AFTER,SAME); tip(edY, "Type the Y position for the window. Leave blank to let the system set it", "Digite a posição Y para a janela. Deixe em branco pro sistema posicionar");
         
         add(new Label(x("Select one to populate the edits below", "Selecione um para popular os edits abaixo")),LEFT,AFTER+25);
         add(rdC = new Radio(x("Custom","Customizado"),rg),LEFT,AFTER);  rdC.appId = 0; tip(rdC,  "Fill the fields below", "Preencha os campos abaixo");
         add(rd32 = new Radio("Win32",rg),AFTER+50,SAME);  rd32.appId = 1; tip(rd32, "Width=240, height=320, bpp=24", "Largura=240, altura=320, bpp=24");
         add(rdCE = new Radio("WinCE",rg),AFTER+50,SAME);  rdCE.appId = 2; tip(rdCE, "Width=240, height=320, bpp=8", "Largura=240, altura=320, bpp=8");
         add(rdA = new Radio("Android",rg),AFTER+50,SAME); rdA .appId = 3; tip(rdA,  "Width=320, height=480, bpp=24", "Largura=320, altura=480, bpp=24");
         add(rdI = new Radio("iOS",rg),AFTER+50,SAME);     rdI .appId = 4; tip(rdI,  "Width=640, height=960, bpp=24, scale=0.75", "Largura=640, altura=960, bpp=24, escala=0.75");
         add(new Label(x("Width: ","Largura: ")),LEFT,AFTER+25);
         add(edW = new Edit("99999"),AFTER,SAME);   tip(edW, "Type the width for the window", "Digite a largura da janela");
         add(new Label(x("Height: ","Altura: ")),AFTER+50,SAME); 
         add(edH = new Edit("99999"),AFTER,SAME);   tip(edH, "Type the height for the window", "Digite a altura da janela");
         add(new Label(x("BitsPerPixel: ","BitsPorPixel: ")),LEFT,AFTER+25);
         add(cbBpp = new ComboBox(new String[]{"8","16","24","32"}),AFTER,SAME); tip(cbBpp, "Select the bits per pixel to be used in the Window. All platforms uses 24bpp, except WinCE, which uses 8bpp", "Selecione os bits por pixel usado na janela. Todas as plataformas, menos WinCE, usam 24bpp");
         add(new Label(x("Font size: ","Tam da letra: ")),LEFT,AFTER+25);
         add(edFS = new Edit("99999"),AFTER,SAME);  tip(edFS, "Type the font size. Leave it blank to use the default one", "Digite o tamanho da fonte. Deixe vazio pra usar o tamanho padrão");
         add(new Label(x("Scale: ","Escala: ")),LEFT,AFTER+25);
         add(cbSc = new ComboBox(new String[]{"0.25","0.5","0.75","1","2","4","6","8"}),AFTER,SAME); tip(cbSc, "Select the scale to apply in the Window. Useful when you test screen sizes above your monitor's resolution", "Selecione a escala pra aplicar na janela. Útil quando usar tamanho de janela maior que a resolução do seu monitor");
         cbSc.setSelectedItem("1");
         
         swSc = new Switch(true); tip(swSc, "Scale slow has a good appearance, while fast scale has a worst appearance", "Escala lenta tem boa aparência, e escala devagar tem uma pior aparência");
         swSc.textBackOn = x(" fast","rápida ");
         swSc.textBackOff = x(" slow","lenta ");
         add(swSc, AFTER+50,SAME);
         
         add(new Label(x("Miscelaneous","Miscelânia")),LEFT,AFTER+50);
         add(new Label(x("Command line to pass to application: ","Linha de comandos para passar à aplicação")),LEFT,AFTER+25);
         add(edCmd = new Edit(),LEFT,AFTER);   tip(edCmd, "You can pass extra arguments to application and retrieve it using MainWindow.getCommandLine()", "Você pode passar argumentos extras pra aplicação e recuperá-los usando MainWindow.getCommandLine()");
         add(chM = new Check(x("Show mouse position","Mostrar posição do mouse")),LEFT,AFTER+25); tip(chM, "Show the mouse position on window's title area", "Mostra a posição do mouse no título da janela");
         btRun = new Button(x("Run the application","Executar a aplicação"));
         btRun.setBackColor(COLOR);
         add(btRun, CENTER,AFTER+100,PARENTSIZE+80,PREFERRED+50);
         tip(btRun, "Fill the fields above and press this button to run the application", "Preencha os campos acima e clique nesse botão para executar a aplicação");
      }
      
      public void onEvent(Event e)
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btRun && checkFilled())
                  configs[sel].run();
               else
               if (e.target instanceof Radio)
               {
                  int w,h,bpp=24;
                  String s="1";
                  switch (((Control)e.target).appId)
                  {
                     case 2: // wince
                        bpp = 8;
                     case 1: // win32
                        w = 240; h = 320; break;
                     case 3: // android
                        w = 320; h = 480; break;
                     case 4: // ios
                        w = 640; h = 960; s="0.75"; break;
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

   class DepContainer extends ScrollContainer
   {
      Check wmo, w32, lin, apl, ios, and, wp8, all, inst, pack;
      Edit edpathd;
      Button btpath, btDep;
      
      public DepContainer()
      {
         super(false,true);
      }
      
      public void initUI()
      {
         add(new Label(x("Platforms","Plataformas")),LEFT,TOP);
         add(all = new Check(x("All platforms listed below", "Todas as plataformas listadas abaixo")),LEFT,AFTER);     all.appId = 1; tip(all, "Deploys for all platforms", "Faz o deploy para todas as plataformas");
         add(new Label("Desktop"),LEFT,AFTER);
         add(w32 = new Check("Win32"),AFTER+50,SAME);        w32.appId = 2; tip(w32, "Deploys for Windows 32 desktop", "Faz o deploy para Windows 32 desktop");
         add(lin = new Check("Linux"),AFTER+50,SAME);        lin.appId = 2; tip(lin, "Deploys for linux desktop", "Faz o deploy para Linux desktop");
         add(apl = new Check("Applet"),AFTER+50,SAME);       apl.appId = 2; tip(apl, "Deploys for applet to run in a browser", "Faz o deploy para executar no navegador");
         add(new Label(x("Mobile", "Móvel")),LEFT,AFTER);
         add(ios = new Check("iOS"),AFTER+50,SAME);          ios.appId = 2; tip(ios, "Deploys for iOS", "Faz o deploy para iOS");
         add(and = new Check("Android"),AFTER+50,SAME);      and.appId = 2; tip(and, "Deploys for Android 2.3 or greater", "Faz o deploy para Android 2.3 ou superior");
         add(wp8 = new Check("Win Phone 8"),AFTER+50,SAME);  wp8.appId = 2; tip(wp8, "Deploys for Windows Phone 8", "Faz o deploy para Windows Phone 8");
         add(wmo = new Check("Win Mobile"),AFTER+50,SAME);   wmo.appId = 2; tip(wmo, "Deploys for Windows Mobile 5, 6 and 7", "Faz o deploy para Windows Mobile 5, 6 e 7");

         Label l;
         add(l = new Label(x("Mobile provision folder: ","Pasta do mobile provision: ")),LEFT,AFTER+50);
         add(btpath = new Button(x(" Select ", " Selecionar ")), RIGHT,SAME); tip(btpath, "Selects the folder where the iOS mobile provision is (required for iOS)", "Seleciona a pasta onde estão os arquivos do mobile provision (requerido para o iOS)");
         add(edpathd = new Edit(), AFTER,SAME,FIT-25,PREFERRED,l); 

         add(inst = new Check(x("Install on device (android or wp8)", "Instalar no equipamento (android ou wp8)")), LEFT,AFTER+50); tip(inst, "Install the package if device is connected. You must only select android or wp8","Instala o pacote se o equipamento estiver conectado. Você deverá selecionar somente android ou wp8");

         add(pack = new Check(x("Package the vm with application", "Empacota a VM com a aplicação")),LEFT, AFTER+50);  tip(pack, "Inserts the VM inside the application's package (android and Windows Mobile); always true for other platforms", "Embute a VM no pacote da aplicação (android e Windows Mobile); sempre embute para as outras plataformas");

         btDep = new Button(x("Deploy the application", "Empacotar a aplicação"));
         btDep.setBackColor(COLOR);
         add(btDep, CENTER,AFTER+100,PARENTSIZE+80,PREFERRED+50);
         tip(btDep, "Fill the fields above and press this button to deploy the application", "Preencha os campos acima e clique nesse botão para empacotar a aplicação");
      }
      
      public void onEvent(Event e)
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == all && all.isChecked()) {w32.clear(); lin.clear(); apl.clear(); ios.clear(); and.clear(); wp8.clear(); wmo.clear(); }
               else
               if (e.target instanceof Check && ((Check)e.target).appId == 2)
               {
                  if (((Check)e.target).isChecked())
                     all.clear();
                  if (e.target == ios && edpathd.getLength() == 0)
                     Toast.show(x("You must set the Mobile Provision path", "Você deve especificar a pasta do Mobile Provision"), 3000);
               }
               else
               if (e.target == btpath)
                  selectPath(edpathd, x("Select the Mobile Provision's path", "Selecione a pasta do Mobile Provision"));
               else
               if (e.target == btDep && checkFilled())
                  configs[sel].deploy();
               break;
         }
      }
   }
   
   private void selectPath(Edit ed, String tit)
   {
      FileChooserBox fb = new FileChooserBox(tit, new String[]{x("Select","Selecionar"), x("Cancel","Cancelar")},new FileChooserBox.Filter()
      {
         public boolean accept(File f) throws IOException
         {
            return f.isDir();
         }
      });
      fb.popup();
      ed.clear();
      if (fb.getPressedButtonIndex() == 0)
      {
         String ret = fb.getAnswer();
         ed.setText(ret);
      }   
   }
}
