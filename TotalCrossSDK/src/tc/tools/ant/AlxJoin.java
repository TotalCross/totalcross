package tc.tools.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class AlxJoin extends Task
{
   private String file;
   private File baseDir;
   private String name = "";
   private String description = "";
   private String version = "";
   private String vendor = "";
   private String copyright = "";
   
   private List fileSets = new ArrayList();
   
   public void setFile(String file)
   {
      this.file = file;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public void setDescription(String description)
   {
      this.description = description;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public void setVendor(String vendor)
   {
      this.vendor = vendor;
   }

   public void setCopyright(String copyright)
   {
      this.copyright = copyright;
   }

   public void addConfiguredFileSet(FileSet fileSet)
   {
      fileSets.add(fileSet);
   }
   
   public void execute() throws BuildException
   {
      if (file == null)
         throw new BuildException("Output file path not specified");
      
      try
      {
         File globalAlx = new File(file).getCanonicalFile();
         if (globalAlx.exists() && !globalAlx.delete())
            throw new BuildException("Could not delete existing output file: " + file);
         
         baseDir = globalAlx.getParentFile();
            
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document finalDoc = db.newDocument();
         Element finalDocRoot = finalDoc.createElement("loader");
         finalDocRoot.setAttribute("version", "1.0");
         
         Project project = getProject();
         
         Iterator it = fileSets.iterator();
         while (it.hasNext())
         {
            FileSet fileSet = (FileSet)it.next();
            File fileSetDir = fileSet.getDir(project);
            String[] files = fileSet.getDirectoryScanner(project).getIncludedFiles();
   
            for (int i = 0; i < files.length; i++)
            {
               File alx = new File(fileSetDir, files[i]).getCanonicalFile();
               System.out.println("Joining ALX: " + alx.getPath());
               
               Document doc = db.parse(alx);
               Element docRoot = (Element)doc.getElementsByTagName("loader").item(0);
               
               if (docRoot != null)
                  join(finalDocRoot, docRoot, new File(fileSetDir, files[i]).getParentFile());
            }
         }
         
         Element element = (Element)finalDocRoot.getElementsByTagName("application").item(0);
         if (element == null)
            throw new BuildException("\"application\" element not found in resulting ALX");
         else
         {
            setSubElementText(element, "name", name);
            setSubElementText(element, "description", description);
            setSubElementText(element, "version", version);
            setSubElementText(element, "vendor", vendor);
            setSubElementText(element, "copyright", copyright);
         }
         
         OutputFormat of = new OutputFormat("XML", "ISO-8859-1",true);
         of.setIndenting(true);
         of.setOmitXMLDeclaration(true);
         
         FileWriter writer = new FileWriter(file);
         XMLSerializer serializer = new XMLSerializer(writer, of);
         serializer.serialize(finalDocRoot);
         writer.close();
         
         System.out.println("Global ALX file written to: " + globalAlx.getPath());
      }
      catch (IOException ex)
      {
         throw new BuildException(ex);
      }
      catch (SAXException ex)
      {
         throw new BuildException(ex);
      }
      catch (ParserConfigurationException ex)
      {
         throw new BuildException(ex);
      }
   }
   
   private void join(Element dstRoot, Element srcRoot, File baseDocDir) throws IOException
   {
      Element srcElement = (Element)srcRoot.getElementsByTagName("application").item(0);
      if (srcElement == null)
         return; // nothing to join
      else
      {
         srcElement = (Element)dstRoot.getOwnerDocument().importNode(srcElement, true);
         updateDirectories(srcElement, baseDocDir, baseDir);
         copyAppVersionAttrToFileSets(srcElement, "_platformVersion");
         copyAppVersionAttrToFileSets(srcElement, "_blackberryVersion");
         
         Element dstElement = (Element)dstRoot.getElementsByTagName("application").item(0);
         if (dstElement == null)
            dstRoot.appendChild(srcElement); // there wasn't an application element in the final document yet
         else
         {
            String dstId = dstElement.getAttribute("id");
            String srcId = srcElement.getAttribute("id");
            if (!dstId.equals(srcId)) // different application IDs
               dstRoot.appendChild(srcElement);
            else // same application IDs
            {
               String dstPlatVer = dstElement.getAttribute("_platformVersion");
               String srcPlatVer = srcElement.getAttribute("_platformVersion");
               
               dstPlatVer = getJoinedAppVersion(dstPlatVer, srcPlatVer);
               if (dstPlatVer != null && !dstPlatVer.isEmpty())
                  dstElement.setAttribute("_platformVersion", dstPlatVer);
               
               String dstBBVer = dstElement.getAttribute("_blackberryVersion");
               String srcBBVer = srcElement.getAttribute("_blackberryVersion");
               
               dstBBVer = getJoinedAppVersion(dstBBVer, srcBBVer);
               if (dstBBVer != null)
                  dstElement.setAttribute("_blackberryVersion", dstBBVer);
               
               moveSubElements(srcElement, dstElement, "fileset");
            }
         }
      }
   }
   
   private void copyAppVersionAttrToFileSets(Element element, String attrName)
   {
      NodeList fileSets = element.getElementsByTagName("fileset");
      
      String attrVersion = element.getAttribute(attrName);
      if (attrVersion != null && !attrVersion.isEmpty())
      {
         for (int i = fileSets.getLength() - 1; i >= 0; i--)
         {
            Element fileSet = (Element)fileSets.item(i);
            String fileSetAttrVersion = fileSet.getAttribute(attrName);
            
            if (fileSetAttrVersion == null || fileSetAttrVersion.isEmpty() || fileSetAttrVersion.compareTo(attrVersion) > 0)
               fileSet.setAttribute(attrName, attrVersion);
         }
      }
   }
   
   private String getJoinedAppVersion(String appVer1, String appVer2)
   {
      if (appVer1 == null || appVer1.isEmpty())
         return appVer2;
      if (appVer2 == null || appVer2.isEmpty())
         return appVer1;
      
      String[] appVer1Splitted = getSplittedAppVersion(appVer1);
      String[] appVer2Splitted = getSplittedAppVersion(appVer2);
      
      String appVer1Lo = appVer1Splitted[0];
      String appVer1Hi = appVer1Splitted[1];
      
      String appVer2Lo = appVer2Splitted[0];
      String appVer2Hi = appVer2Splitted[1];
      
      String appVerLo = appVer1Lo.compareTo(appVer2Lo) < 0 ? appVer1Lo : appVer2Lo;
      String appVerHi = appVer1Hi.compareTo(appVer2Hi) > 0 ? appVer1Hi : appVer2Hi;
      
      String appVer = appVerLo + "," + appVerHi;
      appVer = (appVer.charAt(0) == 'a' ? '[' : '(') + appVer.substring(1);
      appVer = appVer.substring(0, appVer.length() - 1) + (appVer.charAt(appVer.length() - 1) == 'b' ? ']' : ')');
      
      return appVer;
   }
   
   private String[] getSplittedAppVersion(String appVer)
   {
      appVer = (appVer.charAt(0) == '[' ? 'a' : 'b') + appVer.substring(1);
      appVer = appVer.substring(0, appVer.length() - 1) + (appVer.charAt(appVer.length() - 1) == ']' ? 'b' : 'a');
      
      int idx = appVer.indexOf(',');
      return idx == -1 ? new String[] {appVer, appVer} : new String[] {appVer.substring(0, idx), appVer.substring(idx + 1)};
   }
   
   private void moveSubElements(Element src, Element dst, String subElementName)
   {
      NodeList subElements = src.getElementsByTagName(subElementName);
      for (int i = subElements.getLength() - 1; i >= 0; i--)
      {
         Element subElement = (Element)subElements.item(i);
         dst.appendChild(dst.getOwnerDocument().importNode(subElement, true));
      }
   }
   
   private void updateDirectories(Element element, File baseDocDir, File baseDir) throws IOException
   {
      NodeList fileSets = element.getElementsByTagName("fileset");
      for (int i = fileSets.getLength() - 1; i >= 0; i--)
      {
         Element fileSet = (Element)fileSets.item(i);
         NodeList directories = fileSet.getElementsByTagName("directory");
         for (int j = directories.getLength() - 1; i >= 0; i--)
         {
            Element directory = (Element)directories.item(j);
            directory.setTextContent(getRelativePath(new File(baseDocDir, directory.getTextContent()), baseDir, ""));
         }
      }
   }
   
   private void setSubElementText(Element element, String subElementName, String subElementText)
   {
      NodeList subElements = element.getElementsByTagName(subElementName);
      if (subElements.getLength() == 0)
      {
         Element subElement = element.getOwnerDocument().createElement(subElementName);
         subElement.setTextContent(subElementText);
         element.appendChild(subElement);
      }
      else
      {
         for (int i = subElements.getLength() - 1; i >= 0; i--)
            ((Element)subElements.item(i)).setTextContent(subElementText);
      }
   }
   
   private String getRelativePath(File dir, File base, String prefix) throws IOException
   {
      String dirPath = dir.getCanonicalPath();
      if (!dirPath.endsWith(File.separator))
         dirPath += File.separator;
      
      String basePath = base.getCanonicalPath();
      if (!basePath.endsWith(File.separator))
         basePath += File.separator;
      
      if (dirPath.startsWith(basePath)) // file is somewhere inside base directory
         return prefix + dirPath.substring(basePath.length());
      else
      {
         File parent = base.getParentFile();
         if (parent == null)
            return dirPath;
         else
            return getRelativePath(dir, parent, prefix + ".." + File.separator);
      }
   }
}
