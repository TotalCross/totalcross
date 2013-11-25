package tc.test.totalcross.sql.sqlite;

import java.sql.SQLException;

import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.unit.*;

public class InsertQueryTest extends TestCase
{
    interface ConnectionFactory
    {
        Connection getConnection() throws SQLException;

        void dispose() throws SQLException;
    }

    class IndependentConnectionFactory implements ConnectionFactory
    {
        public Connection getConnection() throws SQLException
        {
            return DriverManager.getConnection("jdbc:sqlite:tmp-sqlite.db");
        }

        public void dispose() throws SQLException
        {

        }

    }

    class SharedConnectionFactory implements ConnectionFactory
    {
        private Connection conn = null;

        public Connection getConnection() throws SQLException
        {
            if (conn == null)
                conn = DriverManager.getConnection("jdbc:sqlite:tmp-sqlite.db");
            return conn;
        }

        public void dispose() throws SQLException
        {
            if (conn != null)
                conn.close();
        }
    }

    static class BD
    {
        String fullId;
        String type;

        public BD(String fullId, String type)
        {
            this.fullId = fullId;
            this.type = type;
        }

        public String getFullId()
        {
            return fullId;
        }

        public void setFullId(String fullId)
        {
            this.fullId = fullId;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public static byte[] serializeBD(BD item)
        {
            return new byte[0];
        }

    }

    public void insertLockTestUsingSharedConnection() 
    {
        insertAndQuery(new SharedConnectionFactory());
    }

    public void insertLockTestUsingIndependentConnection() 
    {
        insertAndQuery(new IndependentConnectionFactory());
    }

    void insertAndQuery(ConnectionFactory factory) 
    {
        try
        {
            Statement st = factory.getConnection().createStatement();
            st
                    .executeUpdate("CREATE TABLE IF NOT EXISTS data (fid VARCHAR(255) PRIMARY KEY, type VARCHAR(64), data BLOB);");
            st
                    .executeUpdate("CREATE TABLE IF NOT EXISTS ResourcesTags (bd_fid VARCHAR(255), name VARCHAR(64), version INTEGER);");
            st.close();

            factory.getConnection().setAutoCommit(false);

            // Object Serialization
            PreparedStatement statAddBD = factory.getConnection().prepareStatement(
                    "INSERT OR REPLACE INTO data values (?, ?, ?)");
            PreparedStatement statDelRT = factory.getConnection().prepareStatement(
                    "DELETE FROM ResourcesTags WHERE bd_fid = ?");
            PreparedStatement statAddRT = factory.getConnection().prepareStatement(
                    "INSERT INTO ResourcesTags values (?, ?, ?)");

            for (int i = 0; i < 10; i++)
            {
                BD item = new BD(Integer.toHexString(i), Integer.toString(i));

                // SQLite database insertion
                statAddBD.setString(1, item.getFullId());
                statAddBD.setString(2, item.getType());
                statAddBD.setBytes(3, BD.serializeBD(item));
                statAddBD.execute();

                // Then, its resources tags
                statDelRT.setString(1, item.getFullId());
                statDelRT.execute();

                statAddRT.setString(1, item.getFullId());

                for (int j = 0; j < 2; j++)
                {
                    statAddRT.setString(2, "1");
                    statAddRT.setLong(3, 1L);
                    statAddRT.execute();
                }
            }

            factory.getConnection().setAutoCommit(true);

            statAddBD.close();
            statDelRT.close();
            statAddRT.close();

            //
            PreparedStatement stat;
            String query = "SELECT COUNT(fid) FROM data";

            stat = factory.getConnection().prepareStatement(query);
            ResultSet rs = stat.executeQuery();

            rs.next();
            long result = rs.getLong(1);
            output(""+result);
            //System.out.println("count = " + result);

            rs.close();
            stat.close();
        }
        catch (Exception e) {fail(e);}
        finally
        {
            try {factory.dispose();} catch (Exception e) {fail(e);}
        }
    }

    public void reproduceDatabaseLocked()
    {
       try
       {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:tmp-sqlite.db");
        Connection conn2 = DriverManager.getConnection("jdbc:sqlite:tmp-sqlite.db");
        Statement stat = conn.createStatement();
        Statement stat2 = conn2.createStatement();

        conn.setAutoCommit(false);

        stat.executeUpdate("drop table if exists sample");
        stat.executeUpdate("create table sample(id, name)");
        stat.executeUpdate("insert into sample values(1, 'leo')");

        ResultSet rs = stat2.executeQuery("select count(*) from sample");
        rs.next();

        conn.commit(); // causes "database is locked" (SQLITE_BUSY)
       } catch (Exception e) {fail(e);}
    }

   public void testRun()
   {
      insertLockTestUsingSharedConnection();
      insertLockTestUsingIndependentConnection();
      reproduceDatabaseLocked();
   }
}
