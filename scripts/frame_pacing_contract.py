# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Shared stage configurations and evidence schema for frame-pacing runs."""

from dataclasses import dataclass
from typing import Optional


EXPECTED_IMAGES = 663
EXPECTED_JPEG_PAYLOADS = 660
EXPECTED_PNG_PAYLOADS = 3
EXPECTED_MASK = 6
ROUNDS = 3
PROCESS_TIMEOUT_SECONDS = 180
SCREEN_SPEC = "-1,-1,540,960"
FIXTURE = "ImageScrollRealWorkloadBenchmarkApp"
FNV_OFFSET = 0xCBF29CE484222325
FNV_PRIME = 0x100000001B3
FRAME_FIELDS = (
    "scroll_jpeg_decode_count", "scroll_image_materializations",
    "scroll_native_geometry_materializations",
)
SUMMARY_FIELDS = (
    "frameCount", "durationNs", "frameTimeP50Ns", "frameTimeP95Ns",
    "frameTimeP99Ns", "frameTimeMaxNs", "workTimeP50Ns", "workTimeP95Ns",
    "workTimeP99Ns", "workTimeMaxNs", "paintTimeP50Ns", "paintTimeP95Ns",
    "paintTimeP99Ns", "paintTimeMaxNs", "framesOver16_67Count",
    "framesOver20Count", "framesOver25Count", "framesOver33_3Count",
    "framesOver50Count", "framesOver100Count",
)
PACING_SUMMARY_FIELDS = (
    "sleepRequestCount", "totalRequestedSleepNs", "totalActualSleepNs",
    "sleepOvershootP50Ns", "sleepOvershootP95Ns", "sleepOvershootP99Ns",
    "sleepOvershootMaxNs", "deadlineErrorP50Ns", "deadlineErrorP95Ns",
    "deadlineErrorP99Ns", "deadlineErrorMaxNs",
)
CSV_FIELDS = (
    "stage", "configuration", "sample", "sourceCommit", "runtimeIdentity",
    "frameCount", "callbackCount", "frameIntervalP50Ns", "frameIntervalP95Ns",
    "frameIntervalP99Ns", "frameIntervalMaxNs", "activeWorkP50Ns",
    "activeWorkP95Ns", "activeWorkP99Ns", "activeWorkMaxNs", "paintP50Ns",
    "paintP95Ns", "paintP99Ns", "paintMaxNs", "framesOver16_67Count",
    "framesOver20Count", "framesOver25Count", "framesOver33_3Count",
    "framesOver50Count", "framesOver100Count", "callbackDeltaP50Ns",
    "callbackDeltaP95Ns", "callbackDeltaP99Ns", "callbackDeltaMaxNs",
    "callbackAbsoluteLatenessP50Ns", "callbackAbsoluteLatenessP95Ns",
    "callbackAbsoluteLatenessP99Ns", "callbackAbsoluteLatenessMaxNs",
    "callbackDeltaErrorP50Ns", "callbackDeltaErrorP95Ns",
    "callbackDeltaErrorP99Ns", "callbackDeltaErrorMaxNs",
    "measuredWallDurationNs", "processWallNs", "driver", "timerFps", "clock",
    "timerDeadlinePolicy", "eventLoopPolicy", "yieldPolicy",
    "prefetchThreadMode", "prefetchWorkerSleepMs",
    "expectedCallbackIntervalNs", "syntheticPacingProfile",
    "syntheticPacingIntervalNs", "sleepRequestCount", "totalRequestedSleepNs",
    "totalActualSleepNs", "sleepOvershootP50Ns", "sleepOvershootP95Ns",
    "sleepOvershootP99Ns", "sleepOvershootMaxNs", "deadlineErrorP50Ns",
    "deadlineErrorP95Ns", "deadlineErrorP99Ns", "deadlineErrorMaxNs",
)


@dataclass(frozen=True)
class Configuration:
    name: str
    driver: str
    timer_fps: Optional[int]
    clock: str
    timer_deadline_policy: str
    event_loop_policy: str
    yield_policy: str
    expected_callback_interval_ns: Optional[int]
    synthetic_pacing_profile: Optional[str] = None

    def app_arguments(self):
        if self.synthetic_pacing_profile is not None:
            return [f"--synthetic-pacing={self.synthetic_pacing_profile}"]
        args = [f"--flick-driver={self.driver}", f"--flick-clock={self.clock}"]
        if self.timer_fps is not None:
            args.append(f"--flick-fps={self.timer_fps}")
        return args

    def metadata(self):
        return {
            "driver": self.driver,
            "timerFps": self.timer_fps,
            "clock": self.clock,
            "timerDeadlinePolicy": self.timer_deadline_policy,
            "eventLoopPolicy": self.event_loop_policy,
            "yieldPolicy": self.yield_policy,
            "expectedCallbackIntervalNs": self.expected_callback_interval_ns,
        }


STAGES = {
    1: (
        Configuration("synthetic-current-16ms", "synthetic", None, "monotonic",
                      "absolute-from-start", "benchmark-loop", "none", None,
                      "synthetic-current-16ms"),
        Configuration("synthetic-60hz", "synthetic", None, "monotonic",
                      "absolute-from-start", "benchmark-loop", "none", None,
                      "synthetic-60hz"),
    ),
    2: (
        Configuration("timer-40-millis", "timer", 40, "millis", "native-relative",
                      "timer-events", "none", 25_000_000),
        Configuration("timer-60-millis", "timer", 60, "millis", "native-relative",
                      "timer-events", "none", 16_000_000),
        Configuration("update-millis", "update", None, "millis", "not-applicable",
                      "update-listener", "none", 16_000_000),
    ),
    3: (
        Configuration("timer-60-millis", "timer", 60, "millis", "native-relative",
                      "timer-events", "none", 16_000_000),
        Configuration("timer-60-nano", "timer", 60, "nano", "native-relative",
                      "timer-events", "none", 16_000_000),
        Configuration("update-millis", "update", None, "millis", "not-applicable",
                      "update-listener", "none", 16_000_000),
        Configuration("update-nano", "update", None, "nano", "not-applicable",
                      "update-listener", "none", 16_000_000),
    ),
}


class BenchmarkFailure(RuntimeError):
    pass


def require(condition, message):
    if not condition:
        raise BenchmarkFailure(message)
