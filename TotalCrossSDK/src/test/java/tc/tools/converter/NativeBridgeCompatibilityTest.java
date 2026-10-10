// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeBridgeCompatibilityTest {
  @Test
  void preservesHistoricalNativeImageBackingAliasAndFeatureGuards() throws Exception {
    NativeBridgeCompatibility compatibility = NativeBridgeCompatibility.read(
        Path.of("..", "TotalCrossVM", "src", "nm", "native-bridge-compat.txt"));

    NativeBridgeModel.Entry entry = new NativeBridgeModel.Entry(
        "totalcross/ui/image/NativeImageBacking",
        "totalcross/ui/image/NativeImageBacking",
        "createFromArgbPixelsNative",
        "createFromArgbPixelsNative",
        "([III)J",
        "tuiNIB_createFromArgbPixelsNativ",
        "replaced");

    assertEquals("tuiNIB_createFromArgbPixels_Iii", compatibility.effectiveSymbol(entry));
    assertEquals("TC_ENABLE_RUNTIME_DIAGNOSTICS",
        compatibility.guardFor("tsRDS_readMetricNative_i"));
    assertEquals("TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS",
        compatibility.guardFor("tucSTD_awaitWaiters_si"));
    assertTrue(compatibility.bridgeSymbols().contains("tnsSSLCM_startHandshake"));
    assertTrue(compatibility.headerOnlySymbols().contains("tugG_drawThickLine_iiiii"));
  }

  @Test
  void preservesEveryHistoricalRegistrationFromLegacyTable() throws Exception {
    NativeBridgeCompatibility compatibility = NativeBridgeCompatibility.read(
        Path.of("..", "TotalCrossVM", "src", "nm", "native-bridge-compat.txt"));
    Set<String> required = Set.of(
        "jlS_getBytes",
        "rU_getConfigInfo", "rU_getDeviceInfo", "rU_getProductInfo",
        "tnCM_open", "tpSMS_receive", "tpSMS_send_ss",
        "tpcbIPOIC_GetAllAppointments", "tpcbIPOIC_GetAllContacts", "tpcbIPOIC_GetAllTasks",
        "tpcbIPOIC_NewContact", "tpcbIPOIC_ViewAllAppointments", "tpcbIPOIC_ViewAllContacts",
        "tpcbIPOIC_ViewAllTasks", "tpcbIPOIC_editIAppointment_sssss",
        "tpcbIPOIC_editIContact_sssssssss", "tpcbIPOIC_editITask_ssssssssssss",
        "tpcbIPOIC_getIAppointmentString_", "tpcbIPOIC_getIContactString_s",
        "tpcbIPOIC_getITaskString_s", "tpcbIPOIC_newAppointment", "tpcbIPOIC_newTask",
        "tpcbIPOIC_removeIAppointment_s", "tpcbIPOIC_removeIContact_s", "tpcbIPOIC_removeITask_s",
        "tsC_numberPad_ii", "tsC_numberPad_si");
    Set<String> missing = new TreeSet<String>(required);
    missing.removeAll(compatibility.bridgeSymbols());

    assertTrue(missing.isEmpty(), "Historical native registrations were removed: " + missing);
  }

  @Test
  void rejectsJavaDerivableExplicitBridge(@TempDir Path tempDir) throws Exception {
    Path file = tempDir.resolve("native-bridge-compat.txt");
    Files.writeString(file, "bridge\tjlS_nanoTime\n");
    NativeBridgeCompatibility compatibility = NativeBridgeCompatibility.read(file);
    NativeBridgeModel.Entry entry = new NativeBridgeModel.Entry(
        "jdkcompat/lang/System4D", "java/lang/System", "nanoTime", "nanoTime",
        "()J", "jlS_nanoTime", "native");

    assertThrows(IllegalStateException.class,
        () -> compatibility.validateMinimality(List.of(entry)));
  }
}
