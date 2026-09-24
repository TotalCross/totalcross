// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.bytecode.ByteCode;

class SemaphoreConverterTest {
  private static final String OWNER = "java/util/concurrent/Semaphore";
  private static final String DEVICE_OWNER = "jdkcompat.util.concurrent.Semaphore4D";
  private static final String DIAGNOSTIC_OWNER = "totalcross/util/concurrent/SemaphoreTestDiagnostics";
  private static final String DIAGNOSTIC_SYMBOL = "tucSTD_awaitWaiters_si";
  private static final String DIAGNOSTIC_FLAG = "TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS";
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
  void sourceImportMapsSupportedCallsAndRejectsUnsupportedOverloads(@TempDir Path directory) throws Exception {
    Path source = directory.resolve("fixtures/SemaphoreApiFixture.java");
    Path output = directory.resolve("classes");
    Files.createDirectories(source.getParent());
    Files.createDirectories(output);
    Files.writeString(source, String.join("\n",
        "package fixtures;",
        "import java.util.concurrent.Semaphore;",
        "import java.util.concurrent.TimeUnit;",
        "class SemaphoreApiFixture {",
        "  static void supported() throws InterruptedException {",
        "    Semaphore semaphore = new Semaphore(1);",
        "    semaphore.acquire();",
        "    semaphore.acquireUninterruptibly();",
        "    semaphore.tryAcquire();",
        "    semaphore.release();",
        "  }",
        "  static void unsupported() throws InterruptedException {",
        "    Semaphore semaphore = new Semaphore(1, true);",
        "    semaphore.acquire(1);",
        "    semaphore.tryAcquire(1, TimeUnit.SECONDS);",
        "    semaphore.release(1);",
        "  }",
        "}",
        ""));

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "Semaphore declaration fixture requires a JDK compiler");
    ByteArrayOutputStream compilerErrors = new ByteArrayOutputStream();
    int compilationResult = compiler.run(null, null, compilerErrors,
        "-proc:none", "--release", "8", "-d", output.toString(), source.toString());
    assertEquals(0, compilationResult, compilerErrors.toString());

