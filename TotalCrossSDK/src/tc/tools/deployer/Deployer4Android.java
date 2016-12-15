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

import totalcross.util.*;

import java.io.*;
import java.lang.String;
import java.util.zip.*;
import tc.tools.converter.bb.*;
import tc.tools.converter.bb.attribute.*;
import tc.tools.converter.bb.constant.*;
import tc.tools.converter.bb.constant.Class;
import tc.tools.converter.bb.constant.Integer;

/*
    A launcher for Android is placed in a Android PacKage (APK), which is a 
    zip file, with the following contents:
    
       + META-INF
       |    MANIFEST.MF
       |    CERT.RSA
       |    CERT.SF
       + res
       |    drawable-hdpi
       |       icon.png
       | assets 
       |    tcfiles.zip
       | resources.arsc
       | classes.dex
       | AndroidManifest.xml
    
    The res + resources.arsc + AndroidManifest.xml are stored inside a file named resources.ap_
    (created with APPT) 
    
    Then it adds the classes.dex and signs the package, creating the APK file.
    
    The AndroidManifest.xml is a compiled version of the original AndroidManifest.xml file. 
    Its format is (all numbers in little endian):
       pos 4: file size
       pos 12: pos to € - 8
       pos 36: list of positions to the strings, starting from 0 
          each string is prefixed with its size and ends with 0 (chars in unicode)
     
     
    The resources.arsc has the package name located at pos 156, prefixed with 
    the total length of the name (127 bytes) - which is NOT the length of the package.
    There is a free space to place the package there, so there's no need to shift the file.
     
     
    Since Android requires that data stored in res/raw must be all lower-case, we have to create
    a zip and store the TCZ (and also files from android.pkg) inside that zip. When the program
    first runs, it unpacks that zip in the /data/data/class_package folder. Note that there's no
    way to delete the zip inside tha APK after the installation.
    
    
    Also: all TotalCross programs must be signed with the same key, otherwise the vm and litebase 
    will not be able to read data from the program's folder.
    
    
    To create the launcher for the application, we follow these steps:
    
    1. Edit the files: R.class  Stub.class  R$attr.class  R$drawable.class  R$raw.class  and replace
    the package and Stub names with totalcross.app.MainClass (where MainClass is the class that extends
    MainWindow)
    
    2. Run the dx (dalvik converter) to create a classes.dex file
    
    3. Zip the contents into tcfiles.zip
    
    4. Update the resources as explained above.
    
    5. Create the APK (jar file) with everything
    
    6. Sign the APK.
 */

public class Deployer4Android
{
   private static final boolean DEBUG = false;
   private static String targetDir, sourcePackage, targetPackage, targetTCZ, jarOut, fileName;
   private String tcFolder;
   private boolean singleApk;

   byte[] buf = new byte[8192];
   
   public Deployer4Android() throws Exception
   {
      targetDir = DeploySettings.targetDir+"android/";
      fileName = DeploySettings.filePrefix;
      if (fileName.indexOf(' ') >= 0) // apk with spaces give errors, so we remove the spaces
         fileName = fileName.replace(" ","");
      // create the output folder
      File f = new File(targetDir);
      if (!f.exists())
         f.mkdirs();
      singleApk = DeploySettings.packageVM;
      if (!singleApk)
      {
         targetPackage = "totalcross/app/"+fileName.toLowerCase();
         if (!DeploySettings.quiet)
            System.out.println("Android application folder: /data/data/"+(targetPackage.replace('/','.')));
      }
      else
      {
         tcFolder = DeploySettings.folderTotalCross3DistVM+"android/";
         // source and target packages must have the exact length
         sourcePackage = "totalcross/android";
         targetTCZ = "app"+DeploySettings.applicationId.toLowerCase();
         targetPackage = "totalcross/"+targetTCZ;
         System.out.println("Android application folder: /data/data/"+(targetPackage.replace('/','.')));
      }
      
      if (!singleApk)
      {
         createLauncher();  // 1
         jar2dex();         // 2
      }
      updateResources(); // 3+4+5
      Utils.jarSigner(fileName+".apk", targetDir);         // 6
      new ZipAlign().zipAlign(new File(targetDir+"/"+fileName+".apk"),new File(targetDir+"/"+fileName+"_.apk"));
      String apk = targetDir+"/"+fileName+".apk";
      Utils.copyFile(targetDir+"/"+fileName+"_.apk",apk,true); 
      
      String extraMsg = "";
      if (DeploySettings.installPlatforms.indexOf("android,") >= 0)
         extraMsg = callADB(apk);
      
      System.out.println("... Files written to folder "+targetDir+extraMsg);
      
   }

   private String callADB(String apk) throws Exception
   {
      String adb = Utils.findPath(DeploySettings.etcDir+"tools/android/adb.exe",false);
      if (adb == null)
         throw new DeployerException("File android/adb.exe not found!");
      String message = Utils.exec(new String[]{adb,"install","-r",apk},targetDir);
      if (message != null && message.indexOf("INPUT:Success") >= 0)
         return " (installed)";
      System.out.println(message);
      return " (error on installl)";
   }
   
