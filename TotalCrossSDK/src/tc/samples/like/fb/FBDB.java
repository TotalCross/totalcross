package tc.samples.like.fb;

import java.sql.SQLException;
import java.util.*;

import totalcross.db.sqlite.*;
import totalcross.io.*;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.*;
import totalcross.sys.Time;
import totalcross.ui.image.*;

public class FBDB
{
   public static FBDB db = new FBDB();
   private SQLiteUtil util;
   private PreparedStatement psText, psImage;
   
   private FBDB()
   {
      try
      {
         util = new SQLiteUtil(Settings.appPath,"fb.db");
         createTables();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private void createTables() throws SQLException
   {
      Statement st = util.con().createStatement();
      st.execute("create table if not exists users (name varchar, photo blob)");
      st.execute("create table if not exists news(name varchar, text varchar, image blob, likes int, dt datetime)");
      st.close();
      psText  = util.prepareStatement("insert into news (name, text, datetime) values (?,?,?)");
      psImage = util.prepareStatement("insert into news (name, image, datetime) values (?,?,?)");
   }
   
   public PostData[] getNews()
   {
      ArrayList<PostData> l = new ArrayList<PostData>(10);
      try
      {
         ResultSet rs = util.executeQuery("select * from news n, users u where n.name=u.name");
         while (rs.next())
         {
            String name = rs.getString("name");
            String text = rs.getString("text");
            Image icon = new Image(rs.getBytes("photo"));
            int likes = rs.getInt("likes");
            Time t = rs.getTime("dt");
            PostData n = new PostData(name, text, icon, likes, t);
            l.add(n);
         }
         rs.close();
      }
      catch (Exception e)
      {
         FBUtils.logException(e);
      }
      return l.toArray(new PostData[l.size()]);
   }
   
   public void addStatus(String text) throws SQLException
   {
      psText.setString(1, PostInput.defaultUser);
      psText.setString(2, text);
      psText.setTime(3, new Time());
      psText.executeUpdate();
   }

   public void addStatus(Image img) throws Exception
   {
      psImage.setString(1, PostInput.defaultUser);
      ByteArrayStream bas = new ByteArrayStream(20*1024);
      img.createJpg(bas, 85);
      psImage.setBytes(2, bas.toByteArray());
      psImage.setTime(3, new Time());
      psImage.executeUpdate();
   }
}
