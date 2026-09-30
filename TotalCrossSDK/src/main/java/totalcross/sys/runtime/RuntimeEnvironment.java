// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Settings;

/**
 * Immutable facts about one snapshot of the current runtime environment.
 *
 * <p>A dimension is {@code null} when its value cannot be proven. The graphics
 * backend can be {@code null} before graphics initialization has finalized it.
 * Take a new snapshot after startup to observe the finalized backend.
 */
public final class RuntimeEnvironment {
  private static final int ARCHITECTURE_UNAVAILABLE = 0;
  private static final int ARCHITECTURE_X86 = 1;
  private static final int ARCHITECTURE_X86_64 = 2;
  private static final int ARCHITECTURE_ARM32 = 3;
  private static final int ARCHITECTURE_ARM64 = 4;
  private static final int PLATFORM_UNAVAILABLE = 0;
  private static final int PLATFORM_WINDOWS = 1;
  private static final int PLATFORM_MACOS = 2;
  private static final int PLATFORM_LINUX = 3;
  private static final int PLATFORM_ANDROID = 4;
  private static final int PLATFORM_IOS = 5;
  private static final int BACKEND_UNRESOLVED = 0;
  private static final int BACKEND_RASTER = 1;
  private static final int BACKEND_GPU = 2;

  private final Platform platform;
  private final RuntimeFamily runtimeFamily;
  private final GraphicsBackend graphicsBackend;
  private final Architecture architecture;
  private static volatile String simulatorOsName;
  private static volatile String simulatorOsArch;
  private static volatile GraphicsBackend simulatorGraphicsBackend;

  private RuntimeEnvironment(Platform platform, RuntimeFamily runtimeFamily, GraphicsBackend graphicsBackend,
      Architecture architecture) {
    this.platform = platform;
    this.runtimeFamily = runtimeFamily;
    this.graphicsBackend = graphicsBackend;
    this.architecture = architecture;
  }

  /** Returns a snapshot of the current runtime facts. */
  public static RuntimeEnvironment current() {
    boolean onJavaSE = Settings.onJavaSE;
    String osName = onJavaSE ? simulatorOsName : null;
    String osArch = onJavaSE ? simulatorOsArch : null;
    int architectureCode = onJavaSE ? ARCHITECTURE_UNAVAILABLE : nativeArchitectureCode();
    int platformCode = onJavaSE ? PLATFORM_UNAVAILABLE : nativePlatformCode();
    int backendCode = onJavaSE ? backendCodeFrom(simulatorGraphicsBackend) : nativeGraphicsBackendCode();
    return fromRuntimeSettings(Settings.platform, onJavaSE, osName, osArch, platformCode, architectureCode,
        backendCode);
  }

  static void setSimulatorHostProperties(String osName, String osArch) {
    simulatorOsName = osName;
    simulatorOsArch = osArch;
  }

  static void resetSimulatorGraphicsBackend() {
    simulatorGraphicsBackend = null;
  }

  static void finalizeSimulatorGraphicsBackend(GraphicsBackend backend) {
    if (backend == null) {
      throw new IllegalArgumentException("simulator graphics backend cannot be null");
    }
    if (simulatorGraphicsBackend != null) {
      throw new IllegalStateException("simulator graphics backend is already finalized");
    }
    simulatorGraphicsBackend = backend;
  }

  /** Returns the operating-system platform, or {@code null} when it is unavailable. */
  public Platform platform() {
    return platform;
  }

  /** Returns the broad runtime family, or {@code null} when it cannot be proven. */
  public RuntimeFamily runtimeFamily() {
    return runtimeFamily;
  }

  /**
   * Returns the graphics backend, or {@code null} until runtime graphics
   * initialization has finalized it.
   */
  public GraphicsBackend graphicsBackend() {
    return graphicsBackend;
  }

  /** Returns whether graphics initialization has finalized the backend. */
  public boolean isGraphicsBackendFinalized() {
    return graphicsBackend != null;
  }

  /** Returns the processor architecture, or {@code null} when it is unavailable. */
  public Architecture architecture() {
    return architecture;
  }

