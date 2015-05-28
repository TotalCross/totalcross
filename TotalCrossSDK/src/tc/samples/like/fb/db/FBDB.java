package tc.samples.like.fb.db;

import java.sql.SQLException;
import java.util.*;
import tc.samples.like.fb.*;
import tc.samples.like.fb.ui.*;

import totalcross.db.sqlite.*;
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
   private PreparedStatement psText, psImage, psUser;
   
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
      st.execute("create table if not exists users (name varchar, photo blob, int current)");
      st.execute("create table if not exists posts (name varchar, text varchar, image blob, likes int, dt datetime)");
      st.close();
      psText  = util.prepareStatement("insert into posts (name, text, datetime) values (?,?,?)");
      psImage = util.prepareStatement("insert into posts (name, image, datetime) values (?,?,?)");
      psUser  = util.prepareStatement("insert into users values (?,?,?)");
   }
   
   public PostData[] getNews()
   {
      ArrayList<PostData> l = new ArrayList<PostData>(10);
      try
      {
         ResultSet rs = util.executeQuery("select * from posts n, users u where n.name=u.name");
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
   
   public void addPost(String text) throws SQLException
   {
      psText.setString(1, PostInput.defaultUser);
      psText.setString(2, text);
      psText.setTime(3, new Time());
      psText.executeUpdate();
   }

   public void addPost(Image img) throws Exception
   {
      psImage.setString(1, PostInput.defaultUser);
      psImage.setBytes(2, FBUtils.jpegBytes(img));
      psImage.setTime(3, new Time());
      psImage.executeUpdate();
   }
   
   public void addUser(String name, Image img, boolean current) throws Exception
   {
      psUser.setString(1, name);
      psUser.setBytes(2, FBUtils.jpegBytes(img));
      psUser.setInt(3, current ? 1 : 0);
      psUser.executeUpdate();
   }
}
