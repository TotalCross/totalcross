/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package samples.sys.joins;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;

public class LitebaseTestJoins extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   LitebaseConnection conn;

   private void joinTest()
   {
      conn = LitebaseConnection.getInstance(Settings.applicationId);
      initJoinTables();
   }

   private void createJoinTestTables()
   {
      String sql;

      log("Creating tables");
      sql = "create table ComponentType (id long primary key, name char(20))";
      conn.execute(sql);

      sql = "create table ComponentDefinition (id long primary key, name char(20))";
      conn.execute(sql);

      sql = "create table Panel (id long primary key, name char(20))";
      conn.execute(sql);

      sql = "create table Component (id long primary key, typeid long, componentdefinitionid long, panelid long, name char(20))";
      conn.execute(sql);

      sql = "create index typeid on component(typeid)";
      conn.execute(sql);

      sql = "create index compdefid on component(componentdefinitionid)";
      conn.execute(sql);

      sql = "create index panelid on component(panelid)";
      conn.execute(sql);

      int rowFactor = 4;
      int count = rowFactor;
      
      pb.max = count; 
      pb.suffix = " of "+pb.max;
      PreparedStatement ps = conn.prepareStatement("insert into ComponentType (id,name) values (?,?)");
      conn.setRowInc("ComponentType", count);
      for (int i = 1; i <= count; i++)
      {
         pb.setValue(i);
         ps.setLong(0, i);
         ps.setString(1, "Type " + Convert.toString(i));
         ps.executeUpdate();
      }
      conn.setRowInc("ComponentType", -1);

      ps = conn.prepareStatement("insert into ComponentDefinition (id,name) values (?,?)");
      pb.max = count = rowFactor * 10;
      pb.suffix = " of "+pb.max;
      conn.setRowInc("ComponentDefinition", count);
      for (int i = 1; i <= count; i++)
      {
         pb.setValue(i);
         ps.setLong(0, i);
         ps.setString(1, "Definition " + Convert.toString(i));
         ps.executeUpdate();
      }
      conn.setRowInc("ComponentDefinition", -1);

      ps = conn.prepareStatement("insert into Panel (id,name) values (?,?)");
      pb.max = count = rowFactor * 100;
      pb.suffix = " of "+pb.max;
      conn.setRowInc("Panel", count);
      for (int i = 1; i <= count; i++)
      {
         pb.setValue(i);
         ps.setLong(0, i);
         ps.setString(1, "Panel " + Convert.toString(i));
         ps.executeUpdate();
      }
      conn.setRowInc("Panel", -1);

      ps = conn.prepareStatement("insert into Component (id,typeid,componentdefinitionid,panelid,name) values (?,?,?,?,?)");
      pb.max = count = rowFactor * 1000;
      pb.suffix = " of "+pb.max;
      conn.setRowInc("Component", count);
      for (int i = 1; i <= count; i++)
      {
         pb.setValue(i);
         ps.setLong(0, i);
         ps.setLong(1, (i / 1000) + 1);
         ps.setLong(2, (i / 100) + 1);
         ps.setLong(3, (i / 10) + 1);
         ps.setString(4, "Component " + Convert.toString(i));
         ps.executeUpdate();
      }
      conn.setRowInc("Component", -1);
   }

   private void initJoinTables()
   {
      if (!conn.exists("Component"))
         createJoinTestTables();

      int count = 4000;
      String sql;

      int id = (count / 3);
      id = 1;
      sql = "select Component.name from Component where id = " + id;
      executeTestQuery(sql, "Primer"); // initial query always takes longer
      
      
      
      // ATUALIZAR O CONTADOR SE ALTERAR A QUANTIDADE DOS TESTES ABAIXO!!!!
      pb.max = 24 * 24 + 1;
      pb.suffix = " of "+pb.max;
      
      

      String baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, Panel, ComponentDefinition, ComponentType where ";
      String d = "Component.componentdefinitionid = ComponentDefinition.id";
      String i = "Component.id = " + id;
      String p = "Component.panelid = Panel.id";
      String t = "Component.typeid = ComponentType.id";
      int tempo = 0;
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("1 Component, Panel, ComponentDefinition, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, Panel, ComponentType, ComponentDefinition where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("2 Component, Panel, ComponentType, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentDefinition, Panel, ComponentType where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("3 Component, ComponentDefinition, Panel, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentDefinition, ComponentType, Panel where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("4 Component, ComponentDefinition, ComponentType, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentType, Panel, ComponentDefinition where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("5 Component, ComponentType, Panel, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentType, ComponentDefinition, Panel where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("6 Component, ComponentType, ComponentDefinition, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, Component, ComponentDefinition, ComponentType where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("7 Panel, Component, ComponentDefinition, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, Component, ComponentType, ComponentDefinition where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("8 Panel, Component, ComponentType, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentDefinition, Component, ComponentType where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("9 Panel, ComponentDefinition, Component, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentDefinition, ComponentType, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("10 Panel, ComponentDefinition, ComponentType, Component " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentType, Component, ComponentDefinition where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("11 Panel, ComponentType, Component, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentType, ComponentDefinition, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("12 Panel, ComponentType, ComponentDefinition, Component " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Component, Panel, ComponentType where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("13 ComponentDefinition, Component, Panel, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Component, ComponentType, Panel where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("14 ComponentDefinition, Component, ComponentType, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Panel, Component, ComponentType where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("15 ComponentDefinition, Panel, Component, ComponentType " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Panel, ComponentType, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("16 ComponentDefinition, Panel, ComponentType, Component " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, ComponentType, Component, Panel where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("17 ComponentDefinition, ComponentType, Component, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, ComponentType, Panel, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("C18 omponentDefinition, ComponentType, Panel, Component " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Component, Panel, ComponentDefinition where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("19 ComponentType, Component, Panel, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Component, ComponentDefinition, Panel  where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("20 ComponentType, Component, ComponentDefinition, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Panel, Component, ComponentDefinition  where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("21 ComponentType, Panel, Component, ComponentDefinition " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Panel, ComponentDefinition, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("22 ComponentType, Panel, ComponentDefinition, Component " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, ComponentDefinition, Component, Panel where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("23 ComponentType, ComponentDefinition, Component, Panel " + tempo);

      tempo = 0;
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, ComponentDefinition, Panel, Component where ";
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t, "DIPT");
      tempo += executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p, "DITP");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t, "DPIT");
      tempo += executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i, "DPTI");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p, "DTIP");
      tempo += executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i, "DTPI");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t, "IDPT");
      tempo += executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p, "IDTP");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t, "IPDT");
      tempo += executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d, "IPTD");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p, "ITDP");
      tempo += executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d, "ITPD");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t, "PDIT");
      tempo += executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i, "PDTI");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t, "PIDT");
      tempo += executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d, "PITD");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i, "PTDI");
      tempo += executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d, "PTID");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p, "TDIP");
      tempo += executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i, "TDPI");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p, "TIDP");
      tempo += executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d, "TIPD");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i, "TPDI");
      tempo += executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d, "TPID");
      log("24 ComponentType, ComponentDefinition, Panel, Component " + tempo);
      log("Done");
   }

   private void log(String text)
   {
      lb.add(text);
      lb.selectLast();
      Vm.debug(text);
   }

   int contador;
   private int executeTestQuery(String sql, String label)
   {
      ResultSet rs;
      long start = Vm.getTimeStamp();
      rs = conn.executeQuery(sql);
      long end = Vm.getTimeStamp();
      int rowCount = rs.getRowCount();
      String str = (end - start) + " --> " + label + "   (" + rowCount + ")";
      log(str);

      if (rowCount > 1)
      {
         log("------------------------------------------------------------------------");
         log(sql);
         log("------------------------------------------------------------------------");
         String componentName;
         String componentTypeName;
         String componentDefName;
         String panelName;

         for (int i = 0; i < rowCount; i++)
         {
            rs.next();
            componentName = rs.getString(1);
            componentTypeName = rs.getString(2);
            componentDefName = rs.getString(3);
            panelName = rs.getString(4);

            log("         " + i + " - " + componentName + " | " + componentTypeName + " | " + componentDefName + " | " + panelName);
         }
         log("=========================================================================");
      }
      pb.setValue(++contador);
      return (int) (end - start);
   }

   ListBox lb;
   ProgressBar pb;

   public void initUI()
   {
      add(pb = new ProgressBar(), LEFT,TOP,FILL,PREFERRED);
      add(lb = new ListBox(),LEFT,AFTER,FILL,FILL);
      lb.enableHorizontalScroll();
      joinTest();
   }
}
