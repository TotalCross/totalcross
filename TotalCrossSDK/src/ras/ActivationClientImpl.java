/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/


package ras;

import ras.comm.*;
import ras.comm.v1.*;

import totalcross.crypto.*;
import totalcross.crypto.cipher.*;
import totalcross.crypto.digest.*;
import totalcross.crypto.signature.*;
import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.util.*;

final class ActivationClientImpl extends ActivationClient
{
   private AESKey aesKey;
   private AESCipher aesCipher;
   private RSAPublicKey rsaPubKey;
   private PKCS1Signature pkcs1Signature;

   private String activatedProductId;
   private String activationCode;
   private String platform;
   private String deviceId;
   private String deviceHash;

   private final String PDBFILE_TCRAS_REQUEST = "tcreq.TCvm.RASD"; // the pdb file containing the activation request
   private final String PDBFILE_TCRAS_SUCCESS = "tcsuc.TCvm.RASD"; // the pdb file containing the activation success
   private final String RESFILE_TCKEY = "tckey.bin"; // the resource file containing the registration key
   private static Logger logger;
   static
   {
      Stream s = Logger.DEBUG_CONSOLE;
      int atr = Logger.WARNING | Logger.SEVERE;
      if (Settings.ANDROID.equals(Settings.platform))
         try
         {
            String dir = Settings.appPath+"/logs";
            try {new File(dir).createDir();} catch (Exception ee) {}
            File f = new File(dir+"/ras.log",File.CREATE);
            int len = f.getSize();
            if (len > 0) // if we had already any trouble, log everything
               atr = Logger.ALL;
            f.setPos(len);
            s = f;
         }
         catch (Exception e) {} // use default Stream
      logger = Logger.getLogger("ras.ActivationClient", atr, s);
   }

   public ActivationClientImpl()
   {
      try
      {
         aesKey = new AESKey(getAESKeyData());
         aesCipher = new AESCipher();

         rsaPubKey = new RSAPublicKey(getSignerRSAKeyE(), getSignerRSAKeyN());
         pkcs1Signature = new PKCS1Signature(new MD5Digest());
      }
      catch (CryptoException e)
      {
      }

      byte[] htDump = Vm.getFile("tcparms.bin");
      if (htDump != null)
      {
         Hashtable htVmParams = new Hashtable(new String(htDump));
         String field = null;
         if ((field = htVmParams.getString("activationServerURI")) != null)
            Settings.activationServerURI = field;
         if ((field = htVmParams.getString("activationServerNamespace")) != null)
            Settings.activationServerNamespace = field;
         if ((field = htVmParams.getString("applicationId")) != null)
            Settings.applicationId = field;
         if ((field = htVmParams.getString("appVersion")) != null)
            Settings.appVersion = field;
      }
   }

   public boolean isLitebaseAllowed()
   {
      return litebaseAllowed;
   }

   public String getProductId()
   {
      return activatedProductId;
   }

   public String getActivationCode()
   {
      return activationCode;
   }

   public String getPlatform()
   {
      return platform;
   }

   public String getDeviceId()
   {
      return deviceId;
   }

   public String getDeviceHash()
   {
      return deviceHash;
   }

   public boolean isActivatedSilent()
   {
      try
      {
         return isActivated();
      }
      catch (ActivationException e)
      {
         return false;
      }
   }

