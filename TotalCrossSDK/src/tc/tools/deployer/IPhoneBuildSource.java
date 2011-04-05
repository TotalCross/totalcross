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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.vafer.jdeb.ar.ArEntry;
import org.vafer.jdeb.ar.ArInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import totalcross.crypto.digest.MD5Digest;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * IPhoneBuildSource: build iPhone repositories for Installer & Cydia.
 *
 * i.e. java -classpath output/classes/ tc.tools.deployer.IPhoneBuildSource /home/fdie/workspace/TotalCrossSDK/vm/iphone \
 *          /home/fdie/workspace/TotalCrossSDK/output/samples /home/fdie/workspace/LitebaseSDK/output/samples
 */

public class IPhoneBuildSource
{
   private final static char slash = File.separatorChar;

   private static boolean installerNewApp(Node node, File f)
   {
      Utils.println("process: "+f.getAbsolutePath());
      try
      {
         DOMParser dp = new DOMParser();
         dp.setEntityResolver(new ER());
         dp.parse(new InputSource(new FileReader(f)));
         Document doc = dp.getDocument();
         if (doc == null || doc.getDocumentElement() == null)
         {
            Utils.println("no XML content");
            return false;
         }
         Element elt = doc.getDocumentElement();
         if (elt != null)
         {
            NodeList nl = elt.getChildNodes();
            if (nl != null)
            {
               for (int i = 0; i < nl.getLength(); i++)
               {
                  Node n = nl.item(i);
                  if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("dict"))
                  {
                     node.appendChild(node.getOwnerDocument().adoptNode(n));
                     return true;
                  }
               }
            }
         }
      } catch (Exception e) {e.printStackTrace();}
      return false;
   }

   private static void installerBrowseApps(Node node, String path)
   {
      File f = new File(path);
      if (!f.exists()) return;

      String file = f.getName();

      if (f.isFile())
      {
         if (file.endsWith(".plist") && !file.endsWith("Info.plist"))
            installerNewApp(node, f);
      }
      else // is dir
      {
         if (path.length() > 0 && !path.endsWith("\\") && !path.endsWith("/"))
            path += slash;
         String[] files=f.list();
         for(int i=0;i<files.length;i++)
            installerBrowseApps(node, path + files[i] + slash);
      }
   }

   public static void buildInstaller(String paths[], String targetDir) // guich@tc100b5_9: added targetDir
   {
      try
      {
         if (targetDir == null)
            targetDir = new File("install/iphone1").isDirectory() ? "install/iphone1/" : "";
         else
         if (!targetDir.endsWith("/") && !targetDir.endsWith("\\"))
            targetDir += "/";
         String reposFile = "rep.xml";

         String template =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
            "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"+
            "<plist version=\"1.0\">\n"+
            "<dict>\n"+
            "  <key>info</key>\n"+
            "  <dict>\n"+
            "    <key>name</key>\n"+
            "    <string>TotalCross SDK</string>\n"+
            "    <key>maintainer</key>\n"+
            "    <string>SuperWaba Ltda</string>\n"+
            "    <key>contact</key>\n"+
            "    <string>noreply@superwaba.com.br</string>\n"+
            "    <key>url</key>\n"+
            "    <string>http://www.superwaba.com.br/</string>\n"+
            "    <key>category</key>\n"+
            "    <string>TotalCross</string>\n"+
            "  </dict>\n"+
            "  <key>packages</key>\n"+
            "  <array>\n"+
            "  <insert_here/>\n"+
            "  </array>\n"+
            "</dict>\n"+
            "</plist>\n";

         DOMParser dp = new DOMParser();
         dp.setEntityResolver(new ER());
         dp.parse(new InputSource(new ByteArrayInputStream(template.getBytes())));
         Document doc = dp.getDocument();
         if (doc == null)
         {
            Utils.println("Null doc");
            return;
         }
         NodeList nl = doc.getElementsByTagName("insert_here");
         if (nl.getLength() != 1)
         {
            Utils.println("can't find insert point");
            return;
         }

         Node insert_node = nl.item(0);
         Node anchor_node = insert_node.getParentNode();
         anchor_node.removeChild(insert_node);

         for (int i = 0; i < paths.length; i++)
            if (!paths[i].startsWith("-"))
               installerBrowseApps(anchor_node, paths[i]);

         // write out the TotalCross sources file
         Utils.println("...writing "+reposFile);

         FileOutputStream fos = new FileOutputStream(targetDir+reposFile);
         OutputFormat of = new OutputFormat(doc);
         XMLSerializer xmlWriter = new XMLSerializer(fos, of);
         xmlWriter.serialize(doc);
         fos.close();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }
   
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
         }
      }
      ps.println();
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
            targetDir = new File("install/iphone2+").isDirectory() ? "install/iphone2+/" : "./";
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
      if (args.length == 0)
      {
         System.out.println("IPhoneBuildSource: just pass a list of paths where iPhone zip packages should be located; pass -1 to build to iphone1 and -2 to build iphone2+");
         System.exit(-1);
      }
      boolean i1=false,i2=false;
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];
         if (arg.equals("-v"))
            DeploySettings.quiet = false;
         else
         if (arg.equals("-1"))
            i1 = true;
         else
         if (arg.equals("-2"))
            i2 = true;
      }

      if (!i1 && !i2) // none set? set them all
         i1 = i2 = true;
      
      if (i1)
         {Utils.println("Building repository for iphone 1"); buildInstaller(args, null);}
      if (i2)
         {Utils.println("Building repository for iphone 2"); buildCydia(args, null);}
   }

   static protected class ER implements EntityResolver
   {
      public ER()
      {
      }
      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, java.io.IOException
      {
         return publicId.equals("-//Apple Computer//DTD PLIST 1.0//EN") ?
               new InputSource(ClassLoader.getSystemResourceAsStream("tc/tools/deployer/PropertyList-1.0.dtd")) : new InputSource(systemId);
      }
   }
}
