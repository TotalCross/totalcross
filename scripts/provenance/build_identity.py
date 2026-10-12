#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import hashlib
import json
import re
from urllib.parse import urlsplit


IDENTITY_VERSION = 1
_GIT_OBJECT_ID = re.compile(r"[0-9a-f]{40}|[0-9a-f]{64}")


def normalize_repository(value):
    """Return a stable, credential-free repository identity."""
    if not value:
        return None

    value = value.strip()
    if not value:
        return None

    host = None
    path = value
    if "://" in value:
        parsed = urlsplit(value)
        host = parsed.hostname
        path = parsed.path
    elif value.startswith("git@") and ":" in value:
        authority, path = value.split(":", 1)
        host = authority.split("@", 1)[1]

    path = path.strip("/")
    if path.endswith(".git"):
        path = path[:-4]
    if not path:
        return None

    if host and host.casefold() != "github.com":
        path = f"{host.casefold()}/{path}"
    return path.casefold()


def is_git_object_id(value):
    return isinstance(value, str) and _GIT_OBJECT_ID.fullmatch(value) is not None


def compute_build_id(repository, source_tree, identity_version=IDENTITY_VERSION):
    normalized_repository = normalize_repository(repository)
    if normalized_repository is None:
        raise ValueError("repository identity is missing")
    if not is_git_object_id(source_tree):
        raise ValueError("source tree must be a 40- or 64-character Git object ID")
    if type(identity_version) is not int or identity_version != IDENTITY_VERSION:
        raise ValueError(f"unsupported build identity version: {identity_version!r}")

    canonical = json.dumps(
        {
            "identityVersion": identity_version,
            "repository": normalized_repository,
            "sourceTree": source_tree,
        },
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()
