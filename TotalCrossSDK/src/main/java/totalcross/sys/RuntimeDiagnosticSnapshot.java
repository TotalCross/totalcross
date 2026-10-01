// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Immutable values collected at one point in time. */
public final class RuntimeDiagnosticSnapshot {
  /** Broad observation domains; metric keys and identifiers remain internal. */
  public enum Domain {
    RUNTIME,
    IMAGE,
    SCHEDULING
  }

  /** The interpretation of a numeric observation. */
  public enum Kind {
    COUNTER,
    GAUGE,
    TIMER
  }

  private static final RuntimeDiagnosticSnapshot EMPTY =
      new RuntimeDiagnosticSnapshot(new int[0], new byte[0], new byte[0], new long[0], 0L);

  private final int[] metricIds;
  private final byte[] domains;
  private final byte[] kinds;
  private final long[] values;
  private final long epoch;

  RuntimeDiagnosticSnapshot(int[] metricIds, byte[] domains, byte[] kinds, long[] values, long epoch) {
    if (metricIds == null || domains == null || kinds == null || values == null
        || metricIds.length != domains.length || metricIds.length != kinds.length
        || metricIds.length != values.length) {
      throw new IllegalArgumentException("Snapshot metadata and values must have matching lengths");
    }
    this.metricIds = metricIds.clone();
    this.domains = domains.clone();
    this.kinds = kinds.clone();
    this.values = values.clone();
    this.epoch = epoch;
  }

  static RuntimeDiagnosticSnapshot empty() {
    return EMPTY;
  }

  /** Returns the number of aggregate observations represented by this snapshot. */
  public int size() {
    return metricIds.length;
  }

  /**
   * Returns the sum of values for a domain and kind. Counters and timers are
   * cumulative; gauges are absolute observations. Timer values are nanoseconds.
   *
   * @throws NullPointerException if {@code domain} or {@code kind} is null
   */
  public long getValue(Domain domain, Kind kind) {
    if (domain == null || kind == null) {
      throw new NullPointerException("domain and kind are required");
    }
    long total = 0L;
    for (int i = 0; i < values.length; i++) {
      if (domains[i] == (byte) domain.ordinal() && kinds[i] == (byte) kind.ordinal()) {
        total += values[i];
      }
    }
    return total;
  }

  /**
   * Returns this snapshot minus {@code previous} for counters and timers.
   * Gauges remain absolute values from this snapshot. Different metric sets or
   * reset epochs cannot be compared.
   *
   * @throws IllegalArgumentException if the metric sets or reset epochs differ
   * @throws NullPointerException if {@code previous} is null
   */
  public RuntimeDiagnosticSnapshot deltaSince(RuntimeDiagnosticSnapshot previous) {
    if (previous == null) {
      throw new NullPointerException("previous snapshot is required");
    }
    if (epoch != previous.epoch || metricIds.length != previous.metricIds.length) {
      throw new IllegalArgumentException("Snapshots have incompatible metric sets or epochs");
    }
    long[] delta = new long[values.length];
    for (int i = 0; i < values.length; i++) {
      if (metricIds[i] != previous.metricIds[i] || domains[i] != previous.domains[i]
          || kinds[i] != previous.kinds[i]) {
        throw new IllegalArgumentException("Snapshots have incompatible metric sets or epochs");
      }
      Kind kind = Kind.values()[kinds[i]];
      delta[i] = kind == Kind.GAUGE ? values[i] : values[i] - previous.values[i];
    }
    return new RuntimeDiagnosticSnapshot(metricIds, domains, kinds, delta, epoch);
  }
}
