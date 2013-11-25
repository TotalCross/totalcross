package tc.test.totalcross.sql.sqlite;

import java.sql.SQLException;

import totalcross.io.*;
import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.unit.*;

public class SQLiteConnectionPoolDataSourceTest {

    @Test
    public void connectionTest () throws SQLException {
        ConnectionPoolDataSource ds = new SQLiteConnectionPoolDataSource();

        PooledConnection pooledConn = ds.getPooledConnection();

        Connection handle = pooledConn.getConnection();
        assertFalse(handle.isClosed());
        assertTrue(handle.createStatement().execute("select 1"));

        Connection handle2 = pooledConn.getConnection();
        assertTrue(handle.isClosed());
        try {
            handle.createStatement().execute("select 1");
            fail();
        }
        catch (SQLException e) {
            assertEquals("Connection is closed", e.getMessage());
        }

        assertTrue(handle2.createStatement().execute("select 1"));
        handle2.close();

        handle = pooledConn.getConnection();
        assertTrue(handle.createStatement().execute("select 1"));

        pooledConn.close();
        assertTrue(handle.isClosed());
    }
}
