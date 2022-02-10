package tc.tools.deployer.ipa.blob;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

public class EmbeddedSignature extends SuperBlob {
  /** http://opensource.apple.com/source/libsecurity_codesigning/libsecurity_codesigning-55032/lib/cscdefs.h */
  public static final long CSMAGIC_EMBEDDED_SIGNATURE = 0xfade0cc0;

  public static final long CSSLOT_CODEDIRECTORY = 0L;
  public static final long CSSLOT_REQUIREMENTS = 2L;
  public static final long CSSLOT_ENTITLEMENTS = 5L;
  public static final long CSSLOT_ENTITLEMENTS_DER = 7L;
  public static final long CSSLOT_ALTERNATE_CODEDIRECTORIES = 0x1000;
  public static final long CSSLOT_BLOBWRAPPER = 0x10000;

  public CodeDirectory codeDirectory;
  public Entitlements entitlements;
  EntitlementsDer entitlementsDer;
  Requirements requirements;
  BlobWrapper blobWrapper;
  public List<CodeDirectory> alternateCodeDirectories = new ArrayList<CodeDirectory>();

  public EmbeddedSignature() {
    super(CSMAGIC_EMBEDDED_SIGNATURE);
  }

  public EmbeddedSignature(CodeDirectory codeDirectory, Entitlements entitlements, Requirements requirements,
      BlobWrapper blobWrapper) {
    this();
    add(new BlobIndex(CSSLOT_CODEDIRECTORY, codeDirectory));
    add(new BlobIndex(CSSLOT_REQUIREMENTS, requirements));
    add(new BlobIndex(CSSLOT_ENTITLEMENTS, entitlements));
    add(new BlobIndex(CSSLOT_BLOBWRAPPER, blobWrapper));
  }

  @Override
  public void add(BlobIndex blobIndex) {
    switch ((int) blobIndex.blobType) {
    case (int) CSSLOT_CODEDIRECTORY:
      codeDirectory = (CodeDirectory) blobIndex.blob;
      break;
    case (int) CSSLOT_REQUIREMENTS:
      requirements = (Requirements) blobIndex.blob;
      break;
    case (int) CSSLOT_ENTITLEMENTS:
      entitlements = (Entitlements) blobIndex.blob;
      break;
    case (int) CSSLOT_ENTITLEMENTS_DER:
      entitlementsDer = (EntitlementsDer) blobIndex.blob;
      break;
    case (int) CSSLOT_ALTERNATE_CODEDIRECTORIES:
      alternateCodeDirectories.add((CodeDirectory) blobIndex.blob);
      break;
    case (int) CSSLOT_BLOBWRAPPER:
      blobWrapper = (BlobWrapper) blobIndex.blob;
      break;
    default:
      return;
    }
    super.add(blobIndex);
  }

  public void sign(KeyStore keyStore, Store certStore) throws IOException, CMSException, UnrecoverableKeyException, CertificateEncodingException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException {
    blobWrapper.sign(keyStore, certStore, codeDirectory);
  }

  public void setBundleIdentifier(String bundleIdentifier) {
    codeDirectory.identifier = bundleIdentifier;
    for (CodeDirectory cd : alternateCodeDirectories) {
      cd.identifier = bundleIdentifier;
    }
  }

  public void updateCodeDirectoryHashes(byte[] data, byte[] info, byte[] sourceData, Object object) throws IOException {
    byte[] requirementsBytes = requirements.getBytes();
    byte[] entitlementsBytes = entitlements.getBytes();
    
    List<CodeDirectory> cds = new ArrayList<>(alternateCodeDirectories.size() + 1);
    Collections.copy(alternateCodeDirectories, cds);
    cds.add(codeDirectory);
    cds.forEach(cd -> {
      cd.setSpecialSlotsHashes(info, requirementsBytes, sourceData, null, entitlementsBytes);
      cd.setCodeSlotsHashes(data);
    });
  }
}
