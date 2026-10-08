// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.tclass.TClassConstants;
import totalcross.sys.Convert;
import totalcross.util.Hashtable;

/** Resolves source declaration owners from conversion-owned and device-owned models. */
public final class MethodDeclarationResolver {
  private static final Map<String, JavaClass> programClasses = new HashMap<String, JavaClass>();
  private static final Map<String, JavaClass> deviceClasses = new HashMap<String, JavaClass>();
  private static final Map<String, String[]> explicitJavaSupers = new HashMap<String, String[]>();

  static {
    explicitJavaSupers.put("java/util/Properties", new String[] { "java/util/Hashtable" });
  }

  private MethodDeclarationResolver() {
  }

  public static void beginConversionRun() {
    programClasses.clear();
    deviceClasses.clear();
  }

  public static void registerProgramClass(JavaClass type) {
    register(type.originalClassName, type);
    register(type.className, type);
  }

  public static void registerDeviceClass(JavaClass type) {
    String owner = deployedOwner(type.originalClassName);
    if (owner != null) {
      deviceClasses.put(owner, type);
    }
  }

  public static Resolution resolve(String symbolicOwner, String name, String descriptor) {
    String owner = slash(symbolicOwner);
    if (isConstructor(name)) {
      DeviceResolution device = resolveDevice(owner, name, new DescriptorMatcher(descriptor), false);
      return new Resolution(owner, owner, device.deviceOwner, device.classFound, device.memberFound);
    }
    String programOwner = findProgramDeclaration(owner, name, descriptor, new HashSet<String>());
    if (programOwner != null) {
      return new Resolution(owner, programOwner, null, false, false);
    }
    DeviceResolution device = resolveDevice(owner, name, new DescriptorMatcher(descriptor), true);
    return new Resolution(owner, device.declarationOwner, device.deviceOwner, device.classFound, device.memberFound);
  }

  public static Resolution resolveDeviceCall(String symbolicOwner, String name, int[] tcParameters) {
    String owner = slash(symbolicOwner);
    DeviceResolution device = resolveDevice(owner, name, new TcParameterMatcher(tcParameters), !isConstructor(name));
    String declarationOwner = isConstructor(name) ? owner : device.declarationOwner;
    return new Resolution(owner, declarationOwner, device.deviceOwner, device.classFound, device.memberFound);
  }

  private static DeviceResolution resolveDevice(String owner, String name, ParameterMatcher matcher,
      boolean includeExplicitSupers) {
    boolean classFound = false;
    String[] candidates = includeExplicitSupers ? candidates(owner) : new String[] { owner };
    for (String candidate : candidates) {
      JavaClass compiledDeviceClass = deviceClasses.get(candidate);
      if (compiledDeviceClass != null) {
        classFound = true;
        String declaration =
            findMemberDeclaration(compiledDeviceClass, name, matcher, new HashSet<String>());
        if (declaration != null) {
          return new DeviceResolution(
              declaration, compiledDeviceClass.originalClassName, true, true);
        }
        continue;
      }

      Class<?> deviceClass = findDeviceClass(candidate);
      if (deviceClass == null) continue;
      classFound = true;
      Class<?> declaration = findMemberDeclaration(deviceClass, name, matcher, new HashSet<String>());
      if (declaration != null) {
        return new DeviceResolution(DeviceTypeMapping.javaFacingOwner(declaration, candidate),
            deviceClass.getName(), true, true);
      }
    }
    return new DeviceResolution(null, null, classFound, false);
  }

  private static String[] candidates(String owner) {
    String[] supers = explicitJavaSupers.get(owner);
    if (supers == null) return new String[] { owner };
    String[] result = new String[supers.length + 1];
    result[0] = owner;
    System.arraycopy(supers, 0, result, 1, supers.length);
    return result;
  }

