package tc.tools;

import java.io.*;
import java.net.*;
import java.util.*;

import totalcross.sys.*;

public class Register
{
   String key;
   int expireDate; // yyyymmdd
   String mac;

   void load() throws Exception
   {
      try
      {
         FileInputStream f = new FileInputStream(getFilepath("tckey.dat"));
         DataInputStream d = new DataInputStream(f);
         key  = d.readUTF();
         expireDate = d.readInt();
         d.close();
         f.close();
      }
      catch (FileNotFoundException fnfe)
      {
      }
   }
   
   void save() throws Exception
   {
      FileOutputStream f = new FileOutputStream(getFilepath("tckey.dat"));
      DataOutputStream d = new DataOutputStream(f);
      d.writeUTF(key);
      d.writeInt(expireDate);
      d.close();
      f.close();
   }
   
   boolean activate() throws Exception
   {
      String addr = "http://www.superwaba.net/SDKRegistrationService/services/SDKRegistration";
      // connection setup
      URL url = new URL(addr);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Request-Method", "POST");
      connection.setDoInput(true);  
      connection.setDoOutput(true);  
      connection.connect();
      // send data
      OutputStream os = connection.getOutputStream();
      DataOutputStream dos = new DataOutputStream(os);
      dos.writeUTF(key);
      dos.writeInt(expireDate);
      dos.writeUTF(getMAC());
      dos.close();
      os.close();
      // read back
      byte[] buf = new byte[2048];
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
      InputStream is = connection.getInputStream();
      int r;
      while ((r = is.read(buf,0,buf.length)) > 0)
         baos.write(buf,0,r);
      connection.disconnect();
      // parse return
      byte[] bytes = baos.toByteArray();
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      DataInputStream dis = new DataInputStream(bis);
      String message = dis.readUTF();
      System.out.println("Server returned: "+message);
      expireDate = dis.readInt();
      save();
      return expireDate > 0;
   }
   
   public static class RegistrationFailedException extends RuntimeException
   {
   }
   
   public Register(String newkey)
   {
      this(newkey, false);
   }
   
   private Register(String newkey, boolean force)
   {
      try
      {
         load();
         int today = getYYYYMMDD();
         if (!force && today < expireDate)
            return;
         if (!newkey.equals(key)) // changed key?
         {
            key = newkey;
            save();
         }
         if (!activate())
            throw new RegistrationFailedException();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public static void main(String[] args)
   {
      if (args.length != 1)
         System.out.println("Format: tc.tools.Register <activation key>");
      else
         new Register(args[0], true);
   }
   
   private String getFilepath(String file)
   {
      String f = System.getenv("TOTALCROSS3_HOME");
      if (f == null)
         f = System.getenv("TOTALCROSS3");
      if (f == null)
         for (String s: System.getProperty("java.class.path").split(java.io.File.pathSeparator))
            if (s.endsWith("tc.jar"))
            {
               f = s.substring(0,s.replace('\\','/').lastIndexOf('/'));
               break;
            }
      if (f == null)
         f = System.getProperty("user.dir");
      f = f.replace('\\','/');
      return Convert.appendPath(f, file);
   }
   
   private int getYYYYMMDD()
   {
      Calendar cal = Calendar.getInstance();
      int aa = cal.get(Calendar.YEAR);
      int mm = cal.get(Calendar.MONTH)+1;
      int dd = cal.get(Calendar.DAY_OF_MONTH);
      return aa * 10000 + mm * 100 + dd;
   }

   private String getMAC()
   {
      try 
      {    
         InetAddress ip;
         ip = InetAddress.getLocalHost();
         System.out.println("Current IP address : " + ip.getHostAddress());
         NetworkInterface network = NetworkInterface.getByInetAddress(ip);
         byte[] mac = network.getHardwareAddress();
         System.out.print("Current MAC address : ");
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < mac.length; i++) 
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));     
         return sb.toString();    
      } 
      catch (Exception e) 
      {
         e.printStackTrace();    
      }
      return "";
   }
}
