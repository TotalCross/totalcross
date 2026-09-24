// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;

class SemaphoreConverterTest {
  private static final String OWNER = "java/util/concurrent/Semaphore";
  private static final String DEVICE_OWNER = "jdkcompat.util.concurrent.Semaphore4D";
  private static final String[] METHODS = { "acquire", "acquireUninterruptibly", "tryAcquire", "release" };
  private static final String[] DESCRIPTORS = { "()V", "()V", "()Z", "()V" };
  private static final String[] SYMBOLS = {
      "jucS_create_i", "jucS_destroy", "jucS_acquire", "jucS_acquireUninterruptibly",
      "jucS_tryAcquire", "jucS_release"
  };

  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void mapsOnlyTheSemaphoreV1Surface() {
    MethodDeclarationResolver.Resolution constructor = MethodDeclarationResolver.resolve(OWNER, "<init>", "(I)V");
    assertTrue(constructor.deviceMemberFound);
    assertEquals(DEVICE_OWNER, constructor.deviceOwner);

    for (int i = 0; i < METHODS.length; i++) {
      MethodDeclarationResolver.Resolution method = MethodDeclarationResolver.resolve(OWNER, METHODS[i], DESCRIPTORS[i]);
      assertTrue(method.deviceMemberFound, "missing Semaphore v1 method " + METHODS[i]);
      assertEquals(DEVICE_OWNER, method.deviceOwner, METHODS[i]);
      assertEquals(OWNER, method.declarationOwner, METHODS[i]);
    }

    assertFalse(MethodDeclarationResolver.resolve(OWNER, "<init>", "(IZ)V").deviceMemberFound);
    assertFalse(MethodDeclarationResolver.resolve(OWNER, "acquire", "(I)V").deviceMemberFound);
    assertFalse(MethodDeclarationResolver.resolve(OWNER, "tryAcquire",
        "(JLjava/util/concurrent/TimeUnit;)Z").deviceMemberFound);
    assertFalse(MethodDeclarationResolver.resolve(OWNER, "release", "(I)V").deviceMemberFound);
    assertFalse(MethodDeclarationResolver.resolve(OWNER, "availablePermits", "()I").deviceMemberFound);
    assertFalse(MethodDeclarationResolver.resolve(OWNER, "drainPermits", "()I").deviceMemberFound);
  }

  @Test
  void nativeRegistrationsAndSourceInventoriesStaySynchronized() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    String cmake = Files.readString(vmRoot.resolve("CMakeLists.txt"));
    String android = Files.readString(vmRoot.resolve("src/jni/Android.mk"));
    String vcproj = Files.readString(vmRoot.resolve("vc2008/TCVM.vcproj"));
    String implementation = Files.readString(vmRoot.resolve("src/nm/util/concurrent_Semaphore.c"));
    String[] expectedDeclarations = {
        OWNER + "|native private void create(int permits);",
        OWNER + "|native private void destroy();",
        OWNER + "|native public void acquire() throws InterruptedException;",
        OWNER + "|native public void acquireUninterruptibly();",
        OWNER + "|native public boolean tryAcquire();",
        OWNER + "|native public void release();"
    };
    assertEquals(expectedDeclarations.length,
        declarations.lines().filter(line -> line.startsWith(OWNER + "|")).count());
    for (int i = 0; i < SYMBOLS.length; i++) {
      assertTrue(declarations.contains(expectedDeclarations[i]),
          "missing source-of-truth declaration " + expectedDeclarations[i]);
      assertTrue(prototypes.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing generated prototype for " + SYMBOLS[i]);
      assertTrue(header.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing native header declaration for " + SYMBOLS[i]);
      assertTrue(registrations.contains("hashCode(\"" + SYMBOLS[i] + "\"), &" + SYMBOLS[i]),
          "missing native-address registration for " + SYMBOLS[i]);
      assertTrue(implementation.contains("TC_API void " + SYMBOLS[i] + "(NMParams p)"),
          "missing native implementation for " + SYMBOLS[i]);
    }

    assertTrue(cmake.contains("nm/util/concurrent_Semaphore.c"));
    assertTrue(android.contains("nm/util/concurrent_Semaphore.c"));
    assertTrue(vcproj.contains("nm\\util\\concurrent_Semaphore.c"));
  }
}
