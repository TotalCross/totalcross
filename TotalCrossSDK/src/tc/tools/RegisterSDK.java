package tc.tools;

import java.io.*;
import java.io.File;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import tc.tools.deployer.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.zip.*;

public final class RegisterSDK
{
   private static final String MAGIC = "T0T@LCR0$$";
   private static final int DATE_MASK = 0xBADCFE;
   
   private static class RegistrationFailedException extends RuntimeException 
   {
      public RegistrationFailedException(String s)
      {
         super(s);
      }
   }
   
   public RegisterSDK(String newkey) throws Exception
   {
      this(newkey, false);
   }
   
   private RegisterSDK(String key, boolean force) throws Exception
   {
      int today = Utils.getToday();
      String currentMac = Utils.getMAC();
      String currentUser = Settings.userName;
      String userhome = System.getProperty("user.home");
      File flicense = new File(userhome+"/tc_license.dat");
      if (force || !flicense.exists())
         updateLicense(key, currentMac, currentUser, userhome, today, flicense);
      checkLicense(key, currentMac, currentUser, userhome, today, flicense);
   }
   
   public static void main(String[] args)
   {
      if (args.length != 1)
         System.out.println("Format: tc.tools.RegisterSDK <activation key>");
      else
         try
         {
            new RegisterSDK(args[0], true);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
   }
   
   private byte[] doCrypto(String key, boolean encrypt, byte[] in) throws Exception 
   {
      char[] keystr = (key+"CAFEBABE").toCharArray();
      byte[] keybytes = new byte[16];
      for (int i = 0, n = keystr.length; i < n; i+=2)
         keybytes[i/2] = (byte)Integer.valueOf(""+keystr[i]+keystr[i+1],16).intValue();
      
      Key secretKey = new SecretKeySpec(keybytes, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);
      return cipher.doFinal(in);
   }

   private void checkLicense(String key, String currentMac, String currentUser, String userhome, int today, File flicense) throws Exception
   {
      // read license file
      InputStream in = new FileInputStream(flicense);
      byte[] fin = new byte[in.available()];
      in.read(fin);
      in.close();
      // use the key passed to launcher
      byte[] bin = doCrypto(key, false, fin);
      DataInputStream ds = new DataInputStream(new ByteArrayInputStream(bin));
      // skip trash
      int xdataLen = Math.abs(key.hashCode() % 1000);
      ds.skip(xdataLen);
      // checks if the magic equals
      boolean magicEquals = false;
      try
      {
         String magic     = ds.readUTF();
         magicEquals = magic.equals(MAGIC);
      } catch (Exception e) {}
      if (!magicEquals)
         throw new RegistrationFailedException("This license key does not correspond to the stored key!");
      // read the rest of stored data and compare with current values
      HashMap<String,String> kv = Utils.pipeSplit(ds.readUTF());
      String storedMac  = kv.get("mac");
      String storedUser = kv.get("user");
      String storedFolder = kv.get("userhome");
      int iexp = ds.readInt();
      boolean expired = today >= iexp;
      
      int diffM = storedMac.equals(currentMac) ? 0 : 1;
      int diffU = storedUser.equals(currentUser) ? 0 : 2;
      int diffF = storedFolder.equals(userhome) ? 0 : 4;
      int diff = diffM | diffU | diffF;
      
      if (diff != 0)
         throw new RegistrationFailedException("Invalid license file. Error #"+diff);
      if (expired)
         throw new RegistrationFailedException("This license is EXPIRED!");
      
      System.out.println("License expiration date: "+new Date(iexp));
   }

   private void updateLicense(String key, String currentMac, String currentUser, String userhome, int today, File flicense) throws Exception
   {
      // connect to the registration service and validate the key and mac.
      int expdate = getExpdateFromServer(key, currentMac, currentUser, userhome, today);
      if (expdate > 0)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
         DataOutputStream dos = new DataOutputStream(baos);
         // generate fake data to put before and at end of important data
         java.util.Random r = new java.util.Random(expdate);
         int xdataLen = Math.abs(key.hashCode() % 1000);
         byte[] xdata = new byte[xdataLen]; r.nextBytes(xdata);
         dos.write(xdata);
         // write important data
         dos.writeUTF(MAGIC);
         dos.writeUTF(Utils.pipeConcat("mac",currentMac,"user",currentUser,"userhome",userhome));
         dos.writeInt(expdate);
         // write fake data at end
         r.nextBytes(xdata);
         dos.write(xdata);
         dos.close();
         byte[] bytes = baos.toByteArray();
         byte[] cript = doCrypto(key, true, bytes);
         OutputStream out = new FileOutputStream(flicense);
         out.write(cript);
         out.close();
      }
      else
         switch (expdate)
         {
         }            
   }
   
   private int getExpdateFromServer(String key, String currentMac, String currentUser, String userhome, int today) throws Exception
   {
      URLConnection con = new URL("http://www.superwaba.net/SDKRegistrationService/SDKRegistration").openConnection();
      con.setRequestProperty("Request-Method", "POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);
      
      // zip data
      ByteArrayStream bas = new ByteArrayStream(256);
      ZLibStream zs = new ZLibStream(bas, ZLibStream.DEFLATE);
      DataStream ds = new DataStream(bas);
      ds.writeString(key);
      ds.writeString(Utils.pipeConcat("mac",currentMac,"user",currentUser,"userhome",userhome));
      ds.writeInt(today);
      zs.close();
      byte[] bytes = bas.toByteArray();
      
      // send data
      OutputStream os = con.getOutputStream();
      os.write(bytes);
      os.close();
      
      // get response - the expiration date or a negative value for error codes
      InputStream in = con.getInputStream();
      DataInputStream dis = new DataInputStream(in);
      int expdate = dis.readInt() ^ DATE_MASK;
      dis.close();
      in.close();
      
      return expdate;
   }
}
