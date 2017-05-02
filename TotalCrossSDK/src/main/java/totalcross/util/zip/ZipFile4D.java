package totalcross.util.zip;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.Stream;

public class ZipFile4D
{
   private Object nativeFile;
   private String name;
   private int size;
   
   public ZipFile4D(String name) throws IOException
   {
      this.name = name;
      File file = new File(name, File.READ_WRITE);
      createZipFile(file);
   }

   native private ZipFile createZipFile(File file) throws IOException;
   
   native public void close() throws IOException;

   native public ZipEntry[] entries();
   
   native public String getEntry(String name);

   native public Stream getEntryStream(String name) throws IOException;
   
   public String getName()
   {
      return name;
   }
   
   public int size()
   {
      return size;
   }

   protected void finalize()
   {
      try
      {
         if (nativeFile != null)
            this.close();
      }
      catch (totalcross.io.IOException e)
      {
      }
   }
}
