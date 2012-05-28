package tc.tools.deployer.ipa.blob;

public class BlobIndex
{
   public static final int CSSLOT_CODEDIRECTORY = 0;
   public static final int CSSLOT_REQUIREMENTS = 2;
   public static final int CSSLOT_ENTITLEMENTS = 5;
   public static final int CSSLOT_SIGNATURE = 0x10000;

   public long blobType;
   public long offset;
   public BlobCore blob;

   BlobIndex(long blobType, BlobCore blob)
   {
      this.blobType = blobType;
      this.blob = blob;
   }
}
