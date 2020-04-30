// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.sys.testcases;

import litebase.*;
import totalcross.sys.Convert;
import totalcross.unit.*;
import totalcross.io.*;

/**
 * Tests the composed Index and composed primary key.
 */
public class TestComposedIndexAndPK extends TestCase
{
   /**
    * The connection with Litebase.
    */
   LitebaseConnection driver = AllTests.getInstance("Test");
   
   /**
    * The path where table files are stored.
    */
   String path = driver.getSourcePath();

   /**
    * The main method of the test.
    */
   public void testRun()
   {
      try
      {
         // Creates and populates a table with simple indices.
         if (driver.exists("person")) 
            driver.executeUpdate("DROP TABLE person");
         driver.execute("CREATE TABLE person(id int, name char(60) NOCASE, address char (100), cod short)");
         driver.execute("CREATE INDEX IDX ON person(namE)");
         driver.execute("CREATE INDEX IDX ON person(id)");
         driver.execute("CREATE INDEX IDX ON person(rowid)");
         driver.executeUpdate("INSERT INTO person VALUES (1, 'Renato Novais', 'Rio de Janeiro', 120)");
         driver.executeUpdate("INSERT INTO person VALUES (2, 'Indira', 'Jequi�', 562)");
         driver.executeUpdate("INSERT INTO person VALUES (3, 'Zenes e Jener', 'Cacul�', 120)");
         driver.executeUpdate("INSERT INTO person VALUES (4, 'Jener e Zenes', 'Cacul�', 500)");
         driver.executeUpdate("INSERT INTO person VALUES (5, 'Lucas/Felipe', 'VCQ/Guanambi', 1001)");
         driver.executeUpdate("INSERT INTO person VALUES (6, 'Danilo/Carol', 'SSA/CLE', 1002)");
         driver.executeUpdate("INSERT INTO person VALUES (7, 'Joao Pedro', 'Cacul�', 620)");
         driver.executeUpdate("INSERT INTO person VALUES (8, 'Neide', 'Salvador', 10)");
         driver.executeUpdate("INSERT INTO person VALUES (9, 'Caio', 'Salvador', 19)");
         driver.executeUpdate("INSERT INTO person VALUES (10, 'Z� de Jener', 'Cacul�', 1234)");
         driver.executeUpdate("INSERT INTO person VALUES (11, 'Marlene', 'Cacul�', 156)");
         driver.executeUpdate("INSERT INTO person VALUES (12, 'Nana', 'Cacul�', 2015)");
         
         // Tests that there are no composed index files.
         assertFalse(new File(path + "Test-person&1.idk", File.DONT_OPEN).exists());
   
         // Creates a composed index.
         driver.execute("create index idx on person (id, name)");
   
         driver.closeAll();
         assertTrue(new File(path + "Test-person&1.idk", File.DONT_OPEN).exists()); // The composed index files must exist.
         
         // Deletes a simple index file and opens the table again to re-create it.
         new File(path + "Test-person$1.idk", File.READ_WRITE).delete();
         (driver = AllTests.getInstance("Test")).executeQuery("select * from person").close();
         driver.closeAll();
         assertTrue(new File(path + "Test-person$1.idk", File.DONT_OPEN).exists()); // The simple index files must exist.
         
         // Deletes a composed index file and opens the table again to re-create it.
         new File(path + "Test-person&1.idk", File.READ_WRITE).delete();
         (driver = AllTests.getInstance("Test")).executeQuery("select * from person").close();
         assertTrue(new File(path + "Test-person&1.idk", File.DONT_OPEN).exists()); // The composed index files must exist.
         
         try // Tries to create the composed index again.
         {
            driver.execute("create index idx on person (id, name)");
            fail("1");
         }
         catch (AlreadyCreatedException exception) {}
   
         // Creates other composed index and checks if their files exist.
         driver.execute("create index idx on person (id, address)");
         driver.execute("create index idx on person (cod, name, rowid)");
         driver.execute("create index idx on person (cod, rowid)");
         driver.execute("create index idx on person (rowid, cod)");
         new File(path + "Test-person&2.idk", File.DONT_OPEN).exists();
         new File(path + "Test-person&3.idk", File.DONT_OPEN).exists();
         new File(path + "Test-person&4.idk", File.DONT_OPEN).exists();
         new File(path + "Test-person&5.idk", File.DONT_OPEN).exists();
   
         // Drops a composed index and checks that its file does not exist anymore.
         driver.executeUpdate("drop index id, name on person");
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         try // Tries to drop it again.
         {
            driver.executeUpdate("drop index id, name on person");
            fail("2");
         }
         catch (DriverException exception) {}
   
         driver.closeAll();
   
         // The other indices still exist.
         new File(path + "Test-person&2.idk", File.DONT_OPEN).exists();
         new File(path + "Test-person&3.idk", File.DONT_OPEN).exists();
   
         // Creates the dropped index again and checks that it exists again.
         (driver = AllTests.getInstance("Test")).execute("create index idx on person (id, name)");
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         // Closes the driver and drop the main table files.
         driver.closeAll();
         new File(Convert.appendPath(path, "Test-person.db"), File.READ_WRITE).delete();
         new File(Convert.appendPath(path, "Test-person.dbo"), File.READ_WRITE).delete();
   
         // Tests rowid in the composed primary key.
         (driver = AllTests.getInstance("Test")).execute("create table person(id int, primary key(rowid))");
         driver.executeUpdate("drop table person");
         driver.execute("create table person(id int, primary key(rowid, id))");
         driver.executeUpdate("drop table person");
         driver.execute("create table person(id int, primary key(id, rowid))");
         driver.executeUpdate("drop table person");
   
         try // The composed primary key can't have repeated fields.
         {
            driver.execute("create table person(id int, name char(10), address char(20), age int, years int, primary key (name, name))");
            fail("3");
         }
         catch (SQLParseException exception) {}
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         try // The composed primary key can't have repeated fields.
         {
            driver.execute("create table person(id int, name char(10), address char(20), age int, years int, primary key (rowid, rowid))");
            fail("4");
         }
         catch (SQLParseException exception) {}
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         try // The composed primary key can't have repeated fields.
         {
            driver.execute("create table person(id int, name char(10), address char(20), age int, years int, primary key (name, id, name))");
            fail("5");
         }
         catch (SQLParseException exception) {}
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         // Creates and populates a table with a composed primary key.
         driver.execute("create table person(id int, name char(10), address char(20), age int, years int, primary key (id, name))");
         driver.execute("create index idx on person(years)");
         driver.executeUpdate("Insert into person values (1,'Maria', 'Rio de Janeiro', 30, 1)");
         driver.executeUpdate("Insert into person values (3,'RLN', 'Salvador', 44, 3)");
         
         try // Repeated key.
         {
            driver.executeUpdate("Insert into person values (1,'Maria', 'Juazeiro', 50, 6)");
            fail("6");
         }
         catch (PrimaryKeyViolationException exception) {}
         try // Repeated key.
         {
            driver.executeUpdate("Insert into person values (3, 'RLN', 'Cacul�', 25, 10)");
            fail("7");
         }
         catch (PrimaryKeyViolationException exception) {}
         
         driver.executeUpdate("Insert into person values (1, 'Indira', 'Jequi�', 21, 14)");
         driver.executeUpdate("Insert into person values (3, 'RLN2', 'piracicaba', 14, 122)");
         
         try // Repeated key.
         {
            driver.executeUpdate("Insert into person values (1, 'Maria', 'jacar�', 26, 16)");
            fail("8");
         }
         catch (PrimaryKeyViolationException exception) {}
         try // Null in a key.
         {
            driver.executeUpdate("Insert into person values (1, null, 'jacar�', 26, 16)");
            fail("9");
         }
         catch (DriverException exception) {}
         try // Null in a key.
         {
            driver.executeUpdate("Insert into person values (null, 'Maria', 'jacar�', 26, 16)");
            fail("10");
         }
         catch (DriverException exception) {}
         
         driver.executeUpdate("Insert into person values (3, 'RLN1', 'da u p�', 81,166)");
   
         // Tests where clause with constants.
         ResultSet rs = driver.executeQuery("select * from person where (1 = 1)");
         assertEquals(5, rs.getRowCount());
         rs.close();
         assertEquals(5, (rs = driver.executeQuery("select * from person where not ('a' != 'a')")).getRowCount());
         rs.close();
         assertEquals(0, (rs = driver.executeQuery("select * from person where ('2010/11/23' != '2010/11/23')")).getRowCount());
         rs.close();
         assertEquals(0, (rs = driver.executeQuery("select * from person where not ('2010/11/23 17:28:11' = '2010/11/23 17:28:11')")).getRowCount());
         rs.close();
         assertEquals(0, (rs = driver.executeQuery("select * from person where (0.1d = 0.2d)")).getRowCount());
         rs.close();
         assertEquals(5, (rs = driver.executeQuery("select * from person where not (0 = 1)")).getRowCount());
         rs.close();
         assertEquals(5, (rs = driver.executeQuery("select * from person where ('2010/11/24' != '2010/11/23')")).getRowCount());
         rs.close();
         assertEquals(5, (rs = driver.executeQuery("select * from person where not ('2010/11/23 17:28:11' = '2010/11/23 17:28:12')")).getRowCount());
         rs.close();
         
         // Tests queries that uses the composed primary key.
         assertEquals(1, (rs = driver.executeQuery("select * from person where (1 = 1 and id = 3 and name = 'RLN')")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(1, (rs = driver.executeQuery("select * from person where (name = 'RLN' and id = 3 and 1 = 1)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         // Tests queries that do not use the composed primary key.
         assertEquals(1, (rs = driver.executeQuery("select * from person where (1 = 1 and id = 3 and address = 'Salvador')")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(1, (rs = driver.executeQuery("select * from person where (address = 'Salvador' and id = 3 and 1 = 1)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(3, (rs = driver.executeQuery("select * from person where (1 = 1 and id = 3)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(3, (rs = driver.executeQuery("select * from person where (id = 3 and 1 = 1)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(1, (rs = driver.executeQuery("select * from person where (1 = 1 and name = 'RLN')")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(1, (rs = driver.executeQuery("select * from person where (name = 'RLN' and 1 = 1)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         assertEquals(1, (rs = driver.executeQuery("select * from person where (address = 'Salvador' and age = 44)")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
         
         assertEquals(1, (rs = driver.executeQuery("select * from person where age < 45 and age > 43")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
         
         assertEquals(1, (rs = driver.executeQuery("select * from person where years >= 3 and years <= 3")).getRowCount());
         rs.next();
         assertEquals(3, rs.getInt("id"));
         assertEquals("RLN", rs.getString("name"));
         assertEquals(44, rs.getInt("age"));
         assertEquals("Salvador", rs.getString("address"));
         assertEquals(3, rs.getInt("years"));
         rs.close();
   
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists(); // Tests if the index files exist.
   
         // Drops the index and checks that the files do not exist anymore.
         driver.executeUpdate("ALTER TABLE person DROP primary key");
         new File(path + "Test-person&1.idk", File.DONT_OPEN).exists();
   
         driver.executeUpdate("Insert into person values (1,'Maria', 'jacar�', 26, 16)");
         
         try // Tries to drop again.
         {
            driver.executeUpdate("ALTER TABLE person DROP primary key");
            fail("11");
         }
         catch (DriverException exception) {}
         try // Tries to add. It will fail because of a repeated column name.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, id)");
            fail("12");
         }
         catch (SQLParseException exception) {}
         try // Tries to add. It will fail because of a repeated column name.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name, id)");
            fail("13");
         }
         catch (SQLParseException exception) {}
         try // Tries to add an index. It will fail because of a repeated column name.
         {
            driver.execute("create index idx on person(id, id)");
            fail("14");
         }
         catch (SQLParseException exception) {}
         try // Tries to add an index. It will fail because of a repeated column name.
         {
            driver.execute("create index idx on person(id, name, id)");
            fail("15");
         }
         catch (SQLParseException exception) {}
         try // Tries to add. It will fail because of a repeated key.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
            fail("16");
         }
         catch (PrimaryKeyViolationException exception) {} 
   
         // Tries again after closing the driver.
         driver.closeAll();
         driver = AllTests.getInstance("Test");
         try // Tries to add. It will fail because of a repeated key.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
            fail("17");
         }
         catch (PrimaryKeyViolationException exception) {}
   
         // Deletes the repeated key.
         assertEquals(1, driver.executeUpdate("delete from person where name = 'Maria' and age = 26"));
   
         // Now the composed primary key is added with success.
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
         assertTrue(new File(path + "Test-person&1.idk", File.DONT_OPEN).exists());
   
         // Tests composed PK violations for null inside them.
         driver.executeUpdate("ALTER TABLE person DROP primary key");
         driver.executeUpdate("Insert into person values (4, null, 'Rio de Janeiro', 29, 1)");
   
         try // Tries to add. It will fail because of a null in the key.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
            fail("18");
         }
         catch (DriverException exception) {}
   
         assertEquals(1, driver.executeUpdate("delete from person where id = 4 and age = 29"));
   
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
         driver.executeUpdate("ALTER TABLE person DROP primary key");
         driver.executeUpdate("Insert into person values (null, 'Juliana', 'Rio de Janeiro', 29, 1)");
   
         try // Tries to add. It will fail because of a null in the key.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
            fail("19");
         }
         catch (DriverException exception) {}
   
         assertEquals(1, driver.executeUpdate("delete from person where name = 'Juliana' and age = 29"));
   
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, name)");
   
         // Tests delete;
         assertEquals(2, (rs = driver.executeQuery("select * from person where (id = 1)")).getRowCount());
         rs.close();
         driver.executeUpdate("DELETE from person where id = 1 and name = 'Maria'");
         assertEquals(1, (rs = driver.executeQuery("select * from person where (id = 1)")).getRowCount());
         rs.close();
         driver.executeUpdate("DELETE from person where id = 1");
         assertEquals(0, (rs = driver.executeQuery("select * from person where (id = 1)")).getRowCount());
         rs.close();
   
         // Tests update.
         assertEquals(1, driver.executeUpdate("update person set name = 'juliana' where id = 3 and name = 'RLN'"));
         assertEquals(0, (rs = driver.executeQuery("select * from person where id = 3 and name = 'RLN'")).getRowCount());
         rs.close();
         assertEquals(1, (rs = driver.executeQuery("select * from person where id = 3 and name = 'juliana'")).getRowCount());
         rs.close();
   
         // Tests purge.
         assertEquals(5, driver.purge("person"));
         assertEquals(1, (rs = driver.executeQuery("select * from person where id = 3 and name = 'juliana'")).getRowCount());
         rs.close();
   
         try // Creates a duplicated composed PK.
         {
            assertEquals(1, driver.executeUpdate("update person set name = 'maria' where id = 3"));
            fail("20");
         }
         catch (PrimaryKeyViolationException exception) {}
   
         // Checks that no row was updated.
         assertEquals(1, (rs = driver.executeQuery("select * from person where id = 3 and name = 'maria'")).getRowCount());
         rs.close();
   
         // Tests rowid in the composed primary key.
         driver.executeUpdate("ALTER TABLE person DROP PRIMARY KEY");
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(rowid)");
         driver.executeUpdate("ALTER TABLE person DROP PRIMARY KEY");
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(rowid, id)");
         driver.executeUpdate("ALTER TABLE person DROP PRIMARY KEY");
         driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(id, rowid)");
         driver.executeUpdate("ALTER TABLE person DROP PRIMARY KEY");
         
         try // Repeated field in the primary key.
         {
            driver.executeUpdate("ALTER TABLE person ADD PRIMARY KEY(rowid, rowid)");
            fail("21");
         }
         catch (SQLParseException exception) {} 
         
         // Tests a table with too many columns and composed primary keys.
         if (driver.exists("ITENSPEDIDOS"))
            driver.executeUpdate("drop table ITENSPEDIDOS");
         
         driver.execute("create table ITENSPEDIDOS (NUMPED long, NUMITEM long, ICODCLI char(5), ICODVEND char(3), CODCONDPAG char(2), " 
+ "CODPROD char(14), DATPED datetime, PESOPADRLONG long, PESOPADRDEC short, PRCVENDALONG long, PRCVENDADEC short, PRCVENDIDOLONG long, " 
+ "PRCVENDIDODEC short, VALTOTITEMLONG long, VALTOTITEMDEC short, QTDPEDLONG long, QTDPEDDEC short, VALTOTPRCCUSTOLONG long, " 
+ "VALTOTPRCCUSTODEC short, PERCLUCROLONG long, PERCLUCRODEC short, PODTROC char(1), COMPL char(1), ENV char(1), REENV char(1), CODEMP  varchar(5), " 
+ "primary key (NUMPED, ICODCLI, ICODVEND, CODEMP))");
         
         driver.closeAll();
         (driver = AllTests.getInstance("Test")).executeQuery("select * from ITENSPEDIDOS").close();
         
         if (driver.exists("CDAIT"))
            driver.executeUpdate("drop table CDAIT");
         driver.execute("CREATE TABLE CDAIT (CDAITORGAO short NOT NULL, CDAITCOD char(11) nocase NOT NULL, CDAITDIGITO char(1) NOT NULL, " 
                                          + "CDAITDATAINFRACAO date, CDAITPLACA char(10) nocase, CDAITLOGRADOURO short, " 
                                          + "CDAITINFRACAO char(5), CDAITDESDOBRAMENTO short, CDAITAGENTE char(12) nocase, CDAITDATAENVIO date)");
         driver.execute("CREATE INDEX idx_CDAITAG ON CDAIT(CDAITORGAO, CDAITAGENTE)");
         driver.execute("CREATE INDEX idx_CDAITLOGRAD ON CDAIT(CDAITORGAO, CDAITLOGRADOURO)");
         driver.execute("CREATE INDEX idx_CDAIT ON CDAIT(CDAITORGAO, CDAITCOD, CDAITDIGITO)");
         driver.execute("CREATE INDEX idx_CDAITINFRACAO ON CDAIT(CDAITORGAO, CDAITINFRACAO, CDAITDESDOBRAMENTO, CDAITAGENTE)");
         driver.executeUpdate("insert into CDAIT values (7667, 'EHYYSSSSS�', 'Y', '2011/12/22', 'YYYY:::', 1, 'HX�YH', 1, 'YB�_HE', null)");
         driver.executeUpdate("insert into CDAIT values (7667, 'EHYYSSSSS�', 'Y', '2011/12/22', 'YYYY:::', 1, 'HX�YH', 1, 'YB�_HE', null)");
         assertEquals(2, (rs = driver.executeQuery("SELECT * from CDAIT WHERE CDAITORGAO = 7667 AND CDAITAGENTE = 'YB�_HE' AND CDAITCOD IS NOT NULL")).getRowCount());
         rs.close();
         driver.closeAll();
         
      } 
      catch (IOException exception)
      {
         exception.printStackTrace();
         fail("22");
         
      } 
   } 
}
