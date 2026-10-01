<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame Pacing Diagnostics — Implementation Report

## Summary

Flick uses a shared advancement body for its production TimerEvent driver and
an internal UpdateListener diagnostic driver. Optional SCHEDULING diagnostics
collect five bounded aggregate values. Motion remains on the VM millisecond
clock, and production scheduling behavior is unchanged.

## Final Flick advancement architecture

Both callbacks reach the same advancement operation. It applies the existing
motion equation and scroll limits, notifies listeners, requests target
scrolling, updates page position, and checks the existing completion
conditions.

The TimerEvent adapter validates Flick's active timer, preserves UIRobot abort
behavior, calls the shared body, and consumes the event. The UpdateListener
adapter calls the same body; its elapsed argument does not drive motion.

## Driver semantics

The public constructor selects TimerEvent. Start registers the timer using
1000 / frameRate milliseconds, and stop removes it. The internal
UpdateListener driver registers with the MainWindow captured at start, does
not arm a TimerEvent, and unregisters from that same window at stop.

Deterministic tests use a package-private millisecond clock. Production clock
reads remain backed by Vm.getTimeStamp.

## Production defaults

TimerEvent remains the production driver and Flick.defaultFrameRate remains
40 fps, with a nominal 25 ms interval. The diagnostic 60 fps case uses the
existing integer interval of 16 ms. P11 adds no public pacing selector or
global performance setting.

## SCHEDULING diagnostics

The domain order is RUNTIME, IMAGE, SCHEDULING. Existing domain ordinals and
RUNTIME/IMAGE metrics are preserved. SCHEDULING contains five private
aggregates with private IDs 0x4001 through 0x4005: callback count,
advancement count, completion count, cumulative advancement work, and
cumulative positive lateness. The three counts are COUNTER values; work and
lateness are TIMER values in nanoseconds.

Snapshots report enabled aggregate totals by domain and kind. No metric names
or IDs are exposed. There are no production per-frame samples, histograms,
percentiles, or traces.

## Clock semantics

Animation and drag timing continue to use Vm.getTimeStamp and integer
milliseconds. System.nanoTime is read only when SCHEDULING diagnostics are
enabled, to observe callback lateness and time the shared advancement body.
These readings do not affect the motion equation, completion checks, or
callback scheduling. Flick holds this timing state in a lazily created
diagnostics-only holder. When SCHEDULING is disabled, P11 callback handling
does not read or mutate the holder, allocate diagnostic state, read
System.nanoTime, synchronize, or record metrics. If the domain is disabled and
enabled again during a Flick, the first resumed callback starts a fresh timing
window, so the disabled interval is not reported as lateness.

## Sleep and polling audit

P11 leaves the existing event-loop and polling behavior unchanged. The native
event pump retains its 1 ms wait where configured and its existing platform
exception; SDL continues nonblocking polling. The simulator continues polling
its queue with its existing bounded wait. MainWindow timer traversal and
UpdateListener dispatch ordering are unchanged. Existing main-thread yields,
startup yields, and modal-alert waits are unchanged.

These mechanisms can affect callback arrival time; the diagnostic values
describe observations under the existing behavior.

## Deterministic validation

The focused Flick coverage compares vertical and horizontal motion across
drivers, scroll limits, refused targets, completion, page position, listener
deltas, pen-down stop, UIRobot abort, repeated start/stop, cleanup, and one
active driver. It also verifies that disabled Flicks have no timing holder,
that active disablement leaves that holder untouched, and that re-enabling
starts a fresh timing window without changing motion. Diagnostics coverage
checks enabled aggregates, reset and delta behavior, domain isolation, and
compile-out gates. Converter checks cover diagnostics metadata and ensure the
disabled path reaches normal advancement without diagnostic state access.

The full SDK test suite passes with diagnostics disabled and enabled. The
diagnostics-off artifactContentTest and dist -x test tasks pass. Focused
diagnostics-on artifact and compile-surface checks confirm that
RuntimeDiagnostics, RuntimeDiagnosticSnapshot, and Domain.SCHEDULING are
available without exposing recording hooks or metric IDs.

## macOS measurement evidence

The deployed workload completed the default TimerEvent/40 fps baseline, two
diagnostics-on observations for each requested case, and a diagnostics-off
repeat. Every run completed once with final scroll position 0. Enabled runs
recorded nonzero aggregates.

| Driver and selected cadence | Callbacks per run | Duration | Average advancement work | Average positive lateness |
| --- | ---: | ---: | ---: | ---: |
| TimerEvent, 40 fps / 25 ms | 3 | 75–76 ms | 5.180–5.889 µs | 0–0.167 ms |
| TimerEvent, 60 fps / 16 ms | 4 | 64–65 ms | 4.062–4.718 µs | 0.062–1.026 ms |
| UpdateListener observation, selected 25 ms | 4 | 65–66 ms | 5.042–5.875 µs | 0 ms |

All enabled cases recorded callbacks, advancement work, and completion.
TimerEvent runs recorded positive lateness in at least one repetition for
both selected cadences; one repetition in each pair recorded zero. The
UpdateListener observations recorded zero positive lateness against their
selected cadence. These short macOS measurements are observations, not FPS
targets or Windows performance claims.

## Compatibility

Applications that make no new calls continue to use TimerEvent at 40 fps and
the same millisecond animation clock. The alternate driver and deterministic
clock are package-private. The supported RuntimeDiagnostics surface remains
isSupported, setDomainEnabled, and snapshot; recording uses the existing
feature bridge.

## Known limitations

The workload exercises a short controlled scroll. UpdateListener cadence is
governed by MainWindow's existing update interval and is not guaranteed by the
selected observation setting. The measurements do not characterize sustained
interaction, display refresh alignment, or platforms other than the tested
macOS configuration.

## Deferred work

Absolute-deadline or catch-up scheduling, refresh/vsync pacing, changes to
sleep or polling behavior, native blocking waits, SDL polling changes, and
production percentile, histogram, trace, or frame-history diagnostics remain
out of scope.
