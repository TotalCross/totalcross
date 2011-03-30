/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.deployer;

import totalcross.util.*;
import tc.tools.converter.bb.*;
import tc.tools.converter.bb.attribute.Code;
import tc.tools.converter.bb.attribute.LocalVariableTable;
import tc.tools.converter.bb.attribute.SourceFile;
import tc.tools.converter.bb.constant.NameAndType;
import tc.tools.converter.bb.constant.UTF8;
import tc.tools.converter.bb.constant.Class;

import java.io.*;
import java.util.zip.*;

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
   private static String targetDir, targetPackage, jarOut, fileName;
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
      
      createLauncher();  // 1
      jar2dex();         // 2
      updateResources(); // 3+4+5
      signAPK();         // 6
      
      System.out.println("... Files written to folder "+targetDir);
   }
   
   private void createLauncher() throws Exception
   {
      String jarIn = Utils.findPath(DeploySettings.etcDir+"launchers/android/Launcher.jar",false);
      if (jarIn == null)
         throw new DeployerException("File android/Launcher.jar not found!");
      jarOut = targetDir+fileName+".jar";
      targetPackage = "totalcross/app/"+fileName.toLowerCase();
      
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
      // java -classpath P:\TotalCrossSDK\etc\tools\android\dx.jar com.android.dx.command.Main --dex --output=classes.dex UIGadgets.jar
      String dxjar = Utils.findPath(DeploySettings.etcDir+"tools/android/dx.jar",false);
      if (dxjar == null)
         throw new DeployerException("File android/dx.jar not found!");
      String javaExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("java"));
      String cmd = javaExe+" -classpath "+dxjar+" com.android.dx.command.Main --dex --output=classes.dex "+DeploySettings.pathAddQuotes(new File(jarOut).getAbsolutePath()); // guich@tc124_3: use the absolute path for the file
      String out = Utils.exec(cmd, targetDir);
      if (!new File(targetDir+"classes.dex").exists())
         throw new DeployerException("An error occured when compiling the Java class with the Dalvik compiler. The command executed was: '"+cmd+"' at the folder '"+targetDir+"'\nThe output of the command is "+out);
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
         zos.putNextEntry(ze2=new ZipEntry(name));
         if (name.indexOf("tcfiles.zip") >= 0)
            insertTCFiles_zip(ze2, zos);
         else
         if (name.indexOf("resources.arsc") >= 0)
            insertResources_arsc(zis, zos);
         else
         if (name.indexOf("icon.png") >= 0)
            insertIcon_png(zos);
         else
         if (name.indexOf("AndroidManifest.xml") >= 0)
            insertAndroidManifest_xml(zis,zos);
         
         zos.closeEntry();
      }
      // add classes.dex to the output zip file
      zos.putNextEntry(new ZipEntry("classes.dex"));
      totalcross.io.File f = new totalcross.io.File(targetDir+"classes.dex",totalcross.io.File.READ_WRITE);
      int n;
      while ((n=f.readBytes(buf,0,buf.length)) > 0)
         zos.write(buf, 0, n);
      f.delete(); // delete original file
      
      zis.close();
      zos.close();      
   }
   
   private void signAPK() throws Exception
   {
      // jarsigner -keystore P:\TotalCrossSDK\etc\security\tcandroidkey.keystore -storepass @ndroid$w -keypass @ndroidsw UIGadgets.apk tcandroidkey
      String jarsignerExe = Utils.searchIn(DeploySettings.path, DeploySettings.appendDotExe("jarsigner"));
      String keystore = Utils.findPath(DeploySettings.etcDir+"security/tcandroidkey.keystore",false);
      if (keystore == null)
         throw new DeployerException("File security/tcandroidkey.keystore not found!");
      String apk = fileName+".apk";
      String cmd = jarsignerExe+" -keystore "+keystore+" -storepass @ndroid$w -keypass @ndroidsw "+DeploySettings.pathAddQuotes(apk)+" tcandroidkey";
      String out = Utils.exec(cmd, targetDir);
      if (out != null)
         throw new DeployerException("An error occured when signing the APK. The output is "+out);
   }

   private void insertIcon_png(ZipOutputStream zos) throws Exception
   {
      if (DeploySettings.bitmaps != null) DeploySettings.bitmaps.saveAndroidIcon(zos); // libraries don't have icons
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
   
   private void insertAndroidManifest_xml(ZipInputStream zis, ZipOutputStream zos) throws Exception
   {
      totalcross.io.ByteArrayStream bas = readInputStream(zis);
      bas.mark();
      totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
      
      String oldPackage  = "totalcross.app.stub";
      String oldVersion  = "1.0";
      String oldTitle    = "Stub";
      String oldActivity = ".Stub";
      
      String newPackage  = targetPackage.replace('/','.');
      String newVersion  = DeploySettings.appVersion != null ? DeploySettings.appVersion : oldVersion;
      String newTitle    = DeploySettings.appTitle;
      String newActivity = "."+fileName;
      
      int oldSize = bas.available();
      int difPackage  = (newPackage .length() - oldPackage .length()) * 2;
      int difVersion  = (newVersion .length() - oldVersion .length()) * 2;
      int difTitle    = (newTitle   .length() - oldTitle   .length()) * 2;
      int difActivity = (newActivity.length() - oldActivity.length()) * 2;
      int dif = difPackage + difVersion + difTitle + difActivity;
      
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
      // search and replace the value of versionCode="305419896" (0x12345678) with the current date and time in format YYMMDDHHMM (year with 2 digits only)
      byte[] versionCodeMark = {(byte)0x78,(byte)0x56,(byte)0x34,(byte)0x12};
      int ofs = Utils.indexOf(res, versionCodeMark, false);
      if (ofs == -1)
         throw new DeployerException("Error: could not find position for versionCode");
      int datetime = getDateTime();
      totalcross.io.ByteArrayStream dtbas = new totalcross.io.ByteArrayStream(res);
      dtbas.setPos(ofs);
      totalcross.io.DataStreamLE dsbas = new totalcross.io.DataStreamLE(dtbas);
      dsbas.writeInt(datetime);
      // if is full screen, search and replace Theme.Black.NoTitleBar by Theme.Black.NoTitleBar.Fullscreen
      if (DeploySettings.isFullScreenPlatform(totalcross.sys.Settings.ANDROID)) // guich@tc120_59
      {
         byte[] themeMark = {(byte)0x09,(byte)0x00,(byte)0x03,(byte)0x01};
         ofs = Utils.indexOf(res, themeMark, false);
         if (ofs == -1)
            throw new DeployerException("Error: could not find position for theme");
         res[ofs] = (byte)0xA; // set Fullscreen attribute
      }
      
      // now, change the names accordingly
      for (int i =0; i < len; i++)
      {
         String s = strings[i];
         if (s.equals(oldPackage))
            strings[i] = newPackage;
         else
         if (s.equals(oldVersion))
            strings[i] = newVersion;
         else
         if (s.equals(oldTitle))
            strings[i] = newTitle;
         else
         if (s.equals(oldActivity))
            strings[i] = newActivity;
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
   
   private static int getDateTime()
   {
      java.util.GregorianCalendar d = new java.util.GregorianCalendar();
      int year  = d.get(java.util.GregorianCalendar.YEAR) % 100;
      int month = d.get(java.util.GregorianCalendar.MONTH) + 1;
      int day   = d.get(java.util.GregorianCalendar.DATE);
      int hour  = d.get(java.util.GregorianCalendar.HOUR_OF_DAY);
      int minute= d.get(java.util.GregorianCalendar.MINUTE);
      return year * 100000000 + month * 1000000 + day * 10000 + hour * 100 + minute;
   }

   private void insertResources_arsc(ZipInputStream zis, ZipOutputStream zos) throws Exception
   {
      byte[] key = {'t',(byte)0,'o',(byte)0,'t',(byte)0,'a',(byte)0,'l',(byte)0,'c',(byte)0,'r',(byte)0,'o',(byte)0,'s',(byte)0,'s',(byte)0,'.',(byte)0,'a',(byte)0,'p',(byte)0,'p',(byte)0,'.',(byte)0,'s',(byte)0,'t',(byte)0,'u',(byte)0,'b',(byte)0};
      byte[] all = readInputStream(zis).toByteArray();
      int ofs = Utils.indexOf(all, key, false);
      if (ofs == -1)
         throw new DeployerException("Could not find position for totalcross.app.stub in arsc.");
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

   private void copyStream(InputStream in, OutputStream out) throws Exception
   {
      int n;
      while ((n = in.read(buf)) > 0)
         out.write(buf, 0, n);
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
      vLocals.addElement(DeploySettings.tczFileName);
      if (vGlobals.size() > 0)
         vLocals.addElements(vGlobals.toObjectArray());

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
         zos.putNextEntry(new ZipEntry(name));
         FileInputStream fis = new FileInputStream(pathname);
         copyStream(fis, zos);
         fis.close();
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
}
