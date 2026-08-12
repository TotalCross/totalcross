#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Static producer-to-consumer contract for the reusable build workflow."""

import pathlib
import re
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "build.yml"


class NativeDependencyWorkflowTests(unittest.TestCase):
    def test_downstream_jobs_use_the_checkout_source_artifact_not_cache(self):
        workflow = WORKFLOW.read_text()
        self.assertEqual(7, workflow.count("name: prepared-native-dependencies\n"))
        self.assertEqual(6, workflow.count("name: prepared-native-dependencies\n", workflow.index("build-linux:")))
        self.assertEqual(6, workflow.count("tar -xzf depot-tools-native-dependencies.tar.gz"))
        self.assertNotIn("- name: Restore depot tools cache", workflow)
        producer = workflow[workflow.index("checkout-source:"):workflow.index("build-linux:")]
        self.assertIn("uses: actions/cache@v5", producer)
        self.assertIn("scripts/package-native-dependencies.py", producer)
        self.assertLess(
            producer.index("scripts/prepare-native-dependencies.sh"),
            producer.index("name: prepared-native-dependencies"),
        )
        self.assertEqual(7, len(re.findall(r"^  build-[^:]+:\s*$", workflow, re.MULTILINE)))


if __name__ == "__main__":
    unittest.main()
