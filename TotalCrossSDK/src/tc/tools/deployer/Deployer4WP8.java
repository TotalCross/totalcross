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

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.commons.io.FileUtils;
import tc.tools.deployer.zip.SilverlightZip;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;

public class Deployer4WP8
{
   public Deployer4WP8() throws Exception
   {
      // locate template and target
      File templateFile = new File(Convert.appendPath(DeploySettings.folderTotalCross3DistVM, "wp8/TotalCross.xap"));
      
      // create the output folder
      final String targetDir = Convert.appendPath(DeploySettings.targetDir, "/wp8/");
      File f = new File(targetDir);
      if (!f.exists())
         f.mkdirs();

      File tempFile = File.createTempFile(DeploySettings.appTitle, ".zip");
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
      sz.putEntry(new File(DeploySettings.tczFileName).getName(), new File(DeploySettings.tczFileName));
      // TCBase
      sz.putEntry("TCBase.tcz", new File(DeploySettings.distDir, "vm/TCBase.tcz"));
      // TCFont
      sz.putEntry(new File(DeploySettings.fontTCZ).getName(), new File(DeploySettings.distDir, "vm/" + DeploySettings.fontTCZ));
      // Litebase
      sz.putEntry("LitebaseLib.tcz", new File(DeploySettings.distDir, "vm/LitebaseLib.tcz"));

      // add pkg files
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("wp8.pkg", ht);
      String[] extras = Utils.joinGlobalWithLocals(ht, null, true);
      if (extras.length > 0)
      {
         for (int i = 0; i < extras.length; i++)
            sz.putEntry(extras[i], new File(Utils.getFileName(extras[i])));
      }

      // add icons
      sz.putEntry("Assets/ApplicationIcon.png", readIcon(100, 100));
      sz.putEntry("Assets/Tiles/FlipCycleTileLarge.png", readIcon(691, 336));
      sz.putEntry("Assets/Tiles/FlipCycleTileMedium.png", readIcon(336, 336));
      sz.putEntry("Assets/Tiles/FlipCycleTileSmall.png", readIcon(159, 159));
      sz.putEntry("Assets/Tiles/IconicTileMediumLarge.png", readIcon(134, 202));
      sz.putEntry("Assets/Tiles/IconicTileSmall.png", readIcon(71, 110));

      // copy files from the template and get the WMAppManifest
      TFile wmAppManifestFile = templateZip.listFiles(sz.getCopyZipFilter(null))[0]; // must return exactly ONE result, in case of failure, check the filter!
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wmAppManifestFile.output(baos);

      // overwrite properties on the manifest, like application title, version numbers, etc
      String wmAppManifest = new String(baos.toByteArray(), "UTF-8");
      wmAppManifest = wmAppManifest.replace(
            "<Title>PhoneDirect3DXamlAppInterop</Title>", "<Title>" + DeploySettings.appTitle + "</Title>");
      wmAppManifest = wmAppManifest.replace(
            "Title=\"PhoneDirect3DXamlAppInterop\"", "Title=\"" + DeploySettings.appTitle + "\"");
      wmAppManifest = wmAppManifest.replace(
            "Version=\"1.0.0.0\"", "Version=\"" + DeploySettings.appVersion + "\"");
      wmAppManifest = wmAppManifest.replace(
            "Author=\"PhoneDirect3DXamlAppInterop author\"", "Author=\"" + DeploySettings.companyInfo + "\"");
      wmAppManifest = wmAppManifest.replace(
            "Publisher=\"PhoneDirect3DXamlAppInterop\"", "Publisher=\"" + DeploySettings.companyInfo + "\"");
      sz.putEntry("WMAppManifest.xml", wmAppManifest.getBytes("UTF-8"));

      // close template and final output files      
      TVFS.umount(templateZip);
      sz.close();
      
      System.out.println("... Files written to folder "+ Convert.appendPath(DeploySettings.targetDir, "/wp8/"));
   }

   private byte[] readIcon(int width, int height) throws ImageException, IOException
   {
      Image icon = width == height ? IconStore.getSquareIcon(width) : IconStore.getSquareIcon(width);
      ByteArrayStream bas = new ByteArrayStream(width * height);
      icon.createPng(bas);
      return bas.toByteArray();
   }
}
