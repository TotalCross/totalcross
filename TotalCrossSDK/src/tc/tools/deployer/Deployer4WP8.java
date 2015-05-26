/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.tools.deployer;

import de.schlichtherle.truezip.file.*;
import java.io.*;
import java.io.File;
import org.apache.commons.io.*;
import tc.tools.deployer.zip.*;

import totalcross.io.*;
import totalcross.io.IOException;
import totalcross.sys.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class Deployer4WP8
{
   public Deployer4WP8() throws Exception
   {
      // locate template and target
      File templateFile = new File(Convert.appendPath(DeploySettings.etcDir, "../dist/vm/wp8/TotalCross.xap"));
      
      // create the output folder
      final String targetDir = Convert.appendPath(DeploySettings.targetDir, "/wp8/");
      File f = new File(targetDir);
      if (!f.exists())
         f.mkdirs();

      File tempFile = File.createTempFile(DeploySettings.appTitle+"temp", ".zip");
      tempFile.deleteOnExit();
      // create a copy of the original file
      FileUtils.copyFile(templateFile, tempFile);

      // open new file with truezip
      TFile templateZip = new TFile(tempFile);

      SilverlightZip sz = new SilverlightZip(new File(targetDir, DeploySettings.appTitle + ".xap"));
      String manifestContent =
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" +
                  "<Deployment>\r\n" +
                  "\t<App Name=\"" + DeploySettings.filePrefix + "\">\r\n" +
                  "\t</App>\r\n" +
                  "</Deployment>";
      sz.putEntry("TotalCrossManifest.xml", manifestContent.getBytes("UTF-8"));

      // tcz
      for (int i = 0; i < DeploySettings.tczs.length; i++)
         sz.putEntry(new File(DeploySettings.tczs[i]).getName(), new File(DeploySettings.tczs[i]));
      // TCBase & TCUI
      sz.putEntry("TCBase.tcz", new File(DeploySettings.folderTotalCross3DistVM, "TCBase.tcz"));
      sz.putEntry("TCUI.tcz",   new File(DeploySettings.folderTotalCross3DistVM, "TCUI.tcz"));
      // TCFont
      sz.putEntry(new File(DeploySettings.fontTCZ).getName(), new File(DeploySettings.folderTotalCross3DistVM, DeploySettings.fontTCZ));
      // Litebase
      sz.putEntry("LitebaseLib.tcz", new File(DeploySettings.folderTotalCross3DistVM, "LitebaseLib.tcz"));

      // add pkg files
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("wp8.pkg", ht);
      Vector v = new Vector(Utils.joinGlobalWithLocals(ht, null, true));
      Utils.preprocessPKG(v,true);
      String[] extras = (String[])v.toObjectArray();
      if (extras != null && extras.length > 0)
         for (int i = 0; i < extras.length; i++)
         {
            String pathname = extras[i];
            File ff = new File(pathname);
            if (!ff.exists())
            {
               ff = new File(totalcross.sys.Convert.appendPath(DeploySettings.currentDir, pathname));
               if (!ff.exists())
                  ff = new File(Utils.findPath(extras[i],true));
            }
            if (ff.exists())
               sz.putEntry(extras[i], ff);
         }

      // add icons
      sz.putEntry("Assets/ApplicationIcon.png", readIcon(99, 99));
      sz.putEntry("Assets/Tiles/FlipCycleTileLarge.png", readIcon(691, 336));
      sz.putEntry("Assets/Tiles/FlipCycleTileMedium.png", readIcon(336, 336));
      sz.putEntry("Assets/Tiles/FlipCycleTileSmall.png", readIcon(159, 159));
      sz.putEntry("Assets/Tiles/IconicTileMediumLarge.png", readIcon(134, 202));
      sz.putEntry("Assets/Tiles/IconicTileSmall.png", readIcon(71, 110));

      // copy files from the template and get the WMAppManifest
      TFile wmAppManifestFile = templateZip.listFiles(new CopyZipFilter(sz, null))[0]; // must return exactly ONE result, in case of failure, check the filter!
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wmAppManifestFile.output(baos);

      // overwrite properties on the manifest, like application title, version numbers, etc
      String wmAppManifest = new String(baos.toByteArray(), "UTF-8");
      wmAppManifest = wmAppManifest.replace("<Title>PhoneDirect3DXamlAppInterop</Title>", "<Title>" + DeploySettings.appTitle + "</Title>");
      wmAppManifest = wmAppManifest.replace("Title=\"PhoneDirect3DXamlAppInterop\"", "Title=\"" + DeploySettings.appTitle + "\"");
      wmAppManifest = wmAppManifest.replace("Version=\"1.0.0.0\"", "Version=\"" + (DeploySettings.appVersion != null ? DeploySettings.appVersion : "1.0") + "\"");
      if (DeploySettings.companyContact != null) wmAppManifest = wmAppManifest.replace("Author=\"PhoneDirect3DXamlAppInterop author\"", "Author=\"" + DeploySettings.companyContact + "\"");
      if (DeploySettings.companyInfo != null) wmAppManifest = wmAppManifest.replace("Publisher=\"PhoneDirect3DXamlAppInterop\"", "Publisher=\"" + DeploySettings.companyInfo + "\"");
      if (totalcross.sys.Settings.appDescription != null) wmAppManifest = wmAppManifest.replace("Description=\"Sample description\"","Description=\""+totalcross.sys.Settings.appDescription+"\"");
      // fixes problem that prevent more than one TC program in device
      String appmagic = Convert.bytesToHexString((DeploySettings.applicationId+"tc").getBytes());
      wmAppManifest = wmAppManifest.replace("f0dcbb57357d", appmagic.toLowerCase());
      if (!DeploySettings.quiet) // ProductID="{7db428c2-4514-466c-b1aa-f0dcbb57357d}"
      {
         int i0 = wmAppManifest.indexOf("ProductID=")+12;
         int i1 = wmAppManifest.indexOf("\"",i0+1)-1;
         Utils.println("Windows Phone folder id to be used with ISETool.exe: "+wmAppManifest.substring(i0,i1));
      }
      
      sz.putEntry("WMAppManifest.xml", wmAppManifest.getBytes("UTF-8"));

      // close template and final output files      
      TVFS.umount(templateZip);
      sz.close();
      String inst = "";
      if (DeploySettings.installPlatforms != null && DeploySettings.installPlatforms.toLowerCase().contains("wp8"))
         try
         {
            Utils.exec(new String[]{DeploySettings.etcDir+"tools\\xap\\XapDeployCmd.exe","/installlaunch",DeploySettings.appTitle + ".xap","/targetdevice:de"},targetDir);
            inst = " (Installed)";
         } catch (Exception e) {inst = " (Error: "+e.getMessage()+")";}
      System.out.println("... Files written to folder "+ targetDir + inst);
   }

   private byte[] readIcon(int width, int height) throws ImageException, IOException
   {
      Image icon = width == height ? IconStore.getSquareIcon(width) : IconStore.getIcon(width,height);
      ByteArrayStream bas = new ByteArrayStream(width * height);
      icon.createPng(bas);
      return bas.toByteArray();
   }

   /**
    * Filter that copies the contents of a source TFile to this SilverlightZip.<br>
    * WMAppManifest.xml is NOT copied, it is returned by listFiles instead, so the caller may overwrite its properties
    * before writing it to the target file.
    * 
    * @author Fabio Sobral
    * 
    */
   private class CopyZipFilter implements FilenameFilter
   {
      private ByteArrayOutputStream baos = new ByteArrayOutputStream();

      private SilverlightZip zip;
      private String baseDir;

      CopyZipFilter(SilverlightZip zip, String baseDir)
      {
         this.zip = zip;
         this.baseDir = baseDir;
      }

      public boolean accept(File dir, String name)
      {
         TFile f = new TFile(dir, name);

         if (f.isDirectory())
            f.listFiles(new CopyZipFilter(zip, baseDir == null ? f.getName() : Convert.appendPath(baseDir, f.getName())));
         else
         {
            if ("WMAppManifest.xml".equals(name))
               return true;

            try
            {
               baos.reset();
               f.output(baos);
               byte[] content = baos.toByteArray();
               zip.putEntry(baseDir == null ? f.getName() : Convert.appendPath(baseDir, f.getName()), content);
            }
            catch (java.io.IOException e)
            {
               e.printStackTrace();
            }
         }
         return false;
      }
   }
}
