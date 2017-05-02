package tc.test.totalcross.crypto;

import totalcross.crypto.CryptoException;
import totalcross.crypto.cipher.*;
import totalcross.crypto.digest.*;
import totalcross.crypto.signature.*;
import totalcross.sys.Convert;
import totalcross.ui.MainWindow;

public class SignatureAutoTest extends MainWindow
{
   private static final byte[] RSA_N = new byte[]
   {
      (byte)0, (byte)-60, (byte)-106, (byte)-118, (byte)-19, (byte)57, (byte)-63, (byte)-18,
      (byte)102, (byte)111, (byte)-56, (byte)1, (byte)50, (byte)-101, (byte)-90, (byte)-85,
      (byte)-96, (byte)-66, (byte)-70, (byte)-49, (byte)-52, (byte)-3, (byte)70, (byte)-120,
      (byte)63, (byte)-76, (byte)-34, (byte)-114, (byte)13, (byte)8, (byte)45, (byte)-124,
      (byte)-12, (byte)-6, (byte)87, (byte)90, (byte)61, (byte)-124, (byte)-42, (byte)34,
      (byte)21, (byte)14, (byte)-73, (byte)21, (byte)-104, (byte)70, (byte)11, (byte)-59,
      (byte)58, (byte)-72, (byte)-55, (byte)-98, (byte)68, (byte)123, (byte)-63, (byte)-11,
      (byte)-7, (byte)-115, (byte)32, (byte)57, (byte)-38, (byte)-41, (byte)-9, (byte)-108,
      (byte)79
   };
   
   private static final byte[] RSA_D = new byte[]
   {
      (byte)122, (byte)-69, (byte)13, (byte)-94, (byte)-54, (byte)-61, (byte)67, (byte)37,
      (byte)-38, (byte)-75, (byte)127, (byte)-31, (byte)-21, (byte)-128, (byte)-29, (byte)119,
      (byte)104, (byte)123, (byte)-46, (byte)-115, (byte)-60, (byte)-75, (byte)-53, (byte)12,
      (byte)18, (byte)-52, (byte)58, (byte)-36, (byte)-15, (byte)-11, (byte)17, (byte)34,
      (byte)-109, (byte)-121, (byte)5, (byte)117, (byte)109, (byte)-72, (byte)-27, (byte)-103,
      (byte)-85, (byte)-1, (byte)37, (byte)-30, (byte)38, (byte)-86, (byte)88, (byte)-28,
      (byte)-26, (byte)-102, (byte)-10, (byte)124, (byte)-97, (byte)-18, (byte)-118, (byte)2,
      (byte)36, (byte)40, (byte)-47, (byte)-75, (byte)-44, (byte)69, (byte)10, (byte)1
   };
   
   private static final byte[] RSA_E = new byte[]
   {
      (byte)1, (byte)0, (byte)1
   };
   
   private Signature[] signatures;
   private String input = "0123456789ABCDEF";
   private Key[] sigKeys;
   private Key[] verKeys;
   
   public SignatureAutoTest()
   {
      super("Signature Test", TAB_ONLY_BORDER);
   }
   
   public void initUI()
   {
      try
      {
         signatures = new Signature[]{new PKCS1Signature(new MD5Digest()), new PKCS1Signature(new SHA1Digest()), new PKCS1Signature(new SHA256Digest())};
      }
      catch (CryptoException e) 
      {
         throw new RuntimeException();
      }
      
      verKeys = new Key[3];
      verKeys[0] = verKeys[1] = verKeys[2] = new RSAPublicKey(RSA_E, RSA_N);
      
      sigKeys = new Key[3];
      sigKeys[0] = sigKeys[1] = sigKeys[2] = new RSAPrivateKey(RSA_E, RSA_D, RSA_N);

      if (!testSignature(signatures[0], sigKeys[0], verKeys[0])
         .equals("033DD980F2D1EE9639B9C5A2C59CB23740DE224D03C3BD6994863D6305E750418A7EDE548C2BCE1169521BE25733E0E05620BD26D1CFA4F21D3909A795E608C4"))
         throw new RuntimeException();
      if (!testSignature(signatures[1], sigKeys[1], verKeys[1])
         .equals("8A1431BF28D9365C54561B555804CB409E1559275FCA4CAC665D7517939E769A95404BAD8005F44D198D7E748E61F9CAA7F32365406913BBE4DF7F01F5170C78"))
         throw new RuntimeException();
      if (!testSignature(signatures[2], sigKeys[2], verKeys[2])
         .equals("4A39BA9164E0EB9FB3B9C5A5430524C482840DEB9D233B3AF7FD30F2D1C98E7407B07D7EB4205B3276C98DAB3631CEAB5D6D69AAFA32232D025B4CC01BF068A2"))
         throw new RuntimeException();
      
   }
   
   private String testSignature(Signature signature, Key sigKey, Key verKey)
   {
      try
      {
         byte[] data = input.getBytes();
         signature.reset(Signature.OPERATION_SIGN, sigKey);
         signature.update(data);
         byte[] bytes = signature.sign();
   
         signature.reset(Signature.OPERATION_VERIFY, verKey);
         signature.update(data);
          
         if (!signature.verify(bytes))
            throw new RuntimeException();
         
         return Convert.bytesToHexString(bytes);
      }
      catch (CryptoException exception)
      {
         throw new RuntimeException();
      }
   }
}
