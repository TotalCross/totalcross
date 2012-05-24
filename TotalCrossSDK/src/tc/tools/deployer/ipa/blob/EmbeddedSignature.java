package tc.tools.deployer.ipa.blob;

public class EmbeddedSignature extends SuperBlob
{
   /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
   public static final long CSMAGIC_EMBEDDED_SIGNATURE = 0xfade0cc0;

   public EmbeddedSignature()
   {
      super(CSMAGIC_EMBEDDED_SIGNATURE);
   }
}