   private void createLauncher() throws Exception
   {
      String jarIn = Utils.findPath(DeploySettings.etcDir+"launchers/android/Launcher.jar",false);
      if (jarIn == null)
         throw new DeployerException("File android/Launcher.jar not found!");
      jarOut = targetDir+fileName+".jar";
      
      ZipInputStream zis = new ZipInputStream(new FileInputStream(jarIn));
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarOut));
      ZipEntry ze;

      while ((ze = zis.getNextEntry()) != null)
      {
         String name = convertName(ze.getName());
         if (DEBUG) System.out.println("=== Entry: "+name);
         
         zos.putNextEntry(new ZipEntry(name));
         if (name.endsWith(".class"))
            convertConstantPool(zis,zos);
         zos.closeEntry();
      }

      zis.close();
      zos.close();
   }
   
   private void jar2dex() throws Exception
   {
      // java -classpath P:\TotalCross3\etc\tools\android\dx.jar com.android.dx.command.Main --dex --output=classes.dex UIGadgets.jar
      String dxjar = Utils.findPath(DeploySettings.etcDir+"tools/android/dx.jar",false);
      if (dxjar == null)
         throw new DeployerException("File android/dx.jar not found!");
      String javaExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("java"));
      String []cmd = {javaExe,"-classpath",DeploySettings.pathAddQuotes(dxjar),"com.android.dx.command.Main","--dex","--output=classes.dex",new File(jarOut).getAbsolutePath()}; // guich@tc124_3: use the absolute path for the file
      String out = Utils.exec(cmd, targetDir);
      if (!new File(targetDir+"classes.dex").exists())
         throw new DeployerException("An error occured when compiling the Java class with the Dalvik compiler. The command executed was: '"+Utils.toString(cmd)+"' at the folder '"+targetDir+"'\nThe output of the command is "+out);
      new File(jarOut).delete(); // delete the jar
   }
   
   private void updateResources() throws Exception
   {
      String ap = Utils.findPath(DeploySettings.etcDir+"launchers/android/resources.ap_",false);
      if (ap == null)
         throw new DeployerException("File android/resources.ap_ not found!");
      String apk = targetDir+fileName+".apk";
      ZipInputStream zis = new ZipInputStream(new FileInputStream(ap));
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apk));

      ZipEntry ze,ze2;
      
      // search the input zip file, convert and write each entry to the output zip file
      while ((ze = zis.getNextEntry()) != null)
      {
         String name = ze.getName();
         // keep all the metadata if possible
         if (ze.getMethod() != ZipEntry.STORED)
         {
            ze2 = new ZipEntry(ze);
            // little trick to make the entry reusable
            ze2.setCompressedSize(-1);
         }
         else
         {
            // the trick above doesn't work with stored entries, so we'll ignore the metadata and use only the name
            ze2 = new ZipEntry(ze.getName());
         }
         if (name.indexOf("tcfiles.zip") >= 0)
         {
            zos.putNextEntry(ze2);
            insertTCFiles_zip(ze2, zos);
         }
         else
         if (name.indexOf("resources.arsc") >= 0)
         {
            zos.putNextEntry(ze2);
            insertResources_arsc(zis, zos);
         }
         else
         if (name.indexOf("icon.png") >= 0)
         {
            zos.putNextEntry(ze2);
            insertIcon_png(zos, name);
         }
         else
         if (name.indexOf("AndroidManifest.xml") >= 0)
         {
            zos.putNextEntry(ze2);
            insertAndroidManifest_xml(zis,zos);
         }
         else if (singleApk)
         {
            byte[] bytes = Utils.readJavaInputStream(zis);
            if (name.endsWith(".ogg")) // for zxing beep.ogg file 
            {
               CRC32 crc = new CRC32();
               crc.update(bytes); 
               ze2.setCrc(crc.getValue());
               ze2.setMethod(ZipEntry.STORED);
               ze2.setCompressedSize(bytes.length);
               ze2.setSize(bytes.length);
            }
            zos.putNextEntry(ze2);
            zos.write(bytes,0,bytes.length);
            zis.closeEntry();
         }
         zos.closeEntry();
      }
      if (singleApk)
      {
         processClassesDex(tcFolder+"TotalCross.apk", "classes.dex", zos);
      }
      else
      {
         // add classes.dex
         zos.putNextEntry(new ZipEntry("classes.dex"));
         totalcross.io.File f = new totalcross.io.File(targetDir+"classes.dex",totalcross.io.File.READ_WRITE);
         int n;
         while ((n=f.readBytes(buf,0,buf.length)) > 0)
            zos.write(buf, 0, n);
         zos.closeEntry();
         f.delete(); // delete original file
      }
      // include the vm and litebase
      if (tcFolder != null)
         copyZipEntry(tcFolder+"TotalCross.apk", "lib/armeabi/libtcvm.so", zos);
      
      zis.close();
      zos.close();      
   }

   // http://strazzere.com/blog/?p=3
   private static void calcSignature(byte bytes[]) 
   { 
      java.security.MessageDigest md; 
      try 
      { 
         md = java.security.MessageDigest.getInstance("SHA-1"); 
      } 
      catch(java.security.NoSuchAlgorithmException ex) 
      { 
         throw new RuntimeException(ex); 
      } 
      md.update(bytes, 32, bytes.length - 32); 
      try 
      { 
         int amt = md.digest(bytes, 12, 20); 
         if (amt != 20) 
            throw new RuntimeException((new StringBuilder()).append("unexpected digest write:").append(amt).append("bytes").toString()); 
      } 
      catch(java.security.DigestException ex) 
      { 
         throw new RuntimeException(ex); 
      } 
   } 
    
   private static void calcChecksum(byte bytes[]) 
   { 
      Adler32 a32 = new Adler32(); 
      a32.update(bytes, 12, bytes.length - 12); 
      int sum = (int)a32.getValue(); 
      bytes[8] = (byte)sum; 
      bytes[9] = (byte)(sum >> 8); 
      bytes[10] = (byte)(sum >> 16); 
      bytes[11] = (byte)(sum >> 24); 
   }  
   
   private void processClassesDex(String srcZip, String fileName, ZipOutputStream dstZip) throws Exception
   {
      dstZip.putNextEntry(new ZipEntry(fileName));
      byte[] bytes = Utils.loadZipEntry(srcZip,fileName);

      replaceBytes(bytes, sourcePackage.getBytes(), targetPackage.getBytes());
      if (DeploySettings.autoStart || DeploySettings.isService)
      {
         System.out.println("Is service.");
         replaceBytes(bytes, new byte[]{(byte)0x71,(byte)0xC3,(byte)0x5B,(byte)0x07}, DeploySettings.isService ? new byte[]{1,0,0,0} : new byte[]{0,0,0,0});
      }
      calcSignature(bytes);
      calcChecksum(bytes);
      dstZip.write(bytes,0,bytes.length);
      dstZip.closeEntry();
   }
   
   private void replaceBytes(byte[] bytes, byte[] fromBytes, byte[] toBytes)
   {
      int ofs=0;
      while ((ofs = Utils.indexOf(bytes, fromBytes, false, ofs)) != -1)
      {
         totalcross.sys.Vm.arrayCopy(toBytes, 0, bytes, ofs, toBytes.length);
         ofs += toBytes.length;
      }
   }

   private void copyZipEntry(String srcZip, String fileName, ZipOutputStream dstZip) throws Exception
   {
      byte[] bytes = Utils.loadZipEntry(srcZip,fileName);
      ZipEntry ze = new ZipEntry(fileName);
      
      if (fileName.endsWith(".ogg")) // for zxing beep.ogg file 
      {
         CRC32 crc = new CRC32();
         crc.update(bytes); 
         ze.setCrc(crc.getValue());
         ze.setMethod(ZipEntry.STORED);
         ze.setCompressedSize(bytes.length);
         ze.setSize(bytes.length);
      }
      
      dstZip.putNextEntry(ze);
      dstZip.write(bytes,0,bytes.length);
      dstZip.closeEntry();
   }
   
   private void insertIcon_png(ZipOutputStream zos, String name) throws Exception
   {
      if (DeploySettings.bitmaps != null)
      {
         int res;
         if (name.startsWith("res/drawable-xhdpi") && name.endsWith("icon.png"))   res = 96; else
         if (name.startsWith("res/drawable-xxhdpi") && name.endsWith("icon.png"))  res = 144; else
         if (name.startsWith("res/drawable-xxxhdpi") && name.endsWith("icon.png")) res = 192; 
         else res = 72;
         DeploySettings.bitmaps.saveAndroidIcon(zos,res); // libraries don't have icons
      }
   }

   private totalcross.io.ByteArrayStream readInputStream(java.io.InputStream is)
   {
      totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(2048);
      int len;
      while (true)
      {
         try
         {
            len = is.read(buf);
         }
         catch (java.io.IOException e) {break;}
         if (len > 0)
            bas.writeBytes(buf,0,len);
         else
            break;
      }
      return bas;
   }
   
   private void insertAndroidManifest_xml(InputStream zis, OutputStream zos) throws Exception
   {
      totalcross.io.ByteArrayStream bas;
      if (!singleApk)
         bas = readInputStream(zis);
      else
      {
         byte[] bytes = Utils.loadFile(DeploySettings.etcDir+"tools/android/AndroidManifest_singleapk.xml",true);
         bas = new totalcross.io.ByteArrayStream(bytes);
         bas.skipBytes(bytes.length);
      }
      bas.mark();
      totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
      String oldPackage, oldTitle, oldActivity;
      
      if (singleApk)
      {
         oldPackage  = sourcePackage.replace('/','.');
         oldTitle    = "Stub";
         oldActivity = null;
      }
      else
      {
         oldPackage  = "totalcross.android";
         oldTitle    = "TotalCross Virtual Machine";
         oldActivity = "totalcross.android.Loader";
      }

      String oldVersion  = "!1.0!";
      String oldSharedId = singleApk ? "totalcross.app.sharedid" : null;
      
      String newPackage  = targetPackage.replace('/','.');
      String newVersion  = DeploySettings.appVersion != null ? DeploySettings.appVersion : "1.0";
      String newTitle    = DeploySettings.appTitle;
      String newActivity = singleApk ? null : "."+fileName;
      String newSharedId = singleApk ? "totalcross.app.app"+DeploySettings.applicationId.toLowerCase() : null;
      
      int oldSize = bas.available();
      int difPackage  = (newPackage .length() - oldPackage .length()) * 2;
      int difVersion  = (newVersion .length() - oldVersion .length()) * 2;
      int difTitle    = (newTitle   .length() - oldTitle   .length()) * 2;
      int difActivity = singleApk ? 0 : (newActivity.length() - oldActivity.length()) * 2;
      int difSharedId = !singleApk ? 0 : (newSharedId.length() - oldSharedId.length()) * 2;
      int dif = difPackage + difVersion + difTitle + difActivity + difSharedId;
      String newTcPackage = "totalcross.and"+DeploySettings.applicationId.toLowerCase(); // totalcross.android -> totalcross.app.tctestwin

      // get the xml size
      bas.setPos(12); 
      int xmlsize = ds.readInt();
      xmlsize += dif;
      int gap = ((xmlsize + 3) & ~3) - xmlsize;

      int newSize = oldSize + dif + gap;

      // update new size and position of €
      bas.setPos(4);  ds.writeInt(newSize);
      bas.setPos(12); ds.writeInt(xmlsize + gap);
      int len = ds.readInt();
      
      bas.setPos(40);
      int[] positions = new int[len+1];
      for (int i =0,last=len-1; i <= last; i++)
      {
         if (i < last) positions[i+1] = ds.readInt();
         if (DEBUG) System.out.println(i+" "+positions[i]+" ("+(positions[i+1]-positions[i])+")");
      }
      
      String[] strings = new String[len];
      int pos0 = bas.getPos();
      for (int i =0; i < len; i++)
      {
         int pos = bas.getPos();
         String s = new String(ds.readChars());
         if (DEBUG) System.out.println(i+" #"+pos+" ("+(pos-pos0)+") "+s+" ("+s.length()+" - "+(s.length()*2+2+2)+")");
         strings[i] = s;         
         bas.skipBytes(2); // skip 0-terminated string
      }
      
      // read the rest of the resource (other kinds of data)
      int resSize = bas.available();
      byte[] res = new byte[resSize];
      bas.readBytes(res,0,resSize);
      int ofs;
      
      // now the "versionCode" is used to store some properties of the application
      // search and replace the value of versionCode="305419896" (0x12345678) with the application properties
      // note that currently there's no application properties!
      byte[] versionCodeMark = {(byte)0x78,(byte)0x56,(byte)0x34,(byte)0x12};
      ofs = Utils.indexOf(res, versionCodeMark, false);
      if (ofs == -1)
         throw new DeployerException("Error: could not find position for versionCode");
      totalcross.io.ByteArrayStream dtbas = new totalcross.io.ByteArrayStream(res);
      dtbas.setPos(ofs);
      totalcross.io.DataStreamLE dsbas = new totalcross.io.DataStreamLE(dtbas);
      int props = Utils.version2int(newVersion);
      dsbas.writeInt(props);
      
      // if is full screen, search and replace TCThemeNC by TCThemeFS
      // if this fails again, 
      //    0. build and store original "G:\TotalCross\TotalCrossVM\builders\droid\build\outputs\apk\droid-singleApk-release.apk" / AndroidManifest.xml somewhere
      //    1. open G:\TotalCross\TotalCrossVM\builders\droid\src\main\AndroidManifest.xml
      //    2. replace android:theme="@style/TCThemeNS" by android:theme="@style/TCThemeFS"
      //    3. build and store the changed AndroidManifest.xml inside the apk and then compare the differences.
      if (DeploySettings.isFullScreenPlatform(totalcross.sys.Settings.ANDROID)) // guich@tc120_59
      {
         byte[] themeMark = singleApk ? new byte[]{(byte)0x00,(byte)0x00,(byte)0x01,(byte)0xA4} : new byte[]{(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x7f};
         ofs = Utils.indexOf(res, themeMark, false);
         if (ofs == -1)
            throw new DeployerException("Error: could not find position for theme");
         if (singleApk)
            res[ofs+3] = (byte)0xA3;
         else
            res[ofs] = 1; // set Fullscreen attribute: TCThemeNC -> TCThemeFS
      }
      
      // now, change the names accordingly
      for (int i = 0; i < len; i++)
      {
         String s = strings[i];
         if (s.startsWith(oldPackage))
         {
            if (singleApk)
            {
               strings[i] = newPackage+s.substring(oldPackage.length());
               System.out.println("1. Replacing "+s+" -> "+strings[i]);
//            else
//            if (s.endsWith("google_measurement_service"))
//               strings[i] = newTcPackage+s.substring(oldPackage.length());
            }
         }
         if (oldPackage != null && s.equals(oldPackage))
         {
            strings[i] = newPackage;
            System.out.println("2. Replacing "+s+" -> "+strings[i]);
         }
         else
         if (oldVersion != null && s.equals(oldVersion))
         {
            strings[i] = newVersion;
            System.out.println("3. Replacing "+s+" -> "+strings[i]);
         }
         else
         if (oldTitle != null && s.equals(oldTitle))
         {
            strings[i] = newTitle;
            System.out.println("4. Replacing "+s+" -> "+strings[i]);
         }
         else
         if (oldActivity != null && s.equals(oldActivity))
         {
            strings[i] = newActivity;
            System.out.println("5. Replacing "+s+" -> "+strings[i]);
         }
         else
         if (oldSharedId != null && s.equals(oldSharedId))
         {
            strings[i] = newSharedId;
            System.out.println("6. Replacing "+s+" -> "+strings[i]);
         }
      }
      // update the offsets table
      for (int i = 0; i < len; i++)
         positions[i+1] = positions[i] + (strings[i].length()*2+2+2); // 2 for the positions, and 2 for the 0 termination
      
      // now write everything again
      bas.setPos(36);
      for (int i =0; i < len; i++)
         ds.writeInt(positions[i]);
      for (int i =0; i < len; i++)
      {
         String s = strings[i];
         ds.writeChars(s, s.length());
         ds.writeShort(0);
      }
      if (gap > 0)
         for (int i =0; i < gap; i++)
            ds.writeByte(0);
      
      ds.writeBytes(res);
      int nn = bas.getPos();
      if (nn != newSize)
         throw new DeployerException("Something went wrong when parsing AndroidManifest.xml. Expected size is "+newSize+", but got "+nn); 
      
      zos.write(bas.getBuffer(), 0, newSize);
   }
   
   private void insertResources_arsc(InputStream zis, OutputStream zos) throws Exception
   {
      byte[] all;
      byte[] key = new byte[]{'t',(byte)0,'o',(byte)0,'t',(byte)0,'a',(byte)0,'l',(byte)0,'c',(byte)0,'r',(byte)0,'o',(byte)0,'s',(byte)0,'s',(byte)0,'.',(byte)0,'a',(byte)0,'n',(byte)0,'d',(byte)0,'r',(byte)0,'o',(byte)0,'i',(byte)0,'d',(byte)0};
      if (singleApk)
         all = Utils.loadFile(DeploySettings.etcDir+"tools/android/resources_singleapk.arsc",true);
      else
         all = readInputStream(zis).toByteArray();
      int ofs = Utils.indexOf(all, key, false);
      if (ofs == -1)
         throw new DeployerException("Could not find position for totalcross.android in arsc.");
      // write the name
      char[] chars = targetPackage.replace('/','.').toCharArray();
      if (chars.length > 0x7F)
         throw new DeployerException("The package name length can't be bigger than "+0x7F);
      int i =0,n = ofs + 0x7F * 2;
      for (; i < chars.length; i++, ofs += 2)
         all[ofs] = (byte)chars[i];
      while (ofs < n)
         all[ofs++] = (byte)0;
      zos.write(all);
   }

   private void insertTCFiles_zip(ZipEntry ze, ZipOutputStream z) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
      ZipOutputStream zos = new ZipOutputStream(baos);
      
      // parse the android.pkg
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("android.pkg", ht);

      Vector vLocals  = (Vector)ht.get("[L]"); if (vLocals == null) vLocals  = new Vector();
      Vector vGlobals = (Vector)ht.get("[G]"); if (vGlobals== null) vGlobals = new Vector();
      vLocals.addElements(DeploySettings.tczs);
      if (vGlobals.size() > 0)
         vLocals.addElements(vGlobals.toObjectArray());
      if (singleApk) // include the vm?
      {
         // tc is always included
         // include non-binary files
         vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"TCBase.tcz");
         vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"TCUI.tcz");
         vLocals.addElement(DeploySettings.folderTotalCross3DistVM+DeploySettings.fontTCZ);
         vLocals.addElement(DeploySettings.folderTotalCross3DistVM+"LitebaseLib.tcz");
      }         

      Utils.preprocessPKG(vLocals,true);
      for (int i =0, n = vLocals.size(); i < n; i++)
      {
         String []pathnames = totalcross.sys.Convert.tokenizeString((String)vLocals.items[i],',');
         String pathname = pathnames[0];
         String name = Utils.getFileName(pathname);
         if (pathnames.length > 1)
         {
            name = totalcross.sys.Convert.appendPath(pathnames[1],name);
            if (name.startsWith("/"))
               name = name.substring(1);
         }
         // tcz's name must match the lowercase sharedid
         if (tcFolder != null && pathname.equals(DeploySettings.tczFileName)) 
            name = targetTCZ+".tcz";
         FileInputStream fis;
         try
         {
            fis = new FileInputStream(pathname);
         }
         catch (FileNotFoundException fnfe)
         {
            try
            {
               fis = new FileInputStream(totalcross.sys.Convert.appendPath(DeploySettings.currentDir, pathname));
            }
            catch (FileNotFoundException fnfe2)
            {
               String pp = Utils.findPath(pathname,true);
               if (pp != null)
                  fis = new FileInputStream(pp);
               else
               {
                  System.out.println("File not found: "+pathname);
                  continue;
               }
            }
         }
         byte[] bytes = new byte[fis.available()];
         fis.read(bytes);
         fis.close();
         ZipEntry zze = new ZipEntry(name);
         if (name.endsWith(".tcz")) // tcz files will be stored without compression so they can be read directly
         {
            CRC32 crc = new CRC32();
            crc.update(bytes); 
            zze.setCrc(crc.getValue());
            zze.setMethod(ZipEntry.STORED);
            zze.setCompressedSize(bytes.length);
            zze.setSize(bytes.length);
         }            
         zos.putNextEntry(zze);
         zos.write(bytes);
         zos.closeEntry();
      }
      zos.close();
      // add the file UNCOMPRESSED
      byte[] bytes = baos.toByteArray();
      CRC32 crc = new CRC32();
      crc.update(bytes); 
      ze.setCrc(crc.getValue());
      ze.setMethod(ZipEntry.STORED);
      ze.setCompressedSize(bytes.length);
      ze.setSize(bytes.length);
      z.write(bytes);
   }

   private void convertConstantPool(ZipInputStream is, ZipOutputStream os) throws Exception
   {
      totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(1024);
      totalcross.io.DataStream ds = new totalcross.io.DataStream(bas);
      int n;

      while ((n = is.read(buf)) > 0)
         bas.writeBytes(buf, 0, n);
      bas.setPos(0);
      JavaClass jclass = new JavaClass();
      jclass.load(ds);

      checkConstantPool(jclass);

      bas.reuse();
      jclass.save(ds);
      os.write(bas.getBuffer(), 0, bas.getPos());
   }

   private static void checkConstantPool(JavaClass jclass)
   {
      UTF8 descriptor;
      // Check all class and name/type constants
      if (DEBUG) System.out.println("Constant pool");
      int count = jclass.constantPool.size();
      for (int i = 1; i < count; i++)
      {
         JavaConstant constant = (JavaConstant)jclass.constantPool.items[i];
         i += constant.slots() - 1; // skip empty slots

         switch (constant.tag)
         {
            case JavaConstant.CONSTANT_INTEGER:
               String cla = jclass.getClassName();
               if ((DeploySettings.autoStart || DeploySettings.isService) && cla.endsWith("/StartupIntentReceiver") && ((Integer)constant.info).value == 123454321)
               {
                  Integer it = new Integer();
                  it.value = DeploySettings.isService ? 1 : 0;
                  constant.info = it;
               }
               break;
            case JavaConstant.CONSTANT_CLASS:
               descriptor = ((Class)constant.info).getValueAsName();
               descriptor.value = convertName(descriptor.value);
               break;
            case JavaConstant.CONSTANT_NAME_AND_TYPE:
               descriptor = ((NameAndType)constant.info).getValue2AsDescriptor();
               descriptor.value = convertName(descriptor.value);
               break;
         }
      }

      // Check class fields
      if (DEBUG) System.out.println("Fields");
      count = jclass.fields.size();
      for (int i = 0; i < count; i ++)
      {
         JavaField field = (JavaField)jclass.fields.items[i];

         descriptor = (UTF8)field.descriptor.info;
         descriptor.value = convertName(descriptor.value);

         // Check field attributes
         int count2 = field.attributes.size();
         for (int j = 0; j < count2; j ++)
            checkAttribute((JavaAttribute)field.attributes.items[j]);
      }

      // Check class methods
      if (DEBUG) System.out.println("Methods");
      count = jclass.methods.size();
      for (int i = 0; i < count; i ++)
      {
         JavaMethod method = (JavaMethod)jclass.methods.items[i];

         descriptor = (UTF8)method.descriptor.info;
         descriptor.value = convertName(descriptor.value);

         // Check method attributes
         int count2 = method.attributes.size();
         for (int j = 0; j < count2; j ++)
            checkAttribute((JavaAttribute)method.attributes.items[j]);
      }

      // Check class attributes
      if (DEBUG) System.out.println("Atributes");
      count = jclass.attributes.size();
      for (int i = 0; i < count; i ++)
         checkAttribute((JavaAttribute)jclass.attributes.items[i]);

      if (DEBUG) System.out.println("FINISHED");
   }

   /**
    * @param attribute
    * @param classes
    */
   private static void checkAttribute(JavaAttribute attribute)
   {
      if (attribute.info instanceof SourceFile)
      {
         JavaConstant source = ((SourceFile)attribute.info).sourceFile;
         UTF8 descriptor = (UTF8)source.info;
         descriptor.value = convertName(descriptor.value);
      }
      else
      if (attribute.info instanceof LocalVariableTable)
      {
         Vector variables = ((LocalVariableTable)attribute.info).variables;
         int count = variables.size();
         for (int i = 0; i < count; i ++)
         {
            UTF8 descriptor = (UTF8)((LocalVariableTable.LocalVariable)variables.items[i]).descriptor.info;
            descriptor.value = convertName(descriptor.value);
         }
      }
      else if (attribute.info instanceof Code)
      {
         Code code = (Code)attribute.info;
         Vector attributes = code.attributes;
         int count = attributes.size();
         for (int i = 0; i < count; i ++)
            checkAttribute((JavaAttribute)attributes.items[i]);
      }
   }

   private static String convertName(String name)
   {
      String value = name.replace("totalcross/app/stub", targetPackage); // totalcross/app/stub/R -> totalcross/app/uigadgets/R
      value = value.replace("Stub",fileName); // totalcross/app/stub/Stub -> totalcross/app/uigadgets/Stub
      if (DEBUG) System.out.println(name+" -> "+value);
      return value;
   }

   String[] extras = 
   {
//         "res/anim/abc_fade_in.xml",
//         "res/anim/abc_fade_out.xml",
//         "res/anim/abc_grow_fade_in_from_bottom.xml",
//         "res/anim/abc_popup_enter.xml",
//         "res/anim/abc_popup_exit.xml",
//         "res/anim/abc_shrink_fade_out_from_bottom.xml",
//         "res/anim/abc_slide_in_bottom.xml",
//         "res/anim/abc_slide_in_top.xml",
//         "res/anim/abc_slide_out_bottom.xml",
//         "res/anim/abc_slide_out_top.xml",
//         "res/color/abc_background_cache_hint_selector_material_dark.xml",
//         "res/color/abc_background_cache_hint_selector_material_light.xml",
//         "res/color/abc_primary_text_disable_only_material_dark.xml",
//         "res/color/abc_primary_text_disable_only_material_light.xml",
//         "res/color/abc_primary_text_material_dark.xml",
//         "res/color/abc_primary_text_material_light.xml",
//         "res/color/abc_search_url_text.xml",
//         "res/color/abc_secondary_text_material_dark.xml",
//         "res/color/abc_secondary_text_material_light.xml",
//         "res/color/switch_thumb_material_dark.xml",
//         "res/color/switch_thumb_material_light.xml",
//         "res/color-v11/abc_background_cache_hint_selector_material_dark.xml",
//         "res/color-v11/abc_background_cache_hint_selector_material_light.xml",
//         "res/drawable/abc_btn_borderless_material.xml",
//         "res/drawable/abc_btn_check_material.xml",
//         "res/drawable/abc_btn_default_mtrl_shape.xml",
//         "res/drawable/abc_btn_radio_material.xml",
//         "res/drawable/abc_cab_background_internal_bg.xml",
//         "res/drawable/abc_cab_background_top_material.xml",
//         "res/drawable/abc_dialog_material_background_dark.xml",
//         "res/drawable/abc_dialog_material_background_light.xml",
//         "res/drawable/abc_edit_text_material.xml",
//         "res/drawable/abc_item_background_holo_dark.xml",
//         "res/drawable/abc_item_background_holo_light.xml",
//         "res/drawable/abc_list_selector_background_transition_holo_dark.xml",
//         "res/drawable/abc_list_selector_background_transition_holo_light.xml",
//         "res/drawable/abc_list_selector_holo_dark.xml",
//         "res/drawable/abc_list_selector_holo_light.xml",
//         "res/drawable/abc_ratingbar_full_material.xml",
//         "res/drawable/abc_spinner_textfield_background_material.xml",
//         "res/drawable/abc_switch_thumb_material.xml",
//         "res/drawable/abc_tab_indicator_material.xml",
//         "res/drawable/abc_textfield_search_material.xml",
//         "res/drawable/cast_ic_notification_connecting.xml",
//         "res/drawable/mr_ic_media_route_connecting_mono_dark.xml",
//         "res/drawable/mr_ic_media_route_connecting_mono_light.xml",
//         "res/drawable/mr_ic_media_route_mono_dark.xml",
//         "res/drawable/mr_ic_media_route_mono_light.xml",
//         "res/drawable/mr_ic_pause_dark.xml",
//         "res/drawable/mr_ic_pause_light.xml",
//         "res/drawable/mr_ic_play_dark.xml",
//         "res/drawable/mr_ic_play_light.xml",
//         "res/drawable/mr_ic_settings_dark.xml",
//         "res/drawable/mr_ic_settings_light.xml",
//         "res/drawable/share_via_barcode.png",
//         "res/drawable-hdpi-v4/abc_ab_share_pack_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_btn_check_to_on_mtrl_000.png",
//         "res/drawable-hdpi-v4/abc_btn_check_to_on_mtrl_015.png",
//         "res/drawable-hdpi-v4/abc_btn_radio_to_on_mtrl_000.png",
//         "res/drawable-hdpi-v4/abc_btn_radio_to_on_mtrl_015.png",
//         "res/drawable-hdpi-v4/abc_btn_rating_star_off_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_btn_rating_star_on_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_btn_switch_to_on_mtrl_00001.9.png",
//         "res/drawable-hdpi-v4/abc_btn_switch_to_on_mtrl_00012.9.png",
//         "res/drawable-hdpi-v4/abc_cab_background_top_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_clear_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_commit_search_api_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_go_search_api_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_moreoverflow_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_paste_mtrl_am_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_selectall_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_menu_share_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_search_api_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_ic_voice_search_api_mtrl_alpha.png",
//         "res/drawable-hdpi-v4/abc_list_divider_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_list_focused_holo.9.png",
//         "res/drawable-hdpi-v4/abc_list_longpressed_holo.9.png",
//         "res/drawable-hdpi-v4/abc_list_pressed_holo_dark.9.png",
//         "res/drawable-hdpi-v4/abc_list_pressed_holo_light.9.png",
//         "res/drawable-hdpi-v4/abc_list_selector_disabled_holo_dark.9.png",
//         "res/drawable-hdpi-v4/abc_list_selector_disabled_holo_light.9.png",
//         "res/drawable-hdpi-v4/abc_menu_hardkey_panel_mtrl_mult.9.png",
//         "res/drawable-hdpi-v4/abc_popup_background_mtrl_mult.9.png",
//         "res/drawable-hdpi-v4/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_switch_track_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_tab_indicator_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_textfield_activated_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_textfield_default_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_textfield_search_activated_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_textfield_search_default_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/abc_text_cursor_mtrl_alpha.9.png",
//         "res/drawable-hdpi-v4/cast_ic_notification_0.png",
//         "res/drawable-hdpi-v4/cast_ic_notification_1.png",
//         "res/drawable-hdpi-v4/cast_ic_notification_2.png",
//         "res/drawable-hdpi-v4/cast_ic_notification_on.png",
//         "res/drawable-hdpi-v4/ic_cast_dark.png",
//         "res/drawable-hdpi-v4/ic_cast_disabled_light.png",
//         "res/drawable-hdpi-v4/ic_cast_light.png",
//         "res/drawable-hdpi-v4/ic_cast_off_light.png",
//         "res/drawable-hdpi-v4/ic_cast_on_0_light.png",
//         "res/drawable-hdpi-v4/ic_cast_on_1_light.png",
//         "res/drawable-hdpi-v4/ic_cast_on_2_light.png",
//         "res/drawable-hdpi-v4/ic_cast_on_light.png",
//         "res/drawable-hdpi-v4/ic_media_pause.png",
//         "res/drawable-hdpi-v4/ic_media_play.png",
//         "res/drawable-hdpi-v4/ic_media_route_disabled_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_media_route_off_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_media_route_on_0_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_media_route_on_1_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_media_route_on_2_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_media_route_on_mono_dark.png",
//         "res/drawable-hdpi-v4/ic_pause_dark.png",
//         "res/drawable-hdpi-v4/ic_pause_light.png",
//         "res/drawable-hdpi-v4/ic_play_dark.png",
//         "res/drawable-hdpi-v4/ic_play_light.png",
//         "res/drawable-hdpi-v4/ic_setting_dark.png",
//         "res/drawable-hdpi-v4/ic_setting_light.png",
//         "res/drawable-hdpi-v4/mr_ic_audio_vol.png",
//         "res/drawable-ldrtl-hdpi-v17/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-hdpi-v17/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-hdpi-v17/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-ldrtl-hdpi-v17/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-ldrtl-mdpi-v17/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-mdpi-v17/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-mdpi-v17/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-ldrtl-mdpi-v17/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-ldrtl-xhdpi-v17/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xhdpi-v17/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xhdpi-v17/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-ldrtl-xhdpi-v17/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-ldrtl-xxhdpi-v17/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xxhdpi-v17/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xxhdpi-v17/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-ldrtl-xxhdpi-v17/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-ldrtl-xxxhdpi-v17/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xxxhdpi-v17/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-ldrtl-xxxhdpi-v17/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-ldrtl-xxxhdpi-v17/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_ab_share_pack_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_btn_check_to_on_mtrl_000.png",
//         "res/drawable-mdpi-v4/abc_btn_check_to_on_mtrl_015.png",
//         "res/drawable-mdpi-v4/abc_btn_radio_to_on_mtrl_000.png",
//         "res/drawable-mdpi-v4/abc_btn_radio_to_on_mtrl_015.png",
//         "res/drawable-mdpi-v4/abc_btn_rating_star_off_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_btn_rating_star_on_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_btn_switch_to_on_mtrl_00001.9.png",
//         "res/drawable-mdpi-v4/abc_btn_switch_to_on_mtrl_00012.9.png",
//         "res/drawable-mdpi-v4/abc_cab_background_top_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_clear_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_commit_search_api_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_go_search_api_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_moreoverflow_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_paste_mtrl_am_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_selectall_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_menu_share_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_search_api_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_ic_voice_search_api_mtrl_alpha.png",
//         "res/drawable-mdpi-v4/abc_list_divider_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_list_focused_holo.9.png",
//         "res/drawable-mdpi-v4/abc_list_longpressed_holo.9.png",
//         "res/drawable-mdpi-v4/abc_list_pressed_holo_dark.9.png",
//         "res/drawable-mdpi-v4/abc_list_pressed_holo_light.9.png",
//         "res/drawable-mdpi-v4/abc_list_selector_disabled_holo_dark.9.png",
//         "res/drawable-mdpi-v4/abc_list_selector_disabled_holo_light.9.png",
//         "res/drawable-mdpi-v4/abc_menu_hardkey_panel_mtrl_mult.9.png",
//         "res/drawable-mdpi-v4/abc_popup_background_mtrl_mult.9.png",
//         "res/drawable-mdpi-v4/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_switch_track_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_tab_indicator_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_textfield_activated_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_textfield_default_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_textfield_search_activated_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_textfield_search_default_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/abc_text_cursor_mtrl_alpha.9.png",
//         "res/drawable-mdpi-v4/cast_ic_notification_0.png",
//         "res/drawable-mdpi-v4/cast_ic_notification_1.png",
//         "res/drawable-mdpi-v4/cast_ic_notification_2.png",
//         "res/drawable-mdpi-v4/cast_ic_notification_on.png",
//         "res/drawable-mdpi-v4/ic_cast_dark.png",
//         "res/drawable-mdpi-v4/ic_cast_disabled_light.png",
//         "res/drawable-mdpi-v4/ic_cast_light.png",
//         "res/drawable-mdpi-v4/ic_cast_off_light.png",
//         "res/drawable-mdpi-v4/ic_cast_on_0_light.png",
//         "res/drawable-mdpi-v4/ic_cast_on_1_light.png",
//         "res/drawable-mdpi-v4/ic_cast_on_2_light.png",
//         "res/drawable-mdpi-v4/ic_cast_on_light.png",
//         "res/drawable-mdpi-v4/ic_media_pause.png",
//         "res/drawable-mdpi-v4/ic_media_play.png",
//         "res/drawable-mdpi-v4/ic_media_route_disabled_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_media_route_off_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_media_route_on_0_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_media_route_on_1_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_media_route_on_2_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_media_route_on_mono_dark.png",
//         "res/drawable-mdpi-v4/ic_pause_dark.png",
//         "res/drawable-mdpi-v4/ic_pause_light.png",
//         "res/drawable-mdpi-v4/ic_play_dark.png",
//         "res/drawable-mdpi-v4/ic_play_light.png",
//         "res/drawable-mdpi-v4/ic_setting_dark.png",
//         "res/drawable-mdpi-v4/ic_setting_light.png",
//         "res/drawable-mdpi-v4/mr_ic_audio_vol.png",
//         "res/drawable-xhdpi-v4/abc_ab_share_pack_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_btn_check_to_on_mtrl_000.png",
//         "res/drawable-xhdpi-v4/abc_btn_check_to_on_mtrl_015.png",
//         "res/drawable-xhdpi-v4/abc_btn_radio_to_on_mtrl_000.png",
//         "res/drawable-xhdpi-v4/abc_btn_radio_to_on_mtrl_015.png",
//         "res/drawable-xhdpi-v4/abc_btn_rating_star_off_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_btn_rating_star_on_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_btn_switch_to_on_mtrl_00001.9.png",
//         "res/drawable-xhdpi-v4/abc_btn_switch_to_on_mtrl_00012.9.png",
//         "res/drawable-xhdpi-v4/abc_cab_background_top_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_clear_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_commit_search_api_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_go_search_api_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_moreoverflow_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_paste_mtrl_am_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_selectall_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_menu_share_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_search_api_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_ic_voice_search_api_mtrl_alpha.png",
//         "res/drawable-xhdpi-v4/abc_list_divider_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_list_focused_holo.9.png",
//         "res/drawable-xhdpi-v4/abc_list_longpressed_holo.9.png",
//         "res/drawable-xhdpi-v4/abc_list_pressed_holo_dark.9.png",
//         "res/drawable-xhdpi-v4/abc_list_pressed_holo_light.9.png",
//         "res/drawable-xhdpi-v4/abc_list_selector_disabled_holo_dark.9.png",
//         "res/drawable-xhdpi-v4/abc_list_selector_disabled_holo_light.9.png",
//         "res/drawable-xhdpi-v4/abc_menu_hardkey_panel_mtrl_mult.9.png",
//         "res/drawable-xhdpi-v4/abc_popup_background_mtrl_mult.9.png",
//         "res/drawable-xhdpi-v4/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_switch_track_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_tab_indicator_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_textfield_activated_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_textfield_default_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_textfield_search_activated_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_textfield_search_default_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/abc_text_cursor_mtrl_alpha.9.png",
//         "res/drawable-xhdpi-v4/cast_ic_notification_0.png",
//         "res/drawable-xhdpi-v4/cast_ic_notification_1.png",
//         "res/drawable-xhdpi-v4/cast_ic_notification_2.png",
//         "res/drawable-xhdpi-v4/cast_ic_notification_on.png",
//         "res/drawable-xhdpi-v4/ic_cast_dark.png",
//         "res/drawable-xhdpi-v4/ic_cast_disabled_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_off_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_on_0_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_on_1_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_on_2_light.png",
//         "res/drawable-xhdpi-v4/ic_cast_on_light.png",
//         "res/drawable-xhdpi-v4/ic_media_pause.png",
//         "res/drawable-xhdpi-v4/ic_media_play.png",
//         "res/drawable-xhdpi-v4/ic_media_route_disabled_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_media_route_off_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_media_route_on_0_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_media_route_on_1_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_media_route_on_2_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_media_route_on_mono_dark.png",
//         "res/drawable-xhdpi-v4/ic_pause_dark.png",
//         "res/drawable-xhdpi-v4/ic_pause_light.png",
//         "res/drawable-xhdpi-v4/ic_play_dark.png",
//         "res/drawable-xhdpi-v4/ic_play_light.png",
//         "res/drawable-xhdpi-v4/ic_setting_dark.png",
//         "res/drawable-xhdpi-v4/ic_setting_light.png",
//         "res/drawable-xhdpi-v4/mr_ic_audio_vol.png",
//         "res/drawable-xxhdpi-v4/abc_ab_share_pack_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_btn_check_to_on_mtrl_000.png",
//         "res/drawable-xxhdpi-v4/abc_btn_check_to_on_mtrl_015.png",
//         "res/drawable-xxhdpi-v4/abc_btn_radio_to_on_mtrl_000.png",
//         "res/drawable-xxhdpi-v4/abc_btn_radio_to_on_mtrl_015.png",
//         "res/drawable-xxhdpi-v4/abc_btn_rating_star_off_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_btn_rating_star_on_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_btn_switch_to_on_mtrl_00001.9.png",
//         "res/drawable-xxhdpi-v4/abc_btn_switch_to_on_mtrl_00012.9.png",
//         "res/drawable-xxhdpi-v4/abc_cab_background_top_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_clear_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_commit_search_api_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_go_search_api_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_moreoverflow_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_paste_mtrl_am_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_selectall_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_menu_share_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_search_api_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_ic_voice_search_api_mtrl_alpha.png",
//         "res/drawable-xxhdpi-v4/abc_list_divider_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_focused_holo.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_longpressed_holo.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_pressed_holo_dark.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_pressed_holo_light.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_selector_disabled_holo_dark.9.png",
//         "res/drawable-xxhdpi-v4/abc_list_selector_disabled_holo_light.9.png",
//         "res/drawable-xxhdpi-v4/abc_menu_hardkey_panel_mtrl_mult.9.png",
//         "res/drawable-xxhdpi-v4/abc_popup_background_mtrl_mult.9.png",
//         "res/drawable-xxhdpi-v4/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_switch_track_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_tab_indicator_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_textfield_activated_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_textfield_default_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_textfield_search_activated_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_textfield_search_default_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/abc_text_cursor_mtrl_alpha.9.png",
//         "res/drawable-xxhdpi-v4/cast_ic_notification_0.png",
//         "res/drawable-xxhdpi-v4/cast_ic_notification_1.png",
//         "res/drawable-xxhdpi-v4/cast_ic_notification_2.png",
//         "res/drawable-xxhdpi-v4/cast_ic_notification_on.png",
//         "res/drawable-xxhdpi-v4/ic_cast_dark.png",
//         "res/drawable-xxhdpi-v4/ic_cast_disabled_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_off_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_on_0_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_on_1_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_on_2_light.png",
//         "res/drawable-xxhdpi-v4/ic_cast_on_light.png",
//         "res/drawable-xxhdpi-v4/ic_media_pause.png",
//         "res/drawable-xxhdpi-v4/ic_media_play.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_disabled_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_off_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_on_0_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_on_1_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_on_2_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_media_route_on_mono_dark.png",
//         "res/drawable-xxhdpi-v4/ic_pause_dark.png",
//         "res/drawable-xxhdpi-v4/ic_pause_light.png",
//         "res/drawable-xxhdpi-v4/ic_play_dark.png",
//         "res/drawable-xxhdpi-v4/ic_play_light.png",
//         "res/drawable-xxhdpi-v4/ic_setting_dark.png",
//         "res/drawable-xxhdpi-v4/ic_setting_light.png",
//         "res/drawable-xxhdpi-v4/mr_ic_audio_vol.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_check_to_on_mtrl_000.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_check_to_on_mtrl_015.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_radio_to_on_mtrl_000.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_radio_to_on_mtrl_015.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_switch_to_on_mtrl_00001.9.png",
//         "res/drawable-xxxhdpi-v4/abc_btn_switch_to_on_mtrl_00012.9.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_ab_back_mtrl_am_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_clear_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_copy_mtrl_am_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_cut_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_moreoverflow_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_paste_mtrl_am_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_selectall_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_menu_share_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_search_api_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_ic_voice_search_api_mtrl_alpha.png",
//         "res/drawable-xxxhdpi-v4/abc_spinner_mtrl_am_alpha.9.png",
//         "res/drawable-xxxhdpi-v4/abc_switch_track_mtrl_alpha.9.png",
//         "res/drawable-xxxhdpi-v4/abc_tab_indicator_mtrl_alpha.9.png",
         "res/layout/abc_action_bar_title_item.xml",
         "res/layout/abc_action_bar_up_container.xml",
         "res/layout/abc_action_bar_view_list_nav_layout.xml",
//         "res/layout/abc_action_menu_item_layout.xml",
//         "res/layout/abc_action_menu_layout.xml",
//         "res/layout/abc_action_mode_bar.xml",
//         "res/layout/abc_action_mode_close_item_material.xml",
//         "res/layout/abc_activity_chooser_view.xml",
//         "res/layout/abc_activity_chooser_view_list_item.xml",
//         "res/layout/abc_alert_dialog_material.xml",
//         "res/layout/abc_dialog_title_material.xml",
//         "res/layout/abc_expanded_menu_layout.xml",
//         "res/layout/abc_list_menu_item_checkbox.xml",
//         "res/layout/abc_list_menu_item_icon.xml",
//         "res/layout/abc_list_menu_item_layout.xml",
//         "res/layout/abc_list_menu_item_radio.xml",
//         "res/layout/abc_popup_menu_item_layout.xml",
//         "res/layout/abc_screen_content_include.xml",
//         "res/layout/abc_screen_simple.xml",
//         "res/layout/abc_screen_simple_overlay_action_mode.xml",
//         "res/layout/abc_screen_toolbar.xml",
//         "res/layout/abc_search_dropdown_item_icons_2line.xml",
//         "res/layout/abc_search_view.xml",
//         "res/layout/abc_select_dialog_material.xml",
//         "res/layout/abc_simple_dropdown_hint.xml",
//         "res/layout/bookmark_picker_list_item.xml",
         "res/layout/capture.xml",
         "res/layout/encode.xml",
//         "res/layout/help.xml",
//         "res/layout/history_list_item.xml",
         "res/layout/main.xml",
//         "res/layout/mr_media_route_chooser_dialog.xml",
//         "res/layout/mr_media_route_controller_material_dialog_b.xml",
//         "res/layout/mr_media_route_list_item.xml",
//         "res/layout/notification_media_action.xml",
//         "res/layout/notification_media_cancel_action.xml",
//         "res/layout/notification_template_big_media.xml",
//         "res/layout/notification_template_big_media_narrow.xml",
//         "res/layout/notification_template_lines.xml",
//         "res/layout/notification_template_media.xml",
//         "res/layout/notification_template_part_chronometer.xml",
//         "res/layout/notification_template_part_time.xml",
         "res/layout/route.xml",
//         "res/layout/search_book_contents.xml",
//         "res/layout/search_book_contents_header.xml",
//         "res/layout/search_book_contents_list_item.xml",
//         "res/layout/select_dialog_item_material.xml",
//         "res/layout/select_dialog_multichoice_material.xml",
//         "res/layout/select_dialog_singlechoice_material.xml",
//         "res/layout/share.xml",
//         "res/layout/support_simple_spinner_dropdown_item.xml",
         "res/layout-land/encode.xml",
//         "res/layout-land/share.xml",
         "res/layout-ldpi-v4/capture.xml",
//         "res/layout-v17/mr_media_route_list_item.xml",
//         "res/layout-v21/abc_screen_toolbar.xml",
         "res/menu/capture.xml",
         "res/menu/encode.xml",
//         "res/menu/history.xml",
         "res/raw/beep.ogg",
         "res/xml/preferences.xml",
   };
}