  static RuntimeEnvironment fromRuntimeSettings(String runtimePlatform, boolean onJavaSE, String osName,
      String osArch, int platformCode, int architectureCode, int backendCode) {
    Platform platform = onJavaSE ? platformFromHost(osName) : platformFromCode(platformCode);
    RuntimeFamily family = familyFrom(platform, runtimePlatform, onJavaSE);
    Architecture architecture = onJavaSE ? architectureFromName(osArch) : architectureFromCode(architectureCode);
    GraphicsBackend backend = backendFromCode(backendCode);
    return new RuntimeEnvironment(platform, family, backend, architecture);
  }

  Enum<?> valueFor(Class<? extends Enum<?>> dimension) {
    if (dimension == Platform.class) {
      return platform;
    }
    if (dimension == RuntimeFamily.class) {
      return runtimeFamily;
    }
    if (dimension == GraphicsBackend.class) {
      return graphicsBackend;
    }
    if (dimension == Architecture.class) {
      return architecture;
    }
    return null;
  }

  private static Platform platformFromHost(String osName) {
    String os = normalize(osName);
    if (os.indexOf("windows") >= 0) {
      return Platform.WINDOWS;
    }
    if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
      return Platform.MACOS;
    }
    if (os.indexOf("linux") >= 0) {
      return Platform.LINUX;
    }
    return null;
  }

  private static Platform platformFromCode(int code) {
    switch (code) {
    case PLATFORM_WINDOWS:
      return Platform.WINDOWS;
    case PLATFORM_MACOS:
      return Platform.MACOS;
    case PLATFORM_LINUX:
      return Platform.LINUX;
    case PLATFORM_ANDROID:
      return Platform.ANDROID;
    case PLATFORM_IOS:
      return Platform.IOS;
    default:
      return null;
    }
  }

  private static RuntimeFamily familyFrom(Platform platform, String runtimePlatform, boolean onJavaSE) {
    String rawPlatform = normalize(runtimePlatform);
    if (platform == Platform.ANDROID || platform == Platform.IOS || "android".equals(rawPlatform)
        || "iphone".equals(rawPlatform) || "ipad".equals(rawPlatform)) {
      return RuntimeFamily.MOBILE;
    }
    if (onJavaSE || platform == Platform.MACOS || "macos".equals(rawPlatform) || "mac".equals(rawPlatform)
        || "darwin".equals(rawPlatform)) {
      return RuntimeFamily.DESKTOP;
    }
    if ("win32".equals(rawPlatform)) {
      return RuntimeFamily.DESKTOP;
    }
    if ("windowsce".equals(rawPlatform) || "pocketpc".equals(rawPlatform) || "windowsmobile".equals(rawPlatform)
        || "windowsphone".equals(rawPlatform) || "linux_arm".equals(rawPlatform)) {
      return RuntimeFamily.EMBEDDED;
    }
    if ("linux".equals(rawPlatform)) {
      return RuntimeFamily.DESKTOP;
    }
    return null;
  }

  private static GraphicsBackend backendFromCode(int code) {
    if (code == BACKEND_GPU) {
      return GraphicsBackend.GPU;
    }
    if (code == BACKEND_RASTER) {
      return GraphicsBackend.RASTER;
    }
    return null;
  }

  private static int backendCodeFrom(GraphicsBackend backend) {
    if (backend == GraphicsBackend.GPU) {
      return BACKEND_GPU;
    }
    if (backend == GraphicsBackend.RASTER) {
      return BACKEND_RASTER;
    }
    return BACKEND_UNRESOLVED;
  }

  static Architecture architectureFromName(String name) {
    String architecture = normalize(name);
    if ("x86_64".equals(architecture) || "amd64".equals(architecture) || "x64".equals(architecture)) {
      return Architecture.X86_64;
    }
    if ("x86".equals(architecture) || "i386".equals(architecture) || "i486".equals(architecture)
        || "i586".equals(architecture) || "i686".equals(architecture) || "ia32".equals(architecture)) {
      return Architecture.X86;
    }
    if ("aarch64".equals(architecture) || "arm64".equals(architecture) || "arm64_v8a".equals(architecture)
        || "arm64e".equals(architecture)) {
      return Architecture.ARM64;
    }
    if ("arm".equals(architecture) || "arm32".equals(architecture) || hasVersionedPrefix(architecture, "armv")
        || "armeabi".equals(architecture) || architecture.startsWith("armeabi_") || "armhf".equals(architecture)
        || "armel".equals(architecture) || "thumb".equals(architecture) || "thumb2".equals(architecture)
        || hasVersionedPrefix(architecture, "thumbv")) {
      return Architecture.ARM32;
    }
    return null;
  }

  private static boolean hasVersionedPrefix(String value, String prefix) {
    return value.startsWith(prefix) && value.length() > prefix.length()
        && value.charAt(prefix.length()) >= '0' && value.charAt(prefix.length()) <= '9';
  }

  private static Architecture architectureFromCode(int code) {
    switch (code) {
    case ARCHITECTURE_X86:
      return Architecture.X86;
    case ARCHITECTURE_X86_64:
      return Architecture.X86_64;
    case ARCHITECTURE_ARM32:
      return Architecture.ARM32;
    case ARCHITECTURE_ARM64:
      return Architecture.ARM64;
    default:
      return null;
    }
  }

  @ReplacedByNativeOnDeploy
  private static int nativeArchitectureCode() {
    return architectureCodeFromName(System.getProperty("os.arch"));
  }

  @ReplacedByNativeOnDeploy
  private static int nativePlatformCode() {
    String platform = normalize(Settings.platform);
    if ("win32".equals(platform) || "windowsce".equals(platform) || "pocketpc".equals(platform)
        || "windowsmobile".equals(platform) || "windowsphone".equals(platform)) {
      return PLATFORM_WINDOWS;
    }
    if ("macos".equals(platform) || "mac".equals(platform) || "darwin".equals(platform)) {
      return PLATFORM_MACOS;
    }
    if ("linux".equals(platform) || "linux_arm".equals(platform)) {
      return PLATFORM_LINUX;
    }
    if ("android".equals(platform)) {
      return PLATFORM_ANDROID;
    }
    if ("iphone".equals(platform) || "ipad".equals(platform)) {
      return PLATFORM_IOS;
    }
    return PLATFORM_UNAVAILABLE;
  }

  @ReplacedByNativeOnDeploy
  private static int nativeGraphicsBackendCode() {
    return BACKEND_UNRESOLVED;
  }

  private static int architectureCodeFromName(String name) {
    Architecture architecture = architectureFromName(name);
    if (architecture == null) {
      return ARCHITECTURE_UNAVAILABLE;
    }
    switch (architecture) {
    case X86:
      return ARCHITECTURE_X86;
    case X86_64:
      return ARCHITECTURE_X86_64;
    case ARM32:
      return ARCHITECTURE_ARM32;
    case ARM64:
      return ARCHITECTURE_ARM64;
    default:
      return ARCHITECTURE_UNAVAILABLE;
    }
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    int start = 0;
    int end = value.length();
    while (start < end && value.charAt(start) <= ' ') {
      start++;
    }
    while (end > start && value.charAt(end - 1) <= ' ') {
      end--;
    }
    StringBuilder normalized = new StringBuilder(end - start);
    for (int i = start; i < end; i++) {
      char character = value.charAt(i);
      if (character >= 'A' && character <= 'Z') {
        character = (char) (character + ('a' - 'A'));
      }
      if (character == '-') {
        character = '_';
      }
      normalized.append(character);
    }
    return normalized.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof RuntimeEnvironment)) {
      return false;
    }
    RuntimeEnvironment that = (RuntimeEnvironment) other;
    return platform == that.platform && runtimeFamily == that.runtimeFamily && graphicsBackend == that.graphicsBackend
        && architecture == that.architecture;
  }

  @Override
  public int hashCode() {
    int result = platform == null ? 0 : platform.hashCode();
    result = 31 * result + (runtimeFamily == null ? 0 : runtimeFamily.hashCode());
    result = 31 * result + (graphicsBackend == null ? 0 : graphicsBackend.hashCode());
    result = 31 * result + (architecture == null ? 0 : architecture.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "RuntimeEnvironment[platform=" + platform + ", runtimeFamily=" + runtimeFamily + ", graphicsBackend="
        + graphicsBackend + ", architecture=" + architecture + "]";
  }
}
