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
package tc.tools.deployer;

import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import tc.tools.converter.J2TC;
import tc.tools.converter.java.JavaClass;
import totalcross.sys.Convert;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class Deployer4Applet {
  public Deployer4Applet() throws Exception {
    String targetDir = DeploySettings.targetDir + "applet/";
    try {
      new totalcross.io.File(targetDir).createDir();
    } catch (totalcross.io.IOException e) {
    }
    String names[] = { "_320x480x16_Android.html", "_240x320x16_WindowsMobile.html", "_960x640x16_iPhone.html", };

    for (int i = 0; i < names.length; i++) {
      writeFile(targetDir, names[i]);
    }

    writeJar(targetDir + DeploySettings.filePrefix + ".jar", targetDir);
    System.out.println("... Files written to folder " + targetDir);
  }

  private void writeJar(String name, String targetDir) throws Exception {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(name));
    addHashtable2zip(J2TC.htAddedClasses, zos);
    //addHashtable2zip(J2TC.htExcludedClasses, zos);
    zos.close();
    Utils.jarSigner(name, targetDir);
  }

  private void addZipEntry(ZipOutputStream zos, String name, byte[] bytes) throws Exception {
    ZipEntry entry = new ZipEntry(name);
    entry.setMethod(ZipEntry.DEFLATED);
    entry.setSize(bytes.length);
    CRC32 crc = new CRC32();
    crc.update(bytes);
    entry.setCrc(crc.getValue());
    zos.putNextEntry(entry);
    zos.write(bytes);
  }

  private void addHashtable2zip(Hashtable ht, ZipOutputStream zos) throws Exception {
    Vector keys = ht.getKeys();
    int n = keys.size();
    for (int i = 0; i < n; i++) {
      String name = (String) keys.items[i];
      Object o = ht.get(name);
      byte[] bytes = (o instanceof JavaClass) ? ((JavaClass) o).bytes : (byte[]) o;
      addZipEntry(zos, name, bytes);
    }
  }

  private void writeFile(String targetDir, String name) throws Exception {
    String scr = name.substring(1, name.lastIndexOf('_'));
    if (DeploySettings.mainClassName == null) {
      throw new IllegalArgumentException(
          "Applet requires a MainWindow class, but none was identified in the input files.");
    }
    String className = DeploySettings.mainClassName.replace('/', '.');
    int x1 = scr.indexOf('x');
    int x2 = scr.indexOf('x', x1 + 1);
    int w = Convert.toInt(scr.substring(0, x1));
    int h = Convert.toInt(scr.substring(x1 + 1, x2));
    String out = "<HTML>\n" + "<HEAD>\n" + "<TITLE>TotalCross Application</TITLE>\n" + "</HEAD>\n" + "<BODY>\n"
        + "<center>\n" + "<applet codebase=\".\" code=\"totalcross.Launcher\" archive=\"../../../../tcweb.jar,"
        + DeploySettings.filePrefix + ".jar" + (DeploySettings.isJarOrZip ? ",tc.jar" : "") + "\" width=" + w
        + " height=" + h + ">\n" + "<param name=arguments value=\"/scale 1 /scr " + scr + " " + className + "\">\n"
        + "</applet>\n" + "</center>\n" + "<p>\n" + "Device key emulations:<br>\n"
        + "F2 : TAKE SCREEN SHOT AND SAVE TO CURRENT FOLDER<br>\n" + "F6 : MENU<br>\n" + "F7 : BACK (ESCAPE)<br>\n"
        + "F9 : CHANGE ORIENTATION (don't work on applet)<br>\n" + "F11: OPEN KEYBOARD<br>\n"
        + "<br>Important! You may have to relax the Browser restrictions so that some samples can be run (by default, the Browser won't let the samples access the file system to load images or databases, or to connect to the internet).\n"
        + "<br>If the sample fails, open the Java console and check the error.\n" + "</BODY>\n" + "</HTML>";
    Utils.writeBytes(out.getBytes(), targetDir + name);
  }
}
