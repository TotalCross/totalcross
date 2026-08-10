# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Focused unit tests for source-size policy boundaries and eligibility."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1]
if str(SCRIPTS) not in sys.path:
    sys.path.insert(0, str(SCRIPTS))

from source_validation.size import (  # noqa: E402
    LEGACY_GROWTH_TOLERANCE,
    SOURCE_FILE_LIMIT,
    Outcome,
    evaluate,
    should_check,
)


class SizePolicyTest(unittest.TestCase):
    def assert_outcome(self, expected: Outcome, base: int | None, current: int) -> None:
        self.assertIs(evaluate(base, current).outcome, expected)

    def test_new_file_boundaries(self) -> None:
        self.assert_outcome(Outcome.PASS, None, 10 * 1024)
        self.assert_outcome(Outcome.PASS, None, SOURCE_FILE_LIMIT)
        self.assert_outcome(Outcome.FAIL, None, SOURCE_FILE_LIMIT + 1)

    def test_existing_file_cannot_cross_limit(self) -> None:
        self.assert_outcome(Outcome.PASS, 10 * 1024, 15 * 1024)
        self.assert_outcome(Outcome.PASS, 15 * 1024, SOURCE_FILE_LIMIT)
        self.assert_outcome(Outcome.FAIL, 15 * 1024, SOURCE_FILE_LIMIT + 1)

    def test_oversized_file_may_shrink_or_remain_unchanged(self) -> None:
        self.assert_outcome(Outcome.PASS, 30 * 1024, 29 * 1024)
        self.assert_outcome(Outcome.PASS, 30 * 1024, 30 * 1024)
        self.assert_outcome(Outcome.PASS, 100 * 1024, 98 * 1024)

    def test_legacy_growth_tolerance_boundaries(self) -> None:
        base = 30 * 1024
        self.assert_outcome(Outcome.WARNING, base, base + 1)
        self.assert_outcome(Outcome.WARNING, base, base + LEGACY_GROWTH_TOLERANCE)
        self.assert_outcome(Outcome.FAIL, base, base + LEGACY_GROWTH_TOLERANCE + 1)


class EligibilityTest(unittest.TestCase):
    def test_includes_maintained_source_and_build_languages(self) -> None:
        for path in (
            "src/Main.java", "src/native.mm", "scripts/check.py",
            "web/app.tsx", "module/CMakeLists.txt", "tools/build.gradle",
        ):
            with self.subTest(path=path):
                self.assertTrue(should_check(path))

    def test_excludes_non_source_and_repository_exempt_paths(self) -> None:
        for path in (
            "README.md", "TotalCrossVM/deps/tool/build.py",
            "TotalCrossVM/third_party/zlib/zlib.c", "module/build/output.java",
            "module/.gradle/cache.gradle", "module/generated/Registry.java",
            "legal/copyright-provenance/audits/id/report.py",
            "TotalCrossSDK/src/main/java/totalcross/util/regex/Pattern.java",
        ):
            with self.subTest(path=path):
                self.assertFalse(should_check(path))


if __name__ == "__main__":
    unittest.main()
