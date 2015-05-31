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
   private PreparedStatement psAddText, psAddImage, psAddUser, psGetUser, psGetPosts, psGetPhoto;
   private HashMap<String,Image> hsPhotos = new HashMap<String,Image>(5);
   
   private FBDB()
   {
      try
      {
         util = new SQLiteUtil(Settings.appPath,"fb.db");
         Vm.debug(util.fullPath);
         createTables();
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   private void createTables() throws SQLException
   {
      Statement st = util.con().createStatement();
      st.execute("create table if not exists users (name varchar, photo blob, active int)");
      st.execute("create table if not exists posts (name varchar, text varchar, image blob, likes int, dt datetime)");
      st.close();
      psAddText  = util.prepareStatement("insert into posts (name, text, dt) values (?,?,?)");
      psAddImage = util.prepareStatement("insert into posts (name, image, dt) values (?,?,?)");
      psAddUser  = util.prepareStatement("insert into users values (?,?,?)");
      psGetUser  = util.prepareStatement("select * from users where active=?");
      psGetPosts = util.prepareStatement("select * from posts order by dt desc");
      psGetPhoto = util.prepareStatement("select photo from users where name=?");
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
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
      return l.toArray(new PostData[l.size()]);
   }
   
   public boolean addPost(String text) throws SQLException
   {
      psAddText.setString(1, FaceBookUI.defaultUser);
      psAddText.setString(2, text);
      psAddText.setTime(3, new Time());
      psAddText.executeUpdate();
      return true;
   }

   public boolean addPost(Image img) throws Exception
   {
      psAddImage.setString(1, FaceBookUI.defaultUser);
      psAddImage.setBytes(2, FBUtils.jpegBytes(img));
      psAddImage.setTime(3, new Time());
      psAddImage.executeUpdate();
      return true;
   }
   
   public boolean addUser(String name, Image img, boolean active) throws Exception
   {
      if (active) // dont allow duplicate active users
         try {util.con().createStatement().executeUpdate("delete from users where active = 1");} catch (Throwable t) {FBUtils.logException(t);}
      psAddUser.setString(1, name);
      psAddUser.setBytes(2, img == null ? null : FBUtils.jpegBytes(img));
      psAddUser.setInt(3, active ? 1 : 0);
      psAddUser.executeUpdate();
      if (active)
      {
         FaceBookUI.defaultPhoto = img;
         FaceBookUI.defaultUser = name;
      }
      return true;
   }
   
   public void loadActiveUser() throws Exception
   {
      psGetUser.setInt(1,1);
      ResultSet rs = psGetUser.executeQuery();
      if (rs.next())
      {
         FaceBookUI.defaultPhoto = createPhoto(rs.getBytes("photo"));
         FaceBookUI.defaultUser = rs.getString("name");
      }
      rs.close();
   }
   
   public ArrayList<PostData> getPosts() throws Exception
   {
      ArrayList<PostData> l = new ArrayList<PostData>(10);
      ResultSet rs = psGetPosts.executeQuery();
      while (rs.next())
         l.add(new PostData(rs.getString(1), rs.getString(2), createPhoto(rs.getBytes(3)), rs.getInt(4), rs.getTime(5)));
      rs.close();
      return l;
   }
   
   private Image createPhoto(byte[] bytes) throws Exception
   {
      return bytes == null ? null : new Image(bytes);
   }

   public Image getPhoto(String name) throws Exception
   {
      Image photo = hsPhotos.get(name);
      if (photo == null)
      {
         psGetPhoto.setString(1,name);
         ResultSet rs = psGetPhoto.executeQuery();
         if (rs.next() && (photo = createPhoto(rs.getBytes(1))) != null)
            hsPhotos.put(name, photo);
         rs.close();
      }
      return photo;
   }

   public void dropTables()
   {
      try
      {
         Statement st = util.con().createStatement();
         st.executeUpdate("drop table users");
         st.executeUpdate("drop table posts");
         st.close();
      }
      catch (Throwable t)
      {
         FBUtils.logException(t);
      }
   }
}
