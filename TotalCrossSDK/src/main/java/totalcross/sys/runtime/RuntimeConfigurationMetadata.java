// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

/** Internal versioned wire codec shared by the converter and runtime startup. */
public final class RuntimeConfigurationMetadata {
  private static final int MAGIC = 0x54435243;
  private static final int VERSION = 2;
  private static final int LEGACY_VERSION = 1;
  private static final int DIMENSION_MASK = 0x0f;

  private static final int PLATFORM = 1;
  private static final int FAMILY = 2;
  private static final int BACKEND = 3;
  private static final int ARCHITECTURE = 4;

  private static final Class<?>[] DIMENSIONS = {
      Platform.class, RuntimeFamily.class, GraphicsBackend.class, Architecture.class
  };

  private RuntimeConfigurationMetadata() {
  }

  /**
   * Encodes selectors for a TCZ entry, restricting each selector clause to
   * deployment target tuples. Empty target lists mean that no target facts are
   * known. A {@code null} selector list means the application did not declare
   * runtime configuration and produces no metadata.
   */
  public static byte[] encodeForDeployment(List<RuntimeSelector> selectors, List<DeploymentTarget> targets) {
    if (selectors == null) {
      return null;
    }
    List<RuntimeSelector.DeploymentTarget> selectorTargets = selectorTargets(targets);
    List<RuntimeSelector> retained = new ArrayList<RuntimeSelector>(selectors.size());
    for (RuntimeSelector selector : selectors) {
      if (selector == null) {
        throw new IllegalArgumentException("runtime configuration selectors cannot contain null");
      }
      RuntimeSelector restricted = selector.restrictToDeploymentTargets(selectorTargets);
      if (restricted != null) {
        retained.add(restricted);
      }
    }
    return encode(retained);
  }

  /** Encodes one selector through the common pruning and selector wire format, or returns {@code null} if pruned. */
  static byte[] encodeSingleSelectorForDeployment(RuntimeSelector selector, List<DeploymentTarget> targets) {
    if (selector == null) {
      throw new IllegalArgumentException("runtime configuration selector is required");
    }
    RuntimeSelector retained = selector.restrictToDeploymentTargets(selectorTargets(targets));
    return retained == null ? null : encode(Collections.singletonList(retained));
  }

  private static List<RuntimeSelector.DeploymentTarget> selectorTargets(List<DeploymentTarget> targets) {
    if (targets == null) {
      return null;
    }
    List<RuntimeSelector.DeploymentTarget> selectorTargets = new ArrayList<RuntimeSelector.DeploymentTarget>(targets.size());
    for (DeploymentTarget target : targets) {
      if (target == null) {
        throw new IllegalArgumentException("deployment targets cannot contain null");
      }
      selectorTargets.add(new RuntimeSelector.DeploymentTarget(target.platform, target.family, target.architecture));
    }
    return selectorTargets;
  }