   public boolean isActivated() throws ActivationException
   {
      activatedProductId = null;
      activationCode = null;
      platform = null;
      deviceId = null;
      deviceHash = null;
      litebaseAllowed = false;

      Hashtable deviceInfo = Utils.getDeviceInfo();
      Hashtable productInfo = Utils.getProductInfo();

      try
      {
         //flsobral@tc125: fill the field Settings.activationId
         byte[] key = readKey();
         if (key == null)
            throw new Exception("This application was not signed with a registration key");
         if (!isValidKey(key))
            throw new Exception("The registration key is not valid");
         litebaseAllowed = key[2] == 'L' && key[3] == 'B';
         Settings.activationId = Convert.bytesToHexString(generateActivationCode(key, Convert.hexStringToBytes((String) deviceInfo.get("HASH"))));

         ActivationSuccess success = (ActivationSuccess) readPacket(PDBFILE_TCRAS_SUCCESS);
         ActivationRequest request = success.getRequest();
         Hashtable actDeviceInfo = request.getDeviceInfo();
         Hashtable actProductInfo = request.getProductInfo();

         logger.info("Verifying authencity");
         if (rsaPubKey == null || pkcs1Signature == null)
            throw new Exception("Cannot verify information due to a security error");
         pkcs1Signature.reset(Signature.OPERATION_VERIFY, rsaPubKey);
         if (!success.verify(pkcs1Signature))
            throw new Exception("The activation information is not authentic");

         String devicePlatform = deviceInfo.getString("PLATFORM");
         String activationPlatform = actDeviceInfo.getString("PLATFORM");

         logger.info("Verifying device information");
         if (!devicePlatform.equals(activationPlatform))
            throw new Exception("The device platform does not match with the registration.");

         String deviceIMEI = (String) deviceInfo.get("IMEI");
         String deviceSerial = (String) deviceInfo.get("SERIAL");
         String activationIMEI = (String) actDeviceInfo.get("IMEI");
         String activationSerial = (String) actDeviceInfo.get("SERIAL");

         boolean checkRegistration = deviceInfo.get("HASH").equals(actDeviceInfo.get("HASH"));
         if (!checkRegistration)
            checkRegistration = (deviceIMEI != null) && (activationIMEI != null) && (deviceIMEI.length() > 0) && deviceIMEI.equals(activationIMEI);
         if (!checkRegistration && deviceSerial != null && activationSerial != null && (deviceSerial.length() > 0))
         {
            checkRegistration = deviceSerial.equals(activationSerial);
            if (!checkRegistration && Settings.WIN32.equals(activationPlatform))
            {
               String[] tokens = Convert.tokenizeString(deviceSerial, '-');
               for (int i = tokens.length - 1 ; i >= 0 && !checkRegistration; i--)
                  checkRegistration = activationSerial.indexOf(tokens[i]) != -1;
            }
         }

         if (!checkRegistration)
            throw new Exception("The device information does not match with the registration.");

         logger.info("Verifying activation code");
         if (!Settings.WIN32.equals(activationPlatform) && !isValidActivationCode(
               Convert.hexStringToBytes(request.getActivationCode()),
               Convert.hexStringToBytes((String) deviceInfo.get("HASH"))))
            throw new Exception("The activation code does not match with the registration.");

         logger.info("Verifying expiration time");
         int compilationDate = Utils.toInt((String) productInfo.get("COMPILATION_DATE"),-1);
         int expiration = success.getExpireOn();
         if (expiration < compilationDate)
            throw new Exception("Please purchase renewal credits or use a VM version released before "+new totalcross.util.Date(expiration)+".");

         logger.info("Product is activated");

         String id = activatedProductId = (String) actProductInfo.get("ID");
         litebaseAllowed = id.charAt(2) == 'L' && id.charAt(3) == 'B';
         activationCode = request.getActivationCode();
         platform = (String) actDeviceInfo.get("PLATFORM");
         deviceId = (String) actDeviceInfo.get("ID");
         deviceHash = (String) actDeviceInfo.get("HASH");

         return true;
      }
      catch (FileNotFoundException e)
      {
         //logger.warning("The activation file was not found!"); - this warning confuses users
         return false;
      }
      catch (Exception e)
      {
         ActivationException ex = Utils.processException("Verification", e, true);
         logger.throwing("ras.ActivationClient", "isActivated", ex);
         throw ex;
      }
   }