    Set<String> calls = new HashSet<>();
    Path fixtureClass = output.resolve("fixtures/SemaphoreApiFixture.class");
    new ClassReader(Files.readAllBytes(fixtureClass)).accept(new ClassVisitor(Opcodes.ASM5) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
          String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM5) {
          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (OWNER.equals(owner)) calls.add(name + descriptor);
          }
        };
      }
    }, 0);

    Set<String> supportedCalls = Set.of(
        "<init>(I)V", "acquire()V", "acquireUninterruptibly()V", "tryAcquire()Z", "release()V");
    Set<String> unsupportedCalls = Set.of(
        "<init>(IZ)V", "acquire(I)V", "tryAcquire(JLjava/util/concurrent/TimeUnit;)Z", "release(I)V");
    Set<String> expectedCalls = new HashSet<>(supportedCalls);
    expectedCalls.addAll(unsupportedCalls);
    assertEquals(expectedCalls, calls, "compiled source must exercise the exact v1 and unsupported members");

    MethodDeclarationResolver.beginConversionRun();
    for (String call : supportedCalls) assertMappedCall(call, true);
    for (String call : unsupportedCalls) assertMappedCall(call, false);
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
    String diagnosticSource = Files.readString(
        Path.of("src/smokeTest/java/totalcross/util/concurrent/SemaphoreTestDiagnostics.java"));
    Path productionDiagnosticSource = Path.of(
        "src/main/java/totalcross/util/concurrent/SemaphoreTestDiagnostics.java");
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

    String diagnosticDeclaration = DIAGNOSTIC_OWNER
        + "|native public static int awaitWaiters(java.util.concurrent.Semaphore semaphore, int minimumWaiters);";
    assertTrue(declarations.contains(diagnosticDeclaration), "missing smoke diagnostic declaration");
    assertFalse(declarations.contains(OWNER + "|native public static int awaitWaiters"),
        "the diagnostic must not extend java.util.concurrent.Semaphore");
    assertEquals(6, declarations.lines().filter(line -> line.startsWith(OWNER + "|")).count(),
        "the public Semaphore v1 native surface must remain unchanged");
    assertTrue(diagnosticSource.contains("native int awaitWaiters(Semaphore semaphore, int minimumWaiters)"),
        "missing smoke-source-only diagnostic bridge");
    assertFalse(Files.exists(productionDiagnosticSource), "diagnostic bridge must stay out of SDK main sources");
    assertTrue(prototypes.contains("TC_API void " + DIAGNOSTIC_SYMBOL + "(NMParams p);"));
    assertTrue(header.contains("TC_API void " + DIAGNOSTIC_SYMBOL + "(NMParams p);"));
    assertTrue(registrations.contains("hashCode(\"" + DIAGNOSTIC_SYMBOL + "\"), &" + DIAGNOSTIC_SYMBOL));
    assertTrue(implementation.contains("TC_API void " + DIAGNOSTIC_SYMBOL + "(NMParams p)"));

    String defaultImplementation = withoutSemaphoreDiagnostics(implementation);
    assertFalse(defaultImplementation.contains("diagnosticCondition"),
        "default Semaphore state must omit diagnostic condition fields");
    assertFalse(defaultImplementation.contains("diagnosticWaiters"),
        "default acquire path must omit diagnostic branches");
    assertFalse(defaultImplementation.contains(DIAGNOSTIC_SYMBOL),
        "default VM must not compile the diagnostic native method");
    assertFalse(withoutSemaphoreDiagnostics(registrations).contains(DIAGNOSTIC_SYMBOL),
        "default VM must not register the diagnostic native method");
    assertTrue(implementation.contains("#if defined(" + DIAGNOSTIC_FLAG
        + ")\n   THREAD_CONDITION_TYPE diagnosticCondition;"),
        "diagnostic state fields must be behind the opt-in flag");
    assertTrue(implementation.contains("#if defined(" + DIAGNOSTIC_FLAG + ")\nTC_API void "
        + DIAGNOSTIC_SYMBOL + "(NMParams p)"), "diagnostic native method must be opt-in");
    String diagnosticRegistration = "   htPutPtr(&htNativeProcAddresses, hashCode(\""
        + DIAGNOSTIC_SYMBOL + "\"), &" + DIAGNOSTIC_SYMBOL + ");";
    assertTrue(registrations.contains("#if defined(" + DIAGNOSTIC_FLAG + ")\n"
        + diagnosticRegistration + "\n#endif"),
        "diagnostic native address must be registered only in diagnostic builds");
    assertTrue(cmake.contains("option(" + DIAGNOSTIC_FLAG
        + "\n  \"Enable test-only Semaphore native diagnostics\"\n  OFF\n)"),
        "diagnostics must be disabled by default");
    assertTrue(cmake.contains("if(" + DIAGNOSTIC_FLAG + ")\n  target_compile_definitions(tcvm PRIVATE "
        + DIAGNOSTIC_FLAG + "=1)\nendif()"), "CMake must define diagnostics only when enabled");
    assertFalse(android.contains(DIAGNOSTIC_FLAG), "Android builds must not opt in by default");
    assertFalse(vcproj.contains(DIAGNOSTIC_FLAG), "Windows builds must not opt in by default");

    assertTrue(cmake.contains("nm/util/concurrent_Semaphore.c"));
    assertTrue(android.contains("nm/util/concurrent_Semaphore.c"));
    assertTrue(vcproj.contains("nm\\util\\concurrent_Semaphore.c"));
  }

  private static void assertMappedCall(String call, boolean supported) {
    int descriptorStart = call.indexOf('(');
    String name = call.substring(0, descriptorStart);
    String descriptor = call.substring(descriptorStart);
    MethodDeclarationResolver.Resolution resolution = MethodDeclarationResolver.resolve(OWNER, name, descriptor);
    assertTrue(resolution.deviceClassFound, "missing device Semaphore mapping for " + call);
    assertEquals(supported, resolution.deviceMemberFound, "unexpected v1 support for " + call);
    if (supported) {
      assertEquals(DEVICE_OWNER, resolution.deviceOwner, call);
      assertEquals(OWNER, resolution.declarationOwner, call);
    }
  }

  private static String withoutSemaphoreDiagnostics(String source) {
    StringBuilder output = new StringBuilder();
    int excludedDepth = 0;
    for (String line : source.split("\\R", -1)) {
      String trimmed = line.trim();
      if (excludedDepth == 0 && trimmed.equals("#if defined(" + DIAGNOSTIC_FLAG + ")")) {
        excludedDepth = 1;
        continue;
      }
      if (excludedDepth > 0) {
        if (trimmed.startsWith("#if ")) excludedDepth++;
        if (trimmed.equals("#endif")) excludedDepth--;
      } else {
        output.append(line).append('\n');
      }
    }
    assertEquals(0, excludedDepth, "diagnostic preprocessor blocks must be balanced");
    return output.toString();
  }
}
