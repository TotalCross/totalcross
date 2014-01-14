package tc.test.totalcross.crypto;

import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.crypto.digest.*;
import totalcross.sys.Convert;
import totalcross.ui.MainWindow;

public class DigestAutoTest extends MainWindow
{
   private String input = "0123456789ABCDEF";
   private Digest[] digests;
   
   public DigestAutoTest()
   {
      super("Digest Automatic Test", TAB_ONLY_BORDER);
   }         
   
   public void initUI()
   {
      try
      {
         digests = new Digest[] {new MD5Digest(), new SHA1Digest(), new SHA256Digest()};
      }
      catch (NoSuchAlgorithmException e) 
      {
         throw new RuntimeException();
      }
      
      if (!testDigest(digests[0]).equals("E43DF9B5A46B755EA8F1B4DD08265544"))
         throw new RuntimeException();
      if (!testDigest(digests[1]).equals("CE27CB141098FEB00714E758646BE3E99C185B71"))
         throw new RuntimeException();
      if (!testDigest(digests[2]).equals("2125B2C332B1113AAE9BFC5E9F7E3B4C91D828CB942C2DF1EEB02502ECCAE9E9"))
         throw new RuntimeException();
   }
   
   private String testDigest(Digest digest)
   {
      digest.reset();
      digest.update(input.getBytes());
      return Convert.bytesToHexString(digest.getDigest());
   }
}