   private boolean hasInternet()
   {
      try
      {
         Socket s = new Socket("www.google.com",80,30*1000);
         s.close();
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public void activate() throws ActivationException
   {
      Hashtable productInfo = Utils.getProductInfo();
      Hashtable deviceInfo = Utils.getDeviceInfo();
      RASConnection connection = null;

      try
      {
         logger.info("Retrieving registration key");
         byte[] key = readKey();
         if (key == null)
            throw new Exception("This application was not signed with a registration key");

         logger.info("Validating registration key");
         if (!isValidKey(key))
            throw new Exception("The registration key is not valid");

         logger.info("Generating activation code");
         byte[] activationCode = generateActivationCode(key, Convert.hexStringToBytes((String) deviceInfo.get("HASH")));

         // Generate and store activation request
         logger.info("Generating activation request");
         ActivationRequest request = new ActivationRequest(productInfo, deviceInfo, Convert
               .bytesToHexString(activationCode));
         writePacket(request, PDBFILE_TCRAS_REQUEST);

         // Connect to activation server
         logger.info("Connecting to server");
         connection = RASConnection.connect(30000, 20000);
         if (connection == null)
         {
            if (hasInternet())
               throw new Exception("Could not connect to the activation server, but there's internet available.");
            else
               throw new Exception("Could not connect to the activation server");
         }
         connection.sayHello(); // say hello

         // Send activation request
         logger.info("Sending activation request");
         connection.send(request);

         // Receive activation response
         logger.info("Receiving response");
         Packet packet = connection.receive();
         connection.close();
         connection = null;

         if (packet instanceof ActivationFailure)
         {
            ActivationFailure failure = (ActivationFailure) packet;
            throw new Exception(failure.getReason());
         }
         else
         {
            ActivationSuccess success = (ActivationSuccess) packet;

            // Store activation information
            logger.info("Storing information");
            writePacket(success, PDBFILE_TCRAS_SUCCESS);

            // Verify information (if verification fails, this will cause an exception to be thrown)
            isActivated();

            // And finally, delete the activation request packet
            String dataPath = Settings.dataPath;
            Settings.dataPath = null;
            try
            {
               PDBFile pdbFile = new PDBFile(PDBFILE_TCRAS_REQUEST, PDBFile.READ_WRITE);
               pdbFile.delete();
            }
            finally
            {
               Settings.dataPath = dataPath;
            }
         }
      }
      catch (Exception e)
      {
         if (connection != null)
         {
            try
            {
               connection.close();
               connection = null;
            }
            catch (IOException e2)
            {
            }
         }

         ActivationException ex = Utils.processException("Activation", e, true);
         logger.throwing("ras.ActivationClient", "activate", ex);
         logger.throwing("ras.ActivationClient", "original exception", e); // log also the original exception.
         String st = Vm.getStackTrace(e);
         logger.log(Logger.SEVERE,st,true);
         logger.log(Logger.SEVERE,e.getClass().getName(),true);
         logger.log(Logger.SEVERE,e.getMessage()+"",true);

         throw ex;
      }
      finally
      {
         try
         {
            if (connection != null)
               connection.close();
            //ConnectionManager.close();
         }
         catch (IOException e2)
         {
         }
      }
   }

   public boolean isValidKey(String key)
   {
      return isValidKey(Convert.hexStringToBytes(key));
   }

   private boolean isValidKey(byte[] key)
   {
      // Check key length
      if (key.length != 12)
         return false;

      int[] keyInts = Utils.bytesToInts(key);
      if (keyInts[6] < keyInts[4])
         return false;

      // Check checksums
      if (keyInts[10] != Utils.checksum(key, 0, 5, 0xFF) || keyInts[11] != Utils.checksum(key, 5, 5, 0xFF))
         return false;

      // Check mathematical restrictions
      int[] v = Utils.getTwinPrimes(keyInts[5], keyInts[4]);
      if (v.length == 0)
      {
         if (keyInts[6] != 0)
            return false;
      }
      else
      {
         int i = v.length - 1;
         for (; i >= 0; i--)
            if (v[i] == keyInts[6])
               break;

         if (i < 0)
            return false;
      }

      int[] primes = Utils.PRIMES;
      int i = primes.length - 1;
      for (; i >= 0; i--)
         if (primes[i] <= keyInts[7])
            break;
      i++;
      i = i == primes.length ? 0 : (-primes[i] & 0xFF);

      if (keyInts[9] != i)
         return false;

      return true;
   }

   private byte[] generateActivationCode(byte[] key, byte[] deviceId)
   {
      byte[] activationCode = new byte[12];
      int deviceChecksum = Utils.checksum(deviceId, 0xFFFF);

      activationCode[0] = (byte) Utils.checksum(key, 0, 2, 0xFF);
      activationCode[1] = (byte) (-key[2] & 0xFF);
      activationCode[2] = (byte) Utils.checksum(key, 2, 2, 0xFF);
      activationCode[3] = (byte) (-key[9] & 0xFF);
      activationCode[4] = (byte) Utils.checksum(key, 4, 2, 0xFF);
      activationCode[5] = key[6];
      activationCode[6] = (byte) Utils.checksum(key, 6, 2, 0xFF);
      activationCode[7] = (byte) (-key[1] & 0xFF);
      activationCode[8] = (byte) Utils.checksum(key, 8, 2, 0xFF);
      activationCode[9] = (byte) ((deviceChecksum >> 8) & 0xFF);
      activationCode[10] = (byte) Utils.checksum(key, 10, 2, 0xFF);
      activationCode[11] = (byte) (deviceChecksum & 0xFF);

      return activationCode;
   }

   private boolean isValidActivationCode(byte[] activationCode, byte[] deviceId)
   {
      int checksum = (((int) activationCode[9] & 0xFF) << 8) | ((int) activationCode[11] & 0xFF);
      if (checksum != Utils.checksum(deviceId, 0xFFFF))
         return false;
      else
         return true;
   }

   private void writePacket(Packet packet, String pdbFile) throws IOException, CommException, CryptoException
   {
      // Write packet
      ByteArrayStream bas = new ByteArrayStream(128);
      RASConnection connection = RASConnection.connect(bas);
      connection.send(packet);

      // Encrypt data
      byte[] enc = aesEncrypt(bas.toByteArray());

      // Write encrypted data to the pdb file so it can be easily installed
      String dataPath = Settings.dataPath;
      Settings.dataPath = null;
      try
      {
         PDBFile pdb = new PDBFile(pdbFile, PDBFile.CREATE_EMPTY);
         pdb.addRecord(enc.length);
         pdb.writeBytes(enc, 0, enc.length);
         pdb.setAttributes(PDBFile.DB_ATTR_BACKUP); // guich@tc113_7
         pdb.close();
      }
      finally
      {
         Settings.dataPath = dataPath;
      }
   }

   private Packet readPacket(String pdbFile) throws IOException, CommException, CryptoException
   {
      // Read encrypted data from the pdb file
      String dataPath = Settings.dataPath;
      Settings.dataPath = null;
      try
      {
         PDBFile pdb = new PDBFile(pdbFile, PDBFile.READ_WRITE);
         pdb.setRecordPos(0);
         byte[] enc = new byte[pdb.getRecordSize()];
         pdb.readBytes(enc, 0, enc.length);
         pdb.close();
         // Decrypt data
         byte[] dec = aesDecrypt(enc);

         // Read packet
         ByteArrayStream bas = new ByteArrayStream(dec);
         return RASConnection.connect(bas).receive();
      }
      finally
      {
         Settings.dataPath = dataPath;
      }
   }

   private byte[] readKey() throws IOException, CryptoException
   {
      // Read encrypted data from the key file
      byte[] enc = Vm.getFile(RESFILE_TCKEY);
      if (enc == null)
         return null;

      // Decrypt data and return
      return aesDecrypt(enc);
   }

   private byte[] aesEncrypt(byte[] dec) throws IOException, CryptoException
   {
      aesCipher.reset(AESCipher.OPERATION_ENCRYPT, aesKey, AESCipher.CHAINING_CBC, null, AESCipher.PADDING_PKCS5);
      aesCipher.update(dec);

      byte[] riv = aesCipher.getIV();
      byte[] out = aesCipher.getOutput();

      ByteArrayStream bas = new ByteArrayStream(128);
      DataStream ds = new DataStream(bas, true);

      ds.writeInt(riv.length);
      ds.writeBytes(riv);
      ds.writeInt(out.length);
      ds.writeBytes(out);

      return bas.toByteArray();
   }

   private byte[] aesDecrypt(byte[] enc) throws IOException, CryptoException
   {
      ByteArrayStream bas = new ByteArrayStream(enc);
      DataStream ds = new DataStream(bas);

      byte[] riv = new byte[ds.readInt()];
      ds.readBytes(riv, 0, riv.length);
      byte[] out = new byte[ds.readInt()];
      ds.readBytes(out, 0, out.length);

      aesCipher.reset(AESCipher.OPERATION_DECRYPT, aesKey, AESCipher.CHAINING_CBC, riv, AESCipher.PADDING_PKCS5);
      aesCipher.update(out);

      return aesCipher.getOutput();
   }

   private byte[] getAESKeyData()
   {
      return new byte[] { (byte) 0x06, (byte) 0x05, (byte) 0xF4, (byte) 0xF0, (byte) 0xF4, (byte) 0x08, (byte) 0x01,
            (byte) 0x09, (byte) 0xF7, (byte) 0x09, (byte) 0xFE, (byte) 0xFC, (byte) 0xF5, (byte) 0x04, (byte) 0x00,
            (byte) 0x0B };
   }

   private byte[] getSignerRSAKeyE()
   {
      return new byte[] { (byte) 1, (byte) 0, (byte) 1 };
   }

   private byte[] getSignerRSAKeyN()
   {
      return Convert.ints2bytes(
            new int[] { 0xC0699C00, 0x0A92507E, 0x33020F10, 0xE6CB2A47, 0xE3A18FC9, 0xAA75AEDA, 0x7EDFA963, 0x14112344,
                  0x34711273, 0x89DD103E, 0xB8C82856, 0x407CBBB9, 0x529A8F2A, 0xCEE404DE, 0x963C835C, 0xF9B479C7,
                  0x388689C3, 0x76BF22E8, 0x6408059E, 0x4507B358, 0x02D991BE, 0x9FA869B5, 0x2C705528, 0x214EB1EB,
                  0x5BCBFCE5, 0xC81E5C4F, 0xA92A6489, 0xBAA7ACA1, 0xA0FD4C74, 0x8B90AB3A, 0x42E7B3F0, 0x4258CE77,
                  0x00000067 }, 129);
   }
}
