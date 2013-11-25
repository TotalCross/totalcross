package tc.test.totalcross.sql.sqlite;

import totalcross.unit.*;

public class SQLiteTests extends TestCase
{
   public void testRun()
   {
      new BackupTest().testRun();
      new ConnectionTest().testRun();
      new ExtensionTest().testRun();
      new FetchSizeTest().testRun();
      new InsertQueryTest().testRun();
      new PrepStmtTest().testRun();
      new QueryTest().testRun();
      new RSMetaDataTest().testRun();
      new SQLiteJDBCLoaderTest().testRun();
      new StatementTest().testRun();
      new TransactionTest().testRun();
   }

}
