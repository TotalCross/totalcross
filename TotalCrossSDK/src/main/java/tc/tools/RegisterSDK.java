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

package tc.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.harawata.appdirs.AppDirsFactory;
import tc.tools.deployer.Utils;

public final class RegisterSDK {
  private static final String MAGIC = "T0T@LCR0$$";
  private static final int DATE_MASK = 0xBADCFE;

  private static final int INVALID_DATE = -1000;
  private static final int INVALID_REGISTRATION_KEY = -1001;
  private static final int EXPIRED_CONTRACT = -1002;
  private static final int NO_MORE_DEVELOPERS = -1003;
  private static final int CONTRACT_NOT_ACTIVE = -1004;
  private static final int INVALID_COMPANY = -1005;
  private static final int CONTRACT_DEACTIVATED = -1006;
  private static final int DEVICE_DISABLED = -1007;

  private String user, home, key;
  private int today;
  private File flicense;

  public RegisterSDK(String key, String user) throws Exception {
    this(key, false);
    storeActivationKey(key);
  }

  public RegisterSDK(String newkey) throws Exception {
    this(newkey, false);
  }

  private RegisterSDK(String key, boolean force) throws Exception {
    this.key = key;
    if (key.startsWith("54435354")) {
      return;
    }
    if (key.length() != 24) {
      throw new RegisterSDKException("The key is incorrect");
    }
    today = Utils.getToday();
    user = System.getProperty("user.name");
    home = System.getProperty("user.home");
    flicense = new File(home + "/" + key + ".key");
    if (force || !flicense.exists()) {
      updateLicense();
    }
    int ret = checkLicense();
    if (ret == EXPIRED || ret == OLD || ret == INVALID) // if expired or last activation occured after 8 hours
    {
      updateLicense();
      if (checkLicense() == EXPIRED) {
        throw new RegisterSDKException("The license is expired");
      }
    }
    if (giexp != -1) {
      System.out.println("Next SDK expiration date: " + showDate(giexp));
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Format: tc.tools.RegisterSDK <registration key>");
    } else {
      try {
        new RegisterSDK(args[0], true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private byte[] doCrypto(boolean encrypt, byte[] in) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    char[] keystr = (key + "CAFEBABE").toCharArray();
    byte[] keybytes = new byte[16];
    for (int i = 0, n = keystr.length; i < n; i += 2) {
      keybytes[i / 2] = (byte) Integer.valueOf("" + keystr[i] + keystr[i + 1], 16).intValue();
    }

    Key secretKey = new SecretKeySpec(keybytes, "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);
    return cipher.doFinal(in);
  }

  private static final int EXPIRED = 0;
  private static final int OLD = -1;
  private static final int VALID = -2;
  private static final int INVALID = -3;

  private int giexp = -1;

  private int checkLicense() throws Exception {
    try {
      // read license file
      InputStream in = new FileInputStream(flicense);
      byte[] fin = new byte[in.available()];
      in.read(fin);
      in.close();
      // use the key passed to launcher
      byte[] bin = doCrypto(false, fin);
      DataInputStream ds = new DataInputStream(new ByteArrayInputStream(bin));
      // skip trash
      int xdataLen = Math.abs(key.hashCode() % 1000);
      ds.skip(xdataLen);
      // checks if the magic equals
      boolean magicEquals = false;
      try {
        String magic = ds.readUTF();
        magicEquals = magic.equals(MAGIC);
      } catch (Exception e) {
      }
      if (!magicEquals) {
        System.out.println(
            "This license key does not correspond to the stored key! Requesting new license with current key...");
        return INVALID;
      }
      // read the rest of stored data and compare with current values
      String skv = ds.readUTF();
      HashMap<String, String> kv = Utils.pipeSplit(skv);
      String storedUser = kv.get("user");
      String storedFolder = kv.get("home");
      int iexp = ds.readInt();
      // check if user has changed the time
      long storedTimestamp = ds.readLong();
      long currentTimestamp = System.currentTimeMillis();
      long hoursElapsed = (currentTimestamp - storedTimestamp) / (60 * 60 * 1000);
      if (hoursElapsed < 0) {
        throw new RegisterSDKException("The computer's time is invalid.");
      }
      boolean expired = today >= iexp;

      int diffU = storedUser.equals(user) ? 0 : 1;
      int diffF = storedFolder.equals(home) ? 0 : 2;
      int diff = diffU | diffF;

      if (diff != 0) {
        System.out.println("The license parameters have changed (#" + diff + "). A new license will be requested");
      } else {
        giexp = iexp;
      }
      return diff != 0 ? INVALID : expired ? EXPIRED : hoursElapsed > 12 ? OLD : VALID;
    } catch (FileNotFoundException fnfe) {
      throw new RegisterSDKException("License not found! Check if this computer's internet is available.");
    } catch (javax.crypto.BadPaddingException bpe) {
      System.out.println(
          "This license key does not correspond to the stored key! Requesting new license with current key...");
      return INVALID;
    }
  }

  private void updateLicense() throws Exception {
    try {
      // connect to the registration service and validate the key and mac.
      int expdate = getExpdateFromServer();
      if (expdate <= 0) {
        switch (expdate) {
        case INVALID_DATE:
          throw new RegisterSDKException("Please update your computer's date.");
        case INVALID_REGISTRATION_KEY:
          throw new RegisterSDKException("The registration key is invalid.");
        case EXPIRED_CONTRACT:
          throw new RegisterSDKException("The contract is EXPIRED.");
        case NO_MORE_DEVELOPERS:
          throw new RegisterSDKException("The number of active developers has been reached.");
        case CONTRACT_NOT_ACTIVE:
          throw new RegisterSDKException(
              "This contract is not yet active. Please send email to renato@totalcross.com with your registration key.");
        case INVALID_COMPANY:
          throw new RegisterSDKException("Invalid company");
        case CONTRACT_DEACTIVATED:
          throw new RegisterSDKException(
              "The contract is suspended due to payment reasons. Please contact renato@totalcross.com.");
        case DEVICE_DISABLED:
          throw new RegisterSDKException("This device is disabled.");
        }
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        DataOutputStream ds = new DataOutputStream(baos);
        // generate fake data to put before and at end of important data
        java.util.Random r = new java.util.Random(expdate);
        int xdataLen = Math.abs(key.hashCode() % 1000);
        byte[] xdata = new byte[xdataLen];
        r.nextBytes(xdata);
        ds.write(xdata);
        // write important data
        ds.writeUTF(MAGIC);
        ds.writeUTF(Utils.pipeConcat("home", home, "user", user)); // MUST BE ALPHABETHICAL ORDER!
        ds.writeInt(expdate);
        ds.writeLong(System.currentTimeMillis());
        // write fake data at end
        r.nextBytes(xdata);
        ds.write(xdata);
        ds.close();
        byte[] bytes = baos.toByteArray();
        byte[] cript = doCrypto(true, bytes);
        OutputStream out = new FileOutputStream(flicense);
        out.write(cript);
        out.close();
      }
    } catch (RegisterSDKException rse) {
      throw rse;
    } catch (UnknownHostException uhe) {
      System.out.println(
          "INTERNET NOT AVAILABLE! TotalCross requires activation of the SDK. Once the date below is reached, the SDK will block.");
    } catch (Throwable e) {
      System.out.println("Exception during license update: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }
  }

  private int getExpdateFromServer() throws IOException {
    // zip data
    ByteArrayOutputStream bas = new ByteArrayOutputStream(256);
    Deflater dd = new Deflater(9, false);
    DeflaterOutputStream def = new DeflaterOutputStream(bas, dd);
    DataOutputStream ds = new DataOutputStream(def);
    ds.writeUTF(key);
    ds.writeUTF(Utils.pipeConcat("home", home, "user", user)); // MUST BE ALPHABETHICAL ORDER!
    ds.writeInt(today);
    def.finish();
    dd.end();
    byte[] bytes = bas.toByteArray();

    // prepare connection
    URLConnection con = new URL("http://www.superwaba.net/SDKRegistrationService/SDKRegistration").openConnection();
    con.setRequestProperty("Request-Method", "POST");
    con.setConnectTimeout(5000);
    con.setUseCaches(false);
    con.setDoOutput(true);
    con.setDoInput(true);

    // send data
    OutputStream os = con.getOutputStream();
    os.write(bytes);
    os.close();

    // get response - the expiration date or a negative value for error codes
    InputStream in = con.getInputStream();
    DataInputStream dis = new DataInputStream(in);
    int expdate = dis.readInt() ^ DATE_MASK;
    int expcontract = dis.readInt() ^ DATE_MASK;
    if (expcontract != 0) {
      System.out.println("INFO: the contract will expire at " + showDate(expcontract));
    }
    dis.close();
    in.close();

    return expdate;
  }

  private String showDate(int d) {
    int day = d % 100;
    int month = d / 100 % 100;
    int year = d / 10000;
    return day + " " + totalcross.util.Date.monthNames[month] + ", " + year;
  }

  public static String getStoredActivationKey() {
    String activationKey = null;
    try (FileInputStream fis = new FileInputStream(
        new File(AppDirsFactory.getInstance().getUserDataDir(null, null, "TotalCross"), "key.dat"))) {
      byte[] b = new byte[24];
      fis.read(b);
      activationKey = new String(b, "UTF-8");
    } catch (FileNotFoundException e) {
    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
    return activationKey;
  }

  private static void storeActivationKey(String activationKey) throws IOException {
    File parent = new File(AppDirsFactory.getInstance().getUserDataDir(null, null, "TotalCross"));
    if (!parent.exists()) {
      parent.mkdirs();
    }
    try (FileOutputStream fos = new FileOutputStream(new File(parent, "key.dat"))) {
      fos.write(activationKey.getBytes(Charset.forName("UTF-8")));
    }
  }
}
