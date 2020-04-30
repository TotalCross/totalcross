// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.commons.io.FileUtils;

import com.dd.plist.NSArray;
import com.dd.plist.NSData;
import com.dd.plist.NSDate;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

public class MobileProvision {
  public final String applicationIdentifierPrefix;
  public final NSDate creationDate;
  public final Certificate[] developerCertificates;
  public final NSDictionary entitlements;
  public final NSDate expirationDate;
  public final String name;
  public final boolean provisionsAllDevices;
  public final String[] provisionedDevices;
  public final String[] teamIdentifier;
  public final int timeToLive;
  public final String uuid;
  public final int version;

  public final String bundleIdentifier;

  private final String content;

  public MobileProvision(String content) throws Exception {
    this.content = content;

    NSDictionary rootDictionary = (NSDictionary) PropertyListParser.parse(content.getBytes("UTF-8"));

    // ApplicationIdentifierPrefix
    NSArray array = (NSArray) rootDictionary.objectForKey("ApplicationIdentifierPrefix");
    applicationIdentifierPrefix = array.count() > 0 ? array.objectAtIndex(0).toString() : null;

    // CreationDate
    creationDate = (NSDate) rootDictionary.objectForKey("CreationDate");

    // DeveloperCertificates
    CertificateFactory cf = CertificateFactory.getInstance("X509", "BC");
    array = (NSArray) rootDictionary.objectForKey("DeveloperCertificates");
    developerCertificates = array != null ? new Certificate[array.count()] : null;
    if (developerCertificates != null && developerCertificates.length > 0) {
      NSObject[] certificates = array.getArray();
      for (int i = 0; i < certificates.length; i++) {
        ByteArrayInputStream certificateData = new ByteArrayInputStream(((NSData) certificates[i]).bytes());
        developerCertificates[i] = cf.generateCertificate(certificateData);
      }
    }

    // Entitlements
    entitlements = (NSDictionary) rootDictionary.objectForKey("Entitlements");

    // ExpirationDate
    expirationDate = (NSDate) rootDictionary.objectForKey("ExpirationDate");

    // Name
    NSObject item = rootDictionary.objectForKey("Name");
    this.name = item != null ? item.toString() : "(unknown)";

    // ProvisionsAllDevices
    NSNumber number = (NSNumber) rootDictionary.objectForKey("ProvisionsAllDevices");
    provisionsAllDevices = number != null ? number.boolValue() : false;

    // ProvisionedDevices
    array = (NSArray) rootDictionary.objectForKey("ProvisionedDevices");
    provisionedDevices = array != null ? new String[array.count()] : null;
    if (provisionedDevices != null && provisionedDevices.length > 0) {
      NSObject[] devices = array.getArray();
      for (int i = 0; i < devices.length; i++) {
        provisionedDevices[i] = devices[i].toString();
      }
    }

    // TeamIdentifier
    array = (NSArray) rootDictionary.objectForKey("TeamIdentifier");
    teamIdentifier = array != null ? new String[array.count()] : null;
    if (teamIdentifier != null && teamIdentifier.length > 0) {
      NSObject[] team = array.getArray();
      for (int i = 0; i < team.length; i++) {
        teamIdentifier[i] = team[i].toString();
      }
    }

    // TimeToLive
    number = (NSNumber) rootDictionary.objectForKey("TimeToLive");
    timeToLive = number != null ? number.intValue() : -1;

    // UUID
    item = rootDictionary.objectForKey("UUID");
    uuid = item != null ? item.toString() : null;

    // Version
    number = (NSNumber) rootDictionary.objectForKey("Version");
    version = number != null ? number.intValue() : -1;

    // Entitlements > application-identifier
    String s = entitlements.objectForKey("application-identifier").toString();
    bundleIdentifier = s.substring(s.indexOf('.') + 1);
  }

  @Override
  public String toString() {
    return content;
  }

  public String GetEntitlementsString() {
    NSDictionary XCentPList = new NSDictionary();
    String[] keys = entitlements.allKeys();
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      NSObject item = entitlements.objectForKey(key);
      XCentPList.put(key, item);
    }
    return MyNSObjectSerializer.toXMLPropertyList(XCentPList);
  }

  public static MobileProvision readFromFile(File input) throws Exception {
    byte[] inputData = FileUtils.readFileToByteArray(input);
    String inputString = new String(inputData, "UTF-8");

    int startIdx = inputString.indexOf("<?xml");
    if (startIdx == -1) {
      return null;
    }

    int length = ((inputData[startIdx - 2] & 0xFF) << 8) | (inputData[startIdx - 1] & 0xFF);
    int endIdx = inputString.lastIndexOf('>', length + startIdx) + 1;
    inputString = inputString.substring(startIdx, endIdx);

    return new MobileProvision(inputString);
  }
}
