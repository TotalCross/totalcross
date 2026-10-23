#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Contract tests for the Xcode build diagnostic parser."""

import importlib.util
from pathlib import Path
import unittest

SUT = Path(__file__).with_name("summarize-ios-xcode-build.py")
spec = importlib.util.spec_from_file_location("ios_build_diagnostics", SUT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class XcodeSummaryTests(unittest.TestCase):
    def test_groups_operations_and_preserves_timing(self):
        raw = "\n".join([
            "CompileC /a/A.o A.m (in target 'FirebaseMessaging' from project 'Pods')",
            "CompileC /a/B.o B.c (in target 'tcvm' from project 'tcvm')",
            "CompileC /a/C.o C.m (in target 'TotalCross' from project 'TotalCross')",
            "CompileC /a/D.o D.c",
            "Build Timing Summary",
            "CompileC (4 tasks) | 24.2 seconds",
            "** ARCHIVE SUCCEEDED **",
            "After archive should not appear",
        ])
        summary = module.summarize(raw)
        for group in ("TCVM", "CocoaPods", "Application/other", "Unattributed"):
            self.assertIn(f"| {group} | 1 |", summary)
        self.assertIn("| **Total** | **4** |", summary)
        self.assertIn("CompileC (4 tasks) | 24.2 seconds", summary)
        self.assertNotIn("After archive should not appear", summary)

    def test_failed_build_without_timing(self):
        summary = module.summarize("CompileC /a/x.o x.c\n** ARCHIVE FAILED **")
        self.assertIn("| Unattributed | 1 |", summary)
        self.assertIn("Not emitted", summary)

    def test_archive_elapsed_from_xcresult(self):
        summary = module.summarize("", {"startTime": 5.25, "endTime": 25.75})
        self.assertIn("20.5 seconds", summary)

    def test_empty_log(self):
        summary = module.summarize("")
        self.assertIn("| **Total** | **0** |", summary)
        self.assertIn("Not emitted", summary)


if __name__ == "__main__":
    unittest.main()
