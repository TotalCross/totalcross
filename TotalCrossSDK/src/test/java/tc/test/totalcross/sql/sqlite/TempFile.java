package tc.test.totalcross.sql.sqlite;

import totalcross.io.File;

public class TempFile
{
  File f;
  public TempFile(File f)
  {
    this.f = f;
  }

  @Override
  public void finalize()
  {
    try {f.delete();} catch (Exception e) {}
  }
}
