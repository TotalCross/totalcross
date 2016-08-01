package tc;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

import java.util.*;

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
   Edit edclass,edkey;
   Radio rEn, rPt;
   RunContainer rc;
   DepContainer dc;
   RadioGroupController rgLang = new RadioGroupController();
   
   public void initUI()
   {
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
         add(new Label(x("Key: ","Chave: ")),LEFT,AFTER+25);
         add(edkey = new Edit(),AFTER,SAME);  tip(edkey, "Type the 24-characters registration key that you received by email", "Digite a chave com 24 caracteres que você recebeu por email");
         
         lstatus = new Label("",CENTER);
         lstatus.setBackForeColors(COLOR,0);
         lstatus.setFont(font.asBold());
         lstatus.autoSplit = true;
         add(lstatus,LEFT,BOTTOM,FILL,fmH*2);
         
         tc = new TabbedContainer(new String[]{x("Run","Executar"),x("Deploy","Empacotar")});
         tc.setBackColor(COLOR);
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
   
   RunConfig[] configs;
   int tab,sel;
   
   private void loadConfig()
   {
      try
      {
         File f = new File(Settings.appPath+"/help.dat", File.READ_WRITE);
         DataStream ds = new DataStream(f);
         /*int ver = */ds.readByte();
         int lang = ds.readByte();
         tab = ds.readByte();
         int n = ds.readByte();
         sel = ds.readByte();
         configs = new RunConfig[n];
         for (int i = 0; i < n; i++)
            configs[i] = new RunConfig(ds);
         f.close();
         isEn = lang == 0;
      }
      catch (FileNotFoundException | EOFException fnfe)
      {
         configs = new RunConfig[]{new RunConfig()};
      }
      catch (Exception ee)
      {
         ee.printStackTrace();
      }
   }
   
   private void saveConfig()
   {
      try
      {
         File f = new File(Settings.appPath+"/help.dat", File.CREATE_EMPTY);
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
            if (e.target == rEn || e.target == rPt)
            {
               isEn = e.target == rEn;
               reload(false);
            }
            break;
      }
   }
   
   class RunConfig
   {
      String className, key, xpos, ypos, width, height, fontsize;
      int rdsel;
      boolean showMouse, fast;
      String scale, bpp, cmdline;
      
      public RunConfig()
      {
      }
      
      public RunConfig(DataStream ds) throws IOException
      {
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
      }

      public void toFile(DataStream ds) throws IOException
      {
         fromUI();
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
      }
      
      public void toUI()
      {
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
      }
      
      public void fromUI()
      {
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
      }
   }
   
   class RunContainer extends ScrollContainer
   {
      Edit edX, edY, edW, edH, edFS, edCmd;
      ComboBox cbSc, cbBpp;
      Switch swSc;
      Radio rdA, rdI, rd32, rdCE;
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
         add(rd32 = new Radio("Win32",rg),LEFT,AFTER);     rd32.appId = 1; tip(rd32, "Width=240, height=320, bpp=24", "Largura=240, altura=320, bpp=24");
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
         btRun = new Button("Run the application");
         btRun.setBackColor(COLOR);
         add(btRun, CENTER,AFTER+100,PARENTSIZE+80,PREFERRED+50);
      }
      
      public void onEvent(Event e)
      {
         switch (e.type)
         {
            case ControlEvent.PRESSED:
               if (e.target == btRun)
                  run();
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
   
   private void run()
   {
      ArrayList<String> v = new ArrayList<String>(10);
      
   }
   
   class DepContainer extends ScrollContainer
   {
      public DepContainer()
      {
         super(false,true);
      }
      public void initUI()
      {
      }
   }
}
