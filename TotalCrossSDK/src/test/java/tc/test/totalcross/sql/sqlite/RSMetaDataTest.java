package tc.test.totalcross.sql.sqlite;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.unit.*;

public class RSMetaDataTest extends TestCase
{
   private Connection conn;
   private Statement stat;
   private ResultSetMetaData meta;

   public void connect() throws Exception
   {
      conn = DriverManager.getConnection("jdbc:sqlite:");
      stat = conn.createStatement();
      Vm.gc(); stat.execute("DROP TABLE IF EXISTS people");
      stat.executeUpdate("create table People (pid integer primary key autoincrement, " + " firstname string(255), surname string(25,5), dob date);");
      stat.executeUpdate("insert into people values (null, 'Mohandas', 'Gandhi', " + " '1869-10-02');");
      meta = stat.executeQuery("select pid, firstname, surname from people;").getMetaData();
   }

   public void close() throws Exception
   {
      stat.executeUpdate("drop table people;");
      stat.close();
      conn.close();
   }

   public void catalogName()
   {
      try
      {
         assertEquals(meta.getCatalogName(1), "People");
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void columns()
   {
      try
      {
         assertEquals(meta.getColumnCount(), 3);
         assertEquals(meta.getColumnName(1), "pid");
         assertEquals(meta.getColumnName(2), "firstname");
         assertEquals(meta.getColumnName(3), "surname");
         assertEquals(meta.getColumnType(1), Types.INTEGER);
         assertEquals(meta.getColumnType(2), Types.VARCHAR);
         assertEquals(meta.getColumnType(3), Types.VARCHAR);
         assertTrue(meta.isAutoIncrement(1));
         assertFalse(meta.isAutoIncrement(2));
         assertFalse(meta.isAutoIncrement(3));
         assertEquals(meta.isNullable(1), ResultSetMetaData.columnNoNulls);
         assertEquals(meta.isNullable(2), ResultSetMetaData.columnNullable);
         assertEquals(meta.isNullable(3), ResultSetMetaData.columnNullable);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void columnTypes()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS tbl");
         stat.executeUpdate("create table tbl (col1 INT, col2 INTEGER, col3 TINYINT, " + "col4 SMALLINT, col5 MEDIUMINT, col6 BIGINT, col7 UNSIGNED BIG INT, "
               + "col8 INT2, col9 INT8, col10 CHARACTER(20), col11 VARCHAR(255), " + "col12 VARYING CHARACTER(255), col13 NCHAR(55), "
               + "col14 NATIVE CHARACTER(70), col15 NVARCHAR(100), col16 TEXT, " + "col17 CLOB, col18 BLOB, col19 REAL, col20 DOUBLE, "
               + "col21 DOUBLE PRECISION, col22 FLOAT, col23 NUMERIC, " + "col24 DECIMAL(10,5), col25 BOOLEAN, col26 DATE, col27 DATETIME)");
         // insert empty data into table otherwise getColumnType returns null
         stat.executeUpdate("insert into tbl values (1, 2, 3, 4, 5, 6, 7, 8, 9," + "'c', 'varchar', 'varying', 'n', 'n','nvarchar', 'text', 'clob',"
               + "null, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 0, 12345, 123456)");
         meta = stat.executeQuery(
               "select col1, col2, col3, col4, col5, col6, col7, col8, col9, " + "col10, col11, col12, col13, col14, col15, col16, col17, col18, "
                     + "col19, col20, col21, col22, col23, col24, col25, col26, col27, " + "cast(col1 as boolean) from tbl").getMetaData();

         assertEquals(Types.INTEGER, meta.getColumnType(1));
         assertEquals(Types.INTEGER, meta.getColumnType(2));
         assertEquals(Types.TINYINT, meta.getColumnType(3));
         assertEquals(Types.SMALLINT, meta.getColumnType(4));
         assertEquals(Types.INTEGER, meta.getColumnType(5));
         assertEquals(Types.BIGINT, meta.getColumnType(6));
         assertEquals(Types.BIGINT, meta.getColumnType(7));
         assertEquals(Types.SMALLINT, meta.getColumnType(8));
         assertEquals(Types.BIGINT, meta.getColumnType(9));

         assertEquals(Types.CHAR, meta.getColumnType(10));
         assertEquals(Types.VARCHAR, meta.getColumnType(11));
         assertEquals(Types.VARCHAR, meta.getColumnType(12));
         assertEquals(Types.CHAR, meta.getColumnType(13));
         assertEquals(Types.CHAR, meta.getColumnType(14));
         assertEquals(Types.VARCHAR, meta.getColumnType(15));
         assertEquals(Types.VARCHAR, meta.getColumnType(16));
         assertEquals(Types.CLOB, meta.getColumnType(17));

         assertEquals(Types.BLOB, meta.getColumnType(18));

         assertEquals(Types.REAL, meta.getColumnType(19));
         assertEquals(Types.DOUBLE, meta.getColumnType(20));
         assertEquals(Types.DOUBLE, meta.getColumnType(21));
         assertEquals(Types.FLOAT, meta.getColumnType(22));
         assertEquals(Types.NUMERIC, meta.getColumnType(23));
         assertEquals(Types.DECIMAL, meta.getColumnType(24));
         assertEquals(Types.BOOLEAN, meta.getColumnType(25));

         assertEquals(Types.DATE, meta.getColumnType(26));
         assertEquals(Types.DATE, meta.getColumnType(27));

         assertEquals(Types.BOOLEAN, meta.getColumnType(28));

         assertEquals(10, meta.getPrecision(24));
         assertEquals(5, meta.getScale(24));
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void differentRS()
   {
      try
      {
         meta = stat.executeQuery("select * from people;").getMetaData();
         assertEquals(meta.getColumnCount(), 4);
         assertEquals(meta.getColumnName(1), "pid");
         assertEquals(meta.getColumnName(2), "firstname");
         assertEquals(meta.getColumnName(3), "surname");
         assertEquals(meta.getColumnName(4), "dob");
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void nullable()
   {
      try
      {
         meta = stat.executeQuery("select null;").getMetaData();
         assertEquals(meta.isNullable(1), ResultSetMetaData.columnNullable);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void badCatalogIndex()
   {
      try
      {
         meta.getCatalogName(4);
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void badColumnIndex()
   {
      try
      {
         meta.getColumnName(4);
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void scale()
   {
      try
      {
         assertEquals(0, meta.getScale(2));
         assertEquals(5, meta.getScale(3));
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testRun()
   {
      try
      {
         connect();

         catalogName();
         columns();
         scale();
         columnTypes();
         differentRS();
         nullable();
         badCatalogIndex();
         badColumnIndex();

         close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
}
