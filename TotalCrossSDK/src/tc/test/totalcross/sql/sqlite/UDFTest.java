package tc.test.totalcross.sql.sqlite;

import totalcross.sql.*;

import java.sql.SQLException;

import totalcross.db.sqlite.*;
import totalcross.unit.*;

public class UDFTest extends TestCase
{
    private static int    val        = 0;
    private static byte[] b1         = new byte[] { 2, 5, -4, 8, -1, 3, -5 };
    private static int    gotTrigger = 0;

    private Connection    conn;
    private Statement     stat;

    public void calling() {
       try {
        Function.create(conn, "f1", new Function() {
            public void xFunc() throws SQLException {
                val = 4;
            }
        });
        stat.executeQuery("select f1();").close();
        assertEquals(val, 4);
       } catch (Exception e) {fail(e);}
    }

    public void returning() {
       try {
        Function.create(conn, "f2", new Function() {
            public void xFunc() throws SQLException {
                result(4);
            }
        });
        ResultSet rs = stat.executeQuery("select f2();");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 4);
        rs.close();

        for (int i = 0; i < 20; i++) {
            rs = stat.executeQuery("select (f2() + " + i + ");");
            assertTrue(rs.next());
            assertEquals(rs.getInt(1), 4 + i);
            rs.close();
        }
       } catch (Exception e) {fail(e);}
    }

    public void accessArgs() {
       try {
        Function.create(conn, "f3", new Function() {
            public void xFunc() throws SQLException {
                result(value_int(0));
            }
        });
        for (int i = 0; i < 15; i++) {
            ResultSet rs = stat.executeQuery("select f3(" + i + ");");
            assertTrue(rs.next());
            assertEquals(rs.getInt(1), i);
            rs.close();
        }
       } catch (Exception e) {fail(e);}
    }

    public void multipleArgs() {
       try {
        Function.create(conn, "f4", new Function() {
            public void xFunc() throws SQLException {
                int ret = 0;
                for (int i = 0; i < args(); i++)
                    ret += value_int(i);
                result(ret);
            }
        });
        ResultSet rs = stat.executeQuery("select f4(2, 3, 9, -5);");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 9);
        rs.close();
        rs = stat.executeQuery("select f4(2);");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 2);
        rs.close();
        rs = stat.executeQuery("select f4(-3, -4, -5);");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), -12);
       } catch (Exception e) {fail(e);}
    }

    public void returnTypes() {
       try {
        Function.create(conn, "f5", new Function() {
            public void xFunc() throws SQLException {
                result("Hello World");
            }
        });
        ResultSet rs = stat.executeQuery("select f5();");
        assertTrue(rs.next());
        assertEquals(rs.getString(1), "Hello World");

        Function.create(conn, "f6", new Function() {
            public void xFunc() throws SQLException {
                result(Long.MAX_VALUE);
            }
        });
        rs.close();
        rs = stat.executeQuery("select f6();");
        assertTrue(rs.next());
        assertEquals(rs.getLong(1), Long.MAX_VALUE);

        Function.create(conn, "f7", new Function() {
            public void xFunc() throws SQLException {
                result(Double.MAX_VALUE);
            }
        });
        rs.close();
        rs = stat.executeQuery("select f7();");
        assertTrue(rs.next());
        assertEquals(rs.getDouble(1), Double.MAX_VALUE, 0.0001);

        Function.create(conn, "f8", new Function() {
            public void xFunc() throws SQLException {
                result(b1);
            }
        });
        rs.close();
        rs = stat.executeQuery("select f8();");
        assertTrue(rs.next());
        assertEquals(rs.getBytes(1), b1);
       } catch (Exception e) {fail(e);}
    }

    public void returnArgInt() {
       try {
        Function.create(conn, "farg_int", new Function() {
            public void xFunc() throws SQLException {
                result(value_int(0));
            }
        });
        PreparedStatement prep = conn.prepareStatement("select farg_int(?);");
        prep.setInt(1, Integer.MAX_VALUE);
        ResultSet rs = prep.executeQuery();
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), Integer.MAX_VALUE);
        prep.close();
       } catch (Exception e) {fail(e);}
    }

    public void returnArgLong() {
       try {
        Function.create(conn, "farg_long", new Function() {
            public void xFunc() throws SQLException {
                result(value_long(0));
            }
        });
        PreparedStatement prep = conn.prepareStatement("select farg_long(?);");
        prep.setLong(1, Long.MAX_VALUE);
        ResultSet rs = prep.executeQuery();
        assertTrue(rs.next());
        assertEquals(rs.getLong(1), Long.MAX_VALUE);
        prep.close();
       } catch (Exception e) {fail(e);}
    }

    public void returnArgDouble() {
       try {
        Function.create(conn, "farg_doub", new Function() {
            
            public void xFunc() throws SQLException {
                result(value_double(0));
            }
        });
        PreparedStatement prep = conn.prepareStatement("select farg_doub(?);");
        prep.setDouble(1, Double.MAX_VALUE);
        ResultSet rs = prep.executeQuery();
        assertTrue(rs.next());
        assertEquals(rs.getDouble(1), Double.MAX_VALUE, 0.0001);
        prep.close();
       } catch (Exception e) {fail(e);}
    }

    
    public void returnArgBlob() {
       try {
        Function.create(conn, "farg_blob", new Function() {
            
            public void xFunc() throws SQLException {
                result(value_blob(0));
            }
        });
        PreparedStatement prep = conn.prepareStatement("select farg_blob(?);");
        prep.setBytes(1, b1);
        ResultSet rs = prep.executeQuery();
        assertTrue(rs.next());
        assertEquals(rs.getBytes(1), b1);
        prep.close();
       } catch (Exception e) {fail(e);}
    }

    
    public void returnArgString() {
       try {
        Function.create(conn, "farg_str", new Function() {
            
            public void xFunc() throws SQLException {
                result(value_text(0));
            }
        });
        PreparedStatement prep = conn.prepareStatement("select farg_str(?);");
        prep.setString(1, "Hello");
        ResultSet rs = prep.executeQuery();
        assertTrue(rs.next());
        assertEquals(rs.getString(1), "Hello");
        prep.close();
       } catch (Exception e) {fail(e);}
    }

    public void customErr() {
       try {
        Function.create(conn, "f9", new Function() {
            
            public void xFunc() throws SQLException {
                throw new SQLException("myErr");
            }
        });
        stat.executeQuery("select f9();");
        fail("Should raise Exception");
       }
       catch (SQLException se)
       {
          // ok
       }
    }

    
    public void trigger() {
       try {
        Function.create(conn, "inform", new Function() {
            
            protected void xFunc() throws SQLException {
                gotTrigger = value_int(0);
            }
        });
        stat.executeUpdate("create table trigtest (c1);");
        stat.executeUpdate("create trigger trigt after insert on trigtest" + " begin select inform(new.c1); end;");
        stat.executeUpdate("insert into trigtest values (5);");
        assertEquals(gotTrigger, 5);
       } catch (Exception e) {fail(e);}
    }

    
    public void aggregate() {
       try {
        Function.create(conn, "mySum", new Function.Aggregate() {
            private int val = 0;

            
            protected void xStep() throws SQLException {
                for (int i = 0; i < args(); i++)
                    val += value_int(i);
            }

            
            protected void xFinal() throws SQLException{
                result(val);
            }
        });
        stat.executeUpdate("create table t (c1);");
        stat.executeUpdate("insert into t values (5);");
        stat.executeUpdate("insert into t values (3);");
        stat.executeUpdate("insert into t values (8);");
        stat.executeUpdate("insert into t values (2);");
        stat.executeUpdate("insert into t values (7);");
        ResultSet rs = stat.executeQuery("select mySum(c1), sum(c1) from t;");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), rs.getInt(2));
       } catch (Exception e) {fail(e);}
    }

    
    public void destroy() {
       try {
        Function.create(conn, "f1", new Function() {
            
            public void xFunc() throws SQLException {
                val = 9;
            }
        });
        stat.executeQuery("select f1();").close();
        assertEquals(val, 9);

        Function.destroy(conn, "f1");
        Function.destroy(conn, "f1");
       } catch (Exception e) {fail(e);}
    }

    
    public void manyfunctions() {
       try {
        Function.create(conn, "f1", new Function() {
            
            public void xFunc() throws SQLException {
                result(1);
            }
        });
        Function.create(conn, "f2", new Function() {
            
            public void xFunc() throws SQLException {
                result(2);
            }
        });
        Function.create(conn, "f3", new Function() {
            
            public void xFunc() throws SQLException {
                result(3);
            }
        });
        Function.create(conn, "f4", new Function() {
            
            public void xFunc() throws SQLException {
                result(4);
            }
        });
        Function.create(conn, "f5", new Function() {
            
            public void xFunc() throws SQLException {
                result(5);
            }
        });
        Function.create(conn, "f6", new Function() {
            
            public void xFunc() throws SQLException {
                result(6);
            }
        });
        Function.create(conn, "f7", new Function() {
            
            public void xFunc() throws SQLException {
                result(7);
            }
        });
        Function.create(conn, "f8", new Function() {
            
            public void xFunc() throws SQLException {
                result(8);
            }
        });
        Function.create(conn, "f9", new Function() {
            
            public void xFunc() throws SQLException {
                result(9);
            }
        });
        Function.create(conn, "f10", new Function() {
            
            public void xFunc() throws SQLException {
                result(10);
            }
        });
        Function.create(conn, "f11", new Function() {
            
            public void xFunc() throws SQLException {
                result(11);
            }
        });

        ResultSet rs = stat.executeQuery("select f1() + f2() + f3() + f4() + f5() + f6()"
                + " + f7() + f8() + f9() + f10() + f11();");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11);
        rs.close();
       } catch (Exception e) {fail(e);}
    }

    
    public void multipleThreads(){
       try {
        Function func = new Function() {
            int sum = 0;

            
            protected void xFunc() throws SQLException {
                try {
                    sum += value_int(1);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            
            public String toString() {
                return String.valueOf(sum);
            }
        };
        Function.create(conn, "func", func);
        stat.executeUpdate("create table foo (col integer);");
        stat.executeUpdate("create trigger foo_trigger after insert on foo begin"
                + " select func(new.rowid, new.col); end;");
        final int times = 10;
        Thread[] threads = new Thread[times];
        for (int tn = 0; tn < times; tn++) 
        {
            threads[tn] = new Thread("func thread " + tn) {
                public void run() {
                    try {
                        Statement s = conn.createStatement();
                        s.executeUpdate("insert into foo values (1);");
                        s.close();
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        for (int tn = 0; tn < times; tn++)
           threads[tn].start();
        
        boolean tryAgain = true;
        while (tryAgain)
           for (int tn = 0; tn < times; tn++)
              tryAgain |= threads[tn].isAlive();

        // check that all of the threads successfully executed
        ResultSet rs = stat.executeQuery("select sum(col) from foo;");
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), times);
        rs.close();

        // check that custom function was executed each time
        assertEquals(Integer.parseInt(func.toString()), times);
       } catch (Exception e) {fail(e);}
    }

    public void testRun()
    {
       try {
       conn = DriverManager.getConnection("jdbc:sqlite:");
       System.out.println(conn instanceof SQLiteConnection);
       stat = conn.createStatement();

       calling();
       returning();
       accessArgs();
       multipleArgs();
       returnTypes();
       returnArgInt();
       returnArgLong();
       returnArgDouble();
       returnArgBlob();
       returnArgString();
       customErr();
       trigger();
       aggregate();
       destroy();
       manyfunctions();
       multipleThreads();

       stat.close();
       conn.close();
       } catch (Exception e) {fail(e);}
       
    }
}
