package tc.tools.deployer.ipa.blob;

public class EntitlementsDer extends BlobCore {
  /** https://bitbucket.org/khooyp/gdb/src/c3a263c415ad/include/mach-o/codesign.h */
  public static final long CSMAGIC_EMBEDDED_ENTITLEMENTS_DER = 0xfade7172;

  public EntitlementsDer() {
    super(CSMAGIC_EMBEDDED_ENTITLEMENTS_DER);
  }

  public EntitlementsDer(byte[] data) {
    this();
    this.data = data;
  }
}