  private static Class<?> findMemberDeclaration(Class<?> deviceClass, String name, ParameterMatcher matcher,
      Set<String> visited) {
    if (deviceClass == null || !visited.add(deviceClass.getName())) return null;
    if (isConstructor(name)) {
      for (Constructor<?> constructor : deviceClass.getDeclaredConstructors()) {
        if (matcher.matches(constructor.getParameterTypes())) return deviceClass;
      }
      return null;
    }
    Method[] values = deviceClass.getDeclaredMethods();
    Hashtable names = new Hashtable(values.length);
    for (Object value : values) names.put(((Method) value).getName(), "");
    for (Object value : values) {
      Method method = (Method) value;
      String candidateName = method.getName();
      if (candidateName.endsWith("4D")) {
        candidateName = candidateName.substring(0, candidateName.length() - 2);
      } else if (names.exists(candidateName + "4D")) {
        continue;
      }
      if (candidateName.equals(name) && matcher.matches(method.getParameterTypes())) {
        return method.getDeclaringClass();
      }
    }
    Class<?> declaration;
    for (Class<?> mappedSuper : mappedHierarchyTypes(deviceClass.getSuperclass())) {
      declaration = findMemberDeclaration(mappedSuper, name, matcher, visited);
      if (declaration != null) return declaration;
    }
    for (Class<?> iface : deviceClass.getInterfaces()) {
      for (Class<?> mappedInterface : mappedHierarchyTypes(iface)) {
        declaration = findMemberDeclaration(mappedInterface, name, matcher, visited);
        if (declaration != null) return declaration;
      }
    }
    return null;
  }

  private static String findMemberDeclaration(JavaClass deviceClass, String name,
      ParameterMatcher matcher, Set<String> visited) {
    if (deviceClass == null) return null;
    String owner = deployedOwner(deviceClass.originalClassName);
    if (owner == null || !visited.add(owner)) return null;

    if (isConstructor(name)) {
      if (deviceClass.methods != null) {
        for (JavaMethod method : deviceClass.methods) {
          if ("<init>".equals(method.name) && matcher.matches(method.params)) {
            return owner;
          }
        }
      }
      return null;
    }

    Set<String> names = new HashSet<String>();
    if (deviceClass.methods != null) {
      for (JavaMethod method : deviceClass.methods) names.add(method.name);
      for (JavaMethod method : deviceClass.methods) {
        String candidateName = method.name;
        if (candidateName.endsWith("4D")) {
          candidateName = candidateName.substring(0, candidateName.length() - 2);
        } else if (names.contains(candidateName + "4D")) {
          continue;
        }
        if (candidateName.equals(name) && matcher.matches(method.params)) {
          return owner;
        }
      }
    }

    String declaration = findMemberDeclaration(
        deviceClasses.get(deployedOwner(deviceClass.originalSuperClass)),
        name, matcher, visited);
    if (declaration != null) return declaration;

    if (deviceClass.originalInterfaces != null) {
      for (String iface : deviceClass.originalInterfaces) {
        declaration = findMemberDeclaration(
            deviceClasses.get(deployedOwner(iface)), name, matcher, visited);
        if (declaration != null) return declaration;
      }
    }
    return null;
  }

  private static Class<?>[] mappedHierarchyTypes(Class<?> type) {
    return DeviceTypeMapping.mappedHierarchyTypes(type);
  }

  private static Class<?> findDeviceClass(String javaOwner) {
    Class<?>[] candidates = DeviceTypeMapping.findDeviceClasses(javaOwner);
    return candidates.length == 0 ? null : candidates[0];
  }

  private static String findProgramDeclaration(String owner, String name, String descriptor, Set<String> visited) {
    if (owner == null || !visited.add(owner)) return null;
    JavaClass type = programClasses.get(owner);
    if (type == null) return null;
    if (type.methods != null) {
      for (JavaMethod method : type.methods) {
        if (name.equals(method.name) && descriptor.equals(method.descriptor)) return originalName(type);
      }
    }
    String declaration = findProgramDeclaration(slash(type.originalSuperClass), name, descriptor, visited);
    if (declaration != null) return declaration;
    if (type.originalInterfaces != null) {
      for (String iface : type.originalInterfaces) {
        declaration = findProgramDeclaration(slash(iface), name, descriptor, visited);
        if (declaration != null) return declaration;
      }
    }
    return null;
  }

  private static String originalName(JavaClass type) {
    return slash(type.originalClassName == null ? type.className : type.originalClassName);
  }

  private static String deployedOwner(String name) {
    return name == null || name.length() == 0
        ? null : DeviceTypeMapping.deployedOwner(slash(name));
  }

  private static boolean isConstructor(String name) {
    return "<init>".equals(name) || TClassConstants.CONSTRUCTOR_NAME.equals(name);
  }

  private static String slash(String value) {
    return value == null ? null : value.replace('.', '/');
  }

  private interface ParameterMatcher {
    boolean matches(Class<?>[] deviceParameters);
    boolean matches(String[] deviceParameters);
  }

  private static final class DescriptorMatcher implements ParameterMatcher {
    private final Type[] sourceParameters;

    DescriptorMatcher(String descriptor) {
      sourceParameters = Type.getArgumentTypes(descriptor);
    }

