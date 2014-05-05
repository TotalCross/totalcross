package tc.tools.deployer.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import totalcross.sys.Convert;
import de.schlichtherle.truezip.file.TFile;

/**
 * Creates a zip file compatible with the format accepted by Silverlight.<br>
 * Silverlight can only handle zip files whose contents' headers include compressed size and crc, but most zip
 * implementations for Java only include this information on the extended local header. This class uses a
 * {@link BlackHoleOutputStream} to fill the ZipEntry fields before writing to the actual output stream.<br>
 * Also includes an implementation of {@link FilenameFilter} to copy the contents of another zip file directly using
 * TrueZip.
 * 
 * @author Fabio Sobral
 * 
 */
public class SilverlightZip
{
   private ZipOutputStream zos;
   private BlackHoleOutputStream nos = new BlackHoleOutputStream();

   private Hashtable entries = new Hashtable();

   public SilverlightZip(File outputFile) throws FileNotFoundException
   {
      this.zos = new ZipOutputStream(new FileOutputStream(outputFile));
   }

   /**
    * Adds a new entry on the zip file.
    * 
    * @param name
    * @param content
    * @return true if successful, false if there's already a file with the given name on the zip file.
    * @throws IOException
    */
   public boolean putEntry(String name, byte[] content) throws IOException
   {
      if (entries.containsKey(name))
         return false;

      ZipEntry entry = new ZipEntry(name);

      final ZipOutputStream zipOut = new ZipOutputStream(nos);
      zipOut.setMethod(ZipOutputStream.DEFLATED);
      zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
      zipOut.putNextEntry(entry);
      zipOut.write(content);
      zipOut.closeEntry();
      zipOut.finish();
      zipOut.close();

      zos.putNextEntry(entry);
      zos.write(content);
      zos.closeEntry();

      entries.put(name, entry);
      return true;
   }

   public boolean putEntry(String name, File file) throws IOException
   {
      return putEntry(name, FileUtils.readFileToByteArray(file));
   }

   public void close() throws IOException
   {
      zos.close();
   }

   public CopyZipFilter getCopyZipFilter(String baseDir)
   {
      return new CopyZipFilter(baseDir);
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

      private String baseDir;

      CopyZipFilter(String baseDir)
      {
         this.baseDir = baseDir;
      }

      public boolean accept(File dir, String name)
      {
         TFile f = new TFile(dir, name);

         if (f.isDirectory())
            f.listFiles(new CopyZipFilter(baseDir == null ? f.getName() : Convert.appendPath(baseDir, f.getName())));
         else
         {
            if ("WMAppManifest.xml".equals(name))
               return true;

            try
            {
               baos.reset();
               f.output(baos);
               byte[] content = baos.toByteArray();

               SilverlightZip.this.putEntry(baseDir == null ? f.getName() : Convert.appendPath(baseDir, f.getName()), content);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
         return false;
      }
   }
}
