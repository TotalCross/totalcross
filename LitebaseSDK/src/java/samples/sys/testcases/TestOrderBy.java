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
import totalcross.unit.*;

/**
 * Tests that order by always order the table in the correct order.
 */
public class TestOrderBy extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      
      // Recreates the table.
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      driver.execute("create table person (name char(10), age int, district char(10))");

      // Populates the table.
      driver.executeUpdate("insert into person values ('Juliana', 28, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Roberta', 26, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Guilherme', 37, 'Copacabana')");
      driver.executeUpdate("insert into person values ('Bruno', 23, 'Barra')");
      driver.executeUpdate("insert into person values ('C�lia', 57, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Renato', 35, 'Flamengo')");
      driver.executeUpdate("insert into person values ('Ronaldo', 61, 'Maracan�')");
      driver.executeUpdate("insert into person values ('F�bio', 23, 'Jacar�')");
      driver.executeUpdate("insert into person values ('Vin�cius', 28, 'Ipanema')");
      driver.executeUpdate("insert into person values ('Renato', 23, 'Cacul�')");

      driver.executeUpdate("insert into person values ('Renato', 23, 'Cacul�')");
      driver.executeUpdate("insert into person values ('Vin�cius', 28, 'Ipanema')");
      driver.executeUpdate("insert into person values ('F�bio', 23, 'Jacar�')");
      driver.executeUpdate("insert into person values ('Ronaldo', 61, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Renato', 35, 'Flamengo')");
      driver.executeUpdate("insert into person values ('C�lia', 57, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Bruno', 23, 'Barra')");
      driver.executeUpdate("insert into person values ('Guilherme', 37, 'Copacabana')");
      driver.executeUpdate("insert into person values ('Roberta', 26, 'Maracan�')");
      driver.executeUpdate("insert into person values ('Juliana', 28, 'Maracan�')");

      // The selects.
      ResultSet rs = driver.executeQuery("select * from person order by name desc, age asc, district");
      assertEquals(20, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      rs.close();

      assertEquals((rs = driver.executeQuery("select * from person order by name asc, district desc, age")).getRowCount(), 20);
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      rs.close();

      assertEquals((rs = driver.executeQuery("select * from person order by age, name asc, district desc")).getRowCount(), 20);
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      rs.close();

      assertEquals((rs = driver.executeQuery("select * from person order by age asc, district, name desc")).getRowCount(), 20);

      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      rs.close();
      
      assertEquals((rs = driver.executeQuery("select * from person order by district desc, name asc, age")).getRowCount(), 20);

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      rs.close();

      assertEquals((rs = driver.executeQuery("select * from person order by district asc, age, name desc")).getRowCount(), 20);

      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Bruno", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Barra", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Cacul�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Guilherme", rs.getString(1));
      assertEquals("37", rs.getString(2));
      assertEquals("Copacabana", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString(1));
      assertEquals("35", rs.getString(2));
      assertEquals("Flamengo", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Vin�cius", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Ipanema", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("F�bio", rs.getString(1));
      assertEquals("23", rs.getString(2));
      assertEquals("Jacar�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Roberta", rs.getString(1));
      assertEquals("26", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Juliana", rs.getString(1));
      assertEquals("28", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("C�lia", rs.getString(1));
      assertEquals("57", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));

      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      assertTrue(rs.next());
      assertEquals("Ronaldo", rs.getString(1));
      assertEquals("61", rs.getString(2));
      assertEquals("Maracan�", rs.getString(3));
      rs.close();
      
      // Wrong queries.
      try // Order by and group by must match.
      {
         driver.executeQuery("select * from person group by name order by district");
         fail("1");
      }
      catch (SQLParseException exception) {}
      try // Order by and group by must match.
      {
         driver.executeQuery("select * from person group by name order by name, district");
         fail("2");
      }
      catch (SQLParseException exception) {}
      try // All non-agreggate columns must be in the group by clause.
      {
         driver.executeQuery("select * from person group by name");
         fail("3");
      }
      catch (SQLParseException exception) {}
      try // All non-agreggate columns must be in the group by clause.
      {
         driver.executeQuery("select rowid, name, age, district from person group by name, age, district");
         fail("4");
      }
      catch (SQLParseException exception) {}
      
      // These ones must work.
      driver.executeQuery("select * from person group by name, age, district").close();
      driver.executeQuery("select rowid, name, age, district from person group by rowid, name, age, district").close();
      driver.executeQuery("select rowid, name, age, district from person group by rowid, name, age, district order by rowid, name, age, district").close();     
      driver.executeQuery("select * from person order by rowid").close();
      driver.closeAll();
   }
}