  /** Decodes a versioned selector payload or throws when it is malformed. */
  public static List<RuntimeSelector> decode(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("runtime configuration metadata is empty");
    }
    MetadataReader input = new MetadataReader(bytes);
    if (input.readInt() != MAGIC) {
      throw invalid("unexpected metadata signature");
    }
    int version = input.readUnsignedByte();
    if (version != VERSION && version != LEGACY_VERSION) {
      throw invalid("unsupported metadata version " + version);
    }
    int count = input.readUnsignedShort();
    List<RuntimeSelector> selectors = new ArrayList<RuntimeSelector>(count);
    for (int i = 0; i < count; i++) {
      selectors.add(readSelector(input, i, version));
    }
    if (input.hasRemaining()) {
      throw invalid("contains trailing bytes");
    }
    return Collections.unmodifiableList(selectors);
  }

  private static byte[] encode(List<RuntimeSelector> selectors) {
    if (selectors.size() > 0xffff) {
      throw new IllegalArgumentException("runtime configuration has too many rules");
    }
    MetadataWriter output = new MetadataWriter();
    output.writeInt(MAGIC);
    output.writeByte(VERSION);
    output.writeShort(selectors.size());
    for (RuntimeSelector selector : selectors) {
      List<RuntimeSelector.MetadataClause> clauses = new ArrayList<RuntimeSelector.MetadataClause>(
          selector.clausesForMetadata());
      if (clauses.size() > 0xffff) {
        throw new IllegalArgumentException("runtime selector has too many alternatives");
      }
      Collections.sort(clauses, CLAUSE_ORDER);
      output.writeShort(clauses.size());
      for (RuntimeSelector.MetadataClause clause : clauses) {
        int dimensions = 0;
        for (Class<?> dimension : DIMENSIONS) {
          if (clause.conditions.containsKey(dimension)) {
            dimensions++;
          }
        }
        int dynamicMask = dimensionMask(clause.conditions.keySet());
        int staticMask = clause.staticDimensionMask & ~dynamicMask;
        output.writeByte(dimensions);
        output.writeByte(staticMask);
        for (Class<?> dimension : DIMENSIONS) {
          Set<Enum<?>> values = clause.conditions.get(dimension);
          if (values != null) {
            writeDimension(output, dimension, values);
          }
        }
        if (clause.conditions.size() != dimensions) {
          throw new IllegalArgumentException("runtime selector contains an unknown dimension");
        }
      }
    }
    return output.toByteArray();
  }

  private static RuntimeSelector readSelector(MetadataReader input, int ruleIndex, int version) {
    int clauseCount = input.readUnsignedShort();
    List<RuntimeSelector.MetadataClause> clauses = new ArrayList<RuntimeSelector.MetadataClause>(clauseCount);
    for (int clauseIndex = 0; clauseIndex < clauseCount; clauseIndex++) {
      int dimensionCount = input.readUnsignedByte();
      int staticMask = version >= VERSION ? input.readUnsignedByte() : 0;
      if ((staticMask & ~DIMENSION_MASK) != 0) {
        throw invalid("rule " + ruleIndex + " has an invalid static dimension mask");
      }
      Map<Class<? extends Enum<?>>, Set<Enum<?>>> conditions = new HashMap<Class<? extends Enum<?>>, Set<Enum<?>>>();
      for (int dimensionIndex = 0; dimensionIndex < dimensionCount; dimensionIndex++) {
        int tag = input.readUnsignedByte();
        Class<? extends Enum<?>> dimension = dimensionForTag(tag);
        int dimensionBit = dimensionBit(dimension);
        if ((staticMask & dimensionBit) != 0) {
          throw invalid("rule " + ruleIndex + " encodes a dimension as both static and dynamic");
        }
        if (conditions.containsKey(dimension)) {
          throw invalid("rule " + ruleIndex + " repeats dimension tag " + tag);
        }
        int valueCount = input.readUnsignedByte();
        if (valueCount == 0) {
          throw invalid("rule " + ruleIndex + " has an empty dimension");
        }
        Set<Enum<?>> values = new HashSet<Enum<?>>();
        for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
          values.add(valueForId(dimension, input.readUnsignedByte()));
        }
        conditions.put(dimension, values);
      }
      clauses.add(new RuntimeSelector.MetadataClause(conditions, staticMask));
    }
    return RuntimeSelector.fromMetadataClauses(clauses);
  }

  private static void writeDimension(MetadataWriter output, Class<?> dimension, Set<Enum<?>> values) {
    List<Enum<?>> sorted = new ArrayList<Enum<?>>(values);
    Collections.sort(sorted, new Comparator<Enum<?>>() {
      @Override
      public int compare(Enum<?> left, Enum<?> right) {
        return valueId(dimension, left) - valueId(dimension, right);
      }
    });
    if (sorted.isEmpty() || sorted.size() > 0xff) {
      throw new IllegalArgumentException("runtime selector has an invalid number of values");
    }
    output.writeByte(dimensionTag(dimension));
    output.writeByte(sorted.size());
    for (Enum<?> value : sorted) {
      output.writeByte(valueId(dimension, value));
    }
  }

  private static int dimensionBit(Class<?> dimension) {
    for (int i = 0; i < DIMENSIONS.length; i++) {
      if (DIMENSIONS[i] == dimension) {
        return 1 << i;
      }
    }
    return 0;
  }

  private static int dimensionMask(Iterable<? extends Class<?>> dimensions) {
    int mask = 0;
    for (Class<?> dimension : dimensions) {
      mask |= dimensionBit(dimension);
    }
    return mask;
  }

  private static int dimensionTag(Class<?> dimension) {
    if (dimension == Platform.class) {
      return PLATFORM;
    }
    if (dimension == RuntimeFamily.class) {
      return FAMILY;
    }
    if (dimension == GraphicsBackend.class) {
      return BACKEND;
    }
    if (dimension == Architecture.class) {
      return ARCHITECTURE;
    }
    throw new IllegalArgumentException("unknown runtime configuration dimension " + dimension.getName());
  }

  private static Class<? extends Enum<?>> dimensionForTag(int tag) {
    switch (tag) {
    case PLATFORM:
      return Platform.class;
    case FAMILY:
      return RuntimeFamily.class;
    case BACKEND:
      return GraphicsBackend.class;
    case ARCHITECTURE:
      return Architecture.class;
    default:
      throw invalid("unknown dimension tag " + tag);
    }
  }

  private static int valueId(Class<?> dimension, Enum<?> value) {
    if (dimension == Platform.class) {
      switch ((Platform) value) {
      case WINDOWS:
        return 1;
      case MACOS:
        return 2;
      case LINUX:
        return 3;
      case ANDROID:
        return 4;
      case IOS:
        return 5;
      }
    } else if (dimension == RuntimeFamily.class) {
      switch ((RuntimeFamily) value) {
      case DESKTOP:
        return 1;
      case MOBILE:
        return 2;
      case EMBEDDED:
        return 3;
      }
    } else if (dimension == GraphicsBackend.class) {
      switch ((GraphicsBackend) value) {
      case RASTER:
        return 1;
      case GPU:
        return 2;
      }
    } else if (dimension == Architecture.class) {
      switch ((Architecture) value) {
      case X86:
        return 1;
      case X86_64:
        return 2;
      case ARM32:
        return 3;
      case ARM64:
        return 4;
      }
    }
    throw new IllegalArgumentException("unknown runtime configuration value " + value);
  }

  @SuppressWarnings("unchecked")
  private static Enum<?> valueForId(Class<? extends Enum<?>> dimension, int id) {
    if (dimension == Platform.class) {
      switch (id) {
      case 1:
        return Platform.WINDOWS;
      case 2:
        return Platform.MACOS;
      case 3:
        return Platform.LINUX;
      case 4:
        return Platform.ANDROID;
      case 5:
        return Platform.IOS;
      }
    } else if (dimension == RuntimeFamily.class) {
      switch (id) {
      case 1:
        return RuntimeFamily.DESKTOP;
      case 2:
        return RuntimeFamily.MOBILE;
      case 3:
        return RuntimeFamily.EMBEDDED;
      }
    } else if (dimension == GraphicsBackend.class) {
      switch (id) {
      case 1:
        return GraphicsBackend.RASTER;
      case 2:
        return GraphicsBackend.GPU;
      }
    } else if (dimension == Architecture.class) {
      switch (id) {
      case 1:
        return Architecture.X86;
      case 2:
        return Architecture.X86_64;
      case 3:
        return Architecture.ARM32;
      case 4:
        return Architecture.ARM64;
      }
    }
    throw invalid("unknown value tag " + id + " for " + dimension.getSimpleName());
  }

  private static IllegalArgumentException invalid(String message) {
    return new IllegalArgumentException("Invalid runtime configuration metadata: " + message);
  }

  /** A deployment target with null values for dimensions the target does not fix. */
  public static final class DeploymentTarget {
    private final Platform platform;
    private final RuntimeFamily family;
    private final Architecture architecture;

    public DeploymentTarget(Platform platform, RuntimeFamily family, Architecture architecture) {
      this.platform = platform;
      this.family = family;
      this.architecture = architecture;
    }

    Enum<?> valueFor(Class<?> dimension) {
      if (dimension == Platform.class) {
        return platform;
      }
      if (dimension == RuntimeFamily.class) {
        return family;
      }
      if (dimension == Architecture.class) {
        return architecture;
      }
      return null;
    }
  }

  private static final class MetadataReader {
    private final byte[] bytes;
    private int position;

    MetadataReader(byte[] bytes) {
      this.bytes = bytes;
    }

    int readUnsignedByte() {
      require(1);
      return bytes[position++] & 0xff;
    }

    int readUnsignedShort() {
      return (readUnsignedByte() << 8) | readUnsignedByte();
    }

    int readInt() {
      return (readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8)
          | readUnsignedByte();
    }

    boolean hasRemaining() {
      return position < bytes.length;
    }

    private void require(int count) {
      if (count > bytes.length - position) {
        throw invalid("truncated payload");
      }
    }
  }

  private static final class MetadataWriter {
    private byte[] bytes = new byte[32];
    private int size;

    void writeByte(int value) {
      ensureCapacity(1);
      bytes[size++] = (byte) value;
    }

    void writeShort(int value) {
      writeByte(value >>> 8);
      writeByte(value);
    }

    void writeInt(int value) {
      writeByte(value >>> 24);
      writeByte(value >>> 16);
      writeByte(value >>> 8);
      writeByte(value);
    }

    byte[] toByteArray() {
      byte[] result = new byte[size];
      System.arraycopy(bytes, 0, result, 0, size);
      return result;
    }

    private void ensureCapacity(int extra) {
      long requiredLong = (long) size + extra;
      if (requiredLong > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("runtime configuration metadata is too large");
      }
      int required = (int) requiredLong;
      if (required <= bytes.length) {
        return;
      }
      int capacity = bytes.length;
      while (capacity < required) {
        capacity = capacity > Integer.MAX_VALUE / 2 ? required : capacity * 2;
      }
      byte[] expanded = new byte[capacity];
      System.arraycopy(bytes, 0, expanded, 0, size);
      bytes = expanded;
    }
  }

  private static final Comparator<RuntimeSelector.MetadataClause> CLAUSE_ORDER = new Comparator<RuntimeSelector.MetadataClause>() {
    @Override
    public int compare(RuntimeSelector.MetadataClause leftClause, RuntimeSelector.MetadataClause rightClause) {
      Map<Class<? extends Enum<?>>, Set<Enum<?>>> left = leftClause.conditions;
      Map<Class<? extends Enum<?>>, Set<Enum<?>>> right = rightClause.conditions;
      for (Class<?> dimension : DIMENSIONS) {
        Set<Enum<?>> leftValues = left.get(dimension);
        Set<Enum<?>> rightValues = right.get(dimension);
        if (leftValues == null || rightValues == null) {
          if (leftValues != rightValues) {
            return leftValues == null ? -1 : 1;
          }
          continue;
        }
        int valuesOrder = compareValues(dimension, leftValues, rightValues);
        if (valuesOrder != 0) {
          return valuesOrder;
        }
      }
      return leftClause.staticDimensionMask - rightClause.staticDimensionMask;
    }

    private int compareValues(Class<?> dimension, Set<Enum<?>> left, Set<Enum<?>> right) {
      List<Enum<?>> leftSorted = new ArrayList<Enum<?>>(left);
      List<Enum<?>> rightSorted = new ArrayList<Enum<?>>(right);
      Comparator<Enum<?>> order = new Comparator<Enum<?>>() {
        @Override
        public int compare(Enum<?> first, Enum<?> second) {
          return valueId(dimension, first) - valueId(dimension, second);
        }
      };
      Collections.sort(leftSorted, order);
      Collections.sort(rightSorted, order);
      int size = Math.min(leftSorted.size(), rightSorted.size());
      for (int i = 0; i < size; i++) {
        int difference = valueId(dimension, leftSorted.get(i)) - valueId(dimension, rightSorted.get(i));
        if (difference != 0) {
          return difference;
        }
      }
      return leftSorted.size() - rightSorted.size();
    }
  };
}