    @Override
    public boolean matches(Class<?>[] deviceParameters) {
      if (sourceParameters.length != deviceParameters.length) return false;
      for (int i = 0; i < sourceParameters.length; i++) {
        String source = GlobalConstantPool.javaType2TCType(sourceParameters[i].getDescriptor());
        String device = GlobalConstantPool.javaType2TCType(Type.getDescriptor(deviceParameters[i]));
        if (!compatibleType(source, device)) return false;
      }
      return true;
    }

    @Override
    public boolean matches(String[] deviceParameters) {
      int deviceCount = deviceParameters == null ? 0 : deviceParameters.length;
      if (sourceParameters.length != deviceCount) return false;
      for (int i = 0; i < sourceParameters.length; i++) {
        String source = GlobalConstantPool.javaType2TCType(sourceParameters[i].getDescriptor());
        String device = GlobalConstantPool.javaType2TCType(deviceParameters[i]);
        if (!compatibleType(source, device)) return false;
      }
      return true;
    }
  }

  private static final class TcParameterMatcher implements ParameterMatcher {
    private final int[] sourceParameters;

    TcParameterMatcher(int[] sourceParameters) {
      this.sourceParameters = sourceParameters;
    }

    @Override
    public boolean matches(Class<?>[] deviceParameters) {
      int count = sourceParameters.length - 2;
      if (count != deviceParameters.length) return false;
      for (int i = 0; i < count; i++) {
        String source = GlobalConstantPool.getClassName(sourceParameters[i + 2]);
        String device = validationDeviceType(deviceParameters[i]);
        if (!compatibleType(source, device)) return false;
      }
      return true;
    }

    @Override
    public boolean matches(String[] deviceParameters) {
      int count = sourceParameters.length - 2;
      int deviceCount = deviceParameters == null ? 0 : deviceParameters.length;
      if (count != deviceCount) return false;
      for (int i = 0; i < count; i++) {
        String source = GlobalConstantPool.getClassName(sourceParameters[i + 2]);
        String device = GlobalConstantPool.javaType2TCType(deviceParameters[i]);
        if (!compatibleType(source, device)) return false;
      }
      return true;
    }
  }

  private static String validationDeviceType(Class<?> type) {
    if (!type.isArray()) return GlobalConstantPool.javaType2TCType(Type.getDescriptor(type));
    String name = type.getName();
    int dimensions = 0;
    while (name.charAt(dimensions) == '[') dimensions++;
    String component;
    switch (name.charAt(dimensions)) {
    case 'Z': component = "&b"; break;
    case 'C': component = "&C"; break;
    case 'B': component = "&B"; break;
    case 'S': component = "&S"; break;
    case 'I': component = "&I"; break;
    case 'J': component = "&L"; break;
    case 'F':
    case 'D': component = "&D"; break;
    default: component = name.substring(dimensions + 1, name.length() - 1); break;
    }
    return name.substring(0, dimensions) + component;
  }

  private static boolean compatibleType(String source, String device) {
    if (source.charAt(0) == '&' || source.charAt(0) == '[') return source.equals(device);
    String sourceMapped = java2totalcross(source);
    String deviceMapped = java2totalcross(device);
    return sourceMapped.equals(deviceMapped) || deviceMapped.equals(sourceMapped + "4D")
        || Convert.replace(deviceMapped, "4D", "").equals(sourceMapped);
  }

  private static String java2totalcross(String name) {
    return name.startsWith("java.") ? name.replace("java.", "totalcross.") : name;
  }

  private static void register(String name, JavaClass type) {
    if (name != null) programClasses.put(slash(name), type);
  }

  public static final class Resolution {
    public final String symbolicOwner;
    public final String declarationOwner;
    public final String deviceOwner;
    public final boolean deviceClassFound;
    public final boolean deviceMemberFound;

    Resolution(String symbolicOwner, String declarationOwner, String deviceOwner, boolean deviceClassFound,
        boolean deviceMemberFound) {
      this.symbolicOwner = symbolicOwner;
      this.declarationOwner = declarationOwner;
      this.deviceOwner = deviceOwner;
      this.deviceClassFound = deviceClassFound;
      this.deviceMemberFound = deviceMemberFound;
    }
  }

  private static final class DeviceResolution {
    final String declarationOwner;
    final String deviceOwner;
    final boolean classFound;
    final boolean memberFound;

    DeviceResolution(String declarationOwner, String deviceOwner, boolean classFound, boolean memberFound) {
      this.declarationOwner = declarationOwner;
      this.deviceOwner = deviceOwner;
      this.classFound = classFound;
      this.memberFound = memberFound;
    }
  }
}
