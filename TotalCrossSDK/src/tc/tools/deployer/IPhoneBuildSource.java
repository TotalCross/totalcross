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

import totalcross.crypto.digest.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.tools.bzip2.*;
import org.apache.tools.tar.*;
import org.vafer.jdeb.ar.*;

/**
 * IPhoneBuildSource: build iPhone repositories for Installer & Cydia.
 *
 * i.e. java -classpath output/classes/ tc.tools.deployer.IPhoneBuildSource /home/fdie/workspace/TotalCrossSDK/vm/iphone \
 *          /home/fdie/workspace/TotalCrossSDK/output/samples /home/fdie/workspace/LitebaseSDK/output/samples
 */

public class IPhoneBuildSource
{
   private final static char slash = File.separatorChar;

   private static boolean cydiaNewApp(File debianFile, String reposBasedir, PrintStream ps) throws Exception
   {
      Utils.println("process: "+debianFile.getAbsolutePath());

      if (reposBasedir.endsWith("/"))
         reposBasedir = reposBasedir.substring(0, reposBasedir.length()-1);
      
      String name = debianFile.getName();
      if (name.endsWith(".deb"))
         name = name.substring(0, name.length()-4);
            
      MD5Digest md5 = new MD5Digest();
      InputStream is = new FileInputStream(debianFile);
      byte[] data = new byte[is.available()];
      int count = is.read(data);
      if (count > 0)
      {
         // compute and write md5 checksum
         md5.update(data, 0, count);
         ps.print("MD5sum: ");
         for (int i = 0; i < md5.getDigestLength(); i++)
         {
            String hex = Integer.toHexString(md5.getDigest()[i] & 0xFF);
            if (hex.length() < 2)
               ps.print('0');
            ps.print(hex);
         }
         ps.println();
         ps.println("Size: " + debianFile.length());

         // copy debian file
         String path = "/packages/" + name + ".deb";
         FileOutputStream os = new FileOutputStream(reposBasedir + path);
         ps.println("Filename: ." + path);
         os.write(data, 0, count);
         os.close();
      }
      is.close();

      // browse deb file archive in AR format, see "http://en.wikipedia.org/wiki/Deb_(file_format)"
      is = new FileInputStream(debianFile);
      final ArInputStream ais = new ArInputStream(is);
      ArEntry entry;
      while ((entry = ais.getNextEntry()) != null)
      {         
         if (entry.getName().equals("data.tar.bz2")) // real content
         {
            InputStream eis = entry.getInputStream();
            eis.skip(2); // skip 2 bytes header "Bz"
            TarInputStream tis = new TarInputStream(new CBZip2InputStream(eis));
            TarEntry te = null;
            while ((te = tis.getNextEntry()) != null && !te.getName().endsWith("/icon.png"))
               ;
            if (te != null)
            {
               // process package Icon even it doesn't seem to be supported yet, icons are assigned based on the "Section" header.
               byte[] content = new byte[tis.available()];
               tis.read(content);               
               String path = "/icons/" + name + ".png";
               FileOutputStream os = new FileOutputStream(reposBasedir + path);
               ps.println("Icon: ." + path);
               os.write(content);
               os.close();
            }
            tis.close();
         }
         else if (entry.getName().equals("control.tar.gz")) // debian control files (information file "control") 
         {
            TarInputStream tis = new TarInputStream(new GZIPInputStream(entry.getInputStream()));
            TarEntry te = null;
            while ((te = tis.getNextEntry()) != null && !te.getName().equals("control"))
               ;
            if (te != null)
            {
               // process "control" file
               byte[] content = new byte[tis.available()];
               tis.read(content);
               StringTokenizer tk = new StringTokenizer(new String(content), "\r\n");
               while (tk.hasMoreTokens())
                  ps.println(tk.nextToken());
            }
            tis.close();
         }
      }
      ps.println();
      ais.close();
      is.close();
      return true;
   }
   
   private static void cydiaAddFiles(String path, String reposBasedir, PrintStream ps)
   {
      File f = new File(path);
      if (!f.exists()) return;

      if (f.isFile())
      {
         String file = f.getName();
         if (file.endsWith(".deb"))
         {
            try
            {
               cydiaNewApp(f, reposBasedir, ps);
            } 
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      else // is dir
      {         
         if (path.length() > 0 && !path.endsWith("\\") && !path.endsWith("/"))
            path += slash;
         String[] files=f.list();
         for(int i=0;i<files.length;i++)
            cydiaAddFiles(path + files[i] + slash, reposBasedir, ps);
      }
   }

   private static void buildCydia(String paths[], String targetDir)
   {
      try
      {
         if (targetDir == null)
            targetDir = new File("install/ios").isDirectory() ? "install/ios/" : "./";
         else
         if (!targetDir.endsWith("/") && !targetDir.endsWith("\\"))
            targetDir += "/";

         new File(targetDir).mkdirs();
         new File(targetDir + "packages").mkdirs();
         new File(targetDir + "icons").mkdirs();

         PrintStream packages = new PrintStream(new GZIPOutputStream(new FileOutputStream(targetDir + "Packages.gz")));

         for (int i = 0; i < paths.length; i++)
            if (!paths[i].startsWith("-"))
               cydiaAddFiles(paths[i], targetDir, packages);

         packages.close();
         totalcross.io.File.deleteDir(targetDir + "packages");
         totalcross.io.File.deleteDir(targetDir + "icons");
      }
      catch (Exception e) {e.printStackTrace();}
   }  

   public static void main(String args[])
   {
      Utils.println("Building repository for iphone 2+"); buildCydia(args, null);
   }
}
