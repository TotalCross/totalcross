package tc.test.totalcross.sql.sqlite;

import totalcross.io.*;

public class TempFile
{
   File f;
   public TempFile(File f)
   {
      this.f = f;
   }
   
   public void finalize()
   {
      try {f.delete();} catch (Exception e) {}
   }
}
