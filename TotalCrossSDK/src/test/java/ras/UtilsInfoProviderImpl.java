package ras;

import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.crypto.digest.MD5Digest;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.util.Hashtable;

public class UtilsInfoProviderImpl implements Utils.UtilsInfoProvider {

  @Override
  public Hashtable getProductInfo() {
    Hashtable info = null;
    if (ActivationClient.activateOnJDK_DEBUG)
    {
      info = new Hashtable(10);
      info.put("COMPILATION_DATE", Convert.toString(CompilationDate4D.COMPILATION_DATE ^ 12341234));

      //flsobral@tc125: added more info on v2
      info.put("VERSAO_VM", Settings.versionStr);
    }
    return info;
  }

  @Override
  public Hashtable getDeviceInfo() throws ActivationException {
    Hashtable info = null;
    if (ActivationClient.activateOnJDK_DEBUG)
    {
      MD5Digest md5;
      try
      {
        md5 = new MD5Digest();
      }
      catch (NoSuchAlgorithmException e)
      {
        throw new ActivationException(e.getMessage());
      }
      md5.update("1234ABCD".getBytes());

      info = new Hashtable(10);
      info.put("PLATFORM", Settings.WINDOWSMOBILE);
      info.put("ID", "Activation Test on JDK");
      info.put("HASH", Convert.bytesToHexString(md5.getDigest()));

      //flsobral@tc125: added more info on v2         
      info.put("VERSAO_ROM", Convert.toString(Settings.romVersion));
      info.put("COD_ATIVACAO", Settings.activationId);

      //flsobral@tc138: v3 info
      info.put("IMEI", Settings.imei);
      info.put("SERIAL", Settings.romSerialNumber);
    }
    return info;
  }
}
