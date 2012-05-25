package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import org.bouncycastle.cms.CMSException;

public class EmbeddedSignature extends SuperBlob
{
   /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
   public static final long CSMAGIC_EMBEDDED_SIGNATURE = 0xfade0cc0;

   public static final long CSSLOT_CODEDIRECTORY = 0L;
   public static final long CSSLOT_REQUIREMENTS = 2L;
   public static final long CSSLOT_ENTITLEMENTS = 5L;
   public static final long CSSLOT_BLOBWRAPPER = 0x10000;

   public CodeDirectory codeDirectory;
   Entitlements entitlements;
   Requirements requirements;
   BlobWrapper blobWrapper;

   public EmbeddedSignature()
   {
      super(CSMAGIC_EMBEDDED_SIGNATURE);
   }

   public EmbeddedSignature(CodeDirectory codeDirectory, Entitlements entitlements, Requirements requirements,
         BlobWrapper blobWrapper)
   {
      this();
      add(new BlobIndex(CSSLOT_CODEDIRECTORY, codeDirectory));
      add(new BlobIndex(CSSLOT_REQUIREMENTS, requirements));
      add(new BlobIndex(CSSLOT_ENTITLEMENTS, entitlements));
      add(new BlobIndex(CSSLOT_BLOBWRAPPER, blobWrapper));
   }

   public void add(BlobIndex blobIndex)
   {
      switch ((int) blobIndex.blobType)
      {
         case (int) CSSLOT_CODEDIRECTORY:
            codeDirectory = (CodeDirectory) blobIndex.blob;
         break;
         case (int) CSSLOT_REQUIREMENTS:
            requirements = (Requirements) blobIndex.blob;
         break;
         case (int) CSSLOT_ENTITLEMENTS:
            entitlements = (Entitlements) blobIndex.blob;
         break;
         case (int) CSSLOT_BLOBWRAPPER:
            blobWrapper = (BlobWrapper) blobIndex.blob;
         break;
         default:
            return;
      }
      super.add(blobIndex);
   }

   public void sign() throws IOException, CMSException
   {
      blobWrapper.sign();
   }
}
