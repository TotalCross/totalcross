// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ManifestPermissionEditorTest {
  private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

  @Test
  public void removesOptionalMediaPermissionsAndKeepsUnrelatedElements() {
    String source = manifest(
        permission("uses-permission", "android.permission.READ_MEDIA_IMAGES", ""),
        permission("uses-permission", "android.permission.READ_MEDIA_VIDEO", ""),
        permission("uses-permission", "android.permission.INTERNET", ""),
        element("uses-feature", "android.hardware.camera", "other_field { value: \"nested { braces }\" }"));

    String result = Deployer4Android.ManifestPermissionEditor.edit(source, Collections.emptySet());

    assertFalse(result.contains("android.permission.READ_MEDIA_IMAGES"));
    assertFalse(result.contains("android.permission.READ_MEDIA_VIDEO"));
    assertTrue(result.contains("android.permission.INTERNET"));
    assertTrue(result.contains("android.hardware.camera"));
    assertTrue(result.contains("nested { braces }"));
  }

  @Test
  public void preservesExistingPermissionFieldsAndRemovesDuplicates() {
    String first = permission("uses-permission", "android.permission.CAMERA",
        "      attribute {\n"
            + "        namespace_uri: \"" + ANDROID_NAMESPACE + "\"\n"
            + "        name: \"maxSdkVersion\"\n"
            + "        value: \"30\"\n"
            + "        resource_id: 16843291\n"
            + "        compiled_item { prim { int_decimal_value: 30 } }\n"
            + "      }\n");
    String duplicate = permission("uses-permission-sdk-23", "android.permission.CAMERA", "");
    Set<String> requested = Collections.singleton("android.permission.CAMERA");

    String result = Deployer4Android.ManifestPermissionEditor.edit(
        manifest(first, duplicate), requested);

    assertEquals(1, count(result, "name: \"uses-permission\""));
    assertTrue(result.contains("name: \"maxSdkVersion\""));
    assertTrue(result.contains("resource_id: 16843291"));
    assertTrue(result.contains("int_decimal_value: 30"));
    assertTrue(result.contains("namespace_uri: \"" + ANDROID_NAMESPACE + "\""));
  }

  @Test
  public void requestedPermissionsAreIdempotentAndPreserveCase() {
    String permission = "com.Vendor.permission.AccessDevice";
    Set<String> requested = new LinkedHashSet<>(Collections.singleton(permission));
    String source = manifest();

    String once = Deployer4Android.ManifestPermissionEditor.edit(source, requested);
    String twice = Deployer4Android.ManifestPermissionEditor.edit(once, requested);

    assertEquals(1, count(twice, "name: \"uses-permission\""));
    assertTrue(twice.contains("value: \"" + permission + "\""));
  }

  @Test
  public void rejectsMalformedPermissionNames() {
    assertThrows(DeployerException.class,
        () -> Deployer4Android.addAndroidPermission("android.permission."));
  }

  private static String manifest(String... children) {
    StringBuilder text = new StringBuilder("element {\n  namespace_uri: \"\"\n  name: \"manifest\"\n");
    for (String child : children) text.append(child).append('\n');
    return text.append("}\n").toString();
  }

  private static String permission(String elementName, String permission, String additionalFields) {
    return elementWithAttributes(elementName,
        nameAttribute(permission, true) + additionalFields);
  }

  private static String element(String elementName, String name, String contents) {
    return elementWithAttributes(elementName, nameAttribute(name, false) + contents);
  }

  private static String nameAttribute(String name, boolean withResourceId) {
    return "      attribute {\n"
        + "        namespace_uri: \"" + ANDROID_NAMESPACE + "\"\n"
        + "        name: \"name\"\n"
        + "        value: \"" + name + "\"\n"
        + (withResourceId ? "        resource_id: 16842755\n" : "")
        + "      }\n";
  }

  private static String elementWithAttributes(String elementName, String attributes) {
    return "  child {\n"
        + "    element {\n"
        + "      namespace_uri: \"\"\n"
        + "      name: \"" + elementName + "\"\n"
        + attributes + "\n"
        + "    }\n"
        + "  }\n";
  }

  private static int count(String text, String needle) {
    int count = 0;
    for (int from = 0; (from = text.indexOf(needle, from)) >= 0; from += needle.length()) count++;
    return count;
  }
}
