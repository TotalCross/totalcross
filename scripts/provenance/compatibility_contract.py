#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Strict reader for the repository's artifact compatibility registry."""

import re


COMPATIBILITY_KEYS = (
    "schemaVersion",
    "tczFormat",
    "converterAbi",
    "runtimeAbi",
)
_DECIMAL_INTEGER = re.compile(r"[0-9]+\Z")


def parse_compatibility_contract(text, source="compatibility contract"):
    """Parse and validate the complete compatibility registry."""
    values = {}
    recognized = set(COMPATIBILITY_KEYS)

    for line_number, raw in enumerate(text.splitlines(), 1):
        if not raw.strip() or raw.lstrip().startswith("#"):
            continue
        if raw != raw.strip() or raw.count("=") != 1:
            raise ValueError(
                "invalid compatibility syntax at {0}:{1}: {2!r}".format(
                    source, line_number, raw
                )
            )
        key, value = raw.split("=", 1)
        if not key or key != key.strip() or not value:
            raise ValueError(
                "invalid compatibility syntax at {0}:{1}: {2!r}".format(
                    source, line_number, raw
                )
            )
        if key not in recognized:
            raise ValueError(
                "unknown compatibility key at {0}:{1}: {2!r}".format(
                    source, line_number, key
                )
            )
        if key in values:
            raise ValueError(
                "duplicate compatibility key at {0}:{1}: {2!r}".format(
                    source, line_number, key
                )
            )
        if _DECIMAL_INTEGER.fullmatch(value) is None:
            raise ValueError(
                "compatibility value must be a decimal integer at {0}:{1}: {2!r}".format(
                    source, line_number, value
                )
            )
        values[key] = int(value, 10)

    missing = recognized.difference(values)
    if missing:
        raise ValueError(
            "missing compatibility keys in {0}: {1}".format(
                source, ", ".join(sorted(missing))
            )
        )

    if values["schemaVersion"] != 1:
        raise ValueError(
            "unsupported compatibility schemaVersion={0}".format(
                values["schemaVersion"]
            )
        )
    for key in ("tczFormat", "converterAbi", "runtimeAbi"):
        if values[key] <= 0:
            raise ValueError(
                "compatibility {0} must be a positive integer".format(key)
            )

    return values


def read_compatibility_contract(path):
    """Read and validate the compatibility registry at ``path``."""
    with open(str(path), "r", encoding="utf-8") as source_file:
        text = source_file.read()
    return parse_compatibility_contract(text, str(path))
