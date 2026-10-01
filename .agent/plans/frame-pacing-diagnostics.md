<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame Pacing Diagnostics

## Context

Flick currently advances scrolling from a relative TimerEvent. Its public
default frame rate is 40 fps, which requests a nominal interval of
1000 / frameRate milliseconds. Motion elapsed time comes from the VM
millisecond clock. RuntimeDiagnostics groups optional observations by domain,
with RUNTIME and IMAGE as its existing domains.

This design adds a shared Flick advancement body, an internal alternate driver
for deterministic comparison, and bounded aggregate observations in a new
SCHEDULING domain.

## Objectives

- Preserve existing Flick motion, callback, and completion behavior.
- Make TimerEvent and the internal UpdateListener driver call one advancement
  implementation.
- Keep TimerEvent as the production default at 40 fps.
- Measure callback counts, advancement work, and positive lateness only when
  SCHEDULING diagnostics are enabled.
- Verify behavioral parity with deterministic time and UI fixtures.
- Keep scheduler, event-loop, and native event polling behavior unchanged.

## Scope

The change is limited to Flick, the existing RuntimeDiagnostics snapshot and
support code, focused tests, and a diagnostic smoke workload. RuntimeDiagnostics
continues to expose aggregate snapshots through its existing API. Metric keys,
IDs, and recording hooks remain internal.

The work does not change MainWindow dispatch, TimerEvent scheduling, native
event loops, SDL polling, or global performance settings.

## Flick advancement architecture

Move the current Flick callback body into one authoritative advancement
operation. It reads elapsed milliseconds, applies the current motion equation
and scroll limits, sends deltas to listeners and the target, updates page
position, and evaluates the existing completion conditions.

Both driver adapters call this operation. Keep target refusal, listener
notifications, completion, and event consumption behavior consistent with the
current Flick implementation.

A package-private clock seam supplies deterministic millisecond samples to
tests. Production animation uses the VM clock.

## Driver semantics

The public Flick constructor selects TimerEvent. That driver keeps the relative
interval 1000 / frameRate milliseconds and retains existing timer registration,
removal, abort, and consumed-event behavior.

A package-private UpdateListener mode exists for parity tests and the diagnostic
workload. It registers one listener with the MainWindow captured at start,
does not also arm a TimerEvent, and unregisters from that same window at stop.
The callback elapsed argument does not replace Flick's millisecond motion
clock. MainWindow's existing update interval remains in control of callback
delivery.

## Diagnostic clock semantics

Vm.getTimeStamp remains the source for motion and animation completion. The
injected clock is test-only.

System.nanoTime is used only for SCHEDULING observations and every read is
preceded by the SCHEDULING-enabled gate. Diagnostic timestamps track expected
callback timing and measure callback lateness and the shared advancement body.
They do not drive callbacks, motion, or completion. Negative lateness is
reported as zero. No per-frame samples are retained in production. Flick's
diagnostic timing lives in a lazily created holder that remains absent when
SCHEDULING is off at start. Disabled callbacks do not read or mutate that
holder, allocate diagnostic state, read the diagnostic clock, synchronize, or
record diagnostics. A domain activation generation lets the first callback after
an enabled-to-disabled-to-enabled transition restart its timing window without
counting the disabled interval as lateness.

## SCHEDULING aggregates

Keep exactly five private cumulative aggregates:

- callback count — COUNTER
- advancement count — COUNTER
- completion count — COUNTER
- advancement work in nanoseconds — TIMER
- positive lateness in nanoseconds — TIMER

Use private IDs 0x4001 through 0x4005 when collision-free; if another domain
uses that range, preserve existing IDs and move the complete SCHEDULING range
to the next free private range. Preserve all existing domain ordinals and
append SCHEDULING after existing domains.

Snapshots expose totals by domain and kind, not metric IDs or names. Reset and
delta operations include these aggregates while preserving isolation between
domains.

## Compatibility constraints

- Keep TimerEvent as the production driver and retain the 40 fps default.
- Keep the nominal relative interval and integer-millisecond motion behavior.
- Preserve the existing RUNTIME and IMAGE domains and all their metrics.
- Add no public pacing selector, recording method, or metric ID.
- Do not change timer traversal, event consumption, update-listener ordering,
  scheduler behavior, sleeps, yields, or native polling.
- Add no absolute-deadline scheduler, catch-up policy, histogram, trace, or
  production frame-sample buffer.

## Validation

Use deterministic tests to compare both drivers for vertical and horizontal
motion, scroll limits, target refusal, elapsed-time completion, page position,
listener deltas, pen-down stop, UIRobot abort, repeated start/stop, cleanup, and
single-driver registration.

Test default-off diagnostics, absent Flick timing state while disabled, enabled
counter and timer updates, active Flick disable/re-enable timing reset, reset
and delta behavior, isolation from RUNTIME and IMAGE, compile-out behavior, and
converter metadata for System.nanoTime.

Run the complete SDK test suite with diagnostics disabled and enabled. Run
artifactContentTest and dist -x test with diagnostics disabled, plus focused
diagnostics-on artifact and compile-surface checks. Confirm application-facing
artifacts contain RuntimeDiagnostics, RuntimeDiagnosticSnapshot, and
Domain.SCHEDULING without exposing internal recording hooks or metric IDs.

The deployed macOS workload checks default TimerEvent at 40 fps, TimerEvent at
60 fps, the UpdateListener observation case, nonzero enabled aggregates, and
the same final state with diagnostics disabled. Timing output is observational;
it is not an FPS threshold.

## Risks and tradeoffs

UpdateListener cadence depends on MainWindow's existing update interval, so its
selected cadence is a workload label rather than a production guarantee.
Synchronous listener and target work is included in advancement timing.
Diagnostics add clock reads and aggregation only while their domain is enabled.

## Out of scope

Changing production frame pacing, aligning callbacks with display refresh,
changing sleeps or polling, adding native blocking waits, changing SDL behavior,
or adding production percentiles, histograms, traces, and frame histories.
