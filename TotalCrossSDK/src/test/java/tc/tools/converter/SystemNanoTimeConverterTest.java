// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCMethod;

class SystemNanoTimeConverterTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void simulatorUsesJdkNanoTimeWithMonotonicNanosecondSemantics() throws Exception {
    long first = System.nanoTime();
    long second = System.nanoTime();
    assertTrue(second >= first);

    Thread.sleep(20L);
    long afterSleep = System.nanoTime();
    assertTrue(afterSleep - second >= 10_000_000L);

    long previous = afterSleep;
    for (int i = 0; i < 1000; i++) {
      long current = System.nanoTime();
      assertTrue(current >= previous);
      previous = current;
    }
  }

  @Test
  void deviceDeclarationAndNativeMetadataUseLongNanoTimeContract() throws Exception {
    java.lang.reflect.Method method = Class.forName("jdkcompat.lang.System4D").getDeclaredMethod("nanoTime");
    assertEquals(long.class, method.getReturnType());
    assertTrue(Modifier.isPublic(method.getModifiers()));
    assertTrue(Modifier.isStatic(method.getModifiers()));
    assertTrue(Modifier.isNative(method.getModifiers()));

    Path vmRoot = Path.of("..", "TotalCrossVM");
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    assertTrue(declarations.contains("java/lang/System|native public static long nanoTime();"));
    assertTrue(prototypes.contains("TC_API void jlS_nanoTime(NMParams p);"));
    assertTrue(header.contains("TC_API void jlS_nanoTime(NMParams p);"));
    assertTrue(registrations.contains("hashCode(\"jlS_nanoTime\"), &jlS_nanoTime"));
  }

  @Test
  void nativeImplementationsUsePlatformMonotonicClocks() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String posix = Files.readString(vmRoot.resolve("src/util/posix/utils_c.h"));
    String posixNanoTime = between(posix, "static int64 privateGetNanoTime()", "static int32 privateGetTimeStamp()");
    String win = Files.readString(vmRoot.resolve("src/util/win/utils_c.h"));
    String winNanoTime = between(win, "static int64 privateGetNanoTime()", "static Err privateListFiles");
    String nativeSource = Files.readString(vmRoot.resolve("src/nm/lang/System.c"));
    String cmake = Files.readString(vmRoot.resolve("CMakeLists.txt"));
    String vcproj = Files.readString(vmRoot.resolve("vc2008/TCVM.vcproj"));

    assertTrue(posixNanoTime.contains("clock_gettime(CLOCK_MONOTONIC"));
    assertFalse(posixNanoTime.contains("gettimeofday"));
    assertTrue(posix.contains("mach_absolute_time"));
    assertTrue(posix.contains("mach_timebase_info"));
    assertTrue(posixNanoTime.contains("pthread_once"));
    assertTrue(posixNanoTime.contains("wholeTicks = ticks / nanoTimeTimebase.denom"));
    assertTrue(posixNanoTime.contains("remainderTicks = ticks % nanoTimeTimebase.denom"));

    assertTrue(winNanoTime.contains("QueryPerformanceFrequency"));
    assertTrue(winNanoTime.contains("QueryPerformanceCounter"));
    assertTrue(winNanoTime.contains("count / frequencyValueUnsigned"));
    assertTrue(winNanoTime.contains("remainder * 1000000000ULL"));
    assertTrue(winNanoTime.contains("privateGetTimeStamp()"));
    assertTrue(winNanoTime.contains("1000000LL"));
    assertFalse(winNanoTime.contains("__int128"));

    assertTrue(nativeSource.contains("p->retL = getNanoTime();"));
    assertTrue(cmake.contains("${TC_SRCDIR}/nm/lang/System.c"));
    assertTrue(vcproj.contains("..\\..\\src\\nm\\lang\\System.c"));
  }

  @Test
  void converterEmitsJavaSystemNanoTimeCallWithLongReturn() throws Exception {
    GlobalConstantPool.init();
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    MethodDeclarationResolver.beginConversionRun();

    MethodDeclarationResolver.Resolution resolution = MethodDeclarationResolver.resolve(
        "java/lang/System", "nanoTime", "()J");
    assertEquals("java/lang/System", resolution.declarationOwner);
    assertTrue(resolution.deviceClassFound);
    assertTrue(resolution.deviceMemberFound);

    TCClass converted = new J2TC(new JavaClass(callerClass(), false, true), true).converted;
    TCMethod read = findMethod(converted, "read");
    Call call = findCall(read);
    assertEquals("java/lang/System|nanoTime()", GlobalConstantPool.getMtdName(call.sym));
    assertEquals(TCConstants.type_Long, call.retOrParamType);
    assertTrue(call.isStatic);
  }

  private static TCMethod findMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) {
        return method;
      }
    }
    throw new AssertionError("Converted method not found: " + name);
  }

  private static Call findCall(TCMethod method) {
    for (int i = 0; i < method.insts.size(); i++) {
      Instruction instruction = (Instruction) method.insts.items[i];
      if (instruction instanceof Call) {
        return (Call) instruction;
      }
    }
    throw new AssertionError("Converted System.nanoTime call not found");
  }

  private static String between(String source, String start, String end) {
    int startIndex = source.indexOf(start);
    int endIndex = source.indexOf(end, startIndex + start.length());
    if (startIndex < 0 || endIndex < 0) {
      throw new AssertionError("Could not isolate source section: " + start);
    }
    return source.substring(startIndex, endIndex);
  }

  private static byte[] callerClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/SystemNanoTimeCaller", null,
        "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "read", "()J", null, null);
    method.visitCode();
    method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
    method.visitInsn(Opcodes.LRETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }
}
