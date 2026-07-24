// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.deployer.ipa.MobileProvision;
import totalcross.sys.Time;

class IOSCertDatePolicyTest {
  @BeforeAll
  static void installBouncyCastle() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  void usesProvisioningProfileExpirationDate() throws Exception {
    MobileProvision provision = new MobileProvision(provisioningProfile("2030-01-02T03:04:05Z"));

    Time actual = Deployer4IPhoneIPA.getProvisioningProfileExpirationDate(provision);

    assertEquals(new Time(provision.expirationDate.getDate().getTime(), false).toIso8601(), actual.toIso8601());
  }

  @Test
  void returnsNullWithoutProvisioningProfileExpirationDate() throws Exception {
    MobileProvision provision = new MobileProvision(provisioningProfile(null));

    assertNull(Deployer4IPhoneIPA.getProvisioningProfileExpirationDate(provision));
    assertNull(Deployer4IPhoneIPA.getProvisioningProfileExpirationDate(null));
  }

  private static String provisioningProfile(String expirationDate) {
    String expiration = expirationDate == null ? "" : "<key>ExpirationDate</key><date>" + expirationDate + "</date>";
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<plist version=\"1.0\"><dict>"
        + "<key>ApplicationIdentifierPrefix</key><array><string>TEAMID</string></array>"
        + expiration
        + "<key>Entitlements</key><dict>"
        + "<key>application-identifier</key><string>TEAMID.com.example.app</string>"
        + "</dict></dict></plist>";
  }
}
