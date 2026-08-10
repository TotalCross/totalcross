#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Compatibility wrapper for header-only source validation."""
from __future__ import annotations

import os
import sys
from pathlib import Path

target = Path(__file__).with_name("validate-source-files.py")
os.execv(sys.executable, [sys.executable, str(target), "--check", "headers", *sys.argv[1:]])